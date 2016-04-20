///*
// * Copyright (C) Bernhard Seybold. All rights reserved.
// *
// * This software is published under the terms of the LGPL Software License,
// * a copy of which has been included with this distribution in the LICENSE.txt
// * file.
// *
// * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
// * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
// * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
// *
// * $Id: AllTests.java,v 1.1 2002/12/08 13:27:05 BerniMan Exp $
// */
//
//package chesspresso;
//
//import junit.framework.*;
//
///**
// *
// * @author Bernhard Seybold
// * @version $Revision: 1.1 $
// */
//public class AllTests extends TestCase
//{
//    
//    public static Test suite() {
//        TestSuite suite = new TestSuite();
//        
//        suite.addTest(chesspresso.move.MoveTests.suite());
//        
//        suite.addTest(chesspresso.position.FENTests.suite());
//        
//        suite.addTest(chesspresso.position.TestLightWeightPosition.suite());
//        suite.addTest(chesspresso.position.TestCompactPosition.suite());
//        suite.addTest(chesspresso.position.TestPosition.suite());
//        
//        suite.addTest(chesspresso.pgn.PGNReaderTest.suite());
//        
//        return suite;
//    }
//    
//    public static void main(java.lang.String[] args)
//    {
//        if (args != null && args.length > 0 && "-ui".equals(args[0])) {
//            junit.swingui.TestRunner.run(AllTests.class);
//        } else if (args.length == 0) {
//            junit.textui.TestRunner.run(suite());
//        } else {
//            System.out.println("Usage: Alltests [-ui]");
//        }
//    }
//    
//}