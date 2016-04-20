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

import junit.framework.*;
import java.text.*;


/**
 * Support for performance tests.
 *
 * @author  Bernhard Seybold
 * @version $Revision: 1.1 $
 */
public abstract class PerformanceTest extends TestCase
{

    private static final int
        STATE_INIT    = 0,
        STATE_RESET   = 1;

    private int m_state = init();

    private String[] m_timerNames = null;
    private Timer[] m_timers;
    private double[] m_min;
    private double[] m_max;
    private double[] m_lastDuration;
    private long[] m_numProcessedPerItem;
    private double m_lastTime;
    private int m_lastCategory;

    //======================================================================
    // Timer implementation, inspired by Vlad Roubtsov's Article in JavaWorld
    // http://www.javaworld.com/javaworld/javaqa/2003-01/01-qa-0110-timing.html?

//    private static TimerFactory s_timerFactory = new SystemTimerFactory();
    private static Timer s_timer = new HRTimer();

    public static void setTimer(Timer timer)
    {
        s_timer = timer;
    }

    public interface Timer
    {
        Timer newTimer();  // factory method
        void start();
        void stop();
        double getDuration();
        void reset();
    }

    private static class SystemTimer implements Timer
    {
        public Timer newTimer() {return new SystemTimer();}

        private long m_time;

        public final void start() {m_time = System.currentTimeMillis();}
        public final void stop() {m_time = System.currentTimeMillis() - m_time;}
        public final double getDuration() {return m_time;}
        public final void reset() {m_time = 0;}
    }

    private static class HRTimer implements Timer
    {
        public Timer newTimer() {return new HRTimer();}

        com.vladium.utils.timing.ITimer m_timer = com.vladium.utils.timing.TimerFactory.newTimer();

        public final void start() {m_timer.start();}
        public final void stop()  {m_timer.stop();}
        public final double getDuration() {return m_timer.getDuration();}
        public final void reset() {m_timer.reset();}
    }

    //======================================================================

    private int init()
    {
        // TODO hotspot warm-up?
        // see http://www.javaworld.com/javaworld/javaqa/2003-01/01-qa-0110-timing.html?

        m_timerNames = null;
        m_timers = null;
        m_numProcessedPerItem = null;
        m_lastTime = 0;
        m_lastCategory = -1;
        return STATE_INIT;
    }

    protected final void resetTimer(String[] timerNames) throws Exception
    {
        if (m_state != STATE_INIT)
            throw new Exception("Cannot call reset timer in state " + m_state);

        m_timerNames = timerNames;

        int numOfCategories = m_timerNames.length;
        m_timers = new Timer[numOfCategories];
        m_min = new double[numOfCategories];
        m_max = new double[numOfCategories];
        m_lastDuration = new double[numOfCategories];
        m_numProcessedPerItem = new long[numOfCategories];
        for (int i=0; i < numOfCategories; i++) {
            m_timers[i] = s_timer.newTimer();
            m_numProcessedPerItem[i] = 0;
            m_min[i] = Double.MAX_VALUE;
            m_max[i] = Double.MIN_VALUE;
        }

        m_state = STATE_RESET;
    }

    protected void printReport() throws Exception
    {
        if (m_state != STATE_RESET)
            throw new Exception("Cannot stop test that is not started");

        performanceReport();
        m_state = init();
    }

    //======================================================================

    protected final void startTimer()
    {
        // TODO throw exception if m_lastcategory == -1 ? performance affected?
        m_lastCategory = 0;
        m_lastDuration[0] = m_timers[0].getDuration();
        m_timers[0].start();
    }

    protected final void startTimer(int category)
    {
        // TODO throw exception if m_lastcategory == -1 ? performance affected?
        // TODO throw exception if category is wrong ? performance affected?
        m_lastCategory = category;
        m_lastDuration[category] = m_timers[category].getDuration();
        m_timers[category].start();
    }

    protected final void stopTimer()
    {
        m_timers[m_lastCategory].stop();
        m_numProcessedPerItem[m_lastCategory] += 1;
        
        double last = m_timers[m_lastCategory].getDuration() - m_lastDuration[m_lastCategory];
        if (last < m_min[m_lastCategory]) m_min[m_lastCategory] = last;
        if (last > m_max[m_lastCategory]) m_max[m_lastCategory] = last;
    }

    protected final void stopTimer(long processed)
    {
        m_timers[m_lastCategory].stop();
        if (processed == 0) return;  // =====>
        m_numProcessedPerItem[m_lastCategory] += processed;
        
        double last = (m_timers[m_lastCategory].getDuration() - m_lastDuration[m_lastCategory]) / processed;
        if (last < m_min[m_lastCategory]) m_min[m_lastCategory] = last;
        if (last > m_max[m_lastCategory]) m_max[m_lastCategory] = last;
    }

    //======================================================================

    private int getNumOfCategories()
    {
        return m_timerNames == null ? 0 : m_timerNames.length;
    }

    private void performanceReport()
    {
        if (m_timerNames == null) return;  // =====>

        DecimalFormat df = new DecimalFormat("#0.00000");
        
        ASCIITable table = new ASCIITable(
            new ASCIITable.AlignmentDescriptor[] {
                new ASCIITable.LeftAlignment(),
                new ASCIITable.NumberAlignment(),
                new ASCIITable.NumberAlignment(),
                new ASCIITable.NumberAlignment(),
                new ASCIITable.NumberAlignment(),
                new ASCIITable.NumberAlignment(),
                new ASCIITable.NumberAlignment()
            }
        );
        
        table.addSeparator('=');
        table.addLine(new String[] {"timer", "total", "ms", "item/ms", "ms/item", "min", "max"});
        table.addSeparator('-');
        for (int i=0; i < getNumOfCategories(); i++) {
            table.addLine(new String[] {
                m_timerNames[i],
                String.valueOf(m_numProcessedPerItem[i]),
                df.format(m_timers[i].getDuration()),
                df.format(m_numProcessedPerItem[i] / m_timers[i].getDuration()),
                df.format(m_timers[i].getDuration() / m_numProcessedPerItem[i]),
                df.format(m_min[i]),
                df.format(m_max[i])
            });
        }
        table.addSeparator('=');
        
        table.print(System.out);        
    }

}