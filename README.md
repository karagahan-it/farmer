# TopoGrade / Farmer

Tablet-first Android app for RTK surveying, terrain modeling and grader guidance.

## Current development — v0.16
- Primary and secondary GNSS sample timestamps based on Android monotonic clock
- GNSS freshness checks, RTK fix validation, coordinate sanity checks and quality warnings
- Tablet IMU availability, registration, accuracy and sample-age monitoring
- Rotation Vector preferred; Game Rotation Vector fallback is marked as relative heading
- Stationary roll/pitch calibration from 60 samples with a stability threshold
- Generic sensor-channel validation for range, freshness, accuracy and rate-of-change
- Two-reference-point linear sensor calibration engine
- Machine sensor health adapter for industrial IMU, blade position, hydraulic pressure, wheel speed, steering angle and safety controller
- Farmer-readable sensor health panel with ready / caution / blocked / disconnected / not-installed states
- Safety supervisor for survey, operator guidance, auto-level and autonomous modes
- Auto-level readiness requires machine-grade IMU, dual GNSS, blade position, hydraulic pressure, wheel speed, safety controller, verified interlocks and an installed actuator interface
- Autonomous readiness additionally requires steering angle and a verified geofence
- Emergency-stop and manual-override states always inhibit automatic modes
- Existing v0.15 baseline calibration, GNSS time sync, reports and preset library remain active

## Sensor integration boundary
The currently integrated motion sensor is the Android tablet IMU. It is useful for early UI and guidance validation, but it is not accepted as the machine-grade IMU required for automatic machine control.

Blade-position, hydraulic-pressure, wheel-speed, steering-angle, industrial-IMU and safety-controller drivers are not connected yet. Their operating ranges, rates and calibration points must come from the exact sensor/controller datasheets and bench measurements; no generic hardware limits are assumed.

## Safety boundary
This release is diagnostic and guidance-only. The safety supervisor can deny readiness, but no hydraulic/CAN/valve/steering actuation is implemented. The Android tablet is not a safety controller. Real machine automation requires an independent machine controller, hardwired emergency-stop circuit, manual override, watchdog, validated interlocks and field safety validation.

## Next
1. Confirm exact GNSS receiver models and NMEA/RTCM output profiles
2. Confirm industrial IMU, blade-position sensor, hydraulic pressure transducer and wheel-speed sensor models
3. Add hardware-specific drivers and calibration profiles
4. Pair primary/secondary GNSS samples by UTC measurement epoch
5. Add machine-controller heartbeat and hardwired interlock diagnostics
6. Bench-test sensor faults, disconnects, stale data and implausible jumps
7. Run stationary and controlled low-speed field validation before any actuator integration

> Clean-room implementation; no proprietary APK source or assets are copied.
