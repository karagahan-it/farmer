# TopoGrade / Farmer

Tablet-first Android app for professional RTK surveying, terrain modeling and grader guidance workflows.

## Current development — v0.10
- Tablet-first RTK field workspace
- TCP + Bluetooth GNSS, NMEA, NTRIP/RTCM
- RTK FIX capture + 5-second averaging
- GGA/GSA/GST quality metrics
- Track / Boundary persistence + MapLibre overlays
- Offline Map Packs
- WGS84 ↔ UTM + Local calibration
- Antenna/pole height and geoid settings
- Target Elevation / Single Slope / Dual Slope grading surfaces
- Cut/Fill heatmap rendered directly on MapLibre
- Boundary-clipped Cut/Fill volume integration
- Mixed Cut/Fill triangle zero-surface splitting
- Longitudinal profile from TIN
- Automatic 20 m cross-section at profile midpoint
- Live grader Guidance panel: CUT / ON GRADE / FILL
- Live existing Z / target Z / ΔZ / tolerance
- Engineering CSV + initial DXF export
- Unit tests + green Android CI baseline

## Safety boundary
This version provides surveying and operator guidance only. It does not actuate grader hydraulics or blade controls. Any future machine control must use independent interlocks, watchdogs, calibration, manual override and controlled field validation.

## Next
1. Operator Guidance full-screen landscape mode
2. Blade geometry and antenna-to-blade offsets
3. Heading / IMU / roll / pitch integration
4. Cross-slope target vs actual
5. Boundary-clipped colored TIN triangles on MapLibre
6. Manual profile/cross-section line selection
7. LandXML / DXF surface export and job reports
8. Field replay/logging before machine-control research

> Clean-room implementation; no proprietary APK source or assets are copied.