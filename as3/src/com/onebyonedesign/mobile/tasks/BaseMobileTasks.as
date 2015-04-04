package com.onebyonedesign.mobile.tasks
{
	
import com.onebyonedesign.mobile.tasks.events.*;
import flash.events.EventDispatcher;
import flash.events.StatusEvent;
import flash.external.ExtensionContext;
import flash.system.Capabilities;

public class BaseMobileTasks extends EventDispatcher 
{
    // Definitions

    /** Duration of text display */
	public static const TEXT_DISPLAY_SHORT:int = 0;
    public static const TEXT_DISPLAY_LONG:int = 1;

    // Static vars

	/** Static singleton instance */
	private static var _instance:BaseMobileTasks;
	
	/** Extension context */
	private static var _context:ExtensionContext;
	
	/** @private */
	public function BaseMobileTasks(enforcer:SingletonEnforcer):void 
	{
		if (enforcer == null)
		{
			throw new Error("BaseMobileTasks is Singleton. Use BaseMobileTasks.instance");
		}
		
		if (_instance==null)
		{
			try
			{
				_context=ExtensionContext.createExtensionContext("com.onebyonedesign.basemobiletasks", null);
			}
			catch (e:Error)
			{
				trace("[BaseMobileTasks] Cannot create Extension Context.", e);
			}
            
            if (_context == null)
            {
                trace("[BaseMobileTasks] Extension context not created.");
                return;
            }
            
			_context.addEventListener(StatusEvent.STATUS, onStatusEvent);
			_instance = this;
		}
	}

	/** Get singleton instance */
	public static function get instance():BaseMobileTasks
	{
		return (_instance != null) ? _instance : new BaseMobileTasks(new SingletonEnforcer());
	}
	
	/** Returns true if ANE is supported on current platform */
	public static function isSupported():Boolean
	{
        //TODO: iOS
		return isAndroid();
	}
    
	// Native functions
    
    /**
     * Share an image using generic share options
     * @param imagePath path to image (must be public path - use File.userDirectory rather than applicationStorageDirectory)
     * @param uiTitle   title shown on UI App chooser [Optional]
     */
    public function shareImage(imagePath:String, uiTitle:String=null):void
    { 
        // don't pass null object to native code
        if (uiTitle == null)
            uiTitle = "";
            
        _context.call("shareImage", imagePath, uiTitle);
    }
    
    /**
     * Set to Full Screen. For Android, this uses 'Immersive Mode' only available in Android 4.4 and up
     * @return  An array with full screen dimensions [width, height]. 
     *          May return null if error or not supported.
     */
    public function setFullscreen():Array
    {
        return _context.call("setFullscreen") as Array;
    }
    
    /**
     * Get the current device OS version
     * @return  returns String of OS version (e.g. "4.0") 
     *          May return null if error occurs
     */
    public function getOSVersion():String
    {
        return _context.call("getOSVersion") as String;
    }
    
    /**
     * Show a Native text message (eg. Android Toast text)
     * @param text      text to display
     * @param duration  duration to display text (should be either TEXT_DISPLAY_SHORT or TEXT_DISPLAY_LONG)
     */
    public function showTextDisplay(text:String, duration:int = 0):void
    {
        _context.call("showText", text, duration);
    }
    
    /**
     * Vibrate device
     * @param duration  duration of vibration in milliseconds
     */
    public function vibrate(duration:int):void
    {
        _context.call("vibrate", duration);
    }
    
	//	Implementation
    
	/** is android device */
	private static function isAndroid():Boolean
	{
		return Capabilities.manufacturer.indexOf('Android') > -1;
	}
	
	/** is iOS device */
	private static function isIOS():Boolean
	{
		return Capabilities.manufacturer.indexOf("iOS") > -1;
	}
    
	// Events
    
	/** On status event sent from Native Extension context */
	private function onStatusEvent(event:StatusEvent):void
	{
		var type:String = event.code;
		var reason:String = event.level;
        
		dispatchEvent(new BaseMobileTaskEvent(type, reason));
	}
}
	
}

class SingletonEnforcer
{}