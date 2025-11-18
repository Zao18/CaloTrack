Here is the updated `README.md` content, formatted and ready for you to copy and paste into your GitHub repository.

-----

# CaloTrack Android Application Report 🍏

<img width="298" height="234" alt="image" src="https://github.com/user-attachments/assets/b8aed35b-95d3-4dab-bd76-3e6b20751be1" />


-----

## 1\. Project Overview and Purpose

**CaloTrack** is a fully-featured calorie and nutrition tracking application developed for the Android platform. Its primary purpose is to empower users to manage their health goals through **meticulous food logging** and **personalized calorie target setting**.

The application addresses the common user need for reliable, accessible, and personalized diet management by:

  * **Enabling Comprehensive Calorie Tracking:** Allows users to log consumed foods against a personalized daily calorie goal, displaying historical performance.
  * **Providing User Customization:** Facilitates the creation of custom food items and enables adjustments to personal settings, including goal weight and **multi-language support**.
  * **Ensuring Robust Data Management:** Implements a hybrid local/cloud persistence system to guarantee logging is possible even during periods of network instability (offline mode).
  * **Enhancing User Security and Convenience:** Integrating modern Android features like **biometric authentication** and scheduled notifications for proactive goal attainment.

-----

## 2\. System Architecture and Design Considerations

The architecture of CaloTrack is built upon established Android development best practices, emphasizing **modularity**, data integrity, and a robust persistence layer.

### 2.1. Hybrid Data Persistence (Key Innovation)

A core design consideration was ensuring continuous operation and data integrity, achieved through a hybrid data persistence strategy utilizing **Room Database** as an abstraction layer over SQLite for local storage, coupled with Firebase for the cloud backend.

| Component | Technology | Role |
| :--- | :--- | :--- |
| **Cloud Storage** | **Firebase Realtime Database** | Primary source of truth. Stores user profiles, goals, and the official `food_log`. |
| **Local Storage** | **Room Database (SQLite)** | Caches data and stores **unsynced** food log entries (`OfflineFoodLog`). |
| **Synchronization** | **`SyncManager`** | **Innovation Highlight:** Automatically detects and pushes `unsynced` local data to the Firebase backend when connectivity is restored, preventing data loss during offline use. |

### 2.2. API & Backend Integration

CaloTrack interacts with backend services using a combination of the Firebase SDK and custom Retrofit API endpoints:

| Integration | Technology | Purpose |
| :--- | :--- | :--- |
| **Authentication & Real-time Data** | **Firebase SDK** | Handles secure user sign-up, sign-in, and real-time listening for database changes. |
| **Data Manipulation** | **Retrofit API** (`https://api-q36hnjqyma-uc.a.run.app/`) | Provides RESTful endpoints for CRUD operations on user data: `GET /users/{uid}`, `PUT /users/{uid}`, etc. This is used for structured updates and retrieval of complex objects like the food log. |

### 2.3. Security and User Experience Features

  * **Biometric Authentication**: Utilizes `android.permission.USE_BIOMETRIC` for secure and fast login.
  * **Localization**: Enables dynamic language switching (`en`, `af`, `zu`) via `SettingsActivity` using the Android locale system.
  * **Notifications**: Uses `android.permission.POST_NOTIFICATIONS` and a Firebase Messaging Service for scheduling meal reminders and updates.

-----

## 3\. Quality Assurance & CI/CD with GitHub Actions

**GitHub** is utilized for robust version control, and **GitHub Actions** provides the foundation for our Continuous Integration and Continuous Deployment (CI/CD) pipeline.

### 3.1. GitHub Utilization

  * **Version Control**: Tracks code changes, enabling feature branching, collaborative development, and comprehensive build history.
  * **CI/CD Automation**: GitHub Actions automates the build, test, and artifact generation process, ensuring every merge is validated before deployment.

### 3.2. GitHub Actions Workflow

The workflow (`.github/workflows/generate-apk-aab-debug-release.yml`) is triggered on pushes to `release/**` branches or manually via workflow dispatch.

**Key Workflow Steps**:

1.  **Setup and Permissions**: Configure Java (Zulu JDK 17) and set executable permissions for the Gradle wrapper.
2.  **Run Tests**: Executes all unit tests to ensure code integrity before building any artifacts.
3.  **Build Artifacts**: Generates all necessary distribution files in parallel:
      * Debug APK
      * Release APK
      * Release AAB (Android App Bundle)
4.  **Artifact Upload**: Uploads all generated files to the GitHub Actions run for immediate download and testing.

### 3.3. Artifact Naming

Artifacts are automatically named with the date and type for clear tracking:

  * `2025-10-07 - CaloTrack ID - CaloTrack - APK Debug`
  * `2025-10-07 - CaloTrack ID - CaloTrack - App bundle(s) AAB Release`

-----

## 4\. Release Notes

The following details the evolution of the CaloTrack application since its initial prototype, highlighting key features and major innovations.

### Release 1.0.0 (Initial Prototype Baseline)

  * **Core**: Initial implementation of user registration, login, and the main daily dashboard (`MainActivity`).
  * **Data**: Basic Firebase Realtime Database integration for initial user profile storage.
  * **Functionality**: Placeholder screens for settings and advanced features.

### Release 1.1.0 (Core Tracking Functionality & Persistence Foundation)

  * **Food Logging**: Full implementation of `AddFoodActivity` and `CreateFoodActivity`, allowing users to log pre-existing or custom foods.
  * **History**: Implemented `HistoryActivity` to display daily calorie summaries, showing performance against the goal.
  * **Data Persistence**: **[Innovation Highlight]** Introduced **Room Database (Hybrid Persistence Layer)** to store food logs locally, preparing the app for reliable offline functionality.
  * **Goals**: Added `GoalWeightActivity` and logic for calculating and persisting the user's daily calorie goal.

### Release 1.2.0 (Security, UX, and Offline Synchronization)

  * **Synchronization**: **[Innovation Highlight]** Implemented the **`SyncManager`** for background automatic synchronization, ensuring all offline food logs stored in Room are successfully merged with the Firebase cloud data when connectivity is restored.
  * **User Experience**: **[Innovation Highlight]** Added **Biometric Authentication** (Fingerprint/Face ID) to `LoginActivity` for fast and secure access.
  * **Settings**: **[Innovation Highlight]** Added **Multi-Language Support** in `SettingsActivity`, allowing users to dynamically switch between English, Afrikaans, and Zulu.
  * **Settings**: Implemented user management features: update goal weight, securely log out, and permanently delete account/data from Firebase.

### Release 1.3.0 (Stability and CI/CD)

  * **Notifications**: **[Innovation Highlight]** Integrated **Scheduled Notifications** via a Firebase service and local channels to provide meal reminders and motivational updates.
  * **CI/CD**: Fully integrated the **GitHub Actions** workflow for automated testing and building of all APK/AAB artifacts.
  * **Stability**: Addressed runtime crashes by correcting the `HistoryActivity` declaration in `AndroidManifest.xml`. Fixed an issue where the notification status toast appeared erroneously upon opening `SettingsActivity`.

-----

## 5\. Visual Documentation 📸

### App Home Screen

<img width="446" height="903" alt="image" src="https://github.com/user-attachments/assets/fe64bd9a-31a1-42d3-bc40-7803c4880c59" />


### App Add Food Screen

<img width="447" height="902" alt="image" src="https://github.com/user-attachments/assets/a2a11026-7f50-4d6d-b117-a2f31822069d" />


### App Create Food Screen

<img width="451" height="906" alt="image" src="https://github.com/user-attachments/assets/eaefc2ef-2f1e-431d-96b7-a40b69e6ee19" />


-----

## 6\. References 📚

1.  Ahmed Guedmioui (2024). How To Show Notifications in Android. [YouTube](https://www.youtube.com/watch?v=E0eLKqGgr_4) [Accessed 7 Oct. 2025].
2.  Amazon.in. (2025). AllThatGrows Orange Carrot/Gaajar Gardening Seeds (Pack of 300 Pieces) : Amazon.in: Garden & Outdoors. [Link](https://www.amazon.in/AllThatGrows-Orange-Carrot-Gaajar-Gardening/dp/B0772Q9VH7) [Accessed 7 Oct. 2025].
3.  Android Developers. (2019). Android Developers. [Link](https://developer.android.com)
4.  Android Developers. (2020). Save key-value data. [Link](https://developer.android.com/training/data-storage/shared-preferences)
5.  Android Developers. (2024a). Getting a result from an activity. [Link](https://developer.android.com/training/basics/intents/result)
6.  Android Developers. (2024b). Use saved Preference values. [Link](https://developer.android.com/develop/ui/views/components/settings/use-saved-values)
7.  Android Developers. (2025a). BaseAdapter | API reference | Android Developers. [Link](https://developer.android.com/reference/android/widget/BaseAdapter)
8.  Android Developers. (2025b). Save key-value data. [Link](https://developer.android.com/training/data-storage/shared-preferences)
9.  basickarl (2013). Android: Compress Bitmap from Uri. [Stack Overflow](https://stackoverflow.com/questions/19780812/android-compress-bitmap-from-uri) [Accessed 2014].
10. Big Save Liquor. (2025). Coca-Cola 300ml Can Reg - Big Save Liquor. [Link](https://bigsaveliquor.co.za/product/coca-cola-300ml-can-reg/?srsltid=AfmBOorUcYLJKSXPHghMqxvwa-uunUqQVHPjDeqDQIzs6fc_5guoUX47) [Accessed 7 Oct. 2025].
11. Cal (2021). Schedule Local Notifications Android Studio Kotlin Tutorial. [YouTube](https://www.youtube.com/watch?v=_Z2S63O-1HE)
12. CardioSmart. (2020). Opinions Vary on Benefits of Milk. [Link](https://www.cardiosmart.org/news/2020/2/opinions-vary-on-benefits-of-milk)
13. CodingZest (2023). Android Retrofit Tutorial Using Kotlin 2023 | Getting JSON Response From Api Using Retrofit Kotlin. [YouTube](https://www.youtube.com/watch?v=sRLunCZX2Uc)
14. Diligent Dev (2020). Firebase Cloud Function Tutorial - REST API Part 1 | Diligent Dev. [YouTube](https://www.youtube.com/watch?v=iIVlRZIo2-c) [Accessed 7 Oct. 2025].
15. Dude, T.B. (2023). Vegan Big Mac. [Link](https://theeburgerdude.com/vegan-big-mac/)
16. Evan-Amos (2012). A single banana. [Wikimedia Commons](https://commons.wikimedia.org/wiki/File:Banana-Single.jpg)
17. Greater Chicago Food Depository. (2025). Broccoli | Greater Chicago Food Depository. [Link](https://www.chicagosfoodbank.org/ingredients/broccoli/)
18. Игор (2013). liferay resource within included javascript file. [Stack Overflow](https://stackoverflow.com/questions/18208420/liferay-resource-within-included-javascript-file/18232819#18232819)
19. Istockphoto.com. (2025). Whole Orange Stock Photos. [Link](https://www.istockphoto.com/photos/whole-orange) [Accessed 7 Oct. 2025].
20. Jamshidbek Boynazarov (2025). Kotlin Collections Deep Dive: map, filter, fold, groupBy & More. [Medium](https://jamshidbekboynazarov.medium.com/kotlin-collections-deep-dive-map-filter-fold-groupby-more-215ebd047975) [Accessed 7 Oct. 2025].
21. Justdial.com. (2025). Organic Fresh Apple Fruit 1 Kg in Delhi. [Link](https://www.justdial.com/jdmart/Delhi/Organic-Fresh-Apple-Fruit-1-Kg/pid-2226818126/011PXX11-XX11-220330095305-A5V6) [Accessed 7 Oct. 2025].
22. Karen (2025). Fresh Squeezed Orange Juice. [Link](https://www.kitchentreaty.com/fresh-squeezed-orange-juice/)
23. Kennedy, P. (2013). Who Made That Sliced Bread? The New York Times. [Link](https://www.nytimes.com/2013/03/03/magazine/who-made-that-sliced-bread.html)
24. Levine, E. (2023). A slice of New York pizza history. [Link](https://www.seriouseats.com/new-york-pizza-slice-history)
25. Live Eat Learn (2015). How to Make Greek Yogurt. [Link](https://www.liveeatlearn.com/greek-yogurt/)
26. Medscape (2020). Mifflin-St Jeor equation. [Link](https://reference.medscape.com/calculator/846/mifflin-st-jeor-equation)
27. Minimalist Baker. (2013). How to Cook White Rice | Minimalist Baker Recipes. [Link](https://minimalistbaker.com/how-to-cook-white-rice/)
28. Philipp Lackner (2021). How to Make a Clean Architecture Note App (MVVM / CRUD / Jetpack Compose). [YouTube](https://www.youtube.com/watch?v=8YPXv7xKh2w) [Accessed 7 Oct. 2025].
29. prash (2024). dynamically update TextView in Android. [Stack Overflow](https://stackoverflow.com/questions/4873196/dynamically-update-textview-in-android)
30. Primavera Kitchen. (2021). Garlic Butter Baked Chicken Breast. [Link](https://www.primaverakitchen.com/garlic-butter-baked-chicken-breast/)
31. Stack Overflow. (2012). Animate ProgressBar update in Android. [Link](https://stackoverflow.com/questions/8035682/animate-progressbar-update-in-android)
32. tanzTalks.tech (2021). \#24 Delete User Data | Login and Register Android App using Firebase. [YouTube](https://www.youtube.com/watch?v=Pj12htdSAKE) [Accessed 7 Oct. 2025].
33. The Cozy Cook. (2020). McDonald’s French Fries - Copycat Recipe. [Link](https://thecozycook.com/mcdonalds-french-fries/)
34. Underwood, K. (2024). The Ultimate Creamy Oatmeal. [Link](https://kristineinbetween.com/creamy-oatmeal/)
35. Uq.edu.au. (2018). Are eggs good or bad for our health? [Link](https://stories.uq.edu.au/contact-magazine/eggs-good-or-bad-for-our-health/index.html) [Accessed 7 Oct. 2025].
36. white, I. (2025). Spoon Peanut Butter Images. [Adobe Stock](https://stock.adobe.com/za/search?k=spoon+peanut+butter) [Accessed 7 Oct. 2025].
37. Williams, J. (2023). Top 5 health benefits of avocado. [Link](https://www.bbcgoodfood.com/health/nutrition/health-benefits-avocado)
38. www.youtube.com. (n.d.). Update Data Firebase Android Studio Edit Profile Firebase Realtime Database. [YouTube](https://www.youtube.com/watch?v=L0IIMlJggns)
39. Android Developers. (2025). Login with Biometrics on Android. [online] Available at: [https://developer.android.com/codelabs/biometric-login\#0](https://developer.android.com/codelabs/biometric-login#0).
40. burak selcuk (2023). ADD MULTIPLE LANGUAGES IN YOUR APP - KOTLIN. [online] YouTube. Available at: [https://www.youtube.com/watch?v=ehM1JjCs9PM](https://www.youtube.com/watch?v=ehM1JjCs9PM).
41. Firebase (2019). Firebase Cloud Messaging | Firebase. [online] Firebase. Available at: [https://firebase.google.com/docs/cloud-messaging](https://firebase.google.com/docs/cloud-messaging).
42. Lackner, P. (2023). The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android. [online] [www.youtube.com](https://www.youtube.com). Available at: [https://www.youtube.com/watch?v=bOd3wO0uFr8](https://www.youtube.com/watch?v=bOd3wO0uFr8).

---

