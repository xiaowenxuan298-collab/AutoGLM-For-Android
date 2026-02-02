package com.kevinluo.autoglm.schedule

object TaskExecutorHolder {
  var executor: TaskExecutor = DefaultTaskExecutor()
}
