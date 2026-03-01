package com.oplus.systemui.qs.base.widget

import com.android.systemui.plugins.qs.QSTile
import java.util.concurrent.atomic.AtomicReference

abstract class QsViewBackgroundProxy(val qsView: QsViewInfoProvider?) : QsViewBackground {
    val backgroundReference: AtomicReference<*>
    var mBgOutlineProvider: QsViewOutlineProvider? = null


    init {
        this.backgroundReference = AtomicReference<Any?>()
    }

    fun getQsTileViewBackgroundProxy(qsTileViewInfoProvider: QsTileViewInfoProvider?): QsViewBackgroundProxy {
        throw UnsupportedOperationException("Stub!")
    }


    fun ensureBgOutlineProvider(): QsViewOutlineProvider? {
        throw UnsupportedOperationException("Stub!")
    }

    abstract val targetQsViewBackground: QsViewBackground?

    // com.oplus.systemui.p127qs.base.widget.QsViewBackground
    override fun onBackgroundAttach() {
        switchBackgroundTo(this.targetQsViewBackground)
    }

    // com.oplus.systemui.p127qs.base.widget.QsViewBackground
    override fun refreshViewBackground() {
        switchBackgroundTo(this.targetQsViewBackground)!!.refreshViewBackground()
    }

    // com.oplus.systemui.p127qs.base.widget.QsViewBackground
    override fun handleStateChanged(state: QSTile.State?) {
        switchBackgroundTo(this.targetQsViewBackground)!!.handleStateChanged(state)
    }

    @Synchronized
    fun switchBackgroundTo(qsViewBackground: QsViewBackground?): QsViewBackground? {
        throw UnsupportedOperationException("Stub!")
    }

    // com.oplus.systemui.p127qs.base.widget.QsViewBackground
    override fun onBackgroundDetach() {
        throw UnsupportedOperationException("Stub!")
    }

    // com.oplus.systemui.p127qs.base.widget.QsViewBackground
    override fun onClick() {
        throw UnsupportedOperationException("Stub!")
    }


    companion object {
        fun getMediaPanelBackgroundProxy(qsStaticViewInfoProvider: QsStaticViewInfoProvider?): QsViewBackgroundProxy? {
            throw UnsupportedOperationException("Stub!")
        }

        fun getQsTileViewBackgroundProxy(qsTileViewInfoProvider: QsTileViewInfoProvider?): QsViewBackgroundProxy {
            throw UnsupportedOperationException("Stub!")
        }


    }
}
