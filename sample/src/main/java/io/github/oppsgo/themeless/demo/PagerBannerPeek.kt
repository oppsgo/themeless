package io.github.oppsgo.themeless.demo

import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2

/** Banner：左右 peek，邻页露出一截。 */
internal fun ViewPager.setupBannerPeek(peekDp: Float = 48f, gapDp: Float = 12f) {
    val density = resources.displayMetrics.density
    val peek = (peekDp * density).toInt()
    val gap = (gapDp * density).toInt()
    clipToPadding = false
    setPadding(peek, 0, peek, 0)
    pageMargin = gap
    offscreenPageLimit = 2
}

/** Banner：左右 peek（改内部 RecyclerView padding + MarginPageTransformer）。 */
internal fun ViewPager2.setupBannerPeek(peekDp: Float = 48f, gapDp: Float = 12f) {
    val density = resources.displayMetrics.density
    val peek = (peekDp * density).toInt()
    val gap = (gapDp * density).toInt()
    offscreenPageLimit = 2
    setPageTransformer(MarginPageTransformer(gap))
    post {
        val rv = getChildAt(0) as? RecyclerView ?: return@post
        rv.clipToPadding = false
        rv.setPadding(peek, 0, peek, 0)
    }
}
