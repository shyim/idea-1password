package de.shyim.idea1password

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

@State(name = "OnePasswordSettings", storages = [Storage(StoragePathMacros.PRODUCT_WORKSPACE_FILE)])
class OnePasswordSettings (private val project: Project) : SimplePersistentStateComponent<OnePasswordState>(OnePasswordState()) {
    companion object {
        fun getInstance(project: Project): OnePasswordSettings = project.service()
    }

    fun setUsePasswordRecipe(value: String) {
        state.usePasswordRecipe = value
    }

    fun getUsePasswordRecipe(): String {
        if (state.usePasswordRecipe == null) {
            return "letters,digits,symbols,32"
        }

        return state.usePasswordRecipe!!
    }

    fun getVault(): String {
        if (state.vault == null) {
            return "Private"
        }

        return state.vault!!
    }

    fun setVault(value: String){
        state.vault = value
    }

    fun setAccount(value: String) {
        state.account = value
    }

    fun getAccount(): String {
        if (state.account == null) {
            return ""
        }

        return state.account!!
    }
}

class OnePasswordState : BaseState() {
    var usePasswordRecipe by string("letters,digits,symbols,32")
    var vault by string("Private")
    var account by string("")
}
