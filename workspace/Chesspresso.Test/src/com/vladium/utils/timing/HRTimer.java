
package com.vladium.utils.timing;

// ----------------------------------------------------------------------------
/**
 * A package-private implementation of {@link ITimer} based around native
 * <code>getTime</code> method. It will work on any platform for which a JNI
 * implementation of "hrtlib" library is available.<P>
 *
 * {@link TimerFactory} acts as the Factory for this class.<P>
 *
 * MT-safety: an instance of this class is safe to be used within the same
 * thread only.
 *
 * @author (C) <a href="mailto:vroubtsov@illinoisalumni.org">Vlad Roubtsov</a>, 2002
 */
final class HRTimer implements ITimer, ITimerConstants
{
    // public: ................................................................

    public void start ()
    {
        if (DO_STATE_CHECKS)
        {
            if (m_state != STATE_READY && m_state != STATE_STOPPED)
                throw new IllegalStateException (this + ": start() must be called from READY or STOPPED state, current state is " + STATE_NAMES [m_state]);
        }

        if (DO_STATE_CHECKS) m_state = STATE_STARTED;
        m_data -= getTime ();
    }

    public void stop ()
    {
        // latch stop time in a local var before doing anything else:
        final double data = getTime ();

        if (DO_STATE_CHECKS)
        {
            if (m_state != STATE_STARTED)
                throw new IllegalStateException (this + ": stop() must be called from STARTED state, current state is " + STATE_NAMES [m_state]);
        }

        m_data += data;
        if (DO_STATE_CHECKS) m_state = STATE_STOPPED;
    }

    public double getDuration ()
    {
        if (DO_STATE_CHECKS)
        {
            if (m_state != STATE_STOPPED && m_state != STATE_READY)
                throw new IllegalStateException (this + ": getDuration() must be called from READY or STOPPED state, current state is " + STATE_NAMES [m_state]);
        }

        return m_data;
    }

    public void reset ()
    {
		m_state = 0;
        if (DO_STATE_CHECKS) m_state = STATE_READY;
    }

    // protected: .............................................................

    protected HRTimer() {reset();}

    // package: ...............................................................

    // private: ...............................................................

    /*
     * This is supposed to return a fractional count of milliseconds elapsed
     * since some indeterminate moment in the past. The exact starting point
     * is not relevant because this timer class reports time differences only.
     *
     * JNI code in HRTIMER_LIB library is supposed to implement this.
     */
    public static native double getTime ();


    private int m_state; // used to keep track of timer state
    private double m_data; // timing data

    private static final String HRTIMER_LIB = "hrtlib";

    static
    {
        try
        {
            System.loadLibrary (HRTIMER_LIB);
        }
        catch (UnsatisfiedLinkError e)
        {
            System.out.println ("native lib '" + HRTIMER_LIB
                + "' not found in 'java.library.path': "
                + System.getProperty ("java.library.path"));

            throw e; // re-throw
        }
    }

} // end of class
// ----------------------------------------------------------------------------
