package com.example.islanddisastersurvivalguideapp

import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.islanddisastersurvivalguideapp.viewmodel.MedicalCardViewModel
import com.example.islanddisastersurvivalguideapp.utils.SaveStatus
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalCardScreen(viewModel: MedicalCardViewModel, onBack: () -> Unit) {
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var birthDate by remember { mutableStateOf("") }
    var bloodType by remember { mutableStateOf("A") }
    var medicalHistory by remember { mutableStateOf(TextFieldValue("")) }
    var medications by remember { mutableStateOf(TextFieldValue("")) }
    var emergencyContact by remember { mutableStateOf(TextFieldValue("")) }
    var showForm by remember { mutableStateOf(false) }

    var bloodTypeExpanded by remember { mutableStateOf(false) }
    val bloodTypes = listOf("A", "B", "O", "AB")

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            birthDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val currentCard by viewModel.currentCard.collectAsState()
    val saveStatus by viewModel.saveStatus.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCurrentCard()
    }

    LaunchedEffect(saveStatus) {
        when (saveStatus) {
            is SaveStatus.Success -> {
                showForm = false
                viewModel.resetSaveStatus()
            }
            else -> {}
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFD4E4E2), Color(0xFF77ACA2))
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "返回")
                }
                Text(
                    text = "個人醫療卡",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()

            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        if (!showForm && currentCard != null) {
                            // 顯示現有醫療卡資料
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = Color(0xFFF0F0F0),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = currentCard!!.name,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "血型： ${currentCard!!.bloodType}",
                                            color = Color.Gray,
                                            fontSize = 20.sp
                                        )
                                    }
                                    /*
                                IconButton(onClick = { showForm = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "編輯")
                                }

                                 */
                                }

                                Text(
                                    text = "病史： ${currentCard!!.medicalHistory}",
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = "目前服用藥物： ${currentCard!!.medications}",
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = "緊急聯絡人： ${currentCard!!.emergencyContact}",
                                    fontSize = 20.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { showForm = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFF458F81
                                    )
                                )
                            ) {
                                Text("編輯醫療卡", fontSize = 16.sp)
                            }
                        } else {
                            // 表單界面
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("姓名", fontSize = 15.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Gray,
                                    focusedBorderColor = Color(0xFF458F81)
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = birthDate,
                                onValueChange = { },
                                label = { Text("出生日期", fontSize = 15.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { datePickerDialog.show() }) {
                                        Icon(Icons.Filled.DateRange, "選擇日期")
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Box {
                                OutlinedTextField(
                                    value = bloodType,
                                    onValueChange = { },
                                    label = { Text("血型", fontSize = 15.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = true,
                                    trailingIcon = {
                                        IconButton(onClick = { bloodTypeExpanded = true }) {
                                            Icon(Icons.Default.ArrowDropDown, "選擇血型")
                                        }
                                    }
                                )

                                DropdownMenu(
                                    expanded = bloodTypeExpanded,
                                    onDismissRequest = { bloodTypeExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    bloodTypes.forEach { type ->
                                        DropdownMenuItem(
                                            text = { Text(type) },
                                            onClick = {
                                                bloodType = type
                                                bloodTypeExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = medicalHistory,
                                onValueChange = { medicalHistory = it },
                                label = { Text("病史", fontSize = 15.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = medications,
                                onValueChange = { medications = it },
                                label = { Text("目前服用藥物", fontSize = 15.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = emergencyContact,
                                onValueChange = { emergencyContact = it },
                                label = { Text("緊急聯絡人", fontSize = 15.sp) },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = {
                                        if (currentCard != null) {
                                            showForm = false
                                        } else {
                                            onBack()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF9FB7B3)
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("取消", fontSize = 16.sp)
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Button(
                                    onClick = {
                                        viewModel.saveMedicalCard(
                                            name = name.text,
                                            birthDate = birthDate,
                                            bloodType = bloodType,
                                            medicalHistory = medicalHistory.text,
                                            medications = medications.text,
                                            emergencyContact = emergencyContact.text
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF458F81)
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("儲存", fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}