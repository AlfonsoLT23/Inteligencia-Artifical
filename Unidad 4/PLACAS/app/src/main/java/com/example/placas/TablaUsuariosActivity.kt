package com.example.placas

import android.os.Bundle
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.placas.repository.Repository

class TablaUsuariosActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabla_usuarios)

        val tabla = findViewById<TableLayout>(R.id.tablaUsuarios)

        val repository = Repository()
        val factory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

        viewModel.getUsuarios()

        viewModel.usuarios.observe(this) { response ->
            if (response.isSuccessful && response.body() != null) {

                val lista = response.body()!!

                lista.forEach { usuario ->

                    val fila = TableRow(this)
                    fila.setPadding(4, 4, 4, 4)

                    fun crearTexto(valor: String): TextView {
                        return TextView(this).apply {
                            text = valor
                            setPadding(12, 12, 12, 12)
                        }
                    }

                    val colId = crearTexto(usuario.id.toString())
                    val colNombre = crearTexto(usuario.nombre_completo)
                    val colTipo = crearTexto(usuario.tipo_usuario)
                    val colTelefono = crearTexto(usuario.telefono ?: "—")
                    val colEmail = crearTexto(usuario.email ?: "—")
                    val colRegistro = crearTexto(usuario.fecha_registro)
                    val colDiscap = crearTexto(if (usuario.discapacitado) "Sí" else "No")
                    val colEstado = crearTexto(if (usuario.estado) "Activo" else "Inactivo")

                    fila.addView(colId)
                    fila.addView(colNombre)
                    fila.addView(colTipo)
                    fila.addView(colTelefono)
                    fila.addView(colEmail)
                    fila.addView(colRegistro)
                    fila.addView(colDiscap)
                    fila.addView(colEstado)

                    tabla.addView(fila)
                }

            } else {
                Toast.makeText(this, "Error al cargar usuarios", Toast.LENGTH_LONG).show()
            }
        }
    }
}
