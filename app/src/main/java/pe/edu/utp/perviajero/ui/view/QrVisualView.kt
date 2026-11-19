package pe.edu.utp.perviajero.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * Vista vectorial determinística de código QR para boletos/vouchers de embarque PerúViajero.
 * Genera una matriz 21x21 con 3 patrones localizadores auténticos a partir del código de reserva.
 */
class QrVisualView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var seedCode: String = "PV-2026-0000"
    private val modulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#111815")
        style = Paint.Style.FILL
    }
    private val bracketPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#111815")
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    fun setCode(code: String) {
        if (code.isNotBlank() && code != seedCode) {
            seedCode = code
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = minOf(width, height).toFloat()
        if (size <= 0f) return

        val pad = size * 0.10f
        val inner = size - pad * 2f
        val grid = 21
        val cell = inner / grid

        // Esquinas de encuadre estilo escáner
        val bLen = size * 0.16f
        val stroke = (size * 0.028f).coerceAtLeast(2f)
        bracketPaint.strokeWidth = stroke
        val o = stroke
        // Top-Left
        canvas.drawLine(o, o + bLen, o, o, bracketPaint)
        canvas.drawLine(o, o, o + bLen, o, bracketPaint)
        // Top-Right
        canvas.drawLine(size - o - bLen, o, size - o, o, bracketPaint)
        canvas.drawLine(size - o, o, size - o, o + bLen, bracketPaint)
        // Bottom-Left
        canvas.drawLine(o, size - o - bLen, o, size - o, bracketPaint)
        canvas.drawLine(o, size - o, o + bLen, size - o, bracketPaint)
        // Bottom-Right
        canvas.drawLine(size - o - bLen, size - o, size - o, size - o, bracketPaint)
        canvas.drawLine(size - o, size - o - bLen, size - o, size - o, bracketPaint)

        fun drawFinder(gx: Int, gy: Int) {
            for (r in 0 until 7) {
                for (c in 0 until 7) {
                    val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                    val isCenter = r in 2..4 && c in 2..4
                    if (isBorder || isCenter) {
                        val left = pad + (gx + c) * cell
                        val top = pad + (gy + r) * cell
                        canvas.drawRect(left, top, left + cell + 0.5f, top + cell + 0.5f, modulePaint)
                    }
                }
            }
        }

        drawFinder(0, 0)
        drawFinder(grid - 7, 0)
        drawFinder(0, grid - 7)

        var hash = seedCode.hashCode().toLong() * 31L + 0x5DEECE66DL
        for (r in 0 until grid) {
            for (c in 0 until grid) {
                val inTopLeft = r < 8 && c < 8
                val inTopRight = r < 8 && c >= grid - 8
                val inBottomLeft = r >= grid - 8 && c < 8
                if (inTopLeft || inTopRight || inBottomLeft) continue

                hash = (hash * 6364136223846793005L + (r * 37 + c * 17 + 1442695040888963407L))
                val filled = ((hash ushr 33) and 1L) == 1L
                if (filled) {
                    val left = pad + c * cell
                    val top = pad + r * cell
                    canvas.drawRect(left, top, left + cell + 0.4f, top + cell + 0.4f, modulePaint)
                }
            }
        }
    }
}
