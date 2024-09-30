package com.easyapps.pulltorefresh

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View.MeasureSpec
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.easyapps.pulltorefresh.databinding.ActivityMainBinding
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    private var initialY = 0f
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.header.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        binding.header.tag = binding.header.measuredHeight
        resetHeaderPosition()
        var lastState = R.id.endScene

        binding.motionBase.setOnTouchListener { _, ev ->
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> initialY = ev.y
                MotionEvent.ACTION_MOVE -> {
                    val deltaY = ev.y - initialY
                    if (!binding.scrollableContent.isSelected && lastState != R.id.startScene) lastState = binding.motionBase.currentState

                    if ( deltaY > 0 && !binding.scrollableContent.isSelected && binding.motionBase.currentState == R.id.startScene && lastState == R.id.startScene) {
                        binding.header.let {
                            val newHeight = min(deltaY.toInt() / 2, binding.header.tag as Int)
                            it.layoutParams.height = newHeight
                            it.requestLayout()
                        }
                        return@setOnTouchListener true
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (binding.header.layoutParams.height >= binding.header.tag as Int && !binding.scrollableContent.isSelected && binding.motionBase.currentState == R.id.startScene && lastState == R.id.startScene) triggerRefresh()
                    else resetHeaderPosition()

                }
            }
            return@setOnTouchListener false
        }


SwipeRefreshLayout(this)
    }

    private fun triggerRefresh() {
        Toast.makeText(this, "refreshing", Toast.LENGTH_SHORT).show()
        binding.scrollableContent.isSelected = true
        finishRefreshing()
    }

    fun finishRefreshing() = Handler(Looper.getMainLooper()).postDelayed({
        binding.scrollableContent.isSelected = false
        resetHeaderPosition()
    }, 2000)


    private fun resetHeaderPosition() {
        binding.header.let { header ->
            val currentHeight = header.layoutParams.height
            val animator = ValueAnimator.ofInt(currentHeight, 0)
            animator.duration = 300
            animator.interpolator = OvershootInterpolator()
            animator.addUpdateListener { animation ->
                val animatedValue = (animation.animatedValue as Int).coerceAtLeast(0)
                val layoutParams = header.layoutParams
                layoutParams.height = animatedValue
                binding.header.layoutParams = layoutParams
            }
            animator.start()
        }
    }

}