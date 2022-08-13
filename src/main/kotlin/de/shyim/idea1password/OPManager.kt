package de.shyim.idea1password

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import de.shyim.idea1password.dict.VaultItem
import de.shyim.idea1password.dict.VaultItemField
import de.shyim.idea1password.dict.VaultListItem
import org.codehaus.jettison.json.JSONArray
import org.codehaus.jettison.json.JSONObject
import java.io.File

object OPManager {
    fun preview(srcFile: File): String {
        val commandLine = GeneralCommandLine("op", "inject")
        commandLine.withInput(File(srcFile.path))

        val handler = CapturingProcessHandler(commandLine).runProcess(30000)

        if (handler.exitCode != 0) {
            throw CommandExecutionFailed(handler.stderr)
        }

        return handler.stdout
    }

    fun listItemsInVault(): List<VaultListItem> {
        val commandLine = GeneralCommandLine("op", "item", "list", "--format", "json")

        val handler = CapturingProcessHandler(commandLine).runProcess(30000)

        if (handler.exitCode != 0) {
            throw CommandExecutionFailed(handler.stderr)
        }

        val json = JSONArray(handler.stdout)

        val list = mutableListOf<VaultListItem>()

        for (i in 0 until json.length()) {
            val item = json.getJSONObject(i)

            list.add(VaultListItem(item.getString("id"), item.getString("title"), item.getJSONObject("vault").getString("name")))
        }

        return list
    }

    fun getItem(id: String): VaultItem {
        val commandLine = GeneralCommandLine("op", "item", "get", id, "--format", "json")

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

    fun generatePassword(title: String): String {
        val commandLine = GeneralCommandLine("op", "item", "create", "--category", "login", "--generate-password", "--title", title,  "--format", "json")

        val handler = CapturingProcessHandler(commandLine).runProcess(30000)

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
}

class CommandExecutionFailed(message: String): Exception(message)
class InvalidJSONResponseFromOP(message: String): Exception(message)