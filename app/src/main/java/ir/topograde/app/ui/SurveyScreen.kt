package ir.topograde.app.ui

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import ir.topograde.app.data.CsvCodec
import ir.topograde.app.data.ProjectStore
import ir.topograde.app.geo.CalibrationControl
import ir.topograde.app.geo.CoordinateConverter
import ir.topograde.app.geo.LocalCalibration
import ir.topograde.app.geo.LocalSiteCalibration
import ir.topograde.app.geometry.FieldMetrics
import ir.topograde.app.geometry.TerrainEngine
import ir.topograde.app.gnss.BluetoothGnssTransport
import ir.topograde.app.gnss.FixType
import ir.topograde.app.gnss.GnssConnectionState
import ir.topograde.app.gnss.GnssFix
import ir.topograde.app.gnss.GnssQuality
import ir.topograde.app.gnss.GnssSessionController
import ir.topograde.app.gnss.RtkAverager
import ir.topograde.app.gnss.TcpGnssTransport
import ir.topograde.app.map.MapLatLon
import ir.topograde.app.map.MapLibrePanel
import ir.topograde.app.map.OfflineMapManager
import ir.topograde.app.model.CoordinateSystem
import ir.topograde.app.model.Project
import ir.topograde.app.model.SurveyPoint
import ir.topograde.app.ntrip.NtripClient
import ir.topograde.app.ntrip.NtripConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyScreen(store: ProjectStore, project: Project, onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val controller = remember(scope) { GnssSessionController(scope) }
    val connectionState by controller.state.collectAsState()
    val gnssFix by controller.fix.collectAsState()
    val gnssQuality by controller.quality.collectAsState()
    val offlineManager = remember(context) { OfflineMapManager(context.applicationContext) }
    var points by remember(project.id) { mutableStateOf(store.getPoints(project.id)) }
    var track by remember(project.id) { mutableStateOf(store.getTrack(project.id)) }
    var boundary by remember(project.id) { mutableStateOf(store.getBoundary(project.id)) }
    var localCalibration by remember(project.id) { mutableStateOf(store.getLocalCalibration(project.id)) }
    var tracking by remember { mutableStateOf(false) }
    var lastTrackAt by remember { mutableLongStateOf(0L) }
    var averaging by remember { mutableStateOf(false) }
    var addDialog by remember { mutableStateOf(false) }
    var editPoint by remember { mutableStateOf<SurveyPoint?>(null) }
    var importDialog by remember { mutableStateOf(false) }
    var exportDialog by remember { mutableStateOf(false) }
    var tcpDialog by remember { mutableStateOf(false) }
    var bluetoothDialog by remember { mutableStateOf(false) }
    var ntripDialog by remember { mutableStateOf(false) }
    var calibrationDialog by remember { mutableStateOf(false) }
    var ntripStatus by remember { mutableStateOf("خاموش") }
    var offlineStatus by remember { mutableStateOf("دانلود نشده") }
    var fieldMessage by remember { mutableStateOf<String?>(null) }
    var ntripJob by remember { mutableStateOf<Job?>(null) }
    var ntripClient by remember { mutableStateOf<NtripClient?>(null) }
    val snackbar = remember { SnackbarHostState() }

    fun fixToPoint(fix: GnssFix, name: String): SurveyPoint? {
        val z = fix.altitude ?: return null
        return when (project.coordinateSystem) {
            CoordinateSystem.WGS84 -> SurveyPoint(System.currentTimeMillis(), name, fix.longitude, fix.latitude, z)
            CoordinateSystem.UTM -> {
                val zone = project.utmZone ?: return null
                val u = runCatching { CoordinateConverter.wgs84ToUtm(fix.latitude, fix.longitude, zone) }.getOrNull() ?: return null
                SurveyPoint(System.currentTimeMillis(), name, u.easting, u.northing, z)
            }
            CoordinateSystem.LOCAL -> {
                val cal = localCalibration ?: return null
                val u = runCatching { CoordinateConverter.wgs84ToUtm(fix.latitude, fix.longitude, cal.utmZone) }.getOrNull() ?: return null
                val xy = cal.transform.apply(u.easting, u.northing)
                SurveyPoint(System.currentTimeMillis(), name, xy.first, xy.second, z)
            }
        }
    }

    fun pointToMap(point: SurveyPoint): MapLatLon? {
        return when (project.coordinateSystem) {
            CoordinateSystem.WGS84 -> MapLatLon(point.y, point.x)
            CoordinateSystem.UTM -> {
                val zone = project.utmZone ?: return null
                val w = runCatching { CoordinateConverter.utmToWgs84(point.x, point.y, zone, project.northernHemisphere) }.getOrNull() ?: return null
                MapLatLon(w.latitude, w.longitude)
            }
            CoordinateSystem.LOCAL -> {
                val cal = localCalibration ?: return null
                val source = cal.transform.inverse(point.x, point.y)
                val w = runCatching { CoordinateConverter.utmToWgs84(source.first, source.second, cal.utmZone, cal.northernHemisphere) }.getOrNull() ?: return null
                MapLatLon(w.latitude, w.longitude)
            }
        }
    }

    val mapTrack = remember(track, localCalibration, project) { track.mapNotNull { pointToMap(it) } }
    val mapBoundary = remember(boundary, localCalibration, project) { boundary.mapNotNull { pointToMap(it) } }

    LaunchedEffect(fieldMessage) {
        fieldMessage?.let { snackbar.showSnackbar(it); fieldMessage = null }
    }
    LaunchedEffect(gnssFix, tracking) {
        val fix = gnssFix
        if (tracking && fix?.fixType == FixType.RTK_FIXED && System.currentTimeMillis() - lastTrackAt >= 1000L) {
            fixToPoint(fix, "TRK-" + (track.size + 1))?.let { p ->
                store.appendTrackPoint(project.id, p)
                track = store.getTrack(project.id)
                lastTrackAt = System.currentTimeMillis()
            }
        }
    }
    DisposableEffect(project.id) {
        onDispose {
            controller.stop()
            ntripJob?.cancel()
            ntripClient?.close()
        }
    }

    val bluetoothPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) bluetoothDialog = true else fieldMessage = "مجوز اتصال Bluetooth داده نشد"
    }

    fun openBluetoothPicker() {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        if (manager?.adapter == null) {
            fieldMessage = "Bluetooth روی این تبلت در دسترس نیست"
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            bluetoothPermission.launch(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            bluetoothDialog = true
        }
    }

    fun saveImmediateFix() {
        val fix = gnssFix ?: run { fieldMessage = "موقعیت GNSS موجود نیست"; return }
        if (fix.fixType != FixType.RTK_FIXED) { fieldMessage = "ثبت دقیق فقط در وضعیت RTK FIX فعال است"; return }
        val point = fixToPoint(fix, "RTK-" + (points.size + 1)) ?: run { fieldMessage = "CRS/کالیبراسیون پروژه آماده نیست"; return }
        store.addPoint(project.id, point)
        points = store.getPoints(project.id)
        fieldMessage = "نقطه " + point.name + " ثبت شد"
    }

    fun saveAveragedFix() {
        scope.launch {
            averaging = true
            runCatching { RtkAverager.average(controller.samples, 5000L, 5) }
                .onSuccess { result ->
                    val point = fixToPoint(result.fix, "AVG-" + (points.size + 1))
                    if (point != null) {
                        store.addPoint(project.id, point)
                        points = store.getPoints(project.id)
                        fieldMessage = "AVG: " + result.sampleCount + " نمونه | H=" + "%.3f".format(result.horizontalSpreadM) + "m | V=" + "%.3f".format(result.verticalSpreadM) + "m"
                    } else {
                        fieldMessage = "CRS/کالیبراسیون پروژه آماده نیست"
                    }
                }
                .onFailure { fieldMessage = "Averaging ناموفق: " + (it.message ?: "نمونه کافی نیست") }
            averaging = false
        }
    }

    fun addBoundaryVertex() {
        val fix = gnssFix ?: run { fieldMessage = "موقعیت GNSS موجود نیست"; return }
        if (fix.fixType != FixType.RTK_FIXED) { fieldMessage = "برای مرز RTK FIX لازم است"; return }
        val p = fixToPoint(fix, "BND-" + (boundary.size + 1)) ?: run { fieldMessage = "CRS/کالیبراسیون پروژه آماده نیست"; return }
        store.appendBoundaryPoint(project.id, p)
        boundary = store.getBoundary(project.id)
        fieldMessage = "رأس مرز اضافه شد"
    }

    fun downloadOfflineMap() {
        val fix = gnssFix ?: run { fieldMessage = "برای دانلود نقشه ابتدا موقعیت GNSS لازم است"; return }
        offlineStatus = "در حال شروع دانلود…"
        offlineManager.downloadAround(
            name = project.name + "-2km",
            latitude = fix.latitude,
            longitude = fix.longitude,
            radiusKm = 2.0,
            onProgress = { p -> offlineStatus = if (p.complete) "کامل شد | " + p.completedResources + " resource" else "دانلود " + p.percent + "% | " + p.completedResources + "/" + p.requiredResources },
            onError = { offlineStatus = "خطا: " + it }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(project.name + " | " + project.coordinateSystem.title) },
                navigationIcon = { TextButton(onClick = onBack) { Text("بازگشت") } }
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = { addDialog = true }) { Text("+") } }
    ) { pad ->
        LazyColumn(
            Modifier.padding(pad).fillMaxSize(),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = { tcpDialog = true }, label = { Text("TCP GNSS") })
                    AssistChip(onClick = { openBluetoothPicker() }, label = { Text("Bluetooth GNSS") })
                    AssistChip(onClick = { ntripDialog = true }, label = { Text("NTRIP / RTCM") })
                    AssistChip(onClick = { tracking = !tracking }, label = { Text(if (tracking) "Stop Track" else "Start Track") })
                    AssistChip(onClick = { addBoundaryVertex() }, label = { Text("+ Boundary") })
                    AssistChip(onClick = { downloadOfflineMap() }, label = { Text("Offline Map 2km") })
                    if (project.coordinateSystem == CoordinateSystem.LOCAL) AssistChip(onClick = { calibrationDialog = true }, label = { Text("Local Calibration") })
                    AssistChip(onClick = { importDialog = true }, label = { Text("Import CSV") })
                    AssistChip(onClick = { exportDialog = true }, label = { Text("Export CSV") })
                    if (connectionState !is GnssConnectionState.Disconnected) AssistChip(onClick = { controller.stop() }, label = { Text("قطع گیرنده") })
                }
            }
            item {
                TabletFieldWorkspace(
                    points = points,
                    track = track,
                    boundary = boundary,
                    mapTrack = mapTrack,
                    mapBoundary = mapBoundary,
                    fix = gnssFix,
                    quality = gnssQuality,
                    state = connectionState,
                    ntripStatus = ntripStatus,
                    offlineStatus = offlineStatus,
                    averaging = averaging,
                    onSave = ::saveImmediateFix,
                    onAverage = ::saveAveragedFix,
                    saveEnabled = project.coordinateSystem != CoordinateSystem.LOCAL || localCalibration != null,
                    crs = project.coordinateSystem
                )
            }
            item {
                FieldGeometryCard(
                    track = track,
                    boundary = boundary,
                    crs = project.coordinateSystem,
                    onClearTrack = { store.clearTrack(project.id); track = emptyList() },
                    onClearBoundary = { store.clearBoundary(project.id); boundary = emptyList() }
                )
            }
            item { Text("نقاط برداشت (" + points.size + ")", style = MaterialTheme.typography.titleLarge) }
            items(points, key = { it.id }) { p ->
                PointRow(
                    p,
                    onEdit = { editPoint = p },
                    onDelete = { store.deletePoint(project.id, p.id); points = store.getPoints(project.id) }
                )
            }
        }
    }

    if (addDialog) PointDialog(null, points.size + 1, { addDialog = false }) { p ->
        store.addPoint(project.id, p)
        points = store.getPoints(project.id)
        addDialog = false
    }
    editPoint?.let { current ->
        PointDialog(current, points.size + 1, { editPoint = null }) { p ->
            store.updatePoint(project.id, p)
            points = store.getPoints(project.id)
            editPoint = null
        }
    }
    if (importDialog) CsvImportDialog({ importDialog = false }) { csv ->
        store.appendPoints(project.id, CsvCodec.parse(csv))
        points = store.getPoints(project.id)
        importDialog = false
    }
    if (exportDialog) CsvExportDialog(CsvCodec.export(points)) { exportDialog = false }
    if (tcpDialog) TcpGnssDialog({ tcpDialog = false }) { host, port ->
        tcpDialog = false
        controller.start { TcpGnssTransport(host, port) }
    }
    if (bluetoothDialog) {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        BluetoothPickerDialog(manager?.adapter, { bluetoothDialog = false }) { device ->
            bluetoothDialog = false
            val adapter = manager?.adapter
            if (adapter == null) fieldMessage = "Bluetooth adapter در دسترس نیست"
            else controller.start { BluetoothGnssTransport(adapter, device) }
        }
    }
    if (ntripDialog) NtripDialog({ ntripDialog = false }) { config ->
        ntripDialog = false
        ntripJob?.cancel()
        ntripClient?.close()
        val client = NtripClient(config)
        ntripClient = client
        ntripJob = scope.launch {
            ntripStatus = "در حال اتصال…"
            runCatching { client.connect { bytes -> controller.write(bytes) } }
                .onSuccess { ntripStatus = "قطع شد" }
                .onFailure { ntripStatus = "خطا: " + (it.message ?: "NTRIP") }
        }
    }
    if (calibrationDialog) {
        LocalCalibrationDialog(
            onDismiss = { calibrationDialog = false },
            onSave = { calibration ->
                store.saveLocalCalibration(project.id, calibration)
                localCalibration = calibration
                calibrationDialog = false
                fieldMessage = "کالیبراسیون Local ذخیره شد"
            }
        )
    }
}

@Composable
private fun TabletFieldWorkspace(
    points: List<SurveyPoint>,
    track: List<SurveyPoint>,
    boundary: List<SurveyPoint>,
    mapTrack: List<MapLatLon>,
    mapBoundary: List<MapLatLon>,
    fix: GnssFix?,
    quality: GnssQuality,
    state: GnssConnectionState,
    ntripStatus: String,
    offlineStatus: String,
    averaging: Boolean,
    onSave: () -> Unit,
    onAverage: () -> Unit,
    saveEnabled: Boolean,
    crs: CoordinateSystem
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val tablet = maxWidth >= 720.dp
        if (tablet) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(Modifier.weight(1.7f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MapLibrePanel(fix?.latitude, fix?.longitude, mapTrack, mapBoundary)
                    PointMap(points, track, boundary)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ConnectionCard(state)
                    NtripStatusCard(ntripStatus)
                    OfflineStatusCard(offlineStatus)
                    GnssStatusCard(fix, quality, averaging, onSave, onAverage, saveEnabled)
                    SurveySummary(points)
                    FieldMetricsCard(track, boundary, crs)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ConnectionCard(state)
                NtripStatusCard(ntripStatus)
                OfflineStatusCard(offlineStatus)
                GnssStatusCard(fix, quality, averaging, onSave, onAverage, saveEnabled)
                MapLibrePanel(fix?.latitude, fix?.longitude, mapTrack, mapBoundary)
                SurveySummary(points)
                FieldMetricsCard(track, boundary, crs)
                PointMap(points, track, boundary)
            }
        }
    }
}

@Composable
private fun ConnectionCard(state: GnssConnectionState) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("گیرنده GNSS", style = MaterialTheme.typography.titleMedium)
            val text = when (state) {
                GnssConnectionState.Disconnected -> "قطع"
                GnssConnectionState.Connecting -> "در حال اتصال…"
                is GnssConnectionState.Connected -> "متصل: " + state.description
                is GnssConnectionState.Error -> "خطا / reconnect: " + state.message
            }
            Text(text)
        }
    }
}

@Composable
private fun NtripStatusCard(status: String) {
    ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("NTRIP / RTCM", style = MaterialTheme.typography.titleMedium); Text(status) } }
}

@Composable
private fun OfflineStatusCard(status: String) {
    ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("Offline Map", style = MaterialTheme.typography.titleMedium); Text(status) } }
}

@Composable
private fun GnssStatusCard(fix: GnssFix?, quality: GnssQuality, averaging: Boolean, onSave: () -> Unit, onAverage: () -> Unit, saveEnabled: Boolean) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("GNSS / RTK", style = MaterialTheme.typography.titleLarge)
            if (fix == null) {
                Text("هنوز موقعیت دریافت نشده")
            } else {
                Text("وضعیت: " + fix.fixType.label, style = MaterialTheme.typography.titleMedium)
                Text("Sat: " + fix.satellites + " | GGA HDOP: " + (fix.hdop ?: "-"))
                Text("PDOP: " + fmt(quality.pdop) + " | HDOP: " + fmt(quality.hdop) + " | VDOP: " + fmt(quality.vdop))
                Text("σH: " + fmtMeters(quality.horizontalSigmaMeters) + " | σV: " + fmtMeters(quality.verticalSigmaMeters) + " | RMS: " + fmtMeters(quality.rmsMeters))
                Text("Age correction: " + (fix.correctionAgeSeconds?.let { "%.1f s".format(it) } ?: "-") + " | Base: " + (fix.baseStationId ?: "-"))
                Text("Lat: " + "%.7f".format(fix.latitude))
                Text("Lon: " + "%.7f".format(fix.longitude))
                Text("Alt: " + (fix.altitude?.let { "%.3f m".format(it) } ?: "-"))
                Button(
                    onClick = onSave,
                    enabled = saveEnabled && fix.fixType == FixType.RTK_FIXED && fix.altitude != null,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp)
                ) { Text("ثبت فوری RTK FIX") }
                OutlinedButton(
                    onClick = onAverage,
                    enabled = !averaging && saveEnabled && fix.fixType == FixType.RTK_FIXED,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp)
                ) { Text(if (averaging) "در حال میانگین‌گیری…" else "RTK Average 5s") }
            }
        }
    }
}

@Composable
private fun FieldMetricsCard(track: List<SurveyPoint>, boundary: List<SurveyPoint>, crs: CoordinateSystem) {
    val length = remember(track, crs) { FieldMetrics.trackLengthMeters(track, crs) }
    val area = remember(boundary, crs) { FieldMetrics.boundaryAreaSquareMeters(boundary, crs) }
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("اندازه‌گیری میدانی", style = MaterialTheme.typography.titleMedium)
            Text("طول Track: " + "%.2f m".format(length))
            Text("مساحت Boundary: " + "%.2f m²".format(area))
            if (area >= 10000.0) Text("مساحت: " + "%.3f ha".format(area / 10000.0))
        }
    }
}

@Composable
private fun FieldGeometryCard(track: List<SurveyPoint>, boundary: List<SurveyPoint>, crs: CoordinateSystem, onClearTrack: () -> Unit, onClearBoundary: () -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Track / Boundary", style = MaterialTheme.typography.titleMedium)
            Text("Track points: " + track.size + " | Boundary vertices: " + boundary.size + " | CRS: " + crs.name)
            Row {
                TextButton(onClick = onClearTrack, enabled = track.isNotEmpty()) { Text("پاک کردن Track") }
                TextButton(onClick = onClearBoundary, enabled = boundary.isNotEmpty()) { Text("پاک کردن Boundary") }
            }
        }
    }
}

@Composable
private fun SurveySummary(points: List<SurveyPoint>) {
    val minZ = points.minOfOrNull { it.z }
    val maxZ = points.maxOfOrNull { it.z }
    val avg = if (points.isEmpty()) null else points.map { it.z }.average()
    val tris = remember(points) { TerrainEngine.triangulate(points) }
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("خلاصه برداشت", style = MaterialTheme.typography.titleMedium)
            Text("نقاط: " + points.size + " | TIN: " + tris.size)
            Text("Z min: " + (minZ?.let { "%.3f".format(it) } ?: "-") + " | max: " + (maxZ?.let { "%.3f".format(it) } ?: "-") + " | avg: " + (avg?.let { "%.3f".format(it) } ?: "-"))
        }
    }
}

@Composable
private fun PointMap(points: List<SurveyPoint>, track: List<SurveyPoint>, boundary: List<SurveyPoint>) {
    val all = points + track + boundary
    val tris = remember(points) { TerrainEngine.triangulate(points) }
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("TIN / Track / Boundary", style = MaterialTheme.typography.titleMedium)
            if (all.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) { Text("داده‌ای ثبت نشده") }
            } else {
                Canvas(Modifier.fillMaxWidth().height(300.dp)) {
                    val minX = all.minOf { it.x }; val maxX = all.maxOf { it.x }
                    val minY = all.minOf { it.y }; val maxY = all.maxOf { it.y }
                    val dx = max(maxX - minX, 1.0); val dy = max(maxY - minY, 1.0)
                    val p = 20f; val w = size.width - 2 * p; val h = size.height - 2 * p
                    fun xy(v: SurveyPoint) = Offset(p + ((v.x - minX) / dx).toFloat() * w, p + h - ((v.y - minY) / dy).toFloat() * h)
                    tris.forEach { t ->
                        val a = xy(t.a); val b = xy(t.b); val c = xy(t.c)
                        drawLine(Color.Gray, a, b, 1.5f); drawLine(Color.Gray, b, c, 1.5f); drawLine(Color.Gray, c, a, 1.5f)
                    }
                    track.zipWithNext().forEach { (a, b) -> drawLine(Color(0xFF7E57C2), xy(a), xy(b), 4f) }
                    boundary.zipWithNext().forEach { (a, b) -> drawLine(Color(0xFFE65100), xy(a), xy(b), 4f) }
                    if (boundary.size > 2) drawLine(Color(0xFFE65100), xy(boundary.last()), xy(boundary.first()), 4f)
                    points.forEach { drawCircle(Color(0xFF1565C0), 7f, xy(it)) }
                }
            }
        }
    }
}

@Composable
private fun PointRow(p: SurveyPoint, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(p.name, style = MaterialTheme.typography.titleMedium)
                Text("Z " + "%.3f".format(p.z))
            }
            Text("X " + "%.3f".format(p.x) + " | Y " + "%.3f".format(p.y))
            Row {
                TextButton(onClick = onEdit) { Text("ویرایش") }
                TextButton(onClick = onDelete) { Text("حذف") }
            }
        }
    }
}

@Composable
private fun PointDialog(existing: SurveyPoint?, pointNumber: Int, onDismiss: () -> Unit, onSave: (SurveyPoint) -> Unit) {
    var name by remember { mutableStateOf(existing?.name ?: "P$pointNumber") }
    var x by remember { mutableStateOf(existing?.x?.toString() ?: "") }
    var y by remember { mutableStateOf(existing?.y?.toString() ?: "") }
    var z by remember { mutableStateOf(existing?.z?.toString() ?: "") }
    val xv = x.toDoubleOrNull(); val yv = y.toDoubleOrNull(); val zv = z.toDoubleOrNull()
    val valid = name.isNotBlank() && xv != null && yv != null && zv != null
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "افزودن نقطه" else "ویرایش نقطه") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("نام") })
                OutlinedTextField(x, { x = it }, label = { Text("X / Easting") })
                OutlinedTextField(y, { y = it }, label = { Text("Y / Northing") })
                OutlinedTextField(z, { z = it }, label = { Text("Z / Elevation") })
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = { onSave(SurveyPoint(existing?.id ?: System.currentTimeMillis(), name.trim(), xv!!, yv!!, zv!!)) }
            ) { Text("ذخیره") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

@Composable
private fun CsvImportDialog(onDismiss: () -> Unit, onImport: (String) -> Unit) {
    var csv by remember { mutableStateOf("name,x,y,z\n") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import CSV") },
        text = { OutlinedTextField(csv, { csv = it }, modifier = Modifier.fillMaxWidth().height(220.dp)) },
        confirmButton = { TextButton(onClick = { onImport(csv) }) { Text("ورود") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

@Composable
private fun CsvExportDialog(csv: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export CSV") },
        text = { OutlinedTextField(csv, {}, readOnly = true, modifier = Modifier.fillMaxWidth().height(220.dp)) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("بستن") } }
    )
}

@Composable
private fun TcpGnssDialog(onDismiss: () -> Unit, onConnect: (String, Int) -> Unit) {
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("9001") }
    val pv = port.toIntOrNull()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("TCP GNSS") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(host, { host = it.trim() }, label = { Text("IP / Host") })
                OutlinedTextField(port, { port = it.filter(Char::isDigit).take(5) }, label = { Text("Port") })
            }
        },
        confirmButton = {
            TextButton(enabled = host.isNotBlank() && pv != null && pv in 1..65535, onClick = { onConnect(host, pv!!) }) { Text("اتصال") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

@SuppressLint("MissingPermission")
@Composable
private fun BluetoothPickerDialog(adapter: BluetoothAdapter?, onDismiss: () -> Unit, onSelect: (BluetoothDevice) -> Unit) {
    val devices = remember(adapter) { adapter?.bondedDevices?.sortedBy { it.name ?: it.address }.orEmpty() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("دستگاه‌های Pair شده") },
        text = {
            if (devices.isEmpty()) {
                Text("ابتدا گیرنده را از تنظیمات Android Pair کنید.")
            } else {
                LazyColumn(Modifier.heightIn(max = 360.dp)) {
                    items(devices, key = { it.address }) { d ->
                        ListItem(
                            headlineContent = { Text(d.name ?: "GNSS") },
                            supportingContent = { Text(d.address) },
                            trailingContent = { TextButton(onClick = { onSelect(d) }) { Text("اتصال") } }
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("بستن") } }
    )
}

@Composable
private fun NtripDialog(onDismiss: () -> Unit, onConnect: (NtripConfig) -> Unit) {
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("2101") }
    var mount by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    val pv = port.toIntOrNull()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("NTRIP / RTCM") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(host, { host = it.trim() }, label = { Text("Caster Host") })
                OutlinedTextField(port, { port = it.filter(Char::isDigit).take(5) }, label = { Text("Port") })
                OutlinedTextField(mount, { mount = it.trim() }, label = { Text("Mountpoint") })
                OutlinedTextField(user, { user = it }, label = { Text("Username") })
                OutlinedTextField(pass, { pass = it }, label = { Text("Password") })
            }
        },
        confirmButton = {
            TextButton(enabled = host.isNotBlank() && mount.isNotBlank() && pv != null && pv in 1..65535, onClick = { onConnect(NtripConfig(host, pv!!, mount, user, pass)) }) { Text("اتصال") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

@Composable
private fun LocalCalibrationDialog(onDismiss: () -> Unit, onSave: (LocalSiteCalibration) -> Unit) {
    var zone by remember { mutableStateOf("") }
    var north by remember { mutableStateOf(true) }
    var aE by remember { mutableStateOf("") }; var aN by remember { mutableStateOf("") }; var aX by remember { mutableStateOf("") }; var aY by remember { mutableStateOf("") }
    var bE by remember { mutableStateOf("") }; var bN by remember { mutableStateOf("") }; var bX by remember { mutableStateOf("") }; var bY by remember { mutableStateOf("") }
    val z = zone.toIntOrNull()
    val values = listOf(aE, aN, aX, aY, bE, bN, bX, bY).map { it.toDoubleOrNull() }
    val valid = z != null && z in 1..60 && values.all { it != null }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Local Site Calibration") },
        text = {
            Column(Modifier.heightIn(max = 520.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(zone, { zone = it.filter(Char::isDigit).take(2) }, label = { Text("UTM Zone") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = north, onClick = { north = true }, label = { Text("North") })
                    FilterChip(selected = !north, onClick = { north = false }, label = { Text("South") })
                }
                Text("Control A: UTM → Local")
                Row { OutlinedTextField(aE, { aE = it }, label = { Text("E") }, modifier = Modifier.weight(1f)); Spacer(Modifier.width(6.dp)); OutlinedTextField(aN, { aN = it }, label = { Text("N") }, modifier = Modifier.weight(1f)) }
                Row { OutlinedTextField(aX, { aX = it }, label = { Text("Local X") }, modifier = Modifier.weight(1f)); Spacer(Modifier.width(6.dp)); OutlinedTextField(aY, { aY = it }, label = { Text("Local Y") }, modifier = Modifier.weight(1f)) }
                Text("Control B: UTM → Local")
                Row { OutlinedTextField(bE, { bE = it }, label = { Text("E") }, modifier = Modifier.weight(1f)); Spacer(Modifier.width(6.dp)); OutlinedTextField(bN, { bN = it }, label = { Text("N") }, modifier = Modifier.weight(1f)) }
                Row { OutlinedTextField(bX, { bX = it }, label = { Text("Local X") }, modifier = Modifier.weight(1f)); Spacer(Modifier.width(6.dp)); OutlinedTextField(bY, { bY = it }, label = { Text("Local Y") }, modifier = Modifier.weight(1f)) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    val a = CalibrationControl(values[0]!!, values[1]!!, values[2]!!, values[3]!!)
                    val b = CalibrationControl(values[4]!!, values[5]!!, values[6]!!, values[7]!!)
                    val t = LocalCalibration.fromTwoControls(a, b)
                    onSave(LocalSiteCalibration(z!!, north, t))
                }
            ) { Text("ذخیره کالیبراسیون") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

private fun fmt(v: Double?): String = v?.let { "%.2f".format(it) } ?: "-"
private fun fmtMeters(v: Double?): String = v?.let { "%.3f m".format(it) } ?: "-"