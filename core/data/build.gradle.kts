plugins {
    id("notes.android.library")
    id("notes.android.hilt")
}

android {
    namespace = "com.mhxtecnologia.notes.core.data"
}

dependencies {
    api(projects.core.model)
    implementation(projects.core.database)
    implementation(libs.kotlinx.coroutines.core)
}
