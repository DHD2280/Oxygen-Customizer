package it.dhd.oxygencustomizer.ui.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import it.dhd.oxygencustomizer.R
import it.dhd.oxygencustomizer.databinding.FragmentCropImageViewBinding
import it.dhd.oxygencustomizer.ui.base.BaseFragment
import it.dhd.oxygencustomizer.utils.AppUtils

class FragmentCropImage :
    BaseFragment(),
    CropImageView.OnSetImageUriCompleteListener,
    CropImageView.OnCropImageCompleteListener {
    private var _binding: FragmentCropImageViewBinding? = null
    private val binding get() = _binding!!
    private val aspectRatio: Pair<Int, Int>? = null

    private var options: CropImageOptions? = null
    private val openPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        binding.cropImageView.setImageUriAsync(uri)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCropImageViewBinding.inflate(layoutInflater, container, false)
        if (arguments != null) {
            options = arguments?.getParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.cropImageView.setOnSetImageUriCompleteListener(null)
        binding.cropImageView.setOnCropImageCompleteListener(null)
        _binding = null
    }

    override fun getTitle(): String {
        return getString(R.string.pick_image_chooser_title)
    }

    override fun backButtonEnabled(): Boolean {
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setMenu()
        setOptions()

        binding.cropImageView.setOnSetImageUriCompleteListener(this)
        binding.cropImageView.setOnCropImageCompleteListener(this)

        binding.searchImage.setOnClickListener {
            if (AppUtils.hasStoragePermission())
                openPicker.launch("image/*")
            else
                AppUtils.requestStoragePermission(requireContext())
        }

        binding.reset.setOnClickListener {
            binding.cropImageView.resetCropRect()
        }
    }

    private fun setMenu() {
        val menuHost: MenuHost = requireActivity()
        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.image_pick_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
                R.id.main_action_crop -> {
                    binding.cropImageView.croppedImageAsync()
                    true
                }
                R.id.main_action_rotate -> {
                    binding.cropImageView.rotateImage(90)
                    true
                }
                R.id.main_action_flip_horizontally -> {
                    binding.cropImageView.flipImageHorizontally()
                    true
                }
                R.id.main_action_flip_vertically -> {
                    binding.cropImageView.flipImageVertically()
                    true
                }
                else -> false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onSetImageUriComplete(view: CropImageView, uri: Uri, error: Exception?) {
        if (error != null) {
            Toast.makeText(activity, "Image load failed: " + error.message, Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onCropImageComplete(view: CropImageView, result: CropImageView.CropResult) {
        if (result.error == null) {
            val imageBitmap = if (binding.cropImageView.cropShape == CropImageView.CropShape.OVAL) {
                result.bitmap?.let(CropImage::toOvalBitmap)
            } else {
                result.bitmap
            }
            val resultBundle = Bundle().apply {
                putString(DATA_FILE_URI, result.uriContent.toString())
            }
            setFragmentResult(DATA_CROP_KEY, resultBundle)
            activity?.supportFragmentManager!!.popBackStack()
        } else {
            Toast
                .makeText(activity, "Crop failed: ${result.error?.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun setOptions() {
        binding.cropImageView.setImageCropOptions(options!!)
    }

    companion object {

        const val DATA_CROP_KEY = "cropRequestKey"
        const val DATA_FILE_URI = "fileUri"
    }

}