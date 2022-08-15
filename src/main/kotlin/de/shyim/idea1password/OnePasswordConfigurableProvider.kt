package de.shyim.idea1password

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel

class OnePasswordConfigurableProvider(val project: Project): ConfigurableProvider() {
    override fun createConfigurable(): Configurable {
        return OnePasswordConfigurable(project)
    }
}

class OnePasswordConfigurable internal constructor(private val project: Project): BoundConfigurable("1Password Settings") {
    private val settings = OnePasswordSettings.getInstance(project)

    override fun createPanel(): DialogPanel {
        val panel = panel {
            row {
                label("Password recipe:")
                    .widthGroup("labels")
                textField()
                    .bindText(settings::getUsePasswordRecipe, settings::setUsePasswordRecipe)
            }

            row {
                label("Vault:")
                    .widthGroup("labels")
                textField()
                    .bindText(settings::getVault, settings::setVault)
            }

            row {
                label("Account:")
                    .widthGroup("labels")
                textField()
                    .bindText(settings::getAccount, settings::setAccount)
            }
        }
        return panel
    }
}