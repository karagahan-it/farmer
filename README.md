# TopoGrade / Farmer

Tablet-first Android app for professional RTK surveying, terrain modeling and grading workflows.

## Current development — v0.9
- Tablet-first RTK field workspace
- TCP + Bluetooth GNSS, NMEA, NTRIP/RTCM
- RTK FIX capture + 5-second averaging
- GGA/GSA/GST quality metrics
- Track / Boundary persistence + MapLibre overlays
- Offline Map Packs: download/list/pause/resume/delete
- WGS84 ↔ UTM + Local calibration
- Antenna/pole height and geoid settings
- Multi-point least-squares calibration engine
- Grading modes: Target Elevation / Single Slope / Dual Slope
- Azimuth-aware dual-slope surface
- Cut/Fill classification heatmap
- Exact mixed-triangle Cut/Fill volume splitting across zero surface
- Cut volume / Fill volume / Net volume
- Persistent professional Grading Preset
- Engineering Cut/Fill CSV export
- Initial DXF point export engine
- Unit tests + Android CI

## Next
1. Render Cut/Fill heatmap directly on MapLibre
2. TIN-based colored triangle surface instead of point-only heatmap
3. Boundary-clipped earthwork volume
4. LandXML / DXF surface export
5. Cross-section and profile tools
6. Grader guidance screen: target vs blade elevation, cross-slope and tolerance bands
7. Field-safe guidance validation before any machine actuation

> Clean-room implementation; no proprietary APK source or assets are copied.