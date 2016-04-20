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
 * $Id: IllegalMoveException.java,v 1.1 2002/12/08 13:27:34 BerniMan Exp $
 */

package chesspresso.move;


/**
 * Exception indicating an illegal move.
 *
 * @author  Bernhard Seybold
 * @version $Revision: 1.1 $
 */
public class IllegalMoveException extends Exception
{
    public IllegalMoveException(short move)
    {
        super ("Illegal move: " + Move.getString(move));
    }    

    public IllegalMoveException(short move, String msg)
    {
        super ("Illegal move: " + Move.getString(move) + ": " + msg);
    }    

    public IllegalMoveException(Move move)
    {
        super ("Illegal move: " + move);
    }    

    public IllegalMoveException(Move move, String msg)
    {
        super ("Illegal move: " + move + ": " + msg);
    }    

    public IllegalMoveException(String msg)
    {
        super ("Illegal move: " + msg);
    }    
}