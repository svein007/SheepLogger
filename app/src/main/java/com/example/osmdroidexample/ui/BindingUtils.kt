package com.example.osmdroidexample.ui

import android.widget.EditText
import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.SimpleDateFormat
import java.util.*

val formatter = SimpleDateFormat("HH:mm")

@BindingAdapter("textLong")
fun TextView.setTextFromLong(value: Long?) {
    text = value?.toString() ?: ""
}

@BindingAdapter("textInt")
fun EditText.setTextFromInt(value: Int?) {
    setText(value?.toString() ?: "")
}

@BindingAdapter("textDateTime")
fun TextView.setTextTimeFromDate(date: Date) {
    text = formatter.format(date)
}