package com.example.sheeptracker.ui.rapport

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import com.example.sheeptracker.R
import com.example.sheeptracker.database.AppDatabase
import com.example.sheeptracker.utils.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.simple_rapport_dialog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SimpleRapportDialog : BottomSheetDialogFragment() {

    var rapportText = ""
    var checkedItemIndex = Menu.FIRST
    var year = ""
    var rapportJSON = ""

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
            rapportJSON = getJSONRapport(requireContext())

            rapportText = generateSimpleRapport(requireContext())

            rapportTextView?.text = rapportText

            rapportExportFilesButton.setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    val files = ArrayList<Uri>()

                    val exportIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {

                        if (rapportTextChip.isChecked) {
                            val textRapportFile = FileProvider.getUriForFile(
                                requireContext(),
                                "com.example.sheeptracker.fileprovider",
                                createFileAndWrite(requireContext(), rapportText, "generated_rapport.txt")
                            )
                            files.add(textRapportFile)
                        }

                        if (rapportJSONChip.isChecked) {
                            val jsonFile = FileProvider.getUriForFile(
                                requireContext(),
                                "com.example.sheeptracker.fileprovider",
                                createFileAndWrite(requireContext(), rapportJSON, "generated_rapport.json")
                            )
                            files.add(jsonFile)
                        }

                        if (rapportDBFileChip.isChecked) {
                            val dbFile = FileProvider.getUriForFile(
                                requireContext(),
                                "com.example.sheeptracker.fileprovider",
                                requireContext().getDatabasePath("sheep_database")
                            )
                            files.add(dbFile)
                        }

                        if (rapportImagesChip.isChecked) {
                            val appDao = AppDatabase.getInstance(requireContext()).appDatabaseDao

                            getImageUris(appDao, year.toIntOrNull() ?: -1).forEach {
                                val imgFile = FileProvider.getUriForFile(
                                    requireContext(),
                                    "com.example.sheeptracker.fileprovider",
                                    it.toFile()
                                )
                                files.add(imgFile)
                            }
                        }

                        val subjectYear =
                            if (year.isBlank()) "" else " - ${getString(R.string.year_selected)}: $year"
                        putExtra(Intent.EXTRA_SUBJECT, "Sheep Tracker Rapport$subjectYear")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        putExtra(Intent.EXTRA_TEXT, rapportText)

                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
                        type = "application/octet-stream"
                    }

                    startActivity(Intent.createChooser(exportIntent, "Share Rapport"))
                }
            }

            filterTV.setOnClickListener {
                showFilterPopup(it)
            }

        }

    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
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
                        year = item.title as String
                        rapportJSON = getJSONRapport(requireContext(), item.title.toString().toInt())
                        generateSimpleRapport(requireContext(), item.title.toString().toInt())
                    } else {
                        year = ""
                        rapportJSON = getJSONRapport(requireContext())
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