package com.cunoc.compi1.compiler

import compi.practica_1_compi.lexer.Lexer
import compi.practica_1_compi.parser.parser
import java.io.StringReader

object Compiler {

    data class Result(
        val lexicalErrors: List<String>,
        val syntaxErrors: List<String>
    )

    fun compile(input: String): Result {

        val lexer = Lexer(StringReader(input))
        val parser = parser(lexer)

        try {
            parser.parse()
        } catch (_: Exception) {
            // CUP puede lanzar excepción aunque ya haya guardado errores
        }

        return Result(
            lexicalErrors = lexer.getLexicalErrors(),
            syntaxErrors = parser.getSyntaxErrors()
        )
    }
}