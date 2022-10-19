package org.pcgy.flutter.flutter_install_plugin


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.io.File
import java.io.FileNotFoundException

/** FlutterInstallPlugin */
class FlutterInstallPlugin(private val registrar: Registrar): MethodCallHandler {

  companion object {
    private const val installRequestCode = 1234
    private var apkFile: File? = null
    private var appId: String? = null

    @JvmStatic
    fun registerWith(registrar: Registrar): Unit {
      val channel = MethodChannel(registrar.messenger(), "flutter_install_plugin")
      val installPlugin = FlutterInstallPlugin(registrar)
      channel.setMethodCallHandler(installPlugin)
      registrar.addActivityResultListener { requestCode, resultCode, intent ->
//        Log.d(
//                "ActivityResultListener",
//                "requestCode=$requestCode, resultCode = $resultCode, intent = $intent"
//        )
        if (resultCode == Activity.RESULT_OK && requestCode == installRequestCode) {
          installPlugin.install24(registrar.context(), apkFile, appId)
          true
        } else

          false
      }
    }
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    when (call.method) {
      "getPlatformVersion" -> {
        result.success("Android ${android.os.Build.VERSION.RELEASE}")
      }
      "installApk" -> {
        val filePath = call.argument<String>("filePath")
        val appId = call.argument<String>("appId")
        //Log.d("android plugin", "installApk $filePath $appId")
        try {
          installApk(filePath, appId)
          result.success("Success")
        } catch (e: Throwable) {
          result.error(e.javaClass.simpleName, e.message, null)
        }
      }

      "gotoAndroidMarket" -> {
        try {
          val marketPackageName = call.argument<String>("marketPackageName")
//          val marketClassName = call.argument<String>("marketClassName")
          //Log.d("android plugin", "gotoAndroidMarket $marketPackageName ")
          toMarket(marketPackageName)
        } catch (e: Throwable) {
          result.error(e.javaClass.simpleName, e.message, null)
        }
      }
      else -> result.notImplemented()
    }
  }

  private fun toMarket(marketPackageName: String?) {
    val activity: Activity =
            registrar.activity() ?: throw NullPointerException("context is null!")
    var packageInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
    try {
      /**
      * 去指定的应用市场 如果没有指定就打开任何应用市场
      * */
      val uri = Uri.parse("market://details?id=${packageInfo.packageName}")
      val goToMarket = Intent(Intent.ACTION_VIEW, uri)
      goToMarket.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      if (marketPackageName != null && marketPackageName.isNotEmpty()) {
        goToMarket.setPackage(marketPackageName)
      }
      activity.startActivity(goToMarket)
    }catch (e: Throwable){
      /**
       * 打开Google play 页面
       */
      val uri2 = Uri.parse("https://play.google.com/store/apps/details?id=${packageInfo.packageName}")
      val goToMarket2 = Intent(Intent.ACTION_VIEW, uri2)
      goToMarket2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      activity.startActivity(goToMarket2)
    }
  }

  private fun installApk(filePath: String?, currentAppId: String?) {
    if (filePath == null) throw NullPointerException("fillPath is null!")
    val activity: Activity =
            registrar.activity() ?: throw NullPointerException("context is null!")

    val file = File(filePath)
    if (!file.exists()) throw FileNotFoundException("$filePath is not exist! or check permission")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      if (canRequestPackageInstalls(activity)) install24(activity, file, currentAppId)
      else {
        showSettingPackageInstall(activity)
        apkFile = file
        appId = currentAppId
      }
    } else {
      installBelow24(activity, file)
    }
  }


  private fun showSettingPackageInstall(activity: Activity) { // todo to test with android 26
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      //Log.d("SettingPackageInstall", ">= Build.VERSION_CODES.O")
      val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
      intent.data = Uri.parse("package:" + activity.packageName)
      activity.startActivityForResult(intent, installRequestCode)
    } else {
      throw RuntimeException("VERSION.SDK_INT < O")
    }

  }

  private fun canRequestPackageInstalls(activity: Activity): Boolean {
    return Build.VERSION.SDK_INT <= Build.VERSION_CODES.O || activity.packageManager.canRequestPackageInstalls()
  }

  private fun installBelow24(context: Context, file: File?) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val uri = Uri.fromFile(file)
    intent.setDataAndType(uri, "application/vnd.android.package-archive")
    context.startActivity(intent)
  }

  /**
   * android24及以上安装需要通过 ContentProvider 获取文件Uri，
   * 需在应用中的AndroidManifest.xml 文件添加 provider 标签，
   * 并新增文件路径配置文件 res/xml/provider_path.xml
   * 在android 6.0 以上如果没有动态申请文件读写权限，会导致文件读取失败，你将会收到一个异常。
   * 插件中不封装申请权限逻辑，是为了使模块功能单一，调用者可以引入独立的权限申请插件
   */
  private fun install24(context: Context?, file: File?, appId: String?) {
    if (context == null) throw NullPointerException("context is null!")
    if (file == null) throw NullPointerException("file is null!")
    if (appId == null) throw NullPointerException("appId is null!")
    val intent = Intent(Intent.ACTION_VIEW)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    val uri: Uri = FileProvider.getUriForFile(context, "$appId.fileProvider.install", file)
    intent.setDataAndType(uri, "application/vnd.android.package-archive")
    context.startActivity(intent)
  }


}
