/*
 * Copyright (C) Bernhard Seybold. All rights reserved.
 *
 * This software is published under the terms of the LGPL Software License,
 * a copy of which has been included with this distribution in the LICENSE.txt
 * file.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *
 * $Id: Game.java,v 1.3 2003/01/04 16:21:24 BerniMan Exp $
 */

package chesspresso.game;

import chesspresso.*;
import chesspresso.pgn.*;
import chesspresso.position.*;
import java.io.*;
import java.util.*;
import chesspresso.move.Move;
import chesspresso.move.IllegalMoveException;

/**
 * Abstraction of a chess game.
 *
 * A chess game consists of the following parts:
 *  <ul>
      <li> {@link GameHeaderModel} cantaining information about the game header, 
 *         for instance white name, event, site
 *    <li> {@link GameMoveModel} containing the moves, lines, comments of the game.
 *    <li> a cursor and the current position in the game.
 *  </ul>
 *
 * If you only need the information, not a cursor, use {@link GameModel} consisting
 * of {@link GameHeaderModel} and {@link GameMoveModel}.
 *
 * The game offers the following groups of operation:
 *   <ul>
 *     <li> direct access to values of the game header
 *     <li> methods to append or delete lines of the move model
 *     <li> methods to handle listeners for game changes
 *     <li> methods to walk through the game, beginning mit <code>go</code>
 *     <li> a method to {@link #traverse(GameListener, boolean)} the game in postfix order
 *          (the order used by {@link chesspresso.pgn.PGN})
 *   </ul>
 *
 * @author  Bernhard Seybold
 * @version $Revision: 1.3 $
 */
public class Game implements PositionChangeListener
{
    
    private static boolean DEBUG = false;
    
    //======================================================================
    
    private GameModel m_model;
    private GameHeaderModel m_header;
    private GameMoveModel m_moves;
    private Position m_position;
    private int m_cur;
    private boolean m_ignoreNotifications;
    private boolean m_alwaysAddLine;        // during pgn parsing, always add new lines
    private List m_changeListeners;
    
    //======================================================================
    
    public Game()
    {
        this(new GameModel());
    }

    public Game(GameModel gameModel)
    {
        setModel(gameModel);
        m_ignoreNotifications = false;
        m_alwaysAddLine = false;
    }

    //======================================================================
    
    public GameModel getModel() {return m_model;}
    public Position getPosition() {return m_position;}
    public int getCurNode() {return m_cur;}
    public int getRootNode() {return 0;}
    
    public void pack()
    {
        m_cur = m_moves.pack(m_cur); // TODO pack headers?
    }
    
    private void setModel(GameModel gameModel)
    {
        m_model = gameModel;
        m_header = gameModel.getHeaderModel();
        m_moves = gameModel.getMoveModel();
        
        String fen = m_header.getTag(PGN.TAG_FEN);
        if (fen != null) {
            setPosition(new Position (fen, false));
        } else {
            setPosition(Position.createInitialPosition());
        }
    }
    
    private void setPosition(Position position)
    {
        m_position = position;
        m_position.addPositionChangeListener(this);
        m_cur = 0;
    }
    
    public void setAlwaysAddLine(boolean alwaysAddLine) {m_alwaysAddLine = alwaysAddLine;}
    
    //======================================================================
    
    public void addChangeListener(GameModelChangeListener listener)
    {
        if (m_changeListeners == null) m_changeListeners = new ArrayList();
        m_changeListeners.add(listener);
    }
    
    public void removeChangeListener(GameModelChangeListener listener)
    {
        m_changeListeners.remove(listener);
        if (m_changeListeners.size() == 0) m_changeListeners = null;
    }
    
    protected void fireMoveModelChanged()
    {
        if (m_changeListeners != null) {
            for (Iterator it=m_changeListeners.iterator(); it.hasNext(); ) {
                ((GameModelChangeListener)it.next()).moveModelChanged(this);
            }
        }
    }
    
    //======================================================================
    // methods of PositionChangeListener
    
    public void notifyPositionChanged(ImmutablePosition position)
    {
    }
    
    public void notifyMoveDone(ImmutablePosition position, short move)
    {
        if (DEBUG) System.out.println("ChGame: move made in position " + move);
        
        if (!m_ignoreNotifications) {
            if (!m_alwaysAddLine) {
                short[] moves = getNextShortMoves();
                for (int i=0; i<moves.length; i++) {
                    if (moves[i] == move) {
                        m_cur = m_moves.goForward(m_cur, i);
                        return;  // =====>
                    }
                }
            }
            m_cur = m_moves.appendAsRightMostLine(m_cur, move);
            fireMoveModelChanged();
        }
    }
    
    public void notifyMoveUndone(ImmutablePosition position)
    {
        if (DEBUG) System.out.println("ChGame: move taken back in position");
        
        if (!m_ignoreNotifications) {
            m_cur = m_moves.goBack(m_cur, true);
        }
    }
    
    //======================================================================
    // header methods
    
    public String getTag(String tagName) {return m_header.getTag(tagName);}
    
    public String[] getTags() {return m_header.getTags();}
    
    public void setTag(String tagName, String tagValue)
    {
        m_header.setTag(tagName, tagValue);
        if (PGN.TAG_FEN.equals(tagName)) {
            setPosition(new Position (tagValue, false));
        }
    }
    
    public String getEvent()       {return m_header.getEvent();}
    public String getSite()        {return m_header.getSite();}
    public String getDate()        {return m_header.getDate();}
    public String getRound()       {return m_header.getRound();}
    public String getWhite()       {return m_header.getWhite();}
    public String getBlack()       {return m_header.getBlack();}
    public String getResultStr()   {return m_header.getResultStr();}
    public String getWhiteEloStr() {return m_header.getWhiteEloStr();}
    public String getBlackEloStr() {return m_header.getBlackEloStr();}
    public String getEventDate()   {return m_header.getEventDate();}
    public String getECO()         {return m_header.getECO();}
    
    public int getResult()         {return m_header.getResult();}
    public int getWhiteElo()       {return m_header.getWhiteElo();}
    public int getBlackElo()       {return m_header.getBlackElo();}
        
    //======================================================================
    
    /**
     * Returns whether the given position occurs in the main line of this game.
     *
     *@param position the position to look for, must not be null
     *@return whether the given position occurs in the main line of this game
     */
    public boolean containsPosition(ImmutablePosition position)
    {
        boolean res = false;
        int index = getCurNode();
        gotoStart(true);
        for (;;) {
            if (m_position.getHashCode() == position.getHashCode()) {res = true; break;}
            if (!hasNextMove()) break;
            goForward(true);
        }
        gotoNode(index, true);
        return res;
    }
    
    //======================================================================
    
    /**
     * Returns info about the game consisting of white player,
     * black player and result.
     *
     *@return the info string
     */
    public String getInfoString()
    {
        return getWhite() + " - " + getBlack() + " " + getResultStr();
    }
    
    /**
     * Returns info about the game consisting of white player,
     * black player, event, site, date, result, and ECO.
     *
     *@return the info string
     */
    public String getLongInfoString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(getWhite()).append(" - ").append(getBlack()).append(", ").append(getEvent());
        if (getRound() != null) {
            sb.append(" (").append(getRound()).append(") ");
        }
        sb.append(", ").append(getSite()).append("  ").append(getResult());
        if (getECO() != null) {
            sb.append("  [").append(getECO()).append("]");
        }
        return sb.toString();
    }
    
    /**
     * Returns information to display at the header of a game. The information
     * is split in three parts: (1) white and black player plus their elos, (2)
     * event, site, date, rounf, and (3) the ECO.
     *
     *@param line which line to return (0..2)
     *@return the info string
     */
    public String getHeaderString(int line)
    {
        if (line == 0) {
            StringBuffer sb = new StringBuffer();
            sb.append(getWhite());
            if (getWhiteElo() != 0) sb.append(" [").append(getWhiteElo()).append("]");
            sb.append(" - ").append(getBlack());
            if (getBlackElo() != 0) sb.append(" [").append(getBlackElo()).append("]");
            sb.append("  ").append(getResultStr()).append("  (").append(getNumOfMoves()).append(")");
            return sb.toString();
        } else if (line == 1) {
            StringBuffer sb = new StringBuffer();
            sb.append(getEvent()).append(", ").append(getSite()).append(", ").append(getDate());
            sb.append("  [").append(getRound()).append("]");
            return sb.toString();
        } else if (line == 2) {
            return getECO();
        } else {
            throw new RuntimeException("Only 3 header lines supported");
        }
    }
    
    //======================================================================
    // moves methods
    
    public boolean hasNag(short nag)       {return m_moves.hasNag(m_cur, nag);}
    public short[] getNags()               {return m_cur == 0 ? null : m_moves.getNags(m_cur);}
    public void addNag(short nag)          {m_moves.addNag(m_cur, nag); fireMoveModelChanged();}
    public void removeNag(short nag)       {if (m_moves.removeNag(m_cur, nag)) fireMoveModelChanged();}
    
    public String getComment()             {return m_moves.getComment(m_cur);}
    public void addComment(String comment) {if (m_moves.addComment(m_cur, comment)) fireMoveModelChanged();}
    public void setComment(String comment) {if (m_moves.setComment(m_cur, comment)) fireMoveModelChanged();}
    public void removeComment()            {if (m_moves.removeComment(m_cur)) fireMoveModelChanged();}
    
    //======================================================================
    
    public int getCurrentPly() {return m_position.getPlyNumber();}
    public int getCurrentMoveNumber() {return (m_position.getPlyNumber() + 1) / 2;}
    public int getNextMoveNumber() {return (m_position.getPlyNumber() + 2) / 2;}
    
    public int getNumOfPlies()
    {
        int num = 0;
        int index = 0;
        while (m_moves.hasNextMove(index)) {
            index = m_moves.goForward(index);
            num++;
        }
        return num;
    }
    
    public int getNumOfMoves() {return Chess.plyToMoveNumber(getNumOfPlies());}
    
    public int getTotalNumOfPlies() {return m_moves.getTotalNumOfPlies();}

    //======================================================================
    
    public Move getLastMove()
    {
        return m_position.getLastMove();
    }
    
    public Move getNextMove()
    {
        return getNextMove(0);
    }
    
    public short getNextShortMove()
    {
        return getNextShortMove(0);
    }
    
    public Move getNextMove(int whichLine)
    {
        short shortMove = m_moves.getMove(m_moves.goForward(m_cur, whichLine));
        if (shortMove == GameMoveModel.NO_MOVE) return null;  // =====>
        try {
            m_position.setNotifyListeners(false);
            m_position.doMove(shortMove);
//            ChMove move = m_position.getLastMove(shortMove);
            Move move = m_position.getLastMove();
            m_position.undoMove();
            m_position.setNotifyListeners(true);
            return move;
        } catch (IllegalMoveException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public short getNextShortMove(int whichLine)
    {
        return m_moves.getMove(m_moves.goForward(m_cur, whichLine));
    }
    
    public boolean hasNextMove() {return m_moves.hasNextMove(m_cur);}
    
    public int getNumOfNextMoves() {return m_moves.getNumOfNextMoves(m_cur);}
    
    public short[] getNextShortMoves()
    {
        short[] moves = new short[m_moves.getNumOfNextMoves(m_cur)];
        for (int i=0; i<moves.length; i++) {
            moves[i] = m_moves.getMove(m_moves.goForward(m_cur, i));
        }
        return moves;
    }
    
    public Move[] getNextMoves()
    {
        m_position.setNotifyListeners(false);
        Move[] moves = new Move[m_moves.getNumOfNextMoves(m_cur)];
        for (int i=0; i<moves.length; i++) {
            short move = m_moves.getMove(m_moves.goForward(m_cur, i));
            try {
                m_position.doMove(move);
//                moves[i] = m_position.getLastMove(move);
                moves[i] = m_position.getLastMove();
                m_position.undoMove();
            } catch (IllegalMoveException ex) {
                m_moves.write(System.out);
                System.out.println("cur = " + m_cur + " move=" + GameMoveModel.valueToString(move));
                ex.printStackTrace();
            }
        }
        m_position.setNotifyListeners(true);
        return moves;
    }
    
    public Move[] getMainLine()
    {
        int num = 0;
        int index = m_cur;
        while (m_moves.hasNextMove(index)) {
            index = m_moves.goForward(index);
            num++;
        }
        
        Move[] moves = new Move[num];
        for (int i=0; i<num; i++) {
            moves[i] = goForwardAndGetMove(true);
        }
        
        m_position.setNotifyListeners(false);
        for (int i=0; i<moves.length; i++) m_position.undoMove();
        m_position.setNotifyListeners(true);
        
        return moves;
    }
    
    //======================================================================
    
    public boolean goBack()                    {return goBack(false);}    
    public boolean goForward()                 {return goForward(false);}
    public boolean goForward(int whichLine)    {return goForward(whichLine, false);}
    public void gotoStart()                    {gotoStart(false);}
    public void gotoEndOfLine()                {gotoEndOfLine(false);}
    public void goBackToMainLine()             {goBackToMainLine(false);}
    public void goBackToLineBegin()            {goBackToLineBegin(false);}
    public void gotoNode(int node)             {gotoNode(node, false);}
    public void gotoPosition(ImmutablePosition pos)  {gotoPosition(pos, false);}
    public void deleteCurrentLine()            {deleteCurrentLine(false);}
    
    //======================================================================
    
    private boolean goBack(boolean silent)
    {
        if (DEBUG) System.out.println("goBack");
        
        int index = m_moves.goBack(m_cur, true);
        if (index != -1) {
//        if (m_position.canUndoMove()) {  // do not rely on position since in silent mode it is not updated
//            m_cur = m_moves.goBack(m_cur, true);
            m_cur = index;
            m_ignoreNotifications = true;
            if (silent) m_position.setNotifyListeners(false);
            m_position.undoMove();
            if (silent) m_position.setNotifyListeners(true);
            m_ignoreNotifications = false;
            return true;
        } else {
            return false;
        }
    }
    
    private boolean goBackInLine(boolean silent)
    {
        if (DEBUG) System.out.println("goBackInLine");
        
        int index = m_moves.goBack(m_cur, false);
        if (index != -1) {
            m_cur = index; // needs to be set before undoing the move to allow listeners to check for curNode
            m_ignoreNotifications = true;
            if (silent) m_position.setNotifyListeners(false);
            m_position.undoMove();
            if (silent) m_position.setNotifyListeners(true);
            m_ignoreNotifications = false;
            return true;
        } else {
            return false;
        }
    }
    
    private boolean goForward(boolean silent)
    {
        if (DEBUG) System.out.println("goForward");
        
        return goForward(0, silent);
    }
    
    private Move goForwardAndGetMove(boolean silent)
    {
        if (DEBUG) System.out.println("goForwardAndGetMove");
        
        return goForwardAndGetMove(0, silent);
    }
    
    private boolean goForward(int whichLine, boolean silent)
    {
        if (DEBUG) System.out.println("goForward " + whichLine);
        
        int index = m_moves.goForward(m_cur, whichLine);
        short shortMove = m_moves.getMove(index);
        if (DEBUG) System.out.println("  move = " + Move.getString(shortMove));
        if (shortMove != GameMoveModel.NO_MOVE) {
            try {
                m_cur = index;
                m_ignoreNotifications = true;
                if (silent) m_position.setNotifyListeners(false);
                m_position.doMove(shortMove);
                if (silent) m_position.setNotifyListeners(true);
                m_ignoreNotifications = false;
                return true;
            } catch (IllegalMoveException ex) {
                ex.printStackTrace();
            }
        } else {
//            new Exception("Forward at end of line").printStackTrace();
        }
        return false;
    }
    
    private Move goForwardAndGetMove(int whichLine, boolean silent)
    {
        if (DEBUG) System.out.println("goForwardAndGetMove " + whichLine);
        
        int index = m_moves.goForward(m_cur, whichLine);
        short shortMove = m_moves.getMove(index);
        if (DEBUG) System.out.println("  move = " + Move.getString(shortMove));
        if (shortMove != GameMoveModel.NO_MOVE) {
            try {
                m_cur = index;
                m_ignoreNotifications = true;
                if (silent) m_position.setNotifyListeners(false);
                m_position.doMove(shortMove);
                Move move = m_position.getLastMove();
                if (silent) m_position.setNotifyListeners(true);
                m_ignoreNotifications = false;
                return move;
            } catch (IllegalMoveException ex) {
                ex.printStackTrace();
            }
        } else {
//            new Exception("Forward at end of line").printStackTrace();
        }
        return null;
    }
    
    private void gotoStart(boolean silent)
    {
        while (goBack(silent)) ;
    }
    
    private void gotoEndOfLine(boolean silent)
    {
        while (goForward(silent)) ;
    }
    
    private void goBackToLineBegin(boolean silent)
    {
        if (DEBUG) System.out.println("goBackToLineBegin");
        
        while (goBackInLine(silent)) ;
    }
    
    private void goBackToMainLine(boolean silent)
    {
        if (DEBUG) System.out.println("goBackToMainLine");
        
        goBackToLineBegin(silent);
        goBack(silent);
        goForward(silent);
    }
     
    private int getNumOfPliesToRoot(int node)
    {
        int plies = 0;
        while (node > 0) {
            node = m_moves.goBack(node, true);
            plies++;
        }
        return plies;
    }
    
    private int[] getNodesToRoot(int node)
    {
        int[] nodes;
        int i = 0;
        if (m_moves.getMove(node) != GameMoveModel.NO_MOVE) {
            nodes = new int[getNumOfPliesToRoot(node) + 1];
            nodes[0] = node;
            i = 1;
        } else {
            nodes = new int[getNumOfPliesToRoot(node)];  // if we stand on a line end, don't include node in nodes to root
            i = 0;
        }
        for (; i < nodes.length; i++) {
            node = m_moves.goBack(node, true);
            nodes[i] = node;
        }
        return nodes;
    }
    
    public void gotoNode(int node, boolean silent)
    {
        int[] nodeNodes = getNodesToRoot(node);
        
        gotoStart(silent);
        for (int i = nodeNodes.length - 2; i >= 0; i--) {
            int nextMoveIndex = 0;
            for (int j = 1; j < getNumOfNextMoves(); j++) {
                if (m_moves.goForward(m_cur, j) == nodeNodes[i]) {
                    nextMoveIndex = j; break;
                }
            }
            goForward(nextMoveIndex, silent);
        }
        m_cur = node;  // now that we have made all the moves, set cur to node
    }
    
    
    public void gotoPosition(ImmutablePosition pos, boolean silent)
    {
        if (m_position.equals(pos)) return; // =====>
        
        int curNode = getCurNode();
        gotoStart(true);
        do {
            if (m_position.equals(pos)) {
                int posNode = getCurNode();
                gotoNode(curNode, true);
                gotoNode(posNode, silent);
                return;  // =====>
            }
        } while (goForward(true));
    }
    
    //======================================================================
    
    public void deleteCurrentLine(boolean silent)
    {
        int index = m_cur;
        if (goBack(silent)) {
            m_moves.deleteCurrentLine(index);
            fireMoveModelChanged();
        }
    }
    
    //======================================================================
    
    /**
     * Method to traverse the game in postfix order
     * (first the lines, then the main line). This method is used by {@link chesspresso.pgn.PGN}.
     *
     *@param listener the listener to receive event when arriving at nodes
     *@param withLines whether or not to include lines of the current main line.
     */
    public void traverse(GameListener listener, boolean withLines)
    {
        int index = getCurNode();
        gotoStart(true);
        traverse(listener, withLines, m_position.getPlyNumber(), 0);
        gotoNode(index, true);
    }
    
    private void traverse(GameListener listener, boolean withLines, int plyNumber, int level)
    {
        while(hasNextMove()) {
            int numOfNextMoves = getNumOfNextMoves();
            
            Move move = goForwardAndGetMove(true);
            listener.notifyMove(move, getNags(), getComment(), plyNumber, level);
            
            if (withLines && numOfNextMoves > 1) {
                for (int i=1; i<numOfNextMoves; i++) {
                    goBack(true);
                    listener.notifyLineStart(level);
                    
                    move = goForwardAndGetMove(i, true);
                    listener.notifyMove(move, getNags(), getComment(), plyNumber, level + 1);
                
                    traverse(listener, withLines, plyNumber + 1, level + 1);
                    
                    goBackToMainLine(true);
                    if (i > 0) listener.notifyLineEnd(level);
                }
            }
            
            plyNumber++;
        }
    }
    
	//======================================================================
	
	public void insertGameModel(GameModel gameModel, boolean withLines, boolean includeFinalComment)
	{
		int index = getCurNode();
		gotoStart(true);
		
		Game game = new Game(gameModel);
		game.traverse(new GameListener() {
			public void notifyMove(Move move, short[] nags, String comment, int plyNumber, int level)
			{
				try {
					m_position.doMove(move);
					
					if (comment != null) {
						String s = getComment();
						setComment((s == null ? "" : s + " / ") + comment);
					}
					
					if (nags != null) {
						for (int i=0; i<nags.length; i++) {
							addNag(nags[i]);
						}
					}
					
				} catch (IllegalMoveException ex) {
					ex.printStackTrace();
				}
			}
			
			public void notifyLineStart(int level)
			{
				m_position.undoMove();
			}
			
			public void notifyLineEnd(int level)
			{
				goBackToMainLine(true);
			}
			
		}, withLines);
		
		if (includeFinalComment) {
			String s = getComment();
			setComment((s == null ? "" : s + " / ") + gameModel.getHeaderModel().toString());
		}
		
		gotoNode(index, true);
	}
	
    //======================================================================

//    public void load(DataInput in, int headerMode, int movesMode) throws IOException
//    {
//        m_header = new ChGameHeaderModel(in, headerMode);
//        m_moves = new ChGameMoveModel(in, movesMode);
//    }
//    
    public void save(DataOutput out, int headerMode, int movesMode) throws IOException
    {
        m_model.save(out, headerMode, movesMode);
    }
    
    //======================================================================
    
    /**
     * Returns the hash code of the game, which is defined as the hash code of the
     * move model. That means two game are considered equal if they contain exactly the
     * same lines. The header does not matter.
     *
     *@return the hash code
     */
    public int hashCode()
    {
        return getModel().hashCode();
    }
    
    /**
     * Returns whether two games are equal. This is the case if they contain exactly the
     * same lines. The header does not matter.
     *
     *@return the hash code
     */
    public boolean equals(Object obj)
    {
        if (obj == this) return true;  // =====>
        if (!(obj instanceof Game)) return false;  //=====>
        Game game = (Game)obj;
        return game.getModel().equals(getModel());
    }    
    
    //======================================================================
    
    /**
     * Returns a string represention of the game. Implemented as the string
     * represention of the header plus the move model.
     *
     *@return a string represention of the game
     */
    public String toString() {return m_model.toString();}
    
}