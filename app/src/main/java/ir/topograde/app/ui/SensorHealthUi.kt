package ir.topograde.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ir.topograde.app.sensors.*

@Composable
fun SensorHealthCard(statuses: List<SensorHealthStatus>, safety: MachineSafetyDecision) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("سلامت دستگاه", style = MaterialTheme.typography.titleLarge)
            Text("قبل از شروع کار، وضعیت چراغ‌ها را بررسی کنید.", style = MaterialTheme.typography.bodyMedium)
            statuses.forEach { status ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("●", color = when (status.level) {
                        SensorHealthLevel.GOOD -> Color(0xFF2E7D32)
                        SensorHealthLevel.CAUTION, SensorHealthLevel.WAITING -> Color(0xFFF9A825)
                        SensorHealthLevel.BLOCKED, SensorHealthLevel.DISCONNECTED -> Color(0xFFC62828)
                        SensorHealthLevel.NOT_CONFIGURED -> Color.Gray
                    }, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(status.title, style = MaterialTheme.typography.titleSmall)
                        Text(status.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(when (status.level) {
                        SensorHealthLevel.GOOD -> "آماده"
                        SensorHealthLevel.CAUTION -> "احتیاط"
                        SensorHealthLevel.BLOCKED -> "توقف"
                        SensorHealthLevel.DISCONNECTED -> "قطع"
                        SensorHealthLevel.WAITING -> "انتظار"
                        SensorHealthLevel.NOT_CONFIGURED -> "نصب نشده"
                    }, style = MaterialTheme.typography.labelMedium,
                        color = when (status.level) {
                            SensorHealthLevel.GOOD -> Color(0xFF2E7D32)
                            SensorHealthLevel.CAUTION, SensorHealthLevel.WAITING -> Color(0xFFF9A825)
                            SensorHealthLevel.BLOCKED, SensorHealthLevel.DISCONNECTED -> Color(0xFFC62828)
                            SensorHealthLevel.NOT_CONFIGURED -> MaterialTheme.colorScheme.onSurfaceVariant
                        })
                }
            }
            HorizontalDivider()
            Text(when (safety.state) {
                MachineSafetyState.READY -> "راهنمایی دستگاه آماده است"
                MachineSafetyState.LIMITED -> "راهنمایی با احتیاط انجام شود"
                MachineSafetyState.BLOCKED -> "برای این حالت، دستگاه آماده نیست"
                MachineSafetyState.EMERGENCY_STOP -> "توقف اضطراری فعال است"
                MachineSafetyState.MANUAL_OVERRIDE -> "کنترل دستی فعال است"
            }, style = MaterialTheme.typography.titleMedium,
                color = if (safety.state == MachineSafetyState.READY) Color(0xFF2E7D32) else Color(0xFFC62828))
            Text(if (safety.canEnableAutoLevel) "تنظیم خودکار تیغه آماده است"
                else "تنظیم خودکار هنوز فعال نیست؛ سنسورهای ماشین و کنترلر ایمنی باید نصب و آزمایش شوند.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
