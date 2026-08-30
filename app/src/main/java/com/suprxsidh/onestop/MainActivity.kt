package com.suprxsidh.onestop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.suprxsidh.onestop.battery.data.AppDatabase
import com.suprxsidh.onestop.ui.nav.OneStopNavHost
import com.suprxsidh.onestop.ui.theme.OneStopTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getInstance(applicationContext)
        setContent {
            OneStopTheme {
                OneStopNavHost(db)
            }
        }
    }
}
