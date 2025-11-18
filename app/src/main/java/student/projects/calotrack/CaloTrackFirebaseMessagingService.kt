package student.projects.calotrack

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.github.kittinunf.fuel.Fuel

class CaloTrackFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "FCM_SERVICE"
    private val CHANNEL_ID = "calotrack_channel"

    override fun onNewToken(token: String) { //(Firebase, 2019)
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Log.e(TAG, "Cannot save token — user is not logged in")
            return
        }

        val body = mapOf(
            "uid" to uid,
            "token" to token
        )

        val url = "https://us-central1-calotrack-7d9b0.cloudfunctions.net/api/saveToken\n"

        Fuel.post(url)
            .header("Content-Type" to "application/json")
            .body(Gson().toJson(body))
            .response { _, _, result ->
                result.fold(
                    { Log.d(TAG, "Token sent to server!") },
                    { Log.e(TAG, "Error sending token: ${it.message}") }
                )
            }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) { //(Firebase, 2019)
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "From: ${remoteMessage.from}")

        // Handle data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Data payload: ${remoteMessage.data}")
        }


        remoteMessage.notification?.let { notif ->
            val title = notif.title ?: "CaloTrack"
            val body = notif.body ?: "New notification"

            Log.d(TAG, "Notification Body: $body")

            showNotification(title, body)
        }
    }
    private fun showNotification(title: String, body: String?) { //(Firebase, 2019)
        createNotificationChannel()

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
    private fun createNotificationChannel() { //(Firebase, 2019)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "CaloTrack Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "CaloTrack daily reminders and updates"

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
