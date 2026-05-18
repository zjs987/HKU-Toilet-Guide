# HKU Toilet Guide Admin TODO

## Current Code Status

### Already present
- Android Java single-module prototype runs in Android Studio.
- Main user flow exists: home, list, detail, review, ranking, map placeholder, profile.
- Data is currently backed by `MockToiletRepository`.
- User can submit reviews.
- User can report crowd level through `reportCrowdLevel(toiletId, level)`.
- Toilet detail and list pages already display:
  - crowd level
  - facility tags such as `Tissue`, `Dryer`, `Accessible`, `Mirror`

### Not present yet
- No real admin-side page or workflow.
- No issue report model or submission flow.
- No image upload or image moderation flow.
- No user-submitted supply/status update flow such as:
  - no tissue
  - tissue sufficient
  - under maintenance
  - temporarily closed
- No approval/rejection state machine.
- No audit history or operator log.
- No role-based permission enforcement beyond mock user role text.

## Admin Workstreams

### 1. Issue review
- Users submit a toilet issue report.
- Admin reviews pending reports.
- Admin can approve, reject, or mark duplicate.
- Approved issue updates toilet status or creates a maintenance record.

Suggested issue types:
- closed
- under_maintenance
- information_incorrect
- cleanliness_problem
- queue_too_long
- no_tissue
- dryer_broken
- accessible_facility_problem

### 2. Content moderation
- Merge photo moderation and comment moderation into one admin workflow.
- Users submit comments and toilet photos.
- Admin reviews pending content before public display.
- Admin can approve, reject, or remove submitted content.

Suggested moderation content types:
- comment
- photo

Suggested moderation fields:
- contentType
- contentId
- uploader
- uploadTime
- targetToiletId
- reviewStatus
- rejectionReason

### 3. Live toilet status management
- Admin can directly update live status for each toilet.
- Status should be separate from static facility metadata.

Suggested live status fields:
- operationalStatus: `open`, `closed`, `maintenance`
- tissueStatus: `unknown`, `low`, `ok`
- dryerStatus: `unknown`, `broken`, `ok`
- queueStatus: derived from crowd level or editable separately
- lastVerifiedAt
- lastVerifiedBy

## Other Admin Responsibilities Worth Adding

- Review newly submitted toilets or toilet info edits.
- Resolve duplicate issue reports.
- Pin or hide abusive reviews.
- Manage flagged comments or spam.
- Maintain canonical toilet metadata:
  - name
  - floor
  - gender
  - accessible
  - facilities
  - opening hours
- View simple operation dashboard:
  - pending issues
  - pending images
  - recently updated toilets
  - most reported toilets

## Current User-Side Capability Summary

### Can do now
- View toilet crowd level.
- View static facility tags like tissue/dryer presence.
- Submit text reviews with ratings.
- Like reviews.
- Favorite toilets.
- Report crowd level.

### Cannot do now
- Submit issue reports.
- Submit toilet photos.
- Submit consumable status such as tissue low / tissue full.
- Submit maintenance/closure status.
- View approved image gallery.
- View a dedicated "my submissions" page with real records.

## Important Product Distinction

Current code mixes two concepts that should be separated later:

- Static facilities:
  - hasTissue
  - hasDryer
  - hasMirror
  - accessible

- Dynamic live status:
  - tissue low or sufficient
  - dryer broken or working
  - closed or under maintenance
  - temporary issue reports

`hasTissue = true` currently means "this toilet normally provides tissue", not "there is enough tissue right now".

## Confirmed Product Rules

### 1. Static facilities and live status must be separated
- Static facilities describe what the toilet normally has:
  - `hasTissue`
  - `hasDryer`
  - `hasMirror`
  - `accessible`
- Live status describes the current condition:
  - tissue low
  - tissue sufficient
  - under maintenance
  - temporarily closed

### 2. Users can directly submit live status
- User-submitted live status does not require approval before becoming visible.
- Users can only submit predefined status entries.
- Free-text status submission is not allowed.

Suggested predefined status entries:
- tissue_low
- tissue_ok
- dryer_broken
- dryer_ok
- maintenance
- closed_temporarily
- reopened

### 3. Live status and issue/photo/comment flows must be separated
- Live status submission is one workflow.
- Photo submission is another workflow.
- Comment submission is another workflow.
- They should not share the same approval logic.

### 4. Photos and comments require moderation
- User photos require admin review before public display.
- User comments require admin review before public display.
- These two content types share one moderation flow.

### 5. Admin handles resolution, not pre-approval, for live status
- Live status is reported first and becomes visible immediately.
- The admin side still receives the report.
- After the problem is resolved, admin clears or updates the live status.

## Recommended Next Design Discussion

Before coding admin features, confirm:

1. Multiple live statuses can exist at the same time for one toilet.
- Example: `tissue_low` and `maintenance`
- Users can submit multiple predefined tags in one report.
- The UI can display multiple live statuses reported by different users.

2. How should conflicting statuses behave?
- Decision: keep statuses grouped by status type and keep the latest entry in each group.
- Example:
  - tissue group: `tissue_low` vs `tissue_ok`
  - dryer group: `dryer_broken` vs `dryer_ok`
  - operation group: `maintenance` vs `closed_temporarily` vs `reopened`
- New status does not wipe all existing statuses.
- New status only replaces the latest status inside the same group.

3. Does a user status submission expire automatically?
- Example: auto-expire after 2 hours or 12 hours unless reconfirmed

4. Do we need separate admin accounts now, or just a mock admin role for demo?
- Recommended for current stage: mock admin role first.
