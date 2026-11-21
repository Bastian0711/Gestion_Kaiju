plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    jacoco
}

android {
    namespace = "com.example.appkaiju.stock"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.appkaiju.stock"
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    testCoverage {
        jacocoVersion = "0.8.13"
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

jacoco {
    toolVersion = "0.8.13"
}

// Reporte de cobertura de la lógica (IE 3.2.1).
// Ejecutar:  gradlew jacocoTestReport
// Ver:       app/build/reports/jacoco/jacocoTestReport/html/index.html
// Se excluye la interfaz (pantallas Compose, navegación, tema) y el código generado o de plataforma,
// porque la rúbrica mide "el código lógico": modelos, reglas, repositorios y ViewModels.
val exclusionesCobertura = listOf(
    "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
    "**/*_Impl*", "**/*Dao_Impl*", "**/ComposableSingletons*",
    "**/ui/**", "**/navigation/**", "**/MainActivity*",
    "**/data/AppDatabase*", "**/data/AppContainer*", "**/data/AlmacenFotos*",
    "**/data/NotificadorStock*", "**/data/RoomEjecutorTransaccion*",
    "**/data/remote/RetrofitInstance*"
)

tasks.register<JacocoReport>("jacocoTestReport") {
    group = "verification"
    description = "Genera el reporte de cobertura de las pruebas unitarias (debug)."
    dependsOn("testDebugUnitTest")

    reports {
        html.required.set(true)
        xml.required.set(true)
    }

    val build = layout.buildDirectory
    classDirectories.setFrom(
        fileTree(build.dir("intermediates/built_in_kotlinc/debug")) { exclude(exclusionesCobertura) },
        fileTree(build.dir("tmp/kotlin-classes/debug")) { exclude(exclusionesCobertura) }
    )
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    executionData.setFrom(
        fileTree(build) {
            include(
                "outputs/unit_test_code_coverage/debugUnitTest/*.exec",
                "jacoco/testDebugUnitTest.exec"
            )
        }
    )
}