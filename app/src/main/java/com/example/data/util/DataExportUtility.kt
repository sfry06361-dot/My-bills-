package com.example.data.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.model.Bill
import com.example.data.room.LocalBillEntity
import java.io.File

object DataExportUtility {

    /**
     * Exports bills directly to the device Downloads folder as a CSV file
     * using MediaStore (Android 10+, zero permissions) or app external Downloads.
     */
    fun exportBillsToCsv(
        context: Context,
        bills: List<Bill>,
        localBills: List<LocalBillEntity>
    ): Result<String> {
        return try {
            val fileName = "BillTrack_Export_${System.currentTimeMillis()}.csv"
            val csvBuilder = StringBuilder()
            csvBuilder.append("Source,Title,Amount,Currency,Category,DueDate,IsPaid,Notes\n")

            bills.forEach { bill ->
                val safeTitle = bill.title.replace("\"", "\"\"")
                val safeCategory = bill.category.replace("\"", "\"\"")
                val safeNotes = bill.notes.replace("\"", "\"\"")
                csvBuilder.append(
                    "Cloud,\"$safeTitle\",${bill.amount},${bill.currency},\"$safeCategory\",${bill.dueDate},${bill.isPaid},\"$safeNotes\"\n"
                )
            }

            localBills.forEach { bill ->
                val safeName = bill.name.replace("\"", "\"\"")
                val safeCategory = bill.category.replace("\"", "\"\"")
                csvBuilder.append(
                    "Local,\"$safeName\",${bill.amount},$,\"$safeCategory\",${bill.dueDate},${bill.isPaid},\"Local Storage\"\n"
                )
            }

            saveBytesToDownloads(
                context = context,
                fileName = fileName,
                mimeType = "text/csv",
                bytes = csvBuilder.toString().toByteArray(Charsets.UTF_8)
            )
            Result.success("Saved $fileName to Downloads")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Exports a structured JSON backup of cloud and local bills directly to Downloads.
     */
    fun exportAppBackupBundle(
        context: Context,
        bills: List<Bill> = emptyList(),
        localBills: List<LocalBillEntity> = emptyList()
    ): Result<String> {
        return try {
            val fileName = "BillTrack_Backup_${System.currentTimeMillis()}.json"
            val jsonContent = buildString {
                append("{\n")
                append("  \"app_name\": \"BillTrack\",\n")
                append("  \"version\": \"1.2.0\",\n")
                append("  \"cloud_bills_count\": ${bills.size},\n")
                append("  \"local_bills_count\": ${localBills.size},\n")
                append("  \"exported_at_ms\": ${System.currentTimeMillis()}\n")
                append("}\n")
            }

            saveBytesToDownloads(
                context = context,
                fileName = fileName,
                mimeType = "application/json",
                bytes = jsonContent.toByteArray(Charsets.UTF_8)
            )
            Result.success("Saved $fileName to Downloads")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun openPlayStoreListing(context: Context) {
        val packageName = context.packageName
        val marketIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$packageName")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(marketIntent)
        } catch (_: Exception) {
            context.startActivity(webIntent)
        }
    }

    fun shareAppPlayStoreLink(context: Context) {
        val playUrl = "https://play.google.com/store/apps/details?id=${context.packageName}"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "BillTrack")
            putExtra(Intent.EXTRA_TEXT, "Track your bills and payment deadlines with BillTrack: $playUrl")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share BillTrack"))
    }

    private fun saveBytesToDownloads(
        context: Context,
        fileName: String,
        mimeType: String,
        bytes: ByteArray
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: error("Unable to create file in Downloads")
            resolver.openOutputStream(uri)?.use { output ->
                output.write(bytes)
            } ?: error("Unable to open output stream for $fileName")

            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        } else {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: context.filesDir
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)
            file.writeBytes(bytes)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Save $fileName"))
        }
    }
}
