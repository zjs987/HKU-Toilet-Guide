# HKU Toilet Guide Prototype

This is a Java Android prototype for the COMP7506C group project.

The current goal is to provide a clean skeleton that the team can extend quickly:

- Map placeholder home screen
- Toilet list with search and sorting
- Toilet detail screen
- Crowd level report
- Review submission
- Google Maps navigation intent
- Profile placeholder
- Local mock repository with HKU sample toilet data

## How to Open

1. Open Android Studio.
2. Choose **Open**.
3. Select this folder:

   `HKUToiletGuidePrototype`

4. Let Android Studio sync Gradle.
5. Run the `app` configuration on an emulator or Android phone.

If Android Studio asks to install SDK components, accept the suggested installation.

If command-line Gradle says `SDK location not found`, install Android SDK from Android Studio, then either set `ANDROID_HOME` or copy `local.properties.example` to `local.properties` and update `sdk.dir`.

## Project Structure

```text
app/src/main/java/com/hku/toiletguide/
├── activity/
│   ├── MainActivity.java       # Map placeholder home screen
│   ├── ListActivity.java       # Searchable/sortable toilet list
│   ├── DetailActivity.java     # Toilet detail, crowd report, navigation
│   ├── ReviewActivity.java     # Rating and comment submission
│   └── ProfileActivity.java    # Profile placeholder
├── data/
│   ├── ToiletRepository.java   # Repository interface
│   └── MockToiletRepository.java
├── model/
│   ├── Toilet.java
│   └── Review.java
└── util/
    ├── DistanceUtil.java
    └── UiFactory.java
```

## Recommended Next Steps

1. Replace the map placeholder in `MainActivity` with Google Maps SDK.
2. Replace `MockToiletRepository` with `FirebaseToiletRepository`.
3. Add `google-services.json` after creating the Firebase project.
4. Move the sample toilets from `MockToiletRepository.seed()` into Firestore.
5. Add a real filter dialog for gender, accessible toilets, facilities, rating, and crowd level.
6. Add photo upload with Firebase Storage only after the text review flow is stable.

## Firestore Draft

```text
toilets/{toiletId}
├── building
├── floor
├── gender
├── latitude
├── longitude
├── accessible
├── has_dryer
├── has_tissue
├── has_mirror
├── stalls
├── avg_cleanliness
├── avg_crowdedness
├── avg_overall
├── total_reviews
├── current_crowd_level
└── opening_hours

toilets/{toiletId}/reviews/{reviewId}
├── user_name
├── cleanliness
├── crowdedness
├── overall
├── comment
└── created_at
```

## MVP Submission Checklist

- App launches without crashing.
- List screen displays at least 15 real HKU toilet records.
- Detail screen shows building, floor, gender, facilities, rating, and crowd level.
- User can submit at least one review or crowd report.
- Navigation button opens Google Maps.
- README explains how to compile and run.
- Final report includes background research, design, features, screenshots, and contribution table.
- Demo video shows map/list, detail, review/crowd report, and navigation.
