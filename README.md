# TopoGrade / Farmer

Tablet-first Android app for topographic surveying, terrain modeling and RTK field workflows.

## Current development — v0.6
- Tablet-first two-pane field workspace
- Offline projects and survey points
- LOCAL / WGS84 / UTM project CRS
- Delaunay TIN foundation
- MapLibre live map
- TCP + Bluetooth GNSS
- NMEA GGA parser with RTK FIX/FLOAT
- NTRIP / RTCM forwarding
- GNSS watchdog + auto reconnect
- One-tap RTK FIX capture
- 5-second RTK averaging with horizontal/vertical spread
- Correction age + base station ID display
- Persistent Track recording
- Persistent Boundary vertices
- WGS84 → UTM conversion
- Offline map region model (download pipeline next)
- Android CI: unit tests + debug APK

## Next
1. MapLibre OfflineManager tile download UI
2. Draw track/boundary directly as map layers
3. GST/GSA parsing for receiver-reported accuracy and DOP
4. Track distance / boundary area calculations
5. Local-site calibration / localization
6. Cut/fill surface and grading design

> Clean-room implementation; no proprietary APK source or assets are copied.