# HKU Toilet Guide

HKU Toilet Guide is an Android application for the COMP7506C Smart Phone Apps Development group project. The app helps HKU students and visitors find suitable toilets on campus by combining searchable toilet information, Google Maps, ratings, live status reports, favorites, user reviews, and an admin moderation workflow.

The project is written in Java and built with Android Studio. It uses Google Maps SDK for Android for the map screen, and it stores demo users, toilets, reviews, likes, favorites, reports, and moderation records locally with `SharedPreferences` and JSON serialization.

## Main Features

### 1. Login and User Roles

- Users enter the app through a login screen.
- Demo student and admin accounts are provided for testing.
- New local user accounts can be created from the registration screen.
- Login state is stored locally, so the app can remember the current user.
- Admin users enter a separate admin console instead of the normal user homepage.

### 2. Home Screen and Toilet Discovery

- The home screen shows nearby toilet records with a campus-themed visual design.
- Users can search by building name, floor, gender, facility, and notes.
- Quick filters support male toilet, female toilet, accessible toilet, tissue, and hand dryer.
- Advanced filter supports facility filtering and sorting.
- Results show toilet name, floor, distance, star rating, facilities, and crowd level.
- Crowd level is shown with person icons, where more highlighted people means more crowded.

### 3. Google Maps Screen

- The map screen displays toilet markers around HKU.
- Users can tap a marker to preview toilet name, rating, and crowd status.
- Tapping the marker info window opens the toilet detail page.
- The app can show the user's current location when location permission is granted.
- A custom blue direction marker is used when orientation sensor data is available.
- Detail pages include a `Go` action that opens the map and focuses on the selected toilet.

### 4. Toilet Detail Page

Each toilet detail page includes:

- Building and floor information
- Location note
- Gender type
- Facility tags, such as accessible toilet, tissue, dryer, baby room, and sink
- Current crowd level
- Live status, such as normal, closed, maintenance, no tissue, or queue
- Cleanliness, quietness, and overall rating
- User reviews and review images
- Favorite button
- Review button
- Status report button
- Admin direct-edit controls when logged in as admin

### 5. Reviews, Ratings, and Likes

- Users can add reviews for a toilet.
- Reviews include cleanliness, crowdedness, and overall star ratings.
- Star rating uses a five-star scale.
- Users can write review comments.
- Some seeded reviews include toilet photos for demonstration.
- Users can like reviews.
- Review likes are stored locally.

### 6. Favorites and Personal Page

- Users can save toilets to favorites.
- The Mine page shows account information and shortcuts.
- Users can view saved toilets.
- Users can view their own submissions and moderation status.
- Users can log out and return to the login screen.

### 7. Ranking

- Toilets are ranked by review data and overall rating.
- The top three toilets use gold, silver, and bronze medal icons.
- Later ranks use numbered circles.
- Ranking helps users quickly find highly rated toilets on campus.

### 8. Live Status Reporting

Users can submit live issue reports, including:

- Toilet closed
- Under maintenance
- No tissue
- Long queue
- Strong smell or other abnormal condition

These reports appear in the app and can be reviewed or resolved by the admin.

### 9. Admin Console

Admin users have a separate management interface. The admin console supports:

- Viewing pending user submissions
- Approving or rejecting submitted reviews/photos
- Viewing active toilet status reports
- Resolving live issues
- Updating crowd level or operational status from the toilet detail page

This makes the project more than a simple toilet listing app: it includes a basic user-generated-content and moderation workflow.

## Demo Accounts

| Role | Email | Password | Purpose |
| --- | --- | --- | --- |
| Student | `hku.student@connect.hku.hk` | `student123` | Normal app usage |
| Admin | `admin@hku.hk` | `admin123` | Admin console and moderation |
| Demo reviewer | `reviewer002@connect.hku.hk` | `demo123` | Seeded review account |
| Demo reviewer | `reviewer003@connect.hku.hk` | `demo123` | Seeded review account |
| Demo reviewer | `guide004@connect.hku.hk` | `demo123` | Seeded review account |
| Demo reviewer | `rush005@connect.hku.hk` | `demo123` | Seeded review account |

Registered accounts are stored locally on the device.

## How to Build and Run

### Requirements

- Android Studio
- Android SDK installed through Android Studio
- Java/Gradle environment managed by Android Studio
- A physical Android phone or Android emulator
- Internet connection for Google Maps tiles

### Open the Project

1. Open Android Studio.
2. Choose `Open`.
3. Select this folder:

   ```text
   E:\Software\Android\HKU-Toilet-Guide
   ```

4. Wait for Gradle sync to finish.
5. Select an emulator or physical Android device.
6. Run the `app` configuration.

### Command-Line Build

From the project root:

```powershell
.\gradlew.bat :app:assembleDebug --no-daemon
```

If Android Studio reports missing SDK configuration, make sure `local.properties` exists and points to the correct Android SDK path.

## Google Maps Configuration

The Google Maps API key is configured in:

```text
app/src/main/AndroidManifest.xml
```

Look for:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_API_KEY" />
```

If the map does not load on a different machine, replace the key with a valid Google Maps SDK for Android key. The key should be enabled for Maps SDK for Android and bound to the correct package name and SHA-1 certificate.

## Software Usage Guide

### 1. Start the App

Open the app from Android Studio or from the device launcher. The login page will be shown if there is no active user session.

### 2. Log In

Use the student demo account for normal testing:

```text
Email: hku.student@connect.hku.hk
Password: student123
```

Use the admin demo account for moderation testing:

```text
Email: admin@hku.hk
Password: admin123
```

You can also tap `Create a local account` to register a new local user.

### 3. Find a Toilet from Home

On the Home tab:

1. Type a building name, floor, or facility keyword into the search bar.
2. Tap the quick filter icons to filter by gender or facilities.
3. Tap `Filter` for advanced filtering and sorting.
4. Tap any toilet card to open its detail page.

### 4. Use the Map

On the Map tab:

1. View toilet markers around HKU.
2. Tap a marker to see the toilet name, rating, and crowd status.
3. Tap the info window to open the detail page.
4. Grant location permission if you want the app to show your current location.

### 5. View Toilet Details

On a toilet detail page, users can:

- Read toilet information and facilities
- Check star ratings and crowd level
- View user reviews and photos
- Tap the heart icon to save or unsave the toilet
- Tap `Go` to focus the toilet on the map
- Tap `Add review` to submit a new review
- Tap status/report options to report live issues

### 6. Add a Review

1. Open a toilet detail page.
2. Tap `Add review`.
3. Select ratings for cleanliness, crowdedness, and overall experience.
4. Write a comment.
5. Submit the review.

The review is stored locally. Depending on the workflow, submitted content may appear in the admin moderation queue before becoming public.

### 7. Manage Favorites

1. Open a toilet detail page.
2. Tap the heart icon near the toilet name.
3. Go to the Mine tab.
4. Open `My favorites` to view saved toilets.

### 8. Check Ranking

Open the Ranking tab to see toilets sorted by rating. The top three toilets use medal icons to make the best toilets easy to identify.

### 9. Use the Admin Console

Log in with the admin account:

```text
Email: admin@hku.hk
Password: admin123
```

The admin console allows the admin to:

1. Review pending user submissions.
2. Approve or reject content.
3. View active live status reports.
4. Resolve reported issues.
5. Open toilet detail pages and update crowd/status information directly.

### 10. Log Out

Open the Mine tab and tap `Log out`. The app returns to the login page.

## Data Storage

This prototype uses local persistent storage instead of an external server database.

- Seed data is defined in the repository layer.
- Runtime data is saved through `ToiletGuideDatabase.java`.
- `SharedPreferences` stores serialized JSON for users, reviews, likes, favorites, submissions, live status reports, crowd levels, and current login state.
- Clearing app storage or uninstalling the app resets the local demo data.

This design is suitable for a stable course demonstration. If future work requires real multi-device synchronization, the app can be extended with Firebase Authentication, Cloud Firestore, and Firebase Storage.

## Project Structure

```text
app/src/main/java/com/hku/toiletguide/
+-- activity/
|   +-- LoginActivity.java              # Login and registration
|   +-- MainActivity.java               # Home, map, ranking, and mine tabs
|   +-- DetailActivity.java             # Toilet details, reviews, favorites, reports
|   +-- ContentSubmissionActivity.java  # Add review and submit content
|   +-- AdminActivity.java              # Admin dashboard and moderation
|   +-- FavoritesActivity.java          # Saved toilets
|   +-- MySubmissionsActivity.java      # User submission history
|   +-- ImagePreviewActivity.java       # Full-screen review image preview
|   +-- ListActivity.java               # Legacy/list activity
|   +-- MapActivity.java                # Legacy/map activity
|   +-- ProfileActivity.java            # Legacy/profile activity
|   +-- RankingActivity.java            # Legacy/ranking activity
|   +-- ReviewActivity.java             # Legacy/review activity
|   +-- StatusReportActivity.java       # Live status reporting
+-- data/
|   +-- ToiletRepository.java           # Repository interface
|   +-- MockToiletRepository.java       # Local repository with seeded HKU data
|   +-- ToiletGuideDatabase.java        # Local JSON persistence
+-- model/
|   +-- Toilet.java
|   +-- Review.java
|   +-- ContentSubmission.java
|   +-- LiveStatusReport.java
|   +-- User.java
+-- util/
    +-- DistanceUtil.java
    +-- UiFactory.java
```

## Demo Notes

- If new seeded data does not appear, uninstall the app or clear app storage and run it again.
- If the launcher icon does not update, uninstall the old app from the emulator before reinstalling.
- Location direction behavior works better on a physical phone than on an emulator because emulators may not provide real compass data.
- The map requires network access and a valid Google Maps API key.

## Current Limitations and Future Work

- The current database is local to one device and does not sync across devices.
- Review images are stored as local/demo image references rather than uploaded to cloud storage.
- Route planning is not implemented as a full in-app navigation engine; the current focus is map markers, location, and toilet discovery.
- A future version could add Firebase backend, image upload, campus indoor floor plans, and route guidance.
