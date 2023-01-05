dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-server:3.1.4")
}


tasks.withType<Test> {
    useJUnitPlatform()
}
