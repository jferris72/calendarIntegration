package com.jferris.calendarintegration.calendar

/**
 * Created by jferris on 06/07/17.
 *
 */
class CalendarPresenter(mView: CalendarContract.View): CalendarContract.Presenter {
    init {
        checkNotNull(mView)
        mView.setPresenter(this)
    }



}