package com.example.osmdroidexample.ui.swiper

import android.content.Context
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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.osmdroidexample.R
import com.example.osmdroidexample.databinding.SwiperFragmentBinding
import com.example.osmdroidexample.ui.OnSwipeTouchListener
import com.example.osmdroidexample.ui.addobservation.AddObservationViewModel

class SwiperFragment : Fragment() {

    private lateinit var addObsViewModel: AddObservationViewModel
    private lateinit var binding: SwiperFragmentBinding

    private lateinit var textToSpeech: TextToSpeech

    private var toast: Toast? = null

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
                addObsViewModel.dec(countForEnum(countType))
                echoSelectedCount()
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onSwipeUp() {
                addObsViewModel.inc(countForEnum(countType))
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

    private fun countForEnum(countType: CountType) : MutableLiveData<Int> {
        return when(countType) {
            CountType.SHEEP -> addObsViewModel.observationSheepCount
            CountType.LAMB -> addObsViewModel.observationLambCount
            CountType.BLACK -> addObsViewModel.observationBlackCount
            CountType.GREY -> addObsViewModel.observationGreyCount
            CountType.WHITE -> addObsViewModel.observationWhiteCount
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun echoSelectedCountType() {
        val textToSpeak = "${countType.str(requireContext())} Selected"
        showToast(textToSpeak)
        textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_ADD, null, null)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun echoSelectedCount() {
        val textToSpeak = "${countForEnum(countType).value!!} ${countType.str(requireContext())}"

        showToast(textToSpeak)
        textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun showToast(text: String) {
        toast?.cancel()
        toast = Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT)
        toast?.show()
    }

    enum class CountType {
        SHEEP, LAMB, BLACK, GREY, WHITE;
        fun next(): CountType{
            return values()[(this.ordinal+1) % values().size]
        }
        fun prev(): CountType{
            return values()[(this.ordinal-1+values().size) % values().size]
        }
        fun str(context: Context): String {
            return when (this) {
                SHEEP -> context.resources.getString(R.string.sheep)
                LAMB -> context.resources.getString(R.string.lamb)
                BLACK -> context.resources.getString(R.string.black)
                GREY -> context.resources.getString(R.string.grey)
                WHITE -> context.resources.getString(R.string.white)
            }
        }
    }

}