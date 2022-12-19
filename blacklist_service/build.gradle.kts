plugins {
}

dependencies {
     implementation("com.github.zveronHSe.contract:blacklist:feature~ZV-217-blacklist-service-SNAPSHOT")

    implementation("net.devh:grpc-spring-boot-starter:2.13.1.RELEASE")
}

allOpen {
    annotations("javax.persistence.Entity", "javax.persistence.MappedSuperclass", "javax.persistence.Embedabble")
}