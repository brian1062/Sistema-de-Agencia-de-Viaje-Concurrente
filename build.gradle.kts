plugins {
  java
  jacoco
  id("org.jetbrains.dokka") version "1.9.10"
  application
}

group = "org.petrinet"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
  testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.1")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")

  testImplementation("org.mockito:mockito-core:5.8.0")
  testImplementation("org.mockito:mockito-junit-jupiter:5.8.0")

  testImplementation("org.assertj:assertj-core:3.24.2")
}

application {
  mainClass.set("Main")
}

tasks.jar {
  manifest {
    attributes["Main-Class"] = "Main"
  }
}

tasks.named<JavaExec>("run") {
  standardInput = System.`in`
  args = project.findProperty("args")?.toString()?.split(" ") ?: emptyList()
}

tasks.test {
  useJUnitPlatform()
  finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
  dependsOn(tasks.test)
  reports {
    html.required.set(true)
    xml.required.set(true)
    csv.required.set(true)
  }
}

tasks.jacocoTestCoverageVerification {
  violationRules {
    rule {
      limit {
        minimum = "0.70".toBigDecimal()
      }
    }
  }
}

tasks.compileJava {
  options.encoding = "UTF-8"
  options.compilerArgs.add("-Xlint:unchecked")
  options.compilerArgs.add("-Xlint:deprecation")
}

tasks.compileTestJava {
  options.encoding = "UTF-8"
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}
