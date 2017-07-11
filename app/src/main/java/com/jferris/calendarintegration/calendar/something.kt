package com.jferris.calendarintegration.calendar

import android.view.ViewTreeObserver
import android.widget.ScrollView

/**
 * Created by jferris on 11/07/17.

 */

class something {

    fun methodCall(view: ScrollView) {
        view.viewTreeObserver.addOnScrollChangedListener {
            val scrollY = view.scrollY // For ScrollView
            val scrollX = view.scrollX // For HorizontalScrollView
            // DO SOMETHING WITH THE SCROLL COORDINATES
        }
    }
}
