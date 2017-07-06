package com.jferris.calendarintegration.calendar

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.jferris.calendarintegration.R
import com.jferris.calendarintegration.utils.ActivityUtils

class CalendarActivity : AppCompatActivity() {
    var mPresenter: CalendarContract.Presenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generic)

        var fragment: CalendarFragment? = supportFragmentManager.findFragmentById(R.id.main_layout)
                as? CalendarFragment
        if(fragment == null) {
            fragment = CalendarFragment()
            ActivityUtils.addFragmentToActivity(
                    supportFragmentManager, fragment, R.id.main_layout)
        }

        mPresenter = CalendarPresenter(fragment)
    }

}
