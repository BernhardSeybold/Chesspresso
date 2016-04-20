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
 * $Id: PositionChangeListener.java,v 1.1 2002/12/08 13:27:36 BerniMan Exp $
 */

package chesspresso.position;


public interface PositionChangeListener
{
    // always called when position changes
    public void notifyPositionChanged(ImmutablePosition position);
    
    public void notifyMoveDone(ImmutablePosition position, short move);
    public void notifyMoveUndone(ImmutablePosition position);
//    // called if position changes not caused by a move
//    public void notifyOtherChange(IChPosition position);
}