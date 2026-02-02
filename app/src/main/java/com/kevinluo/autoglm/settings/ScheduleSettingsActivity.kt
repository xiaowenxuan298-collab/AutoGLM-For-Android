package com.kevinluo.autoglm.settings

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import com.kevinluo.autoglm.schedule.ScheduleEntry
import com.kevinluo.autoglm.schedule.ScheduleManager
import java.util.UUID

class ScheduleSettingsActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Simple vertical layout with a few actions
    val layout = LinearLayout(this).apply {
      orientation = LinearLayout.VERTICAL
      layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
      )
      setPadding(24, 24, 24, 24)
    }

    val header = TextView(this).apply {
      text = "定时任务配置"
      textSize = 20f
    }
    layout.addView(header)

    val addBtn = Button(this).apply { text = "添加定时任务" }
    val exportBtn = Button(this).apply { text = "导出定时任务" }
    val importBtn = Button(this).apply { text = "导入定时任务" }
    layout.addView(addBtn)
    layout.addView(exportBtn)
    layout.addView(importBtn)

    addBtn.setOnClickListener { showAddDialog() }
    exportBtn.setOnClickListener {
      val path = ScheduleManager.exportSchedules()
      Toast.makeText(this, "导出到: $path", Toast.LENGTH_LONG).show()
    }
    importBtn.setOnClickListener {
      val ok = ScheduleManager.importSchedules()
      Toast.makeText(this, if (ok) "导入成功" else "导入失败", Toast.LENGTH_SHORT).show()
    }

    setContentView(layout)

    ScheduleManager.init(this)
  }

  private fun showAddDialog() {
    val form = LinearLayout(this).apply {
      orientation = LinearLayout.VERTICAL
      setPadding(16, 16, 16, 16)
    }
    val nameInput = EditText(this).apply { hint = "任务名称" }
    val cronInput = EditText(this).apply { hint = "Cron 表达式（示例：0 9 * * 1）" }
    form.addView(TextView(this).apply { text = "名称" }); form.addView(nameInput)
    form.addView(TextView(this).apply { text = "Cron 表达式" }); form.addView(cronInput)

    AlertDialog.Builder(this)
      .setTitle("新增定时任务（Cron）")
      .setView(form)
      .setPositiveButton("添加") { _, _ ->
        val name = if (nameInput.text.isNotBlank()) nameInput.text.toString() else "Scheduled Task"
        val cron = cronInput.text.toString().trim().takeIf { it.isNotEmpty() }
        val entry = ScheduleEntry(
          id = UUID.randomUUID().toString(),
          name = name,
          cronExpression = cron,
          actionDescription = name,
          enabled = true
        )
        ScheduleManager.addSchedule(entry)
        Toast.makeText(this, "已添加定时任务: $name", Toast.LENGTH_SHORT).show()
      }
      .setNegativeButton("取消", null)
      .show()
  }
}
