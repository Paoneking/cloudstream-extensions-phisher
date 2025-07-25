package com.phisher98.settings

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.phisher98.BuildConfig
import com.phisher98.StreamPlayPlugin
import androidx.core.content.edit
import com.lagradost.cloudstream3.CommonActivity.showToast
import com.phisher98.*

class ToggleFragment(
    private val plugin: StreamPlayPlugin,
    private val sharedPref: SharedPreferences
) : BottomSheetDialogFragment() {

    private val res: Resources = plugin.resources ?: throw Exception("Unable to access plugin resources")

    @SuppressLint("DiscouragedApi")
    private fun getLayout(name: String, inflater: LayoutInflater, container: ViewGroup?): View {
        val id = res.getIdentifier(name, "layout", BuildConfig.LIBRARY_PACKAGE_NAME)
        if (id == 0) throw Resources.NotFoundException("Layout $name not found.")
        val layout = res.getLayout(id)
        return inflater.inflate(layout, container, false)
    }

    private fun getDrawable(name: String): Drawable {
        val id = res.getIdentifier(name, "drawable", BuildConfig.LIBRARY_PACKAGE_NAME)
        return res.getDrawable(id, null)
            ?: throw Resources.NotFoundException("Drawable $name not found.")
    }

    private fun <T : View> View.findView(name: String): T {
        val id = res.getIdentifier(name, "id", BuildConfig.LIBRARY_PACKAGE_NAME)
        if (id == 0) throw Resources.NotFoundException("View ID $name not found.")
        return this.findViewById(id)
    }

    private fun View.makeTvCompatible() {
        val id = res.getIdentifier("outline", "drawable", BuildConfig.LIBRARY_PACKAGE_NAME)
        this.background = res.getDrawable(id, null)
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = getLayout("fragment_toggle_extensions", inflater, container)
        val extensionList = root.findView<LinearLayout>("toggle_list_container")

        val apis = listOf(
            StreamPlay(sharedPref),
            StreamPlayLite(),
            StreamPlayTorrent(),
            StreamPlayAnime(),
            StreamplayTorrentAnime()
        )

        val savedKey = "enabled_plugins_saved"
        val savedSet = sharedPref.getStringSet(savedKey, null)
        val defaultEnabled = apis.map { it.name }.toSet()

        val currentSet = savedSet?.toSet() ?: defaultEnabled

        val outlineDrawable = getDrawable("outline")

        for (api in apis) {
            val toggleItem = getLayout("list_toggle_item", inflater, container)
            val label = toggleItem.findView<TextView>("toggle_title")
            val toggleSwitch = toggleItem.findView<Switch>("toggle_switch")

            label.text = api.name
            toggleSwitch.isChecked = currentSet.contains(api.name)

            // Set background if checked initially
            if (toggleSwitch.isChecked) {
                toggleItem.background = outlineDrawable
            }

            // Update background dynamically on toggle
            toggleSwitch.setOnCheckedChangeListener { _, isChecked ->
                toggleItem.background = if (isChecked) outlineDrawable else null
            }

            extensionList.addView(toggleItem)
        }

        val saveBtn = root.findView<ImageView>("saveIcon")
        saveBtn.setImageDrawable(getDrawable("save_icon"))
        saveBtn.makeTvCompatible()
        saveBtn.setOnClickListener {
            val enabledPluginNames = mutableListOf<String>()
            for (i in 0 until extensionList.childCount) {
                val toggleItem = extensionList.getChildAt(i)
                val label = toggleItem.findViewById<TextView>(
                    res.getIdentifier("toggle_title", "id", BuildConfig.LIBRARY_PACKAGE_NAME)
                )
                val toggleSwitch = toggleItem.findViewById<Switch>(
                    res.getIdentifier("toggle_switch", "id", BuildConfig.LIBRARY_PACKAGE_NAME)
                )
                if (toggleSwitch.isChecked) {
                    enabledPluginNames.add(label.text.toString())
                }
            }

            sharedPref.edit {
                putStringSet(savedKey, enabledPluginNames.toSet()).commit()
            }
            showToast("Settings saved. Please restart the app to apply changes.")
            dismiss()
        }

        return root
    }
}
