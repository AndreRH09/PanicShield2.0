package com.example.panicshield.data.sms

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager(private val context: Context) {

    fun hasAllPermissions(): Boolean {
        return hasSmsPermission() && hasContactsPermission()
    }

    fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getRequiredPermissions(): Array<String> {
        val permissions = mutableListOf<String>()

        if (!hasSmsPermission()) {
            permissions.add(Manifest.permission.SEND_SMS)
        }

        if (!hasContactsPermission()) {
            permissions.add(Manifest.permission.READ_CONTACTS)
        }

        return permissions.toTypedArray()
    }

    fun requestPermissions(activity: Activity, requestCode: Int) {
        val permissions = getRequiredPermissions()
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode)
        }
    }
}

