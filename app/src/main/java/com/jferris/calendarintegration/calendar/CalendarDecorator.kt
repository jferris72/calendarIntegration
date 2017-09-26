package com.jferris.calendarintegration.calendar

import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan

/**
 * Created by jferris on 06/07/17.
 *
 */
class CalendarDecorator(val color: Int, val dates: HashSet<CalendarDay>): DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay?): Boolean = dates.contains(day)

    override fun decorate(view: DayViewFacade?) {
        view!!.addSpan(DotSpan(10f, color))
    }
}