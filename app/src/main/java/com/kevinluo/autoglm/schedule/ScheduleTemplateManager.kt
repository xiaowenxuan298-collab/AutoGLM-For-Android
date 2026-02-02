package com.kevinluo.autoglm.schedule

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object ScheduleTemplateManager {
  private const val FILE_NAME = "schedule_templates.json"
  private var appContext: Context? = null

  fun init(context: Context) {
    appContext = context.applicationContext
  }

  fun listTemplates(): List<ScheduleTemplate> {
    val ctx = appContext ?: return emptyList()
    val file = File(ctx.filesDir, FILE_NAME)
    if (!file.exists()) return emptyList()
    val json = file.readText()
    val arr = JSONArray(json)
    val list = mutableListOf<ScheduleTemplate>()
    for (i in 0 until arr.length()) {
      val obj = arr.optJSONObject(i) ?: continue
      val id = obj.optString("id", java.util.UUID.randomUUID().toString())
      val name = obj.optString("name", "Template")
      val cronExpression = obj.optString("cronExpression", null)
      val intervalMs = if (obj.has("intervalMs") && !obj.isNull("intervalMs")) obj.optLong("intervalMs") else null
      val desc = obj.optString("description", "")
      list.add(ScheduleTemplate(id, name, cronExpression, intervalMs, desc))
    }
    return list
  }

  fun saveTemplate(t: ScheduleTemplate) {
    val ctx = appContext ?: return
    val file = File(ctx.filesDir, FILE_NAME)
    val templates = listTemplates().toMutableList()
    templates.add(t)
    val arr = JSONArray()
    for (tt in templates) {
      val obj = JSONObject()
      obj.put("id", tt.id)
      obj.put("name", tt.name)
      obj.put("cronExpression", tt.cronExpression)
      tt.intervalMs?.let { obj.put("intervalMs", it) }
      obj.put("description", tt.description)
      arr.put(obj)
    }
    file.writeText(arr.toString())
  }

  fun applyTemplate(template: ScheduleTemplate) {
    // Convert template into a ScheduleEntry and add it to ScheduleManager
    val entry = ScheduleEntry(
      id = template.id,
      name = template.name,
      cronExpression = template.cronExpression,
      intervalMs = template.intervalMs,
      actionDescription = template.description,
      enabled = true
    )
    ScheduleManager.addSchedule(entry)
  }
}
