# Lost Items Feature - Complete Implementation Guide

## Overview
This feature allows users to report items they have lost and automatically see matching "Found" posts from other users. The system uses intelligent matching based on item name and location.

## How It Works

### For Users Reporting Lost Items:
1. **Report a Lost Item**
   - Go to Add Post screen
   - Select "What did you Lost?" button (already selected by default)
   - Enter: Item Name, Description, and Location Lost
   - Click "Post" - This creates a lost item report (NOT a regular post)

2. **View Lost Items and Matches**
   - Go to "My Posts" (new icon at top of Home screen)
   - Click "Lost Items" tab
   - See all your reported lost items
   - Click "View Matches" on any lost item to see potential matches

3. **Matching System**
   - System automatically finds "Found" posts that match your lost item
   - Match score is calculated based on:
     - Item name similarity (50 points for exact match, 30 for partial)
     - Location similarity (30 points for exact match, 15 for partial)
   - Only shows items with match score > 0

### Example Scenario:
**User reports lost item:**
- Item: "Watch"
- Description: "Silver watch with black leather strap"
- Location: "Cafe"

**System will show matching found posts:**
- Post 1: "Watch" found in "Cafe" → Match Score: 80 (exact name + exact location)
- Post 2: "Wrist Watch" found in "Cafeteria" → Match Score: 45 (partial name + partial location)
- Post 3: "Watch" found in "Library" → Match Score: 50 (exact name only)

## Files Created/Modified

### Database Files:
1. **`database/LOST_ITEMS_FEATURE.sql`**
   - Creates `lost_items` table
   - Adds matching algorithm
   - Sample data for testing

### API Endpoints:
1. **`api/posts/create_lost_item.php`**
   - POST endpoint to create a lost item report
   - Parameters: user_id, item_name, item_description, location_lost

2. **`api/posts/get_my_lost_items.php`**
   - GET endpoint to fetch user's lost items
   - Parameters: user_id

3. **`api/posts/get_matching_posts.php`**
   - GET endpoint to find matching found posts
   - Parameters: lost_item_id OR (item_name + location)
   - Returns posts sorted by match score

### Android App Files:

#### Models:
1. **`app/src/main/java/.../models/LostItem.kt`**
   - Data class for lost items
   - Data class for matching posts
   - Data class for post users

#### Activities:
1. **`AddPostActivity.kt`** (Modified)
   - Added `createLostItemReport()` method
   - Separates lost item reports from found posts
   - Default selection is "Lost" item

2. **`MyPostsActivity.kt`** (Completely Rebuilt)
   - Added TabLayout with 2 tabs:
     - "Found Posts" - Shows items you found
     - "Lost Items" - Shows items you lost
   - Added 3 RecyclerView Adapters:
     - `MyPostsAdapter` - For found posts
     - `LostItemsAdapter` - For lost items list
     - `MatchingPostsAdapter` - For matching posts dialog
   - Dialog system to show matching posts

#### Layouts:
1. **`activity_my_posts.xml`** (Modified)
   - Added TabLayout for switching views
   - Restructured for better organization

2. **`item_lost_item.xml`** (New)
   - Card layout for displaying lost items
   - Shows: name, description, location, date, status
   - "View Matches" button

3. **`dialog_matching_posts.xml`** (New)
   - Dialog to show matching found posts
   - Contains RecyclerView for matches
   - Progress bar and empty state

4. **`item_matching_post.xml`** (New)
   - Card layout for individual matching post
   - Shows: name, description, location, match score
   - Color-coded match score badge

#### Home Activity:
1. **`activity_home.xml`** (Modified)
   - Repositioned 3 icons to top:
     - Chat icon (email icon)
     - My Posts icon (agenda/list icon) - NEW ICON
     - Profile icon
   - All icons now have white tint on purple background

2. **`strings.xml`** (Modified)
   - Added: messages, my_posts, profile strings

## Database Setup

### Step 1: Run the SQL script
```bash
# Open phpMyAdmin or MySQL command line
# Navigate to your database: lost_and_found_db
# Run the script: database/LOST_ITEMS_FEATURE.sql
```

This creates:
- `lost_items` table
- Matching algorithm
- Sample test data

## API Deployment

### Copy API files to XAMPP:
```bash
# Updated batch file to use D:\xampp\ path
# Run: copy_to_xampp.bat
```

This copies all API files including the new endpoints:
- `create_lost_item.php`
- `get_my_lost_items.php`
- `get_matching_posts.php`

## Testing the Feature

### Test Case 1: Report a Lost Item
1. Open app → Click Add Post (+ button)
2. "What did you Lost?" is selected by default
3. Enter:
   - Item Name: Watch
   - Description: Silver watch with black strap
   - Location: C110
4. Click "Post"
5. Should see: "Lost item reported successfully! We'll notify you of matches."

### Test Case 2: View Lost Items
1. Click My Posts icon (top right, middle icon)
2. Click "Lost Items" tab
3. Should see your lost item listed
4. Click "View Matches" button

### Test Case 3: See Matching Posts
1. Dialog opens showing "Possible Matches for: Watch"
2. If matches found:
   - Shows list of found posts
   - Each with match score
   - Higher scores = better match
3. If no matches:
   - Shows: "No matching posts found yet..."

### Test Case 4: Real Match Scenario
1. User A reports lost: "Watch" in "C110"
2. User B posts found: "Watch" in "C-110" (with image)
3. User A clicks "View Matches" on their lost item
4. Should see User B's found post with high match score (80)
5. User A can contact User B via the contact information

## Match Score Breakdown

| Match Type | Points | Example |
|------------|--------|---------|
| Exact item name | 50 | "Watch" = "Watch" |
| Partial item name | 30 | "Watch" contains "Wat" |
| Exact location | 30 | "C110" = "C110" |
| Partial location | 15 | "C110" contains "C1" |

**Minimum match score to show:** 1 point
**Maximum match score:** 80 points

## Key Features

### Smart Matching:
- Case-insensitive comparison
- Partial word matching
- Location variants (C-110 matches C110)
- Sorted by match score (best matches first)

### User Experience:
- Separate tabs for Found vs Lost
- Visual match score indicators
- Real-time matching
- Contact information available
- Clean, intuitive UI

### Data Organization:
- Lost items stored separately from found posts
- No clutter in main posts feed
- Better analytics possible
- Future: Push notifications for new matches

## Troubleshooting

### Lost Item Not Saving:
1. Check Logcat for errors (Tag: "AddPostActivity")
2. Verify XAMPP Apache is running
3. Check API is accessible: http://localhost/lost_and_found_api/posts/create_lost_item.php
4. Verify database table `lost_items` exists

### No Matches Showing:
1. Check if there are any "Found" posts in database
2. Verify match_score calculation in API
3. Check Logcat (Tag: "MyPostsActivity")
4. Test API directly with: http://localhost/lost_and_found_api/posts/get_matching_posts.php?lost_item_id=1

### UI Issues:
1. Ensure all layout files are created
2. Check TabLayout is visible
3. Verify adapters are properly initialized
4. Check RecyclerView adapters are set correctly

## Future Enhancements

1. **Push Notifications**
   - Notify users when new matching posts appear
   - Daily digest of potential matches

2. **Advanced Filtering**
   - Filter by date range
   - Filter by match score threshold
   - Sort by different criteria

3. **In-App Chat**
   - Direct messaging between users
   - Share additional details/photos

4. **Image Recognition**
   - AI-powered image matching
   - Visual similarity scoring

## Summary

The Lost Items feature is now fully functional with:
- ✅ Separate lost item reporting system
- ✅ Intelligent matching algorithm
- ✅ Two-tab My Posts interface
- ✅ Dialog-based match viewing
- ✅ Match score calculation
- ✅ Complete API backend
- ✅ Updated UI with repositioned icons
- ✅ Comprehensive logging for debugging

Users can now easily report lost items and get intelligent matches from found posts!

