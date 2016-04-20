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
 * $Id: MutablePositionTests.java,v 1.2 2003/04/11 17:48:09 BerniMan Exp $
 */

package chesspresso.position;


/**
 *
 * @author Bernhard Seybold
 * @version $Revision: 1.2 $
 */
public abstract class MutablePositionTests extends PositionTests
{
    
    protected abstract MutablePosition createMutablePosition();
    
    //======================================================================
    
    public void testFENStartpos()
    {
        MutablePosition position = createMutablePosition();
        
        FEN.initFromFEN(position, FEN.START_POSITION, true);
        assertEquals("STARTPOS", FEN.START_POSITION, position.getFEN());
    }
    
    public void testStartpos()
    {
        MutablePosition position = createMutablePosition();
        
        position.setStart();        
        assertTrue("Initial position is not legal", position.isLegal());
        assertTrue("Initial position is not the start position", position.isStartPosition());
    }

}