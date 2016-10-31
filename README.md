##Introduction:


ADB tool is a tiny tool with an easy-to-use UI. Using this tool Android mobile testers can collect string ids (for automation), screenshots, logcat, and dumpsys.

Click [HERE](https://www.youtube.com/watch?v=JQja2PydTIA) for demo video.


#Installation

1. To use this tool you should have ANDROID SDK. You can download ANDROID SDK from this link https://developer.android.com/studio/index.html#downloads

2. Set Android SDK path in Environment varaibles as ANDROID_HOME.

3. Download the [ADBTool.jar](https://sourceforge.net/projects/adbtool/)

#How to run

On MAC or Linux:

Open terminal and go to the path where ADBTool.jar file downloaded. And run this command

java -jar adbtool.jar

On Windows:

Double click on that jar file or run from terminal as

java -jar adbtool.jar

#Capturing Screenshot:

1. Click on Screen Shot button from left side menus.

2. Select your device from device list drop down.

3. Click on Capture button.

4. Click on Edit button if you want to edit screenshot.

5. Click on Save button to save the screenshot.

![capture](https://raw.githubusercontent.com/fission-labs/ADBTool/master/docs/Screenshot.gif)

#Logcat:

1. Click on Logcat button from left side menus.

2. Select your device from device list drop down.

3. Select Filter By Application and select package name.

4. Or you can choose Filter By TAG option and enter TAG.

5. Logcat will be displayed in text area. You can save logcat by clicking on Save button.

6. You can clear the logcat by clicking on Clear button.

7. You can search any text in logcat by entering text in search box.

![capture](https://raw.githubusercontent.com/fission-labs/ADBTool/master/docs/Logcat.gif)

#Extract Strings and String ids:

1. Select Extract Strings from Tools menu.

2. Select your device from device list drop down.

3. Select package name from package list drop down.

4. If you know the string and want to know its string id then enter your string in String Value text box then corrsponding string id will be shown in String ID text box.

5. Or by clicking on Get Strings.xml button, you can get all strings and string ids. 

![capture](https://raw.githubusercontent.com/fission-labs/ADBTool/master/docs/extractstrings.gif)

#Screen Recording:

1. Select Screen Record from left side menus.

2. Select your device from device list drop down.

3. Click on Start button to start recording.

4. Click on Stop button to stop recording.

5. Save the recorded video by clicking on Save button.

![capture](https://raw.githubusercontent.com/fission-labs/ADBTool/master/docs/Screenrecord.gif)





