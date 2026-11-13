# TopoGrade / Farmer

Tablet-first Android app for professional RTK surveying, terrain modeling and grading workflows.

## Current development — v0.8
- Tablet-first field workspace
- TCP + Bluetooth GNSS, NMEA, NTRIP/RTCM
- RTK FIX capture + 5s averaging
- GGA/GSA/GST quality metrics
- Track / Boundary persistence and MapLibre overlays
- Offline map download + Offline Pack manager (list / pause / resume / delete)
- WGS84 ↔ UTM
- Two-point local calibration + multi-point least-squares similarity engine
- Antenna / pole height correction
- GGA orthometric vs ellipsoidal height mode
- Manual geoid undulation support for ellipsoidal input
- Persistent project height settings
- Design Plane persistence
- Initial Cut/Fill engine and summary
- Track length + Boundary area
- Unit tests + Android CI

## Next
1. Multi-control calibration UI with residual table and reject/outlier controls
2. Geoid grid model support (EGM) instead of manual N value
3. Antenna-height presets and instrument profiles
4. Cut/Fill heatmap on MapLibre and volume integration over TIN
5. Grading design tools: single slope / dual slope / target elevation
6. Export reports: CSV / DXF / LandXML / PDF

> Clean-room implementation; no proprietary APK source or assets are copied.