package com.cunoc.compi1

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.cunoc.compi1.nodos.ConfigBuilder
import com.cunoc.compi1.nodos.Configuracion
import com.cunoc.compi1.nodos.GraphBuilder
import com.cunoc.compi1.nodos.DiagramaFlujoView
import com.cunoc.compi1.nodos.Nodo
import compi.practica_1_compi.lexer.Lexer
import compi.practica_1_compi.parser.parser
import java.io.StringReader

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
                try {
                    val lexer = Lexer(StringReader(codigo))
                    val parser = parser(lexer)
                    var parseException: Exception? = null

                    try {
                        parser.parse()
                    } catch (e: Exception) {
                        parseException = e
                    }

                    val erroresLexicos = lexer.getLexicalErrors()
                    if (erroresLexicos.isNotEmpty()) {
                        inputText.error = buildDetailedErrorReport(
                            titulo = "Errores lexicos",
                            errores = erroresLexicos,
                            codigo = codigo,
                            recomendacion = "Revisa caracteres invalidos y formato de literales (cadenas, numeros y colores)."
                        )
                        return@setOnClickListener
                    }

                    val errores = parser.getSyntaxErrors()
                    if (errores.isNotEmpty()) {
                        inputText.error = buildDetailedErrorReport(
                            titulo = "Errores sintacticos",
                            errores = errores,
                            codigo = codigo,
                            recomendacion = "Verifica parentesis, palabras reservadas (INICIO/FIN, SI/FINSI, MIENTRAS/FINMIENTRAS) y separadores."
                        )
                        return@setOnClickListener
                    }

                    if (parseException != null) {
                        inputText.error = buildExceptionReport(parseException, codigo)
                        return@setOnClickListener
                    }

                    val tokens = lexer.getTokens()
                    val nodos: List<Nodo> = GraphBuilder.construir(tokens)
                    val configuraciones: Map<String, Map<Int, Configuracion>> = ConfigBuilder.construir(tokens)

                    val diagramaView = DiagramaFlujoView(this)
                    diagramaView.nodos = nodos
                    diagramaView.configuraciones = configuraciones

                    container.removeAllViews()
                    container.addView(diagramaView)

                } catch (e: Exception) {
                    inputText.error = buildExceptionReport(e, codigo)
                }

            }

            regresarButton.setOnClickListener {
                container.removeAllViews() // Limpia el diagrama
            }
        }

    private fun buildDetailedErrorReport(
        titulo: String,
        errores: List<String>,
        codigo: String,
        recomendacion: String
    ): String {
        val maxErrores = 5
        val mensaje = StringBuilder()
            .append(titulo)
            .append(" (")
            .append(errores.size)
            .append(")")

        errores.take(maxErrores).forEachIndexed { index, error ->
            mensaje.append("\n").append(index + 1).append(") ").append(error)

            val contexto = extractContext(error, codigo)
            if (contexto != null) {
                mensaje.append("\n   -> ").append(contexto)
            }
        }

        if (errores.size > maxErrores) {
            mensaje
                .append("\n...")
                .append("\nSe omitieron ")
                .append(errores.size - maxErrores)
                .append(" errores adicionales")
        }

        mensaje.append("\nSugerencia: ").append(recomendacion)
        return mensaje.toString()
    }

    private fun buildExceptionReport(e: Exception, codigo: String): String {
        val frame = e.stackTrace.firstOrNull()
        val origen = if (frame != null) {
            "${frame.className}.${frame.methodName}(${frame.fileName}:${frame.lineNumber})"
        } else {
            "No disponible"
        }

        val primeraLinea = codigo.lineSequence().firstOrNull()?.take(120) ?: "Entrada vacia"

        return StringBuilder()
            .append("Fallo interno durante el analisis")
            .append("\nTipo: ").append(e::class.java.simpleName)
            .append("\nMensaje: ").append(e.message ?: "Sin detalle")
            .append("\nOrigen: ").append(origen)
            .append("\nContexto de entrada: ").append(primeraLinea)
            .toString()
    }

    private fun extractContext(error: String, codigo: String): String? {
        val lineRegex = Regex("L[ií]nea:\\s*(\\d+)", RegexOption.IGNORE_CASE)
        val lineNumber = lineRegex.find(error)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: return null

        val lineas = codigo.lines()
        if (lineNumber <= 0 || lineNumber > lineas.size) return null

        return "Linea $lineNumber: ${lineas[lineNumber - 1].trim()}"
    }
}