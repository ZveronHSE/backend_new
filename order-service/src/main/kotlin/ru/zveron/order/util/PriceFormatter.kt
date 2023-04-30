package ru.zveron.order.util

object PriceFormatter {
    fun formatToPrice(price: String) = takeIf { price!="0" }?.let { """$price ₽""" } ?: """Договорная"""
}