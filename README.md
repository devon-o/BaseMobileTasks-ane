Air Native Extension for Performing Base Tasks on Mobile Devices
======

This is an (*Android only* atm) [ane](http://www.adobe.com/devnet/air/native-extensions-for-air.html) for performing base tasks on device.

Supported Tasks
------

- Get OS Version String
- Show Text Display (Android Toast)
- Set Full Screen (Android Immersive mode)
- Share Image (User selects an installed app (e.g. Facebook or Twitter) to share image with)

Build
------

Compiled .swc and .ane files are included in the bin directory, but to perform a clean build; set the properties in build.properties.template appropriately, change its name to build.properties and run the default ant target in build.xml in the root directory.

Usage
------

The BaseMobileTasks Actionscript class is a Singleton instance and should be accessed with `BaseMobileTasks.instance`. Before doing so it is recommended to first check if it is supported on the current platform with the static method `BaseMobileTasks.isSupported()`.

To get the OS Version:

```actionscript
if (BaseMobileTasks.isSupported())
	// version = e.g. '5.0.1' - may also be null, so should check before using
	var osVersion:String = BaseMobileTasks.instance.getOSVersion();
```

To display a text message (Toast text):

```actionscript
if (BaseMobileTasks.isSupported())
	// May also use BaseMobileTasks.TEXT_DISPLAY_SHORT
	BaseMobileTasks.instance.showTextDisplay("Hello World", BaseMobileTasks.TEXT_DISPLAY_LONG);
```

To go into full screen (*note:* requires Android 4.4 or higher):

```actionscript
if (BaseMobileTasks.isSupported())
{
	var actualScreenDimensions:Array = BaseMobileTasks.instance.setFullscreen();
	if (actualScreenDimensions != null)
	{
		var actualWidth:int = actualScreenDimensions[0];
		var actualHeight:int = actualScreenDimensions[1];
	}
	else
	{
		// Either an error has occurred or user is on an Android device less than 4.4. You could check with getOSVersion() 1st if desired.
		// Store screen width and height another way (e.g. stage.fullScreenWidth && stage.fullScreenHeight)
	}
}
```

To share an image, first save the image to a public directory (I recommend creating an appropriately named folder within `File.userDirectory`) on the device then send its path to the ane like so:

```actionscript
private function ShareImage(image:BitmapData) 
{
	// can't share
	if (!BaseMobileTasks.isSupported())
		return;
		
	var file:File = File.userDirectory.resolvePath("MyApp" + File.separator + "imageName.jpg");
	
	var imageBytes:ByteArray = image.encode(image.rect, new JPEGEncoderOptions(80));
	var stream:FileStream = new FileStream();
	stream.open(file, FileMode.WRITE);
	stream.writeBytes(imageBytes);
	stream.close();
	
	BaseMobileTasks.instance.addEventListener(BaseMobileTaskEvent.IMAGE_SHARED, onImageShared);
	BaseMobileTasks.instance.addEventListener(BaseMobileTaskEvent.SHARE_IMAGE_ERROR, onImageShareErr);
	
	var title:String = "Share Image from MyApp!";
	BaseMobileTasks.instance.shareImage(file.nativePath, title);
}

private function onImageShared(e:BaseMobileTaskEvent):void
{
	// important to remove event listeners from Singleton instance
	BaseMobileTasks.instance.removeEventListener(BaseMobileTaskEvent.IMAGE_SHARED, onImageShared);
	BaseMobileTasks.instance.removeEventListener(BaseMobileTaskEvent.SHARE_IMAGE_ERROR, onImageShareErr);
	
	// User has been presented with a list of apps to share with. He or she may still decide not to share.
}

private function onImageShareErr(e:BaseMobileTaskEvent):void
{
	// important to remove event listeners from Singleton instance
	BaseMobileTasks.instance.removeEventListener(BaseMobileTaskEvent.IMAGE_SHARED, onImageShared);
	BaseMobileTasks.instance.removeEventListener(BaseMobileTaskEvent.SHARE_IMAGE_ERROR, onImageShareErr);
	
	trace("Could not share image: " + e.reason);
}
```

Installation
------

Include the .swc file in your project and, when compiling, be sure the .ane file is inside your extdir. For more info, [RTFM](http://help.adobe.com/en_US/air/build/WS597e5dadb9cc1e0253f7d2fc1311b491071-8000.html).

In your app descriptor include the extension id like:

```xml
<extensions>
	<extensionID>com.onebyonedesign.basemobiletasks</extensionID>
</extensions>
```

If planning to share images, be sure to include the Android permission to write to external storage:

```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```

Notes
------

- The .swc and .ane files included in the bin directory were compiled using AIR 16.0 sdk and swf-version 27 (16.0)
