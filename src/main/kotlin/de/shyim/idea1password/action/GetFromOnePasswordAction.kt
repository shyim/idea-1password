package de.shyim.idea1password.action

import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.PopupChooserBuilder
import com.intellij.ui.components.JBList
import de.shyim.idea1password.CommandExecutionFailed
import de.shyim.idea1password.OPManager
import de.shyim.idea1password.OnePassword
import de.shyim.idea1password.dict.VaultItemField
import de.shyim.idea1password.dict.VaultListItem
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JList

class GetFromOnePasswordAction: DumbAwareAction("Get from 1Password", "", OnePassword.ICON)
{
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val srcEditor = e.getData(LangDataKeys.EDITOR) ?: return

        val task = object: Task.Backgroundable(project, "Listing all items") {
            override fun run(indicator: ProgressIndicator) {
                try {
                    val items = OPManager.listItemsInVault(project)
                    val popupList = JBList(items)

                    popupList.cellRenderer = object : JBList.StripedListCellRenderer() {
                        override fun getListCellRendererComponent(
                            list: JList<*>?,
                            value: Any?,
                            index: Int,
                            isSelected: Boolean,
                            cellHasFocus: Boolean
                        ): Component {
                            val renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)

                            if (renderer is JLabel && value is VaultListItem) {
                                renderer.text = value.title
                            }

                            return renderer
                        }
                    }

                    val builder = PopupChooserBuilder(popupList)
                        .setTitle("Select Item")
                        .setFilteringEnabled {
                            return@setFilteringEnabled (it as VaultListItem).title
                        }

                    builder.setItemChosenCallback {
                        val vaultListItem = (builder.chooserComponent as JBList<VaultListItem>).selectedValue ?: return@setItemChosenCallback

                        showVaultItemFields(vaultListItem, project, srcEditor)
                    }

                    ApplicationManager.getApplication().invokeLater {
                        builder.createPopup()
                            .showInBestPositionFor(srcEditor)
                    }
                } catch (e: CommandExecutionFailed) {
                    ApplicationManager.getApplication().invokeLater {
                        HintManager.getInstance().showErrorHint(srcEditor, e.message!!)
                    }
                }
            }
        }

        ProgressManager.getInstance().runProcessWithProgressAsynchronously(
            task,
            BackgroundableProcessIndicator(task)
        )
    }

    private fun showVaultItemFields(vaultListItem: VaultListItem, project: Project, editor: Editor) {
        val task = object: Task.Backgroundable(project, "Getting field information") {
            override fun run(indicator: ProgressIndicator) {
                try {
                    val items = OPManager.getItem(project, vaultListItem.id).fields
                    val popupList = JBList(items)

                    popupList.cellRenderer = object : JBList.StripedListCellRenderer() {
                        override fun getListCellRendererComponent(
                            list: JList<*>?,
                            value: Any?,
                            index: Int,
                            isSelected: Boolean,
                            cellHasFocus: Boolean
                        ): Component {
                            val renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)

                            if (renderer is JLabel && value is VaultItemField) {
                                renderer.text = value.label
                            }

                            return renderer
                        }
                    }

                    val builder = PopupChooserBuilder(popupList)
                        .setTitle("Select Item")
                        .setFilteringEnabled {
                            return@setFilteringEnabled (it as VaultItemField).label
                        }

                    builder.setItemChosenCallback {
                        val field = (builder.chooserComponent as JBList<VaultItemField>).selectedValue ?: return@setItemChosenCallback

                        ApplicationManager.getApplication().runWriteAction {
                            CommandProcessor.getInstance().executeCommand(project, {
                                editor.document.insertString(editor.caretModel.offset, field.reference)
                            }, "Insert Field", null)
                        }
                    }

                    ApplicationManager.getApplication().invokeLater {
                        builder.createPopup()
                            .showInBestPositionFor(editor)
                    }
                } catch (e: CommandExecutionFailed) {
                    ApplicationManager.getApplication().invokeLater {
                        HintManager.getInstance().showErrorHint(editor, e.message!!)
                    }
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

        e.presentation.isEnabledAndVisible = srcEditor != null
    }
}
