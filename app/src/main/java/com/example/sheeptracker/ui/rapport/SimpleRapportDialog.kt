package com.example.sheeptracker.ui.rapport

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import com.example.sheeptracker.R
import com.example.sheeptracker.utils.generateSimpleRapport
import com.example.sheeptracker.utils.getTripYears
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.simple_rapport_dialog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SimpleRapportDialog : BottomSheetDialogFragment() {

    var rapportText = ""
    var checkedItemIndex = Menu.FIRST

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
            rapportText = generateSimpleRapport(requireContext())

            rapportTextView?.text = rapportText

            sendEmailRapportFloatingActionButton.setOnClickListener {
                val exportIntent = Intent(Intent.ACTION_SEND).apply {

                    val contentFile = FileProvider.getUriForFile(
                        requireContext(),
                        "com.example.sheeptracker.fileprovider",
                        requireContext().getDatabasePath("sheep_database")
                    )

                    putExtra(Intent.EXTRA_SUBJECT, "Sheep Tracker Rapport")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    putExtra(Intent.EXTRA_TEXT, rapportText)

                    putExtra(Intent.EXTRA_STREAM, contentFile)
                    type = "application/octet-stream"
                }

                startActivity(Intent.createChooser(exportIntent, "Share Rapport"))
            }

            filterTV.setOnClickListener {
                showFilterPopup(it)
            }

        }

    }

    private fun showFilterPopup(v: View) {
        CoroutineScope(Dispatchers.Main).launch {
            val popup = PopupMenu(requireContext(), v)

            popup.menu.clear()
            popup.menu.add(Menu.FIRST, Menu.FIRST, Menu.FIRST, R.string.all).isCheckable = true

            val tripYears = getTripYears(requireContext())

            for ((i, year) in tripYears.withIndex()) {
                popup.menu.add(Menu.FIRST, Menu.FIRST+1+i, Menu.FIRST+1+i, year).apply {
                    isCheckable = true
                }
            }

            popup.setOnMenuItemClickListener { item ->
                checkedItemIndex = item.itemId
                filterTV.text = item.title
                CoroutineScope(Dispatchers.Main).launch {
                    rapportText = if (item.title.toString().toIntOrNull() != null) {
                        generateSimpleRapport(requireContext(), item.title.toString().toInt())
                    } else {
                        generateSimpleRapport(requireContext())
                    }
                    rapportTextView?.text = rapportText
                }
                return@setOnMenuItemClickListener true
            }

            popup.menu.setGroupCheckable(Menu.FIRST, true, true)

            popup.menu.findItem(checkedItemIndex)?.isChecked = true

            popup.show()
        }
    }


}