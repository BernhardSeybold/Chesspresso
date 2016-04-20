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
 * $Id: StringKit.java,v 1.1 2003/04/05 14:03:36 BerniMan Exp $
 */

package ch.seybold.util;

/**
 * String kit.
 *
 * @author  Bernhard Seybold
 * @version $Revision: 1.1 $
 */
public class StringKit
{
    
    public static String getRights(String s, int num)
    {
        return s.substring(s.length() - num);
    }
    
    public static String remove(String s, char ch)
    {
        if (s.indexOf(ch) == -1) return s;
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<s.length(); i++) {
            char curChar = s.charAt(i);
            if (curChar != ch) sb.append(curChar);
        }
        return sb.toString();
    }
    
}