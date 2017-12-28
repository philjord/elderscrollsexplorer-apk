# The Code for the app Call of Red Mountain #

Call of Red Mountain is an Android app that runs on the media files of the amazing Morrowind game.

There is an equivilent app for running on any desktop called ElderScrollsExplorer (https://github.com/philjord/ElderScrollsExplorer)

Both repos are built on a common base project that is built on many sub projects.

In order to make changes to the user interface on Android you only need this repo imported into Android Studio. 
It comes with all the libraries needed to get the game running.

If you want to make changes to the underlying game itself then you need to follow the instructions in ElderScrollsExplorer, that will set up an environment that will allow you to recompile
all the libraries that are used by this project. But be warned this is a huge amount of code written over the past 20 years.


The good news is I'm always happy to help out and any feedback is very welcome (p.j.nz@outlook.com).


### Why is this repo called such a strange name? ###

This repo will build the Call of Red Mountain apk file, which can then be run on any Android device

The app can actually run all of the elder scrolls series games, and the fallout series as well. 

This repo is built on a common base repo called

ElderScrollsExplorerBase (https://bitbucket.org/philjord/ElderScrollsExplorerBase) 

This project is common between both the Android build and the desktop build


This repo is currently called ESEAndroid-apk

 * ESE (for Elder Scrolls Explorer)

 * Android (for the code line which is compatible with Android i.e. no awt code)

-apk for the portion of code that puts together an Android Activity


By comparision the project ElderScrollsExplorer (https://github.com/philjord/ElderScrollsExplorer)

Is an AWT version of ESEAndroid-apk that's built on ElderScrollsExplorerBase but that uses AWT components to show itself and be navigable.


ElderScrollsExplorer was the original project so it got the original name. I should rename this ElderScrollsExplorer-apk


### Contribution guidelines ###

There are no guideline as yet becuase very few people have contributed so far.

### Who do I talk to? ###

If you need help send me (Phil) an email, I'm always glad to help p.j.nz@outlook.com

For help with Java3D or Java OpenGL see the great guys at jogamp.org
