## Description

The program uses Selenium (used normally for website testing) to automatically fill out the daily screener for you each morning at 12:01 AM.


##Setup
If you build from source, be sure to include Selenium for Java in your JAR. Selenium has been shaded  in to pre-compiled binaries via Maven.

This program should work on any platform supported by Selenium and Java. Windows, Mac, and Linux should all be fine provided the setup is done correctly and the program is run with the right arguments.

In order for the program to function, two programs must first be installed on the computer:
1. [Google Chrome](https://chrome.google.com). If you already have Chrome, proceed to the next step.
2. [Selenium Chrome Driver](https://chromedriver.chromium.org/downloads). Be sure to download the correct version of the driver for your operating system and the version of Google Chrome that is installed on your computer.

## Usage
The program takes one mandatory argument: the path to the Selenium Chrome driver you downloaded during setup. This will be the first argument. If you wish to supply a username and password to run the program headless, those should be the next two arguments (in that order).

You can run this program headless (i.e. without a Google Chrome window opening) if both of the following conditions are met:
1. You are willing to submit your NYU Home credentials to the program as arguments. These credentials are not stored or sent anywhere, except for of course to the NYU Home login page.
2. You have your NYU multi-factor authentication settings set to automatically send the Duo mobile app a push. When you open the app, after a few seconds, you will receive a Duo request. Please approve this request so the app can finish filling out the screener for you. This will happen only once, when you first start the program.

If both of these conditions are not met, open the program without any arguments. Then you will be prompted to type in your username and password, and use your multi-factor authentication method of choice. The downside to this is that it opens a Google Chrome window that, if closed, will stop the program. This does not impact the program's ability to fill out the screener, but could be annoying if you have to sort out the program's Chrome window from Chrome windows you use for everyday browsing.

In either case, the program should fill out the Daily Screener for you each morning at 12:01 AM as long as the program stays open. Note that if you restart your computer, you should re-open the program if you wish to continue having the screener filled out for you.

## Disclaimer

I am not planning on updating or providing any kind of support for this program, unless it suits my particular use case. This is why I am putting up the source code. You are welcome to modify the program to fit any noncommercial use case.