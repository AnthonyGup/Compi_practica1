package com.cunoc.compi1.compiler

import com.cunoc.compi1.nodos.ConfigBuilder
import com.cunoc.compi1.nodos.GraphBuilder
import compi.practica_1_compi.lexer.Lexer
import compi.practica_1_compi.parser.parser
import compi.practica_1_compi.parser.sym
import java.io.StringReader
import java_cup.runtime.Symbol

object DiagramAnalyzer {

    // Este método coordina el ciclo completo de análisis: parseo, validación de errores,
    // construcción del grafo de nodos y extracción de configuraciones.
    fun analyze(codigo: String): AnalyzerResult {
        return try {
            val lexer = Lexer(StringReader(codigo))
            val parser = parser(lexer)
            var parseException: Exception? = null

            try {
                parser.parse()
            } catch (e: Exception) {
                parseException = e
            }

            val lexicalErrors = lexer.getLexicalErrors()
            if (lexicalErrors.isNotEmpty()) {
                return AnalyzerError(ParseErrorHelper.buildDetailedErrorReport(
                    titulo = "Errores lexicos",
                    errores = lexicalErrors,
                    codigo = codigo,
                    recomendacion = "Revisa caracteres invalidos y formato de literales (cadenas, numeros y colores)."
                )
                )
            }

            val syntaxErrors = parser.getSyntaxErrors()
            val tokens = lexer.getTokens()
            val canBypassColorSyntax = canBypassColorSyntaxErrors(syntaxErrors, tokens)

            if (syntaxErrors.isNotEmpty() && !canBypassColorSyntax) {
                return AnalyzerError(ParseErrorHelper.buildDetailedErrorReport(
                    titulo = "Errores sintacticos",
                    errores = syntaxErrors,
                    codigo = codigo,
                    recomendacion = "Verifica parentesis, palabras reservadas (INICIO/FIN, SI/FINSI, MIENTRAS/FINMIENTRAS) y separadores."
                )
                )
            }

            if (parseException != null && !canBypassColorSyntax)
                return AnalyzerError(ParseErrorHelper.buildExceptionReport(parseException, codigo))

            val nodos = GraphBuilder.construir(tokens)
            val configuraciones = ConfigBuilder.construir(tokens)
            AnalyzerSuccess(nodos, configuraciones)
        } catch (e: Exception) {
            AnalyzerError(ParseErrorHelper.buildExceptionReport(e, codigo))
        }
    }

    // Este método habilita una compatibilidad temporal cuando los únicos errores
    // sintácticos corresponden a comas en configuraciones de color RGB.
    private fun canBypassColorSyntaxErrors(
        syntaxErrors: List<String>,
        tokens: List<Symbol>
    ): Boolean {
        if (syntaxErrors.isEmpty()) return false

        val colorConfigSymbols = setOf(
            sym.COLOR_TEXTO_SI,
            sym.COLOR_SI,
            sym.COLOR_TEXTO_MIENTRAS,
            sym.COLOR_MIENTRAS,
            sym.COLOR_TEXTO_BLOQUE,
            sym.COLOR_BLOQUE
        )

        val hasColorConfig = tokens.any { it.sym in colorConfigSymbols }
        val hasComma = tokens.any { it.sym == sym.COMA }
        if (!hasColorConfig || !hasComma) return false

        val allowedMarkers = listOf(
            "Token inesperado: ','",
            "Token inesperado: 'COMA'"
        )

        return syntaxErrors.all { err ->
            allowedMarkers.any { marker -> err.contains(marker, ignoreCase = true) }
        }
    }
}