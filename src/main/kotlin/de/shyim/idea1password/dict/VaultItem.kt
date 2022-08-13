package de.shyim.idea1password.dict

class VaultItem(val id: String, val title: String, val fields: MutableList<VaultItemField>) {
}

class VaultItemField(val label: String, val type: String, val reference: String)