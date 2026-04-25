package com.example.texteditor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.texteditor.ui.navigation.AppNavGraph
import dagger.hilt.android.AndroidEntryPoint
import com.example.texteditor.ui.theme.RichTextEditorTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RichTextEditorTheme {
                AppNavGraph()
            }
        }
    }
}