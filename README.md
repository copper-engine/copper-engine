copper-engine
=============

COPPER - the high performance Java workflow engine.

COPPER is an open-source, powerful, light-weight, and easily configurable **workflow engine**. The power of COPPER is that it uses **Java** as a description language for workflows. See [copper-engine.org](http://www.copper-engine.org) for more information.


How to build
------------

COPPER is built using [Gradle](http://www.gradle.org). However, you don't need to install Gradle, because COPPER is using the [Gradle wrapper](http://www.gradle.org/docs/current/userguide/gradle_wrapper.html).

To build all COPPER projects, just execute the following in the projects root directory:

    ./gradlew assemble

If you want to build all and run all tests, just execute:

    ./gradlew build

To generate Eclipse project files, run:

    ./gradlew eclipse

once in the projects root directory and open the corresponding projects with the eclipse IDE. (You must perform this step every time the project dependencies change).

How to contribute
-----------------

1) Create an issue on GitHub

2) Create a fork  on GutHub

3) Commit your changes incl. WHATSNEW.txt
  * Ensure, that your sources are UTF-8 encoded
  * If possible, use [eclipse-codestyle.xml](blob/master/common/eclipse-codestyle.xml) for Eclipse
  * If possible, use [intellij-codestyle.xml](blob/master/common/intellij-codestyle.xml) for IntelliJ IDEA with <code>./gradlew assemble</code>
  
4) Build all and run tests

    ./gradlew build
    

5) Push your changes to your fork

6) Create a pull request on GtHub

**Have fun!**


<hr>
Copyright 2002-2014 Copper Engine Development Team
