package com.fashnix.app.service

import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import com.fashnix.app.MainApplication
import com.fashnix.app.R
import com.fashnix.app.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class FashnixMessagingService : FirebaseMessagingService() {

    @Inject lateinit var firestore: FirebaseFirestore

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                firestore.collection("users").document(userId)
                    .update("fcmToken", token).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Fashnix"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: ""
        val deepLink = remoteMessage.data["deepLink"] ?: "fashnix://home"
        val channelId = remoteMessage.data["channelId"] ?: MainApplication.CHANNEL_DAILY_OUTFIT

        val deepLinkUri = android.net.Uri.parse(deepLink)

        val pendingIntent = NavDeepLinkBuilder(this)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.nav_graph)
            .setDestination(deepLinkUri.lastPathSegment?.let {
                when {
                    deepLink.contains("chat") -> R.id.chatFragment
                    deepLink.contains("laundry") -> R.id.laundryDashboardFragment
                    deepLink.contains("profile") -> R.id.profileFragment
                    else -> R.id.homeFragment
                }
            } ?: R.id.homeFragment)
            .createPendingIntent()

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}