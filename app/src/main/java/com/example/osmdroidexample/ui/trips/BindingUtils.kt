package com.example.osmdroidexample.ui.trips

import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("textLong")
fun TextView.setTextFromLong(value: Long?) {
    text = value?.toString() ?: ""
}