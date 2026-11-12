package com.example.data.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.Bill
import com.example.data.room.LocalBillEntity
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import java.util.Locale

object DataExportUtility {

    /**
     * Exports a list of bills to a CSV file and triggers a share/download intent.
     */
    fun exportBillsToCsv(context: Context, bills: List<Bill>, localBills: List<LocalBillEntity>) {
        try {
            val fileName = "BillTrack_Export_${System.currentTimeMillis()}.csv"
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)

            val header = "Source,Title,Amount,Currency,Category,DueDate,IsPaid,Notes\n"
            outputStream.write(header.toByteArray())

            // Export Cloud Bills
            bills.forEach { bill ->
                val line = "Cloud,\"${bill.title}\",${bill.amount},${bill.currency},\"${bill.category}\",${bill.dueDate},${bill.isPaid},\"${bill.notes}\"\n"
                outputStream.write(line.toByteArray())
            }

            // Export Room Local Bills
            localBills.forEach { bill ->
                val line = "Local,\"${bill.name}\",${bill.amount},$,\"${bill.category}\",${bill.dueDate},${bill.isPaid},\"Local Storage\"\n"
                outputStream.write(line.toByteArray())
            }

            outputStream.close()

            // Trigger Share Intent (Download)
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "BillTrack Data Export")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Download/Share Bill Data"))

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Copies the installed application APK file and triggers a share/download intent.
     */
    fun exportAppApkFile(context: Context) {
        try {
            val sourceApk = File(context.applicationInfo.sourceDir)
            val destinationApk = File(context.cacheDir, "BillTrack_v1.2.0_App.apk")

            sourceApk.inputStream().use { input ->
                FileOutputStream(destinationApk).use { output ->
                    input.copyTo(output)
                }
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                destinationApk
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_SUBJECT, "BillTrack App Installer (APK)")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Download / Save App APK File"))
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: export JSON backup
            exportAppBackupBundle(context)
        }
    }

    /**
     * Backup helper for exporting application configuration and details as a downloadable JSON file.
     */
    fun exportAppBackupBundle(context: Context) {
        try {
            val fileName = "BillTrack_App_Backup.json"
            val file = File(context.cacheDir, fileName)
            val jsonContent = """
                {
                  "app_name": "BillTrack",
                  "version": "1.2.0",
                  "build": 76,
                  "tagline": "Everything in the app is visible and under the cover",
                  "timestamp": ${System.currentTimeMillis()}
                }
            """.trimIndent()
            file.writeText(jsonContent)

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_SUBJECT, "BillTrack App Backup File")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Download App File"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
