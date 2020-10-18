package com.example.sheeptracker.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.sheeptracker.R
import com.example.sheeptracker.utils.checkHasAllPermissions

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        if (preference?.key == "request_permissions") {
            performRequestPermissions(0)
            return true
        }

        return super.onPreferenceTreeClick(preference)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty()
            && grantResults.all { grantResult -> grantResult == PackageManager.PERMISSION_GRANTED}) {
            Toast.makeText(requireContext(), getString(R.string.permissions_granted), Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(requireContext(), getString(R.string.some_permissions_not_granted), Toast.LENGTH_LONG).show()
        }
    }

    private fun performRequestPermissions(requestCode: Int) {
        if (checkHasAllPermissions(requireContext())) {
            Toast.makeText(requireContext(), getString(R.string.permissions_already_granted), Toast.LENGTH_LONG).show()
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                requestCode)
        }
    }
}