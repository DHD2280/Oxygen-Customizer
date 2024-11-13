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
- And everyone who [contributed](https://github.com/DHD2280/Oxygen-Customizer/graphs/contributors) and [translated](https://crowdin.com/project/oxygen-customizer/members)... :)

<!-- CROWDIN-CONTRIBUTORS-START -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/lc98"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/16019099/medium/c318ef0f3a95a0549fed4657528b68f2.jpeg" />
          <br />
          <sub><b>Luigi Conte (lc98)</b></sub></a>
        <br />
        <sub><b>43114 words</b></sub>
      </td>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/If.you.know.better.than.me.do.it.If.not.shut.up"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/13525964/medium/22265802c0ad24a0a71f1abfc4776771.jpg" />
          <br />
          <sub><b>MKAdam (If.you.know.better.than.me.do.it.If.not.shut.up)</b></sub></a>
        <br />
        <sub><b>3966 words</b></sub>
      </td>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/muhammadbahaa2001"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/15231004/medium/1f277872da157dce11a9a6d1fc9120b6.png" />
          <br />
          <sub><b>Muhammad Bahaa (muhammadbahaa2001)</b></sub></a>
        <br />
        <sub><b>3729 words</b></sub>
      </td>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/mikropsoft"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/15972315/medium/4ce5cb2cc203e18840b955f71a9b9da7.png" />
          <br />
          <sub><b>𝗦𝗵𝗟𝗲𝗿𝗣 (mikropsoft)</b></sub></a>
        <br />
        <sub><b>7198 words</b></sub>
      </td>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/haosiang0331"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/16552071/medium/deea21d9147bf33e166156f25c668eb8.png" />
          <br />
          <sub><b>haosiang0331</b></sub></a>
        <br />
        <sub><b>3550 words</b></sub>
      </td>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/ot_inc"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/12457707/medium/32e968375042b7e2532c2e5f24ed83b8.jpg" />
          <br />
          <sub><b>Re*Index.(ot_inc) (ot_inc)</b></sub></a>
        <br />
        <sub><b>3541 words</b></sub>
      </td>
    </tr>
    <tr>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/11451420"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/16540697/medium/e68df5286962d5af9bcecb486bbb77cc_default.png" />
          <br />
          <sub><b>芜蚌湖埠 (11451420)</b></sub></a>
        <br />
        <sub><b>3292 words</b></sub>
      </td>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/AlejandroMoc"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/15175038/medium/d8ddd9948d0a952bff7713e558dcc152.png" />
          <br />
          <sub><b>Alejandro Moctezuma (AlejandroMoc)</b></sub></a>
        <br />
        <sub><b>3285 words</b></sub>
      </td>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/tugaia56"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/34554/medium/b0c4255e0353f5a6efed51ddce3bbc28_default.png" />
          <br />
          <sub><b>tugaia56</b></sub></a>
        <br />
        <sub><b>3173 words</b></sub>
      </td>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/carlosrobertow"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/16560677/medium/bd5c8e4bd49f74b0da1710010b71eadd.jpg" />
          <br />
          <sub><b>carlos (carlosrobertow)</b></sub></a>
        <br />
        <sub><b>2654 words</b></sub>
      </td>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/Osean22"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/15718399/medium/c1732446f8e330322a6101dd554ab494_default.png" />
          <br />
          <sub><b>Osean22</b></sub></a>
        <br />
        <sub><b>2287 words</b></sub>
      </td>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/igormiguell"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/15817659/medium/fc284cc203d362e11d2fbb67fc0aa7f0.jpg" />
          <br />
          <sub><b>igormiguell</b></sub></a>
        <br />
        <sub><b>836 words</b></sub>
      </td>
    </tr>
    <tr>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/Andfi"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/14343672/medium/ed27e5384b37aa115724c44156d4ea58_default.png" />
          <br />
          <sub><b>Andfi</b></sub></a>
        <br />
        <sub><b>760 words</b></sub>
      </td>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/senpai4ek"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/14493092/medium/52c9f6b7343f364ccd8d63d1dbc3b1f7.jpeg" />
          <br />
          <sub><b>ANTI SENPAI (senpai4ek)</b></sub></a>
        <br />
        <sub><b>758 words</b></sub>
      </td>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/hupoow"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/16522587/medium/95ca73a15c10eb9b45e1e969727ea070.png" />
          <br />
          <sub><b>王腾博 (hupoow)</b></sub></a>
        <br />
        <sub><b>318 words</b></sub>
      </td>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/Czak"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/16485797/medium/1f83cf36d385b6dda97fd604bc4ea3b8.jpg" />
          <br />
          <sub><b>Czak</b></sub></a>
        <br />
        <sub><b>182 words</b></sub>
      </td>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/pasqui1978"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/12888356/medium/6acbbcf3a0210a00a50064c3ddddb73c.jpg" />
          <br />
          <sub><b>Pasqui DJ (pasqui1978)</b></sub></a>
        <br />
        <sub><b>155 words</b></sub>
      </td>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/lingtian"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/15270284/medium/bdb5d40104fad4c1fbb053ddef11ab63.png" />
          <br />
          <sub><b>凌天 (lingtian)</b></sub></a>
        <br />
        <sub><b>146 words</b></sub>
      </td>
    </tr>
    <tr>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/renosang"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/16665185/medium/b0bb3b9d974bc17aeaaedb25c13a61cc.jpeg" />
          <br />
          <sub><b>Sang Nguyễn (renosang)</b></sub></a>
        <br />
        <sub><b>135 words</b></sub>
      </td>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/serge.croise"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/15460260/medium/5068dd643cc47609c74d82a8430cf682.png" />
          <br />
          <sub><b>Serge Croise (serge.croise)</b></sub></a>
        <br />
        <sub><b>117 words</b></sub>
      </td>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/jeanrivera"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/16466291/medium/d3c33f97fa0047600cca38eb1ac7bf16.jpeg" />
          <br />
          <sub><b>Jean Rivera (jeanrivera)</b></sub></a>
        <br />
        <sub><b>106 words</b></sub>
      </td>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/lorieeckersonbq2284"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/16527727/medium/5867dfeb593a6d398a194aa8ed5df2a4.jpeg" />
          <br />
          <sub><b>Lorie Eckerson (lorieeckersonbq2284)</b></sub></a>
        <br />
        <sub><b>102 words</b></sub>
      </td>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/wgajuraj"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/16358886/medium/16101b88166b2a06fe7c4d9812f90d6e_default.png" />
          <br />
          <sub><b>Wiktor Gajewicz (wgajuraj)</b></sub></a>
        <br />
        <sub><b>83 words</b></sub>
      </td>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/Neko-Madoka"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/16507005/medium/f64338670cd6bd0d15ce3c276cf87947.png" />
          <br />
          <sub><b>-拂暁- (Neko-Madoka)</b></sub></a>
        <br />
        <sub><b>71 words</b></sub>
      </td>
    </tr>
    <tr>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/marvingrasberger14"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/15425080/medium/06ec04c3d669f010c34247c64f95c520.jpeg" />
          <br />
          <sub><b>Marvin Grasberger (marvingrasberger14)</b></sub></a>
        <br />
        <sub><b>61 words</b></sub>
      </td>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/inok.go"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/16664999/medium/5c05f3c5db8faeedeedfadb0c7db3369.png" />
          <br />
          <sub><b>Дмитрий Мелешкин (inok.go)</b></sub></a>
        <br />
        <sub><b>6 words</b></sub>
      </td>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/a7medhamada76"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/16444713/medium/82e16292326f152222d5154686626630.jpeg" />
          <br />
          <sub><b>Ahmed Hamada (a7medhamada76)</b></sub></a>
        <br />
        <sub><b>4 words</b></sub>
      </td>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/nalankang521"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/16521037/medium/ac82f8d386129a7af83597c7607d0c28.jpeg" />
          <br />
          <sub><b>何康业 (nalankang521)</b></sub></a>
        <br />
        <sub><b>3 words</b></sub>
      </td>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/dilshod199714"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/13264140/medium/f3f0167bfdcd66bcf3c7e365513a5e68.jpg" />
          <br />
          <sub><b>Дилшод Исматов (dilshod199714)</b></sub></a>
        <br />
        <sub><b>2 words</b></sub>
      </td>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/Kirrillak"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/16446461/medium/d37b1ee9ce34b8fb2afdec3ee7eb7a4e_default.png" />
          <br />
          <sub><b>Kirrillak</b></sub></a>
        <br />
        <sub><b>2 words</b></sub>
      </td>
    </tr>
    <tr>
      <td align="center" valign="top">
        <a href="https://crowdin.com/profile/mirroxin_meow"><img alt="logo" style="width: 32px" src="https://crowdin-static.downloads.crowdin.com/avatar/16622307/medium/7d69e335f364b543922d759dece3dfc4.jpeg" />
          <br />
          <sub><b>Дмитрий Вадимович (mirroxin_meow)</b></sub></a>
        <br />
        <sub><b>2 words</b></sub>
      </td>
    </tr>
  </tbody>
</table>
<!-- CROWDIN-CONTRIBUTORS-END -->

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
