package pe.edu.utp.perviajero.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import pe.edu.utp.perviajero.R

/**
 * Fondo de cabecera para el Menú Lateral con curva superior derecha envolvente hacia la cámara/notch.
 */
class DrawerHeaderBgView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var primary = ContextCompat.getColor(context, R.color.colorPrimary)
    private var primaryDark = ContextCompat.getColor(context, R.color.colorPrimaryDark)
    private val density = resources.displayMetrics.density

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val path = Path()
    private var topInsetPx: Int = 0

    fun setPalette(startColor: Int, endColor: Int) {
        if (primary != startColor || primaryDark != endColor) {
            primary = startColor
            primaryDark = endColor
            if (width > 0 && height > 0) {
                paint.shader = LinearGradient(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    primary, primaryDark,
                    Shader.TileMode.CLAMP
                )
            }
            invalidate()
        }
    }

    fun setTopInsetPx(inset: Int) {
        if (topInsetPx != inset) {
            topInsetPx = inset
            invalidate()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        paint.shader = LinearGradient(
            0f, 0f, w.toFloat(), h.toFloat(),
            primary, primaryDark,
            Shader.TileMode.CLAMP
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        // Curva orgánica en la esquina superior derecha envolviendo la zona de la cámara frontal
        val notchDepth = (topInsetPx.coerceAtLeast((28 * density).toInt())).toFloat()
        val curveStartX = w * 0.72f
        val curveMidX = w * 0.86f
        val rightRadius = 24f * density

        path.reset()
        path.moveTo(0f, 0f)
        path.lineTo(curveStartX, 0f)
        path.cubicTo(
            curveMidX, 0f,
            curveMidX - 6f * density, notchDepth,
            w - rightRadius, notchDepth
        )
        path.quadTo(w, notchDepth, w, notchDepth + rightRadius)
        path.lineTo(w, h)
        path.lineTo(0f, h)
        path.close()

        canvas.drawPath(path, paint)
    }
}
