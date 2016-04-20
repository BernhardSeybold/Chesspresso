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
 * $Id: PositionMotionListener.java,v 1.1 2002/12/08 13:27:36 BerniMan Exp $
 */

package chesspresso.position;


import java.awt.event.MouseEvent;


public interface PositionMotionListener
{
    public boolean allowDrag(ImmutablePosition position, int from);
    public int getPartnerSqi(ImmutablePosition position, int from);
    
    public void dragged(ImmutablePosition position, int from, int to, MouseEvent e);
    public void squareClicked(ImmutablePosition position, int sqi, MouseEvent e);
}