# 📱 Weadr - Android Weather Station App
Weadr is the official mobile companion for the IoT Arduino Weather Station.   
It provides a high-performance, real-time dashboard for monitoring environmental data, featuring a dynamic UI that adapts to the time of day and specific weather hazards.  

## ✨ Features
Real-Time Sync: Data is fetched from the Firebase Realtime Database every second. 

Adaptive UI (5-Phase System): The background and interface colors transition through five distinct states: Night, Sunrise, Day, Sunset, and Late Night.  

Intelligent Weather Status: A custom trapezoidal status bar at the top provides 15 possible background variations and 4 text status types based on combined temperature, wind, and rain data.  

Historical Data Visualization: Integrated line charts (via MPAndroidChart) allow users to track trends in environmental parameters over time.  

Safety Alerts: Built-in notification system for storms, high gas concentrations, or station failures.  

## 🛠️ Tech Stack  
Language: Kotlin (100% interoperable with Java segments).  
Architecture: View Binding for efficient UI-data linking.  
Libraries:  
- Firebase SDK: Real-time data synchronization.  
- PAndroidChart: High-quality historical data graphing.  
- Jetpack Compose & Layout Editor: For the modern, responsive UI.  
- Compatibility: Supports Android 7.0 (Nougat) and newer (API 24+).  

##📂 Project Structure 
app/  
├── src/main/java/.../   
│   ├── MainActivity.kt                          # Flash/Splash screen logic     
│   └── Home.kt                                  # Core logic: Firebase sync & UI updates  
├── res/
│   ├── layout/
│   │   ├── activity_main.xml                    # Splash screen layout  
│   │   └── activity_home.xml                    # Dashboard & Grid structure  
│   └── drawable/                                # Custom weather icons and status backgrounds  
└── AndroidManifest.xml                          # Permissions (Notifications, Hardware Accel)  

app/<br>
 ├── src/main/java/...//<br>
 │  ├── MainActivity.kt/<br>
 │  └── Home.kt /<br>
 ├── res/<br>
 │    ├── layout//<br>
 │    │  ├── activity_main.xml<br>
 │    │  └── activity_home.xml<br>
 │    └── drawable//<br>
 └── AndroidManifest.xml

## 📊 UI Logic: The Time Cycle
The app divides the 24-hour cycle into five frames to enhance user experience:  
| Phase       | Time Range      | UI Theme                        |  
|-------------|-----------------|---------------------------------|   
| Night       | 00:00 - 05:29   | Deep Blue / High Contrast       |  
| Sunrise     | 05:30 - 06:59   | Soft Teal & Orange Gradient     |  
| Day         | 07:00 - 19:59   | Bright / Minimalist             |  
| Sunset      | 20:00 - 21:59   | Warm Purple & Orange            |  
| Late Night  | 22:00 - 23:59   | Deep Blue                       |  
