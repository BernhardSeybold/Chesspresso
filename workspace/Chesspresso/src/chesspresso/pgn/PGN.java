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
 * $Id: PGN.java,v 1.2 2003/04/05 14:02:38 BerniMan Exp $
 */

package chesspresso.pgn;

import chesspresso.Chess;
import ch.seybold.util.StringKit;
import java.util.Date;
import java.util.Calendar;


/**
 * General definitions for the PGN standard.
 *
 * The pgn standard is available at <a href="ftp://chess.onenet.net">ftp://chess.onenet.net</a>.
 *
 * @author  Bernhard Seybold
 * @version $Revision: 1.2 $
 */
public abstract class PGN
{

    //======================================================================
    // Elo
    
    public static int getElo(String elo)
    {
        try {
            if (elo == null) return 0;  // =====>
            return Integer.parseInt(elo);
        } catch (NumberFormatException ex) {
            return 0;  // =====>
        }
    }
    
    //======================================================================
    // Result, possible results are only "1-0", "0-1", "1/2-1/2", and "*" 
    
    public static int getResultForPGNResult(String pgnResult)
    {
        if      ("1-0".equals(pgnResult))     return Chess.RES_WHITE_WINS;
        else if ("0-1".equals(pgnResult))     return Chess.RES_BLACK_WINS;
        else if ("1/2-1/2".equals(pgnResult)) return Chess.RES_DRAW;
        else if ("*".equals(pgnResult))       return Chess.RES_NOT_FINISHED;
        else                                  return Chess.NO_RES;
    }
    
    public static String getResultAsPGNResult(int result)
        throws IllegalArgumentException
    {
        if      (result == Chess.RES_WHITE_WINS)   return "1-0";
        else if (result == Chess.RES_BLACK_WINS)   return "0-1";
        else if (result == Chess.RES_DRAW)         return "1/2-1/2";
        else if (result == Chess.RES_NOT_FINISHED) return "*";
        else if (result == Chess.NO_RES)           return "*"; // does not exist in PGN
        else throw new IllegalArgumentException("Illegal result " + result);
    }
    
    public static String getShortResult(int result)
		throws IllegalArgumentException
	{
		if      (result == Chess.RES_WHITE_WINS)   return "1-0";
		else if (result == Chess.RES_BLACK_WINS)   return "0-1";
		else if (result == Chess.RES_DRAW)         return "1/2";
		else if (result == Chess.RES_NOT_FINISHED) return "*";
		else if (result == Chess.NO_RES)           return "*"; // does not exist in PGN
		else throw new IllegalArgumentException("Illegal result " + result);
    }
    
    //======================================================================
    // pgn date
    // examples: 
    // [Date "1992.08.31"]
    // [Date "1993.??.??"]
    // [Date "2001.01.01"]
    
    public static int getYearOfPGNDate(String pgnDate) throws IllegalArgumentException
    {
        if (pgnDate == null) throw new IllegalArgumentException("date string is null");
        try {
            int index = pgnDate.indexOf('.');
            if (index == -1) throw new IllegalArgumentException("string does not contain a dot");
            return Integer.parseInt(pgnDate.substring(0, index));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }
    
    public static String getDateAsPGNDate(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR) + "."
             + StringKit.getRights("00" + (cal.get(Calendar.MONTH) + 1)  , 2) + "."
             + StringKit.getRights("00" +  cal.get(Calendar.DAY_OF_MONTH), 2);
    }
    
    //======================================================================
    // constants for parsing
    
    final static char
        TOK_QUOTE          = '"',
        TOK_PERIOD         = '.',
        TOK_ASTERISK       = '*',
        TOK_TAG_BEGIN      = '[',
        TOK_TAG_END        = ']',
        TOK_LINE_BEGIN     = '(',
        TOK_LINE_END       = ')',
        TOK_LBRACKET       = '<',
        TOK_RBRACKET       = '>',
        TOK_NAG_BEGIN      = '$',
        TOK_LINE_COMMENT   = ';',
        TOK_COMMENT_BEGIN  = '{',
        TOK_COMMENT_END    = '}',
        TOK_PGN_ESCAPE     = '%';
    
    //======================================================================
    // TAG constants
    
    // Seven tag roaster
    public final static String
        TAG_EVENT      = "Event",
        TAG_SITE       = "Site",
        TAG_DATE       = "Date",
        TAG_ROUND      = "Round",
        TAG_WHITE      = "White",
        TAG_BLACK      = "Black",
        TAG_RESULT     = "Result",
    
    // Standard extensions
        TAG_EVENT_DATE = "EventDate",
        TAG_WHITE_ELO  = "WhiteElo",
        TAG_BLACK_ELO  = "BlackElo",
        TAG_ECO        = "ECO",
        TAG_SETUP      = "SetUp",
        TAG_FEN        = "FEN",
    
    // Supplemental tags (selection of the ones mentioned in PGN standard)
        TAG_WHITE_TITLE   = "WhiteTitle",
        TAG_BLACK_TITLE   = "BlackTitle",
        TAG_WHITE_TYPE    = "WhiteType",
        TAG_BLACK_TYPE    = "BlackType",
        TAG_EVENT_SPONSOR = "EventSponsor",
        TAG_SECTION       = "Section",
        TAG_STAGE         = "Stage",
        TAG_BOARD         = "Board",
        TAG_NIC           = "NIC",
        TAG_TIME_CONTROL  = "TimeControl",
        TAG_ANNOTATOR     = "Annotator",
        TAG_MODE          = "Mode",
        TAG_PLY_COUNT     = "PlyCount";
    
    public final static String[] SEVEN_TAG_ROASTER = {
        TAG_EVENT, TAG_SITE, TAG_DATE, TAG_ROUND, TAG_WHITE, TAG_BLACK, TAG_RESULT
    };
    
    public static boolean isInSevenTagRoaster(String tagName)
    {
        for (int i=0; i<SEVEN_TAG_ROASTER.length; i++) {
            if (SEVEN_TAG_ROASTER[i].equals(tagName)) return true;
        }
        return false;
    }

}