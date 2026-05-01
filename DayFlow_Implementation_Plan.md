# DayFlow — Android App Implementation Plan
> **Stack:** Kotlin · Jetpack Compose · Room DB · Material 3 · All Free Libraries
> **Target:** Android 8.0+ (API 26+)
> **Build Tool:** Android Studio (free) + Gradle

---

## Table of Contents
1. [Final Feature Spec](#1-final-feature-spec)
2. [Tech Stack & Libraries](#2-tech-stack--libraries)
3. [Project Structure](#3-project-structure)
4. [Database Schema](#4-database-schema)
5. [Phase 1 — Foundation & Routine Builder](#5-phase-1--foundation--routine-builder)
6. [Phase 2 — Home Screen, Timeline & Notifications](#6-phase-2--home-screen-timeline--notifications)
7. [Phase 3 — End-of-Day, Dashboard & Streaks](#7-phase-3--end-of-day-dashboard--streaks)
8. [Phase 4 — Widgets, Polish & Edge Cases](#8-phase-4--widgets-polish--edge-cases)
9. [AI Prompt Instructions Per Phase](#9-ai-prompt-instructions-per-phase)
10. [Free vs Paid Flags](#10-free-vs-paid-flags)

---

## 1. Final Feature Spec

### 1.1 Routine Builder
- Add tasks with: name, category, start time, duration, frequency (daily / weekdays / weekends / custom days), repeat count per day, priority flag (important = streak tracking)
- Smart conflict detection
- Suggested common tasks on first launch
- Edit / delete / reorder tasks

### 1.2 Timeline View
- Vertical day view (Google Calendar style)
- Vibrant solid color blocks per category
- Swipe left/right for days
- Tap block → bottom sheet with detail + edit
- Free time gaps shown as dashed blocks

### 1.3 Home Screen — Now Focus
- Current task card: bold vibrant color, category color, circular countdown ring
- Pending/skipped tasks: muted chip strip above current card
- Up Next preview: 2 upcoming tasks shown subtly below
- Actions: ✓ Done | ⏭ Skip | +10 min Snooze (overlaps, does not push tasks)
- Haptic feedback on Done
- Confetti on high-completion days

### 1.4 Notifications
- **Sticky ongoing notification** (Foreground Service): live countdown, inline Done/Skip/+10 actions, non-dismissible
- **Lock screen card**: circular timer ring, task name, remaining time
- **Pre-alert**: configurable heads-up X minutes before task starts (default 5 min)
- Quiet hours: no notifications during sleep block

### 1.5 End-of-Day Wrap-Up
- Triggered at sleep time or manually
- Animated score ring (% done)
- Confetti if ≥ 80%
- Done / Skipped / Pending breakdown
- Reschedule skipped tasks to tomorrow
- Streak flame update animation

### 1.6 Dashboard & Streaks
- GitHub-style heatmap calendar per important habit (category color intensity)
- Grace days: configurable (1–3 misses/week), shown in amber — streak not broken
- Swipeable stat cards: Current Streak, Best Streak, Weekly bar chart, Category donut, Consistency Score (0–100, graded S/A/B/C)

### 1.7 Widgets (Phase 4)
- Small widget: current task + countdown
- Medium widget: current + next 2 + pending count

---

## 2. Tech Stack & Libraries

### Core (All Free)
| Library | Purpose | Version |
|---|---|---|
| Kotlin | Primary language | Latest stable |
| Jetpack Compose | UI framework | BOM latest |
| Material 3 | Design system | via Compose BOM |
| Room | Local database | 2.6.x |
| DataStore Preferences | Settings/prefs storage | 1.0.x |
| Hilt | Dependency injection | 2.x |
| Kotlin Coroutines | Async/background | 1.7.x |
| Kotlin Flow | Reactive data streams | bundled with coroutines |
| Navigation Compose | Screen navigation | 2.7.x |
| Lifecycle ViewModel | State management | 2.7.x |
| WorkManager | Scheduled background tasks | 2.9.x |
| Glance API | Home screen widgets | 1.0.x |
| SplashScreen API | Animated splash | 1.0.x |

### UI / Animation (All Free)
| Library | Purpose |
|---|---|
| Lottie Compose | JSON-based animations (confetti, flame) |
| `androidx.compose.animation` | Page transitions, spring animations |
| Accompanist (Pager) | Swipeable pages (timeline days, stat cards) |
| Konfetti (github.com/DanielMartinus/Konfetti) | Confetti burst on wrap-up |
| MPAndroidChart (via Compose wrapper) | Bar chart, donut chart in dashboard |
| `Canvas` (Compose built-in) | Circular progress ring, heatmap grid |

### System (All Free / Built-in)
| Component | Purpose |
|---|---|
| Android Foreground Service | Sticky notification with live timer |
| NotificationManager | Notification channels, lock screen card |
| AlarmManager / WorkManager | Pre-alert scheduling |
| HapticFeedbackConstants | Vibration on task done |
| BroadcastReceiver | Notification action handlers (Done/Skip/Snooze) |

---

## 3. Project Structure

```
com.dayflow/
├── data/
│   ├── db/
│   │   ├── AppDatabase.kt
│   │   ├── dao/
│   │   │   ├── TaskDao.kt
│   │   │   ├── TaskLogDao.kt
│   │   │   └── StreakDao.kt
│   │   └── entity/
│   │       ├── TaskEntity.kt
│   │       ├── TaskLogEntity.kt
│   │       └── StreakEntity.kt
│   ├── repository/
│   │   ├── TaskRepository.kt
│   │   ├── TaskLogRepository.kt
│   │   └── StreakRepository.kt
│   └── datastore/
│       └── UserPreferences.kt
├── domain/
│   ├── model/
│   │   ├── Task.kt
│   │   ├── TaskLog.kt
│   │   └── Streak.kt
│   └── usecase/
│       ├── GetTodayTasksUseCase.kt
│       ├── GetCurrentTaskUseCase.kt
│       ├── MarkTaskDoneUseCase.kt
│       ├── SkipTaskUseCase.kt
│       ├── RescheduleTaskUseCase.kt
│       ├── ComputeStreakUseCase.kt
│       └── EndOfDaySummaryUseCase.kt
├── ui/
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Typography.kt
│   │   ├── Theme.kt
│   │   └── CategoryColors.kt
│   ├── components/
│   │   ├── CircularCountdownRing.kt
│   │   ├── TaskCard.kt
│   │   ├── PendingChipStrip.kt
│   │   ├── SolidColorBlock.kt
│   │   ├── HeatmapCalendar.kt
│   │   ├── StatCard.kt
│   │   ├── BottomSheetTaskDetail.kt
│   │   └── ConfettiOverlay.kt
│   ├── screens/
│   │   ├── splash/
│   │   │   └── SplashScreen.kt
│   │   ├── onboarding/
│   │   │   ├── OnboardingScreen.kt
│   │   │   └── OnboardingViewModel.kt
│   │   ├── builder/
│   │   │   ├── RoutineBuilderScreen.kt
│   │   │   └── RoutineBuilderViewModel.kt
│   │   ├── home/
│   │   │   ├── HomeScreen.kt
│   │   │   └── HomeViewModel.kt
│   │   ├── timeline/
│   │   │   ├── TimelineScreen.kt
│   │   │   └── TimelineViewModel.kt
│   │   ├── wrapup/
│   │   │   ├── WrapUpScreen.kt
│   │   │   └── WrapUpViewModel.kt
│   │   └── dashboard/
│   │       ├── DashboardScreen.kt
│   │       └── DashboardViewModel.kt
│   └── navigation/
│       └── NavGraph.kt
├── service/
│   ├── TaskTimerService.kt          ← Foreground service (sticky notification)
│   └── NotificationActionReceiver.kt ← Handles Done/Skip/Snooze from notification
├── worker/
│   ├── PreAlertWorker.kt            ← WorkManager: fires heads-up before task
│   ├── EndOfDayWorker.kt            ← WorkManager: triggers wrap-up at sleep time
│   └── StreakUpdateWorker.kt        ← WorkManager: updates streak at midnight
├── widget/
│   ├── DayFlowWidget.kt             ← Glance API widget
│   └── WidgetReceiver.kt
├── di/
│   ├── DatabaseModule.kt
│   ├── RepositoryModule.kt
│   └── UseCaseModule.kt
└── MainActivity.kt
```

---

## 4. Database Schema

### Table: `tasks`
```sql
CREATE TABLE tasks (
  id              INTEGER PRIMARY KEY AUTOINCREMENT,
  name            TEXT NOT NULL,
  category        TEXT NOT NULL,          -- HEALTH, WORK, PERSONAL, FAMILY, SPIRITUAL, OTHER
  color_hex       TEXT NOT NULL,          -- Category vibrant solid color
  start_time      TEXT NOT NULL,          -- "HH:mm" 24hr format
  duration_min    INTEGER NOT NULL,       -- Duration in minutes
  is_flexible     INTEGER NOT NULL,       -- 0=fixed, 1=flexible window
  flex_window_end TEXT,                   -- "HH:mm" latest start time if flexible
  repeat_days     TEXT NOT NULL,          -- JSON array: ["MON","TUE",...] or "DAILY","WEEKDAYS","WEEKENDS"
  repeat_count    INTEGER DEFAULT 1,      -- How many times per day (e.g., water intake = 8)
  is_important    INTEGER DEFAULT 0,      -- 1 = tracked in streak
  reminder_min    INTEGER DEFAULT 5,      -- Pre-alert minutes before task
  is_active       INTEGER DEFAULT 1,      -- Soft delete
  created_at      INTEGER NOT NULL        -- Unix timestamp
);
```

### Table: `task_logs`
```sql
CREATE TABLE task_logs (
  id          INTEGER PRIMARY KEY AUTOINCREMENT,
  task_id     INTEGER NOT NULL,
  log_date    TEXT NOT NULL,             -- "YYYY-MM-DD"
  status      TEXT NOT NULL,             -- DONE | SKIPPED | PENDING | SNOOZED
  done_at     INTEGER,                   -- Unix timestamp when marked done
  occurrence  INTEGER DEFAULT 1,         -- Which repetition (for repeat_count tasks)
  FOREIGN KEY (task_id) REFERENCES tasks(id)
);
```

### Table: `streaks`
```sql
CREATE TABLE streaks (
  id              INTEGER PRIMARY KEY AUTOINCREMENT,
  task_id         INTEGER NOT NULL,
  current_streak  INTEGER DEFAULT 0,
  best_streak     INTEGER DEFAULT 0,
  last_done_date  TEXT,                  -- "YYYY-MM-DD"
  grace_used      INTEGER DEFAULT 0,     -- Grace days used this week
  FOREIGN KEY (task_id) REFERENCES tasks(id)
);
```

### DataStore Preferences (UserPreferences.kt)
```
sleep_time          String   "HH:mm"       default "22:30"
wake_time           String   "HH:mm"       default "06:00"
grace_days_per_week Int                    default 1  (configurable 1–3)
pre_alert_minutes   Int                    default 5  (configurable 2/5/10)
theme_mode          String   LIGHT/DARK/SYSTEM
onboarding_done     Boolean               default false
```

---

## 5. Phase 1 — Foundation & Routine Builder

### Goals
- Android project setup with all dependencies
- Room database working
- Full routine builder UI

### Step 1.1 — Project Setup
- Create new Android project in Android Studio
  - Package: `com.dayflow`
  - Min SDK: 26 (Android 8.0)
  - Language: Kotlin
  - Build config: Compose enabled
- Add all dependencies to `build.gradle.kts` (app level)
- Set up Hilt in `Application` class: `DayFlowApp.kt`
- Create `di/` modules: DatabaseModule, RepositoryModule

### Step 1.2 — Database Layer
- Create all 3 Room entities: `TaskEntity`, `TaskLogEntity`, `StreakEntity`
- Create all 3 DAOs with these methods:
  - `TaskDao`: insertTask, updateTask, deleteTask, getAllActiveTasks, getTaskById, getTasksByDay
  - `TaskLogDao`: insertLog, updateLog, getLogsForDate, getLogsForTask, getLogsInRange
  - `StreakDao`: insertOrUpdateStreak, getStreakForTask, getAllStreaks
- Create `AppDatabase.kt` with all entities + migrations placeholder
- Create Repository classes wrapping DAOs with Flow returns

### Step 1.3 — Theme & Design System
- `Color.kt`: Define category vibrant solid colors for all 6 categories
  ```
  HEALTH   → #FF4757 (Radical Red)
  WORK     → #2E86DE (Bleu De France)
  PERSONAL → #833471 (Hollyhock)
  FAMILY   → #F0932B (Orange Hibiscus)
  SPIRITUAL→ #130F40 (Deep Cove)
  OTHER    → #20BF6B (Algal Fuel)
  ```
- `Typography.kt`: Use Google Fonts — `Nunito` for body, `Poppins` for display (free via fonts.google.com, add to assets)
- `Theme.kt`: Material 3 dynamic color + custom overrides
- `CategoryColors.kt`: Helper to get solid color from category enum

### Step 1.4 — Routine Builder Screen
**UI Components needed:**
- `RoutineBuilderScreen.kt`: Full-screen list of tasks + FAB to add
- `AddTaskBottomSheet.kt`: Bottom sheet with:
  - Task name input (large text field)
  - Category selector (horizontal scrolling colored chips)
  - Time picker (Material 3 TimePicker)
  - Duration slider (15 min – 4 hours)
  - Repeat day selector (MON–SUN toggle chips)
  - Times per day stepper (for repeat_count)
  - Important toggle (star icon, feeds streak)
  - Pre-alert minutes selector
- `TaskListItem.kt`: Swipeable list item (swipe left = delete, swipe right = edit)
- Conflict detection: check overlap in ViewModel before saving

**ViewModel (`RoutineBuilderViewModel.kt`):**
- `uiState`: list of tasks for today
- `addTask(task)`, `updateTask(task)`, `deleteTask(id)`
- `checkConflict(startTime, duration)` → returns conflicting task or null

### Step 1.5 — Onboarding
- 3-page onboarding with Pager (swipeable)
  - Page 1: Welcome + app value prop
  - Page 2: Suggested tasks list (checkboxes to auto-add)
  - Page 3: Set wake time + sleep time + grace days preference
- On complete: set `onboarding_done = true` in DataStore, navigate to builder

---

## 6. Phase 2 — Home Screen, Timeline & Notifications

### Goals
- Now Focus home screen
- Timeline view
- Full notification system (foreground service + lock screen + pre-alert)

### Step 2.1 — Use Cases
Implement these domain use cases:
- `GetTodayTasksUseCase`: query tasks for today's weekday, return sorted by start_time
- `GetCurrentTaskUseCase`: from today's tasks + current time, return active task
- `MarkTaskDoneUseCase`: insert DONE log, trigger streak update
- `SkipTaskUseCase`: insert SKIPPED log
- `RescheduleTaskUseCase`: create a one-off task_log override for tomorrow

### Step 2.2 — Home Screen (Now Focus)
**Components:**
- `CircularCountdownRing.kt`:
  - Canvas-drawn ring
  - Sweeps from full to empty as time passes
  - Color matches task category solid color
  - Center: large remaining time text (MM:SS or HH:MM)
  - Animated with `animateFloatAsState` + spring spec
- `TaskCard.kt`:
  - Full-width vibrant solid color card
  - Task name (Poppins Bold, large)
  - Category pill badge
  - CircularCountdownRing embedded
  - Row of action buttons: ✓ Done (filled) | ⏭ Skip (outline) | +10 (text)
  - Haptic feedback on Done tap
- `PendingChipStrip.kt`:
  - Horizontal scrolling row of muted gray chips
  - Each chip: task name + ⚠ icon
  - Tapping expands to show skipped task detail in bottom sheet
- `UpNextRow.kt`: Two subtle cards below main card showing next 2 tasks

**HomeViewModel.kt:**
- Observes current time via `ticker` Flow (updates every second)
- Exposes: `currentTask`, `pendingTasks`, `upNextTasks`, `todayProgress`
- Handles: `onMarkDone()`, `onSkip()`, `onSnooze()` — snooze adds 10 min overlap (does NOT push other tasks)

### Step 2.3 — Timeline Screen
**Components:**
- `TimelineScreen.kt`: HorizontalPager for days (today ± 7 days)
- `DayTimelineView.kt`:
  - LazyColumn with time axis on left (00:00–23:59)
  - Each task = `SolidTimeBlock.kt` positioned by start_time + height by duration
  - Free time = dashed `FreeTimeBlock.kt`
  - Current time indicator: red horizontal line
- `BottomSheetTaskDetail.kt`: shows on block tap, has Edit + Done/Skip buttons
- Long press on block → drag to reschedule (Phase 4 enhancement, stub for now)

### Step 2.4 — Foreground Service (Sticky Notification)
**`TaskTimerService.kt`** (Foreground Service):
```
- Started when: a task begins (scheduled via AlarmManager)
- Stopped when: task marked done/skipped OR task time ends
- Runs a coroutine ticker every second updating remaining time
- Posts ongoing notification with:
    - Title: task name
    - Text: "XX min remaining"
    - Color: category color
    - Ongoing: true (non-dismissible)
    - Actions: [✓ Done] [⏭ Skip] [+10 min]
    - Style: BigTextStyle or MediaStyle for lock screen prominence
- Notification channel: IMPORTANCE_LOW (no sound for ongoing)
```

**`NotificationActionReceiver.kt`** (BroadcastReceiver):
- Receives Done / Skip / Snooze PendingIntents from notification
- Calls corresponding use cases
- Stops or updates the service accordingly

**Lock Screen Behavior:**
- Set notification visibility: `VISIBILITY_PUBLIC`
- Use `DecoratedCustomViewStyle` or standard notification — Android shows it on lock screen automatically with the live countdown
- ⚠️ **Note**: True custom lock screen widgets are restricted in Android 13+. The ongoing notification with live timer is the best free approach — it appears prominently on lock screen without requiring special permissions.

### Step 2.5 — Pre-Alert Scheduling
**`PreAlertWorker.kt`** (WorkManager):
- Scheduled at day start for all of today's tasks
- Fires X minutes before each task (from user preference)
- Posts a heads-up (high importance) notification: "🔔 [Task] starts in X min"
- Uses `OneTimeWorkRequest` with exact delay

**`EndOfDayWorker.kt`** (WorkManager):
- Scheduled at user's sleep time
- Triggers wrap-up screen (deeplink intent to WrapUpScreen)

**AlarmManager Note:** For exact timing on Android 12+, use `AlarmManager.setExactAndAllowWhileIdle()`. Requires permission: `SCHEDULE_EXACT_ALARM`. Add to manifest.

---

## 7. Phase 3 — End-of-Day, Dashboard & Streaks

### Goals
- Animated end-of-day wrap-up screen
- Full dashboard with stats
- Streak calendar with grace day logic

### Step 3.1 — End-of-Day Wrap-Up Screen
**`WrapUpScreen.kt`:**
- Full-screen colorful solid background with high-contrast elements
- Animated score ring (Canvas, animates from 0 to final %)
  - Color: green if ≥80%, amber if 50–79%, red if <50%
- Summary numbers animate counting up
- Cards for each skipped task with "Reschedule Tomorrow" button
- `ConfettiOverlay.kt` using Konfetti library — fires if score ≥ 80%
- Streak flame animation (Lottie JSON — free animations at lottiefiles.com)
- "See you tomorrow 🌙" dismiss button

**`WrapUpViewModel.kt`:**
- `EndOfDaySummaryUseCase`: aggregates today's logs → counts done/skipped/pending
- `onReschedule(taskId)`: calls RescheduleTaskUseCase for tomorrow
- `onDismiss()`: marks day complete in DataStore, triggers streak update

### Step 3.2 — Streak Logic
**`ComputeStreakUseCase.kt`:**
```
For each important task:
1. Get logs for past N days
2. For each day: check if DONE exists for that task
3. Grace rule: if no DONE but grace_used < grace_limit → count as grace (amber), increment grace_used
4. Grace resets every Monday
5. If neither done nor grace → streak breaks, reset current_streak = 0
6. Update StreakEntity in DB
```

**`StreakUpdateWorker.kt`** (WorkManager, runs at midnight):
- Triggers ComputeStreakUseCase for all important tasks
- Updates Room DB

### Step 3.3 — Dashboard Screen
**`DashboardScreen.kt`:**
- Top: Consistency Score card (large number, animated, grade badge)
- Swipeable `StatCard` row:
  - Card 1: 🔥 Current Streak | 🏆 Best Streak (per task selector)
  - Card 2: Weekly bar chart (MPAndroidChart via Compose interop or Canvas-drawn)
  - Card 3: Category time donut chart
- Below: Task selector for streak heatmap (filter by important tasks)
- `HeatmapCalendar.kt`: Full custom Canvas component
  - 52 columns × 7 rows (year grid like GitHub)
  - Cell color = category color at opacity based on completion %
  - Grace days = amber at 50% opacity
  - Missed = gray
  - Future = empty
  - Tap cell → bottom sheet showing that day's log

**`HeatmapCalendar.kt` — Canvas Drawing Logic:**
```
- cellSize = (availableWidth - padding) / 53
- For each week column, draw 7 cells
- Color: category.color.copy(alpha = completionRatio)
- RoundRect corners for soft look
- Row labels: Mon/Wed/Fri on left axis
- Column labels: month names on top
```

### Step 3.4 — Consistency Score Algorithm
```kotlin
// Score out of 100 for the past 7 days
val totalSlots = tasksCount * 7
val doneSlots = doneLogsCount
val graceSlots = graceLogsCount
val score = ((doneSlots + graceSlots * 0.5) / totalSlots * 100).roundToInt()

// Grade
S = 90–100, A = 75–89, B = 60–74, C = 40–59, D = below 40
```

---

## 8. Phase 4 — Widgets, Polish & Edge Cases

### Step 4.1 — Home Screen Widgets (Glance API)
**`DayFlowWidget.kt`** using `androidx.glance`:
- Small (2×1): Task name + countdown ring + Done button
- Medium (4×2): Current task + 2 upcoming + pending count chip
- Widget updates: use `GlanceAppWidgetManager` + coroutine-based updater
- Refresh: every minute via WorkManager periodic task

⚠️ **Note**: Glance API widgets cannot run live-second countdowns (system limits widget refresh). Best approach: show time remaining rounded to nearest minute, refresh every minute.

### Step 4.2 — Polish & Animations
- **App entry**: Splash screen with logo scale + fade (SplashScreen API)
- **Screen transitions**: Shared element transitions (Compose Navigation 2.7+)
- **Task card**: Spring animation on appear, scale bounce on Done tap
- **Confetti**: Konfetti library — particle burst on wrap-up ≥ 80%
- **Streak flame**: Lottie animation on streak increment (download free from lottiefiles.com search "fire" or "flame")
- **Bottom sheets**: `ModalBottomSheet` with drag handle, spring dismiss
- **Score ring**: `animateFloatAsState` with `spring(dampingRatio = Spring.DampingRatioMediumBouncy)`
- **Heatmap**: Fade-in cell animation on first load with staggered delay

### Step 4.3 — Edge Cases to Handle
| Scenario | Handling |
|---|---|
| No tasks today | Home shows "Rest Day 🎉" card |
| Task missed entirely (time passed, not acted on) | Auto-log as SKIPPED at end of task window |
| App closed during task | Service keeps running, on reopen sync state from logs |
| Midnight crossing task (e.g., 11:45 PM → 12:15 AM) | Split log across two dates |
| Repeat task (e.g., water 8x/day) | Show occurrence counter on card, track each with `occurrence` field |
| Snooze beyond task window | Allow overlap, show note "running over" |
| Grace day in streak | Show amber flame icon instead of orange |
| Device restart | BroadcastReceiver for `BOOT_COMPLETED` to reschedule all alarms |

### Step 4.4 — Permissions Required
```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />          <!-- Android 13+ -->
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />        <!-- Android 12+ -->
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />             <!-- Android 13+ -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```
- Request `POST_NOTIFICATIONS` at runtime on first launch
- Request `SCHEDULE_EXACT_ALARM` by directing user to system settings (Android 12+)

---

## 9. AI Prompt Instructions Per Phase

Use these prompts directly with your AI coding assistant. Always provide the full project structure + relevant existing files as context.

---

### Prompt Template Structure
```
Context: [paste relevant existing files]
Task: [specific task below]
Constraints:
- Kotlin + Jetpack Compose only
- No internet required (local Room DB)
- Follow existing package structure: com.dayflow
- Use Hilt for DI
- Return complete file(s), no partial code
```

---

### Phase 1 Prompts

**P1-A: Project Setup**
```
Create the base Android project configuration for DayFlow:
1. app/build.gradle.kts with all dependencies listed in the implementation plan
2. DayFlowApp.kt with @HiltAndroidApp
3. di/DatabaseModule.kt providing Room AppDatabase as singleton
4. di/RepositoryModule.kt binding repositories to interfaces
Include version catalog (libs.versions.toml) with all library versions.
```

**P1-B: Database Layer**
```
Create the complete Room database layer for DayFlow:
- TaskEntity, TaskLogEntity, StreakEntity (exact schema from implementation plan)
- TaskDao, TaskLogDao, StreakDao with all listed methods returning Flow where appropriate
- AppDatabase.kt with all entities registered
- Repository classes: TaskRepository, TaskLogRepository, StreakRepository
All repositories should expose Flow<List<T>> for reactive UI updates.
```

**P1-C: Theme & Design System**
```
Create the complete design system for DayFlow (colorful, energetic, Material 3):
- Color.kt with category vibrant solid colors as listed
- Typography.kt using Poppins (display) and Nunito (body) from Google Fonts assets
- Theme.kt with Material 3 setup, dark/light support
- CategoryColors.kt with getCategoryColor(category: TaskCategory): Color
- TaskCategory enum with: HEALTH, WORK, PERSONAL, FAMILY, SPIRITUAL, OTHER
```

**P1-D: Routine Builder**
```
Create the Routine Builder screen for DayFlow:
- RoutineBuilderScreen.kt: LazyColumn task list + FAB
- AddTaskBottomSheet.kt: full form with all fields (name, category chips, time picker, duration slider, day toggles, repeat count stepper, important star toggle)
- TaskListItem.kt: swipeable (swipe left = delete, swipe right = edit)
- RoutineBuilderViewModel.kt: addTask, updateTask, deleteTask, checkConflict
Use ModalBottomSheet, spring animations on FAB expand, haptic on delete.
```

---

### Phase 2 Prompts

**P2-A: Use Cases**
```
Create all domain use cases for DayFlow:
- GetTodayTasksUseCase: returns tasks scheduled for current weekday, sorted by start_time
- GetCurrentTaskUseCase: given current time + today's tasks, returns the active task
- MarkTaskDoneUseCase: inserts DONE log, triggers streak recalculation
- SkipTaskUseCase: inserts SKIPPED log
- RescheduleTaskUseCase: creates a tomorrow override log entry
Each use case should be injectable via Hilt and use suspend functions or Flow.
```

**P2-B: Home Screen**
```
Create the Now Focus home screen for DayFlow:
- CircularCountdownRing.kt: Canvas-drawn animated ring, category solid color, center time text
- TaskCard.kt: vibrant color card with task name, category pill, countdown ring, Done/Skip/Snooze buttons, haptic on Done
- PendingChipStrip.kt: muted horizontal chip row for skipped/pending tasks
- HomeScreen.kt: assembles all components, shows "Rest Day" if no tasks
- HomeViewModel.kt: ticker Flow updating every second, exposes currentTask/pendingTasks/upNextTasks
```

**P2-C: Timeline Screen**
```
Create the Timeline screen for DayFlow:
- TimelineScreen.kt with HorizontalPager for 15 days (today ± 7)
- DayTimelineView.kt: scrollable vertical timeline with time axis, vibrant solid task blocks positioned by time
- SolidTimeBlock.kt: height proportional to duration, tap opens bottom sheet
- FreeTimeBlock.kt: dashed outline block for gaps
- Current time red line indicator
- BottomSheetTaskDetail.kt: task details + done/skip actions + edit navigation
```

**P2-D: Foreground Service & Notifications**
```
Create the notification system for DayFlow:
- TaskTimerService.kt: Foreground service with coroutine ticker, posts ongoing notification with live countdown, inline Done/Skip/+10 actions
- NotificationActionReceiver.kt: BroadcastReceiver handling all notification actions
- NotificationHelper.kt: creates notification channels, builds notification with correct styling and VISIBILITY_PUBLIC for lock screen
- PreAlertWorker.kt: WorkManager worker that fires heads-up notification X min before task
- EndOfDayWorker.kt: WorkManager worker that fires at sleep time, opens WrapUpScreen via deeplink
- AlarmScheduler.kt: schedules exact alarms for task start times using AlarmManager
Also add BOOT_COMPLETED receiver to reschedule alarms after device restart.
```

---

### Phase 3 Prompts

**P3-A: End-of-Day Wrap-Up**
```
Create the End-of-Day Wrap-Up screen for DayFlow:
- WrapUpScreen.kt: full-screen bold colorful background, animated score ring (Canvas), done/skipped/pending counts with count-up animation, skipped task cards with reschedule button, Konfetti confetti if ≥80%, "See you tomorrow" dismiss
- WrapUpViewModel.kt: aggregates today's logs, handles reschedule, triggers streak update
- ConfettiOverlay.kt: Konfetti library integration, fires programmatically
Use Lottie for streak flame animation (provide placeholder Lottie JSON path).
```

**P3-B: Streak Logic**
```
Create the streak computation system for DayFlow:
- ComputeStreakUseCase.kt: for each important task, scan past logs, apply grace day rule (configurable 1-3/week, resets Monday), update StreakEntity
- StreakUpdateWorker.kt: midnight WorkManager worker calling ComputeStreakUseCase
- Grace day rule: if no DONE log for a day but grace_used < grace_limit, mark amber (grace), increment counter; if neither, reset streak
```

**P3-C: Dashboard & Heatmap**
```
Create the Dashboard screen for DayFlow:
- DashboardScreen.kt: Consistency Score card, swipeable StatCard row, task selector, HeatmapCalendar
- HeatmapCalendar.kt: Canvas-drawn GitHub-style grid (52 weeks × 7 days), category color at varying opacity for completion, amber for grace, gray for missed, tap cell shows day summary bottom sheet
- StatCard.kt: reusable swipeable card (streak numbers, bar chart via Canvas, donut chart via Canvas)
- ConsistencyScore algorithm: weighted formula, animated number + grade badge
- DashboardViewModel.kt: fetches streak data, computes score, exposes heatmap data as 2D array
```

---

### Phase 4 Prompts

**P4-A: Widgets**
```
Create Glance API home screen widgets for DayFlow:
- DayFlowWidget.kt: GlanceAppWidget with two sizes (small 2x1, medium 4x2)
- Small: current task name + time remaining (minute-level) + Done action
- Medium: current task + 2 upcoming + pending count
- WidgetReceiver.kt: GlanceAppWidgetReceiver
- WidgetUpdater: WorkManager periodic task (every 1 min) to refresh widget data from Room
Note: Second-level countdown not possible in widgets, use minute-level updates.
```

**P4-B: Polish & Edge Cases**
```
Add final polish and edge case handling to DayFlow:
1. SplashScreen API: animated logo on launch
2. Shared element transitions between HomeScreen and TimelineScreen
3. Auto-log missed tasks as SKIPPED when their window passes (check in HomeViewModel)
4. Repeat task occurrence tracking (show "3/8 done" on water intake card)
5. Midnight-crossing task handling in GetCurrentTaskUseCase
6. Device boot BroadcastReceiver: reschedule all today's alarms on restart
7. Runtime permission requests: POST_NOTIFICATIONS on first launch, SCHEDULE_EXACT_ALARM via settings redirect
8. Empty states: no tasks today → Rest Day card, no streaks → empty heatmap with setup CTA
```

---

## 10. Free vs Paid Flags

| Feature | Free Approach | Limitation | Paid Alternative |
|---|---|---|---|
| Fonts | Google Fonts bundled in assets | Manual update | N/A |
| Lottie animations | lottiefiles.com free tier | Limited premium animations | Lottie Pro |
| Lock screen live ring | Ongoing notification (best free option) | Not a true fullscreen lock widget (Android 13+ restriction) | Custom accessibility service (complex) |
| Cloud backup/sync | None in v1 (local only) | Data lost on reinstall | Firebase (free tier available for small scale) |
| Exact alarms | AlarmManager + permission prompt | User must grant in settings on Android 12+ | N/A (same API) |
| Widget refresh rate | 1 min via WorkManager | No true second-level countdown in widget | N/A (Android system limit) |
| Charts | Canvas-drawn or MPAndroidChart (free) | Less polished than paid charting libs | None needed |
| Confetti | Konfetti library (free, open source) | None | N/A |
| Push notifications | Local only (WorkManager + AlarmManager) | No server-triggered notifications | FCM (free tier) |
| Analytics | None | No crash reporting | Firebase Crashlytics (free tier) |

---

## Quick Reference — Build Order Checklist

```
Phase 1 □ Project setup + Gradle deps
        □ Room DB: entities + DAOs + repositories
        □ Hilt DI modules
        □ Theme + design system + fonts
        □ Onboarding screens (3 pages)
        □ Routine Builder screen + bottom sheet
        □ Conflict detection

Phase 2 □ Domain use cases (all 5)
        □ Home screen: countdown ring + task card + pending chips
        □ HomeViewModel with ticker
        □ Timeline screen with day pager + blocks
        □ Foreground service + sticky notification
        □ Notification action receiver
        □ PreAlertWorker + EndOfDayWorker
        □ AlarmScheduler + BOOT_COMPLETED receiver

Phase 3 □ WrapUp screen: score ring + confetti + reschedule
        □ Streak computation use case + grace logic
        □ StreakUpdateWorker
        □ Dashboard screen + stat cards
        □ HeatmapCalendar (Canvas)
        □ Consistency Score

Phase 4 □ Glance widgets (small + medium)
        □ Splash screen animation
        □ Screen transitions
        □ Edge cases: missed tasks, repeat tasks, midnight crossing
        □ Permission flows
        □ Empty states
        □ Final QA pass
```

---

*Generated for DayFlow Android App · Kotlin + Jetpack Compose · All Free Stack*
