package com.example.sheeptracker.ui

import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

val timeFormatter = SimpleDateFormat("HH:mm")
val dateFormatter = SimpleDateFormat("dd/MM/yyyy")
val dateTimeFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm")

@BindingAdapter("textLong")
fun TextView.setTextFromLong(value: Long?) {
    text = value?.toString() ?: ""
}

@BindingAdapter("textInt")
fun TextView.setTextFromInt(value: Int?) {
    text = value?.toString() ?: ""
}

@BindingAdapter("textDouble")
fun TextView.setTextFromDouble(value: Double?) {
    text = value?.toString() ?: ""
}

@BindingAdapter("textDoubleRound")
fun TextView.setTextFromDoubleRound(value: Double?) {
    text = value?.roundToInt()?.toString() ?: ""
}

@BindingAdapter("textInt")
fun EditText.setTextFromInt(value: Int?) {
    setText(value?.toString() ?: "")
}

@BindingAdapter("textTime")
fun TextView.setTextTimeFromDate(date: Date?) {
    text = if (date != null) timeFormatter.format(date) else ""
}

@BindingAdapter("textDate")
fun TextView.setTextDateForDate(date: Date?) {
    text = if (date != null) dateFormatter.format(date) else ""
}

@BindingAdapter("textDateTime")
fun TextView.setTextDateTimeForDate(date: Date?) {
    text = if (date != null)  dateTimeFormatter.format(date) else ""
}

@BindingAdapter("boolVisibility")
fun View.setBoolVisibility(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.INVISIBLE
}

@BindingAdapter("boolVisibilityGone")
fun View.setBoolVisibilityGone(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}