package com.upriseus.remindme.layout_features

import android.provider.Settings.Global.getString
import android.text.format.DateFormat
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.upriseus.remindme.R
import com.upriseus.remindme.features.reminders.Reminders
import java.text.DateFormatSymbols
import java.util.*


class ReminderAdapter(private val reminders : MutableList<Reminders>) : BaseAdapter() {

    private var selectedIds = SparseBooleanArray()
    private var displayedReminders = mutableListOf<Reminders>()

    override fun getCount(): Int {
        return displayedReminders.size
    }

    override fun getItem(position: Int): Reminders {
        return displayedReminders[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong();
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val convView : View?
        val viewHolder : RowHolder
        val cal = Calendar.getInstance()

        if (convertView == null) {
            convView = LayoutInflater.from(parent.context).inflate(R.layout.item_reminder, parent, false);
            viewHolder = RowHolder(convView)
            convView.tag = viewHolder
        }else{
            convView = convertView
            viewHolder = convView.tag as RowHolder
        }
        val reminder : Reminders= getItem(position)
        cal.timeInMillis = reminder.reminderTime
        viewHolder.title.text = reminder.message
        if(reminder.recurring == true){
            viewHolder.time.text = convView?.resources?.getString(R.string.recurring_date)?.let { String.format(it, DateFormatSymbols().weekdays[reminder.dayOfWeek!!]) }
        }else{
            viewHolder.time.text = DateFormat.format("HH:mm", cal).toString()
            viewHolder.date.text = DateFormat.format("dd/MM/yyyy", cal).toString()
        }


        return convView;
    }

    private fun dispAllReminders(){
        displayedReminders = mutableListOf()
        displayedReminders.addAll(reminders)
        notifyDataSetChanged()
    }

    private fun dispDueReminders(){
        displayedReminders = mutableListOf()
        displayedReminders.addAll(reminders.filter {
            Calendar.getInstance().timeInMillis >= it.reminderTime
        })
        notifyDataSetChanged()
    }

    fun notifyDataChanged(selectedTab: Int){
        when(selectedTab){
            0 -> dispDueReminders()
            1 -> dispAllReminders()
        }
    }

    fun remove(reminder: Reminders){
        displayedReminders.remove(reminder)
    }

    fun removeSelection(selectedTab: Int){
        selectedIds = SparseBooleanArray()
        notifyDataChanged(selectedTab)
    }

    fun getSelectedIds() : SparseBooleanArray{
        return selectedIds
    }

    fun toggleSelection(position : Int) {
        val notAlreadySelected = !selectedIds.get(position)
        if(notAlreadySelected){
            selectedIds.put(position, notAlreadySelected)
        }else{
            selectedIds.delete(position)
            notifyDataSetChanged()
        }
    }

    inner class RowHolder(listItemView: View){
        val title: TextView = listItemView.findViewById(R.id.title_reminder)
        val time: TextView = listItemView.findViewById(R.id.description_time)
        val date: TextView = listItemView.findViewById(R.id.description_date)
    }
}