package com.onebyonedesign.mobile.tasks;

import android.util.Log;
import com.adobe.fre.FREContext;
import com.adobe.fre.FREExtension;

public class Extension implements FREExtension
{
    /** Output TAG */
    public static final String TAG = "[BaseMobileTasks ANE]";

    /** Context */
    public static FREContext context;

    /** Create Context */
    public FREContext createContext(String extID)
    {
        Extension.debug("Extension.createContext()");
        return Extension.context = new MobileTasksContext();
    }

    /** Dispose */
    public void dispose()
    {
        Extension.debug("Extension.dispose()");
        // 'real' dispose performed in Context
    }

    /** Initialize */
    public void initialize()
    {
        Extension.debug("Extension.initialize()");
        // Nothing happening here
    }

    /** Debug output */
    public static void debug(String output)
    {
        Log.d(TAG, output);
    }

    /** Warn output with error */
    public static void warn(String output, Throwable err)
    {
        Log.w(TAG, output, err);
    }

    /** Warn output */
    public  static void warn(String output)
    {
        Log.w(TAG, output);
    }
}