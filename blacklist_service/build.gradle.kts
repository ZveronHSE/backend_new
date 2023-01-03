
dependencies {
     implementation("com.github.zveronHSe.contract:blacklist:master-SNAPSHOT")
     implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:3.1.4")
     implementation("org.springframework.boot:spring-boot-starter-web")
     implementation("org.springframework:spring-webflux:5.3.24")
     // https://mvnrepository.com/artifact/io.projectreactor.netty/reactor-netty
     implementation("io.projectreactor.netty:reactor-netty:1.1.1")
     // https://mvnrepository.com/artifact/io.netty/netty-resolver-dns-native-macos
     implementation("io.netty:netty-resolver-dns-native-macos:4.1.86.Final:osx-aarch_64")
}