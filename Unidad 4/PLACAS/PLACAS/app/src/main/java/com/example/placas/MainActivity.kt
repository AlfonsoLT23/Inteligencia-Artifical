package com.example.placas



import android.app.Activity

import android.content.Intent

import android.content.pm.PackageManager

import android.graphics.Bitmap

import android.graphics.BitmapFactory

import android.graphics.Canvas

import android.graphics.Color

import android.graphics.Matrix

import android.graphics.Paint

import android.graphics.RectF

import android.net.Uri

import android.os.Bundle

import android.os.Environment

import android.provider.MediaStore

import android.util.Log

import android.widget.Button

import android.widget.ImageView

import android.widget.TextView

import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity

import androidx.core.app.ActivityCompat

import androidx.core.content.ContextCompat

import androidx.core.content.FileProvider

import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.placas.model.IncidenciaRequest
import com.example.placas.repository.Repository

import com.google.mlkit.vision.common.InputImage

import com.google.mlkit.vision.text.TextRecognition

import com.google.mlkit.vision.text.latin.TextRecognizerOptions

import org.tensorflow.lite.Interpreter

import org.tensorflow.lite.DataType

import org.tensorflow.lite.support.image.ImageProcessor

import org.tensorflow.lite.support.image.TensorImage

import org.tensorflow.lite.support.image.ops.ResizeOp

import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

import java.io.File

import java.io.IOException

import java.nio.ByteBuffer

import java.nio.ByteOrder

import java.text.SimpleDateFormat

import java.util.Date

import kotlin.math.max

import kotlin.math.min



class MainActivity : AppCompatActivity() {



// Constantes

    private val REQUEST_IMAGE_CAPTURE = 1

    private val CAMERA_PERMISSION_CODE = 100

    private val MODEL_FILE_NAME = "best_float16.tflite"

    private val INPUT_SIZE = 640

    private val CONFIDENCE_THRESHOLD = 0.2f // Umbral de confianza

    private val IOU_THRESHOLD = 0.45f

    private val NUM_CLASSES = 1

    private val NUM_BOXES = 8400 // Valor verificado de la forma del tensor [1, 5, 8400]



// Variables para almacenar la URI y la ruta del archivo de alta resolución

    private lateinit var currentPhotoUri: Uri

    private lateinit var currentPhotoPath: String



// Vistas y Modelos

    private lateinit var plateImageView: ImageView

    private lateinit var resultTextView: TextView

    private var tfliteInterpreter: Interpreter? = null



    data class Detection(

        val boundingBox: RectF,

        val confidence: Float,

        val classIndex: Int

    )

    private lateinit var viewModel: MainViewModel


// --- Ciclo de Vida (Omitido) ---

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        
        val btnIncidencias: Button = findViewById(R.id.tincidencias)

        btnIncidencias.setOnClickListener {
            val intent = Intent(this, TablaIncidenciasActivity::class.java)
            startActivity(intent)
        }

        val btnOperadores: Button = findViewById(R.id.toperadores)

        btnOperadores.setOnClickListener {
            val intent = Intent(this, TablaOperadoresActivity::class.java)
            startActivity(intent)
        }

        val btnUsuarios: Button = findViewById(R.id.tusuarios)

        btnUsuarios.setOnClickListener {
            val intent = Intent(this, TablaUsuariosActivity::class.java)
            startActivity(intent)
        }

        val btnVehiculos: Button = findViewById(R.id.tvehiculos)

        btnVehiculos.setOnClickListener {
            val intent = Intent(this, TablaVehiculosActivity::class.java)
            startActivity(intent)
        }

        plateImageView = findViewById(R.id.img_plate_capture)

        resultTextView = findViewById(R.id.txt_result_placa)

        val takePhotoBtn: Button = findViewById(R.id.btn_take_photo)

        try {

            val options = Interpreter.Options()

            options.setNumThreads(4)

            tfliteInterpreter = Interpreter(loadModelFile(), options)

            Log.d("TFLite", "Modelo TFLite cargado con éxito.")

        } catch (e: IOException) {

            Log.e("TFLite", "Error al cargar el modelo TFLite", e)

            resultTextView.text = "Error: Modelo TFLite no encontrado en assets."

            Toast.makeText(this, "Error al cargar el modelo de detección.", Toast.LENGTH_LONG).show()

        }



        takePhotoBtn.setOnClickListener {

            if (checkCameraPermission()) {

                dispatchTakePictureIntent()

            } else {

                requestCameraPermission()

            }

        }

    }



// --- Lógica de Permisos (Omitida) ---



    private fun checkCameraPermission(): Boolean {

        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    }



    private fun requestCameraPermission() {

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)

    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE) {

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                dispatchTakePictureIntent()

            } else {

                Toast.makeText(this, "Permiso de cámara denegado.", Toast.LENGTH_LONG).show()

            }

        }

    }



// --- Lógica de Captura en ALTA RESOLUCIÓN ---



    @Throws(IOException::class)

    private fun createImageFile(): File {

        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(

            "JPEG_${timeStamp}_",

            ".jpg",

            storageDir

        ).apply {

            currentPhotoUri = FileProvider.getUriForFile(

                this@MainActivity,

                "com.example.placas.fileprovider",

                this

            )

            currentPhotoPath = absolutePath

        }

    }



    private fun dispatchTakePictureIntent() {

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->

            takePictureIntent.resolveActivity(packageManager)?.also {

                val photoFile: File? = try {

                    createImageFile()

                } catch (ex: IOException) {

                    Log.e("Camera", "Error creando archivo de imagen", ex)

                    null

                }



                photoFile?.also {

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)

                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)

                }

            }

        }

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {

            val originalBitmap: Bitmap? = try {

                contentResolver.openInputStream(currentPhotoUri)?.use { inputStream ->

                    BitmapFactory.decodeStream(inputStream)

                }

            } catch (e: Exception) {

                Log.e("Camera", "Error decodificando imagen de URI", e)

                null

            }



            if (originalBitmap == null) {

                Toast.makeText(this, "Error: No se pudo obtener la imagen de alta resolución.", Toast.LENGTH_LONG).show()

                return

            }



            val finalBitmap = rotateBitmapIfRequired(originalBitmap, currentPhotoPath)

            processImageForPlate(finalBitmap)



        } else {

            if (::currentPhotoUri.isInitialized) {

                contentResolver.delete(currentPhotoUri, null, null)

            }

        }

    }



    private fun rotateBitmapIfRequired(bitmap: Bitmap, photoPath: String): Bitmap {

        try {

            val exifInterface = ExifInterface(photoPath)

            val orientation = exifInterface.getAttributeInt(

                ExifInterface.TAG_ORIENTATION,

                ExifInterface.ORIENTATION_NORMAL

            )



            val matrix = Matrix()

            when (orientation) {

                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)

                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)

                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(270f)

                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1.0f, 1.0f)

                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1.0f, -1.0f)

                ExifInterface.ORIENTATION_TRANSPOSE -> {

                    matrix.setRotate(90f)

                    matrix.preScale(-1.0f, 1.0f)

                }

                ExifInterface.ORIENTATION_TRANSVERSE -> {

                    matrix.setRotate(270f)

                    matrix.preScale(-1.0f, 1.0f)

                }

                else -> return bitmap

            }



            return Bitmap.createBitmap(

                bitmap,

                0,

                0,

                bitmap.width,

                bitmap.height,

                matrix,

                true

            )

        } catch (e: Exception) {

            Log.e("EXIF", "Error al leer o rotar EXIF: ${e.message}")

            return bitmap

        }

    }



// --- FUNCIÓN PARA DIBUJAR CAJA DELIMITADORA ---

    private fun drawBoundingBox(bitmap: Bitmap, detection: Detection): Bitmap {

        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        val canvas = Canvas(mutableBitmap)

        val paint = Paint().apply {

            color = Color.RED

            style = Paint.Style.STROKE

            strokeWidth = 10f

        }

        canvas.drawRect(detection.boundingBox, paint)

        return mutableBitmap

    }



// --- Lógica de Detección (TFLite) y OCR (ML Kit) ---

    private fun processImageForPlate(bitmap: Bitmap) {

        if (tfliteInterpreter == null) {

            resultTextView.text = "Error: Modelo TFLite no cargado."

            return

        }



        resultTextView.text = "Detectando y reconociendo..."


// 1. Pre-procesamiento para TFLite

        val imageProcessor = ImageProcessor.Builder()

            .add(ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))

            .build()



        var tensorImage = TensorImage(DataType.FLOAT32)

        tensorImage.load(bitmap)

        tensorImage = imageProcessor.process(tensorImage)


// 2. Ejecutar inferencia

        val outputShape = intArrayOf(1, 4 + NUM_CLASSES, NUM_BOXES)

        val outputTensor = TensorBuffer.createFixedSize(outputShape, DataType.FLOAT32)



        try {

            tfliteInterpreter!!.run(tensorImage.buffer, outputTensor.buffer.rewind())

        } catch (e: Exception) {

            Log.e("TFLite", "Error de ejecución del modelo.", e)

            resultTextView.text = "Error: Fallo al ejecutar el modelo. Verificar NUM_BOXES o INPUT_SIZE."

            return

        }



// 3. Post-procesamiento (NMS)

        val detections = processYoloOutputAndGetPlateBox(

            outputTensor.floatArray,

            bitmap.width,

            bitmap.height

        )



        val bestPlate = detections.maxByOrNull { it.confidence }



        var displayBitmap = bitmap



        if (bestPlate != null) {

            displayBitmap = drawBoundingBox(displayBitmap, bestPlate)

        } else {

            Log.w("PlacasApp", "No se detectó ninguna placa por encima del umbral ($CONFIDENCE_THRESHOLD).")

            resultTextView.text = "Placa no detectada."

        }



        plateImageView.setImageBitmap(displayBitmap)



        if (bestPlate == null) {

            return

        }



// 4. OCR con ML Kit

        val image = InputImage.fromBitmap(displayBitmap, 0)

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)



        recognizer.process(image)

            .addOnSuccessListener { visionText ->

                var recognizedPlateText = "Placa no encontrada (OCR)"



                for (block in visionText.textBlocks) {

                    for (line in block.lines) {

                        val text = line.text.trim().replace(" ", "").uppercase()



                        if (text.length in 3..10 && text.contains(Regex("[A-Z0-9]"))) {

                            recognizedPlateText = text

                            resultTextView.text = "Registrar incidencia: $recognizedPlateText"

                            val intent = Intent(this@MainActivity, IncidenciaFormActivity::class.java)
                            intent.putExtra("photo_uri", currentPhotoUri.toString())
                            intent.putExtra("plate_text", recognizedPlateText)

                            startActivity(intent)

                            return@addOnSuccessListener


                        }

                    }

                }



                resultTextView.text = recognizedPlateText

            }

            .addOnFailureListener { e ->

                resultTextView.text = "Error en OCR: ${e.message}"

            }

    }



// --- FUNCIONES AUXILIARES (NMS y Carga de Modelo) ---

    private fun processYoloOutputAndGetPlateBox(

        outputArray: FloatArray,

        originalWidth: Int,

        originalHeight: Int

    ): List<Detection> {

        val numChannels = 4 + NUM_CLASSES

        val numBoxes = outputArray.size / numChannels

        val detections = mutableListOf<Detection>()

        var maxConfidence = 0f



        for (i in 0 until numBoxes) {

            val cx = outputArray[i + 0 * NUM_BOXES]

            val cy = outputArray[i + 1 * NUM_BOXES]

            val w = outputArray[i + 2 * NUM_BOXES]

            val h = outputArray[i + 3 * NUM_BOXES]

            val boxConfidence = outputArray[i + 4 * NUM_BOXES]



            if (boxConfidence > maxConfidence) {

                maxConfidence = boxConfidence

            }



            if (boxConfidence >= CONFIDENCE_THRESHOLD) {



                val xmin = cx - w / 2

                val ymin = cy - h / 2

                val xmax = cx + w / 2

                val ymax = cy + h / 2



                val xScale = originalWidth.toFloat() / INPUT_SIZE

                val yScale = originalHeight.toFloat() / INPUT_SIZE



                val visualPadding = 5f



                val scaledXmin = max(0f, xmin * xScale - visualPadding)

                val scaledYmin = max(0f, ymin * yScale - visualPadding)

                val scaledXmax = min(originalWidth.toFloat(), xmax * xScale + visualPadding)

                val scaledYmax = min(originalHeight.toFloat(), ymax * yScale + visualPadding)



                val rect = RectF(scaledXmin, scaledYmin, scaledXmax, scaledYmax)



                detections.add(Detection(rect, boxConfidence, 0))

            }

        }



        Log.d("YOLO_DIAG", "Máx. Confianza em Detecção: $maxConfidence")



        return nonMaxSuppression(detections)

    }



    private fun nonMaxSuppression(detections: List<Detection>): List<Detection> {

        val sortedDetections = detections.sortedByDescending { it.confidence }

        val result = mutableListOf<Detection>()

        val suppress = BooleanArray(sortedDetections.size) { false }



        for (i in sortedDetections.indices) {

            if (suppress[i]) continue



            val detectionA = sortedDetections[i]

            result.add(detectionA)



            for (j in (i + 1) until sortedDetections.size) {

                if (suppress[j]) continue



                val detectionB = sortedDetections[j]

                val iou = calculateIou(detectionA.boundingBox, detectionB.boundingBox)



                if (iou > IOU_THRESHOLD) {

                    suppress[j] = true

                }

            }

        }

        return result

    }



    private fun calculateIou(rectA: RectF, rectB: RectF): Float {

        val intersection = RectF(

            max(rectA.left, rectB.left),

            max(rectA.top, rectB.top),

            min(rectA.right, rectB.right),

            min(rectA.bottom, rectB.bottom)

        )



        val intersectionArea = max(0f, intersection.width()) * max(0f, intersection.height())

        val areaA = rectA.width() * rectA.height()

        val areaB = rectB.width() * rectB.height()



        return intersectionArea / (areaA + areaB - intersectionArea)

    }



    @Throws(IOException::class)

    private fun loadModelFile(): ByteBuffer {

        val fileDescriptor = assets.openFd(MODEL_FILE_NAME)

        val inputStream = fileDescriptor.createInputStream()

        val fileChannel = inputStream.channel

        val startOffset = fileDescriptor.startOffset

        val declaredLength = fileDescriptor.declaredLength



        val byteBuffer = fileChannel.map(java.nio.channels.FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

        byteBuffer.order(ByteOrder.nativeOrder())

        return byteBuffer

    }

}
