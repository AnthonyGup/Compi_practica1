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
                    if (i + 1 >= tokens.size) {
                        i++
                        continue
                    }
                    val nombreToken = tokens[i + 1]

                    val nombre = nombreToken.value.toString()
                    var valor: String? = null

                    if (i + 3 < tokens.size && tokens[i + 2].sym == sym.ASIGNACION) {
                        val exprEnd = encontrarFinExpresion(tokens, i + 3)
                        valor = tokensATexto(tokens, i + 3, exprEnd)
                        i = exprEnd - 1
                    } else {
                        i += 1
                    }

                    nodos.add(DeclaracionNodo(nombre, valor))
                }

                sym.ID -> {
                    if (i + 2 < tokens.size && tokens[i + 1].sym == sym.ASIGNACION) {
                        val nombre = tokens[i].value.toString()
                        val exprEnd = encontrarFinExpresion(tokens, i + 2)
                        val expr = tokensATexto(tokens, i + 2, exprEnd)
                        nodos.add(AsignacionNodo(nombre, expr))
                        i = exprEnd - 1
                    }
                }

                sym.MOSTRAR -> {
                    var mensaje = ""
                    if (i + 1 < tokens.size) {
                        mensaje = tokens[i + 1].value?.toString() ?: ""
                    }
                    nodos.add(MostrarNodo(mensaje))
                    i++
                }

                sym.LEER -> {
                    var variable = ""
                    if (i + 1 < tokens.size) {
                        variable = tokens[i + 1].value?.toString() ?: ""
                    }
                    nodos.add(LeerNodo(variable))
                    i++
                }

                sym.SI -> {
                    val condicion = leerCondicion(tokens, i)
                    val bodyStart = indiceDespues(tokens, i, sym.ENTONCES)
                    val bodyEnd = encontrarFinBloque(tokens, bodyStart, sym.SI, sym.FINSI)

                    val bloque = if (bodyStart in 0 until bodyEnd && bodyEnd <= tokens.size) {
                        construir(tokens.subList(bodyStart, bodyEnd))
                    } else {
                        emptyList()
                    }

                    nodos.add(SiNodo(condicion, bloque))
                    i = if (bodyEnd < tokens.size) bodyEnd else i
                }

                sym.MIENTRAS -> {
                    val condicion = leerCondicion(tokens, i)
                    val bodyStart = indiceDespues(tokens, i, sym.HACER)
                    val bodyEnd = encontrarFinBloque(tokens, bodyStart, sym.MIENTRAS, sym.FINMIENTRAS)

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
    private fun leerCondicion(tokens: List<Symbol>, start: Int): String {
        val open = indiceDespues(tokens, start, sym.PAREN_IZQ)
        if (open == -1) return ""

        var close = open + 1
        while (close < tokens.size && tokens[close].sym != sym.PAREN_DER) {
            close++
        }
        if (close >= tokens.size) return ""

        return tokensATexto(tokens, open + 1, close)
    }

    // Este método localiza la primera aparición del símbolo objetivo y retorna
    // la posición inmediata siguiente para facilitar cortes de sublistas.
    private fun indiceDespues(tokens: List<Symbol>, start: Int, targetSym: Int): Int {
        var i = start
        while (i < tokens.size) {
            if (tokens[i].sym == targetSym) return i + 1
            i++
        }
        return -1
    }

    // Este método calcula el cierre de un bloque soportando anidación de estructuras
    // del mismo tipo mediante un contador de profundidad.
    private fun encontrarFinBloque(
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
    private fun encontrarFinExpresion(tokens: List<Symbol>, from: Int): Int {
        if (from !in tokens.indices) return from

        var i = from
        while (i < tokens.size) {
            if (esLimiteExpresion(tokens, i, from)) {
                return i
            }
            i++
        }
        return tokens.size
    }

    // Este método define la regla de frontera de expresión para evitar consumir
    // tokens que pertenecen a la siguiente sentencia.
    private fun esLimiteExpresion(tokens: List<Symbol>, current: Int, exprStart: Int): Boolean {
        if (current < exprStart || current >= tokens.size) return false

        val statementStarters = intArrayOf(
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

        for (starter in statementStarters) {
            if (tokens[current].sym == starter) {
                return true
            }
        }

        if (current > exprStart &&
            tokens[current].sym == sym.ID &&
            current + 1 < tokens.size &&
            tokens[current + 1].sym == sym.ASIGNACION
        ) {
            return true
        }

        return false
    }

    private fun tokensATexto(tokens: List<Symbol>, start: Int, end: Int): String {
        val text = StringBuilder()
        var i = start
        while (i < end && i < tokens.size) {
            if (text.isNotEmpty()) {
                text.append(" ")
            }
            val value = tokens[i].value?.toString() ?: ""
            text.append(value)
            i++
        }
        return text.toString().trim()
    }
}
