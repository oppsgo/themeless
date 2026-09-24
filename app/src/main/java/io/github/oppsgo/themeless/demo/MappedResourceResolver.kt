package io.github.oppsgo.themeless.demo

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.annotation.AnyRes
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import io.github.oppsgo.android.theme.ResourceResolver
import io.github.oppsgo.android.theme.ThemeViewCompat
import io.github.oppsgo.themeless.R

/**
 * 自定义主题写法：在现有 [ResourceResolver] 上把颜色 id 重映射到另一套**固定**资源。
 * Demo 里「晴空蓝」只存在于 values/，与日夜无关，不要再包一层 DayNight。
 */
class MappedResourceResolver(
    private val delegate: ResourceResolver,
    private val colorMap: Map<Int, Int>,
) : ResourceResolver {

    @ColorRes
    private fun mapColor(@ColorRes id: Int): Int = colorMap[id] ?: id

    override fun getColor(@ColorRes id: Int): Int = delegate.getColor(mapColor(id))

    override fun getColorStateList(@ColorRes id: Int): ColorStateList? =
        delegate.getColorStateList(mapColor(id))

    override fun getDrawable(@AnyRes id: Int): Drawable? {
        val mapped = colorMap[id]
        return if (mapped != null) {
            delegate.getDrawable(mapped)
        } else {
            delegate.getDrawable(id)
        }
    }

    override fun getDimension(@DimenRes id: Int): Float = delegate.getDimension(id)

    override fun getDimensionPixelSize(@DimenRes id: Int): Int = delegate.getDimensionPixelSize(id)

    override fun getResources(): Resources = delegate.getResources()

    override fun getViewCompat(): ThemeViewCompat = delegate.viewCompat

    companion object {
        /** 晴空蓝：把常规 skin 色映射到 _blue 变体。 */
        fun skyBlue(delegate: ResourceResolver): MappedResourceResolver {
            return MappedResourceResolver(
                delegate,
                mapOf(
                    R.color.skin_page_bg to R.color.skin_page_bg_blue,
                    R.color.skin_panel_bg to R.color.skin_panel_bg_blue,
                    R.color.skin_card_bg to R.color.skin_card_bg_blue,
                    R.color.skin_text_primary to R.color.skin_text_primary_blue,
                    R.color.skin_text_secondary to R.color.skin_text_secondary_blue,
                    R.color.skin_accent to R.color.skin_accent_blue,
                    R.color.skin_divider to R.color.skin_divider_blue,
                    R.color.skin_button_bg to R.color.skin_button_bg_blue,
                    R.color.skin_button_text to R.color.skin_button_text_blue,
                ),
            )
        }
    }
}
