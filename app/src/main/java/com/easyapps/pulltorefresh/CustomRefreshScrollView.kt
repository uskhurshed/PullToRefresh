package com.easyapps.pulltorefresh

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.widget.NestedScrollView
import kotlin.math.min

class CustomRefreshScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    private var isRefreshing = false
    private var isBouncing = false
    private var refreshListener: (() -> Unit)? = null
    private var headerView: View? = null
    private val overshootInterpolator = OvershootInterpolator()
    private var initialY = 0f
    private var isDragging = false

    init {
        overScrollMode = OVER_SCROLL_NEVER  // Отключаем стандартный overscroll Android
    }

    // Установка header
    fun setHeaderView(header: View) {
        this.headerView = header
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                // Фиксируем начальную точку касания
                initialY = ev.y
                isDragging = true
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaY = ev.y - initialY

                // Проверяем, если тянем вниз и находимся на верхней границе скроллинга
                if (scrollY == 0 && deltaY > 0 && !isRefreshing) {
                    // Реализуем "растягивание" header при тянут вниз
                    headerView?.translationY = min(deltaY / 2, 300f)  // Ограничение на величину оттяжки
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // Если было растягивание и header был вытянут, запускаем refresh
                headerView?.let {
                    if (it.translationY >= 200) {  // Если header вытянут достаточно сильно
                        triggerRefresh()
                    } else {
                        // Возвращаем header в исходное положение
                        resetHeaderPosition()
                    }
                }
                isDragging = false
            }
        }
        return super.onTouchEvent(ev)
    }

    private fun triggerRefresh() {
        isRefreshing = true
        refreshListener?.invoke()

        // Возвращаем header в исходное положение с bounce эффектом
        headerView?.animate()
            ?.translationY(0f)
            ?.setInterpolator(overshootInterpolator)
            ?.setDuration(500)
            ?.start()

        // Имитация завершения обновления через 2 секунды
        postDelayed({
            finishRefreshing()
        }, 2000)
    }

    fun finishRefreshing() {
        isRefreshing = false
        resetHeaderPosition()  // Возвращаем header в исходное положение после обновления
    }

    private fun resetHeaderPosition() {
        headerView?.animate()
            ?.translationY(0f)
            ?.setInterpolator(overshootInterpolator)
            ?.setDuration(300)
            ?.start()
    }

    fun setOnRefreshListener(listener: () -> Unit) {
        refreshListener = listener
    }
}
