# TopoGrade / Farmer

Tablet-first Android app for professional RTK surveying, terrain modeling and grader guidance workflows.

## Current development — v0.14
- Secondary GNSS over TCP or Bluetooth SPP
- Primary=rear / Secondary=front dual-receiver workflow
- Baseline length and quality validation
- Heading fusion between tablet IMU and GNSS heading
- Large heading innovation fallback to GNSS
- Configurable IMU mounting calibration
- Configurable project Quality Gate thresholds
- Persistent Machine Presets
- Audible operator warnings for blocked/caution guidance and large grade errors
- Field Performance dashboard
- On-Grade / Cut / Fill percentages
- RTK Good / Caution / Blocked uptime percentages
- Mean and maximum absolute grade error
- Colored MapLibre replay for Cut / Fill / Neutral / Quality Caution / Quality Blocked
- Existing surveying, NTRIP/RTCM, grading, profiles, earthwork volumes and offline maps remain active

## Safety boundary
This release is guidance-only. Audio/haptic/visual alerts supplement operator awareness but are not a safety-rated control system. No hydraulic, CAN, valve or blade actuation is implemented.

## Next
1. Baseline nominal-length calibration and deviation alarm
2. Timestamp synchronization between primary/secondary GNSS
3. Low-pass / complementary filtering for roll and pitch
4. Operator acknowledgement and alarm mute policy
5. Session reports with time-on-grade and quality uptime
6. Machine preset library import/export
7. Blade articulation / circle rotation model
8. Controlled field validation workflow

> Clean-room implementation; no proprietary APK source or assets are copied.