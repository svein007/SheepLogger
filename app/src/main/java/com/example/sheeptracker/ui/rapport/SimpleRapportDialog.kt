package com.example.sheeptracker.ui.rapport

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sheeptracker.R
import com.example.sheeptracker.utils.generateSimpleRapport
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.simple_rapport_dialog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SimpleRapportDialog : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.simple_rapport_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        CoroutineScope(Dispatchers.Main).launch {
            val rapportText = generateSimpleRapport(requireContext())

            rapportTextView?.text = Html.fromHtml(rapportText)

            sendEmailRapportFloatingActionButton.setOnClickListener {
                val emailIntent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_SUBJECT, "Sheep Tracker Rapport")
                    putExtra(Intent.EXTRA_TEXT, Html.fromHtml(rapportText))
                    type = "text/html"
                }

                startActivity(emailIntent)
            }
        }

    }
}