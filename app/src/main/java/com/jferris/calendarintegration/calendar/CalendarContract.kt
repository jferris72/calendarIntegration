package com.jferris.calendarintegration.calendar

import com.google.api.services.calendar.model.Event
import com.jferris.calendarintegration.BasePresenter
import com.jferris.calendarintegration.BaseView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.util.*

/**
 * Created by jferris on 06/07/17.
 *
 */
interface CalendarContract {

    interface Presenter: BasePresenter {
        fun onMonthDatePressed(date: CalendarDay, allEvents: ArrayList<Event>)
        fun onWeekDatePressed(date: CalendarDay, allEvents: ArrayList<Event>)
        fun setHash(hash: HashSet<CalendarDay>)
        fun calculateRecyclerHeight(calendarViewWeek: MaterialCalendarView)
        var eventList: ArrayList<Event>
    }

    interface View: BaseView<Presenter> {
        fun setMonthVisibility(visibility: Int)
        fun setWeekVisibility(visibility: Int)
        fun setMonthAlpha(alpha: Float)
        fun setWeekAlpha(alpha: Float)
        fun setMonthDate(date: CalendarDay)
        fun setWeekDate(date: CalendarDay)
        fun setMonthSelectedDate(date: CalendarDay)
        fun setWeekSelectedDate(date: CalendarDay)
        fun scrollTo(x: Int, y: Int)
        fun updateData()
        fun setRecyclerViewHeight(height: Int)
    }
}