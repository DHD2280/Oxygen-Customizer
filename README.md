# Oxygen Customizer

<div align="center">
  <img src=".github/resources/banner.png" width="90%" alt="banner" />
</div>
<p align="center">
  <a href="https://github.com/DHD2280/Oxygen-Customizer/releases"><img src="https://img.shields.io/github/v/release/DHD2280/Oxygen-Customizer?style=for-the-badge&include_prereleases" alt="Release"></a>
  <a href="https://github.com/DHD2280/Oxygen-Customizer/releases/tag/beta_builds"><img src="https://img.shields.io/badge/Download%20Latest-Beta-blue?style=for-the-badge" alt="Beta"></a>
  <a href="https://github.com/DHD2280/Oxygen-Customizer"><img alt="Repo Size" src="https://img.shields.io/github/repo-size/DHD2280/Oxygen-Customizer?style=for-the-badge"></a>
  <a href="https://github.com/DHD2280/Oxygen-Customizer/actions"><img src="https://img.shields.io/github/actions/workflow/status/DHD2280/Oxygen-Customizer/build_debug.yml?branch=beta&label=Nightliy%20Build&style=for-the-badge" alt="Nightly Build"></a>
  <a href="https://t.me/OxygenCustomizerGroup"><img src="https://img.shields.io/badge/Support%20Group-2CA5E0?style=for-the-badge&logo=telegram&logoColor=white" alt="Support Group"></a>
  <a href="https://t.me/OxygenCustomizer"><img src="https://img.shields.io/badge/Update%20Channel-2CA5E0?style=for-the-badge&logo=telegram&logoColor=white" alt="Update Channel"></a>
</p>
<div align="center">

### Free and Open-Source Oxygen OS Customizer Application

</div>
<p align="center">
Oxygen Customizer is an open-source Android application aimed at providing users with the ability to tweak and customize various aspects of Oxygen OS UI.
<br><br>
Furthermore, the open-source nature of Oxygen Customizer encourages community contributions and continuous improvement, ensuring a dynamic and evolving user experience.
</p>

## Table of Contents

- [Requirements](#-requirements)
- [Installation](#-installation)
- [Permissions](#-permissions)
- [Contribution](#-contribution)
- [FAQ](#-faq)
- [Credits](#-credits)
- [License](#-license)
- [Disclaimer](#-disclaimer)
- [Donations](#-donations)

> [!CAUTION]
> 
> This app requires Magisk, KernelSU & APatch for root access and XPosed/LSPosed framework. Any alternative methods won't work.

# 🛠 Requirements

- Oxygen OS 14

- [Magisk](https://github.com/topjohnwu/Magisk) (Recommended) or [KernelSU](https://github.com/tiann/KernelSU) or [APatch](https://github.com/bmax121/APatch)

- [LSPosed](https://github.com/LSPosed/LSPosed)

# 👨‍💻 Installation

  1. Download and install the Oxygen Customizer app.

  ### Installation for Magisk Users:

  2. Enable the Xposed module in LSPosed app.

  3. Open the app, grant root permission and follow the instructions.

  4. Wait for it to finish generating rom specific module.

  5. Reboot the device when prompted.

  ### Installation for KernelSU/APatch Users:

  2. Grant SuperSU/root permission for Oxygen Customizer & SystemUI from KernelSU app
  
  3. Disable `Unmount modules` option if it's available in the app
  
  4. Open the app, wait for it to finish generating rom specific module.
  
  5. Reboot the device when prompted.

# 🔒 Permissions

This app requires the following permissions:

`
android.permission.ACCESS_NETWORK_STATE
android.permission.INTERNET
android.permission.ACCESS_FINE_LOCATION
android.permission.ACCESS_COARSE_LOCATION
android.permission.ACCESS_BACKGROUND_LOCATION
`
Permissions to access the internet and location are required for fetching weather data and location-based services. These permissions are essential for the proper functioning of the Lockscreen Weather and are not used for any other purposes.

`android.permission.USE_BIOMETRIC`
Permission to use biometric authentication is required for showing the Authentiation Prompt when enabled for Advanced Reboot Menu.

`android.permission.VIBRATE`
Permission to vibrate the device is required for haptic feedback when using the app.

`
android.permission.WRITE_EXTERNAL_STORAGE
android.permission.READ_EXTERNAL_STORAGE
android.permission.MANAGE_EXTERNAL_STORAGE
`
Permissions to read and write external storage are required for saving and loading custom images/fonts. These permissions are essential for the proper functioning of the app and are not used for any other purposes.

# 🤝 Contribution

We highly appreciate and welcome all forms of contributions, ranging from code, documentation, graphics, design suggestions, to bug reports. We encourage you to make the most of GitHub's collaborative platform by submitting Pull Requests, providing tutorials or other relevant content. Whatever you have to offer, we value and can utilize it effectively in our project.

# 🤓 FAQ

<details>
  <summary>Do I need a root access for Oxygen Customizer to work?</summary>

- Yes, root access is required for Oxygen Customizer to function properly.
</details>

<details>
  <summary>Which devices does Oxygen Customizer support?</summary>

- Oxygen Customizer exclusively supports Oxygen OS 14 and later versions. Compatibility with other devices or custom ROMs is not guaranteed.
</details>

<details>
  <summary>Is Android version "xx" supported?</summary>

- Oxygen Customizer officially supports Android 14 and later versions. Compatibility with earlier Android versions is not provided, and there are no plans to introduce support for those versions.
</details>

<details>
  <summary>I got bootloop. How do I fix it?</summary>

- Boot into [Safe Mode](https://www.androidauthority.com/how-to-enter-safe-mode-android-801476/) and uninstall module.
</details>

<details>
  <summary>What is the difference between Release build and Debug build?</summary>

- [Release build](https://github.com/DHD2280/Oxygen-Customizer/releases/latest) is an optimized version intended for distribution to end-users, while [Debug build](https://github.com/DHD2280/Oxygen-Customizer/actions) includes additional features and information for debugging and development purposes.
</details>

<details>
  <summary>Can I use Oxygen Customizer in conjunction with other customization apps?</summary>

- Yes, Oxygen Customizer can be used alongside other customization apps. However, it's important to note that conflicts or overlapping modifications may occur, which could affect the overall user experience.
</details>

<details>
  <summary>I found a bug. How do I report it?</summary>

- To report a bug, please navigate to the [Issues](https://github.com/DHD2280/Oxygen-Customizer/issues/new/choose) section. Create a new issue and ensure you select the `Bug Report` template. Provide as much detailed information as possible, including steps to reproduce the bug and any relevant error messages or screenshots.
</details>

<details>
  <summary>How do I request a new feature?</summary>

- If you have a feature request, please go to the [Issues](https://github.com/DHD2280/Oxygen-Customizer/issues/new/choose) section. Create a new issue and choose the `Feature Request` template. Be sure to include comprehensive details about the desired feature, its potential benefits, and any other relevant information that can assist in understanding and evaluating the request.
</details>

<details>
  <summary>Where can I make a donation?</summary>

- You can donate via "[PayPal](https://www.paypal.me/luigifale)", or via "[Buy me a coffee](https://www.buymeacoffee.com/DHD2280)".
</details>

# ❤ Credits

### Thanks to:

- [Android Open Source Project (AOSP)](https://source.android.com) for Android source code.
- [OnePlus](https://www.oneplus.com) for Oxygen OS.
- [Material Icons](https://fonts.google.com/icons) for in-app icons.
- [PixelXpert](https://github.com/siavash79/PixelXpert), [@siavash79](https://github.com/siavash79) for help with Xposed mods, and his great work with Pixel Xpert.
- [Iconify](https://github.com/Mahmud0808/Iconify), [@Mahmud0808](https://github.com/Mahmud0808) for bringing the idea of Iconify and his great work. 
- [crDroid](https://github.com/crdroidandroid) for Pulse Controller and some tweaks.
- [OmniROM](https://github.com/omnirom) for the Weather Providers.
- [Project Matrixx](https://github.com/ProjectMatrixx) for some illustrations.
- [SuperiorExtended](https://github.com/SuperiorExtended) for some tweaks and layouts.
- And everyone who [contributed](./docs/contributors.md) and [translated](./docs/translators.md)... :)

# © License

Oxygen Customizer is licensed under GPLv3. Please see [`LICENSE`](./LICENSE.md) for the full license text.

# 📝 Disclaimer

> [!WARNING]
> - Please note that Oxygen Customizer may not be fully compatible with all custom OOS variants.
> - I cannot be held responsible for any potential damage or issues that may occur to your device while using Oxygen Customizer.

# ⭐ Donations

[<img src=".github/resources/PayPal.svg"
alt='Donate with PayPal'
height="80">](https://www.paypal/luigifale) [<img src=".github/resources/BMC.svg"
alt='Donate with BMC'
height="80">](https://www.buymeacoffee.com/DHD2280)
