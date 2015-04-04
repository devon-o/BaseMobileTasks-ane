package com.onebyonedesign.mobile.tasks;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.view.View;
import android.widget.Toast;
import com.adobe.fre.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Context extends FREContext
{
    /**
     * Create a new Context
     */
    public Context()
    {
        Extension.debug("Context()");
    }

    @Override
    public void dispose()
    {
        Extension.debug("Context.dispose()");

        Extension.context = null;
    }

    @Override
    public Map<String, FREFunction> getFunctions()
    {
        Extension.debug("Context.getFunctions()");

        Map<String, FREFunction> functionMap = new HashMap<String, FREFunction>();

        functionMap.put("shareImage", new ShareImageFunction());
        functionMap.put("setFullscreen", new SetFullScreenFunction());
        functionMap.put("getOSVersion", new GetOSVersionFunction());
        functionMap.put("showText", new ShowToastFunction());
        functionMap.put("vibrate", new VibrateFunction());

        return functionMap;
    }

    // ANE Function calls

    /**
     * Share Image
     * @param imagePath     public path to image file
     * @param chooserTitle  title of app chooser [optional]
     */
    public void shareImage(String imagePath, String chooserTitle)
    {
        // no image path passed
        if (imagePath==null || imagePath.length()==0)
        {
            String reason = "Invalid image path";
            Extension.warn(reason);
            dispatchEvent("shareImageError", reason);
            return;
        }

        // Create File from the image path
        File f = new File(imagePath);

        // Image file doesn't exist or can't be read
        if (!f.exists() || !f.canRead())
        {
            String reason = String.format("File not found or read (%s)", imagePath);
            Extension.warn(reason);
            dispatchEvent("shareImageError", reason);
            return;
        }

        // Create the new Intent using the 'Send' action.
        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        // Set the MIME type
        shareIntent.setType("image/*");

        // Create URI from file and add to intent
        Uri uri = Uri.fromFile(f);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);

        if (chooserTitle!=null && chooserTitle.length() > 0)
            getActivity().startActivity(Intent.createChooser(shareIntent, chooserTitle));
        else
            getActivity().startActivity(shareIntent);

        dispatchEvent("imageShared");
    }

    // Messaging

    /**
     * Dispatch event back to ANE Actionscript
     * @param type  type of Flash BaseMobileTaskEvent (@see BaseMobileTaskEvent.as)
     */
    public void dispatchEvent(String type)
    {
        // Dispose has been called
        if (Extension.context==null)
        {
            Extension.warn(String.format("Attempting to send event (%s) to Flash after ANE dispose()", type));
            return;
        }
        dispatchEvent(type, "");
    }

    /**
     * Dispatch event back to ANE Actionscript
     * @param type		type of Actionscript BaseMobileTaskEvent (@see BaseMobileTaskEvent.as)
     * @param reason	reason for event incl. event code if it exists
     */
    public void dispatchEvent(String type, String reason)
    {
        // Dispose has been called
        if (Extension.context==null)
        {
            Extension.warn(String.format("Attempting to send event (%s) reason (%s) to Flash after ANE dispose()", type, reason));
            return;
        }

        Extension.debug(String.format("Context.dispatchEventWithReason(%s, %s)", type, reason));
        dispatchStatusEventAsync(type, reason);
    }

    // Nested Function Classes

    /** Share Function */
    class ShareImageFunction implements FREFunction
    {
        @Override
        public FREObject call(FREContext freContext, FREObject[] freObjects)
        {
            Extension.debug("ShareImageFunction.call()");

            String imagePath = "";
            String chooserTitle = "";

            try
            {
                imagePath = freObjects[0].getAsString();
            }
            catch (Exception e)
            {
                Extension.warn("No image path sent", e);
            }

            try
            {
                chooserTitle = freObjects[1].getAsString();
            }
            catch (Exception e)
            {
                Extension.warn("No chooser title sent", e);
            }

            // do actual share
            shareImage(imagePath, chooserTitle);

            return null;
        }
    }

    /** Set Full Screen Function */
    class SetFullScreenFunction implements FREFunction
    {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        public FREObject call(FREContext freContext, FREObject[] freObjects)
        {
            Extension.debug("SetFullScreen()");
            
            // utilizes 'immersive mode' only available on KitKat (4.4) and above
            // @see https://developer.android.com/training/system-ui/immersive.html
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
                return null;

            try
            {
                final int uiOptions =  View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                     | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                     | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                     | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                     | View.SYSTEM_UI_FLAG_FULLSCREEN
                                     | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

                final View decorView = getActivity().getWindow().getDecorView();
                decorView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus)
                    {
                        if (hasFocus)
                            decorView.setSystemUiVisibility(uiOptions);
                    }
                });

                decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility)
                    {
                        decorView.setSystemUiVisibility(uiOptions);
                    }
                });
                
                // set immediately
                decorView.setSystemUiVisibility(uiOptions);
                
                // return 'real' size of display in array
                Point p = new Point();
                decorView.getDisplay().getRealSize(p);
                
                FREArray ret = FREArray.newArray(2);
                ret.setObjectAt(0, FREObject.newObject(p.x));
                ret.setObjectAt(1, FREObject.newObject(p.y));
                return ret;
            }
            catch (Exception e)
            {
                Extension.warn("Could not set full screen", e);
            }
            
            return null;
        }
    }

    /** Get OS Version */
    class GetOSVersionFunction implements FREFunction
    {
        @Override
        public FREObject call(FREContext freContext, FREObject[] freObjects)
        {
            Extension.debug("GetOSVersion()");
            try
            {
                return FREObject.newObject(Build.VERSION.RELEASE);
            }
            catch (Exception e)
            {
                Extension.warn("Could not return OS version", e);
                return null;
            }
        }
    }

    /** Show toast text function */
    class ShowToastFunction implements FREFunction
    {
        @Override
        public FREObject call(FREContext freContext, FREObject[] freObjects)
        {
            Extension.debug("ShowToastFunction()");
            try
            {
                String text = freObjects[0].getAsString();
                int dur = freObjects[1].getAsInt();
                
                // default to short display
                int duration = Toast.LENGTH_SHORT;
                if (dur==1)
                    duration = Toast.LENGTH_LONG;
                    
                Toast toast = Toast.makeText(getActivity().getApplicationContext(), text, duration);
                toast.show();
            }
            catch (Exception e)
            {
                Extension.warn("Cannot make toast", e);
            }

            return null;
        }
    }

    /** Vibrate device function */
    class VibrateFunction implements FREFunction
    {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public FREObject call(FREContext freContext, FREObject[] freObjects)
        {
            try
            {
                long duration = (long)freObjects[0].getAsInt();
                Vibrator vibe = (Vibrator) getActivity().getApplicationContext().getSystemService(android.content.Context.VIBRATOR_SERVICE);
                if(!vibe.hasVibrator())
                    return null;

                vibe.vibrate(duration);
            }
            catch (Exception e)
            {
                Extension.warn("Could not vibrate device", e);
            }
            return null;
        }
    }
}