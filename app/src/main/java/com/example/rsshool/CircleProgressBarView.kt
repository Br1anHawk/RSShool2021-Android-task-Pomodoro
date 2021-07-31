package com.example.rsshool

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.annotation.AttrRes

class CircleProgressBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var periodMs = 0L
    private var currentMs = 0L
    private var color = 0
    private val paint = Paint()

    init {
        if (attrs != null) {
            val styledAttrs = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.CircleProgressBarView,
                defStyleAttr,
                0
            )
            color = styledAttrs.getColor(R.styleable.CircleProgressBarView_colorPrimary, Color.TRANSPARENT)
            styledAttrs.recycle()
        }

        paint.color = color
        paint.style = Paint.Style.FILL
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthView = resources.getDimension(R.dimen.custom_progress_bar_view_width)
        val heightView = resources.getDimension(R.dimen.custom_progress_bar_view_height)
        setMeasuredDimension(widthView.toInt(), heightView.toInt())
    }



    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawTimerClockView(canvas, 0f)
        if (currentMs < 0L) currentMs = 0L
        if (periodMs == 0L || currentMs == periodMs) return
        val startAngel = 360 - (((currentMs % periodMs).toFloat() / periodMs) * 360)
        //Log.d("Custom view - angel", startAngel.toString())
        //Log.d("Custom view - currentMs", currentMs.toString())
        canvas.drawArc(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            -90f,
            startAngel,
            true,
            paint
        )
        drawTimerClockView(canvas, startAngel)
    }

    private fun drawTimerClockView(canvas: Canvas, angleRotation: Float) {
        val centerX = width.toFloat() / 2
        val centerY = height.toFloat() / 2

        val paintTimer = Paint()
        paintTimer.color = Color.BLACK
        paintTimer.style = Paint.Style.STROKE
        paintTimer.strokeWidth = resources.getDimension(R.dimen.custom_progress_bar_view_timer_stroke_width)
        canvas.drawOval(
            RectF(
                paintTimer.strokeWidth,
                paintTimer.strokeWidth,
                width.toFloat() - paintTimer.strokeWidth,
                height.toFloat() - paintTimer.strokeWidth
            ),
            paintTimer
        )

        val paintLineSeparators = Paint()
        paintLineSeparators.color = Color.BLACK
        paintLineSeparators.style = Paint.Style.STROKE
        paintLineSeparators.strokeWidth = resources.getDimension(R.dimen.custom_progress_bar_view_timer_line_separator_stroke_width)
        val lineSeparatorLength = resources.getDimension(R.dimen.custom_progress_bar_view_timer_line_separator_length)
        val pathLineSeparator = Path()
        pathLineSeparator.moveTo(centerX, 0f)
        pathLineSeparator.lineTo(centerX, lineSeparatorLength)
        val matrix = Matrix()
        val angleLinesSeparator = 30f
        for (i in 0..360 / angleLinesSeparator.toInt()) {
            matrix.setRotate(angleLinesSeparator, centerX, centerY)
            pathLineSeparator.transform(matrix)
            canvas.drawPath(pathLineSeparator, paintLineSeparators)
        }

        val paintTimerArrow = Paint()
        paintTimerArrow.color = Color.BLACK
        paintTimerArrow.style = Paint.Style.FILL
        val arrowHalfWidth = resources.getDimension(R.dimen.custom_progress_bar_view_arrow_half_width)
        val pathArrow = Path()
        pathArrow.moveTo(centerX, 0f)
        pathArrow.lineTo(centerX - arrowHalfWidth, centerY)
        pathArrow.lineTo(centerX + arrowHalfWidth, centerY)
        pathArrow.moveTo(centerX - arrowHalfWidth, centerY)
        pathArrow.lineTo(centerX + arrowHalfWidth, centerY)
        //val matrix = Matrix()
        matrix.setRotate(angleRotation, centerX, centerY)
        pathArrow.transform(matrix)
        canvas.drawPath(pathArrow, paintTimerArrow)
        canvas.drawCircle(centerX, centerY, arrowHalfWidth, paintTimerArrow)
        val paintTimerArrowCenterCircle = Paint()
        paintTimerArrowCenterCircle.color = Color.WHITE
        paintTimerArrowCenterCircle.style = Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, arrowHalfWidth / 2, paintTimerArrowCenterCircle)
    }

    /**
     * Set lasted milliseconds
     */
    fun setCurrent(current: Long) {
        currentMs = current
        invalidate()
    }

    /**
     * Set time period
     */
    fun setPeriod(period: Long) {
        periodMs = period
    }
}