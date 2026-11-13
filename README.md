# TopoGrade / Farmer

Tablet-first Android app for topographic surveying, terrain modeling and RTK field workflows.

## Current development — v0.7
- Tablet-first two-pane field workspace
- Offline projects / XYZ points / CSV
- LOCAL / WGS84 / UTM coordinate systems
- Delaunay TIN foundation
- MapLibre live map
- Track and Boundary rendered on MapLibre as GeoJSON layers
- Real MapLibre OfflineRegion download around current GNSS position
- TCP + Bluetooth GNSS
- NMEA GGA + GSA + GST parsing
- GPS / DGPS / RTK FIX / RTK FLOAT
- DOP and GST sigma quality display
- NTRIP / RTCM forwarding
- GNSS watchdog + auto reconnect
- One-tap RTK capture + 5-second averaging
- Correction age / base station ID
- Persistent Track + Boundary
- Track length + Boundary area
- WGS84 ↔ UTM conversion
- Two-control-point Local Site calibration with persistent 2D similarity transform
- Local ↔ UTM ↔ WGS84 map projection path
- Unit tests + Android CI

## Next
1. Offline map pack manager: list / pause / resume / delete packs
2. Polygon fill and point symbols on MapLibre
3. Multi-control least-squares local calibration and residual report
4. Antenna height / pole height corrections
5. Geoid / orthometric height workflow
6. Cut/fill surface, volume and grading design

> Clean-room implementation; no proprietary APK source or assets are copied.