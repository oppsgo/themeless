package io.github.oppsgo.themeless

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import io.github.oppsgo.themeless.demo.ThemeDemoPage

class ThemelessApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeDemoPage.init()
        ThemeDemoPage.restorePersisted(this)
        AppCompatDelegate.setDefaultNightMode(ThemeDemoPage.getDefaultNightMode())
    }
}
