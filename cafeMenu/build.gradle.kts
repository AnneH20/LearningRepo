plugins {
    kotlin("jvm") version "2.0.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.apache.pdfbox:pdfbox:2.0.31")
    // implementation("com.sun.mail:javax.mail:1.6.2") <-- If I were to implement an email
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
