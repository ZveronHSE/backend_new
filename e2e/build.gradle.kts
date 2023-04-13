val contractVersion: String by rootProject

dependencies {
    implementation("io.grpc:grpc-netty:1.52.0")

    implementation("com.github.zveronHSe.contract:apigateway:$contractVersion")
}