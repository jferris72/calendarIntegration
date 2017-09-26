package com.jferris.calendarintegration.calendar

import android.content.Context
import android.support.v4.widget.NestedScrollView
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView

/**
 * Created by jferris on 11/07/17.
 *
 */
class CalendarListScroller: NestedScrollView {
    var mScrollable: Boolean = true


    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}


    fun setScrollingEnabled(enabled: Boolean) {
        mScrollable = enabled
    }

    fun isScrollable(): Boolean = mScrollable

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if(ev.action == MotionEvent.ACTION_DOWN) {
            if(mScrollable) return super.onTouchEvent(ev)
            return mScrollable
        } else {
            return super.onTouchEvent(ev)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if(!mScrollable) return false
        else return super.onInterceptTouchEvent(ev)
    }
}