package io.github.oppsgo.themeless.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import io.github.oppsgo.themeless.R

/** ProgressBar / SeekBar 等平台控件 Binding 演示。 */
class DemoExtrasFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_demo_extras, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val seek = view.findViewById<SeekBar>(R.id.demoSeekBar)
        val value = view.findViewById<TextView>(R.id.demoSeekValue)
        fun paintValue(progress: Int) {
            value.text = getString(R.string.demo_extras_seek_value_format, progress)
        }
        paintValue(seek.progress)
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                paintValue(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
    }
}
