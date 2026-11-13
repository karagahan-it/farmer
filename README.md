# TopoGrade / Farmer

Tablet-first Android app for professional RTK surveying, terrain modeling and grader guidance workflows.

## Current development — v0.13
- Independent Secondary GNSS controller over TCP
- True dual-receiver baseline heading: Primary=rear / Secondary=front
- Receiver HDT heading remains preferred when available
- Tablet IMU mounting calibration relative to machine axes
- Heading / Roll / Pitch mounting offsets
- Configurable Guidance Quality thresholds per project
- Machine Preset persistence and apply workflow
- Quality-aware Guidance logging
- Replay colors: Cut / Fill / Neutral plus Quality Caution / Blocked
- Guidance log backwards compatibility for older sessions
- Previous full-screen Guidance, RTK/NTRIP, Cut/Fill, profiles, offline maps and boundary volumes remain active

## Quality / safety behavior
Guidance stays non-actuating. RTK quality gates can block numerical guidance and logging. Thresholds are configurable, but changing them does not certify the system for machine control. Field validation is required before relying on guidance for production grading.

## Next
1. Secondary GNSS Bluetooth transport and receiver role setup
2. Baseline length / heading quality checks for dual GNSS
3. Sensor fusion filtering for IMU + GNSS heading
4. Machine preset library/import/export
5. Audible alert patterns and acknowledgement
6. Replay analytics: time on-grade, cut/fill distribution and quality uptime
7. Blade articulation / circle rotation model
8. Field validation report and calibration wizard

> Clean-room implementation; no proprietary APK source or assets are copied.