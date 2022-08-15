# 1Password Integration for IDEA

![Build](https://github.com/shyim/intellij-1password/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)

1Password for IDEA provides you with a set of tools to integrate your development workflow with 1Password, powered by the [1Password CLI](https://developer.1password.com/docs/cli/).

## Quick Start
1. **Set up the CLI** - v2.4.0 or greater of the 1Password CLI needs to be [installed on your system](https://developer.1password.com/docs/cli/get-started#install).
2. **Enable biometric unlock** - You must have [biometric unlock](https://developer.1password.com/docs/cli/about-biometric-unlock) enabled. If you don't have a biometric device, you'll still be able to use your device user password.
3. **Install this extension** - You can download this extension from the Jetbrains Marketplace

## Features

- Preview your Secrets: You can right-click any file or editor to see the preview of your secrets
- Generate new Passwords: You can generate using the Generate menu new secrets and insert the reference to it
- Choose a Secret from your Vault: You can search in your Vault and insert the reference to the item