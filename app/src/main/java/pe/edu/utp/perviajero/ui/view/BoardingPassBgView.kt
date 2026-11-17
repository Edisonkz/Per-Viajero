package pe.edu.utp.perviajero.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import pe.edu.utp.perviajero.R

/**
 * Fondo vectorial para tarjetas tipo Boarding Pass con muescas laterales semicirculares y línea punteada.
 */
class BoardingPassBgView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val cardColor = ContextCompat.getColor(context, R.color.colorCardBackground)
    private val density = resources.displayMetrics.density

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = cardColor
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f * density
        color = Color.argb(30, 0, 0, 0)
    }

    private val dashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.4f * density
        color = Color.argb(65, 108, 117, 125)
        pathEffect = DashPathEffect(floatArrayOf(6f * density, 5f * density), 0f)
    }

    private val ticketPath = Path()
    private val rect = RectF()
    private var notchRatioFromBottom = 0.29f

    fun setTearLineBottomOffsetRatio(ratio: Float) {
        notchRatioFromBottom = ratio.coerceIn(0.18f, 0.45f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val inset = 1.5f * density
        val corner = 16f * density
        val notchRadius = 10f * density
        val notchY = (h * (1f - notchRatioFromBottom)).coerceIn(corner + notchRadius, h - corner - notchRadius)

        ticketPath.reset()
        ticketPath.moveTo(inset + corner, inset)
        ticketPath.lineTo(w - inset - corner, inset)
        rect.set(w - inset - 2 * corner, inset, w - inset, inset + 2 * corner)
        ticketPath.arcTo(rect, -90f, 90f, false)

        ticketPath.lineTo(w - inset, notchY - notchRadius)
        rect.set(w - inset - notchRadius, notchY - notchRadius, w - inset + notchRadius, notchY + notchRadius)
        ticketPath.arcTo(rect, -90f, -180f, false)

        ticketPath.lineTo(w - inset, h - inset - corner)
        rect.set(w - inset - 2 * corner, h - inset - 2 * corner, w - inset, h - inset)
        ticketPath.arcTo(rect, 0f, 90f, false)

        ticketPath.lineTo(inset + corner, h - inset)
        rect.set(inset, h - inset - 2 * corner, inset + 2 * corner, h - inset)
        ticketPath.arcTo(rect, 90f, 90f, false)

        ticketPath.lineTo(inset, notchY + notchRadius)
        rect.set(inset - notchRadius, notchY - notchRadius, inset + notchRadius, notchY + notchRadius)
        ticketPath.arcTo(rect, 90f, -180f, false)

        ticketPath.lineTo(inset, inset + corner)
        rect.set(inset, inset, inset + 2 * corner, inset + 2 * corner)
        ticketPath.arcTo(rect, 180f, 90f, false)
        ticketPath.close()

        canvas.drawPath(ticketPath, fillPaint)
        canvas.drawPath(ticketPath, strokePaint)
        canvas.drawLine(
            inset + notchRadius + 6f * density,
            notchY,
            w - inset - notchRadius - 6f * density,
            notchY,
            dashPaint
        )
    }
}
