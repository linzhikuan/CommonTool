package com.lzk.inputstyle

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.view.size
import androidx.core.view.updateLayoutParams
import androidx.core.widget.addTextChangedListener
import com.lzk.inputstyle.inner.BaseView
import com.lzk.inputstyle.view.SquareView
import android.text.InputFilter
import android.text.InputType
import com.lzk.input.R


class InputViewGroup : ViewGroup {
    private var editText: EditText = EditText(context)
    private var childViews: MutableList<BaseView> = ArrayList()
    private var hSpace: Int
    private var onInput: (input: String) -> Unit = {}
    private var onEndInput: (endPuts: String) -> Unit = {}
    private var pwdChar: String? = null
    private var textSize: Float
    private var textColor: ColorStateList? = null


    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.InputViewGroup, defStyleAttr, 0)
        pwdChar = ta.getString(R.styleable.InputViewGroup_passwordTransformation)
        hSpace =
            ta.getDimension(R.styleable.InputViewGroup_hSpace, context.sp2px(15f).toFloat()).toInt()
        textSize = ta.getDimensionPixelSize(R.styleable.InputViewGroup_pwdtextSize, 15).toFloat()
        textSize = context.px2sp(textSize).toFloat()
        textColor = ta.getColorStateList(R.styleable.InputViewGroup_pwdtextColor)
        ta.recycle()
    }

    private fun initEdit() {
        setInputType(InputViewType.NUMBER)
        editText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(childViews.size))
        editText.watchText {
            if (it.length == childViews.size) {
                onEndInput(it)
            } else {
                onInput(it)
            }
            for (index in 0 until childViews.size) {
                if (index >= it.length) {
                    childViews[index].onTextChange(null)
                } else {
                    childViews[index].onTextChange(pwdChar?.intern() ?: it[index].toString())
                }
            }
        }
        addView(editText)
    }

    fun setInputType(inputType: InputViewType) {
        when (inputType) {
            InputViewType.NUMBER -> editText.inputType =
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            InputViewType.TEXT -> editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
    }

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        for (list in childViews) {
            val childView: View = list.view()
            val index = childViews.indexOf(list)
            var left = index * childView.measuredWidth + hSpace * index
            val right = left + childView.measuredWidth
            val top = 0
            val bottom = top + childView.measuredHeight
            childView.layout(left, top, right, bottom)
        }

        editText.layout(-1, -1, 0, 0)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)//获取宽度
        val height = MeasureSpec.getSize(heightMeasureSpec)//获取高度
        val modeW = MeasureSpec.getMode(widthMeasureSpec)//获取宽度测量模式
        val modeH = MeasureSpec.getMode(heightMeasureSpec)//获取高度测量模式
        var resultW: Int
        var resultH: Int
        when (modeH) {//判断宽度模式以演示
            MeasureSpec.UNSPECIFIED, MeasureSpec.AT_MOST -> resultH = context.dp2px(30f)
            else -> {
                resultH = height
            }
        }
        when (modeW) {//判断宽度模式以演示
            MeasureSpec.UNSPECIFIED, MeasureSpec.AT_MOST ->
                resultW = resultH * childViews.size + childViews.size * hSpace
            else -> {
                resultW = width
            }
        }

        var perW = (resultW - ((childViews.size - 1) * hSpace)) / childViews.size

        for (list in childViews) {
            val childView: View = list.view()
            val childW = perW
            val childH = resultH
            val widthSpec = getChildMeasureSpec(
                widthMeasureSpec,
                0
                , childW
            )
            val heightSpec = getChildMeasureSpec(
                heightMeasureSpec,
                0, childH
            )
            childView.measure(widthSpec, heightSpec)
        }
        setMeasuredDimension(resultW, resultH)
    }


    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (hasWindowFocus) {
            postDelayed({ editText.showKeyboard() }, 100)
        }

    }


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        editText.showKeyboard()
        return true
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        showSimple(6)
    }

    fun add(
        childViews: MutableList<BaseView>,
        onInput: (input: String) -> Unit = {},
        onEndInput: (endPuts: String) -> Unit = {}
    ) {
        removeAllViews()
        this.childViews.clear()
        this.onInput = onInput
        this.onEndInput = onEndInput
        this.childViews = childViews
        for (child in childViews) {
            addView(child.view())
        }
        initEdit()
    }

    fun showSimple(
        lenth: Int,
        onInput: (input: String) -> Unit = {},
        onEndInput: (endPuts: String) -> Unit = {}
    ) {
        var childViews: MutableList<BaseView> = ArrayList()
        for (index in 0 until lenth) {
            var child = SquareView(context)
            child.textSize = textSize
            child.setTextColor(textColor?.defaultColor ?: 0xff000000.toInt())
            childViews.add(child)
        }
        add(childViews, onInput, onEndInput)
    }
}


private fun EditText.showKeyboard(): Boolean {
    requestFocus()
    text?.run {
        setSelection(length)
    }
    return (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(
        this,
        InputMethodManager.SHOW_IMPLICIT
    )
}

private fun EditText.watchText(block: (input: String) -> Unit) {
    addTextChangedListener(
        { text, start, count, after ->
        },
        { text, start, count, after ->
            block(text.toString())
        },
        { text ->
        })
}


private fun Context.dp2px(dpValue: Float): Int {
    val scale = resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}

private fun Context.px2dp(pxValue: Float): Int {
    val scale = resources.displayMetrics.density
    return (pxValue / scale + 0.5f).toInt()
}

private fun Context.sp2px(spValue: Float): Int {
    val scale = resources.displayMetrics.scaledDensity
    return (spValue * scale + 0.5f).toInt()
}

private fun Context.px2sp(pxValue: Float): Int {
    val scale = resources.displayMetrics.scaledDensity
    return (pxValue / scale + 0.5f).toInt()
}