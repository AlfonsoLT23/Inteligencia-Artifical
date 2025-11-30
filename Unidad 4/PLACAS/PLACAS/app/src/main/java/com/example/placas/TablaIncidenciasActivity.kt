package com.example.placas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.placas.repository.Repository
class TablaIncidenciasActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabla_incidencias)

        val tabla = findViewById<TableLayout>(R.id.tablaIncidencias)

        val repository = Repository()
        val factory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

        viewModel.getIncidencias()

        viewModel.incidencias.observe(this) { response ->
            if (response.isSuccessful && response.body() != null) {

                val lista = response.body()!!

                lista.forEach { incidencia ->

                    val fila = TableRow(this)
                    fila.setPadding(4, 4, 4, 4)

                    fun crearTexto(valor: String): TextView {
                        return TextView(this).apply {
                            text = valor
                            setPadding(12, 12, 12, 12)
                        }
                    }

                    val colVehiculo = crearTexto(incidencia.vehiculo)
                    val colOperador = crearTexto(incidencia.operador.toString())
                    val colDescripcion = crearTexto(incidencia.descripcion_incidencia)
                    val colFecha = crearTexto(incidencia.fecha_hora)
                    val colEvidencia = crearTexto(incidencia.evidencia_fotografia)
                    val colLatitud = crearTexto(incidencia.latitud.toString())
                    val colLongitud = crearTexto(incidencia.longitud.toString())

                    fila.addView(colVehiculo)
                    fila.addView(colOperador)
                    fila.addView(colDescripcion)
                    fila.addView(colFecha)
                    fila.addView(colEvidencia)
                    fila.addView(colLatitud)
                    fila.addView(colLongitud)

                    tabla.addView(fila)
                }

            } else {
                Toast.makeText(this, "Error al cargar incidencias", Toast.LENGTH_LONG).show()
            }
        }
    }
}

