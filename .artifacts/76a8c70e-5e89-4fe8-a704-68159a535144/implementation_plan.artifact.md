# Implementation Plan - Google Maps Style Bottom Sheet

Refactor the UI to use a Material 3 `BottomSheetScaffold`, moving the search controls from a fixed top panel into a draggable bottom sheet.

## Proposed Changes

### UI Components

#### [MODIFY] [MapScreen.kt](file:///C:/Users/A485/Mobile/9-MyMap/app/src/main/java/com/rs/mymap/ui/MapScreen.kt)
- **Scaffold Setup:** Wrap the screen in a `BottomSheetScaffold`.
- **Map Content:** Move the `GoogleMap` composable into the main content area of the scaffold.
- **Sheet Content:**
    - Move all existing controls (Origin, Destination, Mode Selector, Buttons, Info Card) into the `sheetContent`.
    - Add a `BottomSheetDefaults.DragHandle()` at the top.
    - Implement a "Compact Summary" view that remains visible when the sheet is collapsed (peek height). This will include the "Cari Rute" button and basic route info.
    - Implement an "Expanded Content" view that shows the full input fields and settings.
- **State Management:**
    - Use `rememberBottomSheetScaffoldState()` to manage the sheet's behavior.
    - Calculate visibility of components based on the `bottomSheetState` progress or current value to achieve the "Google Maps" feel.
- **Visuals:**
    - Set `sheetPeekHeight` to an appropriate value (e.g., 120dp) to show the compact summary.
    - Ensure the map occupies the full screen background.

## Verification Plan

### Manual Verification
- Deploy the app to a device or emulator.
- Verify that the map is full-screen.
- Verify that a bottom sheet is visible at the bottom.
- Drag the sheet up to see all controls (Origin, Destination, etc.).
- Drag the sheet down to collapse it.
- Verify that in the collapsed state, the "Cari Rute" button is still accessible.
- Perform a route search and verify that markers and polylines update as before.
- Test the "Reset" button and "Transport Mode" selector.
- Ensure the sheet transitions smoothly between Collapsed and Expanded states.
