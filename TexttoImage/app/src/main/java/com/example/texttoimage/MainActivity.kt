package com.example.texttoimage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import coil.transform.GrayscaleTransformation
import com.example.texttoimage.ui.theme.TexttoImageTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    var imageUrl by remember { mutableStateOf("") }
    var inputext by remember {mutableStateOf("")}
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        
        TextField(
            value = inputext,
            onValueChange = {inputext = it}
        )
        Button(onClick = {
            CoroutineScope(Dispatchers.IO).launch {
                val url = generateImage(inputext)
                withContext(Dispatchers.Main) {
                    imageUrl = url
                }
            }
        }) {
            Text(text = "Generate Image")
        }

        Spacer(modifier = Modifier.height(16.dp))


        if (imageUrl.isNotEmpty()) {
            val painter = rememberImagePainter(data = imageUrl)
            Image(

                painter = painter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

suspend fun generateImage(prompt: String): String {
    val apiKey = "API_KEY"
    val apiUrl = "https://api.openai.com/v1/images/generations"
    val requestBody = JSONObject()
    requestBody.put("prompt", prompt)
    requestBody.put("n", 1)
    requestBody.put("size", "1024x1024")

    return withContext(Dispatchers.IO) {
        try {
            val url = URL(apiUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val outputStream: OutputStream = connection.outputStream
            outputStream.write(requestBody.toString().toByteArray())
            outputStream.flush()
            outputStream.close()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val responseStream = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(responseStream)
                val dataArray = jsonResponse.getJSONArray("data")
                if (dataArray.length() > 0) {
                    val imageUrl = dataArray.getJSONObject(0).getString("url")
                    imageUrl
                } else {
                    ""
                }
            } else {
                ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}