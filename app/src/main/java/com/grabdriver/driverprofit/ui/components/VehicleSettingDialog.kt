package com.grabdriver.driverprofit.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grabdriver.driverprofit.data.local.entity.VehicleProfileEntity
import com.grabdriver.driverprofit.ui.theme.GrabGreen

@Composable
fun VehicleSettingDialog(
    vehicles: List<VehicleProfileEntity>,
    selectedVehicleId: Int,
    onDismiss: () -> Unit,
    onSelectVehicle: (Int) -> Unit,
    onAddNewVehicle: (name: String, type: String, defaultKmL: Double, idleRate: Double) -> Unit,
    onUpdateVehicle: (id: Int, name: String, type: String, defaultKmL: Double, idleRate: Double) -> Unit,
    onDeleteVehicle: (VehicleProfileEntity) -> Unit
) {
    var isAddingNew by remember { mutableStateOf(false) }
    var editingVehicle by remember { mutableStateOf<VehicleProfileEntity?>(null) }
    var vehicleToDelete by remember { mutableStateOf<VehicleProfileEntity?>(null) }

    var vehicleName by remember { mutableStateOf("") }
    var isCar by remember { mutableStateOf(false) }
    var kmLText by remember { mutableStateOf("") }
    var idleRateText by remember { mutableStateOf("") }

    val isInFormMode = isAddingNew || editingVehicle != null

    fun startEditing(vehicle: VehicleProfileEntity) {
        editingVehicle = vehicle
        isAddingNew = false
        vehicleName = vehicle.name
        isCar = vehicle.vehicleType == "CAR"
        kmLText = vehicle.defaultKmL.toString()
        idleRateText = vehicle.idleLiterPerHour.toString()
    }

    fun startAdding() {
        isAddingNew = true
        editingVehicle = null
        vehicleName = ""
        isCar = false
        kmLText = ""
        idleRateText = ""
    }

    fun resetToListView() {
        isAddingNew = false
        editingVehicle = null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            AnimatedContent(
                targetState = when {
                    editingVehicle != null -> "✏️ แก้ไขรุ่นรถ"
                    isAddingNew -> "🛵 เพิ่มรุ่นรถใหม่"
                    else -> "🚗 เลือกรุ่นรถที่ใช้งาน"
                },
                transitionSpec = {
                    (slideInVertically(tween(200)) { -it / 2 } + fadeIn(tween(200))) togetherWith
                    (slideOutVertically(tween(180)) { it / 2 } + fadeOut(tween(180)))
                },
                label = "VehicleTitleAnim"
            ) { titleText ->
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            AnimatedContent(
                targetState = isInFormMode,
                transitionSpec = {
                    if (targetState) {
                        (slideInHorizontally(tween(220)) { it } + fadeIn(tween(200))) togetherWith
                        (slideOutHorizontally(tween(180)) { -it / 3 } + fadeOut(tween(150)))
                    } else {
                        (slideInHorizontally(tween(220)) { -it / 3 } + fadeIn(tween(200))) togetherWith
                        (slideOutHorizontally(tween(180)) { it } + fadeOut(tween(150)))
                    }
                },
                label = "VehicleDialogModeAnim"
            ) { inForm ->
                if (!inForm) {
                    Column {
                        Text(
                        text = "เลือกรถที่ใช้ขับในวันนี้ หรือกดไอคอนแก้ไข/ลบตามต้องการ:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 260.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(vehicles, key = { it.id }) { vehicle ->
                            val isCurrent = vehicle.id == selectedVehicleId
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isCurrent) GrabGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSelectVehicle(vehicle.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (vehicle.vehicleType == "CAR") Icons.Default.DirectionsCar else Icons.Default.TwoWheeler,
                                            contentDescription = null,
                                            tint = if (isCurrent) GrabGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Column {
                                            Text(
                                                text = vehicle.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "มาตรฐาน ${vehicle.defaultKmL} km/L | Idling ${vehicle.idleLiterPerHour} L/ชม.",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        if (isCurrent) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "เลือกใช้งานอยู่",
                                                tint = GrabGreen,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { startEditing(vehicle) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "แก้ไข",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        if (vehicles.size > 1) {
                                            IconButton(
                                                onClick = { vehicleToDelete = vehicle },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = "ลบ",
                                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { startAdding() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("เพิ่มรุ่นรถใหม่")
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = vehicleName,
                        onValueChange = { vehicleName = it },
                        label = { Text("ชื่อรุ่นรถ (เช่น Wave 125i)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !isCar,
                            onClick = {
                                isCar = false
                                if (idleRateText.isEmpty() || idleRateText == "0.75") idleRateText = "0.15"
                            },
                            label = { Text("มอเตอร์ไซค์") },
                            leadingIcon = { Icon(Icons.Default.TwoWheeler, null) }
                        )
                        FilterChip(
                            selected = isCar,
                            onClick = {
                                isCar = true
                                if (idleRateText.isEmpty() || idleRateText == "0.15") idleRateText = "0.75"
                            },
                            label = { Text("รถยนต์") },
                            leadingIcon = { Icon(Icons.Default.DirectionsCar, null) }
                        )
                    }

                    OutlinedTextField(
                        value = kmLText,
                        onValueChange = { kmLText = it },
                        label = { Text("อัตราสิ้นเปลืองมาตรฐาน (km/L)") },
                        placeholder = { Text("เช่น 50.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = idleRateText,
                        onValueChange = { idleRateText = it },
                        label = { Text("อัตรา Idling ตอนจอด (ลิตร/ชม.)") },
                        placeholder = { Text("มอไซค์ ~0.15, รถยนต์ ~0.75") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (editingVehicle != null && vehicles.size > 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = { vehicleToDelete = editingVehicle },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ลบรุ่นรถนี้")
                        }
                    }
                }
            }
        }
    },
        confirmButton = {
            AnimatedContent(
                targetState = isInFormMode,
                transitionSpec = {
                    fadeIn(tween(180)) togetherWith fadeOut(tween(140))
                },
                label = "ConfirmBtnAnim"
            ) { inForm ->
                if (inForm) {
                    Button(
                        onClick = {
                            val kmL = kmLText.toDoubleOrNull() ?: (if (isCar) 18.0 else 50.0)
                            val idle = idleRateText.toDoubleOrNull() ?: (if (isCar) 0.75 else 0.15)
                            if (vehicleName.isNotBlank()) {
                                if (editingVehicle != null) {
                                    onUpdateVehicle(
                                        editingVehicle!!.id,
                                        vehicleName.trim(),
                                        if (isCar) "CAR" else "MOTORCYCLE",
                                        kmL,
                                        idle
                                    )
                                } else {
                                    onAddNewVehicle(
                                        vehicleName.trim(),
                                        if (isCar) "CAR" else "MOTORCYCLE",
                                        kmL,
                                        idle
                                    )
                                }
                                resetToListView()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GrabGreen)
                    ) {
                        Text(if (editingVehicle != null) "บันทึกการแก้ไข" else "บันทึกรุ่นรถ")
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = GrabGreen)
                    ) {
                        Text("เรียบร้อย")
                    }
                }
            }
        },
        dismissButton = {
            AnimatedContent(
                targetState = isInFormMode,
                transitionSpec = {
                    fadeIn(tween(180)) togetherWith fadeOut(tween(140))
                },
                label = "DismissBtnAnim"
            ) { inForm ->
                TextButton(
                    onClick = {
                        if (inForm) resetToListView() else onDismiss()
                    }
                ) {
                    Text(if (inForm) "ย้อนกลับ" else "ปิด")
                }
            }
        },
        shape = RoundedCornerShape(20.dp)
    )

    // Dialog ยืนยันการลบรุ่นรถ
    vehicleToDelete?.let { vehicle ->
        AlertDialog(
            onDismissRequest = { vehicleToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text("ยืนยันการลบรุ่นรถ", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("คุณแน่ใจหรือไม่ว่าต้องการลบ \"${vehicle.name}\" ออกจากระบบ?\n(ข้อมูลรอบวิ่งที่เคยบันทึกไว้จะไม่ได้รับผลกระทบ)")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteVehicle(vehicle)
                        if (editingVehicle?.id == vehicle.id) {
                            editingVehicle = null
                        }
                        vehicleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("ลบ", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { vehicleToDelete = null }) {
                    Text("ยกเลิก")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}
