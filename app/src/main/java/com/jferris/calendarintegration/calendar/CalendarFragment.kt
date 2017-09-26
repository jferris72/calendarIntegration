package com.jferris.calendarintegration.calendar

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.jferris.calendarintegration.R
import com.jferris.calendarintegration.adapter.EventAdapter
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.android.synthetic.main.fragment_scroll.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.IOException
import java.util.*

/**
 * Created by jferris on 06/07/17.
 *
 */
class CalendarFragment: Fragment(), CalendarContract.View, EasyPermissions.PermissionCallbacks {
    var mPresenter: CalendarContract.Presenter? = null
    private val SCOPES = arrayOf(CalendarScopes.CALENDAR)
    private var mCredential: GoogleAccountCredential? = null
    val hash: HashSet<CalendarDay> = HashSet()
    val allEvents: ArrayList<Event> = ArrayList()
    var decorator: CalendarDecorator? = null
    private var adapter: EventAdapter? = null

    private val PREF_ACCOUNT_NAME = "accountName"

    private val REQUEST_ACCOUNT_PICKER = 1000
    private val REQUEST_AUTHORIZATION = 1001
    private val REQUEST_GOOGLE_PLAY_SERVICES = 1002
    private val REQUEST_PERMISSION_GET_ACCOUNTS = 1003


    override fun setPresenter(presenter: CalendarContract.Presenter) {
        mPresenter = presenter
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater?.inflate(R.layout.fragment_scroll, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {

        mCredential = GoogleAccountCredential.usingOAuth2(
                activity.applicationContext, Arrays.asList<String>(*SCOPES))
                .setBackOff(ExponentialBackOff())

        val recyclerView = event_list
        adapter = EventAdapter(mPresenter!!.eventList)
        recyclerView.adapter = adapter

        val layoutManager = object : LinearLayoutManager(activity) {
            override fun canScrollVertically(): Boolean = false
            override fun canScrollHorizontally(): Boolean = false
        }
        recyclerView.layoutManager = layoutManager

        scroll_view.isNestedScrollingEnabled = true
        recyclerView.isNestedScrollingEnabled = false

        calendarView.setOnDateChangedListener( { _, date, _ ->
            run {
                mPresenter?.onMonthDatePressed(date, allEvents)
            }
        })

        scroll_view.viewTreeObserver.addOnScrollChangedListener {
            val scrollY = scroll_view.scrollY

            if(scrollY < 100) {
                calendarView.alpha = 1f
                calendarView.visibility = View.VISIBLE
                calendarViewWeek.visibility = View.INVISIBLE
            }
            if(scrollY > 300) {
                calendarView.alpha = 0.7f
                calendarView.visibility = View.VISIBLE
                calendarViewWeek.visibility = View.INVISIBLE
            }
            if(scrollY > 500) {
                calendarView.alpha = 0.5f
                calendarView.visibility = View.VISIBLE
                calendarViewWeek.visibility = View.INVISIBLE
            }
            if(scrollY > 700) {
                calendarView.alpha = 0.3f
                calendarView.visibility = View.VISIBLE
                calendarViewWeek.alpha = 0.1f
                calendarViewWeek.visibility = View.VISIBLE
            }
            if(scrollY > 900) {
                calendarView.alpha = 0.1f
                calendarView.visibility = View.VISIBLE
                calendarViewWeek.alpha = 0.5f
                calendarViewWeek.visibility = View.VISIBLE
            }
            if(scrollY > 1000) {
                calendarView.visibility = View.INVISIBLE
                calendarViewWeek.alpha = 1f
//                scroll_view.scrollTo(0,1001)
            }

        }

        getResultsFromApi()
    }

    override fun setRecyclerViewHeight(height: Int) {
        event_list.layoutParams.height = height
        scroll_view.invalidate()
    }

    override fun setMonthDate(date: CalendarDay) {
        calendarView.currentDate = date
    }

    override fun setWeekDate(date: CalendarDay) {
        calendarViewWeek.currentDate = date
    }

    override fun setMonthSelectedDate(date: CalendarDay) {
        calendarView.selectedDate = date
    }

    override fun setWeekSelectedDate(date: CalendarDay) {
        calendarViewWeek.selectedDate = date
    }

    override fun updateData() {
        adapter?.notifyDataSetChanged()
    }

    override fun setMonthVisibility(visibility: Int) {
        calendarView.visibility = visibility
    }

    override fun setWeekVisibility(visibility: Int) {
        calendarViewWeek.visibility = visibility
    }

    override fun setMonthAlpha(alpha: Float) {
        calendarView.alpha = alpha
    }

    override fun setWeekAlpha(alpha: Float) {
        calendarViewWeek.alpha = alpha
    }

    override fun scrollTo(x: Int, y: Int) {
        scroll_view.scrollTo(x, y)
    }

    private fun getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices()
        } else if (mCredential!!.selectedAccountName == null) {
            chooseAccount()
        } else if (!isDeviceOnline()) {
            Toast.makeText(context, "No network connection", Toast.LENGTH_LONG).show()
        } else {
            MakeRequestTask(mCredential!!).execute()
        }
    }

    @AfterPermissionGranted(1003)
    private fun chooseAccount() = if (EasyPermissions.hasPermissions(
            activity, Manifest.permission.GET_ACCOUNTS)) {
        val accountName = activity.getPreferences(Context.MODE_PRIVATE)
                .getString(PREF_ACCOUNT_NAME, null)
        if (accountName != null) {
            mCredential!!.selectedAccountName = accountName
            getResultsFromApi()
        } else {
            // Start a dialog from which the user can choose an account
            startActivityForResult(
                    mCredential!!.newChooseAccountIntent(),
                    REQUEST_ACCOUNT_PICKER)
        }
    } else {
        // Request the GET_ACCOUNTS permission via a user dialog
        EasyPermissions.requestPermissions(
                this,
                "This app needs to access your Google account (via Contacts).",
                REQUEST_PERMISSION_GET_ACCOUNTS,
                Manifest.permission.GET_ACCOUNTS)
    }

    override fun onActivityResult(
            requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(context, "This app requires google play services", Toast.LENGTH_LONG).show()
            } else {
                getResultsFromApi()
            }
            REQUEST_ACCOUNT_PICKER -> if (resultCode == Activity.RESULT_OK && data != null &&
                    data.extras != null) {
                val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (accountName != null) {
                    val settings = activity.getPreferences(Context.MODE_PRIVATE)
                    val editor = settings.edit()
                    editor.putString(PREF_ACCOUNT_NAME, accountName)
                    editor.apply()
                    mCredential!!.selectedAccountName = accountName
                    getResultsFromApi()
                }
            }
            REQUEST_AUTHORIZATION -> if (resultCode == Activity.RESULT_OK) {
                getResultsFromApi()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, list: List<String>) {
        // Do nothing.
    }

    override fun onPermissionsDenied(requestCode: Int, list: List<String>) {
        // Do nothing.
    }

    private fun isDeviceOnline(): Boolean {
        val connMgr = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(activity)
        return connectionStatusCode == ConnectionResult.SUCCESS
    }

    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(activity)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }

    private fun showGooglePlayServicesAvailabilityErrorDialog(
            connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
                activity,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES)
        dialog.show()
    }

    private inner class MakeRequestTask internal constructor(credential: GoogleAccountCredential) : AsyncTask<Void, Void, List<String>>() {
        private var mService: com.google.api.services.calendar.Calendar? = null
        private var mLastError: Exception? = null

        init {
            val transport = AndroidHttp.newCompatibleTransport()
            val jsonFactory = JacksonFactory.getDefaultInstance()
            mService = com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build()
        }

        override fun doInBackground(vararg params: Void): List<String>? {
            try {
                return dataFromApi
            } catch (e: Exception) {
                mLastError = e
                if(e is UserRecoverableAuthIOException) {
                    startActivity(e.intent)
                    return dataFromApi
                }
                return null
            }

        }

        private val dataFromApi: List<String>
            @Throws(IOException::class)
            get() {
                val now = DateTime(System.currentTimeMillis())
                val eventStrings = ArrayList<String>()
                val events = mService!!.events().list("primary")
                        .setMaxResults(10)
                        .setTimeMin(now)
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute()
                val items = events.items

                for (event in items) {
                    allEvents.add(event)
                    var start: DateTime? = event.start.dateTime
                    if (start == null) {
                        start = event.start.date
                    }
                    val date = Date(start!!.value)
                    hash.add(CalendarDay.from(date))

                }
                mPresenter?.setHash(hash)
                return eventStrings
            }


        override fun onPreExecute() {

        }

        override fun onPostExecute(output: List<String>?) {
            decorator = CalendarDecorator(ContextCompat.getColor(context, R.color.colorAccent), hash)
            calendarView.addDecorator(decorator)
            calendarViewWeek.addDecorator(decorator)
        }

        override fun onCancelled() {

        }

    }
}