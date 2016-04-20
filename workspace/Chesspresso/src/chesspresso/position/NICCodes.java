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
 * $Id: NAG.java,v 1.1 2003/01/04 16:11:46 BerniMan Exp $
 */

package chesspresso.position;

import java.util.*;


/**
 * Support for New In Chess Codes.
 *
 * @author  Bernhard Seybold
 * @version $Revision: 1.1 $
 */
public abstract class NICCodes
{
    
    private static Map s_descriptions = new HashMap();
    private static Map s_strings = new HashMap();

    private static String[] s_defaultStrings = {
        null, null, null, null, null, null, "=", null, null,
        ">", "<", ">=", "<=",
        "!", "!!", "?", "??", "!?", "?!", null,
        null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        "#", "N", "EN", "Z", "T"
    };

//    private static String[] s_nagsOfNicCode = {
//        14, 15, 16, 17, 18, 19, 10, 13, *42,
//         0,  0,  0,  0,
//         1,  3,  2,  4,  5,  6,
//         7,  0,*40,*36,*32,*46,
//         0,  0,*26,*50,  0,  0,  0,  0,  0,  0,  0,*22,*137
//    }


    private static String[] s_nicAbbreviations = {
        "see",
        "editorial comment",
        "Yearbook",
        "national championship",
        "zonal tournament",
        "interzonal tournament",
        "candidates tournament",
        "team tournament",
        "olympiad",
        "match",
        "correspondence",
        "junior"
    };

    static {
        s_descriptions.put(Locale.ENGLISH,
            new String[] {
                "White stands slightly better",
                "Black stands slightly better",
                "White stands better",
                "Black stands better",
                "White has a decisive advantage",
                "Black has a decisive advantage",
                "balanced position",
                "unclear position",
                "compensation for the material",

                "strong (sufficient)",
                "weak (insufficient)",
                "better is",
                "weaker is",

                "good move",
                "excellent move",
                "bad move",
                "blunder",
                "interesting move",
                "dubious move",
                "only move",
                
                "with the idea",
                "attack",
                "initiative",
                "lead in development",
                "counterplay",

                "kingside",
                "queenside",
                "space",
                "center",
                "diagonal",
                "file",
                "pair of bishops",
                "pawn structure",
                "mate",
                "novelty",
                "endgame",
                "zugzwang",
                "time"
            }
        );
    }
}