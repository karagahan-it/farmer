# TopoGrade / Farmer

Tablet-first Android app for topographic surveying, terrain modeling and RTK field workflows.

## Current development — v0.4
- Tablet-first two-pane field workspace
- Offline project creation and persistence
- Manual XYZ points + edit/delete
- CSV import/export
- LOCAL / WGS84 / UTM project CRS
- Delaunay TIN foundation
- MapLibre map
- NMEA GGA parser
- GNSS status: GPS / DGPS / RTK FIX / RTK FLOAT
- TCP GNSS transport
- Bluetooth RFCOMM GNSS transport foundation
- NTRIP client with RTCM forwarding to active GNSS transport
- Android CI build check

## Next
1. Bluetooth paired-device picker + runtime permission flow
2. Real-time GNSS session controller with reconnect/watchdog
3. Save FIX directly as survey point
4. GGA upload to NTRIP caster where required
5. Offline MapLibre tile packs
6. UTM/WGS84 conversion engine
7. Contour rendering and cut/fill surfaces

> Clean-room implementation; no proprietary APK source or assets are copied.
