package com.example.texteditor.data


import android.graphics.Typeface
import android.os.Build
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan

object SpanSerializer {
    fun toHtml(spannable: Spannable): String {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.toHtml(spannable, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
        } else {
            @Suppress("Deprecation")
            Html.toHtml(spannable)
        }
    }

    fun fromHtml(text: String): SpannableStringBuilder {
        val spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(text)
        }
        return SpannableStringBuilder(spanned)
    }

    fun hasBold(spannable: Spannable, start: Int, end: Int): Boolean {
        if (start >= end) return false
        return hasStyleSpan(spannable, start, end, Typeface.BOLD)
    }

    fun hasItalic(spannable: Spannable, start: Int, end: Int): Boolean {
        if (start >= end) return false
        return hasStyleSpan(spannable, start, end, Typeface.ITALIC)
    }

    fun hasUnderline(spannable: Spannable, start: Int, end: Int): Boolean {
        if (start >= end) return false
        return coversRange(spannable, start, end, UnderlineSpan::class.java)
    }

    fun hasStrikethrough(spannable: Spannable, start: Int, end: Int): Boolean {
        if (start >= end) return false
        return coversRange(spannable, start, end, StrikethroughSpan::class.java)
    }

    fun getForegroundColor(spannable: Spannable, start: Int, end: Int): Int? {
        if (start >= end) return null
        return spannable.getSpans(start, end, ForegroundColorSpan::class.java)
            .firstOrNull()?.foregroundColor
    }

    fun getHighlightColor(spannable: Spannable, start: Int, end: Int): Int? {
        if (start >= end) return null
        return spannable.getSpans(start, end, BackgroundColorSpan::class.java)
            .firstOrNull()?.backgroundColor
    }

    // Checks that every character in [start, end) is covered by a StyleSpan of given style
    private fun hasStyleSpan(spannable: Spannable, start: Int, end: Int, style: Int): Boolean {
        val spans = spannable.getSpans(start, end, StyleSpan::class.java)
            .filter { it.style == style }
        return isRangeCovered(spannable, start, end, spans)
    }

    // Checks that every character in [start, end) is covered by at least one span of spanClass
    private fun <T : Any> coversRange(
        spannable: Spannable,
        start: Int,
        end: Int,
        spanClass: Class<T>
    ): Boolean {
        val spans = spannable.getSpans(start, end, spanClass).toList()
        return isRangeCovered(spannable, start, end, spans)
    }

    private fun isRangeCovered(spannable: Spannable, start: Int, end: Int, spans: List<Any>): Boolean {
        if (spans.isEmpty()) return false
        // Build a sorted list of covered intervals and check full coverage
        val intervals = spans.map {
            spannable.getSpanStart(it) to spannable.getSpanEnd(it)
        }.sortedBy { it.first }

        var covered = start
        for ((s, e) in intervals) {
            if (s > covered) return false  // gap found
            if (e > covered) covered = e
            if (covered >= end) return true
        }
        return covered >= end
    }
}