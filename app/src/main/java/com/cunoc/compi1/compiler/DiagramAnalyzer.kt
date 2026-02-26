package com.cunoc.compi1.compiler

import com.cunoc.compi1.nodos.ConfigBuilder
import com.cunoc.compi1.nodos.GraphBuilder
import compi.practica_1_compi.lexer.Lexer
import compi.practica_1_compi.parser.parser
import java.io.StringReader

object DiagramAnalyzer {

    // Este método coordina el ciclo completo de análisis: parseo, validación de errores,
    // construcción del grafo de nodos y extracción de configuraciones.
    fun analizar(codigo: String): AnalyzerResult {
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
                return AnalyzerError(ParseErrorHelper.construirReporteDetalladoError(
                    titulo = "Errores lexicos",
                    errores = lexicalErrors,
                    codigo = codigo,
                    recomendacion = "Revisa caracteres invalidos y formato de literales (cadenas, numeros y colores)."
                )
                )
            }

            val syntaxErrors = parser.getSyntaxErrors()
            val tokens = lexer.getTokens()

            if (syntaxErrors.isNotEmpty()) {
                return AnalyzerError(ParseErrorHelper.construirReporteDetalladoError(
                    titulo = "Errores sintacticos",
                    errores = syntaxErrors,
                    codigo = codigo,
                    recomendacion = "Verifica parentesis, palabras reservadas (INICIO/FIN, SI/FINSI, MIENTRAS/FINMIENTRAS) y separadores."
                )
                )
            }

            if (parseException != null)
                return AnalyzerError(ParseErrorHelper.construirReporteExcepcion(parseException, codigo))

            val nodos = GraphBuilder.construir(tokens)
            val configuraciones = ConfigBuilder.construir(tokens)
            AnalyzerSuccess(nodos, configuraciones)
        } catch (e: Exception) {
            AnalyzerError(ParseErrorHelper.construirReporteExcepcion(e, codigo))
        }
    }
}