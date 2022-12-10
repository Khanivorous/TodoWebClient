import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.0.0"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.7.21"
    kotlin("plugin.spring") version "1.7.21"
}

group = "com.khanivorous"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.spring.io/milestone/")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.7")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("app.getxray:xray-junit-extensions:0.6.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.10.0")
    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner:4.0.0-RC3")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.test {
    useJUnitPlatform() {
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
        excludeTags ("integration")
    }
    testLogging {
        events ("started", "skipped", "passed", "failed", "STANDARD_OUT", "STANDARD_ERROR")
        showExceptions = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        debug {
            events ("started", "skipped", "passed", "failed", "STANDARD_OUT", "STANDARD_ERROR")
        }
    }
}

tasks.register<Test>("integrationTests") {
    useJUnitPlatform() {
        includeTags ("integration")
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
    }
    testLogging {
        events ("started", "skipped", "passed", "failed", "STANDARD_OUT", "STANDARD_ERROR")
        showExceptions = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        debug {
            events ("started", "skipped", "passed", "failed", "STANDARD_OUT", "STANDARD_ERROR")
        }
    }

}

tasks.register<Test>("allTests") {
    useJUnitPlatform() {
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
    }
    testLogging {
        events ("started", "skipped", "passed", "failed", "STANDARD_OUT", "STANDARD_ERROR")
        showExceptions = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        debug {
            events ("started", "skipped", "passed", "failed", "STANDARD_OUT", "STANDARD_ERROR")
        }
    }

}
