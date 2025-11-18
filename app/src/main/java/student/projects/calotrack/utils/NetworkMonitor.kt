package student.projects.calotrack.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

class NetworkMonitor(private val context: Context, private val onNetworkAvailable: () -> Unit) {

    private val networkReceiver = object : BroadcastReceiver() { //(Lackner, 2023)
        override fun onReceive(context: Context?, intent: Intent?) {
            if (isOnline()) {
                onNetworkAvailable()
            }
        }
    }

    fun startMonitoring() { //(Lackner, 2023)
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(networkReceiver, filter)
    }

    fun stopMonitoring() { //(Lackner, 2023)
        context.unregisterReceiver(networkReceiver)
    }

    private fun isOnline(): Boolean { //(Lackner, 2023)
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
