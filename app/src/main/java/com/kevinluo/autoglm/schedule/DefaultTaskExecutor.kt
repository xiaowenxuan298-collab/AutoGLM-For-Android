package com.kevinluo.autoglm.schedule

import android.util.Log

class DefaultTaskExecutor : TaskExecutor {
  override suspend fun execute(name: String, id: String, cronExpression: String?): Boolean {
    // Placeholder for real execution logic
    Log.d("DefaultTaskExecutor", "Executing task: id=$id name=$name cron=$cronExpression")
    // Simulate success
    return true
  }
}
