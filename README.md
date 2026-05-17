# HKU Toilet Guide

HKU Toilet Guide is an Android application designed to help HKU students, staff, and visitors find suitable toilets on campus more easily. HKU's campus is built across slopes, bridges, corridors, and connected buildings, so finding a nearby toilet can be inconvenient, especially for freshmen, exchange students, and first-time visitors.

The app provides an interactive campus toilet map with detailed floor-level information, ratings, crowd reports, filters, navigation, user reviews, and community-submitted toilet data.

## Features

- Interactive Google Maps view centered on HKU campus
- Toilet markers with gender type and crowding status
- Toilet list with search, sorting, and filtering
- Filters for gender, accessibility, facilities, rating, distance, and availability
- Toilet detail page with floor, opening hours, facilities, ratings, and comments
- Real-time crowd level reporting with quick emoji buttons
- Status reporting for issues such as no tissue, maintenance, or out of order
- User reviews with cleanliness, crowdedness, and overall ratings
- Photo upload for toilet reviews and new toilet submissions
- Google Maps walking navigation
- Smart recommendation system based on distance, cleanliness, and crowdedness
- Rankings for cleanest toilets, hidden gems, and most popular toilets
- User profile with favorites, review history, submissions, achievements, and exploration progress
- Admin review system for user-submitted toilet locations

## Target Users

The main target users are HKU students, especially freshmen and exchange students. The app is also useful for staff members and campus visitors who are unfamiliar with the building layout or need to quickly find a cleaner or less crowded toilet during peak hours.

## Recommendation Logic

The recommendation system ranks toilets using three main factors:

- Distance from the user
- Average cleanliness rating
- Current or historical crowdedness level

The app prioritizes real-time crowd reports when available. If no recent reports exist, it falls back to historical crowd patterns for the current day and hour.

## Data Collection

Initial toilet data is collected manually on HKU campus. For each toilet, the team records:

- Building name
- Floor
- Coordinates
- Gender type
- Number of stalls
- Accessibility
- Available facilities
- Phone signal quality
- Photos
- Initial ratings and comments

The expected dataset contains around 40 to 60 toilets across HKU campus.

## Installation

1. Clone this repository.
2. Open the project in Android Studio.
3. Add your Firebase configuration file:

   ```text
   app/google-services.json
   ```

4. Add a valid Google Maps API key.
5. Enable the required Firebase services:

   - Firebase Authentication
   - Cloud Firestore
   - Firebase Storage

6. Build and run the app on an Android device or emulator.

## Permissions

The app may request the following permissions:

- Location permission for nearby toilet search and distance calculation
- Camera or photo library access for review photos and new toilet submissions
- Internet access for Firebase and Google Maps services

## Team Plan

The project is developed as a two-person Android course project. The work is divided across map/list/filter features, detail pages, reviews, Firebase integration, ranking, recommendation, user profile, submission review, data collection, testing, documentation, and demo video production.

## Future Improvements

- More accurate indoor location guidance
- Larger toilet dataset covering more HKU buildings
- Better moderation tools for submitted photos and reviews
- Daily automated update of historical crowd patterns
- More achievement badges and gamified exploration features

## License

This project is developed for academic coursework.
