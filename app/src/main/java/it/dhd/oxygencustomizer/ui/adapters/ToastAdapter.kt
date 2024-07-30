package it.dhd.oxygencustomizer.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import it.dhd.oxygencustomizer.OxygenCustomizer
import it.dhd.oxygencustomizer.R
import it.dhd.oxygencustomizer.databinding.ViewToastFrameBinding
import it.dhd.oxygencustomizer.ui.models.ToastModel
import it.dhd.oxygencustomizer.utils.Constants.Preferences.SELECTED_TOAST_FRAME
import it.dhd.oxygencustomizer.utils.Prefs

class ToastAdapter (
    var context: Context,
    private var itemList: ArrayList<ToastModel>,
    private var toastClick: OnToastClick
) : RecyclerView.Adapter<ToastAdapter.ViewHolder>() {

    private var selected = Prefs.getInt(SELECTED_TOAST_FRAME, -1)
    private var mContext = OxygenCustomizer.getAppContext()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ViewToastFrameBinding =
            ViewToastFrameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        if (selected != -1) {
            itemList[selected].isSelected = true
        } else {
            itemList[0].isSelected = true
        }
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = itemList[holder.bindingAdapterPosition]
        holder.binding.styleName.text = model.title
        holder.binding.toastContainer.background = ContextCompat.getDrawable(mContext, model.style)
        if (model.isSelected) {
            holder.binding.styleName.setTextColor(
                mContext.resources.getColor(
                    R.color.colorAccent,
                    mContext.theme
                )
            )
        } else {
            holder.binding.styleName.setTextColor(
                mContext.resources.getColor(
                    R.color.textColorSecondary,
                    mContext.theme
                )
            )
        }
        holder.binding.listItemToast.setOnClickListener {
            toastClick.onToastClick(holder.bindingAdapterPosition, model)
        }
    }

    fun notifyChange() {
        refresh(false)
        selected = Prefs.getInt(SELECTED_TOAST_FRAME, -1)
        refresh(true)
    }

    private fun refresh(select: Boolean) {
        if (selected != -1) {
            itemList[selected].isSelected = select
            notifyItemChanged(selected)
        } else {
            itemList[0].isSelected = select
            notifyItemChanged(0)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(val binding: ViewToastFrameBinding) : RecyclerView.ViewHolder(binding.getRoot()) {
    }

    /**
     * Interface for the click on the item
     */
    interface OnToastClick {
        fun onToastClick(position: Int, item: ToastModel)
    }

}