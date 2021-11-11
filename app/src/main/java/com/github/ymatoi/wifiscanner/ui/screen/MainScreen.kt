package com.github.ymatoi.wifiscanner.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ymatoi.wifiscanner.ui.composables.ScanErrorDialog
import com.github.ymatoi.wifiscanner.ui.composables.ScanResultState
import com.github.ymatoi.wifiscanner.ui.composables.StartMessage
import com.github.ymatoi.wifiscanner.ui.composables.WifiCardList
import timber.log.Timber

@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.scan()
            Timber.d("Granted")
        } else {
            Timber.d("Denied")
        }
    }
    val context = LocalContext.current

    val onRefresh = {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) -> {
                viewModel.scan()
            }
            else -> {
                launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    val isLoading = viewModel.isLoading
    val isError = viewModel.isError
    val scanResultStates = viewModel.scanResultStates.collectAsState(initial = emptyList())
    val searchKeyword = viewModel.searchKeyword.collectAsState()

    MainScreen(
        isRefreshing = isLoading.value,
        scanResultStates = scanResultStates.value,
        onRefresh = onRefresh,
        searchKeyword = searchKeyword.value,
        onValueSearchKeywordChange = { text -> viewModel.updateSearchKeyword(text) },
        isError = isError.value,
        onClickConfirmButton = { viewModel.closeErrorDialog() }
    )
}

@Composable
fun MainScreen(
    searchKeyword: String,
    onValueSearchKeywordChange: (text: String) -> Unit,
    isRefreshing: Boolean,
    scanResultStates: List<ScanResultState>,
    onRefresh: () -> Unit,
    isError: Boolean,
    onClickConfirmButton: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ScanErrorDialog(openDialog = isError, onClickConfirmButton = onClickConfirmButton)
        TextField(
            value = searchKeyword,
            onValueChange = onValueSearchKeywordChange,
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp, top = 8.dp)
                .fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            WifiCardList(
                scanResultStates = scanResultStates,
                isRefreshing = isRefreshing,
                onRefresh = onRefresh
            )
            if (scanResultStates.isEmpty()) {
                StartMessage()
            }
        }
    }
}

@Preview
@Composable
fun PreviewMainScreen() {
    MainScreen(
        isRefreshing = false,
        scanResultStates = emptyList(),
        onRefresh = {},
        searchKeyword = "text",
        onValueSearchKeywordChange = {},
        isError = false,
        onClickConfirmButton = {}
    )
}

@Preview
@Composable
fun PreviewMainScreenLoading() {
    MainScreen(
        isRefreshing = true,
        scanResultStates = emptyList(),
        onRefresh = {},
        searchKeyword = "text",
        onValueSearchKeywordChange = {},
        isError = false,
        onClickConfirmButton = {}
    )
}
