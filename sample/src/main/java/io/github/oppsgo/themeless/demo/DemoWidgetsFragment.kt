package io.github.oppsgo.themeless.demo

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import io.github.oppsgo.android.theme.ResourceResolver
import io.github.oppsgo.android.theme.ThemeManager
import io.github.oppsgo.android.theme.binding.ImageViewBinding
import io.github.oppsgo.android.theme.binding.TextViewBinding
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
        val binding = ThemeManager.get().of(icon, ImageViewBinding::class.java)
        val size = (72 * resources.displayMetrics.density).toInt()
        binding.setImage(
            DrawableRef.of(R.drawable.mail_star_fill) { resolver ->
                val drawable = resolver.getDrawable(R.drawable.mail_star_fill)?.mutate() ?: return@of null
                drawable.setBounds(0, 0, size, size)
                drawable
            },
        )

        bindCompoundDrawableDemo(view)
        bindSpannableDemo(view)

        val dynamicBtn = view.findViewById<Button>(R.id.btnThemeDynamic)
        var useAlt = false
        dynamicBtn.setOnClickListener {
            useAlt = !useAlt
            // 默认：浅色字 + 按钮底；切换：强调色字 + 面板底（字色白↔强调色，变化明显）
            val text = if (useAlt) R.color.skin_accent else R.color.skin_button_text
            val bg = if (useAlt) R.color.skin_panel_bg else R.color.skin_button_bg
            val textBinding = ThemeManager.get().of(dynamicBtn, TextViewBinding::class.java)
            textBinding.setTextColor(text)
            textBinding.setBackground(bg)
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
            val createdBinding = ThemeManager.get().of(created, TextViewBinding::class.java)
            createdBinding.setTextColor(ColorRef.of(R.color.skin_accent))
            createdBinding.setBackground(
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
     * start / end 都写入 Binding 跟肤；「清空 start」用 [DrawableRef.none] 只清 start，end 仍保留。
     */
    private fun bindCompoundDrawableDemo(view: View) {
        val compound = view.findViewById<TextView>(R.id.themeCompoundMixed)
        val endRef = DrawableRef.of(R.drawable.ic_settings)

        val compoundBinding = ThemeManager.get().of(compound, TextViewBinding::class.java)
        compoundBinding.setTextColor(R.color.skin_text_primary)
        compoundBinding.setCompoundDrawablesRelativeWithIntrinsicBounds(
            DrawableRef.of(R.drawable.mail_star_fill),
            null,
            endRef,
            null,
        )

        view.findViewById<Button>(R.id.btnThemeCompoundClearStart).setOnClickListener {
            compoundBinding.setCompoundDrawablesRelativeWithIntrinsicBounds(
                DrawableRef.none(),
                null,
                endRef,
                null,
            )
        }
    }

    /**
     * 一段文字里混了多种皮肤色，单个 textColor 表达不了：
     * 普通段走 [TextViewBinding.setTextColor] 跟肤，强调段 / 次要段的 span
     * 靠 [io.github.oppsgo.android.theme.OnViewThemeUpdateListener] 在每次主题刷新后整段重建。
     * 回调语义是「一次刷新发生了」，set 时不立即触发，所以首屏要自己建一次。
     */
    private fun bindSpannableDemo(view: View) {
        val spannable = view.findViewById<TextView>(R.id.themeSpannable)
        val spannableBinding = ThemeManager.get().of(spannable, TextViewBinding::class.java)
        spannableBinding.setTextColor(R.color.skin_text_primary)
        spannableBinding.setOnViewThemeUpdateListener { _, resolver ->
            spannable.text = buildThemedSpannable(resolver)
        }
        // 首屏内容归调用方建：已安装 resolver 就直接建，否则等首次刷新触发的回调
        ThemeManager.get().getResolver(requireContext())?.let { resolver ->
            spannable.text = buildThemedSpannable(resolver)
        }
    }

    private fun buildThemedSpannable(resolver: ResourceResolver): CharSequence {
        val normal = getString(R.string.theme_demo_spannable_normal)
        val accent = getString(R.string.theme_demo_spannable_accent)
        val secondary = getString(R.string.theme_demo_spannable_secondary)
        val full = normal + accent + secondary
        return SpannableString(full).apply {
            setSpan(
                ForegroundColorSpan(resolver.getColor(R.color.skin_accent)),
                normal.length,
                normal.length + accent.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
            setSpan(
                ForegroundColorSpan(resolver.getColor(R.color.skin_text_secondary)),
                normal.length + accent.length,
                full.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
    }
}
