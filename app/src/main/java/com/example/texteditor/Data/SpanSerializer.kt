package com.example.texteditor.data.util


import android.graphics.Typeface
import android.os.Build
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan

object SpanSerializer {
    fun toHtml(spannable: Spannable): String {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.toHtml(spannable, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL)
        } else {
            @Suppress("Deprecation")
            Html.toHtml(spannable)
        }
    }

    fun fromHtml(text: String): Spannable {
        val spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(text)
        }
        return SpannableString(spanned)
    }

    // Returns true if the current selection already has this span active
    fun hasSpan(spannable: Spannable, start: Int, end: Int, spanClass: Class<*>): Boolean {
        if (start >= end) return false
        return spannable.getSpans(start, end, spanClass).any { span ->
            spannable.getSpanStart(span) <= start && spannable.getSpanEnd(span) >= end
        }
    }

    fun hasBold(spannable: Spannable, start: Int, end: Int): Boolean =
        spannable.getSpans(start, end, StyleSpan::class.java)
            .any { it.style == Typeface.BOLD }

    fun hasItalic(spannable: Spannable, start: Int, end: Int): Boolean =
        spannable.getSpans(start, end, StyleSpan::class.java)
            .any { it.style == Typeface.ITALIC }

    fun hasUnderline(spannable: Spannable, start: Int, end: Int): Boolean =
        hasSpan(spannable, start, end, UnderlineSpan::class.java)

    fun hasStrikethrough(spannable: Spannable, start: Int, end: Int): Boolean =
        hasSpan(spannable, start, end, StrikethroughSpan::class.java)

    fun getForegroundColor(spannable: Spannable, start: Int, end: Int): Int? =
        spannable.getSpans(start, end, ForegroundColorSpan::class.java)
            .firstOrNull()?.foregroundColor

    fun getHighlightColor(spannable: Spannable, start: Int, end: Int): Int? =
        spannable.getSpans(start, end, BackgroundColorSpan::class.java)
            .firstOrNull()?.backgroundColor

}