package com.example.placas

import android.os.Bundle
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.placas.repository.Repository

class TablaOperadoresActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabla_operadores)

        val tabla = findViewById<TableLayout>(R.id.tablaOperadores)

        val repository = Repository()
        val factory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

        viewModel.getOperadores()

        viewModel.operadores.observe(this) { response ->
            if (response.isSuccessful && response.body() != null) {

                val lista = response.body()!!

                lista.forEach { operador ->

                    val fila = TableRow(this)
                    fila.setPadding(4, 4, 4, 4)

                    fun crearTexto(valor: String): TextView {
                        return TextView(this).apply {
                            text = valor
                            setPadding(12, 12, 12, 12)
                        }
                    }

                    val colId = crearTexto(operador.id.toString())
                    val colNombre = crearTexto(operador.nombre_completo)
                    val colTelefono = crearTexto(operador.telefono ?: "—")
                    val colEmail = crearTexto(operador.email ?: "—")
                    val colEstado = crearTexto(if (operador.estado) "Activo" else "Inactivo")

                    fila.addView(colId)
                    fila.addView(colNombre)
                    fila.addView(colTelefono)
                    fila.addView(colEmail)
                    fila.addView(colEstado)

                    tabla.addView(fila)
                }

            } else {
                Toast.makeText(this, "Error al cargar operadores", Toast.LENGTH_LONG).show()
            }
        }
    }
}
