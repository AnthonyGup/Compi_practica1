package com.cunoc.compi1.compiler

import com.cunoc.compi1.nodos.ConfigBuilder
import com.cunoc.compi1.nodos.Configuracion
import com.cunoc.compi1.nodos.GraphBuilder
import com.cunoc.compi1.nodos.Nodo
import compi.practica_1_compi.lexer.Lexer
import compi.practica_1_compi.parser.parser
import compi.practica_1_compi.parser.sym
import java.io.StringReader
import java_cup.runtime.Symbol

object DiagramAnalyzer {

    sealed class Result {
        data class Success(
            val nodos: List<Nodo>,
            val configuraciones: Map<String, Map<Int, Configuracion>>
        ) : Result()

        data class Error(val message: String) : Result()
    }

    fun analyze(codigo: String): Result {
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
                return Result.Error(ParseErrorHelper.buildDetailedErrorReport(
                    titulo = "Errores lexicos",
                    errores = lexicalErrors,
                    codigo = codigo,
                    recomendacion = "Revisa caracteres invalidos y formato de literales (cadenas, numeros y colores)."
                )
                )
            }

            val syntaxErrors = parser.getSyntaxErrors()
            val tokens = lexer.getTokens()
            val useLegacyCompatibility = canBypassLegacyParser(syntaxErrors, tokens)

            if (syntaxErrors.isNotEmpty() && !useLegacyCompatibility) {
                return Result.Error(ParseErrorHelper.buildDetailedErrorReport(
                    titulo = "Errores sintacticos",
                    errores = syntaxErrors,
                    codigo = codigo,
                    recomendacion = "Verifica parentesis, palabras reservadas (INICIO/FIN, SI/FINSI, MIENTRAS/FINMIENTRAS) y separadores."
                )
                )
            }

            if (parseException != null && !useLegacyCompatibility)
                return Result.Error(ParseErrorHelper.buildExceptionReport(parseException, codigo))

            val nodos = GraphBuilder.construir(tokens)
            val configuraciones = ConfigBuilder.construir(tokens)
            Result.Success(nodos, configuraciones)
        } catch (e: Exception) {
            Result.Error(ParseErrorHelper.buildExceptionReport(e, codigo))
        }
    }

    private fun canBypassLegacyParser(
        syntaxErrors: List<String>,
        tokens: List<Symbol>
    ): Boolean {
        if (syntaxErrors.isEmpty()) return false

        val comparisonSymbols = setOf(
            sym.IGUAL,
            sym.DIFERENTE,
            sym.MAYOR,
            sym.MENOR,
            sym.MAYOR_IGUAL,
            sym.MENOR_IGUAL,
            sym.AND,
            sym.OR,
            sym.NOT
        )

        val hasComparisonToken = tokens.any { token -> token.sym in comparisonSymbols }
        val configKeys = setOf(
            sym.FIGURA_SI,
            sym.FIGURA_MIENTRAS,
            sym.FIGURA_BLOQUE,
            sym.LETRA_SI,
            sym.LETRA_MIENTRAS,
            sym.LETRA_BLOQUE
        )

        val configLiteralSymbols = setOf(
            sym.ELIPSE,
            sym.CIRCULO,
            sym.PARALELOGRAMO,
            sym.RECTANGULO,
            sym.ROMBO,
            sym.RECTANGULO_REDONDEADO,
            sym.ARIAL,
            sym.TIMES_NEW_ROMAN,
            sym.COMIC_SANS,
            sym.VERDANA
        )

        val hasConfigLiteralToken = tokens.any { token -> token.sym in configLiteralSymbols }
            && tokens.any { token -> token.sym in configKeys }

        if (!hasComparisonToken && !hasConfigLiteralToken) return false

        val relatedMarkers = listOf(
            "Token inesperado: '>'",
            "Token inesperado: '<'",
            "Token inesperado: '>='",
            "Token inesperado: '<='",
            "Token inesperado: '!='",
            "Token inesperado: '&&'",
            "Token inesperado: '||'",
            "Token inesperado: '!'",
            "Token inesperado: 'IGUAL'",
            "Token inesperado: 'DIFERENTE'",
            "Token inesperado: 'MAYOR'",
            "Token inesperado: 'MENOR'",
            "Token inesperado: 'MAYOR_IGUAL'",
            "Token inesperado: 'MENOR_IGUAL'",
            "Token inesperado: 'ELIPSE'",
            "Token inesperado: 'CIRCULO'",
            "Token inesperado: 'PARALELOGRAMO'",
            "Token inesperado: 'RECTANGULO'",
            "Token inesperado: 'ROMBO'",
            "Token inesperado: 'RECTANGULO_REDONDEADO'",
            "Token inesperado: 'ARIAL'",
            "Token inesperado: 'TIMES_NEW_ROMAN'",
            "Token inesperado: 'COMIC_SANS'",
            "Token inesperado: 'VERDANA'"
        )

        return syntaxErrors.all { err ->
            relatedMarkers.any { marker -> err.contains(marker, ignoreCase = true) }
        }
    }
}