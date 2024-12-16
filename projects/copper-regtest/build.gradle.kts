ext["moduleName"] = "org.copperengine.regtest"

sourceSets {
    create("workflow") {
        ext["srcDir"] = "$projectDir/src/workflow/java"
    }
}
sourceSets["test"].resources.srcDir(File(sourceSets["workflow"].ext["srcDir"].toString()))

dependencies {
    implementation(project(":projects:copper-coreengine"))
    implementation(project(":projects:copper-ext"))

    implementation("org.ow2.asm:asm:9.7")
    implementation("org.ow2.asm:asm-tree:9.7")
    implementation("org.yaml:snakeyaml:1.33")
    implementation("org.springframework:spring-jdbc:5.3.36")
    implementation("org.springframework:spring-context:5.3.36")
    implementation("org.springframework:spring-tx:5.3.36")
    implementation("com.google.guava:guava:31.0.1-jre")
//        testRuntimeOnly(fileTree(mapOf("dir" to "$rootDir/3rdPartyLibs", "include" to "*.jar")))

    testImplementation("mysql:mysql-connector-java:5.1.25")
    testImplementation("org.apache.derby:derby:10.13.1.1")
    testImplementation("postgresql:postgresql:9.1-901.jdbc4")
    testImplementation("com.h2database:h2:1.4.193")
    testImplementation("com.mchange:c3p0:0.10.0")
    testImplementation("org.slf4j:slf4j-api:2.0.13")
}