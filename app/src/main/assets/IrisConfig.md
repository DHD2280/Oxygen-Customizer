# Iris Config

1. **Ambient Light**
2. **Color Temperature**
3. **Color Gamut**
4. **HDR**
5. **MAXCLL**
6. **ABP (Analog Bypass Mode)**
7. **MEMC**
8. **Display Brightness**
9. **SD2RHDR**
10. **SR**

## Ambient Light:

- **Type: 44**  
  **Feature:** AL control  
  **Param 0:** AL control
    - 0: Disable
    - 1: Enable (Recommended)  
      **Example:**  
      `irisConfig 44 1 1` - Enable ambient light

- **Type: 34**  
  **Feature:** AL level  
  **Param 0:** AL level
    - Range: 4000~30000 (step is 1000)  
      **Example:**  
      `irisConfig 34 1 10000` - Set Ambient light level (lux): 10000

## Color Temperature:

- **Type: 39**  
  **Feature:** Color temperature mode  
  **Param 0:** CT mode
    - 0: Disable
    - 1: Manual mode
    - 2: Auto mode (Recommended)  
      **Example:**  
      `irisConfig 39 1 2` - Enable color temperature auto mode

- **Type: 48**  
  **Feature:** Color temperature value  
  **Param 0:** CT value (K)
    - Range: 2800~10000 (step is 100)
    - Recommended: 6500  
      **Example:**  
      `irisConfig 48 1 6500` - Set color temperature value (K): 6500

- **Type: 35**  
  **Feature:** CCT value (K)  
  **Param 0:** CT mode
    - Range: 2800~10000 (step is 25)  
      **Example:**  
      `irisConfig 35 1 5000` - Set CCT value (K): 5000

## Color Gamut:

- **Type: 40**  
  **Feature:** Color Gamut mode  
  **Param 0:** Gamut table
    - 0: Off
    - 1: Native mode (Recommended)
    - 2: Vivid without CAM
    - 3: Vivid with High CAM
    - 4: sRGB
    - 5: P3
    - 6: BT 2020  
      **Example:**  
      `irisConfig 40 1 1` - Enable native mode

## HDR:

- **Type: 47**  
  **Feature:** HDR mode  
  **Param 0:** HDR mode
    - 0: Disable (Recommended)
    - 61: HLG HDR
    - 62: HDR10
    - 3: Online video
    - 4: Short video
    - 5: Local video
    - 6: Gallery photo
    - 7: Game
    - 8: Reading
    - 9: AI Online video
    - 10: AI Short video
    - 11: AI Local video
    - 12: AI Gallery photo
    - 13: AI Game
    - 14: AI Reading  
      **Example:**  
      `irisConfig 47 1 0` - Disable HDR mode

## MAXCLL:

- **Type: 49**  
  **Feature:** MAXCLL value  
  **Param 0:** MAXCLL value
    - Range: 1000~4000  
      **Example:**  
      `irisConfig 49 1 2000` - Set MAXCLL value to 2000

## ABP (Analog Bypass Mode):

- **Type: 56**  
  **Feature:** ABP mode  
  **Param 0:** ABP mode
    - 0: Exit ABP mode
    - 1: Enter ABP mode
    - 16: Standby ABP mode
    - 32: Sleep ABP mode  
      **Example:**  
      `irisConfig 56 1 1` - Enter ABP mode

## MEMC (HDR Formal Solution):

- **Type: 258**  
  **Feature:** Enable/Disable MEMC/HDR  
  **Param 0:** Mode setting
    - 0: Disable HDR
    - 2: Enable HDR
    - 10: Enable single MEMC
    - 14: Enable TNR
    - 40: Enable dual MEMC  
      **Param 1:** Ignored (-1)  
      **Param 2:** Scene setting
    - MEMC scene (Bit0~3)
        - 0: Normal video
        - 1: MMI test
        - 2: Normal game
        - 3: eMV game mode 1
        - 4: eMV game mode 2
        - 5: Short video
        - 6: eMV API allowed
    - Low latency mode (Bit4~7)
        - 0: Disable low latency
        - 1: Normal latency
        - 2: Low latency
        - 3: Ultra-low latency
    - MEMC level (Bit8~11)
        - 0: Level 0
    - N2M mode (Bit12~15)
        - 0: Disable N2M
        - 1: Enable N2M  
          **Example:**  
          `irisConfig 258 1 40` - Enable dual MEMC mode

  **Example (Game settings):**  
  `irisConfig 258 6 40 -1 3 -1 1 30 0`  
  Enable dual MEMC mode, eMV game mode 1, normal latency, MEMC level 0, disable N2M, pre-defined setting for WangZheRongYao, set 30fps

## Display Brightness:

- **Type: 82**  
  **Feature:** Display brightness parameters  
  **Param 0:** Dimming value
    - 0: Primary display (Recommended)
    - 1: Secondary display  
      **Param 1:** Dimming brightness
    - Range: 0~8191  
      **Param 2:** Brightness mode
    - 0: Smooth brightness (Recommended)
    - 1: DC dimming combine  
      **Example:**  
      `irisConfig 82 3 0 6000 0` - Set Primary display, brightness level 6000, Smooth brightness mode

## SDR2HDR:

- **Type: 267**  
  **Feature:** Set SDR2HDR Setting  
  **Param 0:** Setting Type
    - 3: Pre-defined SDR2HDR setting (Recommended)  
      **Param 1:** Pre-defined Setting
    - 0: All off
    - 1: Video (Recommended)
    - 2: Enhanced video
    - 3: Game
    - 4: Low power video
    - 10: Dark area brightening filter
    - 11: Anti-snow blindness filter
    - 12: Movie mode
    - 13: Vivid mode
    - 14: Bright mode
    - 15: Gentle mode
    - 16: LOL
    - 17: YuanShen
    - 18: HePingJingYing
    - 19: WangZheRongYao
    - 20: Revelation style
    - 21: GunsGirl style
    - 22: Identity-V style
    - 23: Onmyoji style  
      **Example:**  
      `irisConfig 267 2 3 1` - Enable SDR2HDR, using pre-defined setting -- Video

## SR:

- **Type: 273**  
  **Feature:** Set SR Setting  
  **Param 0:** Setting Type
    - 0: Disable SR (Recommended)
    - 1: Enable SR  
      **Param 1:** Game App list
    - 0: HePingJingYing
    - 1: WanMeiShiJie
    - 2: TianDao
    - 3: YuanShen
    - 4: BengHuai3
    - 5: GuiQi
    - 6: TianYu
    - 7: ZhanShenYiJi
    - 8: DiWuRenGe
    - 9: TBD
    - 100: BuLuoChongTu (WaiXiaoJiXing)
    - 101: ShiMingZhaoHuan (WaiXiaoJiXing)
    - 102: YuanShen (WaiXiaoJiXing)  
      **Example:**  
      `irisConfig 273 1 0` - Disable SR  
      `irisConfig 273 1 1 0` - Enable SR with preset values for HePingJingYing