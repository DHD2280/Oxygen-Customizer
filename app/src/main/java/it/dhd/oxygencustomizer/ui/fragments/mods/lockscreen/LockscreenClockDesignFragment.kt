package it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen

import android.Manifest
import android.app.Activity
import android.app.WallpaperManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import com.topjohnwu.superuser.Shell
import it.dhd.oneplusui.appcompat.seekbar.OplusSlider
import it.dhd.oneplusui.preference.OplusSliderPreference
import it.dhd.oxygencustomizer.BuildConfig
import it.dhd.oxygencustomizer.R
import it.dhd.oxygencustomizer.databinding.FragmentClockDesignerBinding
import it.dhd.oxygencustomizer.provider.UsageProvider
import it.dhd.oxygencustomizer.ui.adapters.PackageListAdapter
import it.dhd.oxygencustomizer.ui.fragments.mods.WeatherSettings
import it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen.widgets.DeviceWidgetFragment
import it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen.widgets.WidgetSettingsPreferenceFragment
import it.dhd.oxygencustomizer.ui.widgets.CategoryWidget
import it.dhd.oxygencustomizer.ui.widgets.ColorPickerWidget
import it.dhd.oxygencustomizer.ui.widgets.ListWidget
import it.dhd.oxygencustomizer.ui.widgets.SliderWidget
import it.dhd.oxygencustomizer.ui.widgets.SwitchWidget
import it.dhd.oxygencustomizer.utils.AppUtils
import it.dhd.oxygencustomizer.utils.Constants
import it.dhd.oxygencustomizer.utils.Constants.LOCKSCREEN_WEATHER_CUSTOM_FONT
import it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_BACKGROUND
import it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CENTERED
import it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CUSTOM_COLOR
import it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CUSTOM_COLOR_SWITCH
import it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CUSTOM_FONT_SWITCH
import it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_HUMIDITY
import it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_IMAGE_SIZE
import it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_SHOW_CONDITION
import it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_SHOW_LOCATION
import it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_TEXT_SIZE
import it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_WIND
import it.dhd.oxygencustomizer.utils.Constants.Preferences.DepthWallpaper.DEPTH_WALLPAPER_ENABLED
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_TOP_MARGIN
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_BIG_ACTIVE
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_BIG_ICON_ACTIVE
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_BIG_ICON_INACTIVE
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_BIG_INACTIVE
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_CUSTOM_COLOR
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CIRCULAR_COLOR
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_DEVICE
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_LINEAR_COLOR
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_LIQUID_GLASS
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_STYLE
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_TEXT_COLOR
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_SMALL_ACTIVE
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_SMALL_ICON_ACTIVE
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_SMALL_ICON_INACTIVE
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_SMALL_INACTIVE
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_STYLE
import it.dhd.oxygencustomizer.utils.FileUtil
import it.dhd.oxygencustomizer.utils.OCPreferences
import it.dhd.oxygencustomizer.utils.WallpaperUtil
import it.dhd.oxygencustomizer.utils.json.SettingItem
import it.dhd.oxygencustomizer.xmorph.designer.DesignerBundle
import it.dhd.oxygencustomizer.xmorph.designer.DesignerCanvasView
import it.dhd.oxygencustomizer.xmorph.designer.DesignerFonts
import it.dhd.oxygencustomizer.xmorph.designer.DesignerImages
import it.dhd.oxygencustomizer.xmorph.designer.DesignerRepository
import it.dhd.oxygencustomizer.xmorph.designer.DesignerViewModel
import it.dhd.oxygencustomizer.xmorph.designer.EditorUiState
import it.dhd.oxygencustomizer.xmorph.designer.FontTileView
import it.dhd.oxygencustomizer.xmorph.domain.editor.CustomFaces
import it.dhd.oxygencustomizer.xmorph.domain.editor.ElementFactory
import it.dhd.oxygencustomizer.xmorph.domain.editor.FaceComposer
import it.dhd.oxygencustomizer.xmorph.domain.editor.ItemGrouping
import it.dhd.oxygencustomizer.xmorph.domain.editor.ItemGroups
import it.dhd.oxygencustomizer.xmorph.domain.model.ClockFace
import it.dhd.oxygencustomizer.xmorph.domain.model.EditScope
import it.dhd.oxygencustomizer.xmorph.domain.model.ElementKind
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceAlign
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceImageFilter
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceLayer
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceLayerType
import it.dhd.oxygencustomizer.xmorph.domain.model.FacePlacement
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceSleepStyle
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceTextEffect
import it.dhd.oxygencustomizer.xmorph.domain.model.StyleKey
import it.dhd.oxygencustomizer.xmorph.engine.BundledFonts
import it.dhd.oxygencustomizer.xmorph.engine.FaceColors
import it.dhd.oxygencustomizer.xmorph.engine.FaceFont
import it.dhd.oxygencustomizer.xmorph.view.FaceClockView
import it.dhd.oxygencustomizer.xposed.utils.WidgetFactory
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.DeviceWidgetCell
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.BaseDeviceWidget
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.WidgetMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.roundToInt
import android.R as AndroidR

class LockscreenClockDesignFragment : Fragment() {

    private data class DeviceItemVariant(
        val mode: WidgetMode,
        val config: String,
        val caption: String
    )

    private var _binding: FragmentClockDesignerBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DesignerViewModel
    private lateinit var sheetBehavior: BottomSheetBehavior<View>
    private val canvases = arrayOfNulls<DesignerCanvasView>(PAGES.size)
    private var wallpaper: Bitmap? = null
    private var systemWallpaper: Bitmap? = null
    private var depthSubject: Bitmap? = null
    private var currentPage = START_PAGE
    private var sheet = SHEET_NONE
    private var panelKey: Any? = null
    private var panelFrozen = false
    private var subPageOpen = false
    private var forcePanel = false
    private var pickerReturn = SHEET_NONE
    private var pickerTarget: String? = null
    private var pendingDeviceItem: String? = null
    private var pendingPermissionLabel = 0
    private val widgetPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted && pendingPermissionLabel != 0) Toast.makeText(
                requireContext(),
                pendingPermissionLabel,
                Toast.LENGTH_SHORT
            ).show()
            val config = pendingDeviceItem
            pendingDeviceItem = null
            if (config != null) applyDeviceItem(config) else refreshPanel()
        }
    private val weatherFontPicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            val path = FileUtil.getRealPath(result.data)
            if (path != null && FileUtil.moveToOCHiddenDir(path, LOCKSCREEN_WEATHER_CUSTOM_FONT)) {
                prefs.edit().putBoolean(LOCKSCREEN_WEATHER_CUSTOM_FONT_SWITCH, false).apply()
                prefs.edit().putBoolean(LOCKSCREEN_WEATHER_CUSTOM_FONT_SWITCH, true).apply()
                Toast.makeText(requireContext(), R.string.toast_applied, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), R.string.toast_rename_file, Toast.LENGTH_SHORT)
                    .show()
            }
        }

    private val designPreviews = mutableMapOf<String, DesignerBundle.Design?>()
    private var pendingExportName = ""

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri ?: return@registerForActivityResult
            val context = requireContext().applicationContext
            viewLifecycleOwner.lifecycleScope.launch {
                val image = withContext(Dispatchers.IO) { DesignerImages.import(context, uri) }
                if (image == null) {
                    Toast.makeText(
                        requireContext(),
                        R.string.designer_image_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }
                viewModel.addElement(
                    CustomFaces.image(
                        getString(R.string.designer_element_image),
                        image
                    ), FacePlacement(0.5f, 0.6f, FaceAlign.CENTER), null
                )
                closeSheet()
            }
        }

    private val exportLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument(DESIGN_MIME)) { uri ->
            uri ?: return@registerForActivityResult
            val resolver = requireContext().contentResolver
            viewLifecycleOwner.lifecycleScope.launch {
                val ok = withContext(Dispatchers.IO) {
                    val json = viewModel.exportJson(pendingExportName) ?: return@withContext false
                    try {
                        resolver.openOutputStream(uri, "wt")
                            ?.use { it.write(json.toByteArray()) } != null
                    } catch (_: Exception) {
                        false
                    }
                }
                Toast.makeText(
                    requireContext(),
                    if (ok) R.string.designer_exported else R.string.designer_export_failed,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val importLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri ?: return@registerForActivityResult
            val resolver = requireContext().contentResolver
            viewLifecycleOwner.lifecycleScope.launch {
                val json = withContext(Dispatchers.IO) {
                    try {
                        resolver.openInputStream(uri)
                            ?.use { it.readBytes().toString(Charsets.UTF_8) }
                    } catch (_: Exception) {
                        null
                    }
                }
                val fileName = withContext(Dispatchers.IO) {
                    try {
                        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                            ?.use { if (it.moveToFirst()) it.getString(0) else null }
                    } catch (_: Exception) {
                        null
                    }
                }?.substringBeforeLast('.')?.ifBlank { null }
                    ?: getString(R.string.designer_design_name, viewModel.designs().size + 1)
                importDesign(json, fileName)
            }
        }

    private val fontPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri ?: return@registerForActivityResult
            viewLifecycleOwner.lifecycleScope.launch {
                val file =
                    withContext(Dispatchers.IO) { DesignerFonts.import(requireContext(), uri) }
                if (file == null) {
                    Toast.makeText(
                        requireContext(),
                        R.string.designer_font_import_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }
                applyFont(DesignerFonts.familyOf(file))
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClockDesignerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[DesignerViewModel::class.java]
        DesignerFonts.install()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.content.setPadding(0, bars.top, 0, bars.bottom)
            binding.sheet.setPadding(0, 0, 0, bars.bottom)
            insets
        }

        binding.cancel.setOnClickListener { requireActivity().finish() }
        binding.apply.setOnClickListener { viewModel.save { activity?.finish() } }
        binding.undo.setOnClickListener { viewModel.undo(); refreshPanel() }
        binding.redo.setOnClickListener { viewModel.redo(); refreshPanel() }
        binding.sheetBack.setOnClickListener { goBack() }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    when {
                        subPageOpen -> closeSubPage()
                        sheet == SHEET_DEVICE_ITEMS -> goBack()
                        sheet != SHEET_NONE -> closeSheet()
                        else -> requireActivity().finish()
                    }
                }
            })

        setupPager()
        setupActions()
        setupSheet()
        prefs.registerOnSharedPreferenceChangeListener(depthPrefsListener)

        viewLifecycleOwner.lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                WallpaperUtil.getCompressedWallpaper(
                    requireContext(),
                    80,
                    WallpaperManager.FLAG_LOCK
                )
            }
            systemWallpaper = bitmap
            wallpaper = bitmap
            canvases.forEach { it?.setWallpaper(bitmap) }
            loadDepthPreview()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }

        if (viewModel.uiState.value.face == null) viewModel.open(null)
    }

    override fun onDestroyView() {
        prefs.unregisterOnSharedPreferenceChangeListener(depthPrefsListener)
        super.onDestroyView()
        canvases.fill(null)
        _binding = null
    }

    private fun setupPager() {
        val pager = binding.pager
        pager.offscreenPageLimit = PAGES.size
        pager.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun getItemCount(): Int = PAGES.size

            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder {
                val frame = FrameLayout(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
                frame.addView(
                    DesignerCanvasView(parent.context),
                    FrameLayout.LayoutParams(0, 0, Gravity.CENTER)
                )
                return object : RecyclerView.ViewHolder(frame) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val canvas = (holder.itemView as FrameLayout).getChildAt(0) as DesignerCanvasView
                canvases[position] = canvas
                canvas.setScope(PAGES[position])
                canvas.topMarginDp = topMargin().toFloat()
                canvas.editable = position == currentPage
                wallpaper?.let(canvas::setWallpaper)
                canvas.setDepthOpacity(depthOpacity())
                canvas.setDepthSubject(depthSubject)
                canvas.listener = canvasListener
                viewModel.uiState.value.face?.let(canvas::setFace)
                sizeCard(canvas)
            }
        }
        pager.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> canvases.forEach { it?.let(::sizeCard) } }
        pager.setPageTransformer { page, position ->
            val card = cardSize()
            val gap = dp(PAGE_GAP_DP)
            page.translationX = -position * (page.width - card.first - gap)
        }
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val previous = currentPage
                currentPage = position
                binding.pageLabel.setText(PAGE_LABELS[position])
                viewModel.setScope(PAGES[position])
                viewModel.select(null)
                canvases.forEachIndexed { index, canvas ->
                    canvas?.editable = index == position
                    if (index != position) canvas?.setSelected(null)
                }
                if (previous != position) canvases[position]?.morphTo(
                    PAGES[position],
                    PAGES[previous]
                )
                refreshPanel()
            }
        })
        pager.setCurrentItem(START_PAGE, false)
        binding.pageLabel.setText(PAGE_LABELS[START_PAGE])
    }

    private fun cardSize(): Pair<Int, Int> {
        val b = _binding ?: return 0 to 0
        val metrics = resources.displayMetrics
        val aspect = metrics.widthPixels.toFloat() / metrics.heightPixels.toFloat()
        val maxHeight = b.pager.height - dp(8)
        var width = (b.pager.width * CARD_WIDTH).roundToInt()
        var height = (width / aspect).roundToInt()
        if (maxHeight in 1 until height) {
            height = maxHeight
            width = (height * aspect).roundToInt()
        }
        return width to height
    }

    private fun sizeCard(canvas: DesignerCanvasView) {
        val (width, height) = cardSize()
        if (width <= 0 || height <= 0) return
        val params = canvas.layoutParams as FrameLayout.LayoutParams
        if (params.width == width && params.height == height) return
        params.width = width
        params.height = height
        params.gravity = Gravity.CENTER
        canvas.layoutParams = params
    }

    private val canvasListener = object : DesignerCanvasView.Listener {
        override fun onSelect(id: String?) {
            viewModel.select(id)
            if (id != null && sheet == SHEET_NONE) openSheet(SHEET_STYLE)
        }

        override fun onTransformStart(id: String): Pair<Float, Float> = viewModel.transformBase(id)
        override fun onMove(id: String, dx: Float, dy: Float) = viewModel.move(id, dx, dy)
        override fun onTransform(id: String, size: Float, rotation: Float) =
            viewModel.transform(id, size, rotation, 0f, 0f)

        override fun onResizeStart(id: String) = viewModel.beginResize()
        override fun onResize(id: String, scaleX: Float, scaleY: Float) =
            viewModel.stretchTo(id, scaleX, scaleY)

        override fun onGestureEnd() {
            viewModel.endGesture()
            refreshPanel()
        }
    }

    private fun setupActions() {
        val actions = listOf(
            Triple(R.drawable.ic_designer_faces, R.string.designer_sheet_faces, SHEET_FACES),
            Triple(R.drawable.ic_designer_add, R.string.designer_sheet_add, SHEET_ADD),
            Triple(R.drawable.ic_designer_palette, R.string.designer_sheet_style, SHEET_STYLE),
            Triple(R.drawable.ic_designer_layers, R.string.designer_sheet_layers, SHEET_LAYERS),
            Triple(R.drawable.ic_designer_depth, R.string.designer_sheet_depth, SHEET_DEPTH)
        )
        actions.forEach { (icon, label, target) ->
            binding.actions.addView(roundAction(icon, getString(label)) { openSheet(target) })
        }
    }

    private fun setupSheet() {
        sheetBehavior = BottomSheetBehavior.from(binding.sheet)
        sheetBehavior.isHideable = true
        sheetBehavior.skipCollapsed = true
        sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        binding.root.post {
            val b = _binding ?: return@post
            b.sheet.layoutParams = b.sheet.layoutParams.apply {
                this.height = (b.root.height * SHEET_MAX_HEIGHT).roundToInt()
            }
            sheetBehavior.peekHeight = (b.root.height * SHEET_PEEK_HEIGHT).roundToInt()
        }
        sheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    sheet = SHEET_NONE
                    applySheetFraction(0f)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                applySheetFraction((1f + slideOffset).coerceIn(0f, 1f))
            }
        })
    }

    private fun applySheetFraction(fraction: Float) {
        val b = _binding ?: return
        b.pageRow.alpha = 1f - fraction
        b.actions.alpha = 1f - fraction
    }

    private fun openSheet(target: Int) {
        if (subPageOpen) closeSubPage()
        sheet = target
        refreshPanel()
        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun closeSheet() {
        if (subPageOpen) closeSubPage()
        sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        viewModel.select(null)
    }

    private fun goBack() {
        if (subPageOpen) {
            closeSubPage()
            return
        }
        if (sheet == SHEET_DEVICE_ITEMS) {
            sheet = pickerReturn
            refreshPanel()
            return
        }
        val selected = viewModel.uiState.value.selectedId
        when {
            sheet == SHEET_LAYERS && selected != null && !ItemGroups.isGroup(selected) && viewModel.standaloneKey(
                selected
            ) == null -> viewModel.select(ItemGroups.keyOf(selected))

            sheet == SHEET_LAYERS && selected != null -> viewModel.select(null)
            else -> closeSheet()
        }
        refreshPanel()
    }

    private fun refreshPanel() {
        forcePanel = true
        render(viewModel.uiState.value)
    }

    private fun render(state: EditorUiState) {
        val b = _binding ?: return
        val face = state.face ?: return
        canvases.forEachIndexed { index, canvas ->
            canvas ?: return@forEachIndexed
            canvas.setFace(face)
            canvas.setSelected(if (index == currentPage) state.selectedId else null)
        }
        b.undo.isEnabled = state.canUndo
        b.undo.alpha = if (state.canUndo) 1f else 0.35f
        b.redo.isEnabled = state.canRedo
        b.redo.alpha = if (state.canRedo) 1f else 0.35f
        val key =
            listOf(sheet, state.selectedId, state.scope, face.layers.map { it.id }, state.style)
        if ((!forcePanel && key == panelKey) || panelFrozen || subPageOpen || sheet == SHEET_NONE) return
        forcePanel = false
        val sameSheet =
            (panelKey as? List<*>)?.let { it.getOrNull(0) == sheet && it.getOrNull(1) == state.selectedId } == true
        val scrollY = if (sameSheet) b.sheetScroll.scrollY else 0
        if (!sameSheet) rowScroll.clear()
        panelKey = key
        val panel = b.panel
        panel.removeAllViews()
        b.sheetScroll.post { _binding?.sheetScroll?.scrollTo(0, scrollY) }
        when (sheet) {
            SHEET_FACES -> buildFaces(panel, state)
            SHEET_STYLE -> buildStyle(panel, state, face)
            SHEET_ADD -> buildAdd(panel)
            SHEET_LAYERS -> buildLayers(panel, state, face)
            SHEET_DEPTH -> buildDepth(panel)
            SHEET_DEVICE_ITEMS -> buildDeviceItemPicker(panel, face)
        }
    }

    private fun targetLayers(state: EditorUiState, face: ClockFace): List<FaceLayer> {
        val selected = state.selectedId
        val key = when {
            selected == null -> ItemGroups.keyOfItem(0, true)
            ItemGroups.isGroup(selected) -> selected
            else -> return face.layers.filter { it.id == selected }
        }
        return viewModel.groupLayers(key)
    }

    private fun textual(layer: FaceLayer): Boolean =
        !layer.isShape && !layer.isIcon && !layer.font.startsWith(SYMBOL_FONT)

    private fun buildFaces(panel: LinearLayout, state: EditorUiState) {
        binding.sheetTitle.setText(R.string.designer_sheet_faces)
        binding.sheetBack.visibility = View.GONE
        panel.addView(TextView(requireContext()).apply {
            setText(R.string.designer_faces_hint)
            textSize = 13f
            setTextColor(0x99FFFFFF.toInt())
            setPadding(dp(SHEET_PADDING_DP), 0, dp(SHEET_PADDING_DP), dp(12))
        })
        panel.addView(LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(SHEET_PADDING_DP), 0, dp(SHEET_PADDING_DP), dp(12))
            addView(designAction(R.string.designer_save) {
                askDesignName(R.string.designer_save_title) {
                    saveDesign(
                        it
                    )
                }
            })
            addView(designAction(R.string.designer_export) {
                askDesignName(R.string.designer_export_title) { name ->
                    pendingExportName = name
                    exportLauncher.launch(name + DesignerRepository.DESIGN_EXTENSION)
                }
            })
            addView(designAction(R.string.designer_import) {
                importLauncher.launch(
                    arrayOf(
                        DESIGN_MIME,
                        "*/*"
                    )
                )
            })
        })
        val columns = FACE_COLUMNS
        val width = tileWidth(columns)
        val designs = viewModel.designs()
        if (designs.isNotEmpty()) {
            panel.addView(label(getString(R.string.designer_my_designs)))
            val saved = GridLayout(requireContext()).apply { columnCount = columns }
            designs.forEach { file ->
                val design = designPreviews.getOrPut(file.path + ":" + file.lastModified()) {
                    DesignerBundle.preview(file.readText())
                } ?: return@forEach
                val preview = design.composed()
                val title = design.name.ifBlank { file.nameWithoutExtension }
                val column = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER_HORIZONTAL
                }
                column.addView(
                    faceTile(
                        preview,
                        false,
                        width
                    ) { importDesign(file.readText()) }.apply {
                        layoutParams =
                            LinearLayout.LayoutParams(width, (width * FACE_TILE_RATIO).roundToInt())
                        setOnLongClickListener {
                            AlertDialog.Builder(requireContext())
                                .setTitle(title)
                                .setMessage(R.string.designer_delete_design)
                                .setPositiveButton(R.string.designer_remove) { _, _ ->
                                    viewModel.deleteDesign(file)
                                    refreshPanel()
                                }
                                .setNegativeButton(AndroidR.string.cancel, null)
                                .show()
                            true
                        }
                    })
                column.addView(TextView(requireContext()).apply {
                    text = title
                    gravity = Gravity.CENTER
                    textSize = 12f
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                    setTextColor(0xCCFFFFFF.toInt())
                    setPadding(0, dp(6), 0, dp(TILE_GAP_DP))
                }, LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT))
                saved.addView(column, GridLayout.LayoutParams().apply {
                    this.width = 0
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                })
            }
            panel.addView(saved)
            panel.addView(label(getString(R.string.designer_sheet_faces)))
        }
        val grid = GridLayout(requireContext()).apply { columnCount = columns }
        viewModel.presets().forEach { (style, preset) ->
            val column = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
            }
            column.addView(faceTile(preset, style == state.style, width) {
                viewModel.applyPreset(style)
                refreshPanel()
            }.apply {
                layoutParams =
                    LinearLayout.LayoutParams(width, (width * FACE_TILE_RATIO).roundToInt())
            })
            column.addView(TextView(requireContext()).apply {
                text = preset.name
                gravity = Gravity.CENTER
                textSize = 12f
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                setTextColor(0xCCFFFFFF.toInt())
                setPadding(0, dp(6), 0, dp(TILE_GAP_DP))
            }, LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT))
            grid.addView(column, GridLayout.LayoutParams().apply {
                this.width = 0
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            })
        }
        panel.addView(grid)
        section(
            panel, getString(R.string.designer_layout), listOf(
            prefSlider(
                R.string.lockscreen_clock_top_margin_title,
                LOCKSCREEN_CLOCK_TOP_MARGIN,
                TOP_MARGIN_MIN,
                TOP_MARGIN_MAX,
                TOP_MARGIN_DEFAULT
            ) { applyTopMargin() }
        ))
    }

    private fun buildDepth(panel: LinearLayout) {
        binding.sheetTitle.setText(R.string.depth_wallpaper_category)
        binding.sheetBack.visibility = View.GONE
        val depth = mutableListOf<View>()
        depth += switchWidget(
            getString(R.string.depth_wallpaper_title),
            depthEnabled()
        ) { enabled ->
            localDepthChange = true
            prefs.edit().putBoolean(DEPTH_WALLPAPER_ENABLED, enabled).apply()
            localDepthChange = false
            loadDepthPreview { subject ->
                if (enabled && subject == null) showDepthAlert()
                refreshPanel()
            }
        }
        if (depthEnabled()) {
            if (depthSubject == null) depth += rowWidget(getString(R.string.designer_depth_missing)) {}
            depth += prefSlider(
                R.string.depth_wallpaper_opacity_title,
                DEPTH_OPACITY,
                DEPTH_OPACITY_MIN,
                DEPTH_OPACITY_MAX,
                DEPTH_OPACITY_DEFAULT,
                onLive = ::applyDepthOpacity
            ) { applyDepthOpacity(it) }
        }
        depth += rowWidget(getString(R.string.designer_depth_settings)) {
            openSubPage(DepthWallpaperFragment(), getString(R.string.depth_wallpaper_category))
        }
        section(panel, getString(R.string.depth_wallpaper_category), depth)
    }

    private fun showDepthAlert() {
        if (!isAdded) return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.depth_effect_alert_title)
            .setMessage(getString(R.string.depth_effect_alert_body))
            .setPositiveButton(R.string.depth_effect_ok_btn) { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }

    private fun depthOpacity(): Int =
        OplusSliderPreference.getSingleIntValue(prefs, DEPTH_OPACITY, DEPTH_OPACITY_DEFAULT)

    private fun applyDepthOpacity(value: Int) {
        canvases.forEach { it?.setDepthOpacity(value) }
    }

    private val depthPrefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            DEPTH_WALLPAPER_ENABLED -> if (!localDepthChange) loadDepthPreview()
            DEPTH_OPACITY -> applyDepthOpacity(depthOpacity())
        }
    }

    private var localDepthChange = false

    private fun depthEnabled(): Boolean = prefs.getBoolean(DEPTH_WALLPAPER_ENABLED, false)

    private fun loadDepthPreview(onLoaded: ((Bitmap?) -> Unit)? = null) {
        if (_binding == null) return
        viewLifecycleOwner.lifecycleScope.launch {
            val enabled = depthEnabled()
            val (subject, background) = withContext(Dispatchers.IO) {
                if (!enabled) return@withContext null to null
                BitmapFactory.decodeFile(Constants.getLockScreenSubjectCachePath()) to BitmapFactory.decodeFile(
                    Constants.getLockScreenBitmapCachePath()
                )
            }
            depthSubject = subject
            val shown = if (subject != null && background != null) background else systemWallpaper
            if (shown != null && shown !== wallpaper) {
                wallpaper = shown
                canvases.forEach { it?.setWallpaper(shown) }
            }
            canvases.forEach {
                it?.setDepthOpacity(depthOpacity())
                it?.setDepthSubject(subject)
            }
            onLoaded?.invoke(subject)
        }
    }

    private fun aboveSubjectSwitch(key: String): View? {
        if (!depthEnabled() || viewModel.uiState.value.scope == EditScope.AOD) return null
        return switchWidget(
            getString(R.string.designer_above_subject),
            viewModel.groupAboveSubject(key)
        ) { viewModel.setGroupAboveSubject(key, it) }
    }

    private fun designAction(text: Int, onClick: () -> Unit): View =
        TextView(requireContext()).apply {
            setText(text)
            gravity = Gravity.CENTER
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            background = tileBackground(false, 20)
            setPadding(0, dp(12), 0, dp(12))
            setOnClickListener { onClick() }
            layoutParams =
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = dp(4)
                    marginEnd = dp(4)
                }
        }

    private fun askDesignName(title: Int, onName: (String) -> Unit) {
        val fallback = getString(R.string.designer_design_name, viewModel.designs().size + 1)
        val input = EditText(requireContext()).apply {
            setText(fallback)
            setSingleLine()
            selectAll()
        }
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(FrameLayout(requireContext()).apply {
                setPadding(dp(20), dp(8), dp(20), 0)
                addView(input)
            })
            .setPositiveButton(AndroidR.string.ok) { _, _ ->
                onName(
                    input.text.toString().trim().ifBlank { fallback })
            }
            .setNegativeButton(AndroidR.string.cancel, null)
            .show()
    }

    private fun saveDesign(name: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val ok = withContext(Dispatchers.IO) { viewModel.saveDesign(name) }
            if (!isAdded) return@launch
            Toast.makeText(
                requireContext(),
                if (ok) R.string.designer_saved else R.string.designer_export_failed,
                Toast.LENGTH_SHORT
            ).show()
            refreshPanel()
        }
    }

    private fun importDesign(json: String?, storeAs: String? = null) {
        viewLifecycleOwner.lifecycleScope.launch {
            val ok =
                json != null && withContext(Dispatchers.IO) { viewModel.importJson(json, storeAs) }
            if (!isAdded) return@launch
            if (!ok) {
                Toast.makeText(
                    requireContext(),
                    R.string.designer_import_failed,
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }
            refreshPanel()
        }
    }

    private fun alignTile(icon: Int, text: String, width: Int, onClick: () -> Unit): View =
        LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams =
                LinearLayout.LayoutParams(width, dp(72)).apply { marginEnd = dp(TILE_GAP_DP) }
            background = tileBackground(false)
            contentDescription = text
            setOnClickListener { onClick() }
            addView(
                ImageView(requireContext()).apply { setImageResource(icon) },
                LinearLayout.LayoutParams(dp(24), dp(24))
            )
            addView(TextView(requireContext()).apply {
                this.text = text
                textSize = 11f
                gravity = Gravity.CENTER
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                setTextColor(0xCCFFFFFF.toInt())
                setPadding(dp(4), dp(4), dp(4), 0)
            })
        }

    private fun label(text: String): View = TextView(requireContext()).apply {
        this.text = text
        textSize = 13f
        setTextColor(0x99FFFFFF.toInt())
        setPadding(dp(SHEET_PADDING_DP), dp(14), dp(SHEET_PADDING_DP), dp(8))
    }

    private fun buildStyle(panel: LinearLayout, state: EditorUiState, face: ClockFace) {
        binding.sheetTitle.setText(R.string.designer_sheet_style)
        binding.sheetBack.visibility = View.GONE
        val targets = targetLayers(state, face)
        val textTargets = targets.filter(::textual)
        val tile = tileWidth(3)

        targets.singleOrNull()?.takeIf(::isIcon)?.let { icon -> buildIconPicker(panel, icon) }

        if (textTargets.isNotEmpty()) {
            val current = textTargets.first().font.substringBefore('?')
            val bundled = BundledFonts.ids.keys.filter { it != "symbols" }.map { family ->
                val value = BundledFonts.SCHEME + family
                fontTile(BundledFonts.ids[family]?.let { id ->
                    runCatching {
                        ResourcesCompat.getFont(
                            requireContext(),
                            id
                        )
                    }.getOrNull()
                }, value == current, tile) {
                    applyFont(value)
                }
            }
            val user = DesignerFonts.list().map { file ->
                val value = DesignerFonts.familyOf(file)
                fontTile(
                    runCatching { Typeface.createFromFile(file) }.getOrNull(),
                    value == current,
                    tile
                ) { applyFont(value) }
            }
            val add = addFontTile(tile) {
                fontPicker.launch(
                    arrayOf(
                        "font/*",
                        "application/x-font-ttf",
                        "application/x-font-otf",
                        "application/octet-stream"
                    )
                )
            }
            panel.addView(label(getString(R.string.designer_font)))
            panel.addView(hScroll("fonts", listOf(add) + user + bundled))
        }

        run {
            val selected = state.selectedId
            val (value, range, apply) = if (selected != null && !ItemGroups.isGroup(selected)) {
                Triple(
                    viewModel.value(StyleKey.SIZE),
                    0.015f to 1.5f
                ) { v: Float -> viewModel.setValue(StyleKey.SIZE, v) }
            } else {
                val key = selected ?: ItemGroups.keyOfItem(0, true)
                Triple(viewModel.groupScale(key), 0.5f to 2f) { v: Float ->
                    viewModel.setGroupScale(
                        key,
                        v
                    )
                }
            }
            panel.addView(label(getString(R.string.designer_size)))
            panel.addView(sizeSlider(value, range.first, range.second, apply))
        }

        run {
            val id = state.selectedId ?: ItemGroups.keyOfItem(0, true)
            panel.addView(label(getString(R.string.designer_position)))
            panel.addView(hScroll("position", ALIGNMENTS.map { (align, icon, text) ->
                alignTile(icon, getString(text), tileWidth(3)) {
                    val dx = canvases[binding.pager.currentItem]?.alignOffset(id, align) ?: 0f
                    if (dx != 0f) {
                        viewModel.move(id, dx, 0f)
                        viewModel.endGesture()
                    }
                }
            }))
        }

        val effectTargets =
            targets.filter { textual(it) || isIcon(it) || it.type == FaceLayerType.CARD }
        if (effectTargets.isNotEmpty()) {
            panel.addView(label(getString(R.string.designer_effect)))
            val effect = effectTargets.first().effect
            panel.addView(hScroll("effects", TILE_EFFECTS.map { (value, label) ->
                effectTile(value, getString(label), effect == value, tileWidth(2)) {
                    effectTargets.forEach { layer ->
                        viewModel.updateLayer(
                            layer.id,
                            "effect"
                        ) { it.copy(effect = value) }
                    }
                    refreshPanel()
                }
            }))
        }

        panel.addView(label(getString(R.string.designer_color)))
        panel.addView(colorRow(targets))
    }

    private fun isIcon(layer: FaceLayer): Boolean =
        layer.type == FaceLayerType.TEXT && layer.font.startsWith(SYMBOL_FONT)

    private val symbolTypeface: Typeface? by lazy {
        BundledFonts.ids["symbols"]?.let { id ->
            runCatching {
                ResourcesCompat.getFont(
                    requireContext(),
                    id
                )
            }.getOrNull()
        }
    }

    private val symbolGlyphs: List<String> by lazy {
        val paint = Paint().apply { typeface = symbolTypeface }
        (SYMBOL_FIRST..SYMBOL_LAST).map { String(Character.toChars(it)) }.filter(paint::hasGlyph)
    }

    private fun buildIconPicker(panel: LinearLayout, layer: FaceLayer) {
        val size = tileWidth(ICON_COLUMNS)
        val grid = GridLayout(requireContext()).apply { columnCount = ICON_COLUMNS }
        symbolGlyphs.forEach { glyph ->
            grid.addView(TextView(requireContext()).apply {
                text = glyph
                gravity = Gravity.CENTER
                typeface = symbolTypeface
                setTextSize(TypedValue.COMPLEX_UNIT_PX, size * ICON_GLYPH_RATIO)
                setTextColor(Color.WHITE)
                background = tileBackground(glyph == layer.format, 16)
                setOnClickListener {
                    viewModel.updateLayer(layer.id, "format") { it.copy(format = glyph) }
                    refreshPanel()
                }
            }, GridLayout.LayoutParams().apply {
                width = size
                height = size
                bottomMargin = dp(TILE_GAP_DP)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            })
        }
        section(panel, getString(R.string.designer_element_icon), listOf(grid))
        panel.addView(separator())
    }

    private fun tileWidth(perRow: Int): Int {
        val width = resources.displayMetrics.widthPixels - dp(SHEET_PADDING_DP * 2)
        return (width - dp(TILE_GAP_DP) * (perRow - 1)) / perRow
    }

    private fun separator(): View = View(requireContext()).apply {
        setBackgroundColor(0x1FFFFFFF)
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)).apply {
            setMargins(dp(SHEET_PADDING_DP), dp(16), dp(SHEET_PADDING_DP), dp(16))
        }
    }

    private fun colorRow(targets: List<FaceLayer>): View {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(SHEET_PADDING_DP), dp(20), dp(8), dp(8))
        }
        row.addView(customColorDot(targets))
        row.addView(View(requireContext()).apply {
            setBackgroundColor(0x33FFFFFF)
            layoutParams = LinearLayout.LayoutParams(dp(1), dp(28)).apply { marginEnd = dp(12) }
        })
        COLOR_DOTS.forEach { token ->
            row.addView(
                colorDot(
                    FaceColors.resolve(
                        requireContext(),
                        token
                    )
                ) { applyColor(targets, token) })
        }
        return HorizontalScrollView(requireContext()).apply {
            isHorizontalScrollBarEnabled = false
            addView(
                row,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
            setOnScrollChangeListener { _, x, _, _, _ -> rowScroll["colors"] = x }
            val saved = rowScroll["colors"] ?: 0
            if (saved > 0) post { scrollTo(saved, 0) }
        }
    }

    private fun applyColor(targets: List<FaceLayer>, color: String) {
        val aod = viewModel.uiState.value.scope == EditScope.AOD
        targets.forEach { layer ->
            viewModel.updateLayer(
                layer.id,
                "color"
            ) { if (aod) it.copy(sleepColor = color) else it.copy(color = color) }
        }
    }

    private fun buildAdd(panel: LinearLayout) {
        binding.sheetTitle.setText(R.string.designer_sheet_add)
        binding.sheetBack.visibility = View.GONE
        val grid = GridLayout(requireContext()).apply {
            columnCount = 4
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }
        ADDABLE.forEach { (kind, glyph, label) ->
            grid.addView(componentTile(glyph, getString(label)) {
                if (kind == ElementKind.DEVICE_ITEM) {
                    chooseDeviceItem(null)
                    return@componentTile
                }
                if (kind == ElementKind.IMAGE) {
                    imagePicker.launch("image/*")
                    return@componentTile
                }
                if (kind == ElementKind.WIDGET_BIG || kind == ElementKind.WIDGET_SMALL) {
                    chooseWidget { type ->
                        insertElement(kind, type)
                        closeSheet()
                    }
                    return@componentTile
                }
                addElement(kind)
                if (kind == ElementKind.ICON) openSheet(SHEET_STYLE) else closeSheet()
            }, GridLayout.LayoutParams().apply {
                width = 0
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            })
        }
        panel.addView(grid)
    }


    private fun buildLayerControls(panel: LinearLayout, state: EditorUiState, layer: FaceLayer) {
        val text = textual(layer)
        if (state.scope == EditScope.AOD) {
            val aod = mutableListOf<View>()
            aod += switchWidget(
                getString(R.string.designer_visible),
                viewModel.elementVisible(layer.id)
            ) { viewModel.setElementVisible(layer.id, it) }
            aod += listWidget(
                getString(R.string.designer_aod_style),
                AOD_STYLES.map { getString(it.second) },
                AOD_STYLES.indexOfFirst { it.first == layer.sleepStyle }) { index ->
                viewModel.updateLayer(
                    layer.id,
                    "sleepStyle"
                ) { it.copy(sleepStyle = AOD_STYLES[index].first) }
            }
            if (layer.type != FaceLayerType.CARD && !layer.isHost) aod += styleSlider(
                StyleKey.WEIGHT,
                R.string.designer_weight,
                100f,
                1000f
            )
            aod += styleSlider(StyleKey.OPACITY, R.string.designer_opacity, 0.1f, 1f)
            section(panel, getString(R.string.designer_aod_style), aod)
            return
        }

        if (layer.isHost) {
            buildHostControls(panel, layer)
            return
        }

        if (layer.type == FaceLayerType.TEXT && text || layer.type == FaceLayerType.DATE) {
            val title =
                if (layer.type == FaceLayerType.DATE) R.string.designer_date_format else R.string.designer_content
            section(
                panel,
                getString(R.string.designer_text_section),
                listOf(textField(getString(title), layer.format) { value ->
                    viewModel.updateLayer(layer.id, "format") { it.copy(format = value) }
                })
            )
        }

        val style = mutableListOf<View>()
        style += switchWidget(
            getString(R.string.designer_visible),
            viewModel.elementVisible(layer.id)
        ) { viewModel.setElementVisible(layer.id, it) }
        if (text || isIcon(layer) || layer.type == FaceLayerType.CARD) {
            style += switchWidget(
                getString(R.string.designer_glass),
                viewModel.effect() == FaceTextEffect.GLASS
            ) { on ->
                viewModel.setEffect(if (on) FaceTextEffect.GLASS else FaceTextEffect.SOLID)
                refreshPanel()
            }
        }
        section(panel, getString(R.string.designer_style), style)

        if (layer.type == FaceLayerType.IMAGE) section(
            panel,
            getString(R.string.albumart_filter_title),
            imageFilterControls(layer)
        )

        val shape = mutableListOf<View>()
        if (layer.type != FaceLayerType.CARD && (!text || FaceFont.supportsWeight(layer.font))) {
            shape += styleSlider(
                StyleKey.WEIGHT,
                R.string.designer_weight,
                100f,
                if (text) 1000f else 700f
            )
        }
        if (text && FaceFont.hasWidthAxis(layer.font)) shape += styleSlider(
            StyleKey.WIDTH,
            R.string.designer_width,
            25f,
            151f
        )
        if (text) {
            shape += styleSlider(StyleKey.TRACKING, R.string.designer_tracking, -0.2f, 0.4f)
            shape += styleSlider(StyleKey.LINE_HEIGHT, R.string.designer_line_height, 0.5f, 2f)
        }
        if (viewModel.effect() != FaceTextEffect.SOLID) shape += styleSlider(
            StyleKey.EFFECT_TINT,
            R.string.designer_tint,
            0f,
            1f
        )
        shape += styleSlider(StyleKey.ROTATION, R.string.designer_rotation, -45f, 45f)
        shape += styleSlider(StyleKey.OPACITY, R.string.designer_opacity, 0.1f, 1f)
        section(panel, getString(R.string.designer_size), shape)

        val layout = mutableListOf<View>()
        layout += listWidget(
            getString(R.string.designer_align),
            alignLabels(),
            -1
        ) { viewModel.setAlign(ALIGNS[it]) }
        aboveSubjectSwitch(ItemGroups.keyOf(layer.id))?.let { layout += it }
        section(panel, getString(R.string.designer_layout), layout)
        layerMover(layer)?.let { section(panel, getString(R.string.designer_order), orderRows(it)) }

        if (viewModel.canRemoveTarget(layer.id)) {
            section(
                panel,
                getString(R.string.designer_actions),
                listOf(rowWidget(getString(R.string.designer_remove)) { viewModel.removeTarget(layer.id) })
            )
        }
    }

    private fun layerMover(layer: FaceLayer): ((Int) -> Unit)? {
        if (viewModel.elementPosition(layer.id) != null) return { steps ->
            viewModel.moveElement(
                layer.id,
                steps
            )
        }
        val key = viewModel.standaloneKey(layer.id) ?: return null
        if (viewModel.groupPosition(key) == null) return null
        return { steps -> viewModel.moveGroup(key, steps) }
    }

    private fun orderRows(move: (Int) -> Unit): List<View> = listOf(
        rowWidget(getString(R.string.designer_move_to_back)) { move(-ORDER_EDGE) },
        rowWidget(getString(R.string.designer_move_back)) { move(-1) },
        rowWidget(getString(R.string.designer_move_front)) { move(1) },
        rowWidget(getString(R.string.designer_move_to_front)) { move(ORDER_EDGE) }
    )

    private fun buildHostControls(panel: LinearLayout, layer: FaceLayer) {
        when (layer.type) {
            FaceLayerType.WIDGET_BIG, FaceLayerType.WIDGET_SMALL -> buildWidgetControls(
                panel,
                layer
            )

            FaceLayerType.WEATHER_CARD -> buildWeatherControls(panel)
            FaceLayerType.DEVICE_WIDGET -> buildDeviceControls(panel)
            FaceLayerType.DEVICE_ITEM -> buildDeviceItemControls(panel, layer)
            else -> Unit
        }
        val layout = mutableListOf<View>()
        layout += switchWidget(
            getString(R.string.designer_visible),
            viewModel.elementVisible(layer.id)
        ) { viewModel.setElementVisible(layer.id, it) }
        aboveSubjectSwitch(ItemGroups.keyOf(layer.id))?.let { layout += it }
        section(panel, getString(R.string.designer_layout), layout)
        layerMover(layer)?.let { section(panel, getString(R.string.designer_order), orderRows(it)) }
        section(
            panel, getString(R.string.designer_size), listOf(
                styleSlider(StyleKey.ROTATION, R.string.designer_rotation, -45f, 45f),
                styleSlider(StyleKey.OPACITY, R.string.designer_opacity, 0.1f, 1f)
            )
        )
        if (viewModel.canRemoveTarget(layer.id)) {
            section(
                panel,
                getString(R.string.designer_actions),
                listOf(rowWidget(getString(R.string.designer_remove)) { viewModel.removeTarget(layer.id) })
            )
        }
    }

    private fun buildWidgetControls(panel: LinearLayout, layer: FaceLayer) {
        section(
            panel,
            getString(R.string.designer_widget),
            listOf(rowWidget(getString(R.string.designer_widget)) {
                chooseWidget { type ->
                    viewModel.updateLayer(
                        layer.id,
                        "format"
                    ) { it.copy(format = type) }
                }
            }.apply { setSummary(widgetLabel(layer.format)) })
        )
        val style = mutableListOf<View>()
        style += prefList(
            R.string.lockscreen_widgets_style_title,
            LOCKSCREEN_WIDGETS_STYLE,
            R.array.lockscreen_widgets_style_entries,
            R.array.lockscreen_widgets_style_values,
            "0"
        )
        style += prefSwitch(
            R.string.widgets_custom_color_title,
            LOCKSCREEN_WIDGETS_CUSTOM_COLOR,
            false,
            true
        )
        if (prefs.getBoolean(LOCKSCREEN_WIDGETS_CUSTOM_COLOR, false)) {
            if (layer.type == FaceLayerType.WIDGET_BIG) {
                style += prefColor(R.string.big_active, LOCKSCREEN_WIDGETS_BIG_ACTIVE, Color.WHITE)
                style += prefColor(
                    R.string.big_inactive,
                    LOCKSCREEN_WIDGETS_BIG_INACTIVE,
                    Color.BLACK
                )
                style += prefColor(
                    R.string.big_icon_active,
                    LOCKSCREEN_WIDGETS_BIG_ICON_ACTIVE,
                    Color.BLACK
                )
                style += prefColor(
                    R.string.big_icon_inactive,
                    LOCKSCREEN_WIDGETS_BIG_ICON_INACTIVE,
                    Color.WHITE
                )
            } else {
                style += prefColor(
                    R.string.mini_active,
                    LOCKSCREEN_WIDGETS_SMALL_ACTIVE,
                    Color.WHITE
                )
                style += prefColor(
                    R.string.mini_inactive,
                    LOCKSCREEN_WIDGETS_SMALL_INACTIVE,
                    Color.BLACK
                )
                style += prefColor(
                    R.string.mini_icon_active,
                    LOCKSCREEN_WIDGETS_SMALL_ICON_ACTIVE,
                    Color.BLACK
                )
                style += prefColor(
                    R.string.mini_icon_inactive,
                    LOCKSCREEN_WIDGETS_SMALL_ICON_INACTIVE,
                    Color.WHITE
                )
            }
        }
        section(panel, getString(R.string.lockscreen_widgets_title), style)
    }

    private fun buildWeatherControls(panel: LinearLayout) {
        section(
            panel, getString(R.string.lockscreen_weather), listOf(
                prefSwitch(R.string.weather_show_location, LOCKSCREEN_WEATHER_SHOW_LOCATION, true),
                prefSwitch(
                    R.string.weather_show_condition,
                    LOCKSCREEN_WEATHER_SHOW_CONDITION,
                    true
                ),
                prefSwitch(R.string.weather_show_humidity, LOCKSCREEN_WEATHER_HUMIDITY, true),
                prefSwitch(R.string.weather_show_wind, LOCKSCREEN_WEATHER_WIND, true),
                prefSlider(
                    R.string.weather_text_size,
                    LOCKSCREEN_WEATHER_TEXT_SIZE,
                    WEATHER_SIZE_MIN,
                    WEATHER_SIZE_MAX,
                    WEATHER_TEXT_SIZE
                ),
                prefSlider(
                    R.string.weather_image_size,
                    LOCKSCREEN_WEATHER_IMAGE_SIZE,
                    WEATHER_SIZE_MIN,
                    WEATHER_SIZE_MAX,
                    WEATHER_IMAGE_SIZE
                )
            )
        )
        val look = mutableListOf<View>()
        look += prefSwitch(R.string.weather_centered, LOCKSCREEN_WEATHER_CENTERED, false)
        look += prefList(
            R.string.lockscreen_weather_selection_title,
            LOCKSCREEN_WEATHER_BACKGROUND,
            R.array.lockscreen_weather_bg_entries,
            R.array.lockscreen_weather_bg_values,
            "0"
        )
        look += prefSwitch(
            R.string.weather_custom_color,
            LOCKSCREEN_WEATHER_CUSTOM_COLOR_SWITCH,
            false,
            true
        )
        if (prefs.getBoolean(LOCKSCREEN_WEATHER_CUSTOM_COLOR_SWITCH, false)) {
            look += prefColor(R.string.custom_color, LOCKSCREEN_WEATHER_CUSTOM_COLOR, Color.WHITE)
        }
        look += prefSwitch(
            R.string.lockscreen_clock_font_custom_enabled,
            LOCKSCREEN_WEATHER_CUSTOM_FONT_SWITCH,
            false,
            true
        )
        if (prefs.getBoolean(LOCKSCREEN_WEATHER_CUSTOM_FONT_SWITCH, false)) {
            look += rowWidget(getString(R.string.pick_font_title)) { pickWeatherFont() }
        }
        section(panel, getString(R.string.designer_style), look)
        section(
            panel,
            getString(R.string.weather_settings),
            listOf(rowWidget(getString(R.string.weather_settings)) {
                openSubPage(WeatherSettings(), getString(R.string.weather_settings))
            })
        )
    }

    private fun buildDeviceControls(panel: LinearLayout) {
        val items = mutableListOf<View>()
        items += prefList(
            R.string.lockscreen_device_widget_style_title,
            LOCKSCREEN_WIDGETS_DEVICE_WIDGET_STYLE,
            R.array.lockscreen_device_widget_entries,
            R.array.lockscreen_device_widget_values,
            "0",
            true
        )
        items += prefSwitch(
            R.string.lockscreen_device_widget_liquid_glass_title,
            LOCKSCREEN_WIDGETS_DEVICE_WIDGET_LIQUID_GLASS,
            false
        )
        items += prefSwitch(
            R.string.custom_color,
            LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH,
            false,
            true
        )
        if (prefs.getBoolean(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH, false)) {
            if (prefs.getString(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_STYLE, "0") == "0") {
                items += prefColor(
                    R.string.linear_progress_color,
                    LOCKSCREEN_WIDGETS_DEVICE_WIDGET_LINEAR_COLOR,
                    Color.WHITE
                )
            }
            items += prefColor(
                R.string.circular_progress_color,
                LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CIRCULAR_COLOR,
                Color.WHITE
            )
        }
        items += prefColor(
            R.string.text_color,
            LOCKSCREEN_WIDGETS_DEVICE_WIDGET_TEXT_COLOR,
            Color.WHITE
        )
        items += textField(
            getString(R.string.custom_device_name),
            prefs.getString(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_DEVICE, "") ?: ""
        ) { value ->
            prefs.edit().putString(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_DEVICE, value).apply()
        }
        section(panel, getString(R.string.designer_element_device_widget), items)
        section(
            panel,
            getString(R.string.lockscreen_device_widget_settings),
            listOf(rowWidget(getString(R.string.lockscreen_device_widget_settings)) {
                openSubPage(
                    DeviceWidgetFragment(),
                    getString(R.string.lockscreen_device_widget_settings)
                )
            })
        )
    }

    private fun buildDeviceItemControls(panel: LinearLayout, layer: FaceLayer) {
        val widget = WidgetFactory.fromConfigJson(layer.format, requireContext(), true) ?: return
        val items = mutableListOf<View>()
        items += rowWidget(getString(R.string.designer_widget)) { chooseDeviceItem(layer.id) }.apply {
            setSummary(
                widget.widgetName
            )
        }
        missingPermission(widget.widgetReference)?.let { (permission, label) ->
            items += rowWidget(getString(label)) {
                requestWidgetPermission(
                    permission,
                    label,
                    null
                )
            }
        }
        if (widget.hasBigMode() && widget.hasSmallMode()) {
            val modes = listOf(WidgetMode.BIG, WidgetMode.SMALL)
            items += listWidget(
                getString(R.string.designer_widget_size),
                listOf(
                    getString(R.string.designer_widget_big),
                    getString(R.string.designer_widget_small)
                ),
                modes.indexOf(widget.mode)
            ) { index ->
                widget.setMode(modes[index])
                viewModel.updateLayer(
                    layer.id,
                    "format"
                ) { it.copy(format = WidgetFactory.toConfigJson(widget)) }
            }
        }
        if (widget.customSettings.isNotEmpty()) {
            items += rowWidget(getString(R.string.designer_widget_settings)) {
                val page = WidgetSettingsPreferenceFragment(widget.customSettings)
                page.setPreferenceChangeListener {
                    widget.applySettings(page.updatedSettings)
                    viewModel.updateLayer(
                        layer.id,
                        "format"
                    ) { it.copy(format = WidgetFactory.toConfigJson(widget)) }
                }
                openSubPage(page, getString(R.string.widget_string, widget.widgetName))
            }
        }
        section(panel, getString(R.string.designer_element_device_item), items)
        val style = mutableListOf<View>()
        style += prefSwitch(
            R.string.lockscreen_device_widget_liquid_glass_title,
            LOCKSCREEN_WIDGETS_DEVICE_WIDGET_LIQUID_GLASS,
            false
        )
        style += prefSwitch(
            R.string.custom_color,
            LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH,
            false,
            true
        )
        if (prefs.getBoolean(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH, false)) {
            style += prefColor(
                R.string.circular_progress_color,
                LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CIRCULAR_COLOR,
                Color.WHITE
            )
        }
        style += prefColor(
            R.string.text_color,
            LOCKSCREEN_WIDGETS_DEVICE_WIDGET_TEXT_COLOR,
            Color.WHITE
        )
        section(panel, getString(R.string.designer_style), style)
    }

    private fun chooseDeviceItem(target: String?) {
        pickerReturn = sheet
        pickerTarget = target
        sheet = SHEET_DEVICE_ITEMS
        refreshPanel()
        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun buildDeviceItemPicker(panel: LinearLayout, face: ClockFace) {
        binding.sheetTitle.setText(R.string.designer_element_device_item)
        binding.sheetBack.visibility = View.VISIBLE
        val current = pickerTarget?.let { id -> face.layers.firstOrNull { it.id == id } }
            ?.let { WidgetFactory.fromConfigJson(it.format, requireContext(), true) }
            ?.let(WidgetFactory::toConfigJson)
        val rowWidth = resources.displayMetrics.widthPixels - dp(SHEET_PADDING_DP * 2)
        WidgetFactory.REFERENCES.forEach { reference ->
            val widget =
                WidgetFactory.getNewWidget(requireContext(), reference, WidgetMode.BIG, true)
                    ?: return@forEach
            val variants = deviceItemVariants(reference, widget)
            if (variants.isEmpty()) return@forEach
            panel.addView(label(widget.widgetName))
            var row: LinearLayout? = null
            var used = DEVICE_GRID_COLUMNS
            variants.forEach { variant ->
                val span = if (variant.mode == WidgetMode.SMALL) 1 else 2
                if (row == null || used + span > DEVICE_GRID_COLUMNS) {
                    row = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(
                            dp(SHEET_PADDING_DP) - dp(DEVICE_TILE_PADDING_DP),
                            0,
                            dp(SHEET_PADDING_DP),
                            dp(4)
                        )
                    }.also(panel::addView)
                    used = 0
                }
                used += span
                row?.addView(
                    deviceItemTile(
                        reference,
                        variant,
                        rowWidth,
                        variant.config == current
                    )
                )
            }
        }
    }

    private fun deviceItemVariants(
        reference: String,
        widget: BaseDeviceWidget
    ): List<DeviceItemVariant> {
        val modes = buildList {
            if (widget.hasBigMode()) add(WidgetMode.BIG)
            if (widget.hasSmallMode()) add(WidgetMode.SMALL)
        }
        return modes.flatMap { mode ->
            widget.setMode(mode)
            val menu = widget.customSettings.firstOrNull { it.type == SettingItem.Type.MENU }
            if (menu == null) {
                val caption =
                    if (modes.size > 1) getString(if (mode == WidgetMode.BIG) R.string.designer_widget_big else R.string.designer_widget_small) else ""
                listOf(DeviceItemVariant(mode, WidgetFactory.toConfigJson(widget), caption))
            } else {
                menu.values.indices.mapNotNull { index ->
                    val variant = WidgetFactory.getNewWidget(
                        requireContext(),
                        reference,
                        mode,
                        true,
                        mapOf(menu.key to menu.values[index])
                    ) ?: return@mapNotNull null
                    DeviceItemVariant(
                        mode,
                        WidgetFactory.toConfigJson(variant),
                        menu.entries.getOrElse(index) { "" })
                }
            }
        }
    }

    private fun deviceItemTile(
        reference: String,
        variant: DeviceItemVariant,
        rowWidth: Int,
        selected: Boolean
    ): View {
        val cell = DeviceWidgetCell(requireContext()).apply {
            setReferenceWidth(rowWidth)
            setConfig(variant.config)
            setStyle(
                prefs.getBoolean(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_LIQUID_GLASS, false),
                prefs.getBoolean(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH, false),
                prefs.getInt(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CIRCULAR_COLOR, Color.WHITE),
                prefs.getInt(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_TEXT_COLOR, Color.WHITE),
                prefs.getString(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_DEVICE, "") ?: ""
            )
        }
        val content = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            addView(cell)
            if (variant.caption.isNotEmpty()) {
                addView(
                    TextView(requireContext()).apply {
                        text = variant.caption
                        textSize = 11f
                        gravity = Gravity.CENTER
                        maxLines = 1
                        ellipsize = TextUtils.TruncateAt.END
                        setTextColor(0x99FFFFFF.toInt())
                        setPadding(0, dp(4), 0, 0)
                    },
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
            }
        }
        return object : FrameLayout(requireContext()) {
            override fun onInterceptTouchEvent(ev: MotionEvent): Boolean = true
        }.apply {
            val padding = dp(DEVICE_TILE_PADDING_DP)
            setPadding(padding, padding, padding, padding)
            if (selected) background = tileBackground(true, 22)
            addView(content)
            setOnClickListener { pickDeviceItem(reference, variant.config) }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = dp(DEVICE_TILE_GAP_DP)
            }
        }
    }

    private fun pickDeviceItem(reference: String, config: String) {
        val missing = missingPermission(reference)
        if (missing != null) {
            requestWidgetPermission(missing.first, missing.second, config)
            return
        }
        applyDeviceItem(config)
    }

    private fun missingPermission(reference: String): Pair<String, Int>? {
        val required = WIDGET_PERMISSIONS[reference] ?: return null
        val granted =
            if (required.first == USAGE_ACCESS) UsageProvider.hasUsageAccess(requireContext()) else AppUtils.hasPermission(
                requireContext(),
                required.first
            )
        return if (granted) null else required
    }

    private fun requestWidgetPermission(permission: String, label: Int, config: String?) {
        if (permission == USAGE_ACCESS) {
            requestUsageAccess(label, config)
            return
        }
        pendingDeviceItem = config
        pendingPermissionLabel = label
        widgetPermission.launch(permission)
    }

    private fun requestUsageAccess(label: Int, config: String?) {
        val context = requireContext().applicationContext
        viewLifecycleOwner.lifecycleScope.launch {
            val granted = withContext(Dispatchers.IO) {
                Shell.cmd("appops set ${BuildConfig.APPLICATION_ID} GET_USAGE_STATS allow").exec()
                UsageProvider.hasUsageAccess(context)
            }
            if (!isAdded) return@launch
            if (!granted) {
                Toast.makeText(requireContext(), label, Toast.LENGTH_SHORT).show()
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
            if (config != null) applyDeviceItem(config) else refreshPanel()
        }
    }

    private fun applyDeviceItem(config: String) {
        val target = pickerTarget
        if (target == null) {
            insertElement(ElementKind.DEVICE_ITEM, config)
            closeSheet()
            return
        }
        viewModel.updateLayer(target, "format") { it.copy(format = config) }
        sheet = pickerReturn
        refreshPanel()
    }

    private fun chooseWidget(onPicked: (String) -> Unit) {
        val labels = resources.getStringArray(R.array.widget_entries)
        val values = resources.getStringArray(R.array.widget_values)
        val options = values.indices.filter { values[it].isNotEmpty() }
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.designer_widget)
            .setItems(options.map { labels[it] }.toTypedArray()) { _, index ->
                val value = values[options[index]]
                if (value == CUSTOM_APP_WIDGET) pickApp { onPicked("$CUSTOM_APP_WIDGET:$it") } else onPicked(
                    value
                )
            }
            .setNegativeButton(AndroidR.string.cancel, null)
            .show()
    }

    private fun pickApp(onPicked: (String) -> Unit) {
        val activity = requireActivity()
        Thread {
            val adapter = PackageListAdapter(activity)
            activity.runOnUiThread {
                if (!isAdded) return@runOnUiThread
                AlertDialog.Builder(activity)
                    .setTitle(R.string.qs_widget_custom_app)
                    .setAdapter(adapter) { _, which -> onPicked(adapter.getItem(which).packageName) }
                    .setNegativeButton(AndroidR.string.cancel, null)
                    .show()
            }
        }.start()
    }

    private fun widgetLabel(type: String): String {
        if (type.startsWith("$CUSTOM_APP_WIDGET:")) return AppUtils.getAppName(
            requireContext(),
            type.substringAfter(':')
        )
        val index = resources.getStringArray(R.array.widget_values).indexOf(type)
        return if (index >= 0) resources.getStringArray(R.array.widget_entries)[index] else type
    }

    private val prefs: SharedPreferences
        get() = OCPreferences.getPrefs()

    private fun prefSwitch(
        title: Int,
        key: String,
        default: Boolean,
        refresh: Boolean = false
    ): SwitchWidget =
        switchWidget(getString(title), prefs.getBoolean(key, default)) {
            prefs.edit().putBoolean(key, it).apply()
            if (refresh) refreshPanel()
        }

    private fun prefList(
        title: Int,
        key: String,
        entries: Int,
        values: Int,
        default: String,
        refresh: Boolean = false
    ): ListWidget {
        val keys = resources.getStringArray(values)
        return listWidget(
            getString(title),
            resources.getStringArray(entries).toList(),
            keys.indexOf(prefs.getString(key, default))
        ) { index ->
            prefs.edit().putString(key, keys[index]).apply()
            if (refresh) refreshPanel()
        }
    }

    private fun prefSlider(
        title: Int,
        key: String,
        from: Int,
        to: Int,
        default: Int,
        onLive: ((Int) -> Unit)? = null,
        onChange: ((Int) -> Unit)? = null
    ): SliderWidget = SliderWidget(requireContext()).apply {
        setTitle(getString(title))
        setSliderValueFrom(from)
        setSliderValueTo(to)
        setSliderStepSize(1)
        setSliderValue(
            OplusSliderPreference.getSingleIntValue(prefs, key, default).coerceIn(from, to)
        )
        setOnSliderTouchListener(object : OplusSlider.OnSliderChangeListener {
            override fun onProgressChanged(slider: OplusSlider, fromUser: Boolean) {
                if (fromUser) onLive?.invoke(getSliderValue())
            }

            override fun onStartTrackingTouch(slider: OplusSlider) {
                panelFrozen = true
            }

            override fun onStopTrackingTouch(slider: OplusSlider) {
                panelFrozen = false
                OplusSliderPreference.setValues(prefs, key, listOf(getSliderValue().toFloat()))
                onChange?.invoke(getSliderValue())
            }
        })
    }

    private fun topMargin(): Int = OplusSliderPreference.getSingleIntValue(
        prefs,
        LOCKSCREEN_CLOCK_TOP_MARGIN,
        TOP_MARGIN_DEFAULT
    )

    private fun applyTopMargin() {
        val margin = topMargin().toFloat()
        canvases.forEach { it?.topMarginDp = margin }
    }

    private fun prefColor(title: Int, key: String, default: Int): ColorPickerWidget =
        ColorPickerWidget(requireContext()).apply {
            setTitle(getString(title))
            setPreviewColor(prefs.getInt(key, default))
            setOnClickListener {
                val dialog = ColorPickerDialog.newBuilder().setColor(prefs.getInt(key, default))
                    .setShowAlphaSlider(true).create()
                dialog.setColorPickerDialogListener(object : ColorPickerDialogListener {
                    override fun onColorSelected(dialogId: Int, color: Int) {
                        prefs.edit().putInt(key, color).apply()
                        setPreviewColor(color)
                    }

                    override fun onDialogDismissed(dialogId: Int) {}
                })
                dialog.show(parentFragmentManager, "designer_pref_color")
            }
        }

    private fun pickWeatherFont() {
        if (!AppUtils.hasStoragePermission()) {
            AppUtils.requestStoragePermission(requireContext())
        } else {
            FileUtil.launchFilePicker(weatherFontPicker, "font/*")
        }
    }

    private fun openSubPage(page: Fragment, title: String) {
        val b = _binding ?: return
        subPageOpen = true
        b.sheetScroll.visibility = View.GONE
        b.sheetPage.visibility = View.VISIBLE
        b.sheetTitle.text = title
        b.sheetBack.visibility = View.VISIBLE
        childFragmentManager.beginTransaction().replace(R.id.sheet_page, page, SUB_PAGE_TAG)
            .commit()
        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun closeSubPage() {
        val b = _binding ?: return
        subPageOpen = false
        childFragmentManager.findFragmentByTag(SUB_PAGE_TAG)
            ?.let { childFragmentManager.beginTransaction().remove(it).commit() }
        b.sheetPage.visibility = View.GONE
        b.sheetScroll.visibility = View.VISIBLE
        refreshPanel()
    }


    private fun buildLayers(panel: LinearLayout, state: EditorUiState, face: ClockFace) {
        binding.sheetBack.visibility = if (state.selectedId == null) View.GONE else View.VISIBLE
        val selected = state.selectedId
        when {
            selected == null -> {
                binding.sheetTitle.setText(R.string.designer_sheet_layers)
                val items = state.draft?.parts?.composition?.items.orEmpty()
                val grouping = state.grouping
                if (grouping != null) {
                    section(
                        panel,
                        getString(R.string.designer_group_items),
                        items.filterNot { it.clock }.map { item ->
                            val key = ItemGroups.keyOfItem(item.id, false)
                            switchWidget(viewModel.groupName(key), item.id in grouping) {
                                viewModel.toggleGrouping(key)
                                refreshPanel()
                            }.apply { itemSummary(key)?.let(::setSummary) }
                        })
                    val actions = mutableListOf<View>()
                    if (grouping.size >= 2) actions += rowWidget(getString(R.string.designer_group)) {
                        viewModel.commitGrouping(getString(R.string.designer_group))
                    }
                    actions += rowWidget(getString(R.string.designer_cancel)) {
                        viewModel.cancelGrouping()
                        refreshPanel()
                    }
                    section(panel, getString(R.string.designer_actions), actions)
                    return
                }
                section(panel, getString(R.string.designer_items), items.map { item ->
                    val key = ItemGroups.keyOfItem(item.id, item.clock)
                    rowWidget(
                        viewModel.groupName(key)
                            .ifEmpty { getString(R.string.designer_clock) }) { viewModel.select(key) }
                        .apply { if (!item.clock) itemSummary(key)?.let(::setSummary) }
                })
                if (items.count { !it.clock } >= 2) {
                    section(
                        panel,
                        getString(R.string.designer_actions),
                        listOf(rowWidget(getString(R.string.designer_group_items)) {
                            viewModel.beginGrouping()
                            refreshPanel()
                        })
                    )
                }
            }

            ItemGroups.isGroup(selected) -> {
                binding.sheetTitle.text =
                    viewModel.groupName(selected).ifEmpty { getString(R.string.designer_clock) }
                val item = mutableListOf<View>()
                item += switchWidget(
                    getString(R.string.designer_visible),
                    viewModel.groupVisible(selected, state.scope)
                ) {
                    viewModel.setGroupVisible(selected, state.scope, it)
                }
                aboveSubjectSwitch(selected)?.let { item += it }
                if (state.scope != EditScope.AOD) {
                    viewModel.alignOf(selected)?.let { align ->
                        item += listWidget(
                            getString(R.string.designer_align),
                            alignLabels(),
                            ALIGNS.indexOf(align)
                        ) { viewModel.alignTarget(selected, ALIGNS[it]) }
                    }
                }
                section(panel, getString(R.string.designer_layout), item)
                if (state.scope != EditScope.AOD && viewModel.groupPosition(selected) != null) {
                    section(
                        panel,
                        getString(R.string.designer_order),
                        orderRows { steps -> viewModel.moveGroup(selected, steps) })
                }
                section(
                    panel,
                    getString(R.string.designer_layers),
                    viewModel.groupLayers(selected).map { layer ->
                        rowWidget(layerLabel(layer)) { viewModel.select(layer.id) }
                    })
                val actions = mutableListOf<View>()
                if (viewModel.isGroupItem(selected)) {
                    if (state.scope != EditScope.AOD) {
                        actions += rowWidget(getString(R.string.designer_distribute_horizontal)) {
                            distribute(
                                selected,
                                true
                            )
                        }
                        actions += rowWidget(getString(R.string.designer_distribute_vertical)) {
                            distribute(
                                selected,
                                false
                            )
                        }
                    }
                    actions += rowWidget(getString(R.string.designer_ungroup)) {
                        viewModel.ungroup(
                            selected
                        )
                    }
                }
                if (viewModel.groupChanged(selected)) actions += rowWidget(getString(R.string.designer_reset)) {
                    viewModel.resetGroup(
                        selected
                    ); refreshPanel()
                }
                if (viewModel.canRemoveTarget(selected)) actions += rowWidget(getString(R.string.designer_remove)) {
                    viewModel.removeTarget(
                        selected
                    )
                }
                section(panel, getString(R.string.designer_actions), actions)
            }

            else -> face.layers.firstOrNull { it.id == selected }?.let { layer ->
                binding.sheetTitle.text = layerLabel(layer)
                buildLayerControls(panel, state, layer)
            }
        }
    }

    private fun distribute(key: String, horizontal: Boolean) {
        val canvas = canvases[currentPage] ?: return
        val width = canvas.clockWidth()
        val group = ItemGroups.itemId(key) ?: return
        if (width <= 0f) return
        val prefix = FaceComposer.prefix(group)
        val members = canvas.visibleRects().filterKeys { it.startsWith(prefix) }.entries
            .groupBy { ItemGrouping.memberOf(it.key) }
            .filterKeys { it != null }
            .map { (member, entries) ->
                member!! to RectF(
                    entries.minOf { it.value.left },
                    entries.minOf { it.value.top },
                    entries.maxOf { it.value.right },
                    entries.maxOf { it.value.bottom })
            }
            .sortedBy { (_, rect) -> if (horizontal) rect.left else rect.top }
        if (members.size < 3) {
            Toast.makeText(requireContext(), R.string.designer_distribute_hint, Toast.LENGTH_SHORT)
                .show()
            return
        }
        val start = if (horizontal) members.first().second.left else members.first().second.top
        val end = if (horizontal) members.last().second.right else members.last().second.bottom
        val sizes =
            members.sumOf { (_, rect) -> (if (horizontal) rect.width() else rect.height()).toDouble() }
                .toFloat()
        val gap = (end - start - sizes) / (members.size - 1)
        var cursor = start
        val offsets = members.associate { (member, rect) ->
            val origin = if (horizontal) rect.left else rect.top
            val delta = (cursor - origin) / width
            cursor += (if (horizontal) rect.width() else rect.height()) + gap
            member to if (horizontal) delta to 0f else 0f to delta
        }
        viewModel.distribute(key, offsets)
    }

    private fun addElement(kind: ElementKind) {
        if (kind == ElementKind.TEXT) {
            val input = EditText(requireContext()).apply { setText(R.string.designer_text_default) }
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.designer_content)
                .setView(FrameLayout(requireContext()).apply {
                    setPadding(dp(20), dp(8), dp(20), 0)
                    addView(input)
                })
                .setPositiveButton(AndroidR.string.ok) { _, _ ->
                    val text =
                        input.text.toString().ifBlank { getString(R.string.designer_text_default) }
                    viewModel.addElement(
                        CustomFaces.text(getString(R.string.designer_text), text),
                        null,
                        null
                    )
                }
                .setNegativeButton(AndroidR.string.cancel, null)
                .show()
            return
        }
        insertElement(kind, "")
    }

    private fun insertElement(kind: ElementKind, content: String) {
        val label = ADDABLE.firstOrNull { it.first == kind }?.third?.let(::getString) ?: kind.name
        val element = CustomFaces.element(label, ElementFactory.create(kind, content)) ?: return
        viewModel.addElement(element, FacePlacement(0.5f, 0.6f, FaceAlign.CENTER), null)
    }

    private val rowScroll = mutableMapOf<String, Int>()

    private fun hScroll(key: String, children: List<View>): View = hScrollView(children).apply {
        setOnScrollChangeListener { _, x, _, _, _ -> rowScroll[key] = x }
        val saved = rowScroll[key] ?: 0
        if (saved > 0) post { scrollTo(saved, 0) }
    }

    private fun hScrollView(children: List<View>): HorizontalScrollView {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            isBaselineAligned = false
            setPadding(dp(SHEET_PADDING_DP), dp(4), dp(SHEET_PADDING_DP - TILE_GAP_DP), dp(4))
        }
        children.forEach(row::addView)
        return HorizontalScrollView(requireContext()).apply {
            isHorizontalScrollBarEnabled = false
            addView(
                row,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
    }

    private fun tileBackground(selected: Boolean, radius: Int = 16): GradientDrawable =
        GradientDrawable().apply {
            cornerRadius = dp(radius).toFloat()
            setColor(resources.getColor(R.color.designer_tile, null))
            setStroke(
                dp(2),
                if (selected) resources.getColor(
                    R.color.designer_accent,
                    null
                ) else Color.TRANSPARENT
            )
        }

    private fun faceTile(
        face: ClockFace,
        selected: Boolean,
        width: Int,
        onClick: () -> Unit
    ): View {
        val frame = FrameLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(width, (width * 1.35f).roundToInt())
                .apply { marginEnd = dp(TILE_GAP_DP) }
            background = tileBackground(selected)
            clipToOutline = true
            setPadding(dp(10), dp(18), dp(10), dp(10))
            setOnClickListener { onClick() }
        }
        frame.addView(
            FaceClockView(requireContext(), face),
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        return frame
    }

    private fun applyFont(value: String) {
        val state = viewModel.uiState.value
        val face = state.face ?: return
        targetLayers(state, face).filter(::textual)
            .forEach { layer -> viewModel.updateLayer(layer.id, "font") { it.copy(font = value) } }
        refreshPanel()
    }

    private fun fontTile(
        typeface: Typeface?,
        selected: Boolean,
        width: Int,
        onClick: () -> Unit
    ): View = FontTileView(requireContext()).apply {
        layoutParams =
            LinearLayout.LayoutParams(width, dp(64)).apply { marginEnd = dp(TILE_GAP_DP) }
        background = tileBackground(selected)
        textSizePx = dp(28).toFloat()
        this.typeface = typeface
        setOnClickListener { onClick() }
    }

    private fun addFontTile(width: Int, onClick: () -> Unit): View =
        ImageView(requireContext()).apply {
            layoutParams =
                LinearLayout.LayoutParams(width, dp(64)).apply { marginEnd = dp(TILE_GAP_DP) }
            background = tileBackground(false)
            setImageResource(R.drawable.ic_designer_add)
            imageTintList = ColorStateList.valueOf(Color.WHITE)
            scaleType = ImageView.ScaleType.CENTER
            setOnClickListener { onClick() }
        }

    private fun effectTile(
        effect: FaceTextEffect,
        label: String,
        selected: Boolean,
        width: Int,
        onClick: () -> Unit
    ): View {
        val column = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply { marginEnd = dp(TILE_GAP_DP) }
            setOnClickListener { onClick() }
        }
        val preview = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(width, dp(72))
            gravity = Gravity.CENTER
            text = "21:09"
            textSize = 30f
            typeface = Typeface.DEFAULT_BOLD
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(0xFF3E3A8F.toInt(), 0xFF8E6FD0.toInt(), 0xFFE8848F.toInt())
            ).apply {
                cornerRadius = dp(16).toFloat()
                setStroke(
                    dp(2),
                    if (selected) resources.getColor(
                        R.color.designer_accent,
                        null
                    ) else Color.TRANSPARENT
                )
            }
            when (effect) {
                FaceTextEffect.SOLID -> setTextColor(Color.WHITE)
                FaceTextEffect.BLEND -> setTextColor(0xFFD8C6F5.toInt())
                FaceTextEffect.GLASS -> {
                    setTextColor(0x80FFFFFF.toInt())
                    setShadowLayer(dp(3).toFloat(), 0f, dp(1).toFloat(), Color.WHITE)
                }

                FaceTextEffect.OVERLAP -> setTextColor(0xFF2B1B3F.toInt())
            }
        }
        column.addView(preview)
        column.addView(TextView(requireContext()).apply {
            text = label
            setTextColor(0xCCFFFFFF.toInt())
            textSize = 13f
            setPadding(0, dp(8), 0, 0)
        })
        return column
    }

    private fun colorDot(color: Int, onClick: () -> Unit): View = View(requireContext()).apply {
        layoutParams = LinearLayout.LayoutParams(dp(48), dp(48)).apply { marginEnd = dp(14) }
        background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }
        setOnClickListener { onClick() }
    }

    private fun customColorDot(targets: List<FaceLayer>): View = ImageView(requireContext()).apply {
        layoutParams = LinearLayout.LayoutParams(dp(48), dp(48)).apply { marginEnd = dp(14) }
        setImageResource(R.drawable.ic_designer_colorize)
        imageTintList = ColorStateList.valueOf(0xFF3A2A2A.toInt())
        scaleType = ImageView.ScaleType.CENTER
        background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(0xFFE58F8F.toInt())
        }
        setOnClickListener {
            val current =
                targets.firstOrNull()?.let { FaceColors.resolve(requireContext(), it.color) }
                    ?: Color.WHITE
            val dialog =
                ColorPickerDialog.newBuilder().setColor(current).setShowAlphaSlider(true).create()
            dialog.setColorPickerDialogListener(object : ColorPickerDialogListener {
                override fun onColorSelected(dialogId: Int, color: Int) {
                    applyColor(targets, String.format(Locale.ROOT, "#%08X", color))
                }

                override fun onDialogDismissed(dialogId: Int) {}
            })
            dialog.show(parentFragmentManager, "designer_color")
        }
    }

    private fun sizeSlider(value: Float, from: Float, to: Float, onChange: (Float) -> Unit): View {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(SHEET_PADDING_DP), dp(16), dp(SHEET_PADDING_DP), dp(4))
        }
        row.addView(TextView(requireContext()).apply {
            text = "A"
            textSize = 15f
            setTextColor(0xCCFFFFFF.toInt())
        })
        row.addView(OplusSlider(requireContext()).apply {
            val scale = 100f
            valueFrom = (minOf(from, value) * scale).roundToInt().toFloat()
            valueTo = (maxOf(to, value) * scale).roundToInt().toFloat()
            stepSize = 1f
            setValues((value * scale).roundToInt().toFloat().coerceIn(valueFrom, valueTo))
            addOnSliderChangeListener(object : OplusSlider.OnSliderChangeListener {
                override fun onProgressChanged(slider: OplusSlider, fromUser: Boolean) {
                    if (fromUser) onChange(slider.values[0] / scale)
                }

                override fun onStartTrackingTouch(slider: OplusSlider) {
                    panelFrozen = true
                }

                override fun onStopTrackingTouch(slider: OplusSlider) {
                    panelFrozen = false
                    viewModel.endGesture()
                }
            })
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(TextView(requireContext()).apply {
            text = "A"
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xCCFFFFFF.toInt())
        })
        return row
    }

    private fun roundAction(icon: Int, label: String, onClick: () -> Unit): View {
        val column = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener { onClick() }
        }
        column.addView(ImageView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(dp(56), dp(56))
            setImageResource(icon)
            imageTintList = ColorStateList.valueOf(Color.WHITE)
            scaleType = ImageView.ScaleType.CENTER
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(resources.getColor(R.color.designer_pill, null))
            }
        })
        column.addView(TextView(requireContext()).apply {
            text = label
            gravity = Gravity.CENTER
            textSize = 12f
            minLines = 2
            maxLines = 2
            setTextColor(Color.WHITE)
            setPadding(dp(4), dp(8), dp(4), 0)
        })
        return column
    }

    private fun componentTile(glyph: String, label: String, onClick: () -> Unit): View {
        val column = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(0, dp(10), 0, dp(10))
            setOnClickListener { onClick() }
        }
        column.addView(TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(dp(64), dp(64))
            gravity = Gravity.CENTER
            text = glyph
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            background = tileBackground(false, 20)
        })
        column.addView(TextView(requireContext()).apply {
            text = label
            gravity = Gravity.CENTER
            textSize = 12f
            maxLines = 2
            setTextColor(0xCCFFFFFF.toInt())
            setPadding(0, dp(6), 0, 0)
        })
        return column
    }

    private fun section(panel: LinearLayout, title: String, widgets: List<View>) {
        if (widgets.isEmpty()) return
        panel.addView(CategoryWidget(requireContext()).apply { setTitle(title) })
        widgets.forEachIndexed { index, widget ->
            val position = when {
                widgets.size == 1 -> "full"
                index == 0 -> "top"
                index == widgets.lastIndex -> "bottom"
                else -> "middle"
            }
            when (widget) {
                is SwitchWidget -> widget.forcePosition(position)
                is SliderWidget -> widget.forcePosition(position)
                is ListWidget -> widget.forcePosition(position)
                is ColorPickerWidget -> widget.forcePosition(position)
            }
            panel.addView(widget)
        }
    }

    private fun rowWidget(title: String, onClick: () -> Unit): ListWidget =
        ListWidget(requireContext()).apply {
            setTitle(title)
            setOnRowClickListener { onClick() }
        }

    private fun listWidget(
        title: String,
        entries: List<String>,
        selected: Int,
        onSelect: (Int) -> Unit
    ): ListWidget = ListWidget(requireContext()).apply {
        setTitle(title)
        setEntries(
            entries.toTypedArray<CharSequence>(),
            entries.indices.map { it.toString() }.toTypedArray<CharSequence>()
        )
        if (selected >= 0) setSelectedValue(selected.toString())
        setOnSelectedListener { _, value -> onSelect(value.toString().toInt()) }
    }

    private fun switchWidget(
        title: String,
        checked: Boolean,
        onChange: (Boolean) -> Unit
    ): SwitchWidget = SwitchWidget(requireContext()).apply {
        setTitle(title)
        setSwitchChecked(checked)
        setSwitchChangeListener { _, value -> onChange(value) }
    }

    private fun imageFilterControls(layer: FaceLayer): List<View> {
        val controls = mutableListOf<View>()
        controls += listWidget(
            getString(R.string.albumart_filter_title),
            IMAGE_FILTERS.map { getString(it.second) },
            IMAGE_FILTERS.indexOfFirst { it.first == layer.imageFilter }
        ) { index ->
            viewModel.updateLayer(
                layer.id,
                "imageFilter"
            ) { it.copy(imageFilter = IMAGE_FILTERS[index].first) }
            refreshPanel()
        }
        if (layer.imageFilter == FaceImageFilter.BLUR || layer.imageFilter == FaceImageFilter.GRAY_BLUR) {
            controls += layerSlider(
                getString(R.string.albumart_blur_title),
                layer.imageBlur,
                10,
                100
            ) { value ->
                viewModel.updateLayer(layer.id, "imageBlur") { it.copy(imageBlur = value) }
            }
        }
        if (layer.imageFilter == FaceImageFilter.TINT) {
            controls += layerSlider(
                getString(R.string.albumart_tint_intensity_title),
                layer.imageTint,
                10,
                90
            ) { value ->
                viewModel.updateLayer(layer.id, "imageTint") { it.copy(imageTint = value) }
            }
        }
        return controls
    }

    private fun layerSlider(
        title: String,
        value: Int,
        from: Int,
        to: Int,
        onChange: (Int) -> Unit
    ): SliderWidget =
        SliderWidget(requireContext()).apply {
            setTitle(title)
            setSliderValueFrom(from)
            setSliderValueTo(to)
            setSliderStepSize(1)
            setSliderValue(value.coerceIn(from, to))
            setOnSliderTouchListener(object : OplusSlider.OnSliderChangeListener {
                override fun onProgressChanged(slider: OplusSlider, fromUser: Boolean) {}

                override fun onStartTrackingTouch(slider: OplusSlider) {
                    panelFrozen = true
                }

                override fun onStopTrackingTouch(slider: OplusSlider) {
                    panelFrozen = false
                    onChange(getSliderValue())
                }
            })
        }

    private fun styleSlider(key: StyleKey, label: Int, from: Float, to: Float): SliderWidget {
        val value = viewModel.value(key)
        val scale = when (key) {
            StyleKey.SIZE, StyleKey.OPACITY, StyleKey.EFFECT_TINT, StyleKey.TRACKING, StyleKey.LINE_HEIGHT -> 100f
            else -> 1f
        }
        val min = (minOf(from, value) * scale).roundToInt()
        val max = (maxOf(to, value) * scale).roundToInt()
        return SliderWidget(requireContext()).apply {
            setTitle(getString(label))
            setSliderValueFrom(min)
            setSliderValueTo(max)
            setSliderStepSize(1)
            setOutputScale(scale)
            if (scale != 1f) {
                setIsDecimalFormat(true)
                setDecimalFormat("#.##")
            }
            setSliderValue((value * scale).roundToInt().coerceIn(min, max))
            setOnSliderTouchListener(object : OplusSlider.OnSliderChangeListener {
                override fun onProgressChanged(slider: OplusSlider, fromUser: Boolean) {}

                override fun onStartTrackingTouch(slider: OplusSlider) {
                    panelFrozen = true
                }

                override fun onStopTrackingTouch(slider: OplusSlider) {
                    panelFrozen = false
                    viewModel.endGesture()
                }
            })
            setOnSliderChangeListener(object : OplusSlider.OnSliderChangeListener {
                override fun onProgressChanged(slider: OplusSlider, fromUser: Boolean) {
                    if (fromUser) viewModel.setValue(key, getSliderValue() / scale)
                }

                override fun onStartTrackingTouch(slider: OplusSlider) {}

                override fun onStopTrackingTouch(slider: OplusSlider) {}
            })
        }
    }

    private fun textField(title: String, value: String, onChange: (String) -> Unit): View {
        val column = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(12), dp(20), dp(12))
            setBackgroundResource(R.drawable.preference_background_center)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = dp(16)
                marginEnd = dp(16)
            }
        }
        column.addView(TextView(requireContext()).apply { text = title })
        column.addView(EditText(requireContext()).apply {
            setText(value)
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    onChange(s?.toString().orEmpty())
                }
            })
            setOnFocusChangeListener { _, focused -> panelFrozen = focused }
        })
        return column
    }

    private fun alignLabels(): List<String> =
        listOf(
            getString(R.string.designer_align_start),
            getString(R.string.designer_align_center),
            getString(R.string.designer_align_end)
        )

    private fun layerLabel(layer: FaceLayer): String {
        val base = when (layer.type) {
            FaceLayerType.TIME -> getString(R.string.designer_element_time)
            FaceLayerType.HOURS, FaceLayerType.HOUR_TENS, FaceLayerType.HOUR_ONES -> getString(R.string.designer_element_hours)
            FaceLayerType.MINUTES, FaceLayerType.MINUTE_TENS, FaceLayerType.MINUTE_ONES -> getString(
                R.string.designer_element_minutes
            )

            FaceLayerType.COLON -> ":"
            FaceLayerType.DATE -> getString(R.string.designer_element_date)
            FaceLayerType.HAND_HOUR, FaceLayerType.HAND_MINUTE -> getString(R.string.designer_element_hands)
            FaceLayerType.CARD -> getString(R.string.designer_element_card)
            FaceLayerType.GAUGE -> getString(R.string.designer_element_ring)
            FaceLayerType.IMAGE -> getString(R.string.designer_element_image)
            FaceLayerType.SPACER -> getString(R.string.designer_element_spacer)
            FaceLayerType.WEATHER_CARD -> getString(R.string.designer_element_weather)
            FaceLayerType.DEVICE_WIDGET -> getString(R.string.designer_element_device_widget)
            FaceLayerType.DEVICE_ITEM -> getString(R.string.designer_element_device_item)
            FaceLayerType.WIDGET_BIG -> getString(R.string.designer_element_widget_big)
            FaceLayerType.WIDGET_SMALL -> getString(R.string.designer_element_widget_small)
            FaceLayerType.TEXT -> if (layer.font.startsWith(SYMBOL_FONT)) getString(R.string.designer_element_icon) else getString(
                R.string.designer_text
            )

            else -> layer.type.name.lowercase(Locale.ROOT).replace('_', ' ')
        }
        val detail = layerDetail(layer) ?: layer.format.takeIf {
            it.isNotBlank() && layer.type == FaceLayerType.TEXT && !layer.font.startsWith(
                SYMBOL_FONT
            )
        }
        return if (detail != null) "$base · $detail" else base
    }

    private fun layerDetail(layer: FaceLayer): String? = when (layer.type) {
        FaceLayerType.WIDGET_BIG, FaceLayerType.WIDGET_SMALL -> widgetLabel(layer.format)
        FaceLayerType.DEVICE_ITEM -> WidgetFactory.fromConfigJson(
            layer.format,
            requireContext(),
            true
        )?.widgetName

        else -> null
    }

    private fun itemSummary(key: String): String? {
        val layers = viewModel.groupLayers(key)
        if (layers.isEmpty()) return null
        if (viewModel.isGroupItem(key)) {
            return layers.groupBy { ItemGrouping.memberOf(it.id) }.values
                .joinToString(", ") { member ->
                    member.map(::layerLabel).distinct().joinToString(" + ")
                }
                .ifEmpty { null }
        }
        return layers.singleOrNull()?.let(::layerDetail)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()

    companion object {
        private const val CUSTOM_APP_WIDGET = "customapp"
        private const val SUB_PAGE_TAG = "designer_sub_page"
        private const val WEATHER_SIZE_MIN = 13
        private const val WEATHER_SIZE_MAX = 24
        private const val WEATHER_TEXT_SIZE = 16
        private const val WEATHER_IMAGE_SIZE = 18
        private const val SHEET_NONE = 0
        private const val SHEET_STYLE = 1
        private const val SHEET_ADD = 2
        private const val SHEET_FACES = 3
        private const val SHEET_LAYERS = 4
        private const val SHEET_DEVICE_ITEMS = 5
        private const val SHEET_DEPTH = 6
        private const val DEPTH_OPACITY = "DWOpacity"
        private const val DEPTH_OPACITY_MIN = 25
        private const val DEPTH_OPACITY_MAX = 255
        private const val DEPTH_OPACITY_DEFAULT = 192
        private const val DEVICE_TILE_PADDING_DP = 4
        private const val DEVICE_TILE_GAP_DP = 4
        private val WIDGET_PERMISSIONS = mapOf(
            "CalendarWidget" to (Manifest.permission.READ_CALENDAR to R.string.calendar_permission_required),
            "StepsWidget" to (Manifest.permission.ACTIVITY_RECOGNITION to R.string.steps_permission_required),
            "ScreenTimeWidget" to (USAGE_ACCESS to R.string.usage_access_required),
            "DataUsageWidget" to (USAGE_ACCESS to R.string.usage_access_required)
        )
        private const val USAGE_ACCESS = "usage_access"
        private const val DEVICE_GRID_COLUMNS = 4
        private const val ORDER_EDGE = 1000
        private const val DESIGN_MIME = "application/json"
        private const val TOP_MARGIN_MIN = -100
        private const val TOP_MARGIN_MAX = 300
        private const val TOP_MARGIN_DEFAULT = 100
        private const val FACE_COLUMNS = 3
        private const val FACE_TILE_RATIO = 1.35f
        private const val SHEET_PEEK_HEIGHT = 0.5f
        private const val SHEET_MAX_HEIGHT = 0.75f
        private const val CARD_WIDTH = 0.72f
        private const val PAGE_GAP_DP = 14
        private const val SHEET_PADDING_DP = 20
        private const val TILE_GAP_DP = 12
        private const val SYMBOL_FONT = "res:symbols"
        private const val SYMBOL_FIRST = 0xE000
        private const val SYMBOL_LAST = 0xF8FF
        private const val ICON_COLUMNS = 6
        private const val ICON_GLYPH_RATIO = 0.5f

        private val PAGES = listOf(EditScope.AOD, EditScope.LARGE, EditScope.SMALL)
        private val PAGE_LABELS = listOf(
            R.string.designer_scope_aod,
            R.string.designer_scope_large,
            R.string.designer_scope_small
        )
        private const val START_PAGE = 1
        private val ALIGNS = listOf(FaceAlign.START, FaceAlign.CENTER, FaceAlign.END)

        private val COLOR_DOTS = listOf(
            "neutral1_50",
            "accent1_200",
            "accent2_200",
            "accent3_200",
            "#FFC9D9F8",
            "#FFB8E3F5",
            "#FFC7C2F2",
            "#FFE6C6F2",
            "#FFF6C1C1",
            "#FFF9DCC1",
            "#FFFFFFFF",
            "#FF1A1A1A"
        )

        private val ALIGNMENTS = listOf(
            Triple(
                FaceAlign.START,
                R.drawable.ic_designer_align_start,
                R.string.designer_position_left
            ),
            Triple(
                FaceAlign.CENTER,
                R.drawable.ic_designer_align_center,
                R.string.designer_position_center
            ),
            Triple(
                FaceAlign.END,
                R.drawable.ic_designer_align_end,
                R.string.designer_position_right
            )
        )

        private val TILE_EFFECTS = listOf(
            FaceTextEffect.SOLID to R.string.designer_effect_solid,
            FaceTextEffect.GLASS to R.string.designer_glass
        )

        private val AOD_STYLES = listOf(
            FaceSleepStyle.NONE to R.string.designer_aod_none,
            FaceSleepStyle.THIN to R.string.designer_aod_thin,
            FaceSleepStyle.OUTLINE to R.string.designer_aod_outline,
            FaceSleepStyle.THIN_OUTLINE to R.string.designer_aod_thin_outline,
            FaceSleepStyle.HIDE to R.string.designer_aod_hide
        )

        private val IMAGE_FILTERS = listOf(
            FaceImageFilter.NONE to R.string.albumart_filter_none,
            FaceImageFilter.GRAYSCALE to R.string.albumart_filter_grayscale,
            FaceImageFilter.TINT to R.string.albumart_filter_tint,
            FaceImageFilter.BLUR to R.string.albumart_filter_blur,
            FaceImageFilter.GRAY_BLUR to R.string.albumart_filter_grayblur
        )

        private val ADDABLE = listOf(
            Triple(ElementKind.TIME, "21:09", R.string.designer_element_time),
            Triple(ElementKind.HOURS, "21", R.string.designer_element_hours),
            Triple(ElementKind.MINUTES, "09", R.string.designer_element_minutes),
            Triple(ElementKind.DATE, "16", R.string.designer_element_date),
            Triple(ElementKind.TEXT, "Aa", R.string.designer_text),
            Triple(ElementKind.HANDS, "◷", R.string.designer_element_hands),
            Triple(ElementKind.ICON, "\u2605", R.string.designer_element_icon),
            Triple(ElementKind.CARD, "\u25A2", R.string.designer_element_card),
            Triple(ElementKind.IMAGE, "\u25A7", R.string.designer_element_image),
            Triple(ElementKind.WEATHER_CARD, "\u2600\ufe0e", R.string.designer_element_weather),
            Triple(ElementKind.DEVICE_WIDGET, "\u25A3", R.string.designer_element_device_widget),
            Triple(ElementKind.DEVICE_ITEM, "\u25A6", R.string.designer_element_device_item),
            Triple(ElementKind.WIDGET_BIG, "\u25AC", R.string.designer_element_widget_big),
            Triple(
                ElementKind.WIDGET_SMALL,
                "\u25CF\u25CF",
                R.string.designer_element_widget_small
            ),
        )
    }
}
