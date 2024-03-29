package com.test.push

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushService : FirebaseMessagingService() {

    private val TAG = "firebaseService"

    override fun onNewToken(token: String) {
        Log.d(TAG, "refreshed token: $token")

        // 토큰 값 따로 저장
        /*
        val pref = this.getSharedPreferences("token", Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString("token",token).apply()
        editor.commit()
*/
        sendRegistrationToServer(token)
        Log.i("로그", "토큰 저장 성공적")
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        Log.d(TAG, "From: ${remoteMessage.from}")

        if(remoteMessage.data.isNotEmpty()){
            Log.i("바디", remoteMessage.data["body"].toString())
            Log.i("타이틀",remoteMessage.data["title"].toString())
            sendNotification(remoteMessage)
        }
        else {
            Log.i("수신에러 : ", "data가 비어있습니다. 메시지를 수신하지 못했습니다.")
            Log.i("data값 :",remoteMessage.data.toString())
        }

    }

    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }

    // 알림 생성 (아이콘, 알림 소리 등)
    @SuppressLint("UnspecifiedImmutableFlag")
    private fun sendNotification(remoteMessage: RemoteMessage){
        // RemoteCode, ID를 고유값으로 지정하여 알림이 개별 표시 되도록 함
        val uniId: Int = (System.currentTimeMillis() / 7).toInt()

        // 일회용 PendingIntent
        // PendingIntent : Intent 의 실행 권한을 외부의 어플리케이션에게 위임
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // Activity Stack을 경로만 남김, A-B-C-D-B => A-B
        val pendingIntent = PendingIntent.getActivity(this, uniId, intent, PendingIntent.FLAG_ONE_SHOT)

        // 알림 채널 이름
        val channelId = getString(R.string.app_name)

        // 알림 소리
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // 알림에 대한 UI 정보와 작업을 지정
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)     // 아이콘 설정
            .setContentTitle(remoteMessage.data["body"].toString())     // 제목
            .setContentText(remoteMessage.data["title"].toString())     // 메시지 내용
            .setAutoCancel(true)
            .setSound(soundUri)     // 알림 소리
            .setContentIntent(pendingIntent)       // 알림 실행 시 Intent

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 오레오 버전 이후에는 채널이 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(channelId, "Notice", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // 알림 생성
        notificationManager.notify(uniId, notificationBuilder.build())

    }


}