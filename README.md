[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/copper-engine/copper-engine/blob/master/LICENSE)
[![Build Status](https://img.shields.io/github/workflow/status/copper-engine/copper-engine/Build%20and%20upload)](https://github.com/copper-engine/copper-engine/actions?query=workflow%3A%22Build+and+upload%22)

copper-engine
=============

COPPER - the high performance Java workflow engine.

COPPER is an open-source, powerful, light-weight, and easily configurable **workflow engine**. The power of COPPER is that it uses **Java** as a description language for workflows. The project artifacts can be found on [Maven Central](https://search.maven.org/search?q=g:org.copper-engine%20AND%20a:copper-coreengine&core=gav). See [copper-engine.org](http://www.copper-engine.org) for more information.


How to build
------------

COPPER is built using [Gradle](http://www.gradle.org). However, you don't need to install Gradle, because COPPER is using the [Gradle wrapper](http://www.gradle.org/docs/current/userguide/gradle_wrapper.html).
Note: If you are behind an internet proxy, you have to configure the corresponding system properties in gradle. See [Accessing the web via a proxy](https://docs.gradle.org/current/userguide/build_environment.html#sec:accessing_the_web_via_a_proxy).

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
       ./gradlew clean build
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


Performance Test
----------------

See [PERFORMANCE_TEST_HOWTO.MD](https://github.com/copper-engine/copper-engine/blob/master/projects/copper-performance-test/PERFORMANCE_TEST_HOWTO.MD)


License
-----------------
Copyright 2002-2018 Copper Engine Development Team

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
