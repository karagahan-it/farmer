# Architecture

## Layers
1. Compose UI
2. Project/Survey domain
3. Geometry engine (TIN/contours/cut-fill)
4. Offline persistence
5. GNSS/RTK adapters
6. MapLibre offline map
7. Optional sync/backend

## Safety boundary
Survey and guidance are separated from future machine actuation. Direct grader control requires independent safety interlocks, calibration, watchdogs and field validation.
