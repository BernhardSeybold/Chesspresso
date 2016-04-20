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
 * $Id: TestPosition.java,v 1.2 2003/01/04 16:06:18 BerniMan Exp $
 */

package chesspresso.position;

/**
 * Concrete test for the Position class.
 *
 * @author Bernhard Seybold
 * @version $Revision: 1.2 $
 */
public class TestPosition extends MoveablePositionTests
{
    
    @Override
    protected ImmutablePosition createPosition()         {return new Position();}
    
    @Override
    protected MutablePosition   createMutablePosition()  {return new Position();}
    
    @Override
    protected MoveablePosition  createMoveablePosition() {return new Position();}
    
}