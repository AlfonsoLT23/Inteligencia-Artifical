package com.example.placas

import android.os.Bundle
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.placas.repository.Repository

class TablaVehiculosActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabla_vehiculos)

        val tabla = findViewById<TableLayout>(R.id.tablaVehiculos)

        val repository = Repository()
        val factory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

        viewModel.getVehiculos()   // <- Asegúrate que existe en tu ViewModel

        viewModel.vehiculos.observe(this) { response ->
            if (response.isSuccessful && response.body() != null) {

                val lista = response.body()!!

                lista.forEach { vehiculo ->

                    val fila = TableRow(this)
                    fila.setPadding(4, 4, 4, 4)

                    fun crearTexto(valor: String): TextView {
                        return TextView(this).apply {
                            text = valor
                            setPadding(12, 12, 12, 12)
                        }
                    }

                    val colPlaca = crearTexto(vehiculo.placa)
                    val colUsuario = crearTexto(vehiculo.usuario.toString())
                    val colMarca = crearTexto(vehiculo.marca ?: "—")
                    val colModelo = crearTexto(vehiculo.modelo ?: "—")
                    val colColor = crearTexto(vehiculo.color ?: "—")
                    val colAnio = crearTexto(vehiculo.anio?.toString() ?: "—")
                    val colEstado = crearTexto(if (vehiculo.estado) "Activo" else "Inactivo")

                    fila.addView(colPlaca)
                    fila.addView(colUsuario)
                    fila.addView(colMarca)
                    fila.addView(colModelo)
                    fila.addView(colColor)
                    fila.addView(colAnio)
                    fila.addView(colEstado)

                    tabla.addView(fila)
                }

            } else {
                Toast.makeText(this, "Error al cargar vehículos", Toast.LENGTH_LONG).show()
            }
        }
    }
}
