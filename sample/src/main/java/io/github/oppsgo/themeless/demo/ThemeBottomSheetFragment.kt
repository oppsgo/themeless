package io.github.oppsgo.themeless.demo

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.oppsgo.android.theme.ThemeKey
import io.github.oppsgo.android.theme.ThemeManager
import io.github.oppsgo.android.theme.binding.ViewBinding
import io.github.oppsgo.android.theme.resource.ColorRef
import io.github.oppsgo.themeless.R

/**
 * 底部弹出层：独立窗口内容根会登记进 ThemeDelegate，顶部切肤应一并刷新。
 */
class ThemeBottomSheetFragment : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val sheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                ?: return@setOnShowListener
            sheet.background = Color.TRANSPARENT.toDrawable()
            BottomSheetBehavior.from(sheet).apply {
                skipCollapsed = true
                state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // 走 Activity inflater，保证 ThemeDelegate Factory 能挂上并登记浮层根
        return requireActivity().layoutInflater.inflate(R.layout.dialog_theme_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val accentBar = view.findViewById<View>(R.id.themeBottomSheetAccentBar)
        val binding = ThemeManager.get().of(accentBar)
        binding.bind(KEY_SHEET_ACCENT) { value ->
            accentBar.setBackgroundColor(value.require())
        }
        binding.set(KEY_SHEET_ACCENT, ColorRef.of(R.color.skin_accent))

        var usePanel = false
        view.findViewById<Button>(R.id.btnBottomSheetToggleBar).setOnClickListener {
            usePanel = !usePanel
            val color = if (usePanel) R.color.skin_panel_bg else R.color.skin_accent
            binding.set(KEY_SHEET_ACCENT, ColorRef.of(color))
        }

        val seekLabel = view.findViewById<TextView>(R.id.themeBottomSheetSeekLabel)
        val seek = view.findViewById<SeekBar>(R.id.themeBottomSheetSeek)
        fun syncSeekLabel(progress: Int) {
            seekLabel.text = getString(R.string.theme_demo_bottom_sheet_seek_format, progress)
        }
        syncSeekLabel(seek.progress)
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                syncSeekLabel(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        val rows = view.findViewById<LinearLayout>(R.id.themeBottomSheetRows)
        rows.removeAllViews()
        SHEET_ROWS.forEachIndexed { index, spec ->
            val row = requireActivity().layoutInflater.inflate(R.layout.item_theme_row, rows, false)
            row.findViewById<TextView>(R.id.itemTitle).setText(spec.titleRes)
            row.findViewById<TextView>(R.id.itemSubtitle).setText(spec.subtitleRes)
            (ThemeManager.get().of(row) as? ViewBinding)
                ?.setBackground(ColorRef.of(spec.bgColorRes))
            row.findViewById<View>(R.id.itemAccent)?.let { accent ->
                (ThemeManager.get().of(accent) as? ViewBinding)
                    ?.setBackground(ColorRef.of(spec.accentColorRes))
            }
            if (index > 0) {
                val lp = row.layoutParams as LinearLayout.LayoutParams
                lp.topMargin = (6 * resources.displayMetrics.density).toInt()
                row.layoutParams = lp
            }
            rows.addView(row)
        }

        view.findViewById<Button>(R.id.btnBottomSheetNested).setOnClickListener {
            ThemeLayerDialogFragment.show(
                parentFragmentManager,
                R.string.theme_demo_dialog_nested_message,
            )
        }
        view.findViewById<Button>(R.id.btnBottomSheetClose).setOnClickListener {
            dismissAllowingStateLoss()
        }
    }

    companion object {
        private val KEY_SHEET_ACCENT: ThemeKey = ThemeKey.of("bottomSheetAccent")

        /** 三行底色互不相同（相对 sheet 的 page 底），切肤时层次差要看得出来。 */
        private val SHEET_ROWS = listOf(
            SheetRow(
                R.string.theme_demo_bottom_sheet_row_a,
                R.string.theme_demo_bottom_sheet_row_a_sub,
                R.color.skin_panel_bg,
                R.color.skin_accent,
            ),
            SheetRow(
                R.string.theme_demo_bottom_sheet_row_b,
                R.string.theme_demo_bottom_sheet_row_b_sub,
                R.color.skin_card_bg,
                R.color.skin_text_secondary,
            ),
            SheetRow(
                R.string.theme_demo_bottom_sheet_row_c,
                R.string.theme_demo_bottom_sheet_row_c_sub,
                R.color.skin_button_bg,
                R.color.skin_button_text,
            ),
        )

        fun show(manager: FragmentManager) {
            ThemeBottomSheetFragment().show(manager, "theme-bottom-sheet")
        }
    }

    private data class SheetRow(
        val titleRes: Int,
        val subtitleRes: Int,
        val bgColorRes: Int,
        val accentColorRes: Int,
    )
}
