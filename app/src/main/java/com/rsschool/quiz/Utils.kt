package com.rsschool.quiz

import android.app.Activity
import android.content.Intent


class Utils {
    companion object {
        private var sTheme = 0
        val THEME_DEFAULT = 0
        val THEME_WHITE = 1

        /**
         * Set the theme of the Activity, and restart it by creating a new Activity of the same type.
         */
        fun changeToTheme(activity: Activity, theme: Int) {
            sTheme = theme
            activity.finish()
            activity.startActivity(Intent(activity, activity.javaClass))
        }

        fun onActivityCreateSetTheme(activity: Activity) {
            when (sTheme) {
                THEME_DEFAULT -> activity.theme.applyStyle(R.style.First, true)
                THEME_WHITE -> activity.theme.applyStyle(R.style.Second, true)
                else -> activity.theme.applyStyle(R.style.First, true)
            }
        }
    }

}