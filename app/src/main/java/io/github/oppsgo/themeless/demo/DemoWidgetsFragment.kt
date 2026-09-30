package io.github.oppsgo.themeless.demo

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import io.github.oppsgo.android.theme.ThemeManager
import io.github.oppsgo.android.theme.binding.ImageViewResourceBinding
import io.github.oppsgo.android.theme.binding.TextViewResourceBinding
import io.github.oppsgo.android.theme.ktx.themeAs
import io.github.oppsgo.android.theme.resource.ColorRef
import io.github.oppsgo.android.theme.resource.DrawableRef
import io.github.oppsgo.themeless.R

class DemoWidgetsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_demo_widgets, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.bindBg(R.id.demoWidgetsRoot, R.color.skin_page_bg)

        val icon = view.findViewById<ImageView>(R.id.themeSizedIcon)
        val binding = ThemeManager.get().obtain(icon) as ImageViewResourceBinding
        val size = (72 * resources.displayMetrics.density).toInt()
        binding.setImage(
            DrawableRef.of(R.drawable.mail_star_fill) { resolver ->
                val drawable = resolver.getDrawable(R.drawable.mail_star_fill)?.mutate() ?: return@of null
                drawable.setBounds(0, 0, size, size)
                drawable
            },
        )

        bindCompoundDrawableDemo(view)

        val dynamicBtn = view.findViewById<Button>(R.id.btnThemeDynamic)
        var useAlt = false
        dynamicBtn.setOnClickListener {
            useAlt = !useAlt
            // 默认：浅色字 + 按钮底；切换：强调色字 + 面板底（字色白↔强调色，变化明显）
            val text = if (useAlt) R.color.skin_accent else R.color.skin_button_text
            val bg = if (useAlt) R.color.skin_panel_bg else R.color.skin_button_bg
            dynamicBtn.themeAs<TextViewResourceBinding> {
                setTextColor(text)
                setBackground(bg)
            }
        }

        val rows = view.findViewById<LinearLayout>(R.id.themeExtraRows)
        view.findViewById<Button>(R.id.btnThemeUnmanaged).setOnClickListener {
            val created = TextView(requireContext()).apply {
                text = getString(R.string.theme_demo_unmanaged_line)
                textSize = 16f
                val pad = (8 * resources.displayMetrics.density).toInt()
                setPadding(0, pad, 0, 0)
            }
            rows.addView(created)
            TextViewResourceBinding.of(created)
                .setTextColor(ColorRef.of(R.color.skin_accent))
                .setBackground(
                    DrawableRef.of { resolver ->
                        GradientDrawable().apply {
                            cornerRadius = 15F
                            orientation = GradientDrawable.Orientation.LEFT_RIGHT
                            colors = intArrayOf(
                                resolver.getColor(R.color.skin_card_bg),
                                resolver.getColor(R.color.skin_panel_bg_blue),
                            )
                        }
                    },
                )
        }
    }

    /**
     * start 用 DrawableRef 跟肤；end 先手动挂上且不写入 Binding，切肤后应保留。
     * 「清空 start」演示 [DrawableRef.none] 显式清空，不影响未跟踪的 end。
     */
    private fun bindCompoundDrawableDemo(view: View) {
        val compound = view.findViewById<TextView>(R.id.themeCompoundMixed)
        val fixedEnd = ContextCompat.getDrawable(requireContext(), R.drawable.ic_settings)?.mutate()?.also {
            it.setTint(0xFF888888.toInt())
        }
        compound.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, fixedEnd, null)

        val compoundBinding = TextViewResourceBinding.of(compound)
            .attach() as TextViewResourceBinding
        compoundBinding
            .setTextColor(R.color.skin_text_primary)
            .setCompoundDrawablesRelativeWithIntrinsicBounds(
                DrawableRef.of(R.drawable.mail_star_fill),
                null,
                null,
                null,
            )

        view.findViewById<Button>(R.id.btnThemeCompoundClearStart).setOnClickListener {
            compoundBinding.setCompoundDrawablesRelativeWithIntrinsicBounds(
                DrawableRef.none(),
                null,
                null,
                null,
            )
        }
    }
}
