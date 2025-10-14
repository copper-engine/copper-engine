ext["moduleName"] = "org.copperengine.core"

dependencies {
    api(project(":projects:copper-jmx-interface"))

    implementation("org.slf4j:slf4j-api:2.0.13")

    implementation("org.ow2.asm:asm:9.8")
    implementation("org.ow2.asm:asm-commons:9.8")
    implementation("org.ow2.asm:asm-tree:9.8")
    implementation("org.ow2.asm:asm-util:9.8")
    implementation("org.ow2.asm:asm-analysis:9.8")
}

tasks.register<Zip>("scriptsZip") {
    archiveClassifier.set("scripts")
    from("src/main/database")
    into("scripts/sql")
}

tasks.assemble {
    dependsOn(tasks.named("scriptsZip"))
}

artifacts {
    archives(tasks.named("scriptsZip"))
}
