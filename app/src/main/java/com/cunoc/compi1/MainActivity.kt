package com.cunoc.compi1

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.cunoc.compi1.compiler.AnalyzerError
import com.cunoc.compi1.compiler.AnalyzerSuccess
import com.cunoc.compi1.compiler.DiagramAnalyzer
import com.cunoc.compi1.nodos.DiagramaFlujoView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputText = findViewById<EditText>(R.id.inputText)
        val analyzeButton = findViewById<Button>(R.id.analyzeButton)
        val regresarButton = findViewById<Button>(R.id.backButton)
        val container = findViewById<FrameLayout>(R.id.diagramContainer)

        analyzeButton.setOnClickListener {
            val codigo = inputText.text.toString()
            if (codigo.isBlank()) {
                inputText.error = "Ingrese pseudocódigo"
                return@setOnClickListener
            }

            when (val result = DiagramAnalyzer.analizar(codigo)) {
                is AnalyzerError -> inputText.error = result.message
                is AnalyzerSuccess -> {
                    val diagramaView = DiagramaFlujoView(this)
                    diagramaView.nodos = result.nodos
                    diagramaView.configuraciones = result.configuraciones
                    container.removeAllViews()
                    container.addView(
                        diagramaView,
                        FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    )
                }
            }
        }

        regresarButton.setOnClickListener { container.removeAllViews() }
    }
}