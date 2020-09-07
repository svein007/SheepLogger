package com.example.osmdroidexample.ui.swiper

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
import com.example.osmdroidexample.R
import com.example.osmdroidexample.database.entities.Counter
import com.example.osmdroidexample.databinding.SwiperFragmentBinding
import com.example.osmdroidexample.ui.OnSwipeTouchListener
import com.example.osmdroidexample.ui.addobservation.AddObservationViewModel

class SwiperFragment : Fragment() {

    private lateinit var addObsViewModel: AddObservationViewModel
    private lateinit var binding: SwiperFragmentBinding

    private lateinit var textToSpeech: TextToSpeech

    private var toast: Toast? = null

    private var countType = Counter.CountType.SHEEP

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.swiper_fragment, container, false)

        addObsViewModel = ViewModelProvider(
            requireActivity())[AddObservationViewModel::class.java]

        textToSpeech = TextToSpeech(requireContext()){}

        binding.root.setOnTouchListener(object : OnSwipeTouchListener(requireContext()){
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onSwipeDown() {
                getCurrentCounter()?.dec()
                echoSelectedCount()
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onSwipeUp() {
                getCurrentCounter()?.inc()
                echoSelectedCount()
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onSwipeLeft() {
                countType = countType.next()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    echoSelectedCountType()
                    echoSelectedCount()
                }
            }

            override fun onSwipeRight() {
                countType = countType.prev()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
    }

    override fun onDestroy() {
        super.onDestroy()
        toast?.cancel()
    }

    private fun getCurrentCounter(): Counter? {
        return addObsViewModel.counters.value?.firstOrNull { c -> c.counterType == countType }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun echoSelectedCountType() {
        val textToSpeak = "${countType.str(requireContext())} Selected"
        showToast(textToSpeak)
        textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_ADD, null, null)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun echoSelectedCount() {
        val textToSpeak = "${getCurrentCounter()?.counterValue ?: "--"} ${countType.str(requireContext())}"

        showToast(textToSpeak)
        textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun showToast(text: String) {
        toast?.cancel()
        toast = Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT)
        toast?.show()
    }

}