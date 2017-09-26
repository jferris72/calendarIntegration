package com.jferris.calendarintegration.calendar

import android.graphics.Point
import android.support.v4.app.Fragment
import android.view.View
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.Event
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.util.*
import kotlin.collections.HashSet

/**
 * Created by jferris on 06/07/17.
 *
 */
class CalendarPresenter(val mView: CalendarContract.View): CalendarContract.Presenter {
    override var eventList: ArrayList<Event> = ArrayList()
    var mHash: HashSet<CalendarDay> = HashSet()

    init {
        checkNotNull(mView)
        mView.setPresenter(this)
    }

    override fun onWeekDatePressed(date: CalendarDay, allEvents: ArrayList<Event>) {
        if (date in mHash) {
            mView.setMonthSelectedDate(date)
            mView.setMonthDate(date)

            eventList.clear()
            for (i in allEvents) {
                var start: DateTime? = i.start.dateTime
                if (start == null) {
                    start = i.start.date
                }
                val tempDate = Calendar.getInstance()
                tempDate.timeInMillis = start!!.value
                if (date.day == tempDate.get(Calendar.DAY_OF_MONTH) &&
                        date.month == tempDate.get(Calendar.MONTH) &&
                        date.year == tempDate.get(Calendar.YEAR)) {
                    eventList.add(i)
                    eventList.add(i)
                    eventList.add(i)
                    eventList.add(i)
                    eventList.add(i)
                    eventList.add(i)
                    eventList.add(i)
                    eventList.add(i)
                    eventList.add(i)
                    eventList.add(i)
                    eventList.add(i)
                    eventList.add(i)
                    eventList.add(i)
                }

                mView.updateData()
//                mView.scrollTo(0, 1001)
//                mView.setMonthVisibility(View.GONE)
//                mView.setWeekVisibility(View.VISIBLE)
                mView.setWeekAlpha(1f)
            }
        } else {
            eventList.clear()
            mView.updateData()
        }
    }

    override fun onMonthDatePressed(date: CalendarDay, allEvents: ArrayList<Event>) {
        if (date in mHash) {
            mView.setWeekSelectedDate(date)
            mView.setWeekDate(date)

            eventList.clear()
            for (i in allEvents) {
                var start: DateTime? = i.start.dateTime
                if (start == null) {
                    start = i.start.date
                }
                val tempDate = Calendar.getInstance()
                tempDate.timeInMillis = start!!.value
                if (date.day == tempDate.get(Calendar.DAY_OF_MONTH) &&
                        date.month == tempDate.get(Calendar.MONTH) &&
                        date.year == tempDate.get(Calendar.YEAR)) {
                    eventList.add(i)
                    eventList.add(i)
                    eventList.add(i)
                    eventList.add(i)
                    eventList.add(i)
                    eventList.add(i)
                    eventList.add(i)
                    eventList.add(i)
                    eventList.add(i)
                    eventList.add(i)
                    eventList.add(i)
                    eventList.add(i)
                    eventList.add(i)
                }

                mView.updateData()
//                mView.scrollTo(0, 1001)
//                mView.setMonthVisibility(View.GONE)
//                mView.setWeekVisibility(View.VISIBLE)
//                mView.setWeekAlpha(1f)
            }
        } else {
            eventList.clear()
            mView.updateData()
        }
    }

    override fun setHash(hash: HashSet<CalendarDay>) {
        this.mHash = hash
    }

    override fun calculateRecyclerHeight(calendarViewWeek: MaterialCalendarView) {
        val display = (mView as Fragment).activity.windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        mView.setRecyclerViewHeight(size.x - calendarViewWeek.height)
    }
}