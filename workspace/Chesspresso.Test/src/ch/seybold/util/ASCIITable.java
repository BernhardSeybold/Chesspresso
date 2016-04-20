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
 * $Id: NAG.java,v 1.1 2003/01/04 16:11:46 BerniMan Exp $
 */

package ch.seybold.util;

import java.io.*;
import java.util.*;


/**
 * ASCII Table
 *
 * @author Bernhard Seybold
 * @version $Revision$
 */
public class ASCIITable
{
    
    public abstract static class AlignmentDescriptor
    {
        protected int m_width;
        
        protected int getWidth() {return m_width;}
        
        protected void fill(PrintStream out, int len, char fillChar)
        {
            for (int i=0; i<len; i++) out.print(fillChar);
        }
    
        protected void init(String[] line)
        {
            m_width = 0;
            for (int i=0; i<line.length; i++) {
                if (line[i] != null && line[i].length() > m_width) m_width = line[i].length();
            }
        }
        
        protected abstract void print(PrintStream out, String elem);
    }
    
    public static class LeftAlignment extends AlignmentDescriptor
    {
        protected void print(PrintStream out, String elem)
        {
            out.print(elem);
            fill(out, m_width - elem.length(), ' ');
        }
    }
    
    public static class RightAlignment extends AlignmentDescriptor
    {
        protected void print(PrintStream out, String elem)
        {
            fill(out, m_width - elem.length(), ' ');
            out.print(elem);
        }
    }
    
    public static class CenterAlignment extends AlignmentDescriptor
    {
        protected void print(PrintStream out, String elem)
        {
            int left = (elem.length() - m_width) / 2;
            fill(out, left, ' ');
            out.print(elem);
            fill(out, m_width - left - elem.length(), ' ');
        }
    }
    
    public static class NumberAlignment extends AlignmentDescriptor
    {
        protected int m_dotIndex;
        
        protected void init(String[] line)
        {
            m_width = 0;
            m_dotIndex = 0;
            for (int i=0; i<line.length; i++) {
                if (line[i] != null) {
                    int dotIndex = line[i].indexOf('.');
                    if (dotIndex > m_dotIndex) m_dotIndex = dotIndex;
                }
            }
            for (int i=0; i<line.length; i++) {
                if (line[i] != null) {
                    int dotIndex = line[i].indexOf('.');
                    if (dotIndex == -1) {
                        if (line[i].length() > m_width) m_width = line[i].length();
                    } else {
                        int width = line[i].length() - dotIndex + m_dotIndex;
                        if (width > m_width) m_width = width;
                    }
                }
            }
        }
        
        protected void print(PrintStream out, String elem)
        {
            int dotIndex = elem.indexOf('.');
            if (dotIndex == -1) {
                try {
                    Integer.parseInt(elem);
                    fill(out, m_width - elem.length(), ' ');
                    out.print(elem);
                } catch (NumberFormatException ex) {
                    out.print(elem);
                    fill(out, m_width - elem.length(), ' ');
                }
            } else {
                fill(out, m_dotIndex - dotIndex, ' ');
                out.print(elem);
                fill(out, m_width - (m_dotIndex - dotIndex) - elem.length(), ' ');
            }
        }
    }
    
    //======================================================================
     
    private AlignmentDescriptor[] m_alignment;
    private List m_lines;
    
    //======================================================================
     
    public ASCIITable(AlignmentDescriptor[] alignment)
    {
        m_alignment = alignment;
        m_lines = new ArrayList();
    }
    
    public void addLine(String[] line)
    {
        m_lines.add(line);
    }
     
    public void addSeparator(char separator)
    {
        m_lines.add(new Character(separator));
    }
     
    private String[] getColumn(int col)
    {
        String[] elems = new String[m_lines.size()];
        int i=0;
        for (Iterator it = m_lines.iterator(); it.hasNext(); ) {
            Object next = it.next();
            if (next instanceof String[]) {
                elems[i++] = ((String[])next)[col];
            } else {
                elems[i++] = null;
            }
        }
        return elems;
    }
    
    public void print(PrintStream out)
    {
        for (int i=0; i<m_alignment.length; i++) {
            m_alignment[i].init(getColumn(i));
        }
        
        for (Iterator it = m_lines.iterator(); it.hasNext(); ) {
            Object next = it.next();
            if (next instanceof Character) {
                char separator = ((Character)next).charValue();
                for (int col=0; col<m_alignment.length; col++) {
                    for (int i=m_alignment[col].getWidth(); i>0; i--) out.print(separator);
                    if (col >0) out.print(separator);
                }
                out.println();
            } else {
                String[] line = (String[])next;
                for (int col=0; col<line.length; col++) {
                    m_alignment[col].print(out, line[col]);
                    out.print(" ");
                }
                out.println();
            }
        }
    }
    
}