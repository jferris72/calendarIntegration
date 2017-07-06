package com.jferris.calendarintegration

/**
 * Created by jferris on 06/07/17.
 *
 */
interface BaseView<T> {
    fun setPresenter(presenter: T)
}