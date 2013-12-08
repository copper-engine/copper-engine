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

**Have fun!**


<hr>
Copyright 2002-2013 Copper Engine Development Team
