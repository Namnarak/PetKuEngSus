group = "com.willfp"
version = rootProject.version

repositories {
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://jitpack.io")
    maven("https://repo.codemc.io/repository/creatorfromhell/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.7-R0.1-SNAPSHOT")
    compileOnly("com.github.ben-manes.caffeine:caffeine:3.0.2")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("net.milkbowl.vault:VaultUnlockedAPI:2.20")
    compileOnly("net.luckperms:api:5.4")
}