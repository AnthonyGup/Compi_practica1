package com.cunoc.compi1.nodos

import java_cup.runtime.Symbol
import compi.practica_1_compi.parser.sym

object GraphBuilder {

    fun construir(tokens: List<Symbol>): List<Nodo> {
        val nodos = mutableListOf<Nodo>()
        var i = 0

        while (i < tokens.size) {
            when (tokens[i].sym) {
                sym.INICIO -> nodos.add(InicioNodo(i))
                sym.FIN -> nodos.add(FinNodo(i))

                sym.VAR -> {
                    val nombreToken = tokens.getOrNull(i + 1)
                    if (nombreToken == null) {
                        i++
                        continue
                    }

                    val nombre = nombreToken.value.toString()
                    var valor: String? = null

                    if (i + 3 < tokens.size && tokens[i + 2].sym == sym.ASIGNACION) {
                        valor = tokens[i + 3].value?.toString()
                        i += 3
                    } else {
                        i += 1
                    }

                    nodos.add(DeclaracionNodo(nombre, valor))
                }

                sym.ID -> {
                    if (i + 2 < tokens.size && tokens[i + 1].sym == sym.ASIGNACION) {
                        val nombre = tokens[i].value.toString()
                        val expr = tokens[i + 2].value?.toString() ?: ""
                        nodos.add(AsignacionNodo(nombre, expr))
                        i += 2
                    }
                }

                sym.MOSTRAR -> {
                    val mensaje = tokens.getOrNull(i + 1)?.value?.toString() ?: ""
                    nodos.add(MostrarNodo(mensaje))
                    i++
                }

                sym.LEER -> {
                    val variable = tokens.getOrNull(i + 1)?.value?.toString() ?: ""
                    nodos.add(LeerNodo(variable))
                    i++
                }

                sym.SI -> {
                    val condicion = readCondition(tokens, i)
                    val bodyStart = indexAfter(tokens, i, sym.ENTONCES)
                    val bodyEnd = findBlockEnd(tokens, bodyStart, sym.SI, sym.FINSI)

                    val bloque = if (bodyStart in 0 until bodyEnd && bodyEnd <= tokens.size) {
                        construir(tokens.subList(bodyStart, bodyEnd))
                    } else {
                        emptyList()
                    }

                    nodos.add(SiNodo(condicion, bloque))
                    i = if (bodyEnd < tokens.size) bodyEnd else i
                }

                sym.MIENTRAS -> {
                    val condicion = readCondition(tokens, i)
                    val bodyStart = indexAfter(tokens, i, sym.HACER)
                    val bodyEnd = findBlockEnd(tokens, bodyStart, sym.MIENTRAS, sym.FINMIENTRAS)

                    val bloque = if (bodyStart in 0 until bodyEnd && bodyEnd <= tokens.size) {
                        construir(tokens.subList(bodyStart, bodyEnd))
                    } else {
                        emptyList()
                    }

                    nodos.add(MientrasNodo(condicion, bloque))
                    i = if (bodyEnd < tokens.size) bodyEnd else i
                }
            }
            i++
        }
        return nodos
    }

    private fun readCondition(tokens: List<Symbol>, start: Int): String {
        val open = indexAfter(tokens, start, sym.PAREN_IZQ)
        if (open == -1) return ""

        var close = open + 1
        while (close < tokens.size && tokens[close].sym != sym.PAREN_DER) {
            close++
        }
        if (close >= tokens.size) return ""

        return tokens.subList(open + 1, close)
            .joinToString(" ") { it.value?.toString() ?: "" }
            .trim()
    }

    private fun indexAfter(tokens: List<Symbol>, start: Int, targetSym: Int): Int {
        var i = start
        while (i < tokens.size) {
            if (tokens[i].sym == targetSym) return i + 1
            i++
        }
        return -1
    }

    private fun findBlockEnd(
        tokens: List<Symbol>,
        from: Int,
        openSym: Int,
        closeSym: Int
    ): Int {
        if (from !in tokens.indices) return tokens.size

        var depth = 0
        var i = from
        while (i < tokens.size) {
            when (tokens[i].sym) {
                openSym -> depth++
                closeSym -> {
                    if (depth == 0) return i
                    depth--
                }
            }
            i++
        }
        return tokens.size
    }
}
