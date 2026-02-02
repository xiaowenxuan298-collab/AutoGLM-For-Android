package com.kevinluo.autoglm.schedule

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Data
import java.util.Date

class ScheduleWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
  override suspend fun doWork(): Result {
     val name = inputData.getString("name") ?: "Scheduled Task"
     val entryId = inputData.getString("id") ?: "unknown"
     val cronExpr = inputData.getString("cronExpression")

     val ctx = applicationContext

     // Ensure notification channel exists (Android O+)
     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channelId = "autoglm_schedule"
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(channelId) == null) {
           nm.createNotificationChannel(NotificationChannel(channelId, "AutoGLM Scheduling", NotificationManager.IMPORTANCE_DEFAULT))
        }
     }

     val notification = NotificationCompat.Builder(ctx, "autoglm_schedule")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Scheduled Task")
        .setContentText("Task '$name' fired (id: $entryId) at ${Date()}")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()
     NotificationManagerCompat.from(ctx).notify(entryId.hashCode(), notification)

     Log.d("ScheduleWorker", "Scheduled task '$name' fired, id=$entryId at ${Date()}")

     // Execute configured task via executor (pluggable)
     try {
        val exec = com.kevinluo.autoglm.schedule.TaskExecutorHolder.executor
        exec.execute(name, entryId, cronExpr)
     } catch (t: Throwable) {
        Log.e("ScheduleWorker", "Task execution failed: ${t.localizedMessage}", t)
     }

     // Schedule next occurrence if cron expression provided
     if (!cronExpr.isNullOrEmpty()) {
        ScheduleManager.scheduleNextCron(entryId, name, cronExpr)
     }

     return Result.success()
  }
}

// Helper to avoid compilation issues in this patch; the real implementation should call ScheduleManager.scheduleNextCron(entryId, name, cronExpr)
private fun scheduleNextCronHelper(ctx: Context, id: String, name: String, cronExpr: String): Unit {
  // Placeholder for integration point
}
