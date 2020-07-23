# Yiff.Party-Syncer
A grabber for yiff.party written in java

Can download natively from yiff.party, as well as:
- mega.nz

# Pre-requisites
* A fully set up Java IDE. I work with Eclipse.
* A complete webserver package like XAMPP.
- You need the SQL Server for storing metadata
- If you want to use the Frontpage userscript grabber, you also need the webserver component
* MegaCMD (optional for automatic Mega.nz downloading)
- Downlaod and install from here: https://mega.nz/cmd

# Steps to get your own instance up and running
**Step 0: Clone the repo**

**Step 1: get your SQL Server up and running**

Currently, Yiff.Party Syncer needs a SQL Server to store patreon and file metadata.
Set up your SQL Server, import the yiffparty.sql into your server.
This will create a database named yiffparty with three empty tables.

**Step 2: create a user for Yiff.Party Syncer in your DB**

Create a user with your favourite database management tool, and grant it permissions to do stuff in the yiffparty database.
It's best to log in with the user and test if it can create, alter and delete stuff.

**Step 3: edit configuration in Yiff.Party Syncer**

Currently all configuration is hardcoded in the Logic.Config class.
Look through it, the class is fully commented.
YOU WILL NEED TO INCLUDE THE RIGHT CONNECTOR FOR YOUR DATABASE INTO YOUR PROJECT.
Logic.OUtil has been configured to work with the mysql JDBC connection, but it is **NOT** included in this project.

**Step 4 (optional): set up the yiff.party frontpage grabber UserScript**

The yiffparty.js Userscript refreshed the frontpage of Yiff.Party to look at all patreons which have been updated and writes those in the DB.
This userscript also needs a webserver for the file www/postdata.php to run in. Cause I couldn't figure out how to post data from JS to MySQL.
Paste the postdata.php in the webroot of a webserver running on localhost. edit postdata.php and enter the credentials of the SQL User we set up beforehand.
After you imported the userscript with your favourite script manager, leave the frontpage of yiff.party open in a browser window. 
It will auto-refresh every 10 minutes and post the website data in the DB Server.

**Step 5 (optional): Set up Mega.nz downloading**

You need a mega.nz account for this feature. Also, downloads will obviously count to your transfer quota, and when your account runs out,
you'll have to wait for the daily reset to continue downloading.
Add the following directory to your system PATH Variable after you installed MegaCMD:
(On Windows)
C:\Users\"Username"\AppData\Local\MEGAcmd
You can input your credentials on the settings page.
Attention: If your password contains a "-" minus sign, the CMDlet will error out! Other special characters might break it too...

And that's it! Now you can run it from your IDE or compile a run package.

# Things to keep in mind
It's best to run Yiff.Party syncer the following Java runtime parameters:
> -Xms128M -Xmx8G -server -XX:+AggressiveOpts -XX:+AggressiveHeap

The parameter -server requires a JDK.
Use Xmx8G if you have OoM errors. Yiff.Party Syncer automatically converts PNGs to high quality JPGs.
The problem is that sometimes PNGs on Yiff.Party can be incredibly big. Like, 10000 pixels by 10000 pixels.
The conversion for those big PNGs takes a huge amount of RAM.
You can help by looking at the Method convert in the class Logic.Workers.WorkerDownloader, maybe 
you know of a way to optimize converting big PNGs.

# How to use
Open Tool, leave it running :)
When you have freshly installed it, there is nothing it will do.
You will have to either manually add a creator with the "Add" button after you posted the yiff.party creator page in the text field above it, 
or you check the links that the Userscript has grabbed from the frontpage with the "Next" button -> "Open in browser"
With "Yes" and "No" you can decide if you want to keep the selected, grabbed patreon for future tracking.

This has been tested on Windows 10 with the Oracle JDK 1.8

# Libs and APIs used:

MEGAcmd4J (source) - https://github.com/EliuX/MEGAcmd4J

JSoup (lib) - https://jsoup.org/

Apache Commons Compress (lib) - http://commons.apache.org/proper/commons-compress

XZ for Java (source) - https://tukaani.org/xz/java.html

-------------------------------------------------------------------------------

**TODO:**
- Fix excluded patreon pages
- See if we can support downloads from different websites that are linked in patreon posts