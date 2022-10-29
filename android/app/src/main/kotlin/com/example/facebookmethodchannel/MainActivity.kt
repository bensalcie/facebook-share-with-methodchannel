package com.example.facebookmethodchannel

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.annotation.NonNull
import com.facebook.CallbackManager
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.widget.ShareDialog
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    private val CHANNEL = "samples.flutter.dev/battery"

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
            // Note: this method is invoked on the main thread.
                call, result ->
            if (call.method == "getBatteryLevel") {
                val batteryLevel = getBatteryLevel()
                if (batteryLevel != -1) {
                    result.success(batteryLevel)
                } else {
                    result.error("UNAVAILABLE", "Battery level not available.", null)
                }
            } else if(call.method == "shareLinkOnFaceBook"){

                val url = call.arguments.toString()
                Log.d("Method arguments", "configureFlutterEngine: $url")

                val link: String? = call.argument("link")
               val sharedMessage = link?.let { shareLinkOnFaceBook( link = it) }

                if (sharedMessage != null) {
                    if (sharedMessage.contains("Shared")) {
                        result.success(sharedMessage)
                    } else {
                        result.error("UNAVAILABLE", sharedMessage, null)
                    }
                }


            }

            else {
                result.notImplemented()
            }
        }
    }

    private fun getBatteryLevel(): Int {
        val batteryLevel: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } else {
            val intent = ContextWrapper(applicationContext).registerReceiver(null, IntentFilter(
                Intent.ACTION_BATTERY_CHANGED))
            batteryLevel = intent!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100 / intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        }

        return batteryLevel
    }
    private fun shareLinkOnFaceBook( link:String):String{
        val shareDialog: ShareDialog =  ShareDialog(this)

        var message:String  ="Not shared"

        if (ShareDialog.canShow(ShareLinkContent::class.java)) {
            message = "Shared"
            val linkContent: ShareLinkContent = ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(link))
                .build()
            shareDialog.show(linkContent)

        }

        return  message

    }
}
