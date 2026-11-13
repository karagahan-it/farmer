# TopoGrade / Farmer

Tablet-first Android app for professional RTK surveying, terrain modeling and grader guidance workflows.

## Current development — v0.11
- Full-screen landscape Operator Guidance mode
- Tablet Rotation Vector IMU integration
- Live Heading / Roll / Pitch
- Configurable antenna-to-blade geometry
- Blade center / left / right elevation model
- Actual blade cross-slope
- Target cross-slope from grading surface
- Left/right blade delta vs design
- CUT / ON GRADE / FILL live guidance
- Existing blade Z / Target Z / ΔZ / tolerance
- Persistent per-project Machine Geometry
- Guidance session logging at 1 Hz
- Persistent Guidance logs (up to 10,000 frames per project)
- Replay slider for recorded guidance frames
- Guidance CSV log codec
- Existing RTK, NTRIP/RTCM, Offline Maps, Cut/Fill, profiles and boundary volumes remain active

## Safety boundary
This release is guidance-only. It does not command grader hydraulics, valves, CAN actuators or blade movement. Future control work requires an independent safety architecture, interlocks, watchdogs, manual override, fault-state design and controlled field validation.

## Next
1. Heading source selection: IMU vs dual-GNSS heading
2. IMU calibration / zeroing and mounting orientation
3. Blade lift/rotation articulation model
4. Guidance quality gates based on RTK FIX, GST sigma and correction age
5. Full field replay on MapLibre with timeline
6. Cross-slope / elevation alarms and configurable tolerance bands
7. Machine profile presets
8. Field validation logging and accuracy reports

> Clean-room implementation; no proprietary APK source or assets are copied.