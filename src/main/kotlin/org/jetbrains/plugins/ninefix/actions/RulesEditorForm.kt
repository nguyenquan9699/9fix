package org.jetbrains.plugins.ninefix.actions

import android.annotation.Nullable
import com.intellij.openapi.ui.DialogWrapper
import org.jetbrains.plugins.ninefix.Config
import org.jetbrains.plugins.ninefix.Helper
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.table.DefaultTableModel

class RulesEditorForm : DialogWrapper(true) {
    @Nullable

    val panel = JPanel(BorderLayout())
    private val dataList = Helper.loadRules(Config.PATH, "")
    private val dataArray = Helper.convertListListToArrayArray(dataList)
    private val columns = Config.FIELD_NAMES
    var tableModel = DefaultTableModel(dataArray, columns)
    private val table = JTable(tableModel)

    override fun createCenterPanel(): JComponent {
        val addButton = JButton("Add rule")
        addButton.addActionListener(ActionListener {
            tableModel.insertRow(tableModel.rowCount, arrayOf("", "", "", "", "", "", "", "", "", "", "", "", ""))
            table.changeSelection(tableModel.rowCount-1,0,false,false)
        })

        panel.preferredSize = Dimension(Config.ACTION_SIZE_WIDTH, Config.ACTION_SIZE_HEIGHT)
        panel.add(JScrollPane(table), BorderLayout.NORTH)
        panel.add(addButton, BorderLayout.SOUTH)

        return panel
    }

    init {
        title = "Rules Editor"
        init()
    }

    override fun doOKAction() {
        Helper.saveRules(Config.PATH, tableModel)
        super.doOKAction()
    }
}
