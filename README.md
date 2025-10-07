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



Generated builds are uploaded to GitHub as artifacts with the format:

