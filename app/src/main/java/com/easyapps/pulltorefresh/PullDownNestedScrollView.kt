package com.easyapps.pulltorefresh

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.widget.NestedScrollView
import kotlin.math.max
import kotlin.math.min

class PullDownNestedScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    private var pullDownOffset = 0f
    private var lastMotionY = 0f
    private var isDragging = false
    private var maxPullHeight = 300f // Максимальная высота смещения
    private var onRefreshListener: OnRefreshListener? = null
    private var isRefreshing = false // Флаг для отслеживания состояния обновления
    private var textAlpha = 0f // Прозрачность текста

    private val paint = Paint().apply {
        color = Color.BLACK // Цвет текста
        textSize = 40f // Размер текста
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    init {
        overScrollMode = View.OVER_SCROLL_NEVER // Отключаем стандартное overscroll поведение
    }

    fun setOnRefreshListener(listener: OnRefreshListener) {
        this.onRefreshListener = listener
    }

    fun setRefreshing(refreshing: Boolean) {
        isRefreshing = refreshing
        if (refreshing) {
            // Устанавливаем `pullDownOffset` на заданную высоту и обновляем
            pullDownOffset = maxPullHeight - 50f
            textAlpha = 1f // Устанавливаем видимость текста
            invalidate()
        } else {
            // Возвращаем `pullDownOffset` и прозрачность текста в исходное положение
            val animator = ValueAnimator.ofFloat(pullDownOffset, 0f)
            animator.duration = 300
            animator.addUpdateListener { animation ->
                pullDownOffset = animation.animatedValue as Float
                textAlpha = 0f // Скрываем текст
                invalidate()
            }
            animator.start()
        }
    }

    fun finishRefreshing() {
        setRefreshing(false)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (isRefreshing) {
            // Блокируем скролл, пока идет обновление
            return super.onTouchEvent(ev)
        }

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                lastMotionY = ev.y
                isDragging = true
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaY = ev.y - lastMotionY
                lastMotionY = ev.y

                if (scrollY <= 0 && deltaY > 0) {
                    // Перетягиваем вниз, ограничивая по maxPullHeight
                    pullDownOffset = min(pullDownOffset + deltaY / 2, maxPullHeight)
                    invalidate()

                    // Устанавливаем `textAlpha` на основе `pullDownOffset`
                    textAlpha = if (pullDownOffset >= 50f) min((pullDownOffset - 50f) / (maxPullHeight - 50f), 1f) else 0f

                    // Проверяем, достигли ли мы триггерного значения для обновления
                    if (pullDownOffset >= maxPullHeight - 50f && !isRefreshing) {
                        isRefreshing = true // Устанавливаем флаг, чтобы не вызывать повторно
                        onRefreshListener?.onRefresh() // Вызываем метод обновления
                    }
                    return true
                } else if (deltaY < 0 && pullDownOffset > 0) {
                    // Сбрасываем pullDownOffset, если тянем вверх и достигли верхней границы
                    pullDownOffset = max(pullDownOffset + deltaY / 2, 0f)
                    invalidate()
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                if (pullDownOffset > 0 && !isRefreshing) {
                    // Плавное возвращение в исходное положение, если не идет обновление
                    val animator = ValueAnimator.ofFloat(pullDownOffset, 0f)
                    animator.duration = 300
                    animator.addUpdateListener { animation ->
                        pullDownOffset = animation.animatedValue as Float
                        invalidate()
                    }
                    animator.start()
                }
            }
        }
        return super.onTouchEvent(ev)
    }

    override fun dispatchDraw(canvas: Canvas) {
        // Смещаем канву на значение pullDownOffset, чтобы создать эффект перетягивания
        canvas.save()
        canvas.translate(0f, pullDownOffset)
        super.dispatchDraw(canvas)
        canvas.restore()

        // Отрисовываем текст "Загружается..." с плавной прозрачностью
        if (pullDownOffset > 0) {
            val x = width / 2f
            val y = pullDownOffset / 2 // Позиция текста на половине смещения
            paint.alpha = (textAlpha * 255).toInt() // Применяем прозрачность к тексту
            canvas.drawText("Загружается...", x, y, paint)
        }
    }

    interface OnRefreshListener {
        fun onRefresh()
    }
}
