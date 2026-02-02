package com.kevinluo.autoglm.schedule

data class ScheduleTemplate(
  val id: String,
  val name: String,
  val cronExpression: String? = null,
  val intervalMs: Long? = null,
  val description: String = ""
)
