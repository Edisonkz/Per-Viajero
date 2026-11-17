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
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Vista personalizada responsiva para la Brújula Turística Offline.
 * Dibuja un dial graduado (0°–360°), rosa de los vientos que apunta al Norte magnético
 * y un indicador secundario direccional hacia el destino seleccionado.
 */
class CompassView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var azimuthDegrees: Float = 0f
    private var targetBearingDegrees: Float? = null

    private val primaryColor = ContextCompat.getColor(context, R.color.colorPrimary)
    private val accentColor = ContextCompat.getColor(context, R.color.colorAccent)
    private val starColor = ContextCompat.getColor(context, R.color.colorStar)
    private val cardBgColor = ContextCompat.getColor(context, R.color.colorCardBackground)
    private val textPrimaryColor = ContextCompat.getColor(context, R.color.colorTextPrimary)
    private val textSecondaryColor = ContextCompat.getColor(context, R.color.colorTextSecondary)

    private val dialBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = cardBgColor
    }

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = primaryColor
    }

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = textSecondaryColor
    }

    private val cardinalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val northNeedlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = accentColor
    }

    private val southNeedlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = textSecondaryColor
        alpha = 140
    }

    private val targetPointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = primaryColor
    }

    private val centerPivotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = starColor
    }

    private val needlePath = Path()

    fun updateOrientation(azimuth: Float, targetBearing: Float? = null) {
        azimuthDegrees = azimuth
        targetBearingDegrees = targetBearing
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val size = if (width > 0 && height > 0) min(width, height) else maxOf(width, height, 480)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(cx, cy) * 0.86f
        if (radius <= 0f) return

        // Fondo y anillo exterior
        canvas.drawCircle(cx, cy, radius, dialBgPaint)
        canvas.drawCircle(cx, cy, radius, ringPaint)

        // Rotamos el dial en sentido contrario al azimut para que 'N' apunte al Norte real
        canvas.save()
        canvas.rotate(-azimuthDegrees, cx, cy)

        drawTicks(canvas, cx, cy, radius)
        drawCardinals(canvas, cx, cy, radius)
        drawNorthSouthNeedle(canvas, cx, cy, radius)

        // Aguja de rumbo hacia el destino turístico (si hay coordenadas)
        targetBearingDegrees?.let { bearing ->
            drawTargetPointer(canvas, cx, cy, radius, bearing)
        }

        canvas.restore()

        // Línea fija superior indicando a dónde apunta el teléfono
        val lubberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = starColor
            strokeWidth = 8f
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(cx, cy - radius - 14f, cx, cy - radius + 16f, lubberPaint)

        // Pivote central
        canvas.drawCircle(cx, cy, radius * 0.055f, centerPivotPaint)
        val innerDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawCircle(cx, cy, radius * 0.022f, innerDotPaint)
    }

    private fun drawTicks(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        for (deg in 0 until 360 step 10) {
            val isMajor = deg % 30 == 0
            tickPaint.strokeWidth = if (isMajor) 4f else 2f
            tickPaint.alpha = if (isMajor) 220 else 110
            val rad = Math.toRadians(deg.toDouble() - 90.0)
            val innerR = radius * if (isMajor) 0.84f else 0.90f
            val outerR = radius * 0.96f
            canvas.drawLine(
                cx + (innerR * cos(rad)).toFloat(),
                cy + (innerR * sin(rad)).toFloat(),
                cx + (outerR * cos(rad)).toFloat(),
                cy + (outerR * sin(rad)).toFloat(),
                tickPaint
            )
        }
    }

    private fun drawCardinals(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val labelRadius = radius * 0.70f
        cardinalPaint.textSize = radius * 0.14f
        val cardinals = listOf(
            "N" to 0f,
            "E" to 90f,
            "S" to 180f,
            "O" to 270f
        )
        for ((label, angle) in cardinals) {
            cardinalPaint.color = if (label == "N") accentColor else textPrimaryColor
            val rad = Math.toRadians(angle.toDouble() - 90.0)
            val x = cx + (labelRadius * cos(rad)).toFloat()
            val y = cy + (labelRadius * sin(rad)).toFloat() + (cardinalPaint.textSize * 0.35f)
            canvas.drawText(label, x, y, cardinalPaint)
        }
    }

    private fun drawNorthSouthNeedle(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val halfWidth = radius * 0.07f
        val tipLen = radius * 0.50f

        // Punta Norte
        needlePath.reset()
        needlePath.moveTo(cx, cy - tipLen)
        needlePath.lineTo(cx - halfWidth, cy)
        needlePath.lineTo(cx + halfWidth, cy)
        needlePath.close()
        canvas.drawPath(needlePath, northNeedlePaint)

        // Punta Sur
        needlePath.reset()
        needlePath.moveTo(cx, cy + tipLen)
        needlePath.lineTo(cx - halfWidth, cy)
        needlePath.lineTo(cx + halfWidth, cy)
        needlePath.close()
        canvas.drawPath(needlePath, southNeedlePaint)
    }

    private fun drawTargetPointer(canvas: Canvas, cx: Float, cy: Float, radius: Float, bearing: Float) {
        canvas.save()
        canvas.rotate(bearing, cx, cy)
        val pointerSize = radius * 0.09f
        val topY = cy - radius * 0.88f

        needlePath.reset()
        needlePath.moveTo(cx, topY)
        needlePath.lineTo(cx - pointerSize, topY + pointerSize * 1.6f)
        needlePath.lineTo(cx + pointerSize, topY + pointerSize * 1.6f)
        needlePath.close()
        canvas.drawPath(needlePath, targetPointerPaint)
        canvas.restore()
    }
}
