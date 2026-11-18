package com.ddayforge.widget.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.SizeF
import android.widget.RemoteViews
import com.ddayforge.widget.R
import com.ddayforge.widget.data.Repository
import com.ddayforge.widget.render.WidgetContent
import com.ddayforge.widget.render.WidgetRenderer
import com.ddayforge.widget.style.ThemePresets
import com.ddayforge.widget.ui.WidgetConfigActivity
import java.time.LocalDate
import java.time.ZoneId

class DDayWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { updateWidget(context, manager, it) }
        scheduleMidnight(context)
    }

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, newOptions: Bundle) {
        updateWidget(context, manager, id)
    }

    override fun onDeleted(context: Context, ids: IntArray) {
        Repository.removeWidgets(context, ids)
    }

    override fun onEnabled(context: Context) = scheduleMidnight(context)

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_MIDNIGHT,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                updateAll(context)
                scheduleMidnight(context)
            }
        }
    }

    companion object {
        const val ACTION_MIDNIGHT = "com.ddayforge.widget.MIDNIGHT"

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, DDayWidgetProvider::class.java))
            ids.forEach { updateWidget(context, manager, it) }
        }

        /**
         * 런처가 위젯을 둥글게 잘라내는 반경(dp). 안드로이드 12+ 기본값을 쓰되,
         * 런처마다 더 크게 자르는 경우가 있어 최소 20dp로 잡는다.
         */
        fun systemClipDp(context: Context): Float {
            if (Build.VERSION.SDK_INT < 31) return 0f
            val dm = context.resources.displayMetrics
            val sys = runCatching {
                context.resources.getDimension(android.R.dimen.system_app_widget_background_radius) / dm.density
            }.getOrDefault(0f)
            return maxOf(sys, 20f)
        }

        /** 현재 위젯 크기(dp). 세로 모드 기준: 최소 너비 x 최대 높이 */
        fun widgetSizeDp(manager: AppWidgetManager, id: Int): Pair<Float, Float> {
            val o = manager.getAppWidgetOptions(id)
            var w = o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH).toFloat()
            var h = o.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT).toFloat()
            if (Build.VERSION.SDK_INT >= 31) {
                @Suppress("DEPRECATION")
                val sizes = o.getParcelableArrayList<SizeF>(AppWidgetManager.OPTION_APPWIDGET_SIZES)
                sizes?.firstOrNull()?.let { w = it.width; h = it.height }
            }
            if (w <= 0f || h <= 0f) { w = 180f; h = 80f }
            return w to h
        }

        fun updateWidget(context: Context, manager: AppWidgetManager, id: Int) {
            val config = Repository.widget(context, id)
            val dday = config?.let { Repository.findDDay(context, it.ddayId) }
            val style = config?.style ?: ThemePresets.DEFAULT.style
            val content = when {
                config == null -> WidgetContent.placeholder()
                dday == null -> WidgetContent.placeholder("디데이가 삭제됨 · 탭해서 변경")
                else -> WidgetContent.of(dday, style)
            }
            val (wDp, hDp) = widgetSizeDp(manager, id)
            val density = context.resources.displayMetrics.density
            val bitmap = runCatching {
                WidgetRenderer(context).render(style, content, wDp, hDp, density, systemClipDp(context))
            }.getOrNull()

            val views = RemoteViews(context.packageName, R.layout.widget_dday)
            if (bitmap != null) views.setImageViewBitmap(R.id.widget_image, bitmap)
            views.setContentDescription(R.id.widget_image, "${content.title} ${content.count}")

            val intent = Intent(context, WidgetConfigActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                putExtra(WidgetConfigActivity.EXTRA_RECONFIGURE, true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pi = PendingIntent.getActivity(
                context, id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.widget_root, pi)
            manager.updateAppWidget(id, views)
        }

        /** 다음 자정 직후에 위젯 갱신 (정확 알람 권한 없이 동작하는 일반 알람) */
        fun scheduleMidnight(context: Context) {
            val am = context.getSystemService(AlarmManager::class.java) ?: return
            val zone = ZoneId.systemDefault()
            val next = LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() + 2_000
            val intent = Intent(context, DDayWidgetProvider::class.java).setAction(ACTION_MIDNIGHT)
            val pi = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            am.set(AlarmManager.RTC, next, pi)
        }
    }
}
