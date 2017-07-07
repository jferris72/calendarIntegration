package com.jferris.calendarintegration.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.Event
import com.jferris.calendarintegration.R
import kotlinx.android.synthetic.main.list_item_event.view.*
import java.util.*

/**
 * Created by jferris on 07/07/17.
 *
 */
class EventAdapter(val eventList: ArrayList<Event>): RecyclerView.Adapter<EventAdapter.ViewHolder>() {
    var context: Context? = null

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val dateText = itemView.date_text
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        context = parent!!.context
        val inflater = LayoutInflater.from(context)
        val itemView: View = inflater!!.inflate(R.layout.list_item_event, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        var start: DateTime? = eventList.get(position).start.dateTime
        if (start == null) {
            start = eventList.get(position).start.date
        }
        holder!!.dateText.text = start.toString()
    }
}