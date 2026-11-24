plugins {
    id("notes.android.library.compose")
}

android {
    namespace = "com.mhxtecnologia.notes.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons.core)
}
