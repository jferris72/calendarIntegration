package com.jferris.calendarintegration.calendar

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.jferris.calendarintegration.R
import com.jferris.calendarintegration.activity.MainActivity
import com.jferris.calendarintegration.adapter.EventAdapter
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import kotlinx.android.synthetic.main.fragment_calendar.*
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
    private var mOutputText: TextView? = null
    private var mCallApiButton: Button? = null
    internal var mProgress: ProgressDialog? = null
    private val SCOPES = arrayOf(CalendarScopes.CALENDAR)
    var mCredential: GoogleAccountCredential? = null
    val hash: HashSet<CalendarDay> = HashSet()
    val dateList: ArrayList<Date> = ArrayList()
    val eventList: ArrayList<Event> = ArrayList()
    val allEvents: ArrayList<Event> = ArrayList()
    var decorator: CalendarDecorator? = null

    private val PREF_ACCOUNT_NAME = "accountName"


    internal val REQUEST_ACCOUNT_PICKER = 1000
    internal val REQUEST_AUTHORIZATION = 1001
    internal val REQUEST_GOOGLE_PLAY_SERVICES = 1002
    internal val REQUEST_PERMISSION_GET_ACCOUNTS = 1003


    override fun setPresenter(presenter: CalendarContract.Presenter) {
        mPresenter = presenter
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater!!.inflate(R.layout.fragment_calendar, container, false)

        return root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {

        mCredential = GoogleAccountCredential.usingOAuth2(
                activity.getApplicationContext(), Arrays.asList<String>(*SCOPES))
                .setBackOff(ExponentialBackOff())


        dateList.add(Date())

        val recyclerView = event_list
        val adapter = EventAdapter(eventList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)

        calendarView.setOnDateChangedListener(OnDateSelectedListener { widget, date, selected ->
            run {
                if (date in hash) {
                    eventList.clear()
                    for(i in allEvents) {
                        var start: DateTime? = i.start.dateTime
                        if (start == null) {
                            start = i.start.date
                        }
                        val tempDate = Calendar.getInstance()
                        tempDate.timeInMillis = start!!.value
                        if(date.day == tempDate.get(Calendar.DAY_OF_MONTH) &&
                                date.month == tempDate.get(Calendar.MONTH) &&
                                date.year == tempDate.get(Calendar.YEAR)) {
                            eventList.add(i)
                        }
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    eventList.clear()
                    adapter.notifyDataSetChanged()
                }
            }
        })

        sync_button.setOnClickListener {
            getResultsFromApi()
        }

        add_button.setOnClickListener {
            AddNewCalendarEvent(mCredential as GoogleAccountCredential, activity).execute()
        }
    }

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
//            try {
//                val temp = service!!.CalendarList().get("primary").execute()
//                val something = temp.summary
//            } catch (e: UserRecoverableAuthIOException) {
//                mActivity.startActivity(e.intent)
//            }

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


    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private fun getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices()
        } else if (mCredential!!.getSelectedAccountName() == null) {
            chooseAccount()
        } else if (!isDeviceOnline()) {
//            mOutputText.setText("No network connection available.")
        } else {
            MakeRequestTask(mCredential!!).execute()
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(1003)
    private fun chooseAccount() {
        if (EasyPermissions.hasPermissions(
                activity, Manifest.permission.GET_ACCOUNTS)) {
            val accountName = activity.getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null)
            if (accountName != null) {
                mCredential!!.setSelectedAccountName(accountName)
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
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * *
     * @param resultCode code indicating the result of the incoming
     * *     activity result.
     * *
     * @param data Intent (containing result data) returned by incoming
     * *     activity result.
     */
    override fun onActivityResult(
            requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != Activity.RESULT_OK) {
//                mOutputText.setText(
//                        "This app requires Google Play Services. Please install " + "Google Play Services on your device and relaunch this app.")
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
                    mCredential!!.setSelectedAccountName(accountName)
                    getResultsFromApi()
                }
            }
            REQUEST_AUTHORIZATION -> if (resultCode == Activity.RESULT_OK) {
                getResultsFromApi()
            }
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     * *     requestPermissions(android.app.Activity, String, int, String[])
     * *
     * @param permissions The requested permissions. Never null.
     * *
     * @param grantResults The grant results for the corresponding permissions
     * *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this)
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     * *         permission
     * *
     * @param list The requested permission list. Never null.
     */
    override fun onPermissionsGranted(requestCode: Int, list: List<String>) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     * *         permission
     * *
     * @param list The requested permission list. Never null.
     */
    override fun onPermissionsDenied(requestCode: Int, list: List<String>) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private fun isDeviceOnline(): Boolean {
        val connMgr = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     * *     date on this device; false otherwise.
     */
    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(activity)
        return connectionStatusCode == ConnectionResult.SUCCESS
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(activity)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     * *     Google Play Services on this device.
     */
    internal fun showGooglePlayServicesAvailabilityErrorDialog(
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

        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        override fun doInBackground(vararg params: Void): List<String>? {
            try {
                return dataFromApi
            } catch (e: Exception) {
                mLastError = e
                cancel(true)
                return null
            }

        }

        /**
         * Fetch a list of the next 10 events from the primary calendar.
         * @return List of Strings describing returned events.
         * *
         * @throws IOException
         */
        private // List the next 10 events from the primary calendar.
                // All-day events don't have start times, so just use
                // the start date.
        val dataFromApi: List<String>
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
//                    eventStrings.add(
//                            String.format("%s (%s)", event.summary, start))
                }
                return eventStrings
            }


        override fun onPreExecute() {
//            mOutputText.setText("")
//            mProgress.show()
        }

        override fun onPostExecute(output: List<String>?) {
            decorator = CalendarDecorator(ContextCompat.getColor(context, R.color.colorAccent), hash)
            calendarView.addDecorator(decorator)
//            mProgress.hide()
//            if (output == null || output.size == 0) {
//                mOutputText.setText("No results returned.")
//            } else {
//                output.add(0, "Data retrieved using the Google Calendar API:")
//                mOutputText.setText(TextUtils.join("\n", output))
//        }
        }

        override fun onCancelled() {
//            mProgress.hide()
//            if (mLastError != null) {
//                if (mLastError is GooglePlayServicesAvailabilityIOException) {
//                    showGooglePlayServicesAvailabilityErrorDialog(
//                            (mLastError as GooglePlayServicesAvailabilityIOException)
//                                    .connectionStatusCode)
//                } else if (mLastError is UserRecoverableAuthIOException) {
//                    startActivityForResult(
//                            (mLastError as UserRecoverableAuthIOException).intent,
//                            MainActivity.REQUEST_AUTHORIZATION)
//                } else {
//                    mOutputText.setText("The following error occurred:\n" + mLastError!!.message)
//                }
//            } else {
//                mOutputText.setText("Request cancelled.")
//            }
        }

    }
}