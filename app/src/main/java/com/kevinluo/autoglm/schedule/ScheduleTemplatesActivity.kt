package com.kevinluo.autoglm.schedule

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast

class ScheduleTemplatesActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ScheduleTemplateManager.init(this)
    ScheduleManager.init(this)
    val layout = LinearLayout(this).apply {
      orientation = LinearLayout.VERTICAL
      layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }
    val listView = ListView(this)
    val templates = ScheduleTemplateManager.listTemplates()
    val names = templates.map { it.name }
    listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, names)
    layout.addView(listView)
    setContentView(layout)
  }
}
