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
 * $Id: IllegalPositionException.java,v 1.1 2002/12/08 13:27:35 BerniMan Exp $
 */

package chesspresso.position;

/**
 * Exception to indicate an illegal position.
 *
 * @author  Bernhard Seybold
 * @version $Revision: 1.1 $
 */
public class IllegalPositionException extends java.lang.Exception
{
    public IllegalPositionException(String msg)
    {
        super (msg);
    }
}