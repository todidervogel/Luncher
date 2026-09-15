package de.tobi.luncher.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

/**
 * Laedt die startbaren Apps und haelt sie im Speicher (Spec §6).
 *
 * Der Cache wird nicht bei jedem Tastendruck neu aufgebaut, sondern nur, wenn
 * sich am Paketbestand etwas aendert oder ein Start fehlschlaegt.
 */
class AppRepository private constructor(private val context: Context) {

    @Volatile
    private var cache: List<InstalledApp>? = null

    /** Die startbaren Apps, ohne den Launcher selbst. */
    fun apps(): List<InstalledApp> = cache ?: load().also { cache = it }

    /** Verwirft den Cache; der naechste [apps]-Aufruf laedt neu. */
    fun invalidate() {
        cache = null
    }

    private fun load(): List<InstalledApp> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val packageManager = context.packageManager
        return packageManager.queryIntentActivities(intent, 0)
            .asSequence()
            .filter { it.activityInfo != null }
            .filter { it.activityInfo.packageName != context.packageName }
            .map {
                InstalledApp(
                    label = it.loadLabel(packageManager).toString(),
                    packageName = it.activityInfo.packageName,
                    activityName = it.activityInfo.name,
                )
            }
            .toList()
    }

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) = invalidate()
    }

    /**
     * Haengt sich an Paketaenderungen. Bewusst prozessweit und ohne Abmeldung:
     * eine neu installierte App soll auch dann auffindbar sein, wenn der
     * Launcher waehrend der Installation im Hintergrund stand.
     */
    private fun registerPackageReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        context.registerReceiver(packageReceiver, filter)
    }

    companion object {
        @Volatile
        private var instance: AppRepository? = null

        fun get(context: Context): AppRepository =
            instance ?: synchronized(this) {
                instance ?: AppRepository(context.applicationContext)
                    .also { it.registerPackageReceiver(); instance = it }
            }
    }
}
