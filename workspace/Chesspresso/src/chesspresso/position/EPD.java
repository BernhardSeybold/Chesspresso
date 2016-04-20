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
 * $Id: EPD.java,v 1.1 2002/12/08 13:27:35 BerniMan Exp $
 */

package chesspresso.position;

import java.util.*;

/**
 * Support for the EPD (Extended Position Description) standard, see PGN standard
 * chapter 16.2.
 *
 * @author  Bernhard Seybold
 * @version $Revision: 1.1 $
 */
public class EPD
{
    
    public static final String
        OC_ANALYSIS_COUNT_NODES = "acn",     // int
        OC_ANALYSIS_COUNT_SECONDS = "acs",   // int
        OC_AVOID_MOVES = "av",               // SAN list
        OC_BEST_MOVE = "bm",                 // SAN list
        OC_COMMENT_0 = "c0",                 // opt. string
        OC_COMMENT_1 = "c1",                 // opt. string
        OC_COMMENT_2 = "c2",                 // opt. string
        OC_COMMENT_3 = "c3",                 // opt. string
        OC_COMMENT_4 = "c4",                 // opt. string
        OC_COMMENT_5 = "c5",                 // opt. string
        OC_COMMENT_6 = "c6",                 // opt. string
        OC_COMMENT_7 = "c7",                 // opt. string
        OC_COMMENT_8 = "c8",                 // opt. string
        OC_COMMENT_9 = "c9",                 // opt. string
        OC_EVALUATION = "ce",                // signed int
        OC_DIRECT_MATE = "dm",               // int
        OC_DRAW_ACCEPT = "draw_accept",      // none
        OC_DRAW_CLAIM = "draw_claim",        // none
        OC_DRAW_OFFER = "draw_offer",        // none
        OC_DRAW_REJECT = "draw_reject",      // none
        OC_ECO = "eco",                      // string
        OC_FULLMOVE_NUMBER = "fmvn",         // int
        OC_HALFMOVE_CLOCK = "hmvc",          // int
        OC_IDENTIFICATION = "id",            // string
        OC_NIC_CODE = "nic",                 // string
        OC_NO_OPERATION = "noop",            // list of any type
        OC_PREDICTED_MOVE = "pm",            // move
        OC_PREDICTED_VARIATION = "pv",       // SAN list
        OC_REPETITION_COUNT = "rc",          // int
        OC_RESIGN = "resign",                // none
        OC_SUPPLIED_MOVE = "sm",             // SAN
        OC_TC_GAME_SELECTOR = "tcgs",        // int
        OC_TC_RECEIVER_ID = "tcri",          // email, name
        OC_TC_SENDER_ID = "tcsi",            // email, name
        OC_VARIATION_NAME_0 = "v0",          // opt. string
        OC_VARIATION_NAME_1 = "v1",          // opt. string
        OC_VARIATION_NAME_2 = "v2",          // opt. string
        OC_VARIATION_NAME_3 = "v3",          // opt. string
        OC_VARIATION_NAME_4 = "v4",          // opt. string
        OC_VARIATION_NAME_5 = "v5",          // opt. string
        OC_VARIATION_NAME_6 = "v6",          // opt. string
        OC_VARIATION_NAME_7 = "v7",          // opt. string
        OC_VARIATION_NAME_8 = "v8",          // opt. string
        OC_VARIATION_NAME_9 = "v9";          // opt. string
        

    public static Map initFromEPD(MutablePosition pos, String fen, boolean strict) throws IllegalArgumentException
    {
        FEN.initFromFEN(pos, fen, strict);
        Map opcodes = new HashMap();
        // TODO: parse opcodes here
        return opcodes;
    }

    public static String getEPD(ImmutablePosition pos, Map opcodes)
    {
        // TODO
        return null;
    }
    
}