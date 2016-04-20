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
 * $Id: GameMoveModel.java,v 1.3 2003/04/05 14:28:52 BerniMan Exp $
 */

package chesspresso.game;

import chesspresso.position.NAG;
import chesspresso.move.*;
import java.io.*;

/**
 * Representation of moves of a chess game.
 *
 * @author Bernhard Seybold
 * @version $Revision: 1.3 $
 */
public class GameMoveModel
{
    
    private final static boolean DEBUG = false;
    private final static boolean EXTRA_CHECKS = true;
    
    //======================================================================
    
    public final static int
        MODE_EVERYTHING = 0;
    
    final static short
        NO_MOVE            = (short)Move.NO_MOVE,
        LINE_START         = (short)Move.OTHER_SPECIALS,
        LINE_END           = (short)Move.OTHER_SPECIALS +  1,
        COMMENT_START      = (short)Move.OTHER_SPECIALS +  2,
        COMMENT_END        = (short)Move.OTHER_SPECIALS +  3,
        NAG_BASE           = (short)Move.OTHER_SPECIALS + 16,
        LAST_SPECIAL       = (short)(NAG_BASE + NAG.NUM_OF_NAGS);
    
    static {
        if (LAST_SPECIAL > Move.SPECIAL_MOVE + Move.NUM_OF_SPECIAL_MOVES) {
            throw new RuntimeException("Not enough space to define special moves for game move model");
        }
    }
    
    //======================================================================
    
    private short[] m_moves;
    private int m_size;
    private int m_hashCode;

    //======================================================================
    
    public GameMoveModel()
    {
        m_moves = new short[32];
        m_moves[0] = LINE_START;
        m_moves[1] = LINE_END;
        m_size = 2;
        m_hashCode = 0;
    }

    public GameMoveModel(DataInput in, int mode) throws IOException
    {
        load(in, mode);
        m_hashCode = 0;  // TODO: store in file?
    }
    
    //======================================================================
    // invariant checking
    
    private void checkLegalCursor(int index)
    {
        if (index < 0) throw new RuntimeException("Illegal index " + index);
        if (index >= m_size) throw new RuntimeException("Illegal index " + index + " m_size=" + m_size);
        if (m_moves[index] != LINE_START && !isMoveValue(m_moves[index]))
            throw new RuntimeException("No move at index " + index + " move=" + valueToString(m_moves[index]));
    }
    
    //======================================================================
    
    private static boolean isMoveValue(short value)    {return !Move.isSpecial(value);}
    private static boolean isSpecialValue(short value) {return Move.isSpecial(value);}
    
    private static boolean isNagValue(short value)   {return value >= NAG_BASE && value < NAG_BASE + NAG.NUM_OF_NAGS;}
    private static short getNagForValue(short value) {return (short)(value - NAG_BASE);}
    private static short getValueForNag(short nag)   {return (short)(nag + NAG_BASE);}
    
    //======================================================================
    
    private void changed()
    {
        m_hashCode = 0;
    }
    
    //======================================================================
    
    public boolean hasNag(int index, short nag)
    {
        if (DEBUG) {
            System.out.println("hasNag " + index + " nag " + nag);
            write(System.out);
        }
        
        short nagValue = getValueForNag(nag);
        short value;
        do {
            index++;
            value = m_moves[index];
            if (value == nagValue) return true;
        } while (isNagValue(value));
        
        return false;
    }
    
    public short[] getNags(int index)
    {
        if (EXTRA_CHECKS)
            if (!isMoveValue(m_moves[index]))
                throw new RuntimeException("No move at index " + index + " move=" + valueToString(m_moves[index]));
        
        int num = 0;
        while (isNagValue(m_moves[index + 1])) {index++; num++;}
        if (num == 0) {
            return null;
        } else {
            short[] nags = new short[num];
            // collect nags from back to front (most recently added last)
            for (int i = 0; i < num; i++) nags[i] = getNagForValue(m_moves[index - i]);
            return nags;
        }
    }
    
    public void addNag(int index, short nag)
    {
        if (DEBUG) {
            System.out.println("addNag " + index + " nag " + nag);
            write(System.out);
        }
        
        if (EXTRA_CHECKS)
            if (!isMoveValue(m_moves[index]))
                throw new RuntimeException("No move at index " + index + " val=" + valueToString(m_moves[index]));
        
        makeSpace(index + 1, 1, false);  // most recent nag first
        m_moves[index + 1] = getValueForNag(nag);
        changed();
        
        if (DEBUG) write(System.out);
    }
    
    public boolean removeNag(int index, short nag)
    {
        if (DEBUG) {
            System.out.println("removeNag " + index + " nag " + nag);
            write(System.out);
        }
        
        if (EXTRA_CHECKS)
            if (!isMoveValue(m_moves[index]))
                throw new RuntimeException("No move at index " + index + " val=" + valueToString(m_moves[index]));
        
        short nagValue = getValueForNag(nag);
        short value;
        boolean changed = false;
        do {
            index++;
            value = m_moves[index];
            if (value == nagValue) {
                while (isNagValue(m_moves[index + 1])) {
                    m_moves[index] = m_moves[index + 1];
                    index++;
                }
                m_moves[index] = NO_MOVE;
                changed = true;
                break;
            }
        } while (isNagValue(value));
        changed();
            
        if (DEBUG) write(System.out);
        return changed;
    }
    
    //======================================================================
    
    private int skipComment(int index)
    {
        if (m_moves[index] == COMMENT_START) {
            while (m_moves[index] != COMMENT_END) index++;
        } else if (m_moves[index] == COMMENT_END) {
            while (m_moves[index] != COMMENT_START) index--;
        } else {
            throw new RuntimeException("No comment start or end at index " + index + " move " + valueToString(m_moves[index]));
        }
        return index;
    }
    
    public String getComment(int index)
    {
        if (EXTRA_CHECKS)
            if (!isMoveValue(m_moves[index]) && index != 0)  // comment at index 0 allowed
                throw new RuntimeException("No move at index " + index + " move=" + valueToString(m_moves[index]));
        
        // skip all nags
        while(isNagValue(m_moves[index + 1])) index++;
        
        if (m_moves[index + 1] == COMMENT_START) {
            index += 2;
            StringBuffer sb = new StringBuffer();
            while (m_moves[index] != COMMENT_END) {
                sb.append((char)m_moves[index]);
                index++;
            }
            return sb.toString();
        } else {
            return null;
        }
    }    
    
    public boolean addComment(int index, String comment)
    {
        if (DEBUG) {
            System.out.println("addComment " + index+ " comment " + comment);
            write(System.out);
        }
        
        if (EXTRA_CHECKS)
            if (index != 0 && !isMoveValue(m_moves[index]))
                throw new RuntimeException("No move at index " + index + " val=" + valueToString(m_moves[index]));
        
        if (comment == null || comment.length() == 0) return false;  // =====>
        
        // allow comments before first move (index == 0)
        if (index != 0) {
            while(isNagValue(m_moves[index + 1])) index++;
        }
        makeSpace(index + 1, comment.length() + 2, false);
        m_moves[index + 1] = COMMENT_START;
        for (int i = 0; i < comment.length(); i++) {
            m_moves[index + 2 + i] = (short)comment.charAt(i);
        }
        m_moves[index + comment.length() + 2] = COMMENT_END;
        changed();
        
        if (DEBUG) write(System.out);
        return true;
    }
    
    public boolean removeComment(int index)
    {
        if (DEBUG) {
            System.out.println("removeComment " + index);
            write(System.out);
        }
        
        if (EXTRA_CHECKS)
            if (index != 0 && !isMoveValue(m_moves[index]))
                throw new RuntimeException("No move at index " + index + " val=" + valueToString(m_moves[index]));
        
        // allow comments before first move (index == 0)
        if (index != 0) {
            while(isNagValue(m_moves[index + 1])) index++;
        }
        boolean isChanged = false;
        if (m_moves[index + 1] == COMMENT_START) {
            for (int i = skipComment(index + 1); i > index; i--) {
                m_moves[i] = NO_MOVE;
            }
            isChanged = true;
        }
        if (isChanged) changed();
        
        if (DEBUG) write(System.out);
        return isChanged;
    }
    
    public boolean setComment(int index, String comment)
    {
        boolean changed = removeComment(index);
        return addComment(index, comment) || changed;
    }
    
    //======================================================================
    
    public boolean hasLines()
    {
        for (int i=1; i<m_size; i++) {
            if (m_moves[i] == LINE_START) return true;
        }
        return false;
    }
    
    public int getTotalNumOfPlies()
    {
        int num = 0;
        for (int index = 0; index < m_size; index++) {
            if (isMoveValue(m_moves[index])) num++;
        }
        return num;
    }
    
    public int getTotalCommentSize()
    {
        boolean inComment = false;
        int num = 0;
        for (int i=0; i<m_size; i++) {
            short move = m_moves[i];
            if (move == COMMENT_END)   inComment = false;
            if (inComment) num++;
            if (move == COMMENT_START) inComment = true;
        }
        return num;
    }
    
    public short getMove(int index)
    {
        if (index >= 0 && index < m_size) {
            short move = m_moves[index];
            return (isMoveValue(move) ? move : NO_MOVE);
        } else {
            return NO_MOVE;
        }
    }
    
//    public int goBackToMainLine(int index)
//    {
//        if (DEBUG) {
//            System.out.println("goBackToMainLine " + index);
//            write(System.out);
//        }
//        
//        index--;
//        int level = 1;
//        while (index > 0) {
//            short move = m_moves[index];
//            if      (move == LINE_START)    level--;
//            else if (move == LINE_END)      level++;
//            else if (isNagValue(move))      ;
//            else if (move == COMMENT_START) ;  // error
//            else if (move == COMMENT_END)   index = skipComment(index);
//            else if (move == NO_MOVE)       ;
//            else if (level == 0)            break;
//            index--;
//        }
//        if (DEBUG) System.out.println("  --> " + index);
//        return index;
//    }

    /**
     *@return -1 if at the beginning of a line
     */
    public int goBack(int index, boolean gotoMainLine)
    {
        if (DEBUG) {
            System.out.println("goBack " + index + " " + gotoMainLine);
            write(System.out);
        }
        
        if (EXTRA_CHECKS)
            checkLegalCursor(index);
        
        if (index <= 0) return -1;  // =====>
        
        index--;
        int level = 0;
        while (index > 0) {
            short move = m_moves[index];
            if      (move == LINE_START)   {
                level--;
                if (level == -1) {
                    if (!gotoMainLine) {
                        index = -1; break;
                    } else {
                        index = goBack(index, false);  // now at main line's move
                        index = goBack(index, false);  // now one move back
                        break;
                    }
                }
            }
            else if (move == LINE_END)      level++;
            else if (isNagValue(move))      ;
            else if (move == COMMENT_START) ;  // error
            else if (move == COMMENT_END)   index = skipComment(index);
            else if (move == NO_MOVE)       ;
            else if (level == 0)            break; // =====>
//            else if (level < 0) {
//                if (gotoMainLine) {
//                    return goBack(index, false);  // =====>
//                } else {
//                    index = -1; break;
//                }
//            }
            index--;
        }
        if (DEBUG) System.out.println("  --> " + index);
        return index;
    }
    
    /**
     * Advances one move in the current line.
     *
     * @param index the index of the current move
     *
     * @return the index of the next move. If the next move does not exist, the index
     * points to a LINE_END, where a next move should be inserted.
     */
    public int goForward(int index)
    {
        if (DEBUG) {
            System.out.println("goForward " + index);
            write(System.out);
        }
        
        if (EXTRA_CHECKS)
            checkLegalCursor(index);
        
//        if (index >= 0 && m_moves[index] == LINE_END) return index;  // =====>
//        if (index >= m_size - 1) return index;  // =====>
        
        index++;
        int level = 0;
        while (index < m_size - 1) {
            short move = m_moves[index];
            if      (move == LINE_START)     level++;
            else if (move == LINE_END)      {level--; if (level < 0) break;}
            else if (isNagValue(move))       ;
            else if (move == COMMENT_START)  index = skipComment(index);
            else if (move == COMMENT_END)    ;  // error
            else if (move == NO_MOVE)        ;
            else if (level == 0)             break;
            index++;
        }
        if (DEBUG) System.out.println("  --> " + index);
        return index;
    }
    
    public int goForward(int index, int whichLine)
    {
        if (DEBUG) {
            System.out.println("goForward " + index + " " + whichLine);
            write(System.out);
        }
        
        if (EXTRA_CHECKS)
            checkLegalCursor(index);
        
        index = goForward(index);
        if (m_moves[index] != LINE_END && whichLine > 0) {
            index++;
            int level = 0;
            while (index < m_size - 1) {
                short move = m_moves[index];
                if      (move == LINE_START)          {level++; if (level == 1) whichLine--;}
                else if (move == LINE_END)            {level--; if (level < 0) break;}
                else if (isNagValue(move))             ;
                else if (move == COMMENT_START)        index = skipComment(index);
                else if (move == COMMENT_END)          ;  // error
                else if (move == NO_MOVE)              ;
                else if (level == 1 && whichLine == 0) break;
                else if (level == 0)                  {index = -1; break;}  // =====>   move on level 0 -> not enough lines
                index++;
            }
        }
        if (DEBUG) System.out.println("  --> " + index);
        return index;
    }
    
    public int getNumOfNextMoves(int index)
    {
        if (DEBUG) {
            System.out.println("getNumOfNextMoves " + index);
            write(System.out);
        }
        
        if (EXTRA_CHECKS)
            checkLegalCursor(index);
        
        index = goForward(index);
        if (m_moves[index] == LINE_END) return 0;   // =====>

        index++;
        int numOfMoves = 1;
        int level = 0;
        while (index < m_size && level >= 0) {
            short move = m_moves[index];
            if      (move == LINE_START)    level++;
            else if (move == LINE_END)     {level--; if (level == 0) numOfMoves++;}
            else if (isNagValue(move))      ;
            else if (move == COMMENT_START) index = skipComment(index);
            else if (move == COMMENT_END)   ;  // error
            else if (move == NO_MOVE)       ;
            else if (level == 0)            break;
            index++;
        }
        if (DEBUG) System.out.println("  --> " + numOfMoves);
        return numOfMoves;
    }
    
    public boolean hasNextMove(int index)
    {
        if (DEBUG) {
            System.out.println("hasNextMove " + index);
            write(System.out);
        }
        
        if (EXTRA_CHECKS)
            checkLegalCursor(index);
        
        boolean nextMove = isMoveValue(m_moves[goForward(index)]);
        if (DEBUG) System.out.println("  --> " + nextMove);
        return (nextMove);
    }
    
    //======================================================================
    
    private int findEarliestNoMove(int index)
    {
        while (index > 1 && m_moves[index - 1] == NO_MOVE) index--;
        return index;
    }

    private int findLatestNoMove(int index)
    {
        if (EXTRA_CHECKS)
            if (index < 1 || index > m_size)
                throw new RuntimeException("Index out of bounds " + index);
            else if (m_moves[index] != NO_MOVE)
                throw new RuntimeException("Expected no move  " + index);
        
        while (index > 0 && m_moves[index - 1] == NO_MOVE) index--;
        return index;
    }

    private void enlarge(int index, int size)
    {
        if (DEBUG) {
            System.out.println("enlarge " + index + " " + size);
            write(System.out);
        }
        
        short[] newMoves = new short[m_moves.length + size];
        System.arraycopy(m_moves, 0, newMoves, 0, index);
        System.arraycopy(m_moves, index, newMoves, index + size, m_size - index);
        java.util.Arrays.fill(newMoves, index, index + size, NO_MOVE);
        m_moves = newMoves;
        m_size += size;
        if (DEBUG) write(System.out);
    }
    
    private void makeSpace(int index, int spaceNeeded, boolean possiblyMakeMore)
    {
        if (DEBUG) {
            System.out.println("makeSpace " + index + " " + spaceNeeded);
            write(System.out);
        }
        
        if (EXTRA_CHECKS)
            if (index < 1 || index >= m_size)
                throw new RuntimeException("Index out of bounds " + index + " size=" + m_size);
        
        for (int i = 0; i < spaceNeeded; i++) {
            if (m_moves[index + i] != NO_MOVE) {
                // not enough space, make it
                if (m_size + spaceNeeded - i >= m_moves.length) {
                    int size = (spaceNeeded - i  < 8 && possiblyMakeMore ? 8 : spaceNeeded - i);
                    enlarge(index, size);
                } else {
                    System.arraycopy(m_moves, index + i, m_moves, index + spaceNeeded, m_size - (index + i));
                    java.util.Arrays.fill(m_moves, index + i, index + spaceNeeded, NO_MOVE);
                    m_size += spaceNeeded - i;
                }
                break;
            }
        }
        if (DEBUG) write(System.out);
    }
    
    public int appendAsRightMostLine(int index, short move)
    {
        if (DEBUG) {
            System.out.println("appendAsRightMostLine " + index + " " + Move.getString(move));
            write(System.out);
        }
        
        if (EXTRA_CHECKS)
            checkLegalCursor(index);
        
        if (hasNextMove(index)) {
            index = goForward(index);  // go to the move for which an alternative is entered
            index = goForward(index);  // go to the end of all existing lines
            index = findEarliestNoMove(index);
            makeSpace(index, 3, true);
            m_moves[index]     = LINE_START;
            m_moves[index + 1] = move;
            m_moves[findLatestNoMove(index + 2)] = LINE_END;
            if (DEBUG) write(System.out);
            if (DEBUG) System.out.println("  --> " + index);
            changed();
            return index + 1;
        } else {
            index = goForward(index);
            index = findEarliestNoMove(index);
            makeSpace(index, 1, true);
            m_moves[index] = move;
            if (DEBUG) write(System.out);
            if (DEBUG) System.out.println("  --> " + index);
            changed();
            return index;
        }
    }
    
    public void deleteCurrentLine(int index)
    {
        if (DEBUG) {
            System.out.println("deleteCurrentLine " + index);
            write(System.out);
        }
        
        if (EXTRA_CHECKS)
            checkLegalCursor(index);
        
        int level = 0;
        boolean deleteLineEnd = false;
        
        // check if we stand at a line start
        for (int i=1; i<index; i++) {
            short move = m_moves[index - i];
            if      (move == LINE_START) {index -= i; deleteLineEnd = true; level = -1; break;}
            else if (move != NO_MOVE)     break;
        }
        
        boolean inComment = false;
        while (index < m_size) {
            short move = m_moves[index];
            if      (!inComment && move == LINE_START) level++;
            else if (!inComment && move == LINE_END)   level--;
            else if (move == COMMENT_START)            inComment = true;
            else if (move == COMMENT_END)              inComment = false;
            if (level == -1) {
                if (deleteLineEnd) m_moves[index] = NO_MOVE;
                break;
            }
            m_moves[index] = NO_MOVE;
            index++;
        }
        changed();
        if (DEBUG) write(System.out);
    }   

    //======================================================================
    
    public int pack(int index)
    {
        if (DEBUG) {
            System.out.println("pack");
            write(System.out);
        }
        
        int newSize = 0;
        for (int i=0; i<m_size; i++) {
            if (m_moves[i] != NO_MOVE) newSize++;
        }
        
        short[] newMoves = new short[newSize + 1];
        int j = 0;
        boolean inComment = false;
        for (int i=0; i<m_size; i++) {
            short move = m_moves[i];
            if      (move == COMMENT_START) inComment = true;
            else if (move == COMMENT_END)   inComment = false;
            if (inComment || (move != NO_MOVE)) {
                newMoves[j++] = move;
            }
            if (i == index) index = j - 1;
        }
        
        m_moves = newMoves;
        m_moves[newSize] = LINE_END;
        m_size = newSize;
        
        if (DEBUG) write(System.out);
        if (DEBUG) System.out.println("  --> " + index);
        
        return index;
    }
        
    //======================================================================
    
    public void load(DataInput in, int mode) throws IOException
    {
        m_size = in.readInt() + 2;
        m_moves = new short[m_size];
        byte[] data = new byte[2 * (m_size - 2)];
        in.readFully(data);
        for (int i = 1; i < m_size - 1; i++) {
            // copied from RandomAccesFile.readShort
            m_moves[i] = (short)((data[2*i - 2] << 8) | (data[2*i - 1] & 0xFF));
//            m_moves[i] = in.readShort();
        }
        m_moves[0]          = LINE_START;
        m_moves[m_size - 1] = LINE_END;
        changed();
        if (DEBUG) write(System.out);
    }
    
    public void save(DataOutput out, int mode) throws IOException
    {
        // do not save the guards at index 0 and m_size-1
        out.writeInt(m_size - 2);
        byte[] data = new byte[2 * (m_size - 2)];
        for (int i = 1; i < m_size - 1; i++) {
            short m = m_moves[i];
            // copied from RandomAccesFile.writeShort
            data[2*i - 2] = (byte)((m >>> 8) & 0xFF);
            data[2*i - 1] = (byte)((m >>> 0) & 0xFF);
//            out.writeShort(m_moves[i]);
        }
        out.write(data);
    }

    //======================================================================
    
    static String valueToString(short value)
    {
        if      (value == LINE_START)     return "(";
        else if (value == LINE_END)       return ")";
        else if (value == NO_MOVE)        return "NO";
        else if (value == COMMENT_START)  return "{";
        else if (value == COMMENT_END)    return "}";
        else if (isNagValue(value))       return "$" + getNagForValue(value);
        else                              return Move.getString(value);
    }
    
    public void write(PrintStream out)
    {
        boolean inComment = false;
        for (int i=0; i<m_size; i++) {
            short move = m_moves[i];
            if (move == COMMENT_END)   inComment = false;
            if (inComment) {
                out.print((char)move);
            } else {
                out.print(valueToString(m_moves[i]));
                out.print(" ");
            }
            if ((i % 20) == 19) out.println();
            if (move == COMMENT_START) inComment = true;
        }
        out.println();
    }
    
    //======================================================================
    
//    private static long[] s_rand = new long[65536];
//    static {
//        long randomNumber = 100;
//        for (int i=0; i<65536; i++) {
//            randomNumber = (randomNumber * 0x5DEECE66DL + 0xBL);
//            s_rand[i] = randomNumber;
//        }
//    }
    
//    private static int s_equals = 0, s_fullCompare = 0, s_true = 0, s_false = 0;
    
    public long getHashCode()
    {
        if (m_hashCode == 0) {
            int shift = 0;
            for (int index = 0; ; index = goForward(index)) {
                if (m_moves[index] == LINE_END) break;
                short move = getMove(index);
//                m_hashCode ^= move;
//                m_hashCode += move;
//                m_hashCode += s_rand[(int)move - Short.MIN_VALUE];
//                m_hashCode ^= s_rand[(int)move - Short.MIN_VALUE];
                m_hashCode ^= (long)move << shift;
                if (shift == 12) shift = 0; else shift++;
            }
        }
        return m_hashCode;
    }
    
    public int hashCode()
    {
        return (int)getHashCode();
    }
    
    public boolean equals(Object obj)
    {
//        s_equals++;
        if (obj == this) return true;  // =====>
        if (!(obj instanceof GameMoveModel)) return false;  // =====>
        GameMoveModel gameMoveModel = (GameMoveModel)obj;
        
        if (gameMoveModel.getHashCode() != getHashCode()) return false;  // =====>
//        s_fullCompare++; 
        
        int index1 = 0, index2 = 0;
        for (;;) {
            short move1 = m_moves[index1];
            short move2 = gameMoveModel.m_moves[index2];
            if (move1 == LINE_END && move2 == LINE_END) return true;  // =====>
            if (move1 != move2) return false;  // =====>
//            if (move1 == LINE_END && move2 == LINE_END) {s_true++; System.out.println(s_fullCompare + " / " + s_equals + " " + s_true + " " + s_false);return true;}  // =====>
//            if (move1 != move2) {s_false++; System.out.println(s_fullCompare + " / " + s_equals + " " + s_true + " " + s_false);return false;}  // =====>
            index1 = goForward(index1);
            index2 = gameMoveModel.goForward(index2);
        }
    }
    
}