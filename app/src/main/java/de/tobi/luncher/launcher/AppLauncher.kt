package de.tobi.luncher.launcher

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent

/** Startet eine App ueber ihre Launcher-Activity (Spec §6). */
class AppLauncher(private val context: Context) {

    /**
     * @return true, wenn die App gestartet wurde. Bei false wurde sie
     * vermutlich gerade deinstalliert; der Aufrufer behandelt das wie einen
     * Fehlschlag und verwirft den App-Cache.
     */
    fun launch(app: InstalledApp): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            component = ComponentName(app.packageName, app.activityName)
            addCategory(Intent.CATEGORY_LAUNCHER)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        } catch (e: SecurityException) {
            false
        }
    }
}
