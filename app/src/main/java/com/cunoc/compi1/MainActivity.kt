package com.cunoc.compi1

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import compi.practica_1_compi.lexer.Lexer
import compi.practica_1_compi.parser.sym
import java.io.StringReader

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputText = findViewById<EditText>(R.id.inputText)
        val analyzeButton = findViewById<Button>(R.id.analyzeButton)

        analyzeButton.setOnClickListener {
            val codigo = inputText.text.toString()

            if (codigo.isBlank()) {
                inputText.error = "Ingrese pseudocódigo"
                return@setOnClickListener
            }

            colorearCodigo(inputText)
        }
    }

    private fun colorearCodigo(editText: EditText) {
        val texto = editText.text.toString()
        val spannable = SpannableString(texto)

        val lexer = Lexer(StringReader(texto))
        var token = lexer.next_token()

        while (token.sym != sym.EOF) {
            val start = token.left   // ahora son offsets absolutos
            val end = token.right

            if (start >= 0 && end <= texto.length && end > start) {
                val color = when (token.sym) {
                    sym.INICIO, sym.FIN, sym.VAR, sym.SI, sym.ENTONCES,
                    sym.FINSI, sym.MIENTRAS, sym.HACER, sym.FINMIENTRAS,
                    sym.MOSTRAR, sym.LEER -> Color.BLUE

                    sym.MAS, sym.MENOS, sym.POR, sym.DIV,
                    sym.IGUAL, sym.DIFERENTE, sym.MAYOR,
                    sym.MENOR, sym.MAYOR_IGUAL, sym.MENOR_IGUAL,
                    sym.AND, sym.OR, sym.NOT, sym.ASIGNACION -> Color.MAGENTA

                    sym.ENTERO, sym.DECIMAL -> Color.rgb(0, 150, 0)
                    else -> Color.BLACK
                }

                spannable.setSpan(
                    ForegroundColorSpan(color),
                    start,
                    end,
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            token = lexer.next_token()
        }

        // Colorear errores léxicos en rojo (estos siguen viniendo con línea/columna)
        lexer.getLexicalErrors().forEach { error ->
            val linea = Regex("Línea: (\\d+)").find(error)?.groupValues?.get(1)?.toInt() ?: return@forEach
            val columna = Regex("Columna: (\\d+)").find(error)?.groupValues?.get(1)?.toInt() ?: return@forEach
            val index = calcularIndice(texto, linea, columna)
            if (index in texto.indices) {
                spannable.setSpan(
                    ForegroundColorSpan(Color.RED),
                    index,
                    index + 1,
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        editText.text = Editable.Factory.getInstance().newEditable(spannable)
    }



    private fun calcularIndice(texto: String, linea: Int, columna: Int): Int {
        var l = 1
        var c = 1

        for (i in texto.indices) {
            if (l == linea && c == columna) return i

            if (texto[i] == '\n') {
                l++
                c = 1
            } else {
                c++
            }
        }
        return -1
    }
}