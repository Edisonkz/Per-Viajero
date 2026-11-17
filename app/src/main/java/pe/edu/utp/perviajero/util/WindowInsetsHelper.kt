package pe.edu.utp.perviajero.util

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Helper para aplicar márgenes/paddings seguros ante Edge-to-Edge (Android 15/16+ barra de estado y botones).
 */
object WindowInsetsHelper {

    fun applyEdgePadding(activity: Activity, top: Boolean = true, bottom: Boolean = true) {
        val root = activity.findViewById<ViewGroup>(android.R.id.content).getChildAt(0) ?: return
        applySystemBarPadding(root, top = top, bottom = bottom)
    }

    fun applyToolbarActivityInsets(activity: Activity, toolbar: View) {
        val root = activity.findViewById<ViewGroup>(android.R.id.content).getChildAt(0) ?: return
        val density = activity.resources.displayMetrics.density
        val typedArray = activity.theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
        val attrHeight = typedArray.getDimensionPixelSize(0, (56 * density).toInt())
        typedArray.recycle()

        val lpHeight = toolbar.layoutParams?.height ?: ViewGroup.LayoutParams.WRAP_CONTENT
        val baseToolbarHeight = if (lpHeight > 0) lpHeight else attrHeight

        val tbLeft = toolbar.paddingLeft
        val tbTop = toolbar.paddingTop
        val tbRight = toolbar.paddingRight
        val tbBottom = toolbar.paddingBottom

        val rootLeft = root.paddingLeft
        val rootTop = root.paddingTop
        val rootRight = root.paddingRight
        val rootBottom = root.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->
            val bars = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            toolbar.layoutParams = toolbar.layoutParams?.apply {
                height = baseToolbarHeight + bars.top
            }
            toolbar.setPadding(tbLeft, tbTop + bars.top, tbRight, tbBottom)
            root.setPadding(rootLeft + bars.left, rootTop, rootRight + bars.right, rootBottom + bars.bottom)
            windowInsets
        }
        ViewCompat.requestApplyInsets(root)
    }

    fun applySystemBarPadding(view: View, top: Boolean = true, bottom: Boolean = true) {
        val initialTop = view.paddingTop
        val initialBottom = view.paddingBottom
        val initialLeft = view.paddingLeft
        val initialRight = view.paddingRight

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val bars = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.setPadding(
                initialLeft + bars.left,
                if (top) initialTop + bars.top else initialTop,
                initialRight + bars.right,
                if (bottom) initialBottom + bars.bottom else initialBottom
            )
            windowInsets
        }
        ViewCompat.requestApplyInsets(view)
    }
}
