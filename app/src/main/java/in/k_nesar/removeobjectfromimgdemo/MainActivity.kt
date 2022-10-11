package `in`.k_nesar.removeobjectfromimgdemo

import `in`.k_nesar.removeobjectfromimgdemo.databinding.ActivityMainBinding
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.io.IOException


/*override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val myCustomView = MyCustomView(this)
    myCustomView.createView()
    val rlParent = findViewById<RelativeLayout>(R.id.rlParent)
    rlParent.addView(myCustomView)

}*/

class MainActivity : AppCompatActivity() {
    var selectedImagePath: String? = null
    private lateinit var _binding: ActivityMainBinding
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.connect.setOnClickListener { connectServer() }
        binding.selectImage.setOnClickListener { selectImage() }

    }

    private fun connectServer() {
        val ipv4AddressView = findViewById<EditText>(R.id.IPAddress)
        val ipv4Address = ipv4AddressView.text.toString()
        val portNumberView = findViewById<EditText>(R.id.portNumber)
        val portNumber = portNumberView.text.toString()
        val postUrl = "http://$ipv4Address:$portNumber/"
        val stream = ByteArrayOutputStream()
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.RGB_565
        // Read BitMap by file path
        val bitmap = BitmapFactory.decodeFile(selectedImagePath, options)

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val byteArray: ByteArray = stream.toByteArray()
        val postBodyImage: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image",
                "androidFlask.jpg",
                RequestBody.create(MediaType.parse("image/*jpg"), byteArray)
            )
            .build()
        val responseText = findViewById<TextView>(R.id.responseText)
        responseText.text = "Please wait ..."
        postRequest(postUrl, postBodyImage)
    }

    private fun postRequest(postUrl: String?, postBody: RequestBody?) {
        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(postUrl)
            .post(postBody)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override
            fun onFailure(call: Call, e: IOException?) {
                // Cancel the post on failure.
                call.cancel()

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread {
                    val responseText = findViewById<TextView>(R.id.responseText)
                    responseText.text = "Failed to Connect to Server"
                }
            }

            @Throws(IOException::class)
            override
            fun onResponse(call: Call?, response: Response) {
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread {
                    val responseText = findViewById<TextView>(R.id.responseText)
                    try {
                        responseText.text = response.body()!!.string()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    private fun selectImage() {
        val intent = Intent()
        intent.type = "*/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(reqCode: Int, resCode: Int, data: Intent?) {
        super.onActivityResult(reqCode, resCode, data)
        if (resCode == RESULT_OK && data != null) {
            val uri: Uri? = data.data
            selectedImagePath = getPath(applicationContext, uri)

            val stream = ByteArrayOutputStream()
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            val bitmap = BitmapFactory.decodeFile(selectedImagePath, options)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            Log.d("sizeOfImage", "createView: ${bitmap.width}, ${bitmap.height}")

            val myCustomView = MyCustomView(this)
            myCustomView.createView(
                /*Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)*/
            selectedImagePath
            )
            binding.rlParent.addView(myCustomView)

          /*  binding.inputImg.setImageBitmap(
                bitmap
            )*/

            val imgPath = findViewById<EditText>(R.id.imgPath)
            imgPath.setText(selectedImagePath)
            Toast.makeText(applicationContext, selectedImagePath, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        // Implementation of the getPath() method and all its requirements is taken from the StackOverflow Paul Burke's answer: https://stackoverflow.com/a/20559175/5426539
        fun getPath(context: Context, uri: Uri?): String? {
            val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

            // DocumentProvider
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        return Environment.getExternalStorageDirectory()
                            .toString() + "/" + split[1]
                    }

                    // TODO handle non-primary volumes
                } else if (isDownloadsDocument(uri)) {
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri: Uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                    )
                    return getDataColumn(context, contentUri, null, null)
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(
                        split[1]
                    )
                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }
            } else if ("content".equals(uri?.scheme, ignoreCase = true)) {
                return getDataColumn(context, uri, null, null)
            } else if ("file".equals(uri?.scheme, ignoreCase = true)) {
                return uri?.path
            }
            return null
        }

        fun getDataColumn(
            context: Context, uri: Uri?, selection: String?,
            selectionArgs: Array<String>?
        ): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(
                column
            )
            try {
                cursor = uri?.let {
                    context.getContentResolver().query(
                        it, projection, selection, selectionArgs,
                        null
                    )
                }
                if (cursor != null && cursor.moveToFirst()) {
                    val column_index: Int = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(column_index)
                }
            } finally {
                if (cursor != null) cursor.close()
            }
            return null
        }

        fun isExternalStorageDocument(uri: Uri?): Boolean {
            return "com.android.externalstorage.documents" == uri?.authority
        }

        fun isDownloadsDocument(uri: Uri?): Boolean {
            return "com.android.providers.downloads.documents" == uri?.authority
        }

        fun isMediaDocument(uri: Uri?): Boolean {
            return "com.android.providers.media.documents" == uri?.authority
        }
    }
}
