package it.dhd.oxygencustomizer.ui.views

import android.view.View

class ClockCarouselItemViewModel(
    val clockName: String,
    val clockLayout: Int,
    val isSelected: Boolean,
    val contentDescription: String,
    val view: View
)
