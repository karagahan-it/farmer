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
import ir.topograde.app.geo.CoordinateConverter
import ir.topograde.app.geometry.TerrainEngine
import ir.topograde.app.gnss.BluetoothGnssTransport
import ir.topograde.app.gnss.FixType
import ir.topograde.app.gnss.GnssConnectionState
import ir.topograde.app.gnss.GnssFix
import ir.topograde.app.gnss.GnssSessionController
import ir.topograde.app.gnss.TcpGnssTransport
import ir.topograde.app.map.MapLibrePanel
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
    var points by remember(project.id) { mutableStateOf(store.getPoints(project.id)) }
    var addDialog by remember { mutableStateOf(false) }
    var editPoint by remember { mutableStateOf<SurveyPoint?>(null) }
    var importDialog by remember { mutableStateOf(false) }
    var exportDialog by remember { mutableStateOf(false) }
    var tcpDialog by remember { mutableStateOf(false) }
    var bluetoothDialog by remember { mutableStateOf(false) }
    var ntripDialog by remember { mutableStateOf(false) }
    var ntripStatus by remember { mutableStateOf("خاموش") }
    var fieldMessage by remember { mutableStateOf<String?>(null) }
    var ntripJob by remember { mutableStateOf<Job?>(null) }
    var ntripClient by remember { mutableStateOf<NtripClient?>(null) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(fieldMessage) { fieldMessage?.let { snackbar.showSnackbar(it); fieldMessage = null } }

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
        if (manager?.adapter == null) { fieldMessage = "Bluetooth روی این تبلت در دسترس نیست"; return }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            bluetoothPermission.launch(Manifest.permission.BLUETOOTH_CONNECT)
        } else bluetoothDialog = true
    }

    fun saveRtkPoint() {
        val fix = gnssFix ?: run { fieldMessage = "موقعیت GNSS موجود نیست"; return }
        if (fix.fixType != FixType.RTK_FIXED) { fieldMessage = "ثبت دقیق فقط در وضعیت RTK FIX فعال است"; return }
        val z = fix.altitude ?: run { fieldMessage = "ارتفاع GNSS موجود نیست"; return }
        val point = when (project.coordinateSystem) {
            CoordinateSystem.WGS84 -> SurveyPoint(System.currentTimeMillis(), "RTK-" + (points.size + 1), fix.longitude, fix.latitude, z)
            CoordinateSystem.UTM -> {
                val zone = project.utmZone ?: run { fieldMessage = "UTM Zone پروژه مشخص نیست"; return }
                val utm = runCatching { CoordinateConverter.wgs84ToUtm(fix.latitude, fix.longitude, zone) }.getOrElse { fieldMessage = it.message ?: "خطای تبدیل UTM"; return }
                SurveyPoint(System.currentTimeMillis(), "RTK-" + (points.size + 1), utm.easting, utm.northing, z)
            }
            CoordinateSystem.LOCAL -> { fieldMessage = "برای پروژه Local ابتدا کالیبراسیون Local/GNSS لازم است"; return }
        }
        store.addPoint(project.id, point)
        points = store.getPoints(project.id)
        fieldMessage = "نقطه " + point.name + " از RTK FIX ثبت شد"
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { TopAppBar(title = { Text(project.name + " | " + project.coordinateSystem.title) }, navigationIcon = { TextButton(onClick = onBack) { Text("بازگشت") } }) },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = { addDialog = true }, text = { Text("+ نقطه دستی") }) }
    ) { pad ->
        LazyColumn(Modifier.padding(pad).fillMaxSize(), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = { tcpDialog = true }, label = { Text("TCP GNSS") })
                    AssistChip(onClick = { openBluetoothPicker() }, label = { Text("Bluetooth GNSS") })
                    AssistChip(onClick = { ntripDialog = true }, label = { Text("NTRIP / RTCM") })
                    AssistChip(onClick = { importDialog = true }, label = { Text("Import CSV") })
                    AssistChip(onClick = { exportDialog = true }, label = { Text("Export CSV") })
                    if (connectionState !is GnssConnectionState.Disconnected) AssistChip(onClick = { controller.stop() }, label = { Text("قطع گیرنده") })
                }
            }
            item { TabletFieldWorkspace(points, gnssFix, connectionState, ntripStatus, ::saveRtkPoint, project.coordinateSystem != CoordinateSystem.LOCAL) }
            item { Text("نقاط برداشت (" + points.size + ")", style = MaterialTheme.typography.titleLarge) }
            items(points, key = { it.id }) { p ->
                PointRow(p, onEdit = { editPoint = p }, onDelete = { store.deletePoint(project.id, p.id); points = store.getPoints(project.id) })
            }
        }
    }

    if (addDialog) PointDialog(null, points.size + 1, { addDialog = false }) { p -> store.addPoint(project.id, p); points = store.getPoints(project.id); addDialog = false }
    editPoint?.let { current -> PointDialog(current, points.size + 1, { editPoint = null }) { p -> store.updatePoint(project.id, p); points = store.getPoints(project.id); editPoint = null } }
    if (importDialog) CsvImportDialog({ importDialog = false }) { csv -> store.appendPoints(project.id, CsvCodec.parse(csv)); points = store.getPoints(project.id); importDialog = false }
    if (exportDialog) CsvExportDialog(CsvCodec.export(points)) { exportDialog = false }
    if (tcpDialog) TcpGnssDialog({ tcpDialog = false }) { host, port -> tcpDialog = false; controller.start { TcpGnssTransport(host, port) } }
    if (bluetoothDialog) {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        BluetoothPickerDialog(manager?.adapter, { bluetoothDialog = false }) { device ->
            bluetoothDialog = false
            val adapter = manager?.adapter
            if (adapter == null) fieldMessage = "Bluetooth adapter در دسترس نیست" else controller.start { BluetoothGnssTransport(adapter, device) }
        }
    }
    if (ntripDialog) NtripDialog({ ntripDialog = false }) { config ->
        ntripDialog = false
        ntripJob?.cancel(); ntripClient?.close()
        val client = NtripClient(config); ntripClient = client
        ntripJob = scope.launch {
            ntripStatus = "در حال اتصال…"
            runCatching { client.connect { bytes -> controller.write(bytes) } }
                .onSuccess { ntripStatus = "قطع شد" }
                .onFailure { ntripStatus = "خطا: " + (it.message ?: "NTRIP") }
        }
    }
}

@Composable private fun TabletFieldWorkspace(points: List<SurveyPoint>, fix: GnssFix?, state: GnssConnectionState, ntripStatus: String, onSaveRtk: () -> Unit, saveEnabledForCrs: Boolean) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val tablet = maxWidth >= 720.dp
        if (tablet) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(Modifier.weight(1.7f), verticalArrangement = Arrangement.spacedBy(12.dp)) { MapLibrePanel(fix?.latitude, fix?.longitude); PointMap(points) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) { ConnectionCard(state); NtripStatusCard(ntripStatus); GnssStatusCard(fix, onSaveRtk, saveEnabledForCrs); SurveySummary(points) }
        } else Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { ConnectionCard(state); NtripStatusCard(ntripStatus); GnssStatusCard(fix, onSaveRtk, saveEnabledForCrs); MapLibrePanel(fix?.latitude, fix?.longitude); SurveySummary(points); PointMap(points) }
    }
}

@Composable private fun ConnectionCard(state: GnssConnectionState) {
    ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
        Text("گیرنده GNSS", style = MaterialTheme.typography.titleMedium)
        Text(when (state) { GnssConnectionState.Disconnected -> "قطع"; GnssConnectionState.Connecting -> "در حال اتصال…"; is GnssConnectionState.Connected -> "متصل: " + state.description; is GnssConnectionState.Error -> "خطا / reconnect: " + state.message })
    } }
}

@Composable private fun NtripStatusCard(status: String) { ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("NTRIP / RTCM", style = MaterialTheme.typography.titleMedium); Text(status) } } }

@Composable private fun GnssStatusCard(fix: GnssFix?, onSaveRtk: () -> Unit, saveEnabledForCrs: Boolean) {
    ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text("GNSS / RTK", style = MaterialTheme.typography.titleLarge)
        if (fix == null) Text("هنوز موقعیت دریافت نشده") else {
            Text("وضعیت: " + fix.fixType.label, style = MaterialTheme.typography.titleMedium)
            Text("Sat: " + fix.satellites + " | HDOP: " + (fix.hdop ?: "-"))
            Text("Lat: " + "%.7f".format(fix.latitude)); Text("Lon: " + "%.7f".format(fix.longitude)); Text("Alt: " + (fix.altitude?.let { "%.3f m".format(it) } ?: "-"))
            Button(onClick = onSaveRtk, enabled = saveEnabledForCrs && fix.fixType == FixType.RTK_FIXED && fix.altitude != null, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("ثبت نقطه از RTK FIX") }
        }
    } }
}

@Composable private fun SurveySummary(points: List<SurveyPoint>) {
    val minZ=points.minOfOrNull{it.z}; val maxZ=points.maxOfOrNull{it.z}; val avg=if(points.isEmpty()) null else points.map{it.z}.average(); val tris=remember(points){TerrainEngine.triangulate(points)}
    ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("خلاصه برداشت", style=MaterialTheme.typography.titleMedium); Text("نقاط: "+points.size+" | TIN: "+tris.size); Text("Z min: "+(minZ?.let{"%.3f".format(it)}?:"-")+" | max: "+(maxZ?.let{"%.3f".format(it)}?:"-")+" | avg: "+(avg?.let{"%.3f".format(it)}?:"-")) } }
}

@Composable private fun PointMap(points: List<SurveyPoint>) {
    val tris=remember(points){TerrainEngine.triangulate(points)}
    ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) { Text("TIN / نمای نقاط", style=MaterialTheme.typography.titleMedium)
        if(points.isEmpty()) Box(Modifier.fillMaxWidth().height(220.dp),contentAlignment=Alignment.Center){Text("نقطه‌ای ثبت نشده")} else Canvas(Modifier.fillMaxWidth().height(280.dp)){
            val minX=points.minOf{it.x}; val maxX=points.maxOf{it.x}; val minY=points.minOf{it.y}; val maxY=points.maxOf{it.y}; val dx=max(maxX-minX,1.0); val dy=max(maxY-minY,1.0); val p=20f; val w=size.width-2*p; val h=size.height-2*p
            fun xy(v:SurveyPoint)=Offset(p+((v.x-minX)/dx).toFloat()*w,p+h-((v.y-minY)/dy).toFloat()*h)
            tris.forEach{t->val a=xy(t.a);val b=xy(t.b);val c=xy(t.c);drawLine(Color.Gray,a,b,1.5f);drawLine(Color.Gray,b,c,1.5f);drawLine(Color.Gray,c,a,1.5f)}; points.forEach{drawCircle(Color(0xFF1565C0),7f,xy(it))}
        }
    } }
}

@Composable private fun PointRow(p: SurveyPoint, onEdit:()->Unit, onDelete:()->Unit) { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp)) { Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(p.name,style=MaterialTheme.typography.titleMedium);Text("Z "+"%.3f".format(p.z))};Text("X "+"%.3f".format(p.x)+" | Y "+"%.3f".format(p.y));Row{TextButton(onClick=onEdit){Text("ویرایش")};TextButton(onClick=onDelete){Text("حذف")}} } } }

@Composable private fun PointDialog(existing:SurveyPoint?,pointNumber:Int,onDismiss:()->Unit,onSave:(SurveyPoint)->Unit){
    var name by remember{mutableStateOf(existing?.name?:"P$pointNumber")};var x by remember{mutableStateOf(existing?.x?.toString()?:"")};var y by remember{mutableStateOf(existing?.y?.toString()?:"")};var z by remember{mutableStateOf(existing?.z?.toString()?:"")};val xv=x.toDoubleOrNull();val yv=y.toDoubleOrNull();val zv=z.toDoubleOrNull();val valid=name.isNotBlank()&&xv!=null&&yv!=null&&zv!=null
    AlertDialog(onDismissRequest=onDismiss,title={Text(if(existing==null)"افزودن نقطه" else "ویرایش نقطه")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(name,{name=it},label={Text("نام")});OutlinedTextField(x,{x=it},label={Text("X / Easting")});OutlinedTextField(y,{y=it},label={Text("Y / Northing")});OutlinedTextField(z,{z=it},label={Text("Z / Elevation")})}},confirmButton={TextButton(enabled=valid,onClick={onSave(SurveyPoint(existing?.id?:System.currentTimeMillis(),name.trim(),xv!!,yv!!,zv!!))}){Text("ذخیره")}},dismissButton={TextButton(onClick=onDismiss){Text("انصراف")}})
}

@Composable private fun CsvImportDialog(onDismiss:()->Unit,onImport:(String)->Unit){var csv by remember{mutableStateOf("name,x,y,z\n")};AlertDialog(onDismissRequest=onDismiss,title={Text("Import CSV")},text={OutlinedTextField(csv,{csv=it},modifier=Modifier.fillMaxWidth().height(220.dp))},confirmButton={TextButton(onClick={onImport(csv)}){Text("ورود")}},dismissButton={TextButton(onClick=onDismiss){Text("انصراف")}})}
@Composable private fun CsvExportDialog(csv:String,onDismiss:()->Unit){AlertDialog(onDismissRequest=onDismiss,title={Text("Export CSV")},text={OutlinedTextField(csv,{},readOnly=true,modifier=Modifier.fillMaxWidth().height(220.dp))},confirmButton={TextButton(onClick=onDismiss){Text("بستن")}})}

@Composable private fun TcpGnssDialog(onDismiss:()->Unit,onConnect:(String,Int)->Unit){var host by remember{mutableStateOf("")};var port by remember{mutableStateOf("9001")};val pv=port.toIntOrNull();AlertDialog(onDismissRequest=onDismiss,title={Text("TCP GNSS")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(host,{host=it.trim()},label={Text("IP / Host")});OutlinedTextField(port,{port=it.filter(Char::isDigit).take(5)},label={Text("Port")})}},confirmButton={TextButton(enabled=host.isNotBlank()&&pv!=null&&pv in 1..65535,onClick={onConnect(host,pv!!)}){Text("اتصال")}},dismissButton={TextButton(onClick=onDismiss){Text("انصراف")}})}

@SuppressLint("MissingPermission")
@Composable private fun BluetoothPickerDialog(adapter: BluetoothAdapter?, onDismiss:()->Unit, onSelect:(BluetoothDevice)->Unit){
    val devices=remember(adapter){adapter?.bondedDevices?.sortedBy{it.name?:it.address}.orEmpty()}
    AlertDialog(onDismissRequest=onDismiss,title={Text("دستگاه‌های Pair شده")},text={if(devices.isEmpty())Text("دستگاه Pair شده‌ای پیدا نشد. ابتدا از تنظیمات Android گیرنده را Pair کنید.") else LazyColumn(Modifier.heightIn(max=360.dp)){items(devices,key={it.address}){d->ListItem(headlineContent={Text(d.name?:"GNSS")},supportingContent={Text(d.address)},modifier=Modifier.fillMaxWidth(),trailingContent={TextButton(onClick={onSelect(d)}){Text("اتصال")}})}}},confirmButton={TextButton(onClick=onDismiss){Text("بستن")}})
}

@Composable private fun NtripDialog(onDismiss:()->Unit,onConnect:(NtripConfig)->Unit){
    var host by remember{mutableStateOf("")};var port by remember{mutableStateOf("2101")};var mount by remember{mutableStateOf("")};var user by remember{mutableStateOf("")};var pass by remember{mutableStateOf("")};val pv=port.toIntOrNull()
    AlertDialog(onDismissRequest=onDismiss,title={Text("NTRIP / RTCM")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(host,{host=it.trim()},label={Text("Caster Host")});OutlinedTextField(port,{port=it.filter(Char::isDigit).take(5)},label={Text("Port")});OutlinedTextField(mount,{mount=it.trim()},label={Text("Mountpoint")});OutlinedTextField(user,{user=it},label={Text("Username")});OutlinedTextField(pass,{pass=it},label={Text("Password")})}},confirmButton={TextButton(enabled=host.isNotBlank()&&mount.isNotBlank()&&pv!=null&&pv in 1..65535,onClick={onConnect(NtripConfig(host,pv!!,mount,user,pass))}){Text("اتصال")}},dismissButton={TextButton(onClick=onDismiss){Text("انصراف")}})
}