package com.jferris.calendarintegration.calendar

import android.os.AsyncTask
import android.support.v4.app.FragmentActivity
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import java.util.*

/**
 * Created by jferris on 12/07/17.
 *
 */
class AddNewCalendarEvent internal constructor(credential: GoogleAccountCredential, activity: FragmentActivity): AsyncTask<Void, Void, String>() {
    private var service: com.google.api.services.calendar.Calendar? = null
    private var mLastError: Exception? = null
    val mActivity = activity

    init {
        val transport = AndroidHttp.newCompatibleTransport()
        val jsonFactory = JacksonFactory.getDefaultInstance()
        service = com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Google Calendar API Android Quickstart")
                .build()
    }

    override fun doInBackground(vararg p0: Void?): String {

        try {
            val event = Event()
                    .setSummary("New summary")
                    .setLocation("New Location")
                    .setDescription("New Description")

            val startDateTime = DateTime(Date())
            val start = EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone("America/Los_Angeles")
            event.setStart(start)

            val endDateTime = DateTime(Date())
            val endDate = DateTime(Date())
            val end = EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone("America/Los_Angeles")
            event.setEnd(end)

            val calendarID = "primary"
            service!!.events().insert(calendarID, event).execute()
        } catch (e: UserRecoverableAuthIOException) {
            mActivity.startActivity(e.intent)
        }

        return "You are at postexecute"
    }

}