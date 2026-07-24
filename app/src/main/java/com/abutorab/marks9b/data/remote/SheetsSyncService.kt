package com.abutorab.marks9b.data.remote

import com.abutorab.marks9b.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.io.OutputStreamWriter

object SheetsSyncService {
    private const val ENDPOINT = "https://script.google.com/macros/s/AKfycbyMJngsMEoQ3pbKFSXrbt994G_5FlQ0Yp2GcXuxI6DkKJrS2OKT3FpxdemulU0tIiZ0uA/exec"

    data class ExportEntry(
        val roll: Int,
        val name: String,
        val values: List<Any>
    )

    suspend fun exportSubjectMarks(
        sheetId: String, 
        tabName: String, 
        startColumn: Int = 3, 
        entries: List<ExportEntry>,
        onProgress: ((Int, Int) -> Unit)? = null
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var processedCount = 0
            val totalCount = entries.size
            val chunks = entries.chunked(1)

            for (chunk in chunks) {
                val url = URL(ENDPOINT)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connection.doOutput = true

                val entriesArray = JSONArray()
                chunk.forEach { entry ->
                    val entryObj = JSONObject()
                    entryObj.put("roll", entry.roll)
                    entryObj.put("name", entry.name)
                    val valuesArray = JSONArray()
                    entry.values.forEach { valuesArray.put(it) }
                    entryObj.put("values", valuesArray)
                    entriesArray.put(entryObj)
                }

                val requestJson = JSONObject()
                requestJson.put("secret", BuildConfig.SHEETS_SYNC_SECRET)
                requestJson.put("spreadsheetId", sheetId)
                requestJson.put("inputTabName", tabName)
                requestJson.put("startColumn", startColumn)
                requestJson.put("entries", entriesArray)

                OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                    writer.write(requestJson.toString())
                    writer.flush()
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
                    val inputStream = if (responseCode >= 400) connection.errorStream else connection.inputStream
                    val responseText = inputStream.bufferedReader().use { it.readText() }
                    val responseJson = JSONObject(responseText)
                    
                    if (responseJson.optString("status") == "success") {
                        processedCount += chunk.size
                        withContext(Dispatchers.Main) {
                            onProgress?.invoke(processedCount, totalCount)
                        }
                    } else {
                        return@withContext Result.failure(Exception(responseJson.optString("message", "Unknown error from script")))
                    }
                } else {
                    return@withContext Result.failure(Exception("HTTP error code: $responseCode"))
                }
            }
            Result.success(processedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    data class ImportEntry(
        val roll: Int,
        val name: String,
        val values: List<Int?>
    )

    suspend fun importSubjectMarks(sheetId: String, tabName: String, startColumn: Int = 3, componentCount: Int): Result<List<ImportEntry>> = withContext(Dispatchers.IO) {
        try {
            val url = URL(ENDPOINT)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.doOutput = true

            val requestJson = JSONObject()
            requestJson.put("secret", BuildConfig.SHEETS_SYNC_SECRET)
            requestJson.put("action", "read")
            requestJson.put("spreadsheetId", sheetId)
            requestJson.put("inputTabName", tabName)
            requestJson.put("startColumn", startColumn)
            requestJson.put("componentCount", componentCount)

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(requestJson.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
                val inputStream = if (responseCode >= 400) connection.errorStream else connection.inputStream
                val responseText = inputStream.bufferedReader().use { it.readText() }
                val responseJson = JSONObject(responseText)

                if (responseJson.optString("status") == "success") {
                    val entriesArray = responseJson.optJSONArray("entries") ?: JSONArray()
                    val importEntries = mutableListOf<ImportEntry>()
                    for (i in 0 until entriesArray.length()) {
                        val entryObj = entriesArray.getJSONObject(i)
                        val roll = entryObj.optInt("roll")
                        val name = entryObj.optString("name")
                        val valuesArray = entryObj.optJSONArray("values") ?: JSONArray()
                        val values = mutableListOf<Int?>()
                        for (j in 0 until valuesArray.length()) {
                            values.add(if (valuesArray.isNull(j)) null else valuesArray.optDouble(j).toInt())
                        }
                        importEntries.add(ImportEntry(roll, name, values))
                    }
                    Result.success(importEntries)
                } else {
                    Result.failure(Exception(responseJson.optString("message", "Unknown error from script")))
                }
            } else {
                Result.failure(Exception("HTTP error code: $responseCode"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    data class RosterEntry(
        val roll: Int,
        val name: String,
        val religion: String,
        val group: String,
        val optionalType: String
    )

    suspend fun fetchRoster(sheetId: String): Result<List<RosterEntry>> = withContext(Dispatchers.IO) {
        try {
            val url = URL(ENDPOINT)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.doOutput = true

            val requestJson = JSONObject()
            requestJson.put("secret", BuildConfig.SHEETS_SYNC_SECRET)
            requestJson.put("action", "read_roster")
            requestJson.put("spreadsheetId", sheetId)

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(requestJson.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
                val inputStream = if (responseCode >= 400) connection.errorStream else connection.inputStream
                val responseText = inputStream.bufferedReader().use { it.readText() }
                val responseJson = JSONObject(responseText)

                if (responseJson.optString("status") == "success") {
                    val studentsArray = responseJson.optJSONArray("students") ?: JSONArray()
                    val roster = mutableListOf<RosterEntry>()
                    for (i in 0 until studentsArray.length()) {
                        val obj = studentsArray.getJSONObject(i)
                        roster.add(
                            RosterEntry(
                                roll = obj.optInt("roll"),
                                name = obj.optString("name"),
                                religion = obj.optString("religion"),
                                group = obj.optString("group"),
                                optionalType = obj.optString("optionalType")
                            )
                        )
                    }
                    Result.success(roster)
                } else {
                    Result.failure(Exception(responseJson.optString("message", "Unknown error from script")))
                }
            } else {
                Result.failure(Exception("HTTP error code: $responseCode"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
