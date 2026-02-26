package com.cunoc.compi1.nodos

import java_cup.runtime.Symbol
import compi.practica_1_compi.parser.sym

object GraphBuilder {

    // Este método recorre secuencialmente los tokens y traduce cada instrucción
    // reconocida a su representación intermedia como nodo.
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
                        val exprEnd = findExpressionEnd(tokens, i + 3)
                        valor = tokens.subList(i + 3, exprEnd)
                            .joinToString(" ") { it.value?.toString() ?: "" }
                            .trim()
                        i = exprEnd - 1
                    } else {
                        i += 1
                    }

                    nodos.add(DeclaracionNodo(nombre, valor))
                }

                sym.ID -> {
                    if (i + 2 < tokens.size && tokens[i + 1].sym == sym.ASIGNACION) {
                        val nombre = tokens[i].value.toString()
                        val exprEnd = findExpressionEnd(tokens, i + 2)
                        val expr = tokens.subList(i + 2, exprEnd)
                            .joinToString(" ") { it.value?.toString() ?: "" }
                            .trim()
                        nodos.add(AsignacionNodo(nombre, expr))
                        i = exprEnd - 1
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

    // Este método extrae el texto de la condición delimitada por paréntesis,
    // preservando el orden original de los tokens internos.
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

    // Este método localiza la primera aparición del símbolo objetivo y retorna
    // la posición inmediata siguiente para facilitar cortes de sublistas.
    private fun indexAfter(tokens: List<Symbol>, start: Int, targetSym: Int): Int {
        var i = start
        while (i < tokens.size) {
            if (tokens[i].sym == targetSym) return i + 1
            i++
        }
        return -1
    }

    // Este método calcula el cierre de un bloque soportando anidación de estructuras
    // del mismo tipo mediante un contador de profundidad.
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

    // Este método determina dónde finaliza una expresión lineal antes del inicio
    // de la siguiente instrucción o sección de configuración.
    private fun findExpressionEnd(tokens: List<Symbol>, from: Int): Int {
        if (from !in tokens.indices) return from

        var i = from
        while (i < tokens.size) {
            if (isExpressionBoundary(tokens, i, from)) {
                return i
            }
            i++
        }
        return tokens.size
    }

    // Este método define la regla de frontera de expresión para evitar consumir
    // tokens que pertenecen a la siguiente sentencia.
    private fun isExpressionBoundary(tokens: List<Symbol>, current: Int, exprStart: Int): Boolean {
        if (current < exprStart || current >= tokens.size) return false

        val statementStarters = setOf(
            sym.INICIO,
            sym.FIN,
            sym.VAR,
            sym.MOSTRAR,
            sym.LEER,
            sym.SI,
            sym.MIENTRAS,
            sym.FINSI,
            sym.FINMIENTRAS,
            sym.SEPARADOR,
            sym.DEFAULT,
            sym.COLOR_TEXTO_SI,
            sym.COLOR_SI,
            sym.FIGURA_SI,
            sym.LETRA_SI,
            sym.LETRA_SIZE_SI,
            sym.COLOR_TEXTO_MIENTRAS,
            sym.COLOR_MIENTRAS,
            sym.FIGURA_MIENTRAS,
            sym.LETRA_MIENTRAS,
            sym.LETRA_SIZE_MIENTRAS,
            sym.COLOR_TEXTO_BLOQUE,
            sym.COLOR_BLOQUE,
            sym.FIGURA_BLOQUE,
            sym.LETRA_BLOQUE,
            sym.LETRA_SIZE_BLOQUE
        )

        if (tokens[current].sym in statementStarters) return true

        if (current > exprStart &&
            tokens[current].sym == sym.ID &&
            current + 1 < tokens.size &&
            tokens[current + 1].sym == sym.ASIGNACION
        ) {
            return true
        }

        return false
    }
}
