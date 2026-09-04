plugins {
    `java-library`
}

group = "io.tenoro.${rootProject.name}"
description = "API DTO module"

dependencies {
    implementation("org.springdoc:springdoc-openapi-starter-common:2.8.6")
    implementation("jakarta.validation:jakarta.validation-api")
}
