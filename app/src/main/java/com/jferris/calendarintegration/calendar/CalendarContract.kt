package com.jferris.calendarintegration.calendar

import com.jferris.calendarintegration.BasePresenter
import com.jferris.calendarintegration.BaseView

/**
 * Created by jferris on 06/07/17.
 *
 */
interface CalendarContract {

    interface Presenter: BasePresenter {

    }

    interface View: BaseView<Presenter> {

    }
}