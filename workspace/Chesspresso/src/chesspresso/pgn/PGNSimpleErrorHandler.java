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
 * $Id: PGNSimpleErrorHandler.java,v 1.1 2002/12/08 13:27:34 BerniMan Exp $
 */

package chesspresso.pgn;

import java.io.*;

/**
 * Simple implementation of a PGN error handler. Write the errors and warnings
 * directly to a configured print stream.
 *
 * @author  Bernhard Seybold
 * @version $Revision: 1.1 $
 */
public class PGNSimpleErrorHandler implements PGNErrorHandler
{

    private PrintStream m_out;
    
    /*================================================================================*/
    
    public PGNSimpleErrorHandler(PrintStream out)
    {
        m_out = out;
    }

    /*================================================================================*/
    
    public void handleError(PGNSyntaxError error)
    {
        m_out.println(error);
    }
    
    public void handleWarning(PGNSyntaxError warning)
    {
        m_out.println(warning);
    }    
}