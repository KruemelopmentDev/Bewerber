package de.kruemelopment.org.bewerber

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import java.util.Random

class Widget : AppWidgetProvider() {
    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        widgetInfo: Bundle
    ) {
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        private var words: MutableList<String> = ArrayList()
        private var extensionWord: MutableList<String> = ArrayList()
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            words.clear()
            extensionWord.clear()
            val myDB = DataBaseHelper(context)
            val res = myDB.getAllData("worter")
            if (res.count > 0) {
                while (res.moveToNext()) {
                    words.add(res.getString(1))
                }
            }
            if (words.isEmpty()) {
                words.addAll(mutableListOf("Cola", "Käse", "Krümelopment Dev"))
            }
            val re = myDB.getAllData("erganzung")
            if (re.count > 0) {
                while (re.moveToNext()) {
                    extensionWord.add(re.getString(1))
                }
            }
            myDB.close()
            if (extensionWord.isEmpty()) {
                extensionWord.addAll(
                    mutableListOf(
                        "Bier",
                        "Käse",
                        "Cola",
                        "Salami",
                        "Fanta",
                        "Sprite",
                        "Wasser",
                        "Döner",
                        "Rosen"
                    )
                )
            }
            val views = RemoteViews(context.packageName, R.layout.widgetlayout)
            val intent = Intent(context, MainAcitivity::class.java)
            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.lay, pendingIntent)
            views.setTextViewText(R.id.textView3, spruch(context))
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun spruch(context: Context): String {
            val gh = words[Random().nextInt(words.size)]
            val spruche = context.resources.getStringArray(R.array.sprueche)
            var spruch = spruche[Random().nextInt(spruche.size)]
            spruch = spruch.replace("_", gh)
            if (spruch.contains("~")) {
                val random = Random().nextInt(extensionWord.size)
                val str = extensionWord[random]
                if (extensionWord.size > 1) extensionWord.removeAt(random)
                spruch = spruch.replace("~", str)
            }
            if (spruch.contains("#")) {
                val st = extensionWord[Random()
                    .nextInt(extensionWord.size)]
                spruch = spruch.replace("#", st)
            }
            if (spruch.contains("\n")) {
                val kl = spruch.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                spruch = """
                    ${kl[1]}
                    ${kl[2]}
                    """.trimIndent()
            }
            return spruch
        }
    }
}