package com.example.placas

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.placas.repository.Repository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class IncidenciaFormActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitudActual: Double? = null
    private var longitudActual: Double? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incidencia_form)

        val fotoUri = intent.getStringExtra("photo_uri")
        val placaDetectada = intent.getStringExtra("plate_text")
        val imgPreview = findViewById<ImageView>(R.id.imgPreview)
        val txtPlaca = findViewById<EditText>(R.id.txtPlaca)
        val txtOperadorId = findViewById<EditText>(R.id.txtOperadorId)
        val txtDescripcion = findViewById<EditText>(R.id.txtDescripcion)
        val btnEnviar = findViewById<Button>(R.id.btnEnviar)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        obtenerUbicacion()

        if (fotoUri != null) {
            imgPreview.setImageURI(Uri.parse(fotoUri))
        }

        txtPlaca.isEnabled = false
        txtPlaca.setText(placaDetectada ?: "")

        val repository = Repository()
        val viewModelFactory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        btnEnviar.setOnClickListener {
            val placa = txtPlaca.text.toString().trim()
            val descripcion = txtDescripcion.text.toString().trim()
            val operadorId = txtOperadorId.text.toString().trim().toIntOrNull()

            if (placa.isEmpty()) {
                Toast.makeText(this, "La placa no puede estar vacía", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (descripcion.isEmpty()) {
                Toast.makeText(this, "Escribe una descripción", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (operadorId == null || operadorId <= 0) {
                Toast.makeText(this, "El ID del operador debe ser un número válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (latitudActual == null || longitudActual == null) {
                Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (fotoUri == null) {
                Toast.makeText(this, "No se seleccionó ninguna foto", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val uri = Uri.parse(fotoUri)
                val inputStream = contentResolver.openInputStream(uri)
                val file = File(cacheDir, "incidencia_${System.currentTimeMillis()}.jpg")

                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                // Preparar datos para multipart
                val placaRB = RequestBody.create("text/plain".toMediaTypeOrNull(), placa)
                val operadorRB = RequestBody.create("text/plain".toMediaTypeOrNull(), operadorId.toString())
                val descripcionRB = RequestBody.create("text/plain".toMediaTypeOrNull(), descripcion)
                val latRB = RequestBody.create("text/plain".toMediaTypeOrNull(), latitudActual.toString())
                val lonRB = RequestBody.create("text/plain".toMediaTypeOrNull(), longitudActual.toString())
                val reqFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val fotoPart = MultipartBody.Part.createFormData("evidencia_fotografia", file.name, reqFile)

                viewModel.postIncidencia(placaRB, operadorRB, descripcionRB, latRB, lonRB, fotoPart)

            } catch (e: Exception) {
                Toast.makeText(this, "Error al procesar la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.incidenciaP.observe(this) { response ->
            if (response.isSuccessful) {
                btnEnviar.isEnabled = false
                Toast.makeText(this, "✅ Incidencia registrada exitosamente", Toast.LENGTH_LONG).show()
                viewModel.getVehiculo(txtPlaca.text.toString().trim())
                viewModel.vehiculo.observe(this) { vehiculoResp ->
                    if (vehiculoResp.isSuccessful && vehiculoResp.body() != null) {
                        val usuarioId = vehiculoResp.body()!!.usuario

                        viewModel.getUsuario(usuarioId)
                        viewModel.usuario.observe(this) { usuarioResp ->
                            if (usuarioResp.isSuccessful && usuarioResp.body() != null) {
                                val usuario = usuarioResp.body()!!

                                val tablaUsuario = findViewById<TableLayout>(R.id.tablaUsuario)
                                tablaUsuario.visibility = View.VISIBLE

                                findViewById<TextView>(R.id.tvNombreUsuario).text = usuario.nombre_completo
                                findViewById<TextView>(R.id.tvTipoUsuario).text = usuario.tipo_usuario
                                findViewById<TextView>(R.id.tvTelefonoUsuario).text = usuario.telefono ?: "-"
                                findViewById<TextView>(R.id.tvEmailUsuario).text = usuario.email ?: "-"
                                findViewById<TextView>(R.id.tvDiscapacitadoUsuario).text = if (usuario.discapacitado) "Sí" else "No"
                                findViewById<TextView>(R.id.tvEstadoUsuario).text = if (usuario.estado) "Activo" else "Inactivo"
                            }
                        }
                    }
                }

            } else {
                Toast.makeText(this, "❌ Error: ${response.code()} - ${response.message()}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun obtenerUbicacion() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                latitudActual = location.latitude
                longitudActual = location.longitude
            }
        }
    }

}
