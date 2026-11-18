package com.example.catalogohub

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.catalogohub.ui.CatalogoApp
import com.example.catalogohub.ui.theme.CatalogoHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tela inteira; ícones da barra de status sempre claros, pois o topo do app é um degradê escuro
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        setContent {
            CatalogoHubTheme {
                CatalogoApp()
            }
        }
    }
}