# Alabaster Theme

<img src="./src/main/resources/META-INF/pluginIcon.svg" width="140">

<!-- Plugin description -->

Light minimal theme for Jetbrains IDEs based on [tonsky's sublime-scheme-alabaster](https://github.com/tonsky/sublime-scheme-alabaster)

<!-- Plugin description end -->

![Build](https://github.com/vlnabatov/alabaster-theme/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/20748-alabaster-theme.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/20748-alabaster-theme.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)

![](screenshot.png)

This theme goes more minimal and defines only four elements (all in a broad terms):

#### Strings

#### Constants

#### Comments

#### Function declarations

unlike [sublime-scheme-alabaster](https://github.com/tonsky/sublime-scheme-alabaster) which additionally highlights all global definitions. I think it's would be better to highlight only functions as the the action happens in them.
## Installation

- Using IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "alabaster-theme"</kbd> >
  <kbd>Install Plugin</kbd>
- Manually:

  Download the [latest release](https://github.com/vlnabatov/alabaster-theme/releases) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

---

Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
