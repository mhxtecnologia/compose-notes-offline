package com.mhxtecnologia.notes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mhxtecnologia.notes.core.designsystem.theme.NotesTheme
import com.mhxtecnologia.notes.navigation.NotesNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Desenha atrás das barras do sistema; o Scaffold de cada tela cuida dos insets.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            NotesTheme {
                NotesNavHost()
            }
        }
    }
}
