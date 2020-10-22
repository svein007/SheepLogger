package com.example.sheeptracker.ui.swiper

import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.sheeptracker.R
import com.example.sheeptracker.database.entities.Counter
import com.example.sheeptracker.databinding.SwiperFragmentBinding
import com.example.sheeptracker.ui.OnSwipeTouchListener
import com.example.sheeptracker.ui.addobservation.AddObservationViewModel
import com.example.sheeptracker.ui.herdobservationdetails.HerdObservationDetailsViewModel

class SwiperFragment : Fragment() {

    private lateinit var swiperViewModel: SwiperViewModel
    private lateinit var binding: SwiperFragmentBinding

    private lateinit var textToSpeech: TextToSpeech

    private var toast: Toast? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.swiper_fragment, container, false)

        val args = SwiperFragmentArgs.fromBundle(requireArguments())

        swiperViewModel = if (args.navFromFragment == "add") ViewModelProvider(
            requireActivity())[AddObservationViewModel::class.java] else ViewModelProvider(
            requireActivity())[HerdObservationDetailsViewModel::class.java]

        textToSpeech = TextToSpeech(requireContext()){}

        binding.root.setOnTouchListener(object : OnSwipeTouchListener(requireContext()){
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onSwipeDown() {
                getCurrentCounter()?.dec()
                updateTypeCountText()
                echoSelectedCount()
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onSwipeUp() {
                getCurrentCounter()?.inc()
                updateTypeCountText()
                echoSelectedCount()
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onSwipeLeft() {
                swiperViewModel.countType.value = swiperViewModel.countType.value?.next()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    updateTypeCountText()
                    echoSelectedCountType()
                    echoSelectedCount()
                }
            }

            override fun onSwipeRight() {
                swiperViewModel.countType.value = swiperViewModel.countType.value?.prev()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    updateTypeCountText()
                    echoSelectedCountType()
                    echoSelectedCount()
                }
            }

        })

        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            echoSelectedCountType()
            echoSelectedCount()
        }

        updateTypeCountText()
    }

    override fun onDestroy() {
        super.onDestroy()
        toast?.cancel()
        textToSpeech.stop()
    }

    private fun getCurrentCounter(): Counter? {
        return swiperViewModel.counters.value?.firstOrNull { c -> c.counterType == swiperViewModel.countType.value }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun echoSelectedCountType() {
        val textToSpeak = "${swiperViewModel.countType.value?.str(requireContext())} Selected"
        showToast(textToSpeak)
        textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_ADD, null, null)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun echoSelectedCount() {
        val textToSpeak = "${getCurrentCounter()?.counterValue ?: "--"} ${swiperViewModel.countType.value?.str(requireContext())}"

        showToast(textToSpeak)
        textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun showToast(text: String) {
        toast?.cancel()
        toast = Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT)
        toast?.show()
    }

    private fun updateTypeCountText() {
        val counterValue = swiperViewModel.counters.value?.firstOrNull { counter -> counter.counterType == swiperViewModel.countType.value }?.counterValue
        binding.swipeText.text = "${swiperViewModel.countType.value?.str(requireContext())}: ${counterValue}"

        val nextTypeText = swiperViewModel.countType.value?.next()?.str(requireContext())
        binding.nextTextView.text = nextTypeText

        val prevTypeText = swiperViewModel.countType.value?.prev()?.str(requireContext())
        binding.prevTextView.text = prevTypeText

    }

}