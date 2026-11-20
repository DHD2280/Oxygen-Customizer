package it.dhd.oxygencustomizer.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.widget.FrameLayout
import androidx.core.view.WindowCompat
import it.dhd.oxygencustomizer.ui.base.BaseActivity
import it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen.LockscreenClockDesignFragment

class ClockDesignerActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.decorView.setBackgroundColor(Color.BLACK)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        val container = FrameLayout(this).apply { id = CONTAINER_ID }
        setContentView(container)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(CONTAINER_ID, LockscreenClockDesignFragment())
                .commit()
        }
    }

    companion object {
        private const val CONTAINER_ID = 0x0C10C4
    }
}
