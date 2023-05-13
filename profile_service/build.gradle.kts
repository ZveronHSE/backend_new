val contractVersion: String by rootProject

dependencies {
    implementation("com.vladmihalcea:hibernate-types-55:2.14.0")
    implementation("com.vladmihalcea:hibernate-types-52:2.14.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")

    // Контракты
    implementation("com.github.zveronHSe.contract:profile:a2a5e1b3b8")
    implementation("com.github.zveronHSe.contract:lot:$contractVersion")
    implementation("com.github.zveronHSe.contract:core:$contractVersion")
    implementation("com.github.zveronHSe.contract:blacklist:$contractVersion")
    implementation("com.github.zveronHSe.contract:address:$contractVersion")
}