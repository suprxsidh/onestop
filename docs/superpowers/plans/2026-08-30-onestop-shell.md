# OneStop Shell & Battery Module Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Stand up the OneStop Android app — a new Gradle project with BatteryLab's existing functionality rehoused as its first module, presented through a dashboard-of-tiles home screen, with placeholder tiles for the not-yet-built Gestures and System Info modules.

**Architecture:** Single Gradle app module, package-per-feature. BatteryLab's code (`~/opencode-projects/batterylab`) is copied near-verbatim into `com.suprxsidh.onestop.battery.*`, keeping its existing internal layout and tests. A new top-level dashboard (`com.suprxsidh.onestop.ui.dashboard`) shows a hero Battery tile (backed by a small state-projection mapper over the ported repository) plus two disabled placeholder tiles, and navigates into the Battery module's existing 5-screen internal nav on tap.

**Tech Stack:** Kotlin, Jetpack Compose, Material3, Room, WorkManager, JUnit4 + Robolectric.

**Spec:** `~/claudecode-projects/onestop/docs/superpowers/specs/2026-08-30-onestop-shell-design.md`

## Global Constraints

- Package `com.suprxsidh.onestop`, minSdk 26, targetSdk/compileSdk 34 (exact values from spec §2).
- Kotlin + Jetpack Compose, native only — no Flutter/RN/KMM.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17` and `ANDROID_HOME=/opt/homebrew/share/android-commandlinetools` must be exported before every `./gradlew` invocation.
- Debug-signed sideload only — no Play Store, no release signing config.
- Single Gradle app module, package-per-feature (`battery/`, later `gestures/`, `sysinfo/`) — no `:feature:*` Gradle modules (spec §3).
- BatteryLab's repo (`~/opencode-projects/batterylab`, `suprxsidh/batterylab` on GitHub) stays untouched — code is copied from it, never moved or deleted there.
- No GitHub repo creation/push in this plan — local git only (spec §8).

---

### Task 1: Project scaffold

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/suprxsidh/onestop/MainActivity.kt`
- Create: `app/src/main/res/drawable/ic_launcher.xml`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/values/themes.xml`
- Copy: `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.properties`, `gradle/wrapper/gradle-wrapper.jar` (from `~/opencode-projects/batterylab`, unchanged — generic Gradle boilerplate)

**Interfaces:**
- Produces: a buildable, launchable placeholder app (`MainActivity` shows the text "OneStop"). Later tasks replace `MainActivity`'s content and add source under `app/src/main/java/com/suprxsidh/onestop/`.

- [ ] **Step 1: Copy the Gradle wrapper verbatim**

```bash
cd ~/claudecode-projects/onestop
mkdir -p gradle/wrapper
cp ~/opencode-projects/batterylab/gradlew .
cp ~/opencode-projects/batterylab/gradlew.bat .
cp ~/opencode-projects/batterylab/gradle/wrapper/gradle-wrapper.properties gradle/wrapper/
cp ~/opencode-projects/batterylab/gradle/wrapper/gradle-wrapper.jar gradle/wrapper/
chmod +x gradlew
```

- [ ] **Step 2: Write `settings.gradle.kts`**

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "OneStop"
include(":app")
```

- [ ] **Step 3: Write root `build.gradle.kts`**

```kotlin
plugins {
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("com.google.devtools.ksp") version "1.9.24-1.0.20" apply false
}
```

- [ ] **Step 4: Write `gradle.properties`**

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.nonTransitiveRClass=true
kotlin.code.style=official
```

- [ ] **Step 5: Write `app/build.gradle.kts`**

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.suprxsidh.onestop"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.suprxsidh.onestop"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
    packaging {
        resources.excludes.add("META-INF/LICENSE*")
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("androidx.compose.material:material-icons-extended:1.6.8")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.13")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("androidx.work:work-testing:2.9.1")
    testImplementation("androidx.room:room-testing:2.6.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
```

- [ ] **Step 6: Write `app/src/main/AndroidManifest.xml`** (service/permissions for the Battery module are added in Task 3, not here)

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:name=".OneStopApplication"
        android:allowBackup="true"
        android:icon="@drawable/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.OneStop">

        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>
</manifest>
```

Note: this manifest references `.OneStopApplication`, which does not exist until Task 3. That's expected — this task's build check (Step 9) only compiles resources and `MainActivity`, so create a temporary trivial `Application` stub in this step to keep the manifest valid until Task 3 replaces it:

- [ ] **Step 7: Write a temporary `OneStopApplication` stub** (Task 3 replaces this file's contents entirely)

`app/src/main/java/com/suprxsidh/onestop/OneStopApplication.kt`:
```kotlin
package com.suprxsidh.onestop

import android.app.Application

class OneStopApplication : Application()
```

- [ ] **Step 8: Write `MainActivity.kt`** (Task 6 replaces its content to use the real dashboard)

```kotlin
package com.suprxsidh.onestop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Text("OneStop")
            }
        }
    }
}
```

- [ ] **Step 9: Write resources**

`app/src/main/res/values/strings.xml`:
```xml
<resources>
    <string name="app_name">OneStop</string>
</resources>
```

`app/src/main/res/values/themes.xml`:
```xml
<resources>
    <style name="Theme.OneStop" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```

`app/src/main/res/drawable/ic_launcher.xml` (dark charcoal ground + copper bolt, matching the approved dashboard mockup's palette rather than BatteryLab's green):
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
    <path android:fillColor="#1B1A20" android:pathData="M0,0h108v108h-108z" />
    <path
        android:fillColor="#B0591C"
        android:pathData="M40,22h10v-8h16v8h10v66h-36z" />
</vector>
```

- [ ] **Step 10: Verify the scaffold builds**

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
cd ~/claudecode-projects/onestop
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`, output at `app/build/outputs/apk/debug/app-debug.apk`.

- [ ] **Step 11: Commit**

```bash
git add -A
git commit -m "Scaffold OneStop Gradle project with placeholder MainActivity"
```

---

### Task 2: Port Battery data + calc + health layers

**Files:**
- Copy (package renamed `com.suprxsidh.batterylab.data` → `com.suprxsidh.onestop.battery.data`):
  `AppDatabase.kt`, `ChargeSession.kt`, `ChargeSessionDao.kt`, `HealthEstimate.kt`, `HealthEstimateDao.kt`, `Reading.kt`, `ReadingDao.kt`
- Copy (package renamed `com.suprxsidh.batterylab.calc` → `com.suprxsidh.onestop.battery.calc`):
  `ChargerClassifier.kt`, `DesignCapacityResolver.kt`, `DischargeCalculator.kt`, `HealthEstimator.kt`, `InsightsEngine.kt`, `SessionAggregator.kt`, `TimeEstimator.kt`, `WattCalculator.kt`
- Copy (package renamed `com.suprxsidh.batterylab.health` → `com.suprxsidh.onestop.battery.health`):
  `DesignCapacityStore.kt`, `HealthEstimatePersister.kt`
- Copy tests (same package rename, from `~/opencode-projects/batterylab/app/src/test/java/com/suprxsidh/batterylab/...`):
  `calc/ChargerClassifierTest.kt`, `calc/DesignCapacityResolverTest.kt`, `calc/DischargeCalculatorTest.kt`, `calc/HealthEstimatorTest.kt`, `calc/InsightsEngineTest.kt`, `calc/SessionAggregatorTest.kt`, `calc/TimeEstimatorTest.kt`, `calc/WattCalculatorTest.kt`, `data/AppDatabaseTest.kt`, `health/HealthEstimatePersisterTest.kt`
- Modify: `app/src/main/java/com/suprxsidh/onestop/battery/data/AppDatabase.kt` — change Room DB filename from `"batterylab.db"` to `"onestop.db"`.

**Interfaces:**
- Produces: `com.suprxsidh.onestop.battery.data.AppDatabase.getInstance(context): AppDatabase`, exposing `readingDao(): ReadingDao`, `chargeSessionDao(): ChargeSessionDao`, `healthEstimateDao(): HealthEstimateDao` — same shape as BatteryLab's, consumed by Task 3 (receiver/service/worker), Task 4 (UI screens), Task 5 (tile mapper), Task 6 (dashboard).

- [ ] **Step 1: Copy source files with package rename**

```bash
cd ~/claudecode-projects/onestop
SRC=~/opencode-projects/batterylab/app/src/main/java/com/suprxsidh/batterylab
DST=app/src/main/java/com/suprxsidh/onestop/battery
mkdir -p "$DST/data" "$DST/calc" "$DST/health"

for f in data/AppDatabase.kt data/ChargeSession.kt data/ChargeSessionDao.kt data/HealthEstimate.kt data/HealthEstimateDao.kt data/Reading.kt data/ReadingDao.kt \
         calc/ChargerClassifier.kt calc/DesignCapacityResolver.kt calc/DischargeCalculator.kt calc/HealthEstimator.kt calc/InsightsEngine.kt calc/SessionAggregator.kt calc/TimeEstimator.kt calc/WattCalculator.kt \
         health/DesignCapacityStore.kt health/HealthEstimatePersister.kt; do
  sed 's/^package com\.suprxsidh\.batterylab\./package com.suprxsidh.onestop.battery./; s/com\.suprxsidh\.batterylab\./com.suprxsidh.onestop.battery./g' \
    "$SRC/$f" > "$DST/$f"
done
```

- [ ] **Step 2: Fix the DB filename**

Open `app/src/main/java/com/suprxsidh/onestop/battery/data/AppDatabase.kt` and change:
```kotlin
                    "batterylab.db"
```
to:
```kotlin
                    "onestop.db"
```

- [ ] **Step 3: Copy test files with package rename**

```bash
cd ~/claudecode-projects/onestop
SRC=~/opencode-projects/batterylab/app/src/test/java/com/suprxsidh/batterylab
DST=app/src/test/java/com/suprxsidh/onestop/battery
mkdir -p "$DST/data" "$DST/calc" "$DST/health"

for f in calc/ChargerClassifierTest.kt calc/DesignCapacityResolverTest.kt calc/DischargeCalculatorTest.kt calc/HealthEstimatorTest.kt calc/InsightsEngineTest.kt calc/SessionAggregatorTest.kt calc/TimeEstimatorTest.kt calc/WattCalculatorTest.kt \
         data/AppDatabaseTest.kt \
         health/HealthEstimatePersisterTest.kt; do
  sed 's/^package com\.suprxsidh\.batterylab\./package com.suprxsidh.onestop.battery./; s/com\.suprxsidh\.batterylab\./com.suprxsidh.onestop.battery./g' \
    "$SRC/$f" > "$DST/$f"
done
```

- [ ] **Step 4: Run the ported tests**

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
./gradlew test --tests "com.suprxsidh.onestop.battery.*"
```

Expected: all ported tests `PASS` (they're unchanged logic, only package paths differ — a failure here means the `sed` rename missed a reference, not a real regression).

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "Port BatteryLab data/calc/health layers into battery module"
```

---

### Task 3: Port Battery receiver/work/service layer, wire into OneStopApplication

**Files:**
- Copy (package renamed `com.suprxsidh.batterylab.receiver` → `com.suprxsidh.onestop.battery.receiver`): `BatteryReadingParser.kt`, `BatteryReceiver.kt`
- Copy (package renamed `com.suprxsidh.batterylab.work` → `com.suprxsidh.onestop.battery.work`): `SamplingWorker.kt`
- Copy (package renamed `com.suprxsidh.batterylab.service` → `com.suprxsidh.onestop.battery.service`): `ChargeSessionService.kt`
- Copy tests (same rename): `receiver/BatteryReadingParserTest.kt`, `receiver/BatteryReceiverTest.kt`, `work/SamplingWorkerTest.kt`
- Modify: `app/src/main/java/com/suprxsidh/onestop/OneStopApplication.kt` (replace Task 1's stub)
- Modify: `app/src/main/AndroidManifest.xml` (add permissions + service declaration)
- Create: `app/src/test/java/com/suprxsidh/onestop/OneStopApplicationTest.kt` (ported from `BatteryLabApplicationTest.kt`)

**Interfaces:**
- Consumes: `com.suprxsidh.onestop.battery.data.AppDatabase` (Task 2).
- Produces: `OneStopApplication` registers `BatteryReceiver` and enqueues `SamplingWorker` on process start — same behavior BatteryLab has today, just renamed.

- [ ] **Step 1: Copy source + test files with package rename**

```bash
cd ~/claudecode-projects/onestop
SRC=~/opencode-projects/batterylab/app/src/main/java/com/suprxsidh/batterylab
DST=app/src/main/java/com/suprxsidh/onestop/battery
mkdir -p "$DST/receiver" "$DST/work" "$DST/service"

for f in receiver/BatteryReadingParser.kt receiver/BatteryReceiver.kt work/SamplingWorker.kt service/ChargeSessionService.kt; do
  sed 's/^package com\.suprxsidh\.batterylab\./package com.suprxsidh.onestop.battery./; s/com\.suprxsidh\.batterylab\./com.suprxsidh.onestop.battery./g' \
    "$SRC/$f" > "$DST/$f"
done

TSRC=~/opencode-projects/batterylab/app/src/test/java/com/suprxsidh/batterylab
TDST=app/src/test/java/com/suprxsidh/onestop/battery
mkdir -p "$TDST/receiver" "$TDST/work"
for f in receiver/BatteryReadingParserTest.kt receiver/BatteryReceiverTest.kt work/SamplingWorkerTest.kt; do
  sed 's/^package com\.suprxsidh\.batterylab\./package com.suprxsidh.onestop.battery./; s/com\.suprxsidh\.batterylab\./com.suprxsidh.onestop.battery./g' \
    "$TSRC/$f" > "$TDST/$f"
done
```

- [ ] **Step 2: Replace the `OneStopApplication` stub with real wiring**

`app/src/main/java/com/suprxsidh/onestop/OneStopApplication.kt`:
```kotlin
package com.suprxsidh.onestop

import android.app.Application
import android.content.IntentFilter
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.suprxsidh.onestop.battery.receiver.BatteryReceiver
import com.suprxsidh.onestop.battery.work.SamplingWorker
import java.util.concurrent.TimeUnit

class OneStopApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        registerBatteryReceiver()
        enqueueSamplingWork()
    }

    private fun registerBatteryReceiver() {
        val filter = IntentFilter().apply {
            addAction(android.content.Intent.ACTION_BATTERY_CHANGED)
            addAction(android.content.Intent.ACTION_POWER_CONNECTED)
            addAction(android.content.Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(BatteryReceiver(), filter)
    }

    private fun enqueueSamplingWork() {
        val request = PeriodicWorkRequestBuilder<SamplingWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "battery_sampling",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
```

- [ ] **Step 3: Port the Application test**

```bash
cd ~/claudecode-projects/onestop
SRC=~/opencode-projects/batterylab/app/src/test/java/com/suprxsidh/batterylab/BatteryLabApplicationTest.kt
sed 's/^package com\.suprxsidh\.batterylab/package com.suprxsidh.onestop/; s/com\.suprxsidh\.batterylab\./com.suprxsidh.onestop./g; s/BatteryLabApplication/OneStopApplication/g' \
  "$SRC" > app/src/test/java/com/suprxsidh/onestop/OneStopApplicationTest.kt
```

- [ ] **Step 4: Update the manifest**

Replace `app/src/main/AndroidManifest.xml` with:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_HEALTH" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application
        android:name=".OneStopApplication"
        android:allowBackup="true"
        android:icon="@drawable/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.OneStop">

        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service
            android:name=".battery.service.ChargeSessionService"
            android:foregroundServiceType="health"
            android:exported="false" />

    </application>
</manifest>
```

- [ ] **Step 5: Run tests and build**

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
./gradlew test --tests "com.suprxsidh.onestop.*"
./gradlew assembleDebug
```

Expected: all tests `PASS`, `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "Port battery receiver/work/service, wire into OneStopApplication"
```

---

### Task 4: Port Battery UI screens and internal navigation

**Files:**
- Copy (package renamed `com.suprxsidh.batterylab.ui.charging` → `com.suprxsidh.onestop.battery.ui.charging`): `ChargingScreen.kt`
- Copy (package renamed `com.suprxsidh.batterylab.ui.discharge` → `com.suprxsidh.onestop.battery.ui.discharge`): `DischargeScreen.kt`
- Copy (package renamed `com.suprxsidh.batterylab.ui.history` → `com.suprxsidh.onestop.battery.ui.history`): `HistoryScreen.kt`
- Copy (package renamed `com.suprxsidh.batterylab.ui.insights` → `com.suprxsidh.onestop.battery.ui.insights`): `InsightsScreen.kt`
- Copy (package renamed `com.suprxsidh.batterylab.ui.common` → `com.suprxsidh.onestop.battery.ui.common`): `LineChart.kt`
- Create (renamed from `ui/dashboard/DashboardScreen.kt` + `DashboardViewModel.kt` to avoid colliding with OneStop's own top-level dashboard): `app/src/main/java/com/suprxsidh/onestop/battery/ui/home/BatteryHomeScreen.kt`, `BatteryHomeViewModel.kt`
- Create (renamed from `ui/nav/BatteryLabNavHost.kt`): `app/src/main/java/com/suprxsidh/onestop/battery/ui/nav/BatteryNavHost.kt`

**Interfaces:**
- Consumes: `com.suprxsidh.onestop.battery.data.AppDatabase` (Task 2).
- Produces: `@Composable fun BatteryNavHost(db: AppDatabase)` — the Battery module's full 5-tab screen (Now/Charging/Discharge/History/Insights). Consumed by Task 6's top-level `OneStopNavHost` when the user drills into the Battery tile.

Note: none of these files have existing unit tests in BatteryLab (they're Composables, not tested there either) — this task's verification is a successful build, matching the existing codebase's own testing pattern rather than inventing new UI tests for old code.

- [ ] **Step 1: Copy the four screens + shared chart with package rename**

```bash
cd ~/claudecode-projects/onestop
SRC=~/opencode-projects/batterylab/app/src/main/java/com/suprxsidh/batterylab/ui
DST=app/src/main/java/com/suprxsidh/onestop/battery/ui
mkdir -p "$DST/charging" "$DST/discharge" "$DST/history" "$DST/insights" "$DST/common"

for f in charging/ChargingScreen.kt discharge/DischargeScreen.kt history/HistoryScreen.kt insights/InsightsScreen.kt common/LineChart.kt; do
  sed 's/^package com\.suprxsidh\.batterylab\./package com.suprxsidh.onestop.battery./; s/com\.suprxsidh\.batterylab\./com.suprxsidh.onestop.battery./g' \
    "$SRC/$f" > "$DST/$f"
done
```

- [ ] **Step 2: Create `BatteryHomeViewModel.kt`** (renamed from `DashboardViewModel`)

`app/src/main/java/com/suprxsidh/onestop/battery/ui/home/BatteryHomeViewModel.kt`:
```kotlin
package com.suprxsidh.onestop.battery.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.suprxsidh.onestop.battery.data.AppDatabase
import com.suprxsidh.onestop.battery.data.HealthEstimate
import com.suprxsidh.onestop.battery.data.Reading
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class BatteryHomeViewModel(db: AppDatabase) : ViewModel() {

    val latestReading: StateFlow<Reading?> = db.readingDao().latest()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val latestHealth: StateFlow<HealthEstimate?> = db.healthEstimateDao().latest()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

class BatteryHomeViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return BatteryHomeViewModel(db) as T
    }
}
```

- [ ] **Step 3: Create `BatteryHomeScreen.kt`** (renamed from `DashboardScreen`)

`app/src/main/java/com/suprxsidh/onestop/battery/ui/home/BatteryHomeScreen.kt`:
```kotlin
package com.suprxsidh.onestop.battery.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BatteryHomeScreen(viewModel: BatteryHomeViewModel) {
    val reading by viewModel.latestReading.collectAsState()
    val health by viewModel.latestHealth.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Battery: ${reading?.pct ?: "--"}%")
        Text("Temp: ${reading?.tempC ?: "--"}°C")
        Text("Voltage: ${reading?.voltageMv ?: "--"} mV")
        Text("Power: ${reading?.watts ?: "--"} W")
        Text("Health: ${health?.healthPct?.let { "%.1f".format(it) } ?: "--"}%")
    }
}
```

- [ ] **Step 4: Create `BatteryNavHost.kt`** (renamed from `BatteryLabNavHost`, `"dashboard"` route renamed `"battery_home"` to avoid ambiguity with the top-level dashboard route added in Task 6)

`app/src/main/java/com/suprxsidh/onestop/battery/ui/nav/BatteryNavHost.kt`:
```kotlin
package com.suprxsidh.onestop.battery.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.suprxsidh.onestop.battery.data.AppDatabase
import com.suprxsidh.onestop.battery.ui.charging.ChargingScreen
import com.suprxsidh.onestop.battery.ui.home.BatteryHomeScreen
import com.suprxsidh.onestop.battery.ui.home.BatteryHomeViewModel
import com.suprxsidh.onestop.battery.ui.home.BatteryHomeViewModelFactory
import com.suprxsidh.onestop.battery.ui.discharge.DischargeScreen
import com.suprxsidh.onestop.battery.ui.history.HistoryScreen
import com.suprxsidh.onestop.battery.ui.insights.InsightsScreen

private data class Destination(val route: String, val label: String, val icon: ImageVector)

private val destinations = listOf(
    Destination("battery_home", "Now", Icons.Filled.BatteryFull),
    Destination("charging", "Charging", Icons.Filled.Bolt),
    Destination("discharge", "Discharge", Icons.Filled.TrendingDown),
    Destination("history", "History", Icons.Filled.History),
    Destination("insights", "Insights", Icons.Filled.Lightbulb)
)

@Composable
fun BatteryNavHost(db: AppDatabase) {
    val navController: NavHostController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = backStackEntry?.destination?.route == destination.route,
                        onClick = { navController.navigate(destination.route) },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "battery_home",
            modifier = Modifier.padding(padding)
        ) {
            composable("battery_home") {
                val viewModel: BatteryHomeViewModel = viewModel(factory = BatteryHomeViewModelFactory(db))
                BatteryHomeScreen(viewModel)
            }
            composable("charging") { ChargingScreen(db) }
            composable("discharge") { DischargeScreen(db) }
            composable("history") { HistoryScreen(db) }
            composable("insights") { InsightsScreen(db) }
        }
    }
}
```

- [ ] **Step 5: Verify it builds**

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`. (`BatteryNavHost` isn't wired into `MainActivity` yet — that happens in Task 6 — so this step only proves the module compiles standalone.)

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "Port battery UI screens and internal nav (BatteryNavHost)"
```

---

### Task 5: Battery tile state mapper (new, TDD)

**Files:**
- Modify: `app/src/main/java/com/suprxsidh/onestop/battery/data/ReadingDao.kt:19-21` — add a bounded "recent readings" query (the existing `all()` query has no limit and isn't safe to collect forever on a live dashboard tile).
- Create: `app/src/main/java/com/suprxsidh/onestop/battery/ui/tile/BatteryTileState.kt`
- Create: `app/src/main/java/com/suprxsidh/onestop/battery/ui/tile/BatteryTileMapper.kt`
- Test: `app/src/test/java/com/suprxsidh/onestop/battery/ui/tile/BatteryTileMapperTest.kt`

**Interfaces:**
- Consumes: `com.suprxsidh.onestop.battery.data.Reading` (Task 2), `com.suprxsidh.onestop.battery.calc.DischargeCalculator.drainRate(Reading, Reading): DrainRate` (Task 2), `com.suprxsidh.onestop.battery.calc.TimeEstimator.minutesToEmpty(Int, Double): Long?` (Task 2).
- Produces: `data class BatteryTileState(percent: Int?, isCharging: Boolean, minutesRemaining: Long?, peakWatts: Float?, sparklinePercents: List<Int>)` and `object BatteryTileMapper { fun toBatteryTileState(recentReadings: List<Reading>): BatteryTileState }` — consumed by Task 6's `OneStopDashboardViewModel`.

- [ ] **Step 1: Add the bounded query to `ReadingDao`**

In `app/src/main/java/com/suprxsidh/onestop/battery/data/ReadingDao.kt`, add after the existing `all()` method:
```kotlin
    @Query("SELECT * FROM readings ORDER BY ts DESC LIMIT :limit")
    fun recent(limit: Int): kotlinx.coroutines.flow.Flow<List<Reading>>
```

- [ ] **Step 2: Write the failing test**

`app/src/test/java/com/suprxsidh/onestop/battery/ui/tile/BatteryTileMapperTest.kt`:
```kotlin
package com.suprxsidh.onestop.battery.ui.tile

import android.os.BatteryManager
import com.suprxsidh.onestop.battery.data.Reading
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BatteryTileMapperTest {

    // Fixed epoch base so "minutes ago" maps to an *earlier* (smaller) timestamp,
    // not a later one -- ts must increase forward in time for drainRate()/sortedBy{ts} to behave.
    private val now = 1_000_000_000L

    private fun reading(
        tsMinutesAgo: Long,
        pct: Int,
        watts: Float,
        status: Int = BatteryManager.BATTERY_STATUS_DISCHARGING
    ) = Reading(
        ts = now - tsMinutesAgo * 60_000L,
        pct = pct,
        tempC = 30f,
        voltageMv = 3800,
        currentUa = -500_000,
        watts = watts,
        status = status,
        plugType = 0,
        screenOn = true
    )

    @Test
    fun `empty readings produces empty state`() {
        val state = BatteryTileMapper.toBatteryTileState(emptyList())
        assertNull(state.percent)
        assertTrue(!state.isCharging)
        assertNull(state.minutesRemaining)
        assertNull(state.peakWatts)
        assertTrue(state.sparklinePercents.isEmpty())
    }

    @Test
    fun `single reading has percent but no time remaining`() {
        val state = BatteryTileMapper.toBatteryTileState(listOf(reading(tsMinutesAgo = 0, pct = 62, watts = -4.5f)))
        assertEquals(62, state.percent)
        assertNull(state.minutesRemaining)
        assertEquals(4.5f, state.peakWatts)
        assertEquals(listOf(62), state.sparklinePercents)
    }

    @Test
    fun `discharging status is reflected`() {
        val state = BatteryTileMapper.toBatteryTileState(
            listOf(reading(tsMinutesAgo = 0, pct = 50, watts = -3f, status = BatteryManager.BATTERY_STATUS_DISCHARGING))
        )
        assertTrue(!state.isCharging)
    }

    @Test
    fun `charging status is reflected`() {
        val state = BatteryTileMapper.toBatteryTileState(
            listOf(reading(tsMinutesAgo = 0, pct = 50, watts = 5f, status = BatteryManager.BATTERY_STATUS_CHARGING))
        )
        assertTrue(state.isCharging)
    }

    @Test
    fun `two readings compute minutes remaining from drain rate`() {
        // 90 minutes apart, dropped from 80% to 65% => 15% / 1.5h = 10%/h => 65% left / (10%/h) = 6.5h = 390min
        val older = reading(tsMinutesAgo = 90, pct = 80, watts = -3f)
        val newer = reading(tsMinutesAgo = 0, pct = 65, watts = -3.5f)
        val state = BatteryTileMapper.toBatteryTileState(listOf(older, newer))
        assertEquals(390L, state.minutesRemaining)
    }

    @Test
    fun `peak watts is the max magnitude across readings`() {
        val readings = listOf(
            reading(tsMinutesAgo = 30, pct = 70, watts = -2f),
            reading(tsMinutesAgo = 15, pct = 66, watts = -9.2f),
            reading(tsMinutesAgo = 0, pct = 62, watts = -4f)
        )
        val state = BatteryTileMapper.toBatteryTileState(readings)
        assertEquals(9.2f, state.peakWatts)
    }

    @Test
    fun `sparkline percents follow chronological order regardless of input order`() {
        val readings = listOf(
            reading(tsMinutesAgo = 0, pct = 62, watts = -4f),
            reading(tsMinutesAgo = 30, pct = 70, watts = -2f),
            reading(tsMinutesAgo = 15, pct = 66, watts = -3f)
        )
        val state = BatteryTileMapper.toBatteryTileState(readings)
        assertEquals(listOf(70, 66, 62), state.sparklinePercents)
    }
}
```

- [ ] **Step 3: Run it to verify it fails**

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
./gradlew test --tests "com.suprxsidh.onestop.battery.ui.tile.BatteryTileMapperTest"
```

Expected: `FAIL` — compile error, `BatteryTileState`/`BatteryTileMapper` unresolved.

- [ ] **Step 4: Write `BatteryTileState.kt`**

```kotlin
package com.suprxsidh.onestop.battery.ui.tile

data class BatteryTileState(
    val percent: Int?,
    val isCharging: Boolean,
    val minutesRemaining: Long?,
    val peakWatts: Float?,
    val sparklinePercents: List<Int>
)
```

- [ ] **Step 5: Write `BatteryTileMapper.kt`**

```kotlin
package com.suprxsidh.onestop.battery.ui.tile

import android.os.BatteryManager
import com.suprxsidh.onestop.battery.calc.DischargeCalculator
import com.suprxsidh.onestop.battery.calc.TimeEstimator
import com.suprxsidh.onestop.battery.data.Reading
import kotlin.math.abs

object BatteryTileMapper {
    fun toBatteryTileState(recentReadings: List<Reading>): BatteryTileState {
        if (recentReadings.isEmpty()) {
            return BatteryTileState(
                percent = null,
                isCharging = false,
                minutesRemaining = null,
                peakWatts = null,
                sparklinePercents = emptyList()
            )
        }

        val chronological = recentReadings.sortedBy { it.ts }
        val latest = chronological.last()

        val minutesRemaining = if (chronological.size >= 2) {
            val oldest = chronological.first()
            runCatching {
                val rate = DischargeCalculator.drainRate(oldest, latest)
                if (rate.pctPerHour <= 0) null else TimeEstimator.minutesToEmpty(latest.pct, rate.pctPerHour / 60.0)
            }.getOrNull()
        } else null

        return BatteryTileState(
            percent = latest.pct,
            isCharging = latest.status == BatteryManager.BATTERY_STATUS_CHARGING,
            minutesRemaining = minutesRemaining,
            peakWatts = chronological.maxOf { abs(it.watts) },
            sparklinePercents = chronological.map { it.pct }
        )
    }
}
```

- [ ] **Step 6: Run tests to verify they pass**

```bash
./gradlew test --tests "com.suprxsidh.onestop.battery.ui.tile.BatteryTileMapperTest"
```

Expected: all 7 tests `PASS`.

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "Add BatteryTileMapper for dashboard hero-tile projection"
```

---

### Task 6: OneStop dashboard, top-level nav, MainActivity wiring, build verification

**Files:**
- Create: `app/src/main/java/com/suprxsidh/onestop/ui/theme/OneStopTheme.kt`
- Create: `app/src/main/java/com/suprxsidh/onestop/ui/dashboard/OneStopDashboardViewModel.kt`
- Create: `app/src/main/java/com/suprxsidh/onestop/ui/dashboard/OneStopDashboardScreen.kt`
- Create: `app/src/main/java/com/suprxsidh/onestop/ui/nav/OneStopNavHost.kt`
- Modify: `app/src/main/java/com/suprxsidh/onestop/MainActivity.kt`

**Interfaces:**
- Consumes: `com.suprxsidh.onestop.battery.data.AppDatabase` (Task 2), `com.suprxsidh.onestop.battery.ui.tile.BatteryTileMapper`/`BatteryTileState` (Task 5), `com.suprxsidh.onestop.battery.ui.nav.BatteryNavHost` (Task 4).
- Produces: the app's actual entry point — `MainActivity` now shows the real dashboard instead of Task 1's placeholder text.

- [ ] **Step 1: Write `OneStopTheme.kt`** — carries the approved mockup's palette (copper accent, teal "live" semantic color) into real Material3 color schemes, light and dark

```kotlin
package com.suprxsidh.onestop.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFFB0591C),
    background = Color(0xFFEEF0F4),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1A1920),
    onSurface = Color(0xFF1A1920)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE2853B),
    background = Color(0xFF17161B),
    surface = Color(0xFF201F26),
    onBackground = Color(0xFFF1EFEA),
    onSurface = Color(0xFFF1EFEA)
)

@Composable
fun OneStopTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
```

- [ ] **Step 2: Write `OneStopDashboardViewModel.kt`**

```kotlin
package com.suprxsidh.onestop.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.suprxsidh.onestop.battery.data.AppDatabase
import com.suprxsidh.onestop.battery.ui.tile.BatteryTileMapper
import com.suprxsidh.onestop.battery.ui.tile.BatteryTileState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val SPARKLINE_SAMPLE_COUNT = 40

class OneStopDashboardViewModel(db: AppDatabase) : ViewModel() {

    val batteryTile: StateFlow<BatteryTileState> = db.readingDao()
        .recent(SPARKLINE_SAMPLE_COUNT)
        .map { BatteryTileMapper.toBatteryTileState(it) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            BatteryTileMapper.toBatteryTileState(emptyList())
        )
}

class OneStopDashboardViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return OneStopDashboardViewModel(db) as T
    }
}
```

- [ ] **Step 3: Write `OneStopDashboardScreen.kt`**

```kotlin
package com.suprxsidh.onestop.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.suprxsidh.onestop.battery.ui.tile.BatteryTileState

@Composable
fun OneStopDashboardScreen(viewModel: OneStopDashboardViewModel, onOpenBattery: () -> Unit) {
    val batteryTile by viewModel.batteryTile.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BatteryHeroTile(state = batteryTile, onClick = onOpenBattery)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PlaceholderTile(label = "Gestures", modifier = Modifier.weight(1f))
            PlaceholderTile(label = "System", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun BatteryHeroTile(state: BatteryTileState, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp, 20.dp, 28.dp, 20.dp),
        colors = CardDefaults.cardColors(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Battery")
            Text(state.percent?.let { "$it%" } ?: "--")
            Text(if (state.isCharging) "Charging" else "Discharging")
            Text(state.minutesRemaining?.let { "${it / 60}h ${it % 60}m left" } ?: "-- left")
            Text(state.peakWatts?.let { "Peak %.1f W today".format(it) } ?: "")
        }
    }
}

@Composable
private fun PlaceholderTile(label: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(20.dp, 28.dp, 20.dp, 28.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label)
            Text("Coming soon")
        }
    }
}
```

- [ ] **Step 4: Write `OneStopNavHost.kt`**

```kotlin
package com.suprxsidh.onestop.ui.nav

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.suprxsidh.onestop.battery.data.AppDatabase
import com.suprxsidh.onestop.battery.ui.nav.BatteryNavHost
import com.suprxsidh.onestop.ui.dashboard.OneStopDashboardScreen
import com.suprxsidh.onestop.ui.dashboard.OneStopDashboardViewModel
import com.suprxsidh.onestop.ui.dashboard.OneStopDashboardViewModelFactory

@Composable
fun OneStopNavHost(db: AppDatabase) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            val viewModel: OneStopDashboardViewModel = viewModel(factory = OneStopDashboardViewModelFactory(db))
            OneStopDashboardScreen(
                viewModel = viewModel,
                onOpenBattery = { navController.navigate("battery") }
            )
        }
        composable("battery") {
            BatteryNavHost(db)
        }
    }
}
```

- [ ] **Step 5: Replace `MainActivity.kt`**

```kotlin
package com.suprxsidh.onestop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.suprxsidh.onestop.battery.data.AppDatabase
import com.suprxsidh.onestop.ui.nav.OneStopNavHost
import com.suprxsidh.onestop.ui.theme.OneStopTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getInstance(applicationContext)
        setContent {
            OneStopTheme {
                OneStopNavHost(db)
            }
        }
    }
}
```

- [ ] **Step 6: Full test suite + build**

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
cd ~/claudecode-projects/onestop
./gradlew test
./gradlew assembleDebug
```

Expected: all tests `PASS`, `BUILD SUCCESSFUL`, APK at `app/build/outputs/apk/debug/app-debug.apk`.

- [ ] **Step 7: Install if a device is available**

```bash
adb devices
```

If a device/emulator is listed:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

If no device is listed: skip — leave this for the user to install and visually verify against the approved mockup when they're back. Do not claim the on-screen result looks correct without actually seeing it render.

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "Add OneStop dashboard, top-level nav, wire up MainActivity"
```

---

## Known gap: visual polish not yet at mockup fidelity

Task 6's dashboard is functionally correct (real data wiring, asymmetric tile corner shapes, theme colors matching the approved palette) but does **not** yet pixel-match the approved mockup (https://claude.ai/code/artifact/bd05f1ef-e3f9-4eb3-8354-154ddaeb0cfb) — no charge ring, no sparkline rendering, no custom icons; tiles are plain `Text` rows on a `Card`. That's a deliberate sequencing choice (data/architecture first, visual polish second), not a silent scope cut, but it means what installs after this plan will look plainer than the mockup. Flag this to the user explicitly when reporting completion; a follow-up visual-polish task (Canvas-drawn ring + sparkline, custom vector icons) should be spec'd separately rather than assumed done.

## After all tasks

Update `~/claudecode-projects/onestop/CLAUDE.md`'s Status section to mark sub-project 1 as implemented, and note in `~/claudecode-projects/CLAUDE.md`'s Projects list. Do not create the GitHub repo or push — that's a separate, explicitly-confirmed step (spec §8).
