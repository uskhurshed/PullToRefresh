package com.easyapps.pulltorefresh

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import androidx.core.widget.NestedScrollView
import kotlin.math.min

class CustomRefreshScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    private var isRefreshing = false
    private var refreshListener: (() -> Unit)? = null
    private var headerView: View? = null
    private val overshootInterpolator = OvershootInterpolator(2f) // Высокое значение для "bounce"
    private var initialY = 0f
    private var isDragging = false
    private var headerOriginalHeight = 0

    init {
        overScrollMode = OVER_SCROLL_NEVER
    }

    fun setHeaderView(header: View) {
        val container = getChildAt(0) as? LinearLayout
        container?.let {
            if (headerView == null) {
                headerView = header
                header.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
                headerOriginalHeight = header.measuredHeight
                val layoutParams = header.layoutParams
                layoutParams.height = 0
                header.layoutParams = layoutParams
                it.addView(headerView, 0)
            }
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                initialY = ev.y
                isDragging = true
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaY = ev.y - initialY
                if (scrollY == 0 && deltaY > 0 && !isRefreshing) {
                    headerView?.let {
                        val newHeight = min(deltaY.toInt() / 2, headerOriginalHeight * 2) // Ограничиваем растяжение
                        it.layoutParams.height = newHeight
                        it.requestLayout()
                    }
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                headerView?.let {
                    if (it.layoutParams.height >= headerOriginalHeight) triggerRefresh()
                    else resetHeaderPosition()
                }
                isDragging = false
            }
        }
        return super.onTouchEvent(ev)
    }

    private fun triggerRefresh() {
        isRefreshing = true
        refreshListener?.invoke()

        headerView?.let { header ->
            // Устанавливаем полную высоту header и запускаем анимацию
            header.layoutParams.height = headerOriginalHeight
            header.alpha = 0f // Начинаем с прозрачного
            header.animate()
                .alpha(1f) // Плавно увеличиваем альфа
                .setDuration(300)
                .setInterpolator(overshootInterpolator)
                .start()
        }
    }

    fun finishRefreshing() {
        isRefreshing = false
        resetHeaderPosition()
    }

    private fun resetHeaderPosition() {
        headerView?.let { header ->
            val currentHeight = header.layoutParams.height
            val animator = ValueAnimator.ofInt(currentHeight, 0)
            animator.duration = 500
            animator.interpolator = overshootInterpolator // Bounce эффект

            animator.addUpdateListener { animation ->
                val animatedValue = (animation.animatedValue as Int).coerceAtLeast(0)
                val layoutParams = header.layoutParams
                layoutParams.height = animatedValue
                header.layoutParams = layoutParams
            }

            animator.start()
        }
    }

    fun setOnRefreshListener(listener: () -> Unit) {
        refreshListener = listener
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (isRefreshing) return false

        return super.onInterceptTouchEvent(ev)
    }
}
