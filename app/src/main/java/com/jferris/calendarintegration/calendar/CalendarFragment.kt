package com.jferris.calendarintegration.calendar

import android.support.v4.app.Fragment

/**
 * Created by jferris on 06/07/17.
 *
 */
class CalendarFragment: Fragment(), CalendarContract.View {
    var mPresenter: CalendarContract.Presenter? = null

    override fun setPresenter(presenter: CalendarContract.Presenter) {
        mPresenter = presenter
    }
}