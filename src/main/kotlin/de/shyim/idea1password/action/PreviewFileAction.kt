package de.shyim.idea1password.action

import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.testFramework.LightVirtualFile
import de.shyim.idea1password.OPManager
import de.shyim.idea1password.OnePassword
import de.shyim.idea1password.CommandExecutionFailed
import java.io.File

class PreviewFileAction: DumbAwareAction(OnePassword.message("previewAction"), "", OnePassword.ICON) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val srcEditor = e.getData(LangDataKeys.EDITOR) ?: return
        val srcFile = e.getData(LangDataKeys.VIRTUAL_FILE) ?: return

        FileDocumentManager.getInstance().saveAllDocuments()

        val task = object: Backgroundable(project, "Preview secrets") {
            override fun run(indicator: ProgressIndicator) {
                try {
                    val preview = OPManager.preview(File(srcFile.path))

                    val file = LightVirtualFile("Preview: ${srcFile.name}", srcFile.fileType, preview)

                    ApplicationManager.getApplication().invokeLater {
                        FileEditorManager.getInstance(project).openTextEditor(OpenFileDescriptor(project, file), true)
                    }
                } catch (e: CommandExecutionFailed) {
                    HintManager.getInstance().showErrorHint(srcEditor, "Generating preview failed: ${e.message}")
                }
            }
        }

        ProgressManager.getInstance().runProcessWithProgressAsynchronously(
            task,
            BackgroundableProcessIndicator(task)
        )
    }

    override fun update(e: AnActionEvent) {
        val srcEditor = e.getData(LangDataKeys.EDITOR)

        if (srcEditor == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        e.presentation.isEnabledAndVisible = srcEditor.document.text.contains("op://")
    }
}