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
 * $Id: MutablePosition.java,v 1.1 2002/12/08 13:27:36 BerniMan Exp $
 */

package chesspresso.position;

/**
 *
 * @author $Author: BerniMan $
 * @version $Revision: 1.1 $
 */
public interface MutablePosition extends ImmutablePosition
{
    public void clear();
    public void set(ImmutablePosition position);
    public void setStart();
    
    public void setStone(int sqi, int stone);
    public void setCastles(int castles);
    public void setSqiEP(int sqiEP);
    public void setToPlay(int toPlay);
    public void setPlyNumber(int plyNumber);
    public void setHalfMoveClock(int halfMoveClock);     
}