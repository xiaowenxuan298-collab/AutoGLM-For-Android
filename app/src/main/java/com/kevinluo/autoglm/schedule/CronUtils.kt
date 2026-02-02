package com.kevinluo.autoglm.schedule

import java.util.Calendar

object CronUtils {
  // Very lightweight cron next-trigger calculator supporting patterns like:
  // - "*/N * * * *" every N minutes
  // - "m h * * *" daily at hour h and minute m (or with wildcards)
  // - "0 9 * * 1" every Monday at 09:00
  // This is a simplified implementation for demonstration purposes.
  fun nextTriggerMs(cronExpr: String, fromMs: Long = System.currentTimeMillis()): Long {
     val parts = cronExpr.trim().split(" ")
     if (parts.size < 5) {
        // fallback to 1 minute later
        val cal = Calendar.getInstance()
        cal.timeInMillis = fromMs
        cal.add(Calendar.MINUTE, 1)
        return cal.timeInMillis
     }
     // Parse fields (minute, hour, dom, month, dow)
     val minuteField = parts[0]
     val hourField = parts[1]
     val dowField = parts[4]

     var cal = Calendar.getInstance()
     cal.timeInMillis = fromMs
     // Start from next minute to avoid retriggering immediately
     cal.add(Calendar.MINUTE, 1)
    
     // helper to parse field as wildcard or exact int
     fun parseIntOrNull(s: String): Int? {
        return if (s == "*") null else s.toIntOrNull()
     }
     val minute = parseIntOrNull(minuteField)
     val hour = parseIntOrNull(hourField)
     val dow = parseIntOrNull(dowField)

     val maxChecks = 365 * 24 * 60 // up to ~1 year
     var checks = 0
     while (checks < maxChecks) {
        val m = cal.get(Calendar.MINUTE)
        val h = cal.get(Calendar.HOUR_OF_DAY)
        val w = cal.get(Calendar.DAY_OF_WEEK) // Sunday=1
        if ((minute == null || minute == m) && (hour == null || hour == h)) {
           val dowMatches = if (dow == null) true else {
              // Calendar.DAYS: Sunday=1 ... Saturday=7
              val wIndex = (w + 6) % 7 // convert to 0=Mon ... 6=Sun? we'll align with 0=Sunday
              // simpler: map 0=Sunday,1=Mon,...
              val dowValue = when (w) {
                 Calendar.SUNDAY -> 0
                 Calendar.MONDAY -> 1
                 Calendar.TUESDAY -> 2
                 Calendar.WEDNESDAY -> 3
                 Calendar.THURSDAY -> 4
                 Calendar.FRIDAY -> 5
                 Calendar.SATURDAY -> 6
                 else -> -1
              }
              dowValue == dow
           }
           if (dowMatches) {
              return cal.timeInMillis
           }
        }
        cal.add(Calendar.MINUTE, 1)
        checks++
     }
     // fallback
     return fromMs + 60_000
  }
}
