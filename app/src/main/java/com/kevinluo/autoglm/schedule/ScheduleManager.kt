package com.kevinluo.autoglm.schedule

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit
import com.kevinluo.autoglm.schedule.TaskExecutorHolder

object ScheduleManager {
  private var appContext: Context? = null
  private const val PREFS = "autoglm_schedules" // kept for backward compat if needed

  fun init(context: Context) {
    appContext = context.applicationContext
    // Load and schedule existing cron entries on init
    scheduleAllCron()
    // Ensure a default executor is available
    TaskExecutorHolder // trigger initialization if needed
  }

  private fun ensureContext(): Context? = appContext

  private fun loadSchedulesInternal(): MutableList<ScheduleEntry> {
     // For simplicity, read from a JSON file in internal storage named schedules.json if present
     val ctx = ensureContext() ?: return mutableListOf()
     val file = File(ctx.filesDir, "schedules.json")
     val list = mutableListOf<ScheduleEntry>()
     if (!file.exists()) return list
     val json = file.readText()
     val arr = JSONArray(json)
     for (i in 0 until arr.length()) {
        val obj = arr.optJSONObject(i) ?: continue
        val id = obj.optString("id", java.util.UUID.randomUUID().toString())
        val name = obj.optString("name", "Scheduled Task")
        val triggerAtMs = obj.optLong("triggerAtMs", 0L)
        val intervalMs = if (obj.has("intervalMs") && !obj.isNull("intervalMs")) obj.optLong("intervalMs") else null
        val cronExpr = obj.optString("cronExpression", null)
        val actionDescription = obj.optString("actionDescription", "")
        val enabled = obj.optBoolean("enabled", true)
        list.add(ScheduleEntry(id, name, triggerAtMs, intervalMs, cronExpr, actionDescription, enabled))
     }
     return list
  }

  private fun saveSchedules(list: List<ScheduleEntry>) {
     val ctx = ensureContext() ?: return
     val file = File(ctx.filesDir, "schedules.json")
     val arr = JSONArray()
     for (e in list) {
        val obj = JSONObject()
        obj.put("id", e.id)
        obj.put("name", e.name)
        obj.put("triggerAtMs", e.triggerAtMs ?: 0L)
        e.intervalMs?.let { obj.put("intervalMs", it) }
        obj.put("cronExpression", e.cronExpression)
        obj.put("actionDescription", e.actionDescription)
        obj.put("enabled", e.enabled)
        arr.put(obj)
     }
     file.writeText(arr.toString())
  }

  fun addSchedule(entry: ScheduleEntry) {
     val list = loadSchedulesInternal().toMutableList()
     list.add(entry)
     saveSchedules(list)
     // schedule immediately
     scheduleEntry(entry)
  }

  fun removeSchedule(id: String) {
     val list = loadSchedulesInternal().filter { it.id != id }
     saveSchedules(list)
  }

  private fun scheduleAllCron() {
     val list = loadSchedulesInternal()
     for (e in list) {
        if (!e.enabled) continue
        if (e.cronExpression != null) {
           scheduleNextCron(e.id, e.name, e.cronExpression)
        }
     }
  }

  // Schedule next occurrence for a cron entry
  fun scheduleNextCron(entryId: String, name: String, cronExpression: String) {
     val now = System.currentTimeMillis()
     val next = CronUtils.nextTriggerMs(cronExpression, now)
     if (next <= now) return
     val delay = next - now
     val data = Data.Builder()
       .putString("name", name)
       .putString("id", entryId)
       .putString("cronExpression", cronExpression)
       .build()
     val req = androidx.work.OneTimeWorkRequestBuilder(ScheduleWorker::class.java)
       .setInitialDelay(delay, TimeUnit.MILLISECONDS)
       .setInputData(data)
       .build()
     WorkManager.getInstance(ensureContext()!!).enqueueUniqueWork(entryId, ExistingWorkPolicy.REPLACE, req)
  }

  // Schedule a generic entry (cron or one-time/interval) - when used for non-cron, this is a simple wrapper
  private fun scheduleEntry(entry: ScheduleEntry) {
     // Minimal scheduling: if cron, delegate to scheduleNextCron; else if one-time, schedule a delay using triggerAtMs
     if (entry.cronExpression != null) {
        scheduleNextCron(entry.id, entry.name, entry.cronExpression)
     } else {
        val now = System.currentTimeMillis()
        val delay = (entry.triggerAtMs ?: now) - now
        if (delay <= 0) return
        val data = Data.Builder()
          .putString("name", entry.name)
          .putString("id", entry.id)
          .putString("cronExpression", entry.cronExpression)
          .build()
        val req = OneTimeWorkRequest.Builder(ScheduleWorker::class.java)
          .setInitialDelay(delay, TimeUnit.MILLISECONDS)
          .setInputData(data)
          .build()
        WorkManager.getInstance(ensureContext()!!).enqueueUniqueWork(entry.id, ExistingWorkPolicy.REPLACE, req)
     }
  }

  fun exportSchedules(): String {
     val ctx = ensureContext() ?: return ""
     val file = File(ctx.filesDir, "schedules_export.json")
     val list = loadSchedulesInternal()
     val arr = JSONArray()
     for (e in list) {
        val obj = JSONObject()
        obj.put("id", e.id)
        obj.put("name", e.name)
        obj.put("triggerAtMs", e.triggerAtMs ?: 0L)
        e.intervalMs?.let { obj.put("intervalMs", it) }
        obj.put("cronExpression", e.cronExpression)
        obj.put("actionDescription", e.actionDescription)
        obj.put("enabled", e.enabled)
        arr.put(obj)
     }
     file.writeText(arr.toString())
     return file.absolutePath
  }

  fun importSchedules(): Boolean {
     val ctx = ensureContext() ?: return false
     val file = File(ctx.filesDir, "schedules_export.json")
     if (!file.exists()) return false
     val json = file.readText()
     val arr = JSONArray(json)
     val list = mutableListOf<ScheduleEntry>()
     for (i in 0 until arr.length()) {
        val obj = arr.optJSONObject(i) ?: continue
        val id = obj.optString("id", java.util.UUID.randomUUID().toString())
        val name = obj.optString("name", "Scheduled Task")
        val triggerAtMs = obj.optLong("triggerAtMs", 0L)
        val intervalMs = if (obj.has("intervalMs") && !obj.isNull("intervalMs")) obj.optLong("intervalMs") else null
        val cronExpression = obj.optString("cronExpression", null)
        val actionDescription = obj.optString("actionDescription", "")
        val enabled = obj.optBoolean("enabled", true)
        list.add(ScheduleEntry(id, name, triggerAtMs, intervalMs, cronExpression, actionDescription, enabled))
     }
     saveSchedules(list)
     // Schedule cron tasks for newly imported items
     scheduleAllCron()
     return true
  }
}
