package com.example.test_dynamic_module

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import io.flutter.embedding.engine.FlutterJNI
import io.flutter.embedding.engine.deferredcomponents.DeferredComponentManager
import io.flutter.embedding.engine.systemchannels.DeferredComponentChannel
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * CustomDeferredComponentManager
 *
 * Implementation of [DeferredComponentManager] that loads .so libraries
 * from a remote server (e.g. GitHub) instead of Google Play Dynamic Delivery.
 *
 * This implementation:
 * - Downloads the deferred .so file based on current device ABI.
 * - Saves it locally to app's private storage.
 * - Loads it using System.load().
 * - Notifies Flutter via [DeferredComponentChannel].
 */
class CustomDeferredComponentManager(private val context: Context) : DeferredComponentManager {

    private var flutterJNI: FlutterJNI? = null
    private var channel: DeferredComponentChannel? = null
    private val httpClient = OkHttpClient()

    override fun setJNI(flutterJNI: FlutterJNI) {
        this.flutterJNI = flutterJNI
        Log.i(TAG, "FlutterJNI set")
    }

    override fun setDeferredComponentChannel(channel: DeferredComponentChannel) {
        this.channel = channel
        Log.i(TAG, "DeferredComponentChannel set")
    }

    override fun installDeferredComponent(loadingUnitId: Int, componentName: String) {
        Log.i(TAG, "installDeferredComponent -> id=$loadingUnitId, name=$componentName")

        val abi = when (android.os.Build.SUPPORTED_ABIS[0]) {
            "arm64-v8a" -> "arm64-v8a"
            "armeabi-v7a" -> "armeabi-v7a"
            "x86_64" -> "x86_64"
            else -> "arm64-v8a"
        }

        val soUrl =
            "https://raw.githubusercontent.com/letanssang/assets/main/modules/$abi/libapp.so-2.part.so"
        val targetFile = File(context.filesDir, "libapp_$abi.so")

        Log.i(TAG, "Downloading from $soUrl")

        val request = Request.Builder().url(soUrl).build()
        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Download failed: ${e.message}")
                // Sửa lại: dùng componentName theo docs
                channel?.completeInstallError(componentName, e.message ?: "Unknown error")
            }

            @SuppressLint("UnsafeDynamicallyLoadedCode")
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e(TAG, "HTTP error: ${response.code}")
                    channel?.completeInstallError(componentName, "HTTP ${response.code}")
                    return
                }

                try {
                    Log.i(TAG, "Content-Type: ${response.header("Content-Type")}")

                    response.body?.byteStream()?.use { input ->
                        FileOutputStream(targetFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    Log.i(TAG, "Downloaded: ${targetFile.absolutePath}")

                    if (targetFile.exists() && targetFile.length() > 0) {
                        System.load(targetFile.absolutePath)
                        Log.i(TAG, "Library loaded successfully")
                        flutterJNI?.loadDartDeferredLibrary(loadingUnitId, arrayOf())
                        // Sửa lại: dùng componentName theo docs
                        channel?.completeInstallSuccess(componentName)
                    } else {
                        Log.e(TAG, "Downloaded file invalid")
                        channel?.completeInstallError(componentName, "Downloaded file invalid")
                    }

                } catch (ex: Exception) {
                    Log.e(TAG, "Install failed: ${ex.message}")
                    channel?.completeInstallError(componentName, ex.message ?: "Unknown error")
                }
            }
        })
    }

    override fun getDeferredComponentInstallState(
        loadingUnitId: Int,
        componentName: String
    ): String {
        Log.i(TAG, "getDeferredComponentInstallState -> id=$loadingUnitId, name=$componentName")
        // Since we don't have async install state tracking, just return "installed" when file exists.
        val abi = android.os.Build.SUPPORTED_ABIS[0]
        val targetFile = File(context.filesDir, "libapp_$abi.so")
        return if (targetFile.exists()) "installed" else "unknown"
    }

    override fun loadAssets(loadingUnitId: Int, componentName: String) {
        Log.i(TAG, "loadAssets called -> id=$loadingUnitId, name=$componentName")
        // Optional: If assets exist in a downloaded folder, we can rebind AssetManager here.
        // For this custom implementation, no assets are downloaded, so this is a no-op.
    }

    override fun loadDartLibrary(loadingUnitId: Int, componentName: String) {
        Log.i(TAG, "loadDartLibrary called -> id=$loadingUnitId, name=$componentName")
        flutterJNI?.loadDartDeferredLibrary(loadingUnitId, arrayOf())
    }

    override fun uninstallDeferredComponent(loadingUnitId: Int, componentName: String): Boolean {
        Log.i(TAG, "uninstallDeferredComponent -> id=$loadingUnitId, name=$componentName")

        val abi = android.os.Build.SUPPORTED_ABIS[0]
        val targetFile = File(context.filesDir, "libapp_$abi.so")

        return if (targetFile.exists()) {
            val result = targetFile.delete()
            Log.i(TAG, "Deleted $targetFile -> $result")
            result
        } else {
            Log.w(TAG, "File not found for uninstall")
            false
        }
    }

    override fun destroy() {
        httpClient.dispatcher.executorService.shutdown()
        flutterJNI = null
        channel = null
        Log.i(TAG, "Destroyed CustomDeferredComponentManager")
    }

    companion object {
        private const val TAG = "CustomDeferredManager"
    }
}
