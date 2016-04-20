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
 * $Id: FEN.java,v 1.1 2002/12/08 13:27:35 BerniMan Exp $
 */

package chesspresso.position;

import chesspresso.Chess;

/**
 *
 * @author  Bernhard Seybold
 * @version $Revision: 1.1 $
 */
public class FEN
{

    private static final char fenChars[] =
        {'K', 'P', 'Q', 'R', 'B', 'N', '-', 'n', 'b', 'r', 'q', 'p', 'k'};
    
    public static final int fenCharToStone(char ch)
    {
        for (int stone = Chess.MIN_STONE; stone <= Chess.MAX_STONE; stone++) {
            if (fenChars[stone - Chess.MIN_STONE] == ch) return stone;
        }
        return Chess.NO_STONE;
    }
    
    public static final char stoneToFenChar(int stone)
    {
        if (stone >= Chess.MIN_STONE && stone <= Chess.MAX_STONE) {
            return fenChars[stone - Chess.MIN_STONE];
        } else {
            return '?';
        }
    }
    
    public static final String START_POSITION =
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    
    //======================================================================
    
    public static void initFromFEN(MutablePosition pos, String fen, boolean strict) throws IllegalArgumentException
    {
        pos.clear();
        
        int index = 0;
        char ch;
        
        /*========== 1st field : pieces ==========*/
        int row = 7;
        int col = 0;
        while (index < fen.length() && fen.charAt(index) != ' ') {
            ch = fen.charAt(index);
            if (ch == '/') {
                if (col != 8)
                    throw new IllegalArgumentException("Malformatted fen string: unexpected '/' found at index " + index);
                row--; col = 0;
            } else if (ch >= '1' && ch <= '8') {
                int num = (int)(ch - '0');
                if (col + num > 8)
                    throw new IllegalArgumentException("Malformatted fen string: too many pieces in rank at index " + index + ": " + ch);
                for (int j=0; j<num; j++) {
                    pos.setStone(Chess.coorToSqi(col, row), Chess.NO_STONE);
                    col++;
                }
            } else {
                int stone = FEN.fenCharToStone(ch);
                if (stone == Chess.NO_STONE)
                    throw new IllegalArgumentException("Malformatted fen string: illegal piece char: " + ch);
                pos.setStone(Chess.coorToSqi(col, row), stone);
                col++;
            }
            index++;
        }
        if (row != 0 || col != 8)
            throw new IllegalArgumentException("Malformatted fen string: missing pieces at index: " + index);
        
        /*========== 2nd field : to play ==========*/
        if (index + 1 < fen.length() && fen.charAt(index) == ' ') {
            ch = fen.charAt(index + 1);
            if      (ch == 'w') pos.setToPlay(Chess.WHITE);
            else if (ch == 'b') pos.setToPlay(Chess.BLACK);
            else
                throw new IllegalArgumentException("Malformatted fen string: expected 'to play' as second field but found " + ch);
            index += 2;
        }
        
        /*========== 3rd field : castles ==========*/
        if (index + 1 < fen.length() && fen.charAt(index) == ' ') {
            index++;
            int castles = ImmutablePosition.NO_CASTLES;
            if (fen.charAt(index) == '-') {
                index++;
            } else {
                int last = -1;
                while (index < fen.length() && fen.charAt(index) != ' ') {
                    ch = fen.charAt(index);
                    if      (ch == 'K')                          {castles |= ImmutablePosition.WHITE_SHORT_CASTLE; last = 0;}
                    else if (ch == 'Q' && (!strict || last < 1)) {castles |= ImmutablePosition.WHITE_LONG_CASTLE;  last = 1;}
                    else if (ch == 'k' && (!strict || last < 2)) {castles |= ImmutablePosition.BLACK_SHORT_CASTLE; last = 2;}
                    else if (ch == 'q' && (!strict || last < 3)) {castles |= ImmutablePosition.BLACK_LONG_CASTLE;  last = 3;}
                    else
                        throw new IllegalArgumentException("Malformatted fen string: illegal castles identifier or sequence " + ch);
                    index++;
                }
            }
            pos.setCastles(castles);
        } else {
            throw new IllegalArgumentException("Malformatted fen string: expected castles at index " + index);
        }
        
        /*========== 4th field : ep square ==========*/
        if (index + 1 < fen.length() && fen.charAt(index) == ' ') {
            index++;
            int sqiEP = Chess.NO_SQUARE;
            if (fen.charAt(index) == '-') {
                index++;
            } else {
                if (index + 2 < fen.length()) {
                    sqiEP = Chess.strToSqi(fen.substring(index, index + 2));
                    index += 2;
                }
            }
            pos.setSqiEP(sqiEP);
        } else {
            throw new IllegalArgumentException("Malformatted fen string: expected ep square at index " + index);
        }
        
        /*========== 5th field : half move clock ==========*/
        if (index + 1 < fen.length() && fen.charAt(index) == ' ') {
            index++;
            int start = index; while(index < fen.length() && fen.charAt(index) != ' ') index++;
            pos.setHalfMoveClock(Integer.parseInt(fen.substring(start, index)));
        } else {
            throw new IllegalArgumentException("Malformatted fen string: expected half move clock at index " + index);
        }
        
        /*========== 6th field : full move number ==========*/
        if (index + 1 < fen.length() && fen.charAt(index) == ' ') {
            if (pos.getToPlay() == Chess.WHITE) {
                pos.setPlyNumber(2 * (Integer.parseInt(fen.substring(index + 1)) - 1));
            } else {
                pos.setPlyNumber(2 * (Integer.parseInt(fen.substring(index + 1)) - 1) + 1);
            }
        } else {
            throw new IllegalArgumentException("Malformatted fen string: expected ply number at index " + index);
        }
        
        /*========== now check the produced position ==========*/
        try {
            pos.validate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Malformatted fen string: " + e.getMessage());
        }
        
    }
    
    public static String getFEN(ImmutablePosition pos)
    {
        StringBuffer sb = new StringBuffer();
        
        /*========== 1st field : pieces ==========*/
        int row = 7, col = 0;
        int blanks = 0;
        while (row >= 0) {
            int stone = pos.getStone(Chess.coorToSqi(col, row));
            if (stone == Chess.NO_STONE) {
                blanks++;
            } else {
                if (blanks > 0) {
                    sb.append(blanks); blanks = 0;
                }
                sb.append(stoneToFenChar(stone));
            }
            col++;
            if (col > 7) {
                if (blanks > 0) {
                    sb.append(blanks); blanks = 0;
                }
                row--; col = 0; blanks = 0;
                if (row >= 0) sb.append('/');
            }
        }
        
        /*========== 2nd field : to play ==========*/
        sb.append(' ').append(pos.getToPlay() == Chess.WHITE ? 'w' : 'b');
        
        /*========== 3rd field : castles ==========*/
        sb.append(' ');
        int castles = pos.getCastles();
        if (castles != ImmutablePosition.NO_CASTLES) {
            if ((castles & ImmutablePosition.WHITE_SHORT_CASTLE) != 0) sb.append('K');
            if ((castles & ImmutablePosition.WHITE_LONG_CASTLE)  != 0) sb.append('Q');
            if ((castles & ImmutablePosition.BLACK_SHORT_CASTLE) != 0) sb.append('k');
            if ((castles & ImmutablePosition.BLACK_LONG_CASTLE)  != 0) sb.append('q');
        } else {
            sb.append('-');
        }
        
        /*========== 4th field : ep square ==========*/
        sb.append(' ');
        if (pos.getSqiEP() == Chess.NO_SQUARE)
            sb.append('-');
        else
            sb.append(Chess.sqiToStr(pos.getSqiEP()));
        
        /*========== 5th field : half move clock ==========*/
        sb.append(' ').append(pos.getHalfMoveClock());
        
        /*========== 6th field : full move number ==========*/
        sb.append(' ').append(pos.getPlyNumber() / 2 + 1);
        
        return sb.toString();        
    }
}
