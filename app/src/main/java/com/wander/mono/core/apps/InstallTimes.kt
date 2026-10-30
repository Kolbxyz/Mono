package com.wander.mono.core.apps

import android.content.pm.PackageManager
import android.os.Build

/**
 * When [packageName] was first installed, in epoch milliseconds; null when the package manager no
 * longer knows it (removed between the listing and this read, or archived). Main profile only:
 * Android gives a launcher no install time for another profile's packages.
 */
internal fun firstInstallTime(packageManager: PackageManager, packageName: String): Long? = try {
    val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        packageManager.getPackageInfo(packageName, 0)
    }
    info.firstInstallTime
} catch (_: PackageManager.NameNotFoundException) {
    null
}
