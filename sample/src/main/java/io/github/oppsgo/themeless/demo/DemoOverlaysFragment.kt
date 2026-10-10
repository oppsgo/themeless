package io.github.oppsgo.themeless.demo

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import io.github.oppsgo.themeless.R

class DemoOverlaysFragment : Fragment() {

    private var popup: PopupWindow? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_demo_overlays, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.bindBg(R.id.demoOverlaysRoot, R.color.skin_page_bg)

        view.findViewById<Button>(R.id.btnThemeDialog).setOnClickListener {
            ThemeLayerDialogFragment.show(
                parentFragmentManager,
                R.string.theme_demo_dialog_message,
            )
        }

        val popupAnchor = view.findViewById<Button>(R.id.btnThemePopup)
        popupAnchor.setOnClickListener {
            popup?.dismiss()
            val content = layoutInflater.inflate(R.layout.popup_theme_layer, null)
            val window = PopupWindow(
                content,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true,
            )
            window.isOutsideTouchable = true
            window.elevation = 8f * resources.displayMetrics.density
            window.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            popup = window
            window.setOnDismissListener { if (popup === window) popup = null }
            val titleBar: View =
                requireActivity().findViewById<View>(R.id.themeTitleBar) ?: popupAnchor
            val loc = IntArray(2)
            titleBar.getLocationInWindow(loc)
            val y = loc[1] + titleBar.height
            window.showAtLocation(titleBar, Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, y)
        }
    }

    override fun onDestroyView() {
        popup?.dismiss()
        popup = null
        super.onDestroyView()
    }
}
