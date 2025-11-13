import java.nio.file.Paths

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "io.github.chitao1234.xdusrunlogin"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.chitao1234.xdusrunlogin"
        minSdk = 21
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true
        }
    }

    applicationVariants.all {
        outputs.all {
            val apkOutput = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl

            val pkg = applicationId
            val bt = buildType.name
            val abiFilter = apkOutput.filters.find {
                it.filterType == com.android.build.VariantOutput.FilterType.ABI.name
            }?.identifier
            val archPart = abiFilter ?: "universal"

            val safePkg = pkg.replace('.', '_')

            // `this` is ApkVariantOutputImpl underneath
            apkOutput.outputFileName = "${safePkg}-${archPart}-${bt}.apk"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

val aarOut = layout.buildDirectory.file("gomobile/xdsrun.aar")

val gomobileBind by tasks.registering(Exec::class) {
    val goLibDir = Paths.get("$rootDir", "lib", "xdsrun-login")
    workingDir = file(goLibDir)

    commandLine(
        "gomobile", "bind",
        "-target=android",
        "-javapkg", "io.chitao1234.xdusrunlogin",
        "-o", aarOut.get().asFile.absolutePath,
        "xdsrun/core"
    )
    inputs.files(
        file(goLibDir.resolve("go.mod")),
        file(goLibDir.resolve("go.sum")),
        fileTree(goLibDir) {
            include("**/*.go")
        }
    )
    outputs.file(aarOut)
}

tasks.named("preBuild") {
    dependsOn(gomobileBind)
}

dependencies {
    implementation(files(aarOut.map { it.asFile }))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.android.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}