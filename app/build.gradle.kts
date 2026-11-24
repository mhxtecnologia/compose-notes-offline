plugins {
    id("notes.android.application")
    id("notes.android.application.compose")
    id("notes.android.hilt")
}

android {
    namespace = "com.mhxtecnologia.notes"

    defaultConfig {
        applicationId = "com.mhxtecnologia.notes"
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        debug {
            // Pacote próprio no debug: um build de desenvolvimento ou de teste nunca
            // instala por cima do app de produção nem mexe nos dados dele.
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.data)
    implementation(projects.feature.notes)
    implementation(projects.feature.editor)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
}
