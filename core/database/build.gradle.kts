plugins {
    id("notes.android.library")
    id("notes.android.room")
    id("notes.android.hilt")
}

android {
    namespace = "com.mhxtecnologia.notes.core.database"
}

dependencies {
    api(projects.core.model)
    implementation(libs.kotlinx.coroutines.core)
}
