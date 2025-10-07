# Frogobox Android App

<img width="262" height="234" alt="image" src="https://github.com/user-attachments/assets/b24d5dcf-bf33-4e1f-924d-95aa38759165" />

## Purpose

**Frogobox** is an Android application designed to provide a seamless user experience for [describe app functionality, e.g., tracking tasks, calories, content, or whatever your app does]. The app aims to:  

- Enable users to [main app functionality]  
- Ensure quick and reliable builds for testing and deployment  
- Provide easy management of app versions and releases  

This project automates the build process and simplifies artifact management using **GitHub Actions**, making it easier for developers to distribute and test builds.

---

## Design Considerations

When designing Frogobox, several key factors were considered:  

- **Modular Architecture**: The app follows a modular design with a main `app` module, allowing scalability and easy management of features.  
- **Gradle Integration**: All build tasks are handled through Gradle for consistency and automation.  
- **Version Control**: The project is managed via GitHub to track changes, enable collaboration, and maintain a history of builds.  
- **Automated Builds**: GitHub Actions automatically generates APKs and AABs for both debug and release versions.  
- **User-Friendly**: Focused on simplicity and efficiency in the user interface and interaction design.

## API

CaloTrack interacts with backend services via APIs and Firebase:

Firebase Realtime Database: Stores user data, food logs, and custom foods.

Firebase Authentication: Handles secure login and registration.

Retrofit API Endpoints (hosted at https://api-q36hnjqyma-uc.a.run.app/):

GET /users/{uid} → Fetch user data (including food log and custom foods)

POST /users → Create a new user

PUT /users/{uid} → Update user info (profile, goal weight, etc.)

PUT /users/{uid}/clearFoodLog → Reset daily food log

All network operations use Retrofit with callbacks (Call<T>), ensuring asynchronous requests. Base64-encoded images are sent for custom food items.

Firebase Auth operations (sign-up, sign-in) are handled separately through Firebase SDK; these don’t hit the Retrofit API directly.

---

## GitHub & GitHub Actions

GitHub is used for:

- **Version Control**: Tracking code changes and collaboration.  
- **Repository Hosting**: Centralized place for storing and managing the app.  
- **CI/CD Automation**: GitHub Actions automates the following tasks:  
  - Running tests  
  - Building debug APKs  
  - Building release APKs  
  - Building release AAB (App Bundles)  
  - Uploading generated artifacts for download and testing  

This ensures a smooth workflow from development to deployment.

---

## Workflow

The GitHub Actions workflow (`.github/workflows/generate-apk-aab-debug-release.yml`) is triggered:  

- On pushes to branches matching `release/**`  
- Manually via workflow dispatch  

**Workflow Steps**:  

1. Checkout repository  
2. Set environment variables (date, repository name)  
3. Setup Java (Zulu JDK 17)  
4. Change Gradle wrapper permissions  
5. Run Gradle tests  
6. Build debug APK  
7. Build release APK  
8. Build release AAB  
9. Upload all generated artifacts to GitHub  

---

## Artifacts

Examples:

- `2025-10-07 - Frogobox ID - Frogobox - APK Debug`  
- `2025-10-07 - Frogobox ID - Frogobox - APK Release`  
- `2025-10-07 - Frogobox ID - Frogobox - App bundle(s) AAB Release`  

These artifacts can be downloaded directly from the GitHub Actions page for testing or deployment.

---

- App Home Screen  
<img width="386" height="692" alt="image" src="https://github.com/user-attachments/assets/48dda1db-2892-41d3-b2bf-0d32dd7db6c3" />

## References

1. Ahmed Guedmioui (2024). How To Show Notifications in Android. [YouTube](https://www.youtube.com/watch?v=E0eLKqGgr_4) [Accessed 7 Oct. 2025].  
2. Amazon.in. (2025). AllThatGrows Orange Carrot/Gaajar Gardening Seeds (Pack of 300 Pieces) : Amazon.in: Garden & Outdoors. [Link](https://www.amazon.in/AllThatGrows-Orange-Carrot-Gaajar-Gardening/dp/B0772Q9VH7) [Accessed 7 Oct. 2025].  
3. Android Developers. (2019). Android Developers. [Link](https://developer.android.com)  
4. Android Developers. (2020). Save key-value data. [Link](https://developer.android.com/training/data-storage/shared-preferences)  
5. Android Developers. (2024a). Getting a result from an activity. [Link](https://developer.android.com/training/basics/intents/result)  
6. Android Developers. (2024b). Use saved Preference values. [Link](https://developer.android.com/develop/ui/views/components/settings/use-saved-values)  
7. Android Developers. (2025a). BaseAdapter  |  API reference  |  Android Developers. [Link](https://developer.android.com/reference/android/widget/BaseAdapter)  
8. Android Developers. (2025b). Save key-value data. [Link](https://developer.android.com/training/data-storage/shared-preferences)  
9. basickarl (2013). Android: Compress Bitmap from Uri. [Stack Overflow](https://stackoverflow.com/questions/19780812/android-compress-bitmap-from-uri) [Accessed 2014].  
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
32. tanzTalks.tech (2021). #24 Delete User Data | Login and Register Android App using Firebase. [YouTube](https://www.youtube.com/watch?v=Pj12htdSAKE) [Accessed 7 Oct. 2025].  
33. The Cozy Cook. (2020). McDonald’s French Fries - Copycat Recipe. [Link](https://thecozycook.com/mcdonalds-french-fries/)  
34. Underwood, K. (2024). The Ultimate Creamy Oatmeal. [Link](https://kristineinbetween.com/creamy-oatmeal/)  
35. Uq.edu.au. (2018). Are eggs good or bad for our health? [Link](https://stories.uq.edu.au/contact-magazine/eggs-good-or-bad-for-our-health/index.html) [Accessed 7 Oct. 2025].  
36. white, I. (2025). Spoon Peanut Butter Images. [Adobe Stock](https://stock.adobe.com/za/search?k=spoon+peanut+butter) [Accessed 7 Oct. 2025].  
37. Williams, J. (2023). Top 5 health benefits of avocado. [Link](https://www.bbcgoodfood.com/health/nutrition/health-benefits-avocado)  
38. www.youtube.com. (n.d.). Update Data Firebase Android Studio Edit Profile Firebase Realtime Database. [YouTube](https://www.youtube.com/watch?v=L0IIMlJggns)  

---

