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
 * $Id: PGNWriter.java,v 1.4 2003/04/09 18:37:20 BerniMan Exp $
 */

package chesspresso.pgn;


import chesspresso.*;
import chesspresso.game.*;
import chesspresso.move.Move;
import chesspresso.position.*;

import java.io.*;


/**
 * A PGN writer is able to write a game (collection) in PGN export format.
 *
 * @author  Bernhard Seybold
 * @version $Revision: 1.4 $
 */
public class PGNWriter extends PGN
{

    private PrintWriter m_out;
    private int m_charactersPerLine;
    private int m_curCol;
    private String[] m_additionalHeaderTags;
    
    //======================================================================
    
    public PGNWriter(Writer out)
    {
        this(new PrintWriter(out));
    }

    public PGNWriter(PrintWriter out)
    {
        m_out = out;
        setCharactersPerLine(80);
        setAdditionalHeaderTags(new String[] {
            TAG_WHITE_ELO, TAG_BLACK_ELO, TAG_EVENT_DATE, TAG_ECO
        });
    }

    //======================================================================
    
    /**
     * Set the maximal characters per line. The writer will insert a line break
     * if the next token to be printed would exceed the line width.
     *
     *@param chars the number of characters allowed per line, default is 80
     */
    public void setCharactersPerLine(int chars) {m_charactersPerLine = chars;}

    /**
     * Writes a collection of game models
     *
     *@param iterator the iterator fot the collection
     */
    public void write(GameModelIterator iterator)
    {
        while (iterator.hasNext()) {
            write(iterator.nextGameModel());
            m_out.println();
        }
    }
    
    /**
     * Writes the game model.
     *
     *@param the gameModel to be written, will remain unchaned
     */
    public void write(GameModel gameModel)
    {
        Game game = new Game(gameModel);
        writeHeader(game);
        m_out.println();
        m_curCol = 0;
        writeMoves(game);
        if (m_curCol > 0) m_out.println();
    }
    
    /**
     * Determines which additional headers (except the seven tag roaster and the
     * FEN tag) should be included in the header. The sequence is according the
     * order in the array. The tag will only be included if it is set in the game.
     * Doubles are not checked.
     * Default is: white elo, black elo, event date, eco.
     *
     *@param tags the tags to be included in the header. If null print all
     *       available tags. To print no additional tag, pass an empty array.
     */
    public void setAdditionalHeaderTags(String[] tags)
    {
        m_additionalHeaderTags = tags;
    }
    
    //======================================================================
    
    private void writeTag(String tag, String value)
    {
        m_out.println(TOK_TAG_BEGIN + tag + " " + TOK_QUOTE + value + TOK_QUOTE + TOK_TAG_END);
    }
    
    private void writeHeader(Game game)
    {
        if (m_additionalHeaderTags == null) {
            String[] allTags = game.getTags();
            for (int i=0; i<allTags.length; i++) {
                writeTag(allTags[i], game.getTag(allTags[i]));
            }
        } else {
            writeTag(TAG_EVENT,  game.getEvent());
            writeTag(TAG_SITE,   game.getSite());
            writeTag(TAG_DATE,   game.getDate());
            writeTag(TAG_ROUND,  game.getRound());
            writeTag(TAG_WHITE,  game.getWhite());
            writeTag(TAG_BLACK,  game.getBlack());
            writeTag(TAG_RESULT, game.getResultStr());

            for (int i=0; i<m_additionalHeaderTags.length; i++) {
                String value = game.getTag(m_additionalHeaderTags[i]);
                if (value != null) {
                    writeTag(m_additionalHeaderTags[i], value);
                }
            }
        }
        
        if (!game.getPosition().isStartPosition()) {
            writeTag(TAG_SETUP, "1");
            writeTag(TAG_FEN, FEN.getFEN(game.getPosition()));
        }
    }

    private void writeMoves(Game game)
    {
        // print leading comments before move 1
        String comment = game.getComment();
        if (comment != null) {
            print(TOK_COMMENT_BEGIN + comment + TOK_COMMENT_END, true);
        }
        
        // traverse the game
        game.traverse(new GameListener() {
            private boolean needsMoveNumber = true;
            public void notifyMove(Move move, short[] nags, String comment, int plyNumber, int level)
            {
                if (needsMoveNumber) {
                    if (move.isWhiteMove()) {
                        print(Chess.plyToMoveNumber(plyNumber) + ".", true);
                    } else {
                        print(Chess.plyToMoveNumber(plyNumber) + "...", true);
                    }
                }
                print(move.toString(), true);
                
                if (nags != null) {
                    for (int i=0; i < nags.length; i++) {
                        print(String.valueOf(TOK_NAG_BEGIN) + String.valueOf(nags[i]), true);
                    }
                }
                if (comment != null) print(TOK_COMMENT_BEGIN + comment + TOK_COMMENT_END, true);
                needsMoveNumber = !move.isWhiteMove() || (comment != null);
            }
            public void notifyLineStart(int level)
            {
                print(String.valueOf(TOK_LINE_BEGIN), false);
                needsMoveNumber = true;
            }
            public void notifyLineEnd(int level)
            {
                print(String.valueOf(TOK_LINE_END), true);
                needsMoveNumber = true;
            }
        }, true);
        
        print(game.getResultStr(), false);
    }

    private void print(String s, boolean addSpace)
    {
        if (m_curCol + s.length() > m_charactersPerLine) {
            m_out.println();
            m_curCol = 0;
        }
        m_out.print(s);
        m_curCol += s.length();
        if (m_curCol > 0 && addSpace) {
            m_out.print(" ");
            m_curCol += 1;
        }
    }
}