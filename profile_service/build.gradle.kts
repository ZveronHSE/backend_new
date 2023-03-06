val contractVersion: String = "1.8.6"

dependencies {
    implementation("com.vladmihalcea:hibernate-types-55:2.14.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")

    // Контракты
    implementation("com.github.zveronHSe.contract:profile:feature~ZV-342-favorite-lots-separation-SNAPSHOT")
    implementation("com.github.zveronHSe.contract:lot:$contractVersion")
    implementation("com.github.zveronHSe.contract:blacklist:$contractVersion")
    implementation("com.github.zveronHSe.contract:address:$contractVersion")
}