package com.example.osmdroidexample.utils

import java.text.SimpleDateFormat
import java.util.*

val formatter = SimpleDateFormat("dd/MM/yyyy")

fun getToday(): Date {
    return formatter.parse(formatter.format(Date()))!!
}

fun dateToFormattedString(date: Date): String {
    return formatter.format(date)
}

fun formattedStringToDate(formattedString: String): Date {
    return formatter.parse(formattedString)!!
}