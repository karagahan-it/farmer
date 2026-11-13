# TopoGrade / Farmer

Tablet-first Android app for professional RTK surveying, terrain modeling and grader guidance workflows.

## Current development — v0.12
- Full-screen landscape Operator Guidance
- Tablet IMU Heading / Roll / Pitch
- Heading source selection: Tablet IMU / receiver HDT (dual-GNSS) / Manual
- NMEA HDT parser for dual-antenna receiver heading
- Heading offset and Roll/Pitch zero calibration
- Configurable antenna-to-blade geometry
- Blade center / left / right elevation model
- Actual vs target cross-slope
- Guidance Quality Gate using RTK FIX, satellites, correction age, GST sigma and PDOP
- Guidance states: GOOD / CAUTION / BLOCKED
- Haptic warning on degraded/blocked guidance
- Persistent Heading/IMU configuration per project
- Guidance logging at 1 Hz
- Replay path on MapLibre with timeline slider
- Existing RTK, NTRIP/RTCM, Cut/Fill, profiles, boundary volume and offline maps remain active

## Safety boundary
This release is guidance-only. If GNSS quality is blocked, numerical guidance is intentionally suppressed. No hydraulic/CAN/valve actuation is implemented. Machine control requires an independent safety architecture, interlocks, watchdogs, manual override and controlled field validation.

## Next
1. Separate secondary GNSS transport for true dual-receiver baseline heading
2. IMU mounting-axis calibration and filtering
3. Guidance quality thresholds configurable per machine/job
4. Audible alarm patterns and operator acknowledgement
5. Replay color-coded by quality and cut/fill state
6. Blade articulation / circle rotation model
7. Machine presets and calibration reports
8. Field validation accuracy dashboard

> Clean-room implementation; no proprietary APK source or assets are copied.