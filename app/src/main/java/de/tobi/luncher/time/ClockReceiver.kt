package de.tobi.luncher.time

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

/**
 * Minutentakt fuer die Uhr (Spec §4.1). [Intent.ACTION_TIME_TICK] feuert einmal
 * pro Minute und laesst sich nur zur Laufzeit registrieren -- deshalb in
 * `onStart` an- und in `onStop` abmelden. Kein Polling, kein Timer.
 */
class ClockReceiver(private val onTick: () -> Unit) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) = onTick()

    companion object {
        fun filter(): IntentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
    }
}
