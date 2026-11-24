plugins {
    id("notes.android.feature")
}

android {
    namespace = "com.mhxtecnologia.notes.feature.editor"
}

dependencies {
    // BackHandler, para o voltar do sistema salvar a nota.
    implementation(libs.androidx.activity.compose)
}
