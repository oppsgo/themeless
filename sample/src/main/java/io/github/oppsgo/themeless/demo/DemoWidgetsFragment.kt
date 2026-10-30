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
import io.github.oppsgo.android.theme.ThemeKey
import io.github.oppsgo.android.theme.ThemeManager
import io.github.oppsgo.android.theme.binding.ImageViewBinding
import io.github.oppsgo.android.theme.binding.TextViewBinding
import io.github.oppsgo.android.theme.resource.ColorRef
import io.github.oppsgo.android.theme.resource.DrawableRef
import io.github.oppsgo.android.theme.resource.ResourceValue
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
                val drawable = resolver.getDrawable(R.drawable.mail_star_fill)?.mutate() ?: return@of ResourceValue.empty()
                drawable.setBounds(0, 0, size, size)
                ResourceValue.of(drawable)
            },
        )

        bindThemeKeyDemo(view)
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
            // new 出来未 inflate：of 临时 Binding 不挂 tag；setXxx 用当前 ResourceResolver 写一次，之后切肤不再跟。
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
                    ResourceValue.of(
                        GradientDrawable().apply {
                            cornerRadius = 15f
                            orientation = GradientDrawable.Orientation.LEFT_RIGHT
                            colors = intArrayOf(
                                resolver.getColor(R.color.skin_card_bg),
                                resolver.getColor(R.color.skin_panel_bg_blue),
                            )
                        },
                    )
                },
            )
        }
    }

    /**
     * 非 [android.R.attr] 槽位：用 [ThemeKey.of] 业务 token 登记色条，
     * 切肤走同表刷新；按钮只 [io.github.oppsgo.android.theme.IViewBinding.set] 换 ResourceRef。
     */
    private fun bindThemeKeyDemo(view: View) {
        val bar = view.findViewById<View>(R.id.themeKeyAccentBar)
        val binding = ThemeManager.get().of(bar)
        binding.bind(KEY_ACCENT_BAR) { value ->
            bar.setBackgroundColor(value.require())
        }
        binding.set(KEY_ACCENT_BAR, ColorRef.of(R.color.skin_accent))

        var usePanel = false
        view.findViewById<Button>(R.id.btnThemeKeyToggle).setOnClickListener {
            usePanel = !usePanel
            val color = if (usePanel) R.color.skin_panel_bg else R.color.skin_accent
            binding.set(KEY_ACCENT_BAR, ColorRef.of(color))
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
     * 靠 [io.github.oppsgo.android.theme.OnViewThemeUpdateListener] 整段重建。
     * 登记后 [io.github.oppsgo.android.theme.IViewBinding.notifyViewThemeUpdate] 补一次首屏。
     */
    private fun bindSpannableDemo(view: View) {
        val spannable = view.findViewById<TextView>(R.id.themeSpannable)
        val spannableBinding = ThemeManager.get().of(spannable, TextViewBinding::class.java)
        spannableBinding.setTextColor(R.color.skin_text_primary)
        spannableBinding.setOnViewThemeUpdateListener { _, resolver ->
            spannable.text = buildThemedSpannable(resolver)
        }
        spannableBinding.notifyViewThemeUpdate()
    }

    companion object {
        /** 业务侧 ThemeKey：与 R.attr 隔离，不进 XML 读取。 */
        private val KEY_ACCENT_BAR: ThemeKey = ThemeKey.of("accentBar")
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
