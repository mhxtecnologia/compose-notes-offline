plugins {
    id("notes.android.library")
}

android {
    namespace = "com.mhxtecnologia.notes.core.testing"
}

// Peças compartilhadas pelos testes das features: fakes e regras de JUnit.
dependencies {
    api(projects.core.model)
    api(projects.core.data)
    api(libs.junit4)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
}
