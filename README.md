# TopoGrade / Farmer

Tablet-first Android app for professional RTK surveying, terrain modeling and grader guidance workflows.

## Current development — v0.15
- Nominal dual-GNSS baseline calibration and deviation tolerance
- Primary/secondary GNSS UTC time synchronization check
- Dual-GNSS heading accepted only when baseline and time sync are valid
- Orientation low-pass filtering for heading / roll / pitch
- Session Report from recorded Guidance logs
- Session duration, On-Grade/Cut/Fill, RTK quality uptime and grade-error metrics
- Session Report CSV export
- Machine Preset Library with save/apply/delete
- Global preset library persistence
- Preset Library CSV codec foundation
- Existing Secondary Bluetooth/TCP, sensor fusion, audible alerts, field analytics and replay remain active

## Safety boundary
This release is guidance-only. Dual-GNSS heading is rejected when time synchronization or calibrated baseline checks fail. No hydraulic/CAN/valve actuation is implemented.

## Next
1. Baseline calibration wizard using averaged stationary samples
2. GNSS sample pairing by UTC timestamp instead of latest-value pairing
3. Configurable orientation filter parameters
4. Session naming/start-stop boundaries instead of one rolling project log
5. Preset Library import/export UI
6. PDF field validation report
7. Alarm acknowledgement and mute controls
8. Field calibration workflow and acceptance criteria

> Clean-room implementation; no proprietary APK source or assets are copied.