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
 * $Id: PositionTests.java,v 1.1 2002/12/08 13:27:28 BerniMan Exp $
 */

package chesspresso.position;

import junit.framework.*;

/**
 *
 * @author Bernhard Seybold
 * @version $Revision: 1.1 $
 */
public abstract class PositionTests extends TestCase
{
    
    /**
     * Factory method to create position implementations.
     */
    protected abstract ImmutablePosition createPosition();
    
    //======================================================================
    
    public void testSomething()
    {
    }
    
}