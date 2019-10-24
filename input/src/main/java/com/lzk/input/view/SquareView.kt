package com.lzk.inputstyle.view

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.lzk.input.R
import com.lzk.inputstyle.inner.BaseView

class SquareView : TextView, BaseView {
    constructor(context: Context) : super(context) {
        setBackgroundResource(R.drawable.input_normal_border)
        gravity = Gravity.CENTER
    }

    override fun view(): View {
        return this
    }

    override fun onTextChange(input: String?) {
        text = input?.intern() ?: ""
    }
}