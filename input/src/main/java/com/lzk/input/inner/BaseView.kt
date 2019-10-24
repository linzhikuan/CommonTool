package com.lzk.inputstyle.inner

import android.view.View

interface BaseView {
    fun view(): View
    fun onTextChange(input: String?)
}