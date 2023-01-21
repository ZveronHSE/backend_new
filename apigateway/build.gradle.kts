val protobufVersion: String by project

dependencies {
    implementation("com.github.zveronHSe:contract:feature~ZV-220-apigateway-SNAPSHOT")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.5.2")

    compileOnly("com.google.protobuf:protobuf-java-util:$protobufVersion")
}
