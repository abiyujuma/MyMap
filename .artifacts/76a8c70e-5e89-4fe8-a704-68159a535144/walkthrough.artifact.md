# Walkthrough - Google Maps Style UI Refactor

I have refactored the UI of the map screen to use a Material 3 `BottomSheetScaffold`. This provides a modern, interactive experience similar to the Google Maps app, where controls are tucked away in a draggable bottom sheet.

## Changes Made

### UI Components

#### [MapScreen.kt](file:///C:/Users/A485/Mobile/9-MyMap/app/src/main/java/com/rs/mymap/ui/MapScreen.kt)
- **BottomSheetScaffold Integration:** Replaced the previous `Box` and `Column` overlay with a `BottomSheetScaffold`.
- **Full-Screen Map:** The `GoogleMap` now resides in the main content area of the scaffold, allowing it to occupy the entire background.
- **Draggable Bottom Sheet:**
    - **Sheet Content:** Moved all search controls (Origin, Destination, Mode Selector, Reset) into the bottom sheet.
    - **Peek Height:** Set the `sheetPeekHeight` to `140dp`. When collapsed, it shows a "Compact Summary" with the route duration/distance (if available) and the "Cari Rute" button.
    - **Drag Handle:** Added a Material 3 `DragHandle` to indicate the sheet is interactive.
    - **Smooth Transitions:** Users can drag the sheet up to modify coordinates or change transportation modes, and drag it down to focus on the map.
- **Improved Layout:**
    - Added icons to the text fields (`Place` icon with Blue/Red tints) for better visual clarity.
    - Used `surfaceVariant` colors for the detailed info card within the sheet.
- **Best Practices:** Added `@OptIn(ExperimentalMaterial3Api::class)` to support the Material 3 `BottomSheetScaffold` API.

## Verification Results

### Automated Tests
- The project was successfully compiled using `gradle assembleDebug`.

### Manual Verification
- **Map Focus:** The map is fully visible behind the bottom sheet.
- **Sheet Behavior:** The sheet successfully collapses to show only the summary and expands to show all inputs.
- **Functionality Preservation:**
    - Searching for a route updates the markers and polyline correctly.
    - The "Cari Rute" button works from both the collapsed summary and the expanded view.
    - The "Reset" button clears all fields and collapses the sheet back to its peek state.
    - Switching transport modes (Mobil/Motor) triggers an automatic route refresh as before.

> [!TIP]
> You can tap the "Cari Rute" button directly from the collapsed sheet to quickly update your journey, or drag the handle up to refine your start and end points.

```kotlin
// Scaffold Structure Snippet
BottomSheetScaffold(
    scaffoldState = scaffoldState,
    sheetPeekHeight = 140.dp,
    sheetDragHandle = { BottomSheetDefaults.DragHandle() },
    sheetContent = {
        // Compact Summary & Expanded Controls
    }
) { innerPadding ->
    // Full-screen GoogleMap
}
```
