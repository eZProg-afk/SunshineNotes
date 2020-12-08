package spiral.bit.dev.sunshinenotes.other

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import java.util.*

private fun openRemindDialog() {
    val builder = AlertDialog.Builder(this)
    val view: View = LayoutInflater.from(this)
            .inflate(R.layout.dialog_picker,
                    findViewById<View>(R.id.layout_dialog_picker_container) as ViewGroup?)
    builder.setView(view)
    dialogRemind = builder.create()
    if (dialogRemind.getWindow() != null) {
        dialogRemind.getWindow().setBackgroundDrawable(ColorDrawable(0))
    }
    timePicker = view.findViewById<TimePicker>(R.id.timePicker)
    val btnOk = view.findViewById<TextView>(R.id.btn_ok)
    view.findViewById<View>(R.id.text_cancel).setOnClickListener { dialogRemind.dismiss() }
    btnOk.setOnClickListener {
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val c = Calendar.getInstance()
        val intent = Intent(getApplicationContext(), AlarmReceiver::class.java)
        intent.putExtra("nameOfNote", alreadyAvailableNoteInFolder.getTitle())
        c[Calendar.HOUR_OF_DAY] = timePicker.getHour()
        c[Calendar.MINUTE] = timePicker.getMinute()
        pendingIntent = PendingIntent.getBroadcast(this@CreateNoteActivity, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.set(AlarmManager.RTC_WAKEUP, c.timeInMillis, pendingIntent)
        dialogRemind.dismiss()
    }
    dialogRemind.show()
}