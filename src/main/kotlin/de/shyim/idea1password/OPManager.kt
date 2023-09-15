package de.shyim.idea1password

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.openapi.project.Project
import de.shyim.idea1password.dict.VaultItem
import de.shyim.idea1password.dict.VaultItemField
import de.shyim.idea1password.dict.VaultListItem
import org.codehaus.jettison.json.JSONArray
import org.codehaus.jettison.json.JSONObject
import java.io.File

object OPManager {
    fun preview(project: Project, srcFile: File): String {
        val commandLine = GeneralCommandLine("op", "inject", "--in-file")
        commandLine.withInput(File(srcFile.path))
        appendConfig(project, commandLine, false)

        val handler = CapturingProcessHandler(commandLine).runProcess(30000)

        if (handler.exitCode != 0) {
            throw CommandExecutionFailed(handler.stderr)
        }

        return handler.stdout
    }

    fun listItemsInVault(project: Project): List<VaultListItem> {
        val commandLine = GeneralCommandLine("op", "item", "list", "--format", "json")
        appendConfig(project, commandLine)

        val handler = CapturingProcessHandler(commandLine).runProcess(30000)

        if (handler.exitCode != 0) {
            throw CommandExecutionFailed(handler.stderr)
        }

        val json = JSONArray(handler.stdout)

        val list = mutableListOf<VaultListItem>()

        for (i in 0 until json.length()) {
            val item = json.getJSONObject(i)

            list.add(
                VaultListItem(
                    item.getString("id"),
                    item.getString("title"),
                    item.getJSONObject("vault").getString("name")
                )
            )
        }

        return list
    }

    fun getItem(project: Project, id: String): VaultItem {
        val commandLine = GeneralCommandLine("op", "item", "get", id, "--format", "json")
        appendConfig(project, commandLine)
        val handler = CapturingProcessHandler(commandLine).runProcess(30000)

        if (handler.exitCode != 0) {
            throw CommandExecutionFailed(handler.stderr)
        }

        val json = JSONObject(handler.stdout)
        val jsonFields = json.getJSONArray("fields")

        val fields = mutableListOf<VaultItemField>()

        for (i in 0 until jsonFields.length()) {
            val fieldItem = jsonFields.getJSONObject(i)

            fields.add(VaultItemField(fieldItem.getString("id"), fieldItem.getString("type"), fieldItem.getString("reference")))
        }

        return VaultItem(json.getString("id"), json.getString("title"), fields)
    }

    fun generatePassword(project: Project, title: String): String {
        val payload = JSONObject()
        payload.put("title", title)
        payload.put("category", "LOGIN")
        payload.put("generatePassword", OnePasswordSettings.getInstance(project).getUsePasswordRecipe())

        val file = File.createTempFile("temp", null)
        file.writeText(payload.toString())

        val commandLine = GeneralCommandLine("op", "item", "create", "--format", "json")
        commandLine.withInput(file)
        appendConfig(project, commandLine)

        val handler = CapturingProcessHandler(commandLine).runProcess(30000)
        file.delete()

        if (handler.exitCode != 0) {
            throw CommandExecutionFailed(handler.stderr)
        }

        val json = JSONObject(handler.stdout)

        val jsonFields = json.getJSONArray("fields")

        for (i in 0 until jsonFields.length()) {
            val fieldItem = jsonFields.getJSONObject(i)

            if (fieldItem.getString("id") == "password") {
                return fieldItem.getString("reference")
            }
        }

        throw InvalidJSONResponseFromOP("Could not find reference in op command")
    }

    fun readPath(project: Project?, reference: String): String {
        val commandLine = GeneralCommandLine("op", "read", "-n", reference)
        val handler = CapturingProcessHandler(commandLine).runProcess(30000)
        if (project != null) {
            appendConfig(project, commandLine, false)
        }

        if (handler.exitCode != 0) {
            throw CommandExecutionFailed(handler.stderr)
        }
        return handler.stdout
    }

    private fun appendConfig(project: Project, cmd: GeneralCommandLine, addVault: Boolean = true) {
        val settings = OnePasswordSettings.getInstance(project)

        if (settings.getAccount().isNotEmpty()) {
            cmd.withEnvironment("OP_ACCOUNT", settings.getAccount())
        }

        if (addVault && settings.getVault().isNotEmpty()) {
            cmd.addParameter("--vault")
            cmd.addParameter(settings.getVault())
        }
    }
}

class CommandExecutionFailed(message: String) : Exception(message)
class InvalidJSONResponseFromOP(message: String) : Exception(message)
