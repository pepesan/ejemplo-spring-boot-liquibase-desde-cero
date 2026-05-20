plugins {
    java
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.cursosdedesarrollo"
version = "0.0.1-SNAPSHOT"
description = "ejemplo-spring-boot-liquibase-desde-cero"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

val liquibaseCli: Configuration by configurations.creating


dependencies {
    implementation("org.springframework.boot:spring-boot-h2console")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("com.mysql:mysql-connector-j")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testCompileOnly("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")

    liquibaseCli("org.liquibase:liquibase-core:5.0.2")
    liquibaseCli("com.h2database:h2:2.4.240")
    liquibaseCli("info.picocli:picocli:4.7.6")

}

tasks.withType<Test> {
    useJUnitPlatform()
}

// ── Tareas Liquibase CLI ───────────────────────────────────────────────────────

val liquibaseBaseArgs = listOf(
    "--changelog-file=db/changelog/db.changelog-master.yaml",
    "--search-path=src/main/resources",
    "--url=jdbc:h2:file:./data/migracion-demo",
    "--username=sa",
    "--password=",
    "--driver=org.h2.Driver"
)

tasks.register<JavaExec>("liquibaseUpdate") {
    group = "liquibase"
    description = "Aplica todos los changesets pendientes"
    classpath = liquibaseCli
    mainClass.set("liquibase.integration.commandline.LiquibaseCommandLine")
    jvmArgs("-Dliquibase.headless=true")
    args = liquibaseBaseArgs + "update"
}

tasks.register<JavaExec>("liquibaseUpdateToTag") {
    group = "liquibase"
    description = "Aplica changesets hasta -Ptag=<nombre>"
    classpath = liquibaseCli
    mainClass.set("liquibase.integration.commandline.LiquibaseCommandLine")
    jvmArgs("-Dliquibase.headless=true")
    args = liquibaseBaseArgs + listOf("update-to-tag", project.findProperty("tag")?.toString() ?: "")
}

tasks.register<JavaExec>("liquibaseRollback") {
    group = "liquibase"
    description = "Hace rollback hasta -Ptag=<nombre>"
    classpath = liquibaseCli
    mainClass.set("liquibase.integration.commandline.LiquibaseCommandLine")
    jvmArgs("-Dliquibase.headless=true")
    args = liquibaseBaseArgs + listOf("rollback", project.findProperty("tag")?.toString() ?: "")
}

tasks.register<JavaExec>("liquibaseStatus") {
    group = "liquibase"
    description = "Lista los changesets pendientes de aplicar"
    classpath = liquibaseCli
    mainClass.set("liquibase.integration.commandline.LiquibaseCommandLine")
    jvmArgs("-Dliquibase.headless=true")
    args = liquibaseBaseArgs + "status"
}

tasks.register<JavaExec>("liquibaseHistory") {
    group = "liquibase"
    description = "Muestra el historial de changesets aplicados"
    classpath = liquibaseCli
    mainClass.set("liquibase.integration.commandline.LiquibaseCommandLine")
    jvmArgs("-Dliquibase.headless=true")
    args = liquibaseBaseArgs + "history"
}
