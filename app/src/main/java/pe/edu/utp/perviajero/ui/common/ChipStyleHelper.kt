package pe.edu.utp.perviajero.ui.common

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import pe.edu.utp.perviajero.R

/**
 * Helper uniforme para crear y estilizar chips píldora en los filtros de pantalla.
 */
object ChipStyleHelper {

    fun createFilterChip(
        context: Context,
        label: String,
        isCheckedInitial: Boolean,
        id: Int
    ): Chip {
        return Chip(context).apply {
            this.id = id
            text = label
            isCheckable = true
            isChecked = isCheckedInitial
            checkedIcon = null
            shapeAppearanceModel = shapeAppearanceModel.toBuilder().setAllCornerSizes(100f).build()
            applyStyle(context, this, isCheckedInitial)
        }
    }

    fun applyStyle(context: Context, chip: Chip, isSelected: Boolean) {
        val primary = ContextCompat.getColor(context, R.color.colorPrimary)
        val cardBg = ContextCompat.getColor(context, R.color.colorCardBackground)
        val textPrimary = ContextCompat.getColor(context, R.color.colorTextPrimary)
        if (isSelected) {
            chip.chipBackgroundColor = ColorStateList.valueOf(primary)
            chip.setTextColor(Color.WHITE)
            chip.chipStrokeWidth = 0f
        } else {
            chip.chipBackgroundColor = ColorStateList.valueOf(cardBg)
            chip.setTextColor(textPrimary)
            chip.chipStrokeColor = ColorStateList.valueOf(Color.argb(40, 0, 0, 0))
            chip.chipStrokeWidth = 2f
        }
    }
}
