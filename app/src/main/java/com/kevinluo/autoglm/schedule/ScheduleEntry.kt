package com.kevinluo.autoglm.schedule

import java.util.UUID

data class ScheduleEntry(
  val id: String = UUID.randomUUID().toString(),
  val name: String,
  val triggerAtMs: Long? = null,
  val intervalMs: Long? = null,
  val cronExpression: String? = null,
  val actionDescription: String = "",
  val enabled: Boolean = true
)
