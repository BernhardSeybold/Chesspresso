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
 * $Id: FootprintTestCase.java,v 1.3 2003/01/10 08:31:17 BerniMan Exp $
 */

package ch.seybold.util;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.Writer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


/**
 * Extension of a test case that supports footprints. A footprint is a stream
 * of text which the test produces. In order to succeed the foorprint must be
 * identical to the original version found over the classpath. If no original
 * is found, the test produces the output in the current directory and will fail
 * at the end. The programmer can then (manually) verify the footprint and
 * check it in if correct.
 *
 * <p>Example:<br>
 * <code>
 * <quote>
 *   public void test() {
 *     startFootprint("Test.footprint", true);
 *     writeln("a line for the footprint");
 *     stopFootprint();
 *   }
 * <quote>
 * </code>
 *
 * @author  Bernhard Seybold
 * @version $Revision: 1.3 $
 */
public abstract class FootprintTestCase
{
    
    private LineNumberReader m_in;
    private PrintStream m_out;

    //======================================================================
    
    private class FootprintWriter extends Writer
    {
        @Override
		public void close() throws IOException
        {
            m_out.close();
        }
        
        @Override
		public void flush() throws IOException
        {
            m_out.flush();
        }
        
        @Override
		public void write(char[] cbuf, int off, int len) throws IOException
        {
            FootprintTestCase.this.write(new String(cbuf, off, len));
        }
        
    }
    
    //======================================================================
    
    /**
     * Initiates a footprint with the given name. If the file is over the classpath
     * (with the current package as prefix), subsequent output is checked against
     * the content of the file. If no file is found, output will be written to
     * the current directory.
     *
     *@param name the name fo the footprint
     *@zipped whether or not the footprint is compressed with gzip
     */
    public void startFootprint(String name, boolean zipped) throws Exception
    {
        if (m_in != null)  fail("Footprint already started in read mode.");
        if (m_out != null) fail("Footprint already started in write mode.");
        
        String packageName = getClass().getPackage().getName().replace('.', '/');
        InputStream in = ClassLoader.getSystemResourceAsStream(packageName + '/' + name);
        if (in != null) {
            // input found
            if (zipped) {
                in = new GZIPInputStream(in);
            }
            m_in = new LineNumberReader(new InputStreamReader(in));
        } else {
            // no input found -> produce output
            assertTrue("Output file exists", !new File(name).exists());
            if (zipped) {
                m_out = new PrintStream(new GZIPOutputStream(new FileOutputStream(name)));
            } else {
                m_out = new PrintStream(new FileOutputStream(name));
            }
        }
    }
    
    /**
     * Stop processing of the current footprint. This method must be called at the
     * end of the footprint production. From then on, writing to the footprint will
     * result in a runtime exception.
     */
    public void stopFootprint() throws Exception
    {
        if (m_in != null) {
            try {
                for (;;) {
                    String line = m_in.readLine();
                    if (line == null) {
                        break;
                    } else if (line.length() > 0) {
                        fail("Too many lines in footprint: '" + line + "'");
                    }
                }
            } catch (EOFException ex) {
                // ok
            } finally {
                m_in.close();
                m_in = null;
            }
        } else if (m_out != null) {
            m_out.close();
            m_out = null;
            fail("No input footprint found, output produced");
        } else {
            fail("Footprint was not started.");
        }
    }
    
    //======================================================================
    
    /**
     * Access to the current footprint to be used in external methods.
     *
     *@return the current footprint writer
     */
    protected Writer getFootprint()
    {
        return new FootprintWriter();
    }
    
    private void write(String s, boolean newline)
    {
        try {
            if (m_in == null) {
                if (newline) {
                    m_out.println(s);
                } else {
                    m_out.print(s);
                }
            } else {
                if (newline) {
                    assertEquals("In line " + m_in.getLineNumber(), m_in.readLine(), s);
                } else {
                    char[] buf = new char[s.length()];
                    m_in.read(buf, 0, s.length());
                    assertEquals("In line " + m_in.getLineNumber(), new String(buf), s);
                }
            }
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
    }
    
    /**
     * Writes the string to the footprint.
     *@param s the text to be written
     */
    protected void write(String s)
    {
        write(s, false);
    }
    
    /**
     * Terminates the current line.
     */
    protected void writeln()
    {
        write("", true);
    }
    
    /**
     * Writes the string to the footprint and terminates the current line.
     *@param s the text to be written
     */
    protected void writeln(String s)
    {
        write(s, true);
    }
    
}