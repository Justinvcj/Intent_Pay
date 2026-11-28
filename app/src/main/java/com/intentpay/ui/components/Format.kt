package com.intentpay.ui.components

import java.text.NumberFormat
import java.util.Locale

private val inr: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
    minimumFractionDigits = 2
    maximumFractionDigits = 2
}

/** The single money format used across the app, e.g. ₹1,24,860.40. */
fun formatInr(value: Double): String = inr.format(value)
