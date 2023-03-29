package de.shyim.idea1password

import com.intellij.DynamicBundle
import com.intellij.openapi.util.IconLoader
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val BUNDLE = "messages.OnePassword"

object OnePassword : DynamicBundle(BUNDLE) {

    @Suppress("SpreadOperator")
    @JvmStatic
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) =
        getMessage(key, *params)

    @Suppress("SpreadOperator", "unused")
    @JvmStatic
    fun messagePointer(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) =
        getLazyMessage(key, *params)

    @JvmField
    val ICON = IconLoader.getIcon("/META-INF/pluginIcon.svg", javaClass)
}
