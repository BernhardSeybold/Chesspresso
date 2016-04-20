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
 * $Id: NAG.java,v 1.2 2003/04/09 18:09:52 BerniMan Exp $
 */

package chesspresso.position;


/**
 * Support for NAG (Numeric Annotation Glyphs).
 *
 * @author  Bernhard Seybold
 * @version $Revision: 1.2 $
 */
public abstract class NAG
{
    
    // TODO add short NAG chars for chess fonts

    
    // allow higher nags
    public static short NUM_OF_NAGS = 256;

    private static String[] LONG_DESCRIPTION = {
       "null annotation",  // 0
       "good move",  // 1
       "poor move",  // 2
       "very good move",  // 3
       "very poor move",  // 4
       "speculative move",  // 5
       "questionable move",  // 6
       "forced move (all others lose quickly)",  // 7
       "singular move (no reasonable alternatives)",  // 8
       "worst move",  // 9
       "drawish position",  // 10
       "equal chances, quiet position",  // 11
       "equal chances, active position",  // 12
       "unclear position",  // 13
       "White has a slight advantage",  // 14
       "Black has a slight advantage",  // 15
       "White has a moderate advantage",  // 16
       "Black has a moderate advantage",  // 17
       "White has a decisive advantage",  // 18
       "Black has a decisive advantage",  // 19
       "White has a crushing advantage (Black should resign)",  // 20
       "Black has a crushing advantage (White should resign)",  // 21
       "White is in zugzwang",  // 22
       "Black is in zugzwang",  // 23
       "White has a slight space advantage",  // 24
       "Black has a slight space advantage",  // 25
       "White has a moderate space advantage",  // 26
       "Black has a moderate space advantage",  // 27
       "White has a decisive space advantage",  // 28
       "Black has a decisive space advantage",  // 29
       "White has a slight time (development) advantage",  // 30
       "Black has a slight time (development) advantage",  // 31
       "White has a moderate time (development) advantage",  // 32
       "Black has a moderate time (development) advantage",  // 33
       "White has a decisive time (development) advantage",  // 34
       "Black has a decisive time (development) advantage",  // 35
       "White has the initiative",  // 36
       "Black has the initiative",  // 37
       "White has a lasting initiative",  // 38
       "Black has a lasting initiative",  // 39
       "White has the attack",  // 40
       "Black has the attack",  // 41
       "White has insufficient compensation for material deficit",  // 42
       "Black has insufficient compensation for material deficit",  // 43
       "White has sufficient compensation for material deficit",  // 44
       "Black has sufficient compensation for material deficit",  // 45
       "White has more than adequate compensation for material deficit",  // 46
       "Black has more than adequate compensation for material deficit",  // 47
       "White has a slight center control advantage",  // 48
       "Black has a slight center control advantage",  // 49
       "White has a moderate center control advantage",  // 50
       "Black has a moderate center control advantage",  // 51
       "White has a decisive center control advantage",  // 52
       "Black has a decisive center control advantage",  // 53
       "White has a slight kingside control advantage",  // 54
       "Black has a slight kingside control advantage",  // 55
       "White has a moderate kingside control advantage",  // 56
       "Black has a moderate kingside control advantage",  // 57
       "White has a decisive kingside control advantage",  // 58
       "Black has a decisive kingside control advantage",  // 59
       "White has a slight queenside control advantage",  // 60
       "Black has a slight queenside control advantage",  // 61
       "White has a moderate queenside control advantage",  // 62
       "Black has a moderate queenside control advantage",  // 63
       "White has a decisive queenside control advantage",  // 64
       "Black has a decisive queenside control advantage",  // 65
       "White has a vulnerable first rank",  // 66
       "Black has a vulnerable first rank",  // 67
       "White has a well protected first rank",  // 68
       "Black has a well protected first rank",  // 69
       "White has a poorly protected king",  // 70
       "Black has a poorly protected king",  // 71
       "White has a well protected king",  // 72
       "Black has a well protected king",  // 73
       "White has a poorly placed king",  // 74
       "Black has a poorly placed king",  // 75
       "White has a well placed king",  // 76
       "Black has a well placed king",  // 77
       "White has a very weak pawn structure",  // 78
       "Black has a very weak pawn structure",  // 79
       "White has a moderately weak pawn structure",  // 80
       "Black has a moderately weak pawn structure",  // 81
       "White has a moderately strong pawn structure",  // 82
       "Black has a moderately strong pawn structure",  // 83
       "White has a very strong pawn structure",  // 84
       "Black has a very strong pawn structure",  // 85
       "White has poor knight placement",  // 86
       "Black has poor knight placement",  // 87
       "White has good knight placement",  // 88
       "Black has good knight placement",  // 89
       "White has poor bishop placement",  // 90
       "Black has poor bishop placement",  // 91
       "White has good bishop placement",  // 92
       "Black has good bishop placement",  // 93
       "White has poor rook placement",  // 94
       "Black has poor rook placement",  // 95
       "White has good rook placement",  // 96
       "Black has good rook placement",  // 97
       "White has poor queen placement",  // 98
       "Black has poor queen placement",  // 99
       "White has good queen placement",  // 100
       "Black has good queen placement",  // 101
       "White has poor piece coordination",  // 102
       "Black has poor piece coordination",  // 103
       "White has good piece coordination",  // 104
       "Black has good piece coordination",  // 105
       "White has played the opening very poorly",  // 106
       "Black has played the opening very poorly",  // 107
       "White has played the opening poorly",  // 108
       "Black has played the opening poorly",  // 109
       "White has played the opening well",  // 110
       "Black has played the opening well",  // 111
       "White has played the opening very well",  // 112
       "Black has played the opening very well",  // 113
       "White has played the middlegame very poorly",  // 114
       "Black has played the middlegame very poorly",  // 115
       "White has played the middlegame poorly",  // 116
       "Black has played the middlegame poorly",  // 117
       "White has played the middlegame well",  // 118
       "Black has played the middlegame well",  // 119
       "White has played the middlegame very well",  // 120
       "Black has played the middlegame very well",  // 121
       "White has played the ending very poorly",  // 122
       "Black has played the ending very poorly",  // 123
       "White has played the ending poorly",  // 124
       "Black has played the ending poorly",  // 125
       "White has played the ending well",  // 126
       "Black has played the ending well",  // 127
       "White has played the ending very well",  // 128
       "Black has played the ending very well",  // 129
       "White has slight counterplay",  // 130
       "Black has slight counterplay",  // 131
       "White has moderate counterplay",  // 132
       "Black has moderate counterplay",  // 133
       "White has decisive counterplay",  // 134
       "Black has decisive counterplay",  // 135
       "White has moderate time control pressure",  // 136
       "Black has moderate time control pressure",  // 137  
       "White has severe time control pressure",  // 138
       "Black has severe time control pressure"
    };  // 139
        
    private static String[] SHORT_DESCRIPTION = {
       null,  // 0
       "!",  // 1
       "?",  // 2
       "!!",  // 3
       "??",  // 4
       "!?",  // 5
       "?!",  // 6
       null, // "forced move (all others lose quickly)",  // 7
       null, // "singular move (no reasonable alternatives)",  // 8
       null, // "worst move",  // 9
       null, // "drawish position",  // 10
       null, // "equal chances, quiet position",  // 11
       null, // "equal chances, active position",  // 12
       null, // "unclear position",  // 13
       "+=",  // 14
       "=+",  // 15
       null, // "White has a moderate advantage",  // 16
       null, // "Black has a moderate advantage",  // 17
       "+-",  // 18
       "-+",  // 19
       null, // "White has a crushing advantage (Black should resign)",  // 20
       null, // "Black has a crushing advantage (White should resign)",  // 21
       null, // "White is in zugzwang",  // 22
       null, // "Black is in zugzwang",  // 23
       null, // "White has a slight space advantage",  // 24
       null, // "Black has a slight space advantage",  // 25
       null, // "White has a moderate space advantage",  // 26
       null, // "Black has a moderate space advantage",  // 27
       null, // "White has a decisive space advantage",  // 28
       null, // "Black has a decisive space advantage",  // 29
       null, // "White has a slight time (development) advantage",  // 30
       null, // "Black has a slight time (development) advantage",  // 31
       null, // "White has a moderate time (development) advantage",  // 32
       null, // "Black has a moderate time (development) advantage",  // 33
       null, // "White has a decisive time (development) advantage",  // 34
       null, // "Black has a decisive time (development) advantage",  // 35
       null, // "White has the initiative",  // 36
       null, // "Black has the initiative",  // 37
       null, // "White has a lasting initiative",  // 38
       null, // "Black has a lasting initiative",  // 39
       null, // "White has the attack",  // 40
       null, // "Black has the attack",  // 41
       null, // "White has insufficient compensation for material deficit",  // 42
       null, // "Black has insufficient compensation for material deficit",  // 43
       null, // "White has sufficient compensation for material deficit",  // 44
       null, // "Black has sufficient compensation for material deficit",  // 45
       null, // "White has more than adequate compensation for material deficit",  // 46
       null, // "Black has more than adequate compensation for material deficit",  // 47
       null, // "White has a slight center control advantage",  // 48
       null, // "Black has a slight center control advantage",  // 49
       null, // null, // "White has a moderate center control advantage",  // 50
       null, // "Black has a moderate center control advantage",  // 51
       null, // "White has a decisive center control advantage",  // 52
       null, // "Black has a decisive center control advantage",  // 53
       null, // "White has a slight kingside control advantage",  // 54
       null, // "Black has a slight kingside control advantage",  // 55
       null, // "White has a moderate kingside control advantage",  // 56
       null, // "Black has a moderate kingside control advantage",  // 57
       null, // "White has a decisive kingside control advantage",  // 58
       null, // "Black has a decisive kingside control advantage",  // 59
       null, // "White has a slight queenside control advantage",  // 60
       null, // "Black has a slight queenside control advantage",  // 61
       null, // "White has a moderate queenside control advantage",  // 62
       null, // "Black has a moderate queenside control advantage",  // 63
       null, // "White has a decisive queenside control advantage",  // 64
       null, // "Black has a decisive queenside control advantage",  // 65
       null, // "White has a vulnerable first rank",  // 66
       null, // "Black has a vulnerable first rank",  // 67
       null, // "White has a well protected first rank",  // 68
       null, // "Black has a well protected first rank",  // 69
       null, // "White has a poorly protected king",  // 70
       null, // "Black has a poorly protected king",  // 71
       null, // "White has a well protected king",  // 72
       null, // "Black has a well protected king",  // 73
       null, // "White has a poorly placed king",  // 74
       null, // "Black has a poorly placed king",  // 75
       null, // "White has a well placed king",  // 76
       null, // "Black has a well placed king",  // 77
       null, // "White has a very weak pawn structure",  // 78
       null, // "Black has a very weak pawn structure",  // 79
       null, // "White has a moderately weak pawn structure",  // 80
       null, // "Black has a moderately weak pawn structure",  // 81
       null, // "White has a moderately strong pawn structure",  // 82
       null, // "Black has a moderately strong pawn structure",  // 83
       null, // "White has a very strong pawn structure",  // 84
       null, // "Black has a very strong pawn structure",  // 85
       null, // "White has poor knight placement",  // 86
       null, // "Black has poor knight placement",  // 87
       null, // "White has good knight placement",  // 88
       null, // "Black has good knight placement",  // 89
       null, // "White has poor bishop placement",  // 90
       null, // "Black has poor bishop placement",  // 91
       null, // "White has good bishop placement",  // 92
       null, // "Black has good bishop placement",  // 93
       null, // "White has poor rook placement",  // 94
       null, // "Black has poor rook placement",  // 95
       null, // "White has good rook placement",  // 96
       null, // "Black has good rook placement",  // 97
       null, // "White has poor queen placement",  // 98
       null, // "Black has poor queen placement",  // 99
       null, // "White has good queen placement",  // 100
       null, // "Black has good queen placement",  // 101
       null, // "White has poor piece coordination",  // 102
       null, // "Black has poor piece coordination",  // 103
       null, // "White has good piece coordination",  // 104
       null, // "Black has good piece coordination",  // 105
       null, // "White has played the opening very poorly",  // 106
       null, // "Black has played the opening very poorly",  // 107
       null, // "White has played the opening poorly",  // 108
       null, // "Black has played the opening poorly",  // 109
       null, // "White has played the opening well",  // 110
       null, // "Black has played the opening well",  // 111
       null, // "White has played the opening very well",  // 112
       null, // "Black has played the opening very well",  // 113
       null, // "White has played the middlegame very poorly",  // 114
       null, // "Black has played the middlegame very poorly",  // 115
       null, // "White has played the middlegame poorly",  // 116
       null, // "Black has played the middlegame poorly",  // 117
       null, // "White has played the middlegame well",  // 118
       null, // "Black has played the middlegame well",  // 119
       null, // "White has played the middlegame very well",  // 120
       null, // "Black has played the middlegame very well",  // 121
       null, // "White has played the ending very poorly",  // 122
       null, // "Black has played the ending very poorly",  // 123
       null, // "White has played the ending poorly",  // 124
       null, // "Black has played the ending poorly",  // 125
       null, // "White has played the ending well",  // 126
       null, // "Black has played the ending well",  // 127
       null, // "White has played the ending very well",  // 128
       null, // "Black has played the ending very well",  // 129
       null, // "White has slight counterplay",  // 130
       null, // "Black has slight counterplay",  // 131
       null, // "White has moderate counterplay",  // 132
       null, // "Black has moderate counterplay",  // 133
       null, // "White has decisive counterplay",  // 134
       null, // "Black has decisive counterplay",  // 135
       null, // "White has moderate time control pressure",  // 136
       null, // "Black has moderate time control pressure",  // 137  
       null, // "White has severe time control pressure",  // 138
       null  // "Black has severe time control pressure";  // 139
    };
    
    /*================================================================================*/
    
    public static String[] getDefinedShortNags()
    {
        int num=0;
        for (int i=0; i<SHORT_DESCRIPTION.length; i++) if (SHORT_DESCRIPTION[i] != null) num++;
        String[] res = new String[num];
        num=0;
        for (int i=0; i<SHORT_DESCRIPTION.length; i++) if (SHORT_DESCRIPTION[i] != null) res[num++] = SHORT_DESCRIPTION[i];
        return res;
    }
    
    public static String[] getUnDefinedShortNags()
    {
        int num=0;
        for (int i=0; i<SHORT_DESCRIPTION.length; i++) if (SHORT_DESCRIPTION[i] == null) num++;
        String[] res = new String[num];
        num=0;
        for (int i=0; i<SHORT_DESCRIPTION.length; i++) if (SHORT_DESCRIPTION[i] == null) res[num++] = LONG_DESCRIPTION[i];
        return res;
    }
    
    /*================================================================================*/
    
    public static String getLongString(short nag)
    {
        if (nag >= 0 && nag < LONG_DESCRIPTION.length) {
            return LONG_DESCRIPTION[nag];
        } else {
            return "<unknown nag " + nag + ">";
        }
    }
    
    public static String getShortString(short nag)
    {
        return getShortString(nag, true);
    }

    public static String getShortString(short nag, boolean takeLongIfNull)
    {
        if (nag >= 0 && nag < SHORT_DESCRIPTION.length) {
            if (SHORT_DESCRIPTION[nag] == null) {
                if (takeLongIfNull) {
                    return LONG_DESCRIPTION[nag];
                } else {
                    return "$" + nag;
                }
            } else {
                return SHORT_DESCRIPTION[nag];
            }
        } else {
            return "$" + nag;
        }
    }
    
    public static short ofString(String description) throws IllegalArgumentException
    {
        if (description != null) {
            for (short i=0; i < SHORT_DESCRIPTION.length; i++) {
                if (description.equals(SHORT_DESCRIPTION[i])) return i;
                if (description.equals(LONG_DESCRIPTION[i])) return i;
            }
        }
        throw new IllegalArgumentException("Nag unknown " + description);
    }
}
