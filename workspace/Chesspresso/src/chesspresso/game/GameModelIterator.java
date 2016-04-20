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
 * $Id: GameModelIterator.java,v 1.1 2002/12/08 13:27:34 BerniMan Exp $
 */

package chesspresso.game;

/**
 * Iterator over a collection of game models.
 *
 * @author Bernhard Seybold
 * @version $Revision: 1.1 $
 */
public interface GameModelIterator extends java.util.Iterator
{
    public GameModel nextGameModel();
}