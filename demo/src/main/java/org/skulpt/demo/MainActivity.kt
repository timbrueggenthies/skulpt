package org.skulpt.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.skulpt.demo.ui.demos.FoxDemo
import org.skulpt.demo.ui.theme.SkulptTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SkulptTheme {
                FoxDemo()
            }
        }
    }
}
