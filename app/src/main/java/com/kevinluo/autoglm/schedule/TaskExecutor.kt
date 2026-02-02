package com.kevinluo.autoglm.schedule

interface TaskExecutor {
  suspend fun execute(name: String, id: String, cronExpression: String?): Boolean
}
