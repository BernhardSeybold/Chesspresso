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
 * $Id: Position.java,v 1.4 2003/04/09 18:08:52 BerniMan Exp $
 */

package chesspresso.position;


import chesspresso.*;
import chesspresso.move.*;

import java.text.*;


public final class Position extends AbstractMoveablePosition
{
    
    //======================================================================
    // Debug flag 
    
    private final static boolean DEBUG = false;
    
    //======================================================================
    // Profiling
    
    private final static boolean PROFILE = false;
    
    private static long m_numIsAttacked = 0;
    private static long m_numDirectAttackers = 0;
    private static long m_numGetAllAttackers = 0;
    private static long m_numIsCheck = 0;
    private static long m_numIsMate = 0;
    private static long m_numIsStaleMate = 0;
    private static long m_numGetAllMoves = 0;
    private static long m_numPositions = 0;
    private static long m_numGetPinnedDirection = 0;
    private static long m_numDoMove = 0;
    private static long m_numLongsBackuped = 0;
    private static long m_numUndoMove = 0;
    private static long m_numSet = 0;
    private static long m_numGetSquare = 0;
    
    private static DecimalFormat df = new DecimalFormat("#");
    
    private static String format(long num)
    {
        String res = "               " + df.format(num);
        return res.substring(res.length() - 12);
    }
    
    public static void printProfile()
    {
        if (!PROFILE) return;  // =====>
        
        //        NumberFormat nf = NumberFormat.getNumberInstance();
        //        nf.form
        
        System.out.println("Instances created:");
        System.out.println("  ChPosition:         " + format(m_numPositions));
        System.out.println("Methods called:");
        System.out.println("  isAttacked:         " + format(m_numIsAttacked));
        System.out.println("  directAttackers:    " + format(m_numDirectAttackers));
        System.out.println("  getAllAttackers:    " + format(m_numGetAllAttackers));
        System.out.println("  isCheck:            " + format(m_numIsCheck));
        System.out.println("  isMate:             " + format(m_numIsMate));
        System.out.println("  isStaleMate:        " + format(m_numIsStaleMate));
        System.out.println("  getAllMoves:        " + format(m_numGetAllMoves));
        System.out.println("  getPinnedDirection: " + format(m_numGetPinnedDirection));
        System.out.println("  doMove:             " + format(m_numDoMove));
        System.out.println("    longs backuped    " + format(m_numLongsBackuped) + "  " + ((double)m_numLongsBackuped / m_numDoMove) + " per move");
        System.out.println("  undoMove:           " + format(m_numUndoMove));
        System.out.println("  set:                " + format(m_numSet));
        System.out.println("  getSquare:          " + format(m_numGetSquare));
    }
    
    //======================================================================
    // Bit Board operations
    // put here for performance (inlining)
    // do before hashing!
    
    private final static long[] s_ofCol, s_ofRow, s_ofSquare;
    
    static {
        s_ofCol = new long[Chess.NUM_OF_COLS];
        for(int col=0; col<Chess.NUM_OF_COLS; col++) s_ofCol[col] = 0x0101010101010101L << col;
        
        s_ofRow = new long[Chess.NUM_OF_ROWS];
        for(int row=0; row<Chess.NUM_OF_ROWS; row++) s_ofRow[row] = 255L << (8*row);
        
        s_ofSquare = new long[Chess.NUM_OF_SQUARES];
        for(int sqi=0; sqi<Chess.NUM_OF_SQUARES; sqi++) s_ofSquare[sqi] = 1L << sqi;
    }
    
    private static final boolean isExactlyOneBitSet(long bb)
    {
        return bb != 0L && (bb & (bb - 1L)) == 0L;
    }
    
    private static final boolean isMoreThanOneBitSet(long bb)
    {
        return (bb & (bb - 1L)) != 0L;
    }
    
    private static final int numOfBitsSet(long bb)
    {
        int num = 0;
        while (bb != 0L) {bb &= bb - 1; num++;}
        //        System.out.println(bb + " " + num);
        return num;
    }
    
    //public static final long ofSquare(int sqi) {return 1L << sqi;}
    //public static final long ofCol(int col) {return 0x0101010101010101L << col;}
    //public static final long ofRow(int row) {return 255L << (8*row);}
    public static final long ofSquare(int sqi) {return s_ofSquare[sqi];}
    public static final long ofCol(int col) {return s_ofCol[col];}
    public static final long ofRow(int row) {return s_ofRow[row];}
    
    private static final int getFirstSqi(long bb)
    {
        // inefficient for bb == 0L, test outside (in while loop condition)
        //        if (bb == 0L) {
        //            return Chess.NO_SQUARE;
        //        } else {
        int sqi=0;
        if ((bb & 0xFFFFFFFFL) == 0L) {bb >>>= 32; sqi += 32;}  // TODO: is this good for all VMs?
        if ((bb & 0xFFFFL) == 0L) {bb >>>= 16; sqi += 16;}
        if ((bb & 0xFFL) == 0L) {bb >>>= 8; sqi += 8;}
        if ((bb & 0xFL) == 0L) {bb >>>= 4; sqi += 4;}
        if ((bb & 0x3L) == 0L) {bb >>>= 2; sqi += 2;}
        if ((bb & 0x1L) == 0L) {bb >>>= 1; sqi += 1;}
        //            while ((bb % 2L) == 0L) {bb >>>= 1; sqi++;}  change from % to &
        return sqi;
        //        }
    }
    
    public static final long getFirstSqiBB(long bb)  // returns 0 if no bit set, not -1!!!
    {
        return bb & -bb;
    }
    
    private static final String toString(long bb)
    {
        String ZEROS = "0000000000000000000000000000000000000000000000000000000000000000";
        String s = ZEROS + Long.toBinaryString(bb);
        return s.substring(s.length() - 64);
    }
    
    private static final void printBoard(long bb)
    {
        for (int row = Chess.NUM_OF_ROWS - 1; row >= 0; row--) {
            for (int col = 0; col < Chess.NUM_OF_COLS; col++) {
                if ((bb & ofSquare(Chess.coorToSqi(col, row))) != 0L) {
                    System.out.print('x');
                } else {
                    System.out.print('.');
                }
            }
            System.out.println();
        }
    }
    
    //======================================================================
    // directions
    
    public static final int
        NO_DIR = -1, NUM_OF_DIRS = 8,
        SW = 0, S = 1, SE = 2, E = 3, NE = 4, N = 5, NW = 6, W = 7;  // need to start there, to allow calculation between pawn move dir and pawn capture dir
    
    private static final int[] DIR_SHIFT = {-9, -8, -7, 1, 9, 8, 7, -1};
    private static final long[] RIM_BOARD;
    private static final int[][] DIR;
    private static final long[][] RAY;
    private static final long[][] SQUARES_BETWEEN;
    
    static {
        /*---------- RIM_BOARD ----------*/
        RIM_BOARD = new long[NUM_OF_DIRS];
        RIM_BOARD[S] = ofRow(0);
        RIM_BOARD[E] = ofCol(Chess.NUM_OF_COLS-1);
        RIM_BOARD[N] = ofRow(Chess.NUM_OF_ROWS-1);
        RIM_BOARD[W] = ofCol(0);
        RIM_BOARD[SW] = RIM_BOARD[S] | RIM_BOARD[W];
        RIM_BOARD[SE] = RIM_BOARD[S] | RIM_BOARD[E];
        RIM_BOARD[NE] = RIM_BOARD[N] | RIM_BOARD[E];
        RIM_BOARD[NW] = RIM_BOARD[N] | RIM_BOARD[W];
        
        /*---------- DIR, RAY, SQUARES_BETWEEN ----------*/
        DIR = new int[Chess.NUM_OF_SQUARES][];
        RAY = new long[Chess.NUM_OF_SQUARES][];
        SQUARES_BETWEEN = new long[Chess.NUM_OF_SQUARES][];
        for (int from = Chess.A1; from <= Chess.H8; from++) {
            DIR[from] = new int[Chess.NUM_OF_SQUARES];
            SQUARES_BETWEEN[from] = new long[Chess.NUM_OF_SQUARES];
            for (int to = Chess.A1; to <= Chess.H8; to++) {
                DIR[from][to] = getDir(from, to);
                SQUARES_BETWEEN[from][to] = 0L;
                if (DIR[from][to] != NO_DIR) {
                    for (int sqi = from + DIR_SHIFT[DIR[from][to]]; sqi != to; sqi += DIR_SHIFT[DIR[from][to]]) {
                        SQUARES_BETWEEN[from][to] |= ofSquare(sqi);
                    }
                    //System.out.println(Chess.sqiToStr(from) + " " + Chess.sqiToStr(to));
                    //ChBitBoard.printBoard(SQUARES_BETWEEN[from][to]);
                }
            }
            RAY[from] = new long[NUM_OF_DIRS];
            for (int dir=0; dir < NUM_OF_DIRS; dir++) {
                long bb = ofSquare(from);
                for(;;) {
                    RAY[from][dir] |= bb;
                    if ((bb & RIM_BOARD[dir]) != 0L) break;
                    if (DIR_SHIFT[dir] < 0) bb >>>= -DIR_SHIFT[dir]; else bb <<= DIR_SHIFT[dir];
                }
                RAY[from][dir] &= ~ofSquare(from);
            }
        }
    }
    
    private static final boolean isDiagonal(int dir) {return dir != NO_DIR && (dir & 1) == 0;}
    
    private static final boolean areDirectionsParallel(int dir1, int dir2)
    {
        return dir1 != NO_DIR && dir2 != NO_DIR && (dir1 & 3) == (dir2 & 3);
    }
    
    private static final int getDir(int from, int to)
    {
        // used to generate DIR[from][to]
        
        int dcol = Chess.deltaCol(from, to);
        int drow = Chess.deltaRow(from, to);
        
        if (Math.abs(dcol) != Math.abs(drow) && dcol != 0 && drow != 0) return NO_DIR;
        
        dcol = sign(dcol); drow = sign(drow);
        if (dcol == -1 && drow == -1) return SW;
        if (dcol == -1 && drow ==  0) return W;
        if (dcol == -1 && drow ==  1) return NW;
        if (dcol ==  0 && drow == -1) return S;
        if (dcol ==  0 && drow ==  1) return N;
        if (dcol ==  1 && drow == -1) return SE;
        if (dcol ==  1 && drow ==  0) return E;
        if (dcol ==  1 && drow ==  1) return NE;
        return NO_DIR;
    }
    
    //======================================================================
    // precomputed bit boards
    
    private static final long[] KNIGHT_ATTACKS       = new long[Chess.NUM_OF_SQUARES];
    private static final long[] BISHOP_ATTACKS       = new long[Chess.NUM_OF_SQUARES];
    private static final long[] ROOK_ATTACKS         = new long[Chess.NUM_OF_SQUARES];
    private static final long[] QUEEN_ATTACKS        = new long[Chess.NUM_OF_SQUARES];
    private static final long[] KING_ATTACKS         = new long[Chess.NUM_OF_SQUARES];
    private static final long[] ALL_ATTACKS          = new long[Chess.NUM_OF_SQUARES];
    private static final long[] WHITE_PAWN_MOVES     = new long[Chess.NUM_OF_SQUARES];
    private static final long[] BLACK_PAWN_MOVES     = new long[Chess.NUM_OF_SQUARES];
    private static final long[] WHITE_PAWN_ATTACKS   = new long[Chess.NUM_OF_SQUARES];
    private static final long[] BLACK_PAWN_ATTACKS   = new long[Chess.NUM_OF_SQUARES];
    
    private static final long WHITE_SHORT_CASTLE_EMPTY_MASK =
        ofSquare(Chess.F1) | ofSquare(Chess.G1);
    private static final long WHITE_LONG_CASTLE_EMPTY_MASK =
        ofSquare(Chess.D1) | ofSquare(Chess.C1) | ofSquare(Chess.B1);
    private static final long BLACK_SHORT_CASTLE_EMPTY_MASK =
        ofSquare(Chess.F8) | ofSquare(Chess.G8);
    private static final long BLACK_LONG_CASTLE_EMPTY_MASK =
        ofSquare(Chess.D8) | ofSquare(Chess.C8) | ofSquare(Chess.B8);
    
    private static final long WHITE_SHORT_CASTLE_KING_CHANGE_MASK =
        ofSquare(Chess.E1) | ofSquare(Chess.G1);
    private static final long WHITE_LONG_CASTLE_KING_CHANGE_MASK =
        ofSquare(Chess.E1) | ofSquare(Chess.C1);
    private static final long BLACK_SHORT_CASTLE_KING_CHANGE_MASK =
        ofSquare(Chess.E8) | ofSquare(Chess.G8);
    private static final long BLACK_LONG_CASTLE_KING_CHANGE_MASK =
        ofSquare(Chess.E8) | ofSquare(Chess.C8);
    
    private static final long WHITE_SHORT_CASTLE_ROOK_CHANGE_MASK =
        ofSquare(Chess.F1) | ofSquare(Chess.H1);
    private static final long WHITE_LONG_CASTLE_ROOK_CHANGE_MASK =
        ofSquare(Chess.D1) | ofSquare(Chess.A1);
    private static final long BLACK_SHORT_CASTLE_ROOK_CHANGE_MASK =
        ofSquare(Chess.F8) | ofSquare(Chess.H8);
    private static final long BLACK_LONG_CASTLE_ROOK_CHANGE_MASK =
        ofSquare(Chess.D8) | ofSquare(Chess.A8);

    static {
        for (int from = Chess.A1; from <= Chess.H8; from++) {
            KNIGHT_ATTACKS[from] = 0L;
            BISHOP_ATTACKS[from] = 0L;
            ROOK_ATTACKS[from] = 0L;
            KING_ATTACKS[from] = 0L;
            WHITE_PAWN_MOVES[from] = 0L;
            BLACK_PAWN_MOVES[from] = 0L;
            WHITE_PAWN_ATTACKS[from] = 0L;
            BLACK_PAWN_ATTACKS[from] = 0L;
            for (int to = Chess.A1; to <= Chess.H8; to++) {
                if (to != from) {
                    long bbTo = ofSquare(to);
                    int dcol = Chess.deltaCol(from, to);
                    int drow = Chess.deltaRow(from, to);
                    if (Math.abs(dcol * drow) == 2) {
                        KNIGHT_ATTACKS[from] |= bbTo;
                    } else if (dcol == drow || dcol == -drow) {
                        BISHOP_ATTACKS[from] |= bbTo;
                    } else if (dcol * drow == 0) {
                        ROOK_ATTACKS [from] |= bbTo;
                    }
                    if (Math.abs(dcol) <= 1 && Math.abs(drow) <= 1) {
                        KING_ATTACKS [from] |= bbTo;
                    }
                    if (dcol ==  0 && drow ==  1) WHITE_PAWN_MOVES[from]   |= bbTo;
                    if (dcol ==  0 && drow == -1) BLACK_PAWN_MOVES[from]   |= bbTo;
                    if (dcol == -1 && drow ==  1) WHITE_PAWN_ATTACKS[from] |= bbTo;
                    if (dcol ==  1 && drow ==  1) WHITE_PAWN_ATTACKS[from] |= bbTo;
                    if (dcol == -1 && drow == -1) BLACK_PAWN_ATTACKS[from] |= bbTo;
                    if (dcol ==  1 && drow == -1) BLACK_PAWN_ATTACKS[from] |= bbTo;
                }
            }
            QUEEN_ATTACKS[from] = BISHOP_ATTACKS[from] | ROOK_ATTACKS[from];
            ALL_ATTACKS[from] = QUEEN_ATTACKS[from] | KNIGHT_ATTACKS[from];
        }
    }
    
    //======================================================================
    // settings for information flags in m_flags
    
    private final static int
        FLAG_UNKNOWN          =   0,
        FLAG_YES              =   1,
        FLAG_NO               =   2,
        FLAG_MASK             = 0x3;
    private final static int
        TO_PLAY_SHIFT         =  0,   TO_PLAY_MASK          = 0x01,
        CASTLES_SHIFT         =  1,   CASTLES_MASK          = 0x0F,
        SQI_EP_SHIFT          =  5,   SQI_EP_MASK           = 0x7F,
        HASH_COL_EP_SHIFT     = 12,   HASH_COL_EP_MASK      = 0x0F,
        CHECK_SHIFT           = 16,   CHECK_MASK            = FLAG_MASK,
        CAN_MOVE_SHIFT        = 18,   CAN_MOVE_MASK         = FLAG_MASK,
        HALF_MOVE_CLOCK_SHIFT = 20,   HALF_MOVE_CLOCK_MASK  = 0xFF,
        PLY_NUMBER_SHIFT      = 28,   PLY_NUMBER_MASK       = 0x3FF;
    
    private final static int
        OTHER_CHANGE_MOVE     = Move.OTHER_SPECIALS;
    
        // can use up to 47 bits (64 bits - 2 * 6 to store king squares - 5 for change mask)
    

    //======================================================================
    
    private long m_bbWhites, m_bbBlacks, m_bbPawns, m_bbKnights, m_bbBishops, m_bbRooks;
    private int m_whiteKing, m_blackKing;  // actually only a short (6 bit)
    private long m_flags;
    private long m_hashCode;
    
    private long[] m_bakStack;
    private int m_bakIndex;
    private short[] m_moveStack;
    private int m_moveStackIndex;

    private short[] m_moves = new short[256];   // buffer for getAllMoves, allocated once for efficiency
    
    //======================================================================
    
    public static Position createInitialPosition()
    {
        return new Position (FEN.START_POSITION, true);
    }

    public Position()
    {
        this(60);  // make room for 120 plies
    }
    
    public Position(int bufferLength)
    {
        if (PROFILE) m_numPositions++;
        
        m_bakStack = new long[4 * bufferLength];  //on average, we need about 3.75 longs to backup a position
        m_moveStack = new short[bufferLength];
        clear();
    }
    
    public Position(ImmutablePosition pos)
    {
        this();
        set(pos);
    }
    
    public Position(String fen) throws IllegalArgumentException
    {
        this(fen, true);
    }
    
    public Position(String fen, boolean strict) throws IllegalArgumentException
    {
        this();
        FEN.initFromFEN(this, fen, true);
    }
    
    //======================================================================
    
    public void clear()
    {
        super.clear();
//        m_bakIndex = 0;
    }
    
    //======================================================================
    
    public final int getToPlay()                {return      ((m_flags >> TO_PLAY_SHIFT) & TO_PLAY_MASK) == 0 ? Chess.WHITE : Chess.BLACK;}
    private final int getNotToPlay()            {return      ((m_flags >> TO_PLAY_SHIFT) & TO_PLAY_MASK) != 0 ? Chess.WHITE : Chess.BLACK;}
    public final boolean isSquareEmpty(int sqi) {return      ((m_bbWhites | m_bbBlacks) & ofSquare(sqi)) == 0L;}
    public final int getCastles()               {return (int) (m_flags >> CASTLES_SHIFT) & CASTLES_MASK;}
    public final int getSqiEP()                 {return (int)((m_flags >> SQI_EP_SHIFT) & SQI_EP_MASK) + Chess.NO_SQUARE;}
    private final int getHashColEP()            {return (int)((m_flags >> HASH_COL_EP_SHIFT) & HASH_COL_EP_MASK) + Chess.NO_SQUARE;}
    public final int getHalfMoveClock()         {return (int) (m_flags >> HALF_MOVE_CLOCK_SHIFT) & HALF_MOVE_CLOCK_MASK;}
    public final int getPlyNumber()             {return (int) (m_flags >> PLY_NUMBER_SHIFT) & PLY_NUMBER_MASK;}
    public final long getHashCode()             {return m_hashCode;}
    
    public final int getStone(int sqi)
    {
        if (PROFILE) m_numGetSquare++;
        
        long bbSqi = ofSquare(sqi);
        if ((m_bbWhites & bbSqi) != 0L) {
            if ((m_bbPawns & bbSqi) != 0L) return Chess.WHITE_PAWN;
            if ((m_bbBishops & bbSqi) != 0L)
                return ((m_bbRooks & bbSqi) != 0L ? Chess.WHITE_QUEEN : Chess.WHITE_BISHOP);
            if ((m_bbKnights & bbSqi) != 0L) return Chess.WHITE_KNIGHT;
            if (sqi == m_whiteKing) return Chess.WHITE_KING;
            return Chess.WHITE_ROOK;
        } else if ((m_bbBlacks & bbSqi) != 0L) {
            if ((m_bbPawns & bbSqi) != 0L) return Chess.BLACK_PAWN;
            if ((m_bbBishops & bbSqi) != 0L)
                return ((m_bbRooks & bbSqi) != 0L ? Chess.BLACK_QUEEN : Chess.BLACK_BISHOP);
            if ((m_bbKnights & bbSqi) != 0L) return Chess.BLACK_KNIGHT;
            if (sqi == m_blackKing) return Chess.BLACK_KING;
            return Chess.BLACK_ROOK;
        } else {
            return Chess.NO_STONE;
        }
    }
    
    public final int getPiece(int sqi)
    {
        if (PROFILE) m_numGetSquare++; // TODO
        
        long bbSqi = ofSquare(sqi);
        if ((m_bbPawns & bbSqi) != 0L) return Chess.PAWN;
        if ((m_bbKnights & bbSqi) != 0L) return Chess.KNIGHT;
        if ((m_bbBishops & bbSqi) != 0L)
            return ((m_bbRooks & bbSqi) != 0L ? Chess.QUEEN : Chess.BISHOP);
        if ((m_bbRooks & bbSqi) != 0L) return Chess.ROOK;
        if (sqi == m_whiteKing || sqi == m_blackKing) return Chess.KING;
        return Chess.NO_PIECE;
    }
    
    public final int getColor(int sqi)
    {
        if (PROFILE) m_numGetSquare++; // TODO
        
        long bbSqi = ofSquare(sqi);
        if ((m_bbWhites & bbSqi) != 0L) return Chess.WHITE;
        if ((m_bbBlacks & bbSqi) != 0L) return Chess.BLACK;
        return Chess.NOBODY;
    }
    
    private final long getBitBoard(int stone)
    {
        switch(stone) {
            case Chess.NO_STONE:     return 0L;
            case Chess.WHITE_KING:   return ofSquare(m_whiteKing);
            case Chess.WHITE_PAWN:   return m_bbPawns & m_bbWhites;
            case Chess.WHITE_KNIGHT: return m_bbKnights & m_bbWhites;
            case Chess.WHITE_BISHOP: return m_bbBishops & (~m_bbRooks) & m_bbWhites;
            case Chess.WHITE_ROOK:   return m_bbRooks & (~m_bbBishops) & m_bbWhites;
            case Chess.WHITE_QUEEN:  return m_bbBishops & m_bbRooks & m_bbWhites;
            case Chess.BLACK_KING:   return ofSquare(m_blackKing);
            case Chess.BLACK_PAWN:   return m_bbPawns & m_bbBlacks;
            case Chess.BLACK_KNIGHT: return m_bbKnights & m_bbBlacks;
            case Chess.BLACK_BISHOP: return m_bbBishops & (~m_bbRooks) & m_bbBlacks;
            case Chess.BLACK_ROOK:   return m_bbRooks & (~m_bbBishops) & m_bbBlacks;
            case Chess.BLACK_QUEEN:  return m_bbBishops & m_bbRooks & m_bbBlacks;
            default:
                throw new RuntimeException("Unknown stone: " + stone);
        }
    }

// unused
//    
//    private final long getBitBoard(int piece, int color)
//    {
//        long bb;
//        switch(piece) {
//            case Chess.NO_PIECE: return 0L;
//            case Chess.KING:     return ofSquare(color == Chess.WHITE ? m_whiteKing : m_blackKing);
//            case Chess.PAWN:     bb = m_bbPawns; break;
//            case Chess.KNIGHT:   bb = m_bbKnights; break;
//            case Chess.BISHOP:   bb = m_bbBishops & (~m_bbRooks); break;
//            case Chess.ROOK:     bb = m_bbRooks & (~m_bbBishops); break;
//            case Chess.QUEEN:    bb = m_bbBishops & m_bbRooks; break;
//            default:
//                throw new RuntimeException("Unknown piece: " + piece);
//        }
//        if (color == Chess.WHITE) return bb & m_bbWhites; else return bb & m_bbBlacks;
//    }
    
    //======================================================================
    
    public final void setStone(int sqi, int stone)
    {
        if (PROFILE) m_numSet++;
        
        if (DEBUG) System.out.println("Set " + Chess.stoneToChar(stone) + " to " + Chess.sqiToStr(sqi));
        
        int old = getStone(sqi);
        if (old != stone) {
            long bbSqi = ofSquare(sqi);
            
            /*---------- remove stone from sqi ----------*/
            switch(old) {
                case Chess.NO_STONE:     break;
                case Chess.WHITE_KING:   m_bbWhites &= ~bbSqi; m_whiteKing = Chess.NO_SQUARE; break;
                case Chess.WHITE_PAWN:   m_bbWhites &= ~bbSqi; m_bbPawns &= ~bbSqi; break;
                case Chess.WHITE_KNIGHT: m_bbWhites &= ~bbSqi; m_bbKnights &= ~bbSqi; break;
                case Chess.WHITE_BISHOP: m_bbWhites &= ~bbSqi; m_bbBishops &= ~bbSqi; break;
                case Chess.WHITE_ROOK:   m_bbWhites &= ~bbSqi; m_bbRooks &= ~bbSqi; break;
                case Chess.WHITE_QUEEN:  m_bbWhites &= ~bbSqi; m_bbBishops &= ~bbSqi; m_bbRooks &= ~bbSqi; break;
                case Chess.BLACK_KING:   m_bbBlacks &= ~bbSqi; m_blackKing = Chess.NO_SQUARE; break;
                case Chess.BLACK_PAWN:   m_bbBlacks &= ~bbSqi; m_bbPawns &= ~bbSqi; break;
                case Chess.BLACK_KNIGHT: m_bbBlacks &= ~bbSqi; m_bbKnights &= ~bbSqi; break;
                case Chess.BLACK_BISHOP: m_bbBlacks &= ~bbSqi; m_bbBishops &= ~bbSqi; break;
                case Chess.BLACK_ROOK:   m_bbBlacks &= ~bbSqi; m_bbRooks &= ~bbSqi; break;
                case Chess.BLACK_QUEEN:  m_bbBlacks &= ~bbSqi; m_bbBishops &= ~bbSqi; m_bbRooks &= ~bbSqi; break;
            }
            
            /*---------- add new stone to sqi ----------*/
            switch(stone) {
                case Chess.NO_STONE: break;
                case Chess.WHITE_KING:   m_bbWhites |= bbSqi; m_whiteKing = sqi; break;
                case Chess.WHITE_PAWN:   m_bbWhites |= bbSqi; m_bbPawns |= bbSqi; break;
                case Chess.WHITE_KNIGHT: m_bbWhites |= bbSqi; m_bbKnights |= bbSqi; break;
                case Chess.WHITE_BISHOP: m_bbWhites |= bbSqi; m_bbBishops |= bbSqi; break;
                case Chess.WHITE_ROOK:   m_bbWhites |= bbSqi; m_bbRooks |= bbSqi; break;
                case Chess.WHITE_QUEEN:  m_bbWhites |= bbSqi; m_bbBishops |= bbSqi; m_bbRooks |= bbSqi; break;
                case Chess.BLACK_KING:   m_bbBlacks |= bbSqi; m_blackKing = sqi; break;
                case Chess.BLACK_PAWN:   m_bbBlacks |= bbSqi; m_bbPawns |= bbSqi; break;
                case Chess.BLACK_KNIGHT: m_bbBlacks |= bbSqi; m_bbKnights |= bbSqi; break;
                case Chess.BLACK_BISHOP: m_bbBlacks |= bbSqi; m_bbBishops |= bbSqi; break;
                case Chess.BLACK_ROOK:   m_bbBlacks |= bbSqi; m_bbRooks |= bbSqi; break;
                case Chess.BLACK_QUEEN:  m_bbBlacks |= bbSqi; m_bbBishops |= bbSqi; m_bbRooks |= bbSqi; break;
            }
            
            /*---------- hash value ----------*/
            if (old != Chess.NO_STONE)   m_hashCode ^= s_hashMod[sqi][old   - Chess.MIN_STONE];
            if (stone != Chess.NO_STONE) m_hashCode ^= s_hashMod[sqi][stone - Chess.MIN_STONE];
            //System.out.println("hash code set: " + m_hashCode);
            
            /*---------- listeners ----------*/
            if (m_notifyListeners && m_listeners != null) fireSquareChanged(sqi);
        }
    }
    
    public final void setPlyNumber(int plyNumber)
    {
//        new Exception("setPlyNumber " + plyNumber).printStackTrace();
        if (DEBUG) System.out.println("setPlyNumber " + plyNumber);
        long flags = m_flags;
        m_flags &= ~(PLY_NUMBER_MASK << PLY_NUMBER_SHIFT);
        m_flags |= (long)plyNumber << PLY_NUMBER_SHIFT;
        if (m_flags != flags) {
            if (m_notifyListeners && m_listeners != null) firePlyNumberChanged();
        }
//        if (plyNumber != getPlyNumber()) new Exception("Ply number " + plyNumber).printStackTrace();
    }
    
    private final void incPlyNumber()
    {
//        System.out.println("incPlyNumber");
        if (DEBUG) System.out.println("incPlyNumber");
        m_flags += 1L << PLY_NUMBER_SHIFT;
        if (m_notifyListeners && m_listeners != null) firePlyNumberChanged();
    }  
    
//    private final void decPlyNumber()
//    {
////        System.out.println("decPlyNumber");
//        if (DEBUG) System.out.println("decPlyNumber");
//        m_flags -= 1L << PLY_NUMBER_SHIFT;
//        if (m_notifyListeners && m_listeners != null) firePlyNumberChanged();
//    }  
    
    public void setHalfMoveClock(int halfMoveClock)
    {
        if (DEBUG) System.out.println("setHalfMoveClock " + halfMoveClock);
        long flags = m_flags;
        m_flags &= ~(HALF_MOVE_CLOCK_MASK << HALF_MOVE_CLOCK_SHIFT);
        m_flags |= (long)halfMoveClock << HALF_MOVE_CLOCK_SHIFT;
        if (m_flags != flags) {
            if (m_notifyListeners && m_listeners != null) fireHalfMoveClockChanged();
        }
    }
    
    public final void setCastles(int castles)
    {
        if (DEBUG) System.out.println("setCastles " + castles);
        int oldCastles = getCastles();
        if (oldCastles != castles) {
            m_flags &= ~(CASTLES_MASK << CASTLES_SHIFT);
            m_flags |= castles << CASTLES_SHIFT;
            /*---------- hash value ----------*/
            m_hashCode ^= s_hashCastleMod[oldCastles];
            m_hashCode ^= s_hashCastleMod[castles];
            //System.out.println("hash code castles: " + m_hashCode);
            /*---------- listeners ----------*/
            if (m_notifyListeners && m_listeners != null) fireCastlesChanged();
        }
    }
    
    public void setSqiEP(int sqiEP)
    {
        if (DEBUG) System.out.println("setSqiEP " + sqiEP);
        if (getSqiEP() != sqiEP) {
            m_flags &= ~(SQI_EP_MASK << SQI_EP_SHIFT);
            m_flags |= (sqiEP - Chess.NO_SQUARE) << SQI_EP_SHIFT;
            
            /*---------- hash value ----------*/
            int hashColEP = getHashColEP();
            if (hashColEP != Chess.NO_SQUARE) m_hashCode ^= s_hashEPMod[hashColEP];
            
            hashColEP = (sqiEP == Chess.NO_COL ? Chess.NO_COL : Chess.sqiToCol(sqiEP));
            // ignore ep square for hashing if there is no opponent pawn to actually capture the pawn ep
            // only in this case is the position different
            
//            if (sqiEP < 0 || sqiEP > 63) {
//                System.out.println(sqiEP);
//            }
            
            if (sqiEP != Chess.NO_COL) {
                if (sqiEP < Chess.A4) {   // test is independent of whether toplay is set before or afterwards
                    if ((WHITE_PAWN_ATTACKS[sqiEP] & m_bbPawns & m_bbBlacks) == 0L) {
                        hashColEP = Chess.NO_COL;
                    }
                } else {
                    if ((BLACK_PAWN_ATTACKS[sqiEP] & m_bbPawns & m_bbWhites) == 0L) {
                        hashColEP = Chess.NO_COL;
                    }
                }
                if (hashColEP != Chess.NO_COL) m_hashCode ^= s_hashEPMod[hashColEP];            
            }
            m_flags &= ~(HASH_COL_EP_MASK << HASH_COL_EP_SHIFT);
            // encode column of ep square in hash code (NO_SQUARE if no ep)
            m_flags |= (hashColEP - Chess.NO_SQUARE) << HASH_COL_EP_SHIFT;
            //System.out.println("hash code ep: " + m_hashCode);
            
            /*---------- listeners ----------*/
            if (m_notifyListeners && m_listeners != null) fireSqiEPChanged();
        }
    }
    
    public final void setToPlay(int toPlay)
    {
        if (DEBUG) System.out.println("setToPlay " + toPlay);
        if (toPlay != getToPlay()) {
            toggleToPlay();
        }
    }
    
    public final void toggleToPlay()
    {
        if (DEBUG) System.out.println("toggleToPlay");
        m_flags ^= (TO_PLAY_MASK << TO_PLAY_SHIFT);
        /*---------- hash value ----------*/
        m_hashCode ^= HASH_TOPLAY_MULT;
        //System.out.println("hash code toPlay: " + m_hashCode);
        /*---------- listeners ----------*/
        if (m_notifyListeners && m_listeners != null) fireToPlayChanged();
    }
    
    private final void setMove(short move)
    {
        boolean increaseHalfMoveClock = true;
        int sqiEP = Chess.NO_SQUARE;
        long squaresChanged = 0L;
        
        /*---------- moves the pieces ----------*/
        if (Move.isCastle(move)) {
            if (getToPlay() == Chess.WHITE) {
                if (Move.isShortCastle(move)) {
                    squaresChanged = WHITE_SHORT_CASTLE_KING_CHANGE_MASK | WHITE_SHORT_CASTLE_ROOK_CHANGE_MASK;
                    m_bbWhites ^= WHITE_SHORT_CASTLE_KING_CHANGE_MASK | WHITE_SHORT_CASTLE_ROOK_CHANGE_MASK;
                    m_whiteKing = Chess.G1;
                    m_bbRooks  ^= WHITE_SHORT_CASTLE_ROOK_CHANGE_MASK;
                    m_hashCode ^= s_hashMod[Chess.E1][Chess.WHITE_KING - Chess.MIN_STONE];
                    m_hashCode ^= s_hashMod[Chess.F1][Chess.WHITE_ROOK - Chess.MIN_STONE];
                    m_hashCode ^= s_hashMod[Chess.G1][Chess.WHITE_KING - Chess.MIN_STONE];
                    m_hashCode ^= s_hashMod[Chess.H1][Chess.WHITE_ROOK - Chess.MIN_STONE];
                } else {
                    squaresChanged = WHITE_LONG_CASTLE_KING_CHANGE_MASK | WHITE_LONG_CASTLE_ROOK_CHANGE_MASK;
                    m_bbWhites ^= WHITE_LONG_CASTLE_KING_CHANGE_MASK | WHITE_LONG_CASTLE_ROOK_CHANGE_MASK;
                    m_whiteKing = Chess.C1;
                    m_bbRooks  ^= WHITE_LONG_CASTLE_ROOK_CHANGE_MASK;
                    m_hashCode ^= s_hashMod[Chess.E1][Chess.WHITE_KING - Chess.MIN_STONE];
                    m_hashCode ^= s_hashMod[Chess.D1][Chess.WHITE_ROOK - Chess.MIN_STONE];
                    m_hashCode ^= s_hashMod[Chess.C1][Chess.WHITE_KING - Chess.MIN_STONE];
                    m_hashCode ^= s_hashMod[Chess.A1][Chess.WHITE_ROOK - Chess.MIN_STONE];
                }
                excludeCastles(WHITE_CASTLE);
            } else {
                if (Move.isShortCastle(move)) {
                    squaresChanged = BLACK_SHORT_CASTLE_KING_CHANGE_MASK | BLACK_SHORT_CASTLE_ROOK_CHANGE_MASK;
                    m_bbBlacks ^= BLACK_SHORT_CASTLE_KING_CHANGE_MASK | BLACK_SHORT_CASTLE_ROOK_CHANGE_MASK;
                    m_blackKing = Chess.G8;
                    m_bbRooks  ^= BLACK_SHORT_CASTLE_ROOK_CHANGE_MASK;
                    m_hashCode ^= s_hashMod[Chess.E8][Chess.BLACK_KING - Chess.MIN_STONE];
                    m_hashCode ^= s_hashMod[Chess.F8][Chess.BLACK_ROOK - Chess.MIN_STONE];
                    m_hashCode ^= s_hashMod[Chess.G8][Chess.BLACK_KING - Chess.MIN_STONE];
                    m_hashCode ^= s_hashMod[Chess.H8][Chess.BLACK_ROOK - Chess.MIN_STONE];
                } else {
                    squaresChanged = BLACK_LONG_CASTLE_KING_CHANGE_MASK | BLACK_LONG_CASTLE_ROOK_CHANGE_MASK;
                    m_bbBlacks ^= BLACK_LONG_CASTLE_KING_CHANGE_MASK | BLACK_LONG_CASTLE_ROOK_CHANGE_MASK;
                    m_blackKing = Chess.C8;
                    m_bbRooks  ^= BLACK_LONG_CASTLE_ROOK_CHANGE_MASK;
                    m_hashCode ^= s_hashMod[Chess.E8][Chess.BLACK_KING - Chess.MIN_STONE];
                    m_hashCode ^= s_hashMod[Chess.D8][Chess.BLACK_ROOK - Chess.MIN_STONE];
                    m_hashCode ^= s_hashMod[Chess.C8][Chess.BLACK_KING - Chess.MIN_STONE];
                    m_hashCode ^= s_hashMod[Chess.A8][Chess.BLACK_ROOK - Chess.MIN_STONE];
                }
                excludeCastles(BLACK_CASTLE);
            }
        } else {
            int sqiFrom = Move.getFromSqi(move);
            int sqiTo = Move.getToSqi(move);
            
            long bbFrom = ofSquare(sqiFrom);
            long bbTo = ofSquare(sqiTo);
            long bbFromTo = bbFrom | bbTo;
            squaresChanged |= bbFromTo;
            
            if (Move.isCapturing(move)) {
                if (DEBUG) if (isSquareEmpty(sqiTo) && !(getSqiEP() == sqiTo)) throw new RuntimeException("Capture square is empty " + Integer.toBinaryString(move) + " " + Move.getString(move));
                if (DEBUG) if (getColor(sqiTo) == getToPlay()) throw new RuntimeException("Cannot capture own piece " + Integer.toBinaryString(move) + " " + Move.getString(move));
                if (DEBUG) if (getPiece(sqiTo) == Chess.KING) throw new RuntimeException("Cannot capture the king" + Integer.toBinaryString(move) + " " + Move.getString(move));
                
                long notBBTo;
                if (Move.isEPMove(move)) {
                    int pawnSqi = getSqiEP() + (getToPlay() == Chess.WHITE ? -Chess.NUM_OF_COLS : Chess.NUM_OF_COLS);
                    notBBTo = ~ofSquare(pawnSqi);
                    squaresChanged |= ~notBBTo;
                    m_hashCode ^= s_hashMod[pawnSqi][(getToPlay() == Chess.WHITE ? Chess.BLACK_PAWN : Chess.WHITE_PAWN) - Chess.MIN_STONE];
                } else {
                    notBBTo = ~bbTo;
//                    int capturedStone = Chess.pieceToStone(ChMove.getCapturedPiece(move), getNotToPlay());
                    int capturedStone = getStone(Move.getToSqi(move));
                    m_hashCode ^= s_hashMod[sqiTo][capturedStone - Chess.MIN_STONE];
                }
                //                this.printBoard(notBBTo);
                //TODO:  remove all bits -> faster than switching?
                m_bbWhites &= notBBTo; m_bbBlacks &= notBBTo;
                m_bbPawns  &= notBBTo; m_bbKnights &= notBBTo; m_bbBishops &= notBBTo; m_bbRooks &= notBBTo;
                //                this.printBoard(m_bbWhites);this.printBoard(m_bbBlacks);this.printBoard(m_bbPawns);
                increaseHalfMoveClock = false;
            }
            if (Move.isPromotion(move)) {
//                System.out.println("PROMOTION");
//                System.out.println(move + " " + ChMove.getBinaryString(move) + " " + ChMove.getString(move));
                int promotionStone = Chess.pieceToStone(Move.getPromotionPiece(move), getToPlay());
                if (getToPlay() == Chess.WHITE) {
                    m_bbWhites ^= bbFromTo; m_bbPawns ^= bbFrom;
                    m_hashCode ^= s_hashMod[sqiFrom][Chess.WHITE_PAWN - Chess.MIN_STONE];
                    switch(promotionStone) {
                        case Chess.WHITE_KNIGHT: m_bbKnights ^= bbTo; break;
                        case Chess.WHITE_BISHOP: m_bbBishops ^= bbTo; break;
                        case Chess.WHITE_ROOK:   m_bbRooks   ^= bbTo; break;
                        case Chess.WHITE_QUEEN:  m_bbBishops ^= bbTo; m_bbRooks ^= bbTo; break;
                        default: throw new RuntimeException("Illegal promotion stone " + promotionStone + " " + Chess.stoneToChar(promotionStone));
                    }
                } else {
                    m_bbBlacks ^= bbFromTo; m_bbPawns ^= bbFrom;
                    m_hashCode ^= s_hashMod[sqiFrom][Chess.BLACK_PAWN - Chess.MIN_STONE];
                    switch(promotionStone) {
                        case Chess.BLACK_KNIGHT: m_bbKnights ^= bbTo; break;
                        case Chess.BLACK_BISHOP: m_bbBishops ^= bbTo; break;
                        case Chess.BLACK_ROOK:   m_bbRooks   ^= bbTo; break;
                        case Chess.BLACK_QUEEN:  m_bbBishops ^= bbTo; m_bbRooks ^= bbTo; break;
                        default: throw new RuntimeException("Illegal promotion stone " + promotionStone + " " + Chess.stoneToChar(promotionStone));
                    }
                }
                m_hashCode ^= s_hashMod[sqiTo][promotionStone - Chess.MIN_STONE];
                increaseHalfMoveClock = false;
            } else {
//                int stone = Chess.pieceToStone(ChMove.getMovingPiece(move), getToPlay());
                int stone = getStone(Move.getFromSqi(move));
                switch(stone) {
                    case Chess.NO_STONE:     {System.out.println(this); throw new RuntimeException("Moving stone is non-existent " + Move.getString(move));}
                    case Chess.WHITE_KING:   m_bbWhites ^= bbFromTo; m_whiteKing = sqiTo; break;
                    case Chess.WHITE_PAWN:   m_bbWhites ^= bbFromTo; m_bbPawns   ^= bbFromTo; increaseHalfMoveClock = false;
                                             if (sqiTo - sqiFrom == 2 * Chess.NUM_OF_COLS) sqiEP = sqiTo - Chess.NUM_OF_COLS;
                                             break;
                    case Chess.WHITE_KNIGHT: m_bbWhites ^= bbFromTo; m_bbKnights ^= bbFromTo; break;
                    case Chess.WHITE_BISHOP: m_bbWhites ^= bbFromTo; m_bbBishops ^= bbFromTo; break;
                    case Chess.WHITE_ROOK:   m_bbWhites ^= bbFromTo; m_bbRooks   ^= bbFromTo; break;
                    case Chess.WHITE_QUEEN:  m_bbWhites ^= bbFromTo; m_bbBishops ^= bbFromTo; m_bbRooks ^= bbFromTo; break;
                    case Chess.BLACK_KING:   m_bbBlacks ^= bbFromTo; m_blackKing = sqiTo; break;
                    case Chess.BLACK_PAWN:   m_bbBlacks ^= bbFromTo; m_bbPawns   ^= bbFromTo; increaseHalfMoveClock = false;
                                             if (sqiFrom - sqiTo == 2 * Chess.NUM_OF_COLS) sqiEP = sqiTo + Chess.NUM_OF_COLS;
                                             break;
                    case Chess.BLACK_KNIGHT: m_bbBlacks ^= bbFromTo; m_bbKnights ^= bbFromTo; break;
                    case Chess.BLACK_BISHOP: m_bbBlacks ^= bbFromTo; m_bbBishops ^= bbFromTo; break;
                    case Chess.BLACK_ROOK:   m_bbBlacks ^= bbFromTo; m_bbRooks   ^= bbFromTo; break;
                    case Chess.BLACK_QUEEN:  m_bbBlacks ^= bbFromTo; m_bbBishops ^= bbFromTo; m_bbRooks ^= bbFromTo; break;
                }
                m_hashCode ^= s_hashMod[sqiFrom][stone - Chess.MIN_STONE];
                m_hashCode ^= s_hashMod[sqiTo][stone - Chess.MIN_STONE];
            }
            
            /*---------- update castles ----------*/
            int castles = getCastles();
            if (castles != NO_CASTLES) {
                if      (sqiFrom == Chess.A1 || sqiTo == Chess.A1) {castles &= ~WHITE_LONG_CASTLE;}
                else if (sqiFrom == Chess.H1 || sqiTo == Chess.H1) {castles &= ~WHITE_SHORT_CASTLE;}
                else if (sqiFrom == Chess.A8 || sqiTo == Chess.A8) {castles &= ~BLACK_LONG_CASTLE;}
                else if (sqiFrom == Chess.H8 || sqiTo == Chess.H8) {castles &= ~BLACK_SHORT_CASTLE;}
                else if (sqiFrom == Chess.E1) {castles &= ~WHITE_CASTLE;}
                else if (sqiFrom == Chess.E8) {castles &= ~BLACK_CASTLE;}
                setCastles(castles);
            }
        }
        
        /*---------- update toplay, ply number ----------*/
        incPlyNumber();
        toggleToPlay();
        
        /*---------- notify listeners ----------*/
        if (m_notifyListeners && m_listeners != null) {
            // enabled this to be sure that changes are sent
//            for (int i=0; i<Chess.NUM_OF_SQUARES; i++) fireSquareChanged(i);
            
            while (squaresChanged != 0L) {
                int sqi = getFirstSqi(squaresChanged);
                fireSquareChanged(sqi);
                squaresChanged &= squaresChanged - 1;
            }
        }
        
        /*---------- update ep square ----------*/
        setSqiEP(sqiEP);
        
        /*---------- update half move clock ----------*/
        if (increaseHalfMoveClock) incHalfMoveClock(); else resetHalfMoveClock();
        
        /*---------- store move in stack ----------*/
        int index = m_moveStackIndex;
        checkMoveStack();
        m_moveStack[index] = move;
        m_moveStackIndex++;
    }
    
    private void checkMoveStack()
    {
        if (m_moveStackIndex >= m_moveStack.length) {
            short[] newMoveStack = new short[m_moveStack.length * 2];
            System.arraycopy(m_moveStack, 0, newMoveStack, 0, m_moveStack.length);
            m_moveStack = newMoveStack;
//            if (index >= m_moveStack.length) System.out.println("Too big");
        }
    }
    
    private void checkBackupStack()
    {
        if (m_bakIndex + 7 >= m_bakStack.length) {
            long[] oldBak = m_bakStack;
            m_bakStack = new long[2 * oldBak.length];
            System.arraycopy(oldBak, 0, m_bakStack, 0, oldBak.length);
//            System.out.println(m_bakIndex + " " + m_bakStack.length);
        }
    }
    
    private long getAllFlags(int changeMask)
    {
        long allFlags  = (((m_flags << 6) | (long)m_whiteKing) << 6) | (long)m_blackKing;
        return (allFlags << 5) | changeMask;
    }
    
    public void takeBaseline()
    {
        checkBackupStack();
        
        m_bakStack[m_bakIndex++] = m_hashCode;
        m_bakStack[m_bakIndex++] = m_bbWhites;
        m_bakStack[m_bakIndex++] = m_bbPawns;
        m_bakStack[m_bakIndex++] = m_bbKnights;
        m_bakStack[m_bakIndex++] = m_bbBishops;
        m_bakStack[m_bakIndex++] = m_bbRooks;
        
        int changeMask = 0x1F;
        long bakFlags  = (((m_flags << 6) | (long)m_whiteKing) << 6) | (long)m_blackKing;
        m_bakStack[m_bakIndex++] = (bakFlags << 5) | changeMask;
        m_bakStack[m_bakIndex] = 0L;  // prevent redos
        
        checkMoveStack();
        m_moveStack[m_moveStackIndex++] = OTHER_CHANGE_MOVE;
    }
    
    public void doMove(short move) throws IllegalMoveException
    {
        doMoveNoMoveListeners(move);
        if (m_notifyListeners && m_changeListeners != null) fireMoveDone(move);
    }
    
    private final void doMoveNoMoveListeners(short move) throws IllegalMoveException
    {
        if (PROFILE) m_numDoMove++;
        
        boolean notify = m_notifyPositionChanged;
        m_notifyPositionChanged = false;
        
        if (!Move.isValid(move)) throw new IllegalMoveException (move);
        
        /*---------- backup of  current state ----------*/
        checkBackupStack();
        
        /*---------- take baseline ----------*/
        long bakWhites   = m_bbWhites;
        long bakPawns    = m_bbPawns;
        long bakKnights  = m_bbKnights;
        long bakBishops  = m_bbBishops;
        long bakRooks    = m_bbRooks;
        long bakFlags    = (((m_flags << 6) | (long)m_whiteKing) << 6) | (long)m_blackKing;  //       (((((long)m_whiteKing) << 6) | m_blackKing) << 47) | m_flags;
        m_bakStack[m_bakIndex++] = m_hashCode;
        
        /*---------- delete position properties in m_flags ----------*/
        m_flags &= ~(CHECK_MASK << CHECK_SHIFT);        // delete isCheck info
        m_flags &= ~(CAN_MOVE_MASK << CAN_MOVE_SHIFT);  // delete canMove info
        
        /*---------- move pieces ----------*/
        setMove(move);
                
        /*---------- compare state and push changes ----------*/
        // only push data that have actually changed
        // on average, we need about 3.75 longs per position (instead of 7 if we back up all)
        // (hashCode, flags, 1/2 whites, 1 piece bb, plus sometimes another piece bb for captures, promotions, castles)
        int changeMask = 0;
        if (bakWhites  != m_bbWhites)  {m_bakStack[m_bakIndex++] = bakWhites;  changeMask++;} changeMask <<= 1;
        if (bakPawns   != m_bbPawns)   {m_bakStack[m_bakIndex++] = bakPawns;   changeMask++;} changeMask <<= 1;
        if (bakKnights != m_bbKnights) {m_bakStack[m_bakIndex++] = bakKnights; changeMask++;} changeMask <<= 1;
        if (bakBishops != m_bbBishops) {m_bakStack[m_bakIndex++] = bakBishops; changeMask++;} changeMask <<= 1;
        if (bakRooks   != m_bbRooks)   {m_bakStack[m_bakIndex++] = bakRooks;   changeMask++;}
        m_bakStack[m_bakIndex++] = (bakFlags << 5) | changeMask;
        m_bakStack[m_bakIndex] = 0L;

        m_notifyPositionChanged = notify;
        
        if (PROFILE) m_numLongsBackuped += numOfBitsSet(changeMask) + 2;
        
        if (DEBUG) System.out.println("I did a move " + Move.getString(move));
    }
    
    public boolean canUndoMove()
    {
        return m_bakIndex > 0;
    }
    
    public boolean undoMove()
    {
        boolean res = undoMoveNoMoveListeners();
        if (m_notifyListeners && m_changeListeners != null) fireMoveUndone();
        return res;
    }
    
    private boolean undoMoveNoMoveListeners()
    {
        if (PROFILE) m_numUndoMove++;
        
        boolean notify = m_notifyPositionChanged;
        m_notifyPositionChanged = false;
        
        if (m_bakIndex > 0) {
            long bbWhites = m_bbWhites, bbBlacks = m_bbBlacks;
            int sqiEP = getSqiEP();
            int castles = getCastles();
            
            /*---------- reset pieces ----------*/
            long allFlags  = m_bakStack[--m_bakIndex];
            int changeMask = (int)(allFlags & 0x1F); allFlags >>>=  5;
            
            int newChangeMask = 0;
            if ((changeMask & 1) != 0) {m_bakStack[m_bakIndex] = m_bbRooks;   m_bbRooks   = m_bakStack[--m_bakIndex]; newChangeMask++;} changeMask >>>= 1; newChangeMask <<= 1;
            if ((changeMask & 1) != 0) {m_bakStack[m_bakIndex] = m_bbBishops; m_bbBishops = m_bakStack[--m_bakIndex]; newChangeMask++;} changeMask >>>= 1; newChangeMask <<= 1;
            if ((changeMask & 1) != 0) {m_bakStack[m_bakIndex] = m_bbKnights; m_bbKnights = m_bakStack[--m_bakIndex]; newChangeMask++;} changeMask >>>= 1; newChangeMask <<= 1;
            if ((changeMask & 1) != 0) {m_bakStack[m_bakIndex] = m_bbPawns;   m_bbPawns   = m_bakStack[--m_bakIndex]; newChangeMask++;} changeMask >>>= 1; newChangeMask <<= 1;
            if ((changeMask & 1) != 0) {m_bakStack[m_bakIndex] = m_bbWhites;  m_bbWhites  = m_bakStack[--m_bakIndex]; newChangeMask++;}            
            m_bakStack[m_bakIndex] = m_hashCode; m_hashCode = m_bakStack[--m_bakIndex];
            m_bakStack[m_bakIndex] = getAllFlags(newChangeMask);
            
            m_blackKing    = (int)(allFlags & 0x3F); allFlags >>>=  6;
            m_whiteKing    = (int)(allFlags & 0x3F); allFlags >>>=  6;
            m_flags        =       allFlags;
            m_bbBlacks = ((1L << m_blackKing) | m_bbPawns | m_bbKnights | m_bbBishops | m_bbRooks) & (~m_bbWhites);
            
            m_moveStackIndex--;
            
            if (DEBUG) System.out.println("I undid the last move");
            
            //---------- notify listeners ----------
            if (m_notifyListeners) {
                if (m_listeners != null) {
//                    // enable this to be sure that changes are sent
//                    long squaresChanged = ~0L;
                    long squaresChanged = (bbWhites ^ m_bbWhites) | (bbBlacks ^ m_bbBlacks);
                    while (squaresChanged != 0L) {
                        int sqi = getFirstSqi(squaresChanged);
                        fireSquareChanged(sqi);
                        squaresChanged &= squaresChanged - 1;
                    }
                    if (getSqiEP() != sqiEP) fireSqiEPChanged();
                    if (getCastles() != castles) fireCastlesChanged();
                    fireHalfMoveClockChanged();
                    fireToPlayChanged();
                }
            }

            m_notifyPositionChanged = notify;
            return true;
            
        } else {
            m_notifyPositionChanged = notify;
            return false;
        }
    }
    
    public boolean canRedoMove()
    {
        return m_bakIndex < m_bakStack.length && m_bakStack[m_bakIndex] != 0;
    }
    
    public boolean redoMove()
    {
        boolean res = redoMoveNoMoveListeners();
        if (m_notifyListeners && m_changeListeners != null) fireMoveDone(getLastShortMove());
        return res;
    }
    
    private final boolean redoMoveNoMoveListeners()
    {
//        if (PROFILE) m_numRedoMove++;
        
        boolean notify = m_notifyPositionChanged;
        m_notifyPositionChanged = false;
        
        if (canRedoMove()) {
            long bbWhites = m_bbWhites, bbBlacks = m_bbBlacks;
            int sqiEP = getSqiEP();
            int castles = getCastles();
            
            /*---------- reset pieces ----------*/
            long allFlags  = m_bakStack[m_bakIndex];
            int changeMask = (int)(allFlags & 0x1F); allFlags >>>=  5;
            
            int newChangeMask = 0;
            m_bakStack[m_bakIndex] = m_hashCode; m_hashCode = m_bakStack[++m_bakIndex];
            if ((changeMask & 1) != 0) {m_bakStack[m_bakIndex] = m_bbWhites;  m_bbWhites  = m_bakStack[++m_bakIndex]; newChangeMask++;} changeMask >>>= 1; newChangeMask <<= 1;
            if ((changeMask & 1) != 0) {m_bakStack[m_bakIndex] = m_bbPawns;   m_bbPawns   = m_bakStack[++m_bakIndex]; newChangeMask++;} changeMask >>>= 1; newChangeMask <<= 1;
            if ((changeMask & 1) != 0) {m_bakStack[m_bakIndex] = m_bbKnights; m_bbKnights = m_bakStack[++m_bakIndex]; newChangeMask++;} changeMask >>>= 1; newChangeMask <<= 1;
            if ((changeMask & 1) != 0) {m_bakStack[m_bakIndex] = m_bbBishops; m_bbBishops = m_bakStack[++m_bakIndex]; newChangeMask++;} changeMask >>>= 1; newChangeMask <<= 1;
            if ((changeMask & 1) != 0) {m_bakStack[m_bakIndex] = m_bbRooks;   m_bbRooks   = m_bakStack[++m_bakIndex]; newChangeMask++;}            
            m_bakStack[m_bakIndex++] = getAllFlags(newChangeMask);
            
            m_blackKing    = (int)(allFlags & 0x3F); allFlags >>>=  6;
            m_whiteKing    = (int)(allFlags & 0x3F); allFlags >>>=  6;
            m_flags        =       allFlags;
            m_bbBlacks = ((1L << m_blackKing) | m_bbPawns | m_bbKnights | m_bbBishops | m_bbRooks) & (~m_bbWhites);
            
            m_moveStackIndex++;
            
            if (DEBUG) System.out.println("I redid the last move");
            
            /*---------- notify listeners ----------*/
            if (m_notifyListeners) {
                if (m_listeners != null) {
//                    // enable this to be sure that changes are sent
//                    long squaresChanged = ~0L;
                    long squaresChanged = (bbWhites ^ m_bbWhites) | (bbBlacks ^ m_bbBlacks);
                    while (squaresChanged != 0L) {
                        int sqi = getFirstSqi(squaresChanged);
                        fireSquareChanged(sqi);
                        squaresChanged &= squaresChanged - 1;
                    }
                    if (getSqiEP() != sqiEP) fireSqiEPChanged();
                    if (getCastles() != castles) fireCastlesChanged();
                    fireHalfMoveClockChanged();
                    fireToPlayChanged();
                }
            }
            
            m_notifyPositionChanged = notify;
            return true;
            
        } else {
            m_notifyPositionChanged = notify;
            return false;
        }
    }
    
    //======================================================================
    
    public boolean isLegal()
    {
        if (!super.isLegal()) return false;  // =====>
        
        /*---------- king of toPlay must not be attacked ----------*/
        int kingSquare = (getToPlay() == Chess.WHITE ? m_blackKing : m_whiteKing);
        if (isAttacked(kingSquare, getToPlay(), 0L)) return false;  // =====>
        
        return true;
    }
    
    public void validate() throws IllegalPositionException
    {
        super.validate();
        
        if (m_whiteKing < 0 || m_whiteKing >= Chess.NUM_OF_SQUARES)
            throw new IllegalPositionException("White king square illegal: " + m_whiteKing);
        if (m_blackKing < 0 || m_blackKing >= Chess.NUM_OF_SQUARES)
            throw new IllegalPositionException("White king square illegal: " + m_blackKing);
        
        int kingSquare = (getToPlay() == Chess.WHITE ? m_blackKing : m_whiteKing);
        if (isAttacked(kingSquare, getToPlay(), 0L))
            throw new IllegalPositionException("King of notToPlay is checked: " + Chess.sqiToStr(kingSquare));
        
        if (super.getHashCode() != getHashCode()) {
            System.out.println("Wrong hash code " + getHashCode() + " should be " + super.getHashCode());
//            ChBitBoard.printBoard(getHashCode()); System.out.println(); ChBitBoard.printBoard(super.getHashCode()); 
            long diff = getHashCode() - super.getHashCode();
            System.out.println("Difference " + diff);
            for (int i=0; i<Chess.NUM_OF_SQUARES; i++) {
                for (int j=0; j<s_hashMod[i].length; j++) {
                    if (s_hashMod[i][j] == diff) {
                        System.out.println("Diff is sqi=" + i + " stone=" + (j - Chess.MIN_STONE));
                    }
                }
            }
            for (int i=0; i<16; i++) {
                if (s_hashCastleMod[i] == diff) {
                    System.out.println("Diff is castle " + i);
                }
            }
            for (int i=0; i<8; i++) {
                if (s_hashEPMod[i] == diff) {
                    System.out.println("Diff is sqiEP " + i);
                }
            }
            System.out.println(FEN.getFEN(this));
            System.out.println(FEN.getFEN(new LightWeightPosition(this)));
            throw new IllegalPositionException("Wrong hash code " + getHashCode() + " should be " + super.getHashCode() + " difference " + (getHashCode() - super.getHashCode()));
        }
    }
    
    //======================================================================
    
    public final boolean isCheck()
    {
        if (PROFILE) m_numIsCheck++;
        
        int cacheInfo = (int)(m_flags >> CHECK_SHIFT) & CHECK_MASK;
        if (cacheInfo == FLAG_YES) {
            return true;
        } else if (cacheInfo == FLAG_NO) {
            return false;
        } else {
            boolean isCheck;
            if (getToPlay() == Chess.WHITE) {
                isCheck = isAttacked(m_whiteKing, Chess.BLACK, 0L);
            } else {
                isCheck = isAttacked(m_blackKing, Chess.WHITE, 0L);
            }
            m_flags &= ~(CHECK_MASK << CHECK_SHIFT);
            m_flags |= (isCheck ? FLAG_YES : FLAG_NO) << CHECK_SHIFT;
            return isCheck;
        }
    }
    
    public boolean isTerminal()
    {
        return !canMove() || getHalfMoveClock() >= 100;
    }
    
    public boolean isMate()
    {
        if (PROFILE) m_numIsMate++;
        
        return isCheck() && !canMove();
    }
    
    public boolean isStaleMate()
    {
        if (PROFILE) m_numIsStaleMate++;
        
        return !isCheck() && !canMove();
    }
    
    //======================================================================
    
    public short getLastShortMove()
    {
        return (m_moveStackIndex <= 0 ? Move.NO_MOVE : m_moveStack[m_moveStackIndex - 1]);
    }
    
    public Move getLastMove()
    {
        if (m_moveStackIndex == 0) return null;  // =====>
        short move = m_moveStack[m_moveStackIndex - 1];
        boolean wasWhiteMove = (getToPlay() == Chess.BLACK);
        if (Move.isCastle(move)) {
            return Move.createCastle(move, isCheck(), isMate(), wasWhiteMove);  // ======>
        } else {
            int from = Move.getFromSqi(move);
            int to = Move.getToSqi(move);
            int piece = (Move.isPromotion(move) ? Chess.PAWN : getPiece(to));
            boolean isCapturing = Move.isCapturing(move);
            
            int colFrom = Chess.NO_COL;
            int rowFrom = Chess.NO_ROW;
            if (piece == Chess.PAWN) {
                if (isCapturing) colFrom = Chess.sqiToCol(from);
                return new Move (move, Chess.PAWN, colFrom, rowFrom, isCheck(), isMate(), wasWhiteMove);
            } else {
                try {
                    boolean notify = m_notifyListeners;
                    m_notifyListeners = false;
                    undoMove();
                    Move m = getPieceMoveAndDo(move);
                    m_notifyListeners = notify;
                    return m;
                } catch (IllegalMoveException ex) {
                    return null;
                }
            }
        }
    }
    
    private final int getFromSqi(int piece, int colFrom, int rowFrom, int to)
    {
        long bb = getBitBoard(Chess.pieceToStone(piece, getToPlay()));
        if (colFrom != Chess.NO_COL) {bb &= ofCol(colFrom);}
        if (rowFrom != Chess.NO_ROW) {bb &= ofRow(rowFrom);}
        
        while (bb != 0L) {
            int from = getFirstSqi(bb);
            if (DEBUG) System.out.print("  trying from: " + from);
            int pinnedDir = getPinnedDirection(from, getToPlay());
            if (attacks(from, to) && (pinnedDir == NO_DIR || areDirectionsParallel(pinnedDir, DIR[from][to]))) {
                if (DEBUG) System.out.println(" ok");
                return from;
            }
            bb &= bb -1;
        }
        return Chess.NO_SQUARE;
    }
    
    public short getPawnMove(int colFrom, int to, int promoPiece)
    {
//        if (getColor(from) != getToPlay()) throw new ChIllegalMoveException("Wrong color");
        if (to == getSqiEP()) {
            int from = Chess.coorToSqi(colFrom, getToPlay() == Chess.WHITE ? 4 : 3);
            return Move.getEPMove(from, to);
        } else if (colFrom == Chess.NO_COL) {
            int delta = ((getToPlay() == Chess.WHITE) ? Chess.NUM_OF_COLS : -Chess.NUM_OF_COLS);
            int from = !isSquareEmpty(to - delta) ? to-delta : to-2*delta;
            return Move.getPawnMove(from, to, false, promoPiece);
        } else {
            int from = Chess.coorToSqi(colFrom, Chess.sqiToRow(to) + (getToPlay() == Chess.WHITE ?  -1 : 1));
            return Move.getPawnMove(from, to, true, promoPiece);
        }
    }
    
    public short getPieceMove(int piece, int colFrom, int rowFrom, int to)
    {
        return Move.getRegularMove(getFromSqi(piece, colFrom, rowFrom, to), to, !isSquareEmpty(to));
    }
    
    private Move getPieceMoveAndDo(short move) throws IllegalMoveException
    {
        if (!Move.isValid(move)) throw new IllegalMoveException (move);
        
        int from = Move.getFromSqi(move);
        int to = Move.getToSqi(move);
        boolean isCapturing = Move.isCapturing(move);
        int stone = getStone(from);

        int colFrom = Chess.NO_COL;
        int rowFrom = Chess.NO_ROW;

        long bb = getBitBoard(stone) & getDirectAttackers(to, getToPlay(), false) & ~ofSquare(from);
        if (!isCapturing) bb &= (~m_bbPawns);
        if (bb != 0L) {
            for (long bb2 = bb; bb2 != 0L; bb2 &= bb2 -1) {
                int tryFrom = getFirstSqi(bb2);
                short tryMove = Move.getRegularMove(tryFrom, to, isCapturing);
                doMoveNoMoveListeners(tryMove);
                if (!isLegal()) {
                    bb = bb & (~ofSquare(tryFrom));
                }
                undoMoveNoMoveListeners();
            }
        }
        if (bb != 0L) {
            if ((bb & ofCol(Chess.sqiToCol(from))) == 0L) {
                colFrom = Chess.sqiToCol(from);
            } else if ((bb & ofRow(Chess.sqiToRow(from))) == 0L) {
                rowFrom = Chess.sqiToRow(from);
            } else {
                colFrom = Chess.sqiToCol(from);
                rowFrom = Chess.sqiToRow(from);
            }
        }

        doMoveNoMoveListeners(move);
        Move m = new Move (move, Chess.stoneToPiece(stone), colFrom, rowFrom, isCheck(), isMate(), getToPlay() == Chess.BLACK);        
        if (m_notifyListeners && m_changeListeners != null) fireMoveDone(move);        
        return m;
    }
    
    //======================================================================
    
    /**
     * Returns the direction in which a piece on <code>sqi</code> is pinned in front of the king
     * of <code>color</code>. Returns <code>NO_DIR</code> if piece is not pinned.
     *
     *@param sqi the square for which the pinned direction should be computed. It is not rquired
     * that there is a piece on the square
     *@param color of king with respect to which the pinned direction is computed
     **/
    private int getPinnedDirection(int sqi, int color)
    {
        if (PROFILE) m_numGetPinnedDirection++;
        
        int kingSqi = (color == Chess.WHITE ? m_whiteKing : m_blackKing);
        long bbSqi = ofSquare(sqi);
        
        if ((QUEEN_ATTACKS[kingSqi] & bbSqi) == 0L) return NO_DIR;  // =====>
        
        int kingDir = DIR[kingSqi][sqi];
        long kingDirRim = RIM_BOARD[kingDir];
        if ((kingDirRim & bbSqi) != 0L) return NO_DIR;  // =====>  nothing behind piece
        
        long bbTarget;
        if (isDiagonal(kingDir)) {
            bbTarget = BISHOP_ATTACKS[kingSqi] & m_bbBishops & (color == Chess.WHITE ? m_bbBlacks : m_bbWhites);
        } else {
            bbTarget = ROOK_ATTACKS[kingSqi] & m_bbRooks & (color == Chess.WHITE ? m_bbBlacks : m_bbWhites);
        }
        if (bbTarget == 0L) return NO_DIR;  // =====>
        
        long bbAllPieces = m_bbWhites | m_bbBlacks;
        if ((SQUARES_BETWEEN[kingSqi][sqi] & bbAllPieces) != 0L) return NO_DIR;  // =====>
        
        //        System.out.println("now cheching behind sqi");
        long bb = bbSqi;
        int vector = DIR_SHIFT[kingDir];
        do{
            // bb not on rim checked above -> can increment without test
            if (vector < 0) bb >>>= -vector; else bb <<= vector;
            //            ChBitBoard.printBoard(bb);
            if ((bbTarget & bb) != 0L) return kingDir;  // =====>
            if ((bbAllPieces & bb) != 0L) return NO_DIR;  // =====>
        } while ((kingDirRim & bb) == 0L);
        return NO_DIR;
    }
    
    private static final int sign(int i)
    {
        if (i<0) {return -1;} else if (i>0) {return 1;} else {return 0;}
    }
    
    private boolean attacks(int from, int to)
    {
        // TODO: replace by is attacked
        int piece = getPiece(from);
        long bbTo = ofSquare(to);
        switch(piece) {
            case Chess.NO_PIECE: return false;
            case Chess.PAWN:
                if (getToPlay() == Chess.WHITE)
                    return (WHITE_PAWN_ATTACKS[from] & bbTo) != 0;
                else
                    return (BLACK_PAWN_ATTACKS[from] & bbTo) != 0;
            case Chess.KNIGHT: return (KNIGHT_ATTACKS[from] & bbTo) != 0;
            case Chess.KING: return (KING_ATTACKS[from] & bbTo) != 0;
            case Chess.BISHOP:
            case Chess.ROOK:
            case Chess.QUEEN:
                if (piece == Chess.BISHOP && (BISHOP_ATTACKS[from] & bbTo) == 0) return false;  // =====>
                if (piece == Chess.ROOK && (ROOK_ATTACKS[from] & bbTo) == 0) return false;  // =====>
                if (piece == Chess.QUEEN && (QUEEN_ATTACKS[from] & bbTo) == 0) return false;  // =====>
                long bbFrom = ofSquare(from);
                int vector = DIR_SHIFT[DIR[from][to]];
                if (vector < 0) bbFrom >>>= -vector; else bbFrom <<= vector;
                while (bbFrom != bbTo) {
                    if (((m_bbWhites | m_bbBlacks) & bbFrom) != 0) return false;  // =====>
                    if (vector < 0) bbFrom >>>= -vector; else bbFrom <<= vector;
                }
                return true;  // =====>
                default: throw new RuntimeException("Illegal piece: " + piece);
        }
    }
    
    //======================================================================
    
    private final boolean isAttacked(int sqi, int attacker, long bbExclude)
    {
        if (PROFILE) m_numIsAttacked++;
        
        // only to print sqi, otherwise not needed
        if (sqi < 0 || sqi >63) throw new IllegalArgumentException("Illegal sqi: " + sqi);
        
        long bbAttackerPieces = (attacker == Chess.WHITE ? m_bbWhites : m_bbBlacks) & (~bbExclude);
        long bbAllPieces = (m_bbWhites | m_bbBlacks) & (~bbExclude);
        
        /*---------- knights ----------*/
        if ((KNIGHT_ATTACKS[sqi] & bbAttackerPieces & m_bbKnights) != 0) return true;  // =====>
        
        /*---------- sliding pieces ----------*/
        long bbTargets = ((BISHOP_ATTACKS[sqi] & m_bbBishops) | (ROOK_ATTACKS[sqi] & m_bbRooks)) & bbAttackerPieces;
        while (bbTargets != 0L) {
            int from = getFirstSqi(bbTargets);
            //            if (SQUARES_BETWEEN[from][sqi] == 0L) System.out.println("SQB is 0");
            if ((SQUARES_BETWEEN[from][sqi] & bbAllPieces) == 0L) return true;  // =====>
            bbTargets &= bbTargets -1;
        }
        
        /*---------- king & pawns ----------*/
        if (attacker == Chess.WHITE) {
            // inverse -> black_pawn_attacks
            if ((BLACK_PAWN_ATTACKS[sqi] & bbAttackerPieces & m_bbPawns) != 0) return true;  // =====>
            if ((KING_ATTACKS[sqi] & ofSquare(m_whiteKing) & (~bbExclude)) != 0) return true;  // =====>
        } else {
            if ((WHITE_PAWN_ATTACKS[sqi] & bbAttackerPieces & m_bbPawns) != 0) return true;  // =====>
            if ((KING_ATTACKS[sqi] & ofSquare(m_blackKing) & (~bbExclude)) != 0) return true;  // =====>
        }
        
        return false;
    }
    
    private final long getDirectAttackers(int sqi, int color, boolean includeInbetweenSquares)
    {
        if (PROFILE) m_numDirectAttackers++;
        
        long attackers = 0L;
        long bbAttackerPieces = (color == Chess.WHITE ? m_bbWhites : m_bbBlacks);
        long bbAllPieces = m_bbWhites | m_bbBlacks;
        
        /*---------- knights ----------*/
        attackers |= KNIGHT_ATTACKS[sqi] & bbAttackerPieces & m_bbKnights;
        
        /*---------- sliding pieces ----------*/
        long bbTargets = ((BISHOP_ATTACKS[sqi] & m_bbBishops) | (ROOK_ATTACKS[sqi] & m_bbRooks)) & bbAttackerPieces;
        while (bbTargets != 0L) {
            int from = getFirstSqi(bbTargets);
            long squaresInBetween = SQUARES_BETWEEN[from][sqi];
            if ((squaresInBetween & bbAllPieces) == 0L) {
                attackers |= ofSquare(from);
                if (includeInbetweenSquares) attackers |= squaresInBetween;
            }
            bbTargets &= bbTargets -1;
        }
        
        /*---------- pawns & king ----------*/
        if (color == Chess.WHITE) {
            // inverse -> black_pawn_attacks
            attackers |= BLACK_PAWN_ATTACKS[sqi] & bbAttackerPieces & m_bbPawns;
            attackers |= KING_ATTACKS[sqi] & ofSquare(m_whiteKing);
            if (sqi == getSqiEP()) {
                attackers |= BLACK_PAWN_ATTACKS[sqi-Chess.NUM_OF_COLS] & bbAttackerPieces & m_bbPawns;
            }
        } else {
            attackers |= WHITE_PAWN_ATTACKS[sqi] & bbAttackerPieces & m_bbPawns;
            attackers |= KING_ATTACKS[sqi] & ofSquare(m_blackKing);
            if (sqi == getSqiEP()) {
                attackers |= WHITE_PAWN_ATTACKS[sqi+Chess.NUM_OF_COLS] & bbAttackerPieces & m_bbPawns;
            }
        }
        
        return attackers;
    }
    
    private final long getAllAttackers(int sqi, int color)
    {
        if (PROFILE) m_numGetAllAttackers++;
        
        long attackers = 0L;
        long bbAttackerPieces = (color == Chess.WHITE ? m_bbWhites : m_bbBlacks);
        long bbAllPieces = m_bbWhites | m_bbBlacks;
        
        /*---------- knights ----------*/
        attackers |= KNIGHT_ATTACKS[sqi] & bbAttackerPieces & m_bbKnights;
        
        /*---------- sliding pieces ----------*/
        long bbTargets = BISHOP_ATTACKS[sqi] & m_bbBishops & bbAttackerPieces;
        long bb = bbTargets;
        while (bb != 0L) {
            int from = getFirstSqi(bb);
            if ((SQUARES_BETWEEN[from][sqi] & bbAllPieces & (~bbTargets)) == 0L) {
                attackers |= ofSquare(from);
            }
            bb &= bb -1;
        }
        
        bbTargets = ROOK_ATTACKS[sqi] & m_bbRooks & bbAttackerPieces;
        bb = bbTargets;
        while (bb != 0L) {
            int from = getFirstSqi(bb);
            if ((SQUARES_BETWEEN[from][sqi] & bbAllPieces & (~bbTargets)) == 0L) {
                attackers |= ofSquare(from);
            }
            bb &= bb -1;
        }
        
        /*---------- pawns & king ----------*/
        if (color == Chess.WHITE) {
            // inverse -> black_pawn_attacks
            attackers |= BLACK_PAWN_ATTACKS[sqi] & bbAttackerPieces & m_bbPawns;
            attackers |= KING_ATTACKS[sqi] & ofSquare(m_whiteKing);
            if (sqi == getSqiEP()) {
                attackers |= BLACK_PAWN_ATTACKS[sqi-Chess.NUM_OF_COLS] & bbAttackerPieces & m_bbPawns;
            }
        } else {
            attackers |= WHITE_PAWN_ATTACKS[sqi] & bbAttackerPieces & m_bbPawns;
            attackers |= KING_ATTACKS[sqi] & ofSquare(m_blackKing);
            if (sqi == getSqiEP()) {
                attackers |= WHITE_PAWN_ATTACKS[sqi+Chess.NUM_OF_COLS] & bbAttackerPieces & m_bbPawns;
            }
        }
        
        return attackers;
    }
    
    private final int getAllKnightMoves(int moveIndex, long bbTargets)
    {
        if (bbTargets == 0L) return moveIndex;
        
        long bbToPlay = (getToPlay() == Chess.WHITE ? m_bbWhites : m_bbBlacks);
        
        /*---------- knights moves ----------*/
        long bbPieces = m_bbKnights & bbToPlay;
        while (bbPieces != 0L) {
            int from = getFirstSqi(bbPieces);
            if (getPinnedDirection(from, getToPlay()) == NO_DIR) {
                long destSquares = KNIGHT_ATTACKS[from] & (~bbToPlay) & bbTargets;
                while (destSquares != 0L) {
                    if (moveIndex == -1) return 1;  // =====>
                    int to = getFirstSqi(destSquares);
                    m_moves[moveIndex++] = Move.getRegularMove(from, to, !isSquareEmpty(to));
                    destSquares &= destSquares - 1;
                }
            }
            bbPieces &= bbPieces - 1;
        }
        return moveIndex;
    }
    
    private final int getAllSlidingMoves(int moveIndex, long bbTargets, long bbPieces, int piece)
    {
        if (bbTargets == 0L) return moveIndex;
        
        long bbToPlay = (getToPlay() == Chess.WHITE ? m_bbWhites : m_bbBlacks);
        long bbNotToPlay = (getToPlay() == Chess.WHITE ? m_bbBlacks : m_bbWhites);
        
        int dirStep = (piece == Chess.QUEEN ? 1 : 2);
        int startDir = (piece == Chess.ROOK ? S : SW);
        
        while (bbPieces != 0L) {
            int from = getFirstSqi(bbPieces);
            int pinnedDir = getPinnedDirection(from, getToPlay());
            for (int dir = startDir; dir < NUM_OF_DIRS; dir += dirStep) {
                if ((RAY[from][dir] & bbTargets) != 0L) {
                    if (pinnedDir == NO_DIR || areDirectionsParallel(dir, pinnedDir)) {
                        int dirShift = DIR_SHIFT[dir];
                        long bb = ofSquare(from);
                        int to = from;
                        long rimBoard = RIM_BOARD[dir];
                        while ((bb & rimBoard) == 0L) {
                            if (dirShift < 0) bb >>>= -dirShift; else bb <<= dirShift;
                            to += dirShift;
                            //ChBitBoard.printBoard(bb);
                            if ((bb & bbToPlay) != 0L) break;
                            //System.out.println("move:"+ Chess.sqiToStr(from) + "-" + Chess.sqiToStr(to));
                            if ((bb & bbTargets) != 0L) {
                                if (moveIndex == -1) return 1;  // =====>
                                if ((bb & bbNotToPlay) == 0L) {
                                    m_moves[moveIndex++] = Move.getRegularMove(from, to, false);
                                } else {
                                    m_moves[moveIndex++] = Move.getRegularMove(from, to, true);
                                    break;
                                }
                            } else if ((bb & bbNotToPlay) != 0) break;
                        }
                    }
                }
            }
            bbPieces &= bbPieces - 1;
        }
        return moveIndex;
    }
    
    private final int getAllKingMoves(int moveIndex, long bbTargets, boolean withCastles)
    {
        if (bbTargets == 0L) return moveIndex;
        
        long bbToPlay = (getToPlay() == Chess.WHITE ? m_bbWhites : m_bbBlacks);
        long bbAllPieces = m_bbWhites | m_bbBlacks;
        
        /*---------- regular king moves ----------*/
        int from = (getToPlay() == Chess.WHITE ? m_whiteKing : m_blackKing);
        long bbFrom = ofSquare(from);
        long destSquares = KING_ATTACKS[from] & (~bbToPlay) & bbTargets;
        while (destSquares != 0L) {
            int to = getFirstSqi(destSquares);
            if (!isAttacked(to, getNotToPlay(), bbFrom)) {
                //System.out.println("move:"+ Chess.sqiToStr(from) + "-" + Chess.sqiToStr(to));
                if (moveIndex == -1) return 1;  // =====>
                m_moves[moveIndex++] = Move.getRegularMove(from, to, !isSquareEmpty(to));
            }
            destSquares &= destSquares - 1;
        }
        
        /*---------- castles ----------*/
        if (withCastles) {
            int castles = getCastles();
            if (getToPlay() == Chess.WHITE) {
                // don't need to exclude anything for isAttack since other check would fail in those cases
                if ((castles & WHITE_SHORT_CASTLE) != 0 && (ofSquare(Chess.G1) & bbTargets) != 0L && (bbAllPieces & WHITE_SHORT_CASTLE_EMPTY_MASK) == 0L && !isAttacked(Chess.F1, Chess.BLACK, 0L) && !isAttacked(Chess.G1, Chess.BLACK, 0L)) {
                    if (moveIndex == -1) return 1;  // =====>
                    m_moves[moveIndex++] = Move.WHITE_SHORT_CASTLE;
                }
                if ((castles & WHITE_LONG_CASTLE) != 0 && (ofSquare(Chess.C1) & bbTargets) != 0L && (bbAllPieces & WHITE_LONG_CASTLE_EMPTY_MASK) == 0L && !isAttacked(Chess.D1, Chess.BLACK, 0L) && !isAttacked(Chess.C1, Chess.BLACK, 0L)) {
                    if (moveIndex == -1) return 1;  // =====>
                    m_moves[moveIndex++] = Move.WHITE_LONG_CASTLE;
                }
            } else {
                if ((castles & BLACK_SHORT_CASTLE) != 0 && (ofSquare(Chess.G8) & bbTargets) != 0L && (bbAllPieces & BLACK_SHORT_CASTLE_EMPTY_MASK) == 0L && !isAttacked(Chess.F8, Chess.WHITE, 0L) && !isAttacked(Chess.G8, Chess.WHITE, 0L)) {
                    if (moveIndex == -1) return 1;  // =====>
                    m_moves[moveIndex++] = Move.BLACK_SHORT_CASTLE;
                }
                if ((castles & BLACK_LONG_CASTLE) != 0 && (ofSquare(Chess.C8) & bbTargets) != 0L && (bbAllPieces & BLACK_LONG_CASTLE_EMPTY_MASK) == 0L && !isAttacked(Chess.D8, Chess.WHITE, 0L) && !isAttacked(Chess.C8, Chess.WHITE, 0L)) {
                    if (moveIndex == -1) return 1;  // =====>
                    m_moves[moveIndex++] = Move.BLACK_LONG_CASTLE;
                }
            }
        }
        return moveIndex;
    }
    
    private final int getAllPawnMoves(int moveIndex, long bbTargets)
    {
        if (bbTargets == 0L) return moveIndex;
        
        long bbToPlay, bbNotToPlay, bbAllPieces;
        int thePawn, pawnMoveDir, secondRank, eighthRank;
        
        if (getToPlay() == Chess.WHITE) {
            thePawn = Chess.WHITE_PAWN;
            bbToPlay = m_bbWhites; bbNotToPlay = m_bbBlacks;
            pawnMoveDir = N;
            secondRank = 1; eighthRank = 7;
        } else {
            thePawn = Chess.BLACK_PAWN;
            bbToPlay = m_bbBlacks; bbNotToPlay = m_bbWhites;
            pawnMoveDir = S;
            secondRank = 6; eighthRank = 0;
        }
        
        // if pawn belonging to ep square is a target, include ep square as target
        int sqiEP = getSqiEP();
        if (getSqiEP() != Chess.NO_SQUARE) {
            int epPawnSqi = sqiEP + (getToPlay() == Chess.WHITE ? -Chess.NUM_OF_COLS : Chess.NUM_OF_COLS);
            if ((bbTargets & ofSquare(epPawnSqi)) != 0) {
                bbTargets |= ofSquare(sqiEP);    // pawn cannot move on ep square without capturing (blocked by ep pawn), so adding it is safe
                bbNotToPlay |= ofSquare(sqiEP);  // to prevent the ep square from being filtered
            }
        }
        
        long bbPieces = m_bbPawns & bbToPlay;
        while (bbPieces != 0L) {
            int from = getFirstSqi(bbPieces);
            
            /*---------- pawn move ----------*/
            int to = from + DIR_SHIFT[pawnMoveDir];
            int pinnedDir = getPinnedDirection(from, getToPlay());
            if (isSquareEmpty(to)) {
                if (pinnedDir == NO_DIR || areDirectionsParallel(pinnedDir, pawnMoveDir)) {
                    long bbTo = ofSquare(to);
                    if (Chess.sqiToRow(to) == eighthRank) {
                        if ((bbTo & bbTargets) != 0L) {
                            if (moveIndex == -1) return 1;  // =====>
                            m_moves[moveIndex++] = Move.getPawnMove(from, to, false, Chess.QUEEN);
                            m_moves[moveIndex++] = Move.getPawnMove(from, to, false, Chess.ROOK);
                            m_moves[moveIndex++] = Move.getPawnMove(from, to, false, Chess.BISHOP);
                            m_moves[moveIndex++] = Move.getPawnMove(from, to, false, Chess.KNIGHT);
                        }
                    } else {
                        if ((bbTo & bbTargets) != 0L) {
                            if (moveIndex == -1) return 1;  // =====>
                            m_moves[moveIndex++] = Move.getPawnMove(from, to, false, Chess.NO_PIECE);
                        }
                        if (Chess.sqiToRow(from) == secondRank) {
                            to += DIR_SHIFT[pawnMoveDir];
                            // no need to check is pinned again, since double steps are always possible
                            // if single steps are
                            if (isSquareEmpty(to) && (ofSquare(to) & bbTargets) != 0L) {
                                if (moveIndex == -1) return 1;  // =====>
                                m_moves[moveIndex++] = Move.getPawnMove(from, to, false, Chess.NO_PIECE);
                            }
                        }
                    }
                }
            }
            
            /*---------- pawn capture ----------*/
            long destSquares = (getToPlay() == Chess.WHITE ? WHITE_PAWN_ATTACKS[from] : BLACK_PAWN_ATTACKS[from]) & bbTargets;
            destSquares &= bbNotToPlay;

//            if (sqiEP != Chess.NO_SQUARE) {
//                if (
//                long bbEP = ofSquare(sqiEP + (getToPlay() == Chess.WHITE ? Chess.NUM_OF_COL : -Chess.NUM_OF_COL));
//                if ((destSquares & bbEP) != 0) {
//                    destSquares &= bbNotToPlay;
//                    destSquares |= bbEP;        // leave ep square if corresponding pawn was in destSquares
//                } else {
//                    destSquares &= bbNotToPlay;
//                }
//            } else {
//                destSquares &= bbNotToPlay;
//            }
            
//            if (getSqiEP() != Chess.NO_SQUARE) {
//                destSquares &= bbNotToPlay | ofSquare(getSqiEP());
//            } else {
//                destSquares &= bbNotToPlay;
//            }
            while (destSquares != 0L) {
                to = getFirstSqi(destSquares);
                int dir = DIR[from][to];
                if (pinnedDir == NO_DIR || dir == NO_DIR || areDirectionsParallel(pinnedDir, dir)) {
                    if (moveIndex == -1) return 1;  // =====>
                    int piece = getPiece(to);
                    if (Chess.sqiToRow(to) == eighthRank) {
                        m_moves[moveIndex++] = Move.getPawnMove(from, to, true, Chess.QUEEN);
                        m_moves[moveIndex++] = Move.getPawnMove(from, to, true, Chess.ROOK);
                        m_moves[moveIndex++] = Move.getPawnMove(from, to, true, Chess.BISHOP);
                        m_moves[moveIndex++] = Move.getPawnMove(from, to, true, Chess.KNIGHT);
                    } else if (to == sqiEP) {
                        m_moves[moveIndex++] = Move.getEPMove(from, to);
                    } else {
                        m_moves[moveIndex++] = Move.getPawnMove(from, to, true, Chess.NO_PIECE);
                    }
                }
                destSquares &= destSquares - 1;
            }
            bbPieces &= bbPieces - 1;
        }
        return moveIndex;
    }
    
    public short[] getAllMoves()
    {
        return getAllMoves(~0L, ~0L);
    }
    
    public short[] getAllReCapturingMoves(short lastMove)
    {
        if (Move.isValid(lastMove)) {
            long bbTargets = ofSquare(Move.getToSqi(lastMove));
            long bbPawnTargets = (getSqiEP() == Chess.NO_SQUARE ? bbTargets : bbTargets | ofSquare(getSqiEP()));
            return getAllMoves(bbTargets, bbPawnTargets);
        } else {
            return new short[0];
        }
    }
    
    public short[] getAllCapturingMoves()
    {
        long bbTargets = getToPlay() == Chess.WHITE ? m_bbBlacks : m_bbWhites;
        // can include sqiEP safely since no pawn can move on sqi if it is set
        long bbPawnTargets = (getSqiEP() == Chess.NO_SQUARE ? bbTargets : bbTargets | ofSquare(getSqiEP()));
        return getAllMoves(bbTargets, bbPawnTargets);
    }
    
    public short[] getAllNonCapturingMoves()
    {
        long bbTargets = getToPlay() == Chess.WHITE ? ~m_bbBlacks : ~m_bbWhites;
        // can exclude sqiEP safely since no pawn can move on sqi if it is set
        long bbPawnTargets = (getSqiEP() == Chess.NO_SQUARE ? bbTargets : bbTargets & (~ofSquare(getSqiEP())));
        return getAllMoves(bbTargets, bbPawnTargets);
    }
    
    private final short[] getAllMoves(long bbTargets, long bbPawnTargets)
    {
        if (PROFILE) m_numGetAllMoves++;
        
        if (bbTargets == 0L) return new short[0];  // =====>
        
        int moveIndex = 0;  // TODO: make class?
        
        long bbToPlay = (getToPlay() == Chess.WHITE ? m_bbWhites : m_bbBlacks);
        if (isCheck()) {
            moveIndex = getAllKingMoves(moveIndex, bbTargets, false);
            long attackers = getDirectAttackers((getToPlay() == Chess.WHITE ? m_whiteKing : m_blackKing), getNotToPlay(), false);
            //ChBitBoard.printBoard(attackers);
            if (isExactlyOneBitSet(attackers)) {
                //                System.out.println("investigate piece moves");
                attackers = getDirectAttackers((getToPlay() == Chess.WHITE ? m_whiteKing : m_blackKing), getNotToPlay(), true);
                bbTargets &= attackers; bbPawnTargets &= attackers;
                moveIndex = getAllKnightMoves(moveIndex, bbTargets);
                moveIndex = getAllSlidingMoves(moveIndex, bbTargets, m_bbBishops & (~m_bbRooks) & bbToPlay, Chess.BISHOP);
                moveIndex = getAllSlidingMoves(moveIndex, bbTargets, m_bbRooks & (~m_bbBishops) & bbToPlay, Chess.ROOK);
                moveIndex = getAllSlidingMoves(moveIndex, bbTargets, m_bbRooks & m_bbBishops & bbToPlay, Chess.QUEEN);
                //                moveIndex = getAllSlidingMoves(moveIndex, bbTargets, m_bbBishops & bbToPlay, SW);
                //                moveIndex = getAllSlidingMoves(moveIndex, bbTargets, m_bbRooks & bbToPlay, S);
                moveIndex = getAllPawnMoves(moveIndex, bbPawnTargets);
            } else { // double check
                //printBoard(attackers);
            }
        } else {
            moveIndex = getAllKnightMoves(moveIndex, bbTargets);
            moveIndex = getAllSlidingMoves(moveIndex, bbTargets, m_bbBishops & (~m_bbRooks) & bbToPlay, Chess.BISHOP);
            moveIndex = getAllSlidingMoves(moveIndex, bbTargets, m_bbRooks & (~m_bbBishops) & bbToPlay, Chess.ROOK);
            moveIndex = getAllSlidingMoves(moveIndex, bbTargets, m_bbRooks & m_bbBishops & bbToPlay, Chess.QUEEN);
            moveIndex = getAllKingMoves(moveIndex, bbTargets, true);
            moveIndex = getAllPawnMoves(moveIndex, bbPawnTargets);
        }
        
        short[] onlyTheMoves = new short[moveIndex];
        System.arraycopy(m_moves, 0, onlyTheMoves, 0, moveIndex);
        
        return onlyTheMoves;
    }
    
    public boolean canMove()
    {
        int cacheInfo = (int)(m_flags >> CAN_MOVE_SHIFT) & CAN_MOVE_MASK;
        if (cacheInfo == FLAG_YES) {
            return true;
        } else if (cacheInfo == FLAG_NO) {
            return false;
        } else {
            boolean canMove = false;
            long bbToPlay = (getToPlay() == Chess.WHITE ? m_bbWhites : m_bbBlacks);
            if (isCheck()) {
                //            ChBitBoard.printBoard(bbTargets);
                if (getAllKingMoves(-1, ~0L, false) > 0) {
                    canMove = true;
                } else {
                    long attackers = getDirectAttackers((getToPlay() == Chess.WHITE ? m_whiteKing : m_blackKing), getNotToPlay(), false);
                    if (isExactlyOneBitSet(attackers)) {
                        attackers = getDirectAttackers((getToPlay() == Chess.WHITE ? m_whiteKing : m_blackKing), getNotToPlay(), true);
                        canMove = (getAllKnightMoves(-1, attackers) > 0) ||
                                  (getAllPawnMoves(-1, attackers) > 0) ||
                                  (getAllSlidingMoves(-1, attackers, m_bbBishops & (~m_bbRooks) & bbToPlay, Chess.BISHOP) > 0) ||
                                  (getAllSlidingMoves(-1, attackers, m_bbRooks & (~m_bbBishops) & bbToPlay, Chess.ROOK) > 0) ||
                                  (getAllSlidingMoves(-1, attackers, m_bbRooks & m_bbBishops & bbToPlay, Chess.QUEEN) > 0);
                    }
                }
            } else {
                long bbTargets = ~0L;
                canMove = (getAllKnightMoves(-1, bbTargets) > 0) ||
                          (getAllPawnMoves(-1, bbTargets) > 0) ||
                          (getAllSlidingMoves(-1, bbTargets, m_bbBishops & (~m_bbRooks) & bbToPlay, Chess.BISHOP) > 0) ||
                          (getAllSlidingMoves(-1, bbTargets, m_bbRooks & (~m_bbBishops) & bbToPlay, Chess.ROOK) > 0) ||
                          (getAllSlidingMoves(-1, bbTargets, m_bbRooks & m_bbBishops & bbToPlay, Chess.QUEEN) > 0) ||
                          (getAllKingMoves(-1, bbTargets, false) > 0);  // don' test castling since it cannot be the only move
            }
            m_flags &= ~(CAN_MOVE_MASK << CAN_MOVE_SHIFT);
            m_flags |= (canMove ? FLAG_YES : FLAG_NO) << CAN_MOVE_SHIFT;
            return canMove;
        }
    }
    
    public String getMovesAsString(short[] moves, boolean validateEachMove)
    {
        StringBuffer sb = new StringBuffer();
        Move.normalizeOrder(moves);
        
        sb.append('{');
        for (int i=0; i < moves.length; i++) {
            if (i>0) sb.append(',');
            try {
                doMove(moves[i]);
//                sb.append(getLastMove(moves[i]));
                sb.append(getLastMove());
                if (validateEachMove) {
                    try {
                        validate();
                    } catch (Throwable t) {
                        sb.append("EXCEPTION: after move " + Move.getString(moves[i]) + ": " + t.getMessage());
                    }
                }
                undoMove();
            } catch (IllegalMoveException ex) {
                sb.append("Illegal Move " + Move.getString(moves[i]) + ": " + ex.getMessage());
            }
        }
        sb.append('}');
        
        return sb.toString();
    }
    
    //======================================================================
    
    public int getMaterial()
    {
        int value = 0;
        value += 100 * (numOfBitsSet(m_bbPawns & m_bbWhites) - numOfBitsSet(m_bbPawns & m_bbBlacks));
        value += 300 * (numOfBitsSet(m_bbKnights & m_bbWhites) - numOfBitsSet(m_bbKnights & m_bbBlacks));
        value += 325 * (numOfBitsSet(m_bbBishops & (~m_bbRooks) & m_bbWhites) - numOfBitsSet(m_bbBishops & (~m_bbRooks) & m_bbBlacks));
        value += 500 * (numOfBitsSet(m_bbRooks & (~m_bbBishops) & m_bbWhites) - numOfBitsSet(m_bbRooks & (~m_bbBishops) & m_bbBlacks));
        value += 900 * (numOfBitsSet(m_bbRooks & m_bbBishops & m_bbWhites) - numOfBitsSet(m_bbRooks & m_bbBishops & m_bbBlacks));
        //        System.out.println(value);
        return (getToPlay() == Chess.WHITE ? value : -value);
    }
    
    public double getDomination()
    {
        int[] SQUARE_IMPORTANCE =
            {1, 1, 1, 1, 1, 1, 1, 1,
             1, 2, 2, 2, 2, 2, 2, 1,
             1, 2, 4, 6, 6, 4, 2, 1,
             1, 2, 5,10,10, 5, 1, 1,
             1, 2, 5,10,10, 5, 1, 1,
             1, 2, 4, 6, 6, 4, 2, 1,
             1, 2, 2, 2, 2, 2, 2, 1,
             1, 1, 1, 1, 1, 1, 1, 1};
        
        double value = 0;
        for (int sqi = 0; sqi < Chess.NUM_OF_SQUARES; sqi++) {
            long bbWhiteAttackers = getAllAttackers(sqi, Chess.WHITE);
            long bbBlackAttackers = getAllAttackers(sqi, Chess.BLACK);
            int score = sign(numOfBitsSet(bbWhiteAttackers) - numOfBitsSet(bbBlackAttackers));
            value += SQUARE_IMPORTANCE[sqi] * score;
        }
        return (getToPlay() == Chess.WHITE ? value : -value);
    }
    
}