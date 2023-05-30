package com.vlnabatov.alabaster.listeners

import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.util.SystemInfoRt
import com.vlnabatov.alabaster.lightThemes
import com.vlnabatov.alabaster.macOSLightTheme
import javax.swing.UIManager
import com.vlnabatov.alabaster.laf.macos.MacLafProvider

class LafManagerListener : LafManagerListener {
    override fun lookAndFeelChanged(source: LafManager) {
        val name = source.currentLookAndFeel.name
        val editorColorsManager = EditorColorsManager.getInstance()

        println(UIManager.getInstalledLookAndFeels())

        if (name in lightThemes && SystemInfoRt.isMac) {
            val info = source.installedLookAndFeels.first { it.name === macOSLightTheme }
//            UIManager.setLookAndFeel(info as UIManager.LookAndFeelInfo)
//            editorColorsManager.globalScheme = editorColorsManager.getScheme(name)
        }
    }
}