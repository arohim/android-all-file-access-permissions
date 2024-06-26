package com.him.sama.storagepermissionandroid12

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.him.sama.storagepermissionandroid12.ui.theme.StoragePermissionAndroid12Theme
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

class MainActivity : ComponentActivity() {

    private val fileNames = MutableStateFlow<String>("")

    private val REQUEST_CODE = 1

    private val storagePermissionActivityLauncher = registerForActivityResult(
        StartActivityForResult()
    ) {
        // Granted - access external storage code here
        readFiles()
    }

    private val requestFileLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    val file = uri.path?.let { File(it) }
                    fileNames.value = "${file?.path}"
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isExternalStorageGranted = checkStoragePermission()
        if (!isExternalStorageGranted) {
            // Request permission
            requestStoragePermission()
        } else {
            // Granted - access external storage code here
            readFiles()
        }
        setContent {
            StoragePermissionAndroid12Theme {
                // A surface container using the 'background' color from the theme
                val fileNameState by fileNames.collectAsState()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier,
                    ) {
                        Greeting("Android")
                        Button(
                            modifier = Modifier.wrapContentSize(),
                            onClick = {
                                openFolderBrowser()
                            }) {
                            Text(text = "File Picker")
                        }
                        Text(text = fileNameState)
                    }
                }
            }
        }
    }

    private fun openFolderBrowser() {
        val mimeTypes = arrayOf("text/plain", "video/*")
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        requestFileLauncher.launch(intent)
    }

    private fun readFiles() {
        Toast.makeText(this, "Granted", Toast.LENGTH_LONG).show()
        val downloadFolder = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        Log.d("download folder", "path ${downloadFolder?.path}")
        downloadFolder?.listFiles()?.forEach {
            Log.d("download folder", "file ${it.path}")
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent()
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.setData(uri)
                storagePermissionActivityLauncher.launch(intent)
            } catch (e: Exception) {
                val intent = Intent()
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                storagePermissionActivityLauncher.launch(intent)
            }
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE)
        }
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StoragePermissionAndroid12Theme {
        Greeting("Android")
    }
}