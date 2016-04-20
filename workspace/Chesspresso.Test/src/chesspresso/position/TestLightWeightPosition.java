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
 * $Id: TestLightWeightPosition.java,v 1.1 2002/12/08 13:27:28 BerniMan Exp $
 */

package chesspresso.position;

import junit.framework.*;

/**
 *
 * @author Bernhard Seybold
 * @version $Revision: 1.1 $
 */
public class TestLightWeightPosition extends MutablePositionTests
{
    
    public static Test suite()
    {
        return new TestSuite(TestLightWeightPosition.class);
    }
    
    public static void main (String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    //======================================================================
    
    protected ImmutablePosition       createPosition()        {return new LightWeightPosition();}
    protected MutablePosition createMutablePosition() {return new LightWeightPosition();}
    
}