/*
 * ChAbstractPositionReadMap.java
 *
 * Created on 3. Juli 2001, 13:24
 */

package chesspresso.position.map;

import chesspresso.*;
import chesspresso.position.*;
import chesspresso.game.*;
import chesspresso.move.*;
import java.util.*;
import java.io.*;

/**
 *
 * @author  BerniMan
 * @version 
 */
public abstract class ChAbstractPositionReadMap implements PositionReadMap
{
    
    public interface PositionDataListener
    {
        public void notifyPositionData(PositionData data, PositionData parentData, Position pos, double value);
    }
    
    /*================================================================================*/
    
    private class GameModelListModel extends javax.swing.AbstractListModel
    {
        public int getSize() {return getNumOfGames();}        
        public Object getElementAt(int index) {return getGameModel(index);}
    }
    
    
    protected GameModelListModel m_gameModelListModel;

    public javax.swing.ListModel getGameModelListModel()
    {
        if (m_gameModelListModel == null) m_gameModelListModel = new GameModelListModel();
        return m_gameModelListModel;
    }
    
    /*================================================================================*/
    
    public PositionData getData(ImmutablePosition pos)
    {
        return getData(pos.getHashCode());
    }
    
    /*================================================================================*/
    
    public static interface IChPositionSelector
    {
        public boolean isSelected(ImmutablePosition position);
        public boolean continueRecursively(ImmutablePosition position);
        public String toString();
    }
    
    public IChPositionSelector[] getAllSelectors()
    {
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        
        return new IChPositionSelector[] {
            new ChNoveltySelector(thisYear,      thisYear),
            new ChNoveltySelector(thisYear - 5,  thisYear),
            new ChNoveltySelector(thisYear - 10, thisYear),
        };
    }

    public class ChNoveltySelector implements IChPositionSelector
    {
        private int m_from;
        private int m_to;
        
        public ChNoveltySelector(int from, int to)
        {
            m_from = from;
            m_to = to;
        }
            
        public boolean isSelected(ImmutablePosition position)
        {
            PositionData data = getData(position);
            return data != null && data.getFirstOccurrence() >= m_from && data.getFirstOccurrence() <= m_to;
        }
        
        public boolean continueRecursively(ImmutablePosition position)
        {
            PositionData data = getData(position);
            return data != null && data.getFirstOccurrence() <= m_from;
        }
        
        public String toString() {return "Novelties from " + m_from + " to " + m_to;}
    }
    
    /*================================================================================*/
    
    public static interface IChPositionEvaluator
    {
        public double eval(ImmutablePosition position);
        public String toString();
    }
       
    public IChPositionEvaluator[] getAllEvaluators()
    {
        return new IChPositionEvaluator[] {
            new ChNumOfGamesEvaluator(),
            new ChResultEvaluator(),
            new ChWeightedResultEvaluator(),
            new ChEloEvaluator(),
            new ChDeltaPerformanceEvaluator(),
            
            new ChEntropyEvaluator(new ChNumOfGamesEvaluator()),
            new ChEntropyEvaluator(new ChResultEvaluator()),
            new ChEntropyEvaluator(new ChWeightedResultEvaluator()),
            new ChEntropyEvaluator(new ChEloEvaluator()),
            new ChEntropyEvaluator(new ChDeltaPerformanceEvaluator())
        };
    }
    
    public ChNumOfGamesEvaluator getNumOfGamesEvaluator() {return new ChNumOfGamesEvaluator();}
    public ChResultEvaluator getResultEvaluator() {return new ChResultEvaluator();}
    public ChWeightedResultEvaluator getWeightedResultEvaluator() {return new ChWeightedResultEvaluator();}
    public ChEloEvaluator getEloEvaluator() {return new ChEloEvaluator();}
    public ChDeltaPerformanceEvaluator getDeltaPerformanceEvaluator() {return new ChDeltaPerformanceEvaluator();}            
    public ChEntropyEvaluator getEntropyEvaluator(IChPositionEvaluator e) {return new ChEntropyEvaluator(e);}

    /*================================================================================*/
    
    public class ChNumOfGamesEvaluator implements IChPositionEvaluator
    {
        public double eval(ImmutablePosition position)
        {
            PositionData data = getData(position);
            return data == null ? 0.0 : (double)data.getNumOfGames();
        }
        public String toString() {return "Number of games";}
    }
    
    public class ChResultEvaluator implements IChPositionEvaluator
    {
        public double eval(ImmutablePosition position)
        {
            PositionData data = getData(position);
            return data == null ? 0.0 : (double)data.getWhiteResult();
        }
        public String toString() {return "Result";}
    }
    
    public class ChWeightedResultEvaluator implements IChPositionEvaluator
    {
        public double eval(ImmutablePosition position)
        {
            PositionData data = getData(position);
            return data == null ? 0.0 : (double)data.getWhiteResult() * data.getNumOfGames();
        }
        public String toString() {return "Weighted result";}
    }
    
    public class ChEloEvaluator implements IChPositionEvaluator
    {
        public double eval(ImmutablePosition position)
        {
            PositionData data = getData(position);
            return data == null ? 0.0 : (double)data.getEloAverage(Chess.otherPlayer(data.getToPlay()));
        }
        public String toString() {return "Elo";}
    }
    
    public class ChDeltaPerformanceEvaluator implements IChPositionEvaluator
    {
        public double eval(ImmutablePosition position)
        {
            PositionData data = getData(position);
            int movingPlayer = Chess.otherPlayer(position.getToPlay());
            return data == null ? 0.0 : (double)data.getResult(movingPlayer) - data.getExpectation(movingPlayer);
        }
        public String toString() {return "Performance delta";}
    }
    
    public class ChEntropyEvaluator implements IChPositionEvaluator
    {
        private IChPositionEvaluator m_evaluator;
        
        public ChEntropyEvaluator(IChPositionEvaluator evaluator)
        {
            m_evaluator = evaluator;
        }
        
        public double eval(ImmutablePosition position)
        {
            Position pos = new Position(position);
            short[] moves = pos.getAllMoves();
            double[] values = new double[moves.length];
            double sum = 0;
            for (int i=0; i<moves.length; i++) {
                try {
                    pos.doMove(moves[i]);
                    values[i] = m_evaluator.eval(pos);
                    sum += values[i];
                    pos.undoMove();
                } catch (IllegalMoveException ex) {
                    ex.printStackTrace();
                }
            }
            double entropy = 0;
            for (int i=0; i<moves.length; i++) {
                if (values[i] != 0) entropy -= values[i] / sum * Math.log(values[i] / sum);
            }
            return entropy;
        }
        
        public String toString() {return m_evaluator.toString() + " entropy";}
    }
    
    /*================================================================================*/
    
    private final static double VALUE_IGNORE_RES = 2;
    private final static double VALUE_IGNORE_PERF = 2;
    
    private double getDrawValue(PositionData data, int toPlay, boolean takePerformance)
    {
        if (takePerformance) {
            return (data == null ? 0 : 0.5 - data.getExpectation(toPlay));
        } else {
            return 0.5;
        }
    }
    
    private double getValue(PositionData data, int toPlay, boolean takePerformance)
    {
        if (takePerformance) {
            return (data == null) ? VALUE_IGNORE_PERF : data.getResult(toPlay) - data.getExpectation(toPlay);
        } else {
            return (data == null) ? VALUE_IGNORE_RES : data.getResult(toPlay);
        }
    }
    
    private double getMiniMax(Position pos, double alpha, double beta, int depth, int minGames, boolean takePerformance, int outOfMap, long[] path, short[][] bestLine)
    {
        PositionData data = getData(pos);
        int numOfGames = 0;
        if (data != null) {
            if (takePerformance) {
                numOfGames = (data.getWhiteEloGames() + data.getBlackEloGames()) / 2;
            } else {
                numOfGames = data.getNumOfGames();
            }
        }
        
//        if (numOfGames < minGames) return VALUE_IGNORE;  // =====>

        //---------- search for repetitions -----------
        long hashCode = pos.getHashCode();
        if (outOfMap == 0) {
            for (int i=depth; i < path.length; i++) {
                if (hashCode == path[i]) return getDrawValue(data, pos.getToPlay(), takePerformance);  // =====>
            }
        }

        if (depth <= 0) {
            if (outOfMap > 0) return takePerformance ? VALUE_IGNORE_PERF : VALUE_IGNORE_RES;
            return getValue(data, pos.getToPlay(), takePerformance);
        } else {
            if (numOfGames < minGames) outOfMap++; else outOfMap = 0;
            path[depth - 1] = hashCode;
            short[] moves = pos.getAllMoves();
            double bestValue = -9999;
            bestLine[depth-1][0] = 0;
            for (int i = 0; i < moves.length; i++) {
                try {
                    pos.doMove(moves[i]);
                    PositionData moveData = getData(pos);
                    if (moveData != null && (moveData.getNumOfGames() >= minGames || outOfMap < 3)) {
//                        if (depth > 1) java.util.Arrays.fill(bestLine[depth-2], 0);  //TODO necessary?
                        double value = 0;
                        if (takePerformance) {
                            value = -getMiniMax(pos, -beta, -Math.max(bestValue, alpha), depth - 1, minGames, takePerformance, outOfMap, path, bestLine);
                            if (value != -VALUE_IGNORE_PERF && value > bestValue) {
                                bestValue = value;
                                if (depth > 1) System.arraycopy(bestLine[depth-2], 0, bestLine[depth-1], 1, depth - 1);
                                bestLine[depth-1][0] = moves[i];
                            }
                        } else {
                            value = 1 - getMiniMax(pos, 1-beta, 1-Math.max(bestValue, alpha), depth - 1, minGames, takePerformance, outOfMap, path, bestLine);
                            if (value != (1 - VALUE_IGNORE_RES) && value > bestValue) {
                                bestValue = value;
                                if (depth > 1) System.arraycopy(bestLine[depth-2], 0, bestLine[depth-1], 1, depth - 1);
                                bestLine[depth-1][0] = moves[i];
                            }
                        }
                    }
                    pos.undoMove();
                    if (bestValue >= beta) return bestValue;
                } catch (IllegalMoveException ex) {}
            }
            if (bestValue == -9999) {
                if (outOfMap > 0 && depth < bestLine.length) return takePerformance ? VALUE_IGNORE_PERF : VALUE_IGNORE_RES;
                return getValue(data, pos.getToPlay(), takePerformance);
            } else {
                return bestValue;
            }
        }
    }
    
    public final double getMiniMax(Position pos, int depth, int minGames, boolean takePerformance, short[] bestLine)
    {
        short[][] myBestLine = new short[depth][];
        for (int i=0; i<depth; i++) {
            myBestLine[i] = new short[i + 1];
        }
        
        double value = getMiniMax(pos, -9999, 9999, depth, minGames, takePerformance, 0, new long[depth], myBestLine);
        System.arraycopy(myBestLine[depth-1], 0, bestLine, 0, depth);
        return value;
        
//        return getMiniMax(pos, depth, 0, new long[depth], bestLine);
    }
    
    /*================================================================================*/
    
//    private void findNovelties(ChPosition pos, int fromYear, int toYear, PositionDataListener listener, IChPositionData parentData, ChLongHashSet hashSet)
//    {
////        System.out.print("(");
//        
//        //---------- search for repetitions -----------
//        if (hashSet.contains(pos.getHashCode())) return;  // =====>
//        
//        //---------- get data -----------
//        IChPositionData data = getData(pos);
//        if (data == null) return;  // =====>
//        if (data.getNumOfGames() <= 5) return;  // =====>
//        if (data.getFirstOccurrence() > toYear) return;  // =====>
//        
//        hashSet.add(pos.getHashCode());
////        System.out.println(data);
//    
////        int depth=0;
////        while (depth < path.length && path[depth] != 0) {
////            if (hashCode == path[depth]) return;  // =====>
////            depth++;
////        }
////        path[depth] = hashCode;
//        
//        //---------- is it a novelty -----------
//        int year = data.getFirstOccurrence();
//        if (year >= fromYear && year <= toYear && parentData != null && parentData.getFirstOccurrence() < year) {
//            listener.notifyPositionData(data, parentData, pos, parentData.getFirstOccurrence());
//        }
//        
//        short[] moves = data.getPlayedMoves();
//        for (int i = 0; i < moves.length; i++) {
//            try {
//                pos.doMove(moves[i]);
//                findNovelties(pos, fromYear, toYear, listener, data, hashSet);
//                pos.undoMove();
//            } catch (ChIllegalMoveException ex) {}
//        }
//        
////        System.out.print(")");
////        path[depth] = 0;
//    }
//    
//    public void findNovelties(ChPosition pos, int fromYear, int toYear, PositionDataListener listener)
//    {
//        findNovelties(pos, fromYear, toYear, listener, null, new ChLongHashSet());
//    }
    
    /*================================================================================*/
    
//    private void findHotSpots(ChPosition pos, double[] entropies, IChPositionData[] hotspots, String[] fen, IChPositionData parentData, ChLongHashSet hashSet)
//    {
////        System.out.print("(");
//        
//        //---------- search for repetitions -----------
//        if (hashSet.contains(pos.getHashCode())) return;  // =====>
//        
//        //---------- get data -----------
//        IChPositionData data = getData(pos);
//        if (data == null) return;  // =====>
//        if (data.getNumOfGames() <= 5) return;  // =====>
//        
//        hashSet.add(pos.getHashCode());
//        
//        short[] moves = data.getPlayedMoves();
//        int[] num = new int[moves.length];
//        int numOfNextPos = 0;
//        for (int i = 0; i < moves.length; i++) {
//            try {
//                pos.doMove(moves[i]);
//                IChPositionData d = getData(pos);
//                if (d != null && d.getNumOfGames() >= 5) {
//                    numOfNextPos += d.getNumOfGames();
//                    num[i] = d.getNumOfGames();
//                }
//                findHotSpots(pos, entropies, hotspots, fen, data, hashSet);
//                pos.undoMove();
//            } catch (ChIllegalMoveException ex) {}
//        }
//        double entropy = 0;
//        for (int i = 0; i < num.length; i++) {
//            if (num[i] > 0) {
//                double perc = (double)num[i] / numOfNextPos;
////                System.out.print("(" + num[i] + "," + numOfNextPos + "," + perc + ") ");
//                entropy -= perc * Math.log(perc);
//            }
//        }
////        System.out.println(entropy);
//        
////        int index = Arrays.binarySearch(entropies, entropy);
//        for (int i=0; i<entropies.length; i++) {
//            if (entropies[i] < entropy) {
//                for (int j=entropies.length-1; j>i; j--) {
//                    entropies[j] = entropies[j-1];
//                    hotspots[j] = hotspots[j-1];
//                    fen[j] = fen[j-1];
//                }
//                entropies[i] = entropy;
//                hotspots[i] = data;
//                fen[i] = pos.getFEN();
////                for (int j=0; j<entropies.length; j++) {
////                    System.out.println(j + ": " + entropies[j] + " " + fen[j]);
////                }
////                System.out.println();
//                
//                break;
//            }
//        }
//        
////        System.out.print(")");
////        path[depth] = 0;
//    }
//    
//    public void findHotSpots(ChPosition pos, int numOfHotspots, PositionDataListener listener)
//    {
//        IChPositionData[] hotspots = new IChPositionData[numOfHotspots];
//        double[] entropies = new double[numOfHotspots];
//        String[] fen = new String[numOfHotspots];
//        findHotSpots(pos, entropies, hotspots, fen, null, new ChLongHashSet());
////        for (int i=0; i<numOfHotspots && hotspots[i] != null; i++) {
//        for (int i=0; i<numOfHotspots; i++) {
////            System.out.println(hotspots[i]);
//            listener.notifyPositionData(hotspots[i], null, new ChPosition(fen[i]), entropies[i]);
//        }
//    }
    
    /*================================================================================*/
    
    private void findHotSpots(Position pos, IChPositionSelector selector, PositionDataListener listener, PositionData parentData, ChLongHashSet hashSet)
    {
        //---------- search for repetitions -----------
        if (hashSet.contains(pos.getHashCode())) return;  // =====>
        
        //---------- get data -----------
        PositionData data = getData(pos);
        if (data == null) return;  // =====>
        if (data.getNumOfGames() <= 5) return;  // =====>
        
        hashSet.add(pos.getHashCode());
        
        if (selector.isSelected(pos)) {
            listener.notifyPositionData(data, parentData, pos, 0);
            if (!selector.continueRecursively(pos)) return;  // =====>
        }
        
        short[] moves = data.getPlayedMoves();
        for (int i = 0; i < moves.length; i++) {
            try {
                pos.doMove(moves[i]);
                findHotSpots(pos, selector, listener, data, hashSet);
                pos.undoMove();
            } catch (IllegalMoveException ex) {}
        }
    }
    
    public void findHotSpots(Position pos, IChPositionSelector selector, PositionDataListener listener)
    {
        findHotSpots(pos, selector, listener, null, new ChLongHashSet());
    }
    
    /*================================================================================*/
    
    private void findHotSpots(Position pos, IChPositionEvaluator evaluator, double[] values, PositionData[] hotspots, String[] fen, PositionData parentData, ChLongHashSet hashSet)
    {
//        System.out.print("(");
        
        //---------- search for repetitions -----------
        if (hashSet.contains(pos.getHashCode())) return;  // =====>
        
        //---------- get data -----------
        PositionData data = getData(pos);
        if (data == null) return;  // =====>
        if (data.getNumOfGames() <= 5) return;  // =====>
        
        hashSet.add(pos.getHashCode());
        
        double value = evaluator.eval(pos);
        for (int i = 0; i < values.length; i++) {
            if (values[i] < value) {
                for (int j=values.length-1; j>i; j--) {
                    values[j] = values[j-1];
                    hotspots[j] = hotspots[j-1];
                    fen[j] = fen[j-1];
                }
                values[i] = value;
                hotspots[i] = data;
                fen[i] = pos.getFEN();
                break;
            }
        }
        
        short[] moves = data.getPlayedMoves();
        for (int i = 0; i < moves.length; i++) {
            try {
                pos.doMove(moves[i]);
                findHotSpots(pos, evaluator, values, hotspots, fen, data, hashSet);
                pos.undoMove();
            } catch (IllegalMoveException ex) {}
        }
    }
    
    public void findHotSpots(Position pos, IChPositionEvaluator evaluator, int numOfHotspots, PositionDataListener listener)
    {
        PositionData[] hotspots = new PositionData[numOfHotspots];
        double[] values = new double[numOfHotspots];
        String[] fen = new String[numOfHotspots];
        findHotSpots(pos, evaluator, values, hotspots, fen, null, new ChLongHashSet());
        
        //---------- notify listener ----------
        for (int i=0; i<numOfHotspots; i++) {
            if (fen[i] != null) {
                listener.notifyPositionData(hotspots[i], null, new Position(fen[i]), values[i]);
            }
        }
    }
    
    /*================================================================================*/
    
//    private void findHotSpots2(ChPosition pos, double[] entropies, IChPositionData[] hotspots, String[] fen, IChPositionData parentData, ChLongHashSet hashSet)
//    {
////        System.out.print("(");
//        
//        //---------- search for repetitions -----------
//        if (hashSet.contains(pos.getHashCode())) return;  // =====>
//        
//        //---------- get data -----------
//        IChPositionData data = getData(pos);
//        if (data == null) return;  // =====>
//        if (data.getNumOfGames() <= 5) return;  // =====>
//        
//        hashSet.add(pos.getHashCode());
//        
//        short[] moves = data.getPlayedMoves();
//        double[] res = new double[moves.length];
//        for (int i = 0; i < moves.length; i++) {
//            try {
//                pos.doMove(moves[i]);
//                IChPositionData d = getData(pos);
//                if (d != null && d.getNumOfGames() >= 5) {
//                    res[i] = d.getNumOfGames() * d.getWhiteResult();
//                }
//                findHotSpots2(pos, entropies, hotspots, fen, data, hashSet);
//                pos.undoMove();
//            } catch (ChIllegalMoveException ex) {}
//        }
//        double entropy = 0;
//        for (int i = 0; i < res.length; i++) {
//            if (res[i] > 0) {
//                //TODO should take num of next pos, not num of games
//                double perc = res[i] / (data.getNumOfGames() * data.getWhiteResult());
////                System.out.print("(" + num[i] + "," + numOfNextPos + "," + perc + ") ");
//                entropy -= perc * Math.log(perc);
//            }
//        }
////        System.out.println(entropy);
//        
////        int index = Arrays.binarySearch(entropies, entropy);
//        for (int i=0; i<entropies.length; i++) {
//            if (entropies[i] < entropy) {
//                for (int j=entropies.length-1; j>i; j--) {
//                    entropies[j] = entropies[j-1];
//                    hotspots[j] = hotspots[j-1];
//                    fen[j] = fen[j-1];
//                }
//                entropies[i] = entropy;
//                hotspots[i] = data;
//                fen[i] = pos.getFEN();
////                for (int j=0; j<entropies.length; j++) {
////                    System.out.println(j + ": " + entropies[j] + " " + fen[j]);
////                }
////                System.out.println();
//                
//                break;
//            }
//        }
//        
////        System.out.print(")");
////        path[depth] = 0;
//    }
//    
//    public void findHotSpots2(ChPosition pos, int numOfHotspots, PositionDataListener listener)
//    {
//        IChPositionData[] hotspots = new IChPositionData[numOfHotspots];
//        double[] entropies = new double[numOfHotspots];
//        String[] fen = new String[numOfHotspots];
//        findHotSpots2(pos, entropies, hotspots, fen, null, new ChLongHashSet());
////        for (int i=0; i<numOfHotspots && hotspots[i] != null; i++) {
//        for (int i=0; i<numOfHotspots; i++) {
////            System.out.println(hotspots[i]);
//            listener.notifyPositionData(hotspots[i], null, new ChPosition(fen[i]), 0);
//        }
//    }
    
    /*================================================================================*/
    
//    private void findMostGames(ChPosition pos, int[] games, IChPositionData[] hotspots, String[] fen, IChPositionData parentData, ChLongHashSet hashSet)
//    {
////        System.out.print("(");
//        
//        //---------- search for repetitions -----------
//        if (hashSet.contains(pos.getHashCode())) return;  // =====>
//        
//        //---------- get data -----------
//        IChPositionData data = getData(pos);
//        if (data == null) return;  // =====>
//        if (data.getNumOfGames() <= 5) return;  // =====>
//        
//        hashSet.add(pos.getHashCode());
//        
//        short[] moves = data.getPlayedMoves();
//        for (int i = 0; i < moves.length; i++) {
//            try {
//                pos.doMove(moves[i]);
//                findMostGames(pos, games, hotspots, fen, data, hashSet);
//                pos.undoMove();
//            } catch (ChIllegalMoveException ex) {}
//        }
//
//        for (int i=0; i<games.length; i++) {
//            if (games[i] < data.getNumOfGames()) {
//                for (int j=games.length-1; j>i; j--) {
//                    games[j] = games[j-1];
//                    hotspots[j] = hotspots[j-1];
//                    fen[j] = fen[j-1];
//                }
//                games[i] = data.getNumOfGames();
//                hotspots[i] = data;
//                fen[i] = pos.getFEN();
////                for (int j=0; j<entropies.length; j++) {
////                    System.out.println(j + ": " + entropies[j] + " " + fen[j]);
////                }
////                System.out.println();
//                
//                break;
//            }
//        }
//        
////        System.out.print(")");
////        path[depth] = 0;
//    }
//    
//    public void findMostGames(ChPosition pos, int numOfHotspots, PositionDataListener listener)
//    {
//        IChPositionData[] hotspots = new IChPositionData[numOfHotspots];
//        int[] games = new int[numOfHotspots];
//        String[] fen = new String[numOfHotspots];
//        findMostGames(pos, games, hotspots, fen, null, new ChLongHashSet());
////        for (int i=0; i<numOfHotspots && hotspots[i] != null; i++) {
//        for (int i=0; i<numOfHotspots; i++) {
////            System.out.println(hotspots[i]);
//            listener.notifyPositionData(hotspots[i], null, new ChPosition(fen[i]), 0);
//        }
//    }
    
    /*================================================================================*/
    
    
//    private void getAllGames(ChPosition pos, String[] fen, ChLongHashSet hashSet)
//    {
////        System.out.print("(");
//        
//        //---------- search for repetitions -----------
//        if (hashSet.contains(pos.getHashCode())) return;  // =====>
//        
//        //---------- get data -----------
//        IChPositionData data = getData(pos);
//        if (data == null) return;  // =====>
//        
//        ChGameModel model = data.getGameModel();
//        if (model != null) {
//            System.out.println(model);
//            return;  // =====>
//        }
//        
//        hashSet.add(pos.getHashCode());
//        
//        short[] moves = data.getPlayedMoves();
//        for (int i = 0; i < moves.length; i++) {
//            try {
//                pos.doMove(moves[i]);
//                getAllGames(pos, fen, hashSet);
//                pos.undoMove();
//            } catch (ChIllegalMoveException ex) {}
//        }
//    }
//    
////    private void getAllGames(ChPosition pos, Hashtable table, Hashtable gameTable)
////    {
////        if (table.containsKey(new Long(pos.getHashCode()))) return;  // =====>
////        table.put(new Long(pos.getHashCode()), Boolean.TRUE);
////        
////        IChPositionData data = getData(pos);
////        if (data == null) return;  // =====>
////        if (data instanceof ChPositionDataGame) {
////            ChPositionDataGame gameData = (ChPositionDataGame)data;
//////            if (gameTable.containsKey(new Long(gameData.getGamePointer()))) return;  // =====>
//////            gameTable.put(new Long(gameData.getGamePointer()), Boolean.TRUE);
////            System.out.println(data.getGame());
////        } else {
////            short[] moves = pos.getAllMoves();
//////            System.out.println("(");
////            for (int i = 0; i < moves.length; i++) {
////                try {
////                    pos.doMove(moves[i]);
////                    IChPositionData moveData = getData(pos);
////                    if (moveData != null && data.wasMovePlayed((short)moves[i])) {
//////                        System.out.println(ChMove.getString(moves[i]));
////                        getAllGames(pos, table, gameTable);
////                    }
////                    pos.undoMove();
////                } catch (ChIllegalMoveException ex) {}
////            }
//////            System.out.println(")");
////        }
////    }
//    
//    public void getAllGames(ChPosition pos)
//    {
//        getAllGames(pos, new String[1], new ChLongHashSet());
//    }
    
    public Collection getAllGames(Position pos)
    {
        Collection games = new ArrayList();
        for (GameModelIterator it = getGameModelIterator(); it.hasNext(); ) {
            GameModel model = it.nextGameModel();
            Game game = new Game(model);
            if (game.containsPosition(pos)) {
                games.add(model);
//                System.out.println(model + " " + model.hashCode());
            }
        }
        return games;
    }
    
    /*================================================================================*/
    
    private int m_gameOffset = 0;
    
    public void setGameOffset(int offset) {m_gameOffset = offset;}
    public int getGameOffset() {return m_gameOffset;}
        
    public void initForWriting() {}
        
    public void writeGames(DataOutput gameIndices, File gamesFile, int headerMode, int movesMode) throws IOException
    {
        DataOutputStream outGames = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(gamesFile)));
        for (Iterator it = getGameModelIterator(); it.hasNext(); ) {
            GameModel gameModel = (GameModel)it.next();
            gameIndices.writeInt(outGames.size());
//            System.out.println(gameModel);
            gameModel.save(outGames, headerMode, movesMode);
        }
        outGames.close();
    }
    
    /*================================================================================*/
    
    public void print()
    {
        PositionDataIterator it = getDataIterator();
        for (PositionData data = it.getNext(); data != null; data = it.getNext()) {
            System.out.println(data);
        }
    }
}