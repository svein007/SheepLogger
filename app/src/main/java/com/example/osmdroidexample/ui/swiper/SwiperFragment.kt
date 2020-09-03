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
import com.example.osmdroidexample.databinding.SwiperFragmentBinding
import com.example.osmdroidexample.ui.OnSwipeTouchListener
import com.example.osmdroidexample.ui.addobservation.AddObservationViewModel

class SwiperFragment : Fragment() {

    private lateinit var addObsViewModel: AddObservationViewModel
    private lateinit var binding: SwiperFragmentBinding

    private lateinit var textToSpeech: TextToSpeech

    private var countType = CountType.SHEEP

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
                when (countType) {
                    CountType.SHEEP -> {
                        addObsViewModel.decSheepCount()
                    }
                    CountType.LAMB -> {
                        addObsViewModel.decLambCount()
                    }
                }
                echoSelectedCount()
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onSwipeUp() {
                when (countType) {
                    CountType.SHEEP -> {
                        addObsViewModel.incSheepCount()
                    }
                    CountType.LAMB -> {
                        addObsViewModel.incLambCount()
                    }
                }
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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun echoSelectedCountType() {
        val textToSpeak = "${countType.str()} Selected"
        Toast.makeText(requireContext(), textToSpeak, Toast.LENGTH_SHORT).show()
        textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_ADD, null, null)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun echoSelectedCount() {
        val textToSpeak = when (countType) {
            CountType.SHEEP -> {
                "${addObsViewModel.observationSheepCount.value!!} ${countType.str()}"
            }
            CountType.LAMB -> {
                "${addObsViewModel.observationLambCount.value!!} ${countType.str()}"
            }
        }

        Toast.makeText(requireContext(), textToSpeak, Toast.LENGTH_SHORT).show()
        textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_ADD, null, null)
    }

    enum class CountType {
        SHEEP, LAMB;
        fun next(): CountType{
            return values()[(this.ordinal+1) % values().size]
        }
        fun prev(): CountType{
            return values()[(this.ordinal-1+values().size) % values().size]
        }
        fun str(): String {
            return when (this) {
                SHEEP -> "sheep"
                LAMB -> "lamb"
                else -> ""
            }
        }
    }

}