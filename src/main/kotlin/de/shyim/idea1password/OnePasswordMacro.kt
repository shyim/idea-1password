package de.shyim.idea1password

import com.intellij.ide.macro.Macro
import com.intellij.ide.macro.MacroWithParams
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext

class OnePasswordMacro : Macro(), MacroWithParams {
    override fun getName(): String {
        return "1Password"
    }

    override fun getDescription(): String {
        return OnePassword.message("macroDescription")
    }

    override fun expand(dataContext: DataContext, vararg args: String): String? {
        return if (args.size == 1) {
            // It might be a bug but in the Java application run config,
            // we get access to the Project from the "Program Args" but not from VM Options
            val project = CommonDataKeys.PROJECT.getData(dataContext)
            OPManager.readPath(project, args[0])
        } else null;
    }

    override fun expand(dataContext: DataContext): String? {
        return null
    }
}
