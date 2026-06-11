package com.example.fluidz

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val eventId = intent.getStringExtra("event_id") ?: return

        if (action == "APPROVE_EVENT") {
            Log.d("NotificationReceiver", "Approving event: $eventId")
            AndroidCalendarManager.approveFluidzEvent(context, eventId)
            // Optionally clear notification here
        }
    }
}
