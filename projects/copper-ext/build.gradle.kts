ext["moduleName"] = "org.copperengine.ext"

dependencies {
    implementation(project(":projects:copper-coreengine"))

    implementation("org.eclipse.jgit:org.eclipse.jgit:7.1.0.202411261347-r")
    implementation("org.ow2.asm:asm:9.8")
    implementation("org.ow2.asm:asm-tree:9.8")
    implementation("commons-io:commons-io:2.16.1")
    implementation("com.google.guava:guava:31.0.1-jre")
    implementation("org.yaml:snakeyaml:1.33")

    testImplementation("org.slf4j:slf4j-api:2.0.13")
}