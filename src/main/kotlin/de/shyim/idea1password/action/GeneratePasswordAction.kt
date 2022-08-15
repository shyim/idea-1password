package de.shyim.idea1password.action

import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import de.shyim.idea1password.CommandExecutionFailed
import de.shyim.idea1password.OPManager
import de.shyim.idea1password.OnePassword
import javax.swing.JComponent

class GeneratePasswordAction: DumbAwareAction("Generate A Password", "", OnePassword.ICON) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(LangDataKeys.EDITOR) ?: return

        val panel = GeneratePasswordDialog()
        if (!panel.showAndGet()) {
            return
        }

        val task = object : Task.Backgroundable(project, "Creating new Entry") {
            override fun run(indicator: ProgressIndicator) {
                try {
                    val reference = OPManager.generatePassword(panel.vaultTitle)

                    ApplicationManager.getApplication().invokeLater {
                        ApplicationManager.getApplication().runWriteAction {
                            CommandProcessor.getInstance().executeCommand(project, {
                                editor.document.insertString(editor.caretModel.offset, reference)
                            }, "Insert Field", null)
                        }
                    }
                } catch (e: CommandExecutionFailed) {
                    ApplicationManager.getApplication().invokeLater {
                        HintManager.getInstance().showErrorHint(editor, e.message!!)
                    }
                }
            }

            override fun onCancel() {
            }
        }

        ProgressManager.getInstance().runProcessWithProgressAsynchronously(
            task,
            BackgroundableProcessIndicator(task)
        )
    }

    override fun update(e: AnActionEvent) {
        val srcEditor = e.getData(LangDataKeys.EDITOR)

        e.presentation.isEnabledAndVisible = srcEditor != null
    }
}

class GeneratePasswordDialog: DialogWrapper(true) {
    var vaultTitle = ""

    override fun createCenterPanel(): JComponent {
        val cur = this

        return panel {
            row("Title:") {
                textField()
                    .focused()
                    .bindText(cur::vaultTitle)
            }
        }
    }

    init {
        title = "Generate a new Password"
        init()
        setSize(80, 100)
    }
}