[English](/README.md) | **Italiano**

# Oxygen Customizer

<div align="center">
  <img src="/.github/resources/banner.png" width="90%" alt="banner" />
</div>
<p align="center">
  <a href="https://github.com/DHD2280/Oxygen-Customizer/releases"><img src="https://img.shields.io/github/v/release/DHD2280/Oxygen-Customizer?style=for-the-badge&label=STABLE" alt="Stable"></a>
  <a href="https://github.com/DHD2280/Oxygen-Customizer/releases"><img src="https://img.shields.io/github/v/release/DHD2280/Oxygen-Customizer?style=for-the-badge&include_prereleases&label=BETA" alt="Beta"></a>
  <a href="https://github.com/DHD2280/Oxygen-Customizer/actions/workflows/build_debug.yml"><img src="https://img.shields.io/github/actions/workflow/status/DHD2280/Oxygen-Customizer/build_debug.yml?branch=beta&label=Nightliy&style=for-the-badge" alt="Nightly Build"></a>
  <a href="https://github.com/DHD2280/Oxygen-Customizer"><img alt="Repo Size" src="https://img.shields.io/github/repo-size/DHD2280/Oxygen-Customizer?style=for-the-badge"></a>
  <br>
  <a href="https://t.me/OxygenCustomizerGroup"><img src="https://img.shields.io/badge/Support%20Group-2CA5E0?style=for-the-badge&logo=telegram&logoColor=white" alt="Support Group"></a>
  <a href="https://t.me/OxygenCustomizer"><img src="https://img.shields.io/badge/Update%20Channel-2CA5E0?style=for-the-badge&logo=telegram&logoColor=white" alt="Update Channel"></a>
</p>
<div align="center">

### Applicazione Oxygen OS Customizer gratuita e Open-Source

</div>
<p align="center">
Oxygen Customizer è un'applicazione Android open source che mira a offrire agli utenti la possibilità di modificare e personalizzare vari aspetti dell'interfaccia utente di Oxygen OS.
<br><br>
Inoltre, la natura open source di Oxygen Customizer incoraggia i contributi della comunità e il miglioramento continuo, garantendo un'esperienza utente dinamica e in continua evoluzione.
</p>

## Table of Contents

- [Compatibilità](#-compatibilità)
- [Requisiti](#-requisiti)
- [Varianti di rilascio](#-varianti-di-rilascio)
- [Installazione](#-installazione)
- [Permessi](#-permessi)
- [Contributo](#-contributo)
- [FAQ](#-faq)
- [Crediti](#-crediti)
- [Licenza](#-licenza)
- [Dichiarazione di non responsabilità](#-dichiarazione-di-non-responsabilità)
- [Donazioni](#-donazioni)

> [!CAUTION]
> 
> Questa app richiede Magisk, KernelSU e APatch per l'accesso root e il framework XPosed/LSPosed. Qualsiasi altro metodo alternativo non funzionerà.

# 🧩 Compatibilità

Oxygen Customizer è compatibile con OxygenOS/ColorOS/RUI 16, 15, 14 e 13.

> [!WARNING]
> Se si utilizza una ROM basata su OOS15, utilizzare almeno le versioni beta.

# 🛠 Requisiti

- Oxygen OS 16, 15, 14 o 13 (verificare la compatibilità sopra)

- [Magisk](https://github.com/topjohnwu/Magisk) (Raccomandato) o [KernelSU](https://github.com/tiann/KernelSU) o [APatch](https://github.com/bmax121/APatch)

- [LSPosed](#lsposed)

# LSPosed

LSPosed ufficiale può essere trovato [qui](https://github.com/LSPosed/LSPosed).
In ogni caso questa versione è ora adatta solo per OxygenOS 14.

Ora puoi utilizzare diverse varianti:

- LSPosed IT (Internal Test),
- [LSPosed Irena](https://github.com/re-zero001/LSPosed-Irena)
- [LSPosed JingMatrix](https://github.com/JingMatrix/LSPosed)
- [ReLSPosed](https://github.com/ThePedroo/ReLSPosed)

Per tutti questi è necessario Zygisk (soprattutto su KSU).
Sono disponibili molti moduli Zygisk.
Se si sceglie di utilizzare LSPosed Internal Test, funzionerà qualsiasi versione di Zygisk.
Se scegli di utilizzare LSPosed Irena, ti consigliamo di utilizzare Zygisk Next.
Se scegli di utilizzare ReLSPosed, ti consigliamo di utilizzare ReZygisk.

> [!WARNING]
> Scarica sempre i moduli da fonti ufficiali e assicurati di utilizzare la build più recente
> dalla pagina delle azioni.

### Suggerimenti rapidi

OxygenOS 14: qualsiasi LSPosed
OxygenOS 15: LSPosed IT o LSPosed Irena 7280+ (Zygisk Next 534+) o ReLSPosed 7200+ (ReZygisk CI)
OxygenOS 16: LSposed IT o LSPosed Irena 7280+ (Zygisk Next 534+) o ReLSPosed 7200+ (ReZygisk CI)

# 📦 Varianti di rilascio

Questa mod è disponibile in tre varianti diverse:

### 🟢 **Stable**
Se stai utilizzando una ROM basata su OOS14 o OOS13.
<br>
<a href="https://github.com/DHD2280/Oxygen-Customizer/releases"><img src="https://img.shields.io/github/v/release/DHD2280/Oxygen-Customizer?style=for-the-badge&label=STABLE" alt="Stable"></a>
    
### 🟡 **Beta**
Aggiornato con correzioni e nuove funzionalità, se si utilizza una ROM basata su OOS15 o OOS16, 
si consiglia di utilizzare la variante nightly.
<br>
<a href="https://github.com/DHD2280/Oxygen-Customizer/releases"><img src="https://img.shields.io/github/v/release/DHD2280/Oxygen-Customizer?style=for-the-badge&include_prereleases&label=BETA" alt="Beta"></a>
  
### 🌒 **Nightly**
Questa variante viene rilasciata a ogni push su questo repository.
Può includere varie correzioni o nuove funzionalità che saranno disponibili in versione beta e stabile.
<br>
<a href="https://github.com/DHD2280/Oxygen-Customizer/actions/workflows/build_debug.yml"><img src="https://img.shields.io/github/actions/workflow/status/DHD2280/Oxygen-Customizer/build_debug.yml?branch=beta&label=Nightliy&style=for-the-badge" alt="Nightly Build"></a>

> [!WARNING]
>
> Se stai utilizzando OOS15 .850+ DEVI utilizzare LSPosed IT (Internal Test) o qualsiasi versione di LSPosed Irena 7280+ (consigliato con Zygisk Next 534+) o ReLSPosed 7200+ (consigliata con ReZygisk CI). Qualsiasi segnalazione di problemi con una versione LSPosed diversa o software incompatibile verrà chiusa.

# 💻 Installazione

  1. Scarica e installa l'app Oxygen Customizer.

  ### Installazione per utenti Magisk:

  2. Abilita il modulo Xposed nell'app LSPosed.

  3. Apri l'app, concedi i permessi di root e segui le istruzioni.

  4. Attendi che venga completata la generazione del modulo specifico della rom.

  5. Riavviare il dispositivo quando richiesto.

  ### Installazione per utenti KernelSU/APatch:

  2. Concedi l'autorizzazione SuperSU/root per Oxygen Customizer e SystemUI dall'app KernelSU

3. Disattiva l'opzione `Unmount modules` se disponibile nell'app.
   Se non ti interessa il rilevamento del sistema, abilita semplicemente OverlayFS
   o [configura meta modulo](#configura-meta-modulo).
  
  4. Apri l'app e attendi che finisca di generare il modulo specifico della rom.
  
  5. Riavviare il dispositivo quando richiesto.

# Configura Meta Modulo

Se utilizzi KernelSU (o Next) 3.0+, hai bisogno di meta-moduli per montare i moduli, perché ksu
non li monta.
Per OxygenOS è suggerito [Mountify](https://github.com/backslashxx/mountify),
in alternativa puoi provare [Magic Mount](https://github.com/7a72/meta-magic_mount/),
o [Hybrid Mount](https://github.com/Hybrid-Mount/meta-hybrid_mount).

> [!WARNING]
> Scarica sempre i moduli da fonti ufficiali e assicurati di utilizzare la build più recente
> dalla pagina delle azioni.

## Mountify

Dopo aver installato mountify è necessaria una piccola configurazione.
Aprire la Web UI di mountify e inserire i seguenti valori:

`mountify_mount = 2`  
`mount_device_name = KSU`  
Se stai utilizzando susfs 2.0.0+  
`mountify_custom_umount = 2`  
Se stai utilizzando una versione precedente di susfs  
`mountify_custom_umount = 1`

Dopo la configurazione, riavviare e continuare con l'installazione di Oxygen Customizer.

## Magic Mount

Magic Mount non richiede alcuna configurazione aggiuntiva da parte dell'utente.

# 🔒 Permessi

Questa app richiede le seguenti autorizzazioni:

`android.permission.ACCESS_NETWORK_STATE`  
`android.permission.INTERNET`  
`android.permission.ACCESS_FINE_LOCATION`  
`android.permission.ACCESS_COARSE_LOCATION`  
`android.permission.ACCESS_BACKGROUND_LOCATION`  
Per recuperare i dati meteo e i servizi basati sulla posizione, sono necessarie le autorizzazioni per accedere a Internet e alla posizione. Queste autorizzazioni sono essenziali per il corretto funzionamento del Lockscreen Weather e non vengono utilizzate per altri scopi.

`android.permission.USE_BIOMETRIC`  
È richiesta l'autorizzazione all'utilizzo dell'autenticazione biometrica per visualizzare la richiesta di autenticazione quando abilitata per il menu di riavvio avanzato.

`android.permission.VIBRATE`  
Per ottenere il feedback tattile quando si utilizza l'app, è necessaria l'autorizzazione a far vibrare il dispositivo.

`android.permission.WRITE_EXTERNAL_STORAGE`  
`android.permission.READ_EXTERNAL_STORAGE`  
`android.permission.MANAGE_EXTERNAL_STORAGE`  
Per salvare e caricare immagini/font personalizzati sono necessarie autorizzazioni di lettura e scrittura su dispositivi di archiviazione esterni. Queste autorizzazioni sono essenziali per il corretto funzionamento dell'app e non vengono utilizzate per altri scopi.

# 🤝 Contributo

Apprezziamo e accogliamo con piacere ogni tipo di contributo, dal codice alla documentazione, dalla grafica ai suggerimenti di progettazione, fino alle segnalazioni di bug. Vi invitiamo a sfruttare al meglio la piattaforma collaborativa di GitHub inviando Pull Request, fornendo tutorial o altri contenuti pertinenti. Qualunque cosa abbiate da offrire, la apprezziamo e possiamo utilizzarla efficacemente nel nostro progetto.

# 🤓 FAQ

<details>
  <summary>Ho bisogno di un accesso root affinché Oxygen Customizer funzioni?</summary>

- Sì, affinché Oxygen Customizer funzioni correttamente è necessario l'accesso root.
</details>

<details>
  <summary>Quali dispositivi supporta Oxygen Customizer?</summary>

- Oxygen Customizer supporta le ROM basate su Oxygen OS 13 e versioni successive. Essendo basato sul sistema Oxygen OS, 
  la compatibilità con ColorOS e Realme UI non è garantita, ma stiamo continuando a migliorare questo progetto per supportare 
  tutti e tre i sistemi contemporaneamente.
</details>

<details>
  <summary>È supportata la versione "xx" di Android?</summary>

- Oxygen Customizer supporta ufficialmente Android 14 e versioni successive. Non è garantita la compatibilità con le versioni precedenti di Android e non è previsto il supporto per tali versioni.
</details>

<details>
  <summary>Ho avuto bootloop. Come posso risolverlo?</summary>

- Avvia in [Safe Mode](https://www.androidauthority.com/how-to-enter-safe-mode-android-801476/) e disinstallare il modulo.
- Per KernelSU e i suoi fork: [Guida](https://kernelsu.org/guide/rescue-from-bootloop.html#brick-by-modules).
</details>

<details>
  <summary>Qual è la differenza tra Release build e Debug build?</summary>

- [Release build](https://github.com/DHD2280/Oxygen-Customizer/releases/latest) è una versione ottimizzata pensata per la distribuzione agli utenti finali, mentre [Debug build](https://github.com/DHD2280/Oxygen-Customizer/actions) include funzionalità e informazioni aggiuntive per scopi di debug e sviluppo.
</details>

<details>
  <summary>Posso usare Oxygen Customizer insieme ad altre app di personalizzazione?</summary>

- Sì, Oxygen Customizer può essere utilizzato insieme ad altre app di personalizzazione. Tuttavia, è importante notare che potrebbero verificarsi conflitti o modifiche sovrapposte, che potrebbero influire sull'esperienza utente complessiva.
</details>

<details>
  <summary>Ho trovato un bug. Come posso segnalarlo?</summary>

- Assicurati di eseguire l'ultima versione nightly prima di creare un nuovo problema!
- Per segnalare un bug, vai alla sezione [Issues](https://github.com/DHD2280/Oxygen-Customizer/issues/new/choose). Crea un nuovo problema e assicurati di selezionare il modello `Bug Report`. Fornire quante più informazioni dettagliate possibili, inclusi i passaggi per riprodurre il bug e tutti i messaggi di errore o screenshot pertinenti.
</details>

<details>
  <summary>Come posso richiedere una nuova funzionalità?</summary>

- Se hai una richiesta di funzionalità, vai alla sezione [Issues](https://github.com/DHD2280/Oxygen-Customizer/issues/new/choose). Crea un nuovo problema e scegli il modello `Feature Request`. Assicurati di includere dettagli completi sulla funzionalità desiderata, sui suoi potenziali vantaggi e su qualsiasi altra informazione rilevante che possa aiutare a comprendere e valutare la richiesta.
</details>

<details>
  <summary>Dove posso fare una donazione?</summary>

- Puoi donare tramite "[PayPal](https://www.paypal.me/luigifale)", o tarmite "[Buy me a coffee](https://www.buymeacoffee.com/DHD2280)".
</details>

# ❤ Crediti

### Grazie a:

- [Android Open Source Project (AOSP)](https://source.android.com) per il codice sorgente di Android.
- [OnePlus](https://www.oneplus.com) per Oxygen OS.
- [Material Icons](https://fonts.google.com/icons) per le icone in-app.
- [PixelXpert](https://github.com/siavash79/PixelXpert), [@siavash79](https://github.com/siavash79) per l'aiuto con le mod Xposed e per il suo fantastico lavoro con Pixel Xpert.
- [Iconify](https://github.com/Mahmud0808/Iconify), [@Mahmud0808](https://github.com/Mahmud0808) per aver portato l'idea di Iconify e il suo fantastico lavoro.
- [crDroid](https://github.com/crdroidandroid) per Pulse Controller e alcune modifiche.
- [OmniROM](https://github.com/omnirom) per i fornitori di servizi meteorologici.
- [Project Matrixx](https://github.com/ProjectMatrixx) per alcune illustrazioni.
- [SuperiorExtended](https://github.com/SuperiorExtended) per alcune modifiche e layout.
- E tutti coloro che hanno [contribuito](/docs/contributors.md) e [tradotto](/docs/translators.md)... :)

# © Licenza

Oxygen Customizer è concesso in licenza con GPLv3. Consultare [`LICENSE`](/LICENSE.md) per il testo completo della licenza.

# 📝 Dichiarazione di non responsabilità

> [!WARNING]
> - Si prega di notare che Oxygen Customizer potrebbe non essere completamente compatibile con tutte le varianti OOS personalizzate.
> - Non posso essere ritenuto responsabile per eventuali danni o problemi che potrebbero verificarsi al tuo dispositivo durante l'utilizzo di Oxygen Customizer.

# ⭐ Donazioni

[<img src="/.github/resources/PayPal.svg"
alt='Donate with PayPal'
height="80">](https://www.paypal.me/luigifale) [<img src="/.github/resources/BMC.svg"
alt='Donate with BMC'
height="80">](https://www.buymeacoffee.com/DHD2280)
