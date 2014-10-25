copper-engine
=============

COPPER - the high performance Java workflow engine.

COPPER is an open-source, powerful, light-weight, and easily configurable **workflow engine**. The power of COPPER is that it uses **Java** as a description language for workflows. The project artifacts can be found at [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.copper-engine/copper-coreengine/badge.svg)](http://maven-badges.herokuapp.com/maven-central/org.copper-engine/copper-coreengine). See [copper-engine.org](http://www.copper-engine.org) for more information.


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

1. Create an issue on GitHub.
2. Create a fork  on GitHub.
3. Configure your IDE (Eclipse, IntelliJ IDEA) as described [below](#how-to-configure-your-ide).
4. Run `./gradlew assemble` once if you haven't done so in step 3. This will generate some WSDL stubs needed for some tests.
5. Commit your changes incl. [WHATSNEW.txt](WHATSNEW.txt)
   * Ensure, that your sources are UTF-8 encoded!
   * Ensure, that your sources start with our [Apache License header](common/apache-license-file.txt). (The build will fail if they don't.)
6. Build all and run tests:
       ./gradlew build
7. Push your changes to your fork.
8. Create a pull request on GtHub.

**Have fun!**


How to configure your IDE
-------------------------

### Eclipse

Run `./gradlew eclipse` once. This will create Eclipse project files which you can import. This also creates proper code style settings. Before committing you should always reformat the code. You can configure Eclipse to do this automatically on each save.

Every time a dependency changes in `build.gradle` you must run `./gradlew eclipse` again. You don't need to restart Eclipse for this, simply press F5 on the projects.

### IntelliJ IDEA

Before you open the project in IntelliJ for the first time, run `./gradlew assemble` once. This also creates proper code style settings, which IntelliJ automatically picks up. After that open `build.gradle`  with "File->Open" and follow the instructions, accept the defaults.

Before committing you should always reformat the code. You can configure IntelliJ to do this automatically on each commit.


<hr>
Copyright 2002-2014 Copper Engine Development Team
