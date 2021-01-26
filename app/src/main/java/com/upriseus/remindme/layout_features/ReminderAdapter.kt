package com.upriseus.remindme.layout_features

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.upriseus.remindme.R
import com.upriseus.remindme.features.Reminders


class ReminderAdapter(private val reminders : List<Reminders>) : BaseAdapter() {

    override fun getCount(): Int {
        return reminders.size
    }

    override fun getItem(position: Int): Reminders {
        return reminders[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong();
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val convView : View?
        val viewHolder : RowHolder

        if (convertView == null) {
            convView = LayoutInflater.from(parent.context).inflate(R.layout.item_reminder, parent, false);
            viewHolder = RowHolder(convView)
            convView.tag = viewHolder
        }else{
            convView = convertView
            viewHolder = convView.tag as RowHolder
        }

        viewHolder.title.text = getItem(position).title
        viewHolder.description.text = getItem(position).description

        return convView;
    }

    inner class RowHolder(listItemView: View){
        val title = listItemView.findViewById<TextView>(R.id.title_reminder)
        val description = listItemView.findViewById<TextView>(R.id.description_reminder)
    }
}