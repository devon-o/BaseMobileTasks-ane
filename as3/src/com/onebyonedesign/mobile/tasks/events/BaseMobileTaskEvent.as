package com.onebyonedesign.mobile.tasks.events
{
	
import flash.events.Event;

public class BaseMobileTaskEvent extends Event 
{
	// Definitions
    
    /** Dispatched when sharing image fails */
	public static const SHARE_IMAGE_ERROR:String = "shareImageError";
    
    /** Dispatched once UI Dialog to share image is shown */
    public static const IMAGE_SHARED:String = "imageShared";
    
    /** Dispatched when user hits 'positive' button on confirmation dialog */
    public static const CONFIRMATION_POSITIVE:String = "confirmationPositive";
    
    /** Dispatched when user hits 'negative' button on confirmation dialog */
    public static const CONFIRMATION_NEGATIVE:String = "confirmationNegative";
    
    /** Dispatched when a confirmation dialog cannot be shown */
    public static const CONFIRMATION_ERROR:String = "confirmationError";
    
    // Instance vars

	/** Reason for error or additional data */
	public var reason:String;

	// Public Methods
	
	/** Create new BaseMobileTaskEvent */
	public function BaseMobileTaskEvent(type:String, reason:String) 
	{ 
		this.reason = reason;
		super(type);
	} 
	
	/** Clone */
	public override function clone():Event 
	{ 
		return new BaseMobileTaskEvent(this.type, this.reason);
	} 
	
	/** To String */
	public override function toString():String 
	{ 
		return formatToString("BaseMobileTaskEvent", "type", "reason"); 
	}
	
}
	
}