package pe.edu.utp.perviajero.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import pe.edu.utp.perviajero.R

/**
 * Fondo vectorial responsivo para la barra inferior de 5 menús con arco central elevado en media luna.
 * Soporta relleno inferior dinámico para barras de navegación del sistema (Edge-to-Edge).
 */
class CurvedBottomBarBgView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val bgColor = ContextCompat.getColor(context, R.color.colorCardBackground)
    private val density = resources.displayMetrics.density

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = bgColor
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.2f * density
        color = Color.argb(35, 0, 0, 0)
    }

    private val path = Path()
    private val borderPath = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val archRise = 18f * density
        val topY = archRise
        val centerX = w / 2f
        val archHalfWidth = 44f * density
        val archPeakY = 2f * density

        path.reset()
        path.moveTo(0f, topY)
        path.lineTo(centerX - archHalfWidth, topY)
        path.cubicTo(
            centerX - archHalfWidth * 0.55f, topY,
            centerX - archHalfWidth * 0.45f, archPeakY,
            centerX, archPeakY
        )
        path.cubicTo(
            centerX + archHalfWidth * 0.45f, archPeakY,
            centerX + archHalfWidth * 0.55f, topY,
            centerX + archHalfWidth, topY
        )
        path.lineTo(w, topY)
        path.lineTo(w, h)
        path.lineTo(0f, h)
        path.close()

        borderPath.reset()
        borderPath.moveTo(0f, topY)
        borderPath.lineTo(centerX - archHalfWidth, topY)
        borderPath.cubicTo(
            centerX - archHalfWidth * 0.55f, topY,
            centerX - archHalfWidth * 0.45f, archPeakY,
            centerX, archPeakY
        )
        borderPath.cubicTo(
            centerX + archHalfWidth * 0.45f, archPeakY,
            centerX + archHalfWidth * 0.55f, topY,
            centerX + archHalfWidth, topY
        )
        borderPath.lineTo(w, topY)

        canvas.drawPath(path, fillPaint)
        canvas.drawPath(borderPath, borderPaint)
    }
}
