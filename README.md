# 🏝️ 海島災害生存指南 (Island Disaster Survival Guide)

> **2024 國科會大專生研究計畫作品**
> 一款整合「離線地圖導航」、「硬體感測器」與「物資管理」的原生 Android 防災應用程式。

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white) ![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white) ![Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=googleplay&logoColor=white)

## 專案背景 (Background)
台灣位處環太平洋地震帶，災害頻發。現有的防災應用多仰賴網路查詢，但在極端災難（如基地台損毀）下，使用者往往失去資訊來源。
本計畫旨在開發一款 **「離線優先 (Offline-First)」** 的生存指南，結合圖資快取、手機內建感測器與本地資料庫，確保在 **無網路、無 GPS 訊號** 的極端環境下，仍能提供避難導航與生存支援。

## 核心功能 (Key Features)

### 1. 🧭 離線避難導航 (Offline Navigation)
針對網路中斷情境，開發「無網路步數導航系統」：
* **感測器整合**：利用手機內建 **加速度計 (G-Sensor)** 計算步數，結合 **電子羅盤 (Magnetometer)** 判定方位。
* **訊號處理演算法**：實作 **移動平均法 (Moving Average Filter)** 處理感測器數據，有效過濾手持晃動產生的雜訊 (Noise)，提升步數計算精準度。
* **距離演算**：應用 **球面三角學 (Haversine Formula/Spherical Trigonometry)** 計算兩點間經緯度距離，在無 GPS 訊號下推算移動距離。

### 2. 🗺️ 避難所與地圖整合 (Map Integration)
* **Google Maps Platform**：串接 API 顯示附近避難所資訊（容納人數、適用災害類型）。
* **常用地點快取**：使用 **Room Database** 實作本地資料庫，允許使用者預先儲存「常用避難點」（如學校、住家），確保斷網時仍可讀取關鍵座標。

### 3. 🎒 物資庫存管理 (Supply Management)
* **數位化盤點**：提供物資拍照、分類、數量管理功能。
* **效期追蹤**：記錄物資有效期限，協助使用者進行資源盤點與風險控制。
* **智慧建議**：根據家庭成員結構（如嬰兒、寵物、長者），自動推薦對應的防災物資清單。

### 4. 🆘 緊急應變模組 (Emergency Response)
* **摩斯密碼求救**：將當前經緯度座標自動轉譯為 **摩斯密碼 (Morse Code)**，並控制手機閃光燈與揚聲器發送聲光求救訊號。
* **一鍵報案**：整合 Android **SmsManager** 與通話功能，實作 119 一鍵撥號與緊急簡訊推播。
* **醫療資訊卡**：建立個人緊急醫療檔案（血型、過敏史），供救難人員快速檢視。

## App截圖 (Screenshots)

| 首頁與導航 | 物資管理 | 緊急求救 |
|:---:|:---:|:---:|
| <img src="https://github.com/AinsleeWang/Island-Disaster-Survival-App/blob/389ea142efa9afa28f3c8d3f9d2101fc094ed59e/demoimages/nav.png" width="250" alt="離線導航介面"/> | <img src="https://github.com/AinsleeWang/Island-Disaster-Survival-App/blob/389ea142efa9afa28f3c8d3f9d2101fc094ed59e/demoimages/supply.png" width="250" alt="物資管理介面"/> | <img src="https://github.com/AinsleeWang/Island-Disaster-Survival-App/blob/389ea142efa9afa28f3c8d3f9d2101fc094ed59e/demoimages/sos.png" width="250" alt="緊急求救介面"/> |



## 技術堆疊 (Tech Stack)

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Material Design 3)
* **Architecture:** MVVM (Model-View-ViewModel)
* **Local Storage:** Room Database (SQLite), DataStore
* **Sensors:** Accelerometer, Magnetometer (SensorManager)
* **Maps:** Google Maps SDK for Android
* **Build Tool:** Gradle (Version Catalog management)

## 使用者研究 (User Research)
本專案在開發過程中導入量化研究方法，確保功能符合真實需求：
* **研究方法**：結構式問卷調查 (N=72)。
* **數據分析**：運用 **Friedman 檢定** 分析使用者需求優先級。
* **研究發現**：統計顯示使用者對「離線導航」(加權分 2.58) 的需求顯著高於其他功能，據此調整開發權重，優先優化感測器演算法。

## 如何執行 (Getting Started)

為了保護 API 金鑰安全，本專案未上傳 `local.properties`。請依照以下步驟設定環境：

1.  **Clone 專案**
    ```bash
    git clone [https://github.com/YourUsername/Island-Disaster-Survival-App.git](https://github.com/YourUsername/Island-Disaster-Survival-App.git)
    ```
2.  **設定 API Key**
    在專案根目錄建立 `local.properties` 檔案，並填入您的 Google Maps API Key：
    ```properties
    sdk.dir=/Users/yourname/Library/Android/sdk
    MAPS_API_KEY=YOUR_REAL_API_KEY_HERE
    ```
3.  **編譯與執行**
    使用 Android Studio 開啟專案，同步 Gradle 後即可安裝至模擬器或實體裝置。

---

部分功能&WebApp版本仍在實作的路上⋯⋯

**Developed by wit.et**
