package com.example.test_dynamic_module

import android.app.Application
import io.flutter.FlutterInjector

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val manager = CustomDeferredComponentManager(this)
        FlutterInjector.setInstance(
            FlutterInjector.Builder()
                .setDeferredComponentManager(manager)
                .build()
        )
    }
}