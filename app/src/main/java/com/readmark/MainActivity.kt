package com.readmark

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import com.readmark.ui.screen.MainScreen
import com.readmark.ui.theme.ReadMarkTheme

/**
 * MainActivity - ReadMark 앱의 진입점
 * @AndroidEntryPoint: Hilt 의존성 주입 활성화
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        enableEdgeToEdge()
        setContent {
            ReadMarkTheme {
                MainScreen()
            }
        }
    }
}