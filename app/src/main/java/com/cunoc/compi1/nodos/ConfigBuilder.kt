package com.cunoc.compi1.nodos

import android.graphics.Color
import compi.practica_1_compi.parser.sym
import java_cup.runtime.Symbol

data class Configuracion(val tipo: String, val valor: Any, val indice: Int)

object ConfigBuilder {
    fun construir(tokens: List<Symbol>): Map<String, Map<Int, Configuracion>> {
        val configs = mutableMapOf<String, MutableMap<Int, Configuracion>>()
        var i = 0

        while (i < tokens.size) {
            val token = tokens[i]
            when (token.sym) {
                sym.DEFAULT -> {
                    val targetIndex = parseDefaultIndex(tokens, i)
                    if (targetIndex != null) {
                        putConfig(configs, "DEFAULT", targetIndex, targetIndex)
                        i += 3
                        continue
                    }
                }

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
                sym.LETRA_SIZE_BLOQUE -> {
                    val parsed = parseIndexedConfig(tokens, i, token.sym)
                    if (parsed != null) {
                        putConfig(configs, parsed.key, parsed.indice, parsed.valor)
                        i = parsed.nextIndex
                        continue
                    }
                }
            }
            i++
        }

        return configs
    }

    private data class ParsedConfig(
        val key: String,
        val valor: Any,
        val indice: Int,
        val nextIndex: Int
    )

    private fun putConfig(
        configs: MutableMap<String, MutableMap<Int, Configuracion>>,
        key: String,
        indice: Int,
        valor: Any
    ) {
        val perIndex = configs.getOrPut(key) { mutableMapOf() }
        perIndex[indice] = Configuracion(key, valor, indice)
    }

    private fun parseDefaultIndex(tokens: List<Symbol>, start: Int): Int? {
        if (start + 2 >= tokens.size) return null
        if (tokens[start + 1].sym != sym.ASIGNACION) return null
        return tokens[start + 2].value?.toString()?.toIntOrNull()
    }

    private fun parseIndexedConfig(tokens: List<Symbol>, start: Int, keySym: Int): ParsedConfig? {
        if (start + 4 >= tokens.size) return null
        if (tokens[start + 1].sym != sym.ASIGNACION) return null

        var barIndex = -1
        var j = start + 2
        while (j < tokens.size) {
            if (tokens[j].sym == sym.BARRA) {
                barIndex = j
                break
            }
            j++
        }
        if (barIndex == -1 || barIndex + 1 >= tokens.size) return null
        if (tokens[barIndex + 1].sym != sym.ENTERO) return null

        val indice = tokens[barIndex + 1].value?.toString()?.toIntOrNull() ?: return null
        val rawValue = tokens.subList(start + 2, barIndex)
            .joinToString("") { it.value?.toString() ?: "" }
            .trim()
        if (rawValue.isEmpty()) return null

        val key = keyName(keySym)
        val valor = parseValueByKey(keySym, rawValue)

        return ParsedConfig(
            key = key,
            valor = valor,
            indice = indice,
            nextIndex = barIndex + 2
        )
    }

    private fun keyName(keySym: Int): String {
        return when (keySym) {
            sym.COLOR_TEXTO_SI -> "COLOR_TEXTO_SI"
            sym.COLOR_SI -> "COLOR_SI"
            sym.FIGURA_SI -> "FIGURA_SI"
            sym.LETRA_SI -> "LETRA_SI"
            sym.LETRA_SIZE_SI -> "LETRA_SIZE_SI"
            sym.COLOR_TEXTO_MIENTRAS -> "COLOR_TEXTO_MIENTRAS"
            sym.COLOR_MIENTRAS -> "COLOR_MIENTRAS"
            sym.FIGURA_MIENTRAS -> "FIGURA_MIENTRAS"
            sym.LETRA_MIENTRAS -> "LETRA_MIENTRAS"
            sym.LETRA_SIZE_MIENTRAS -> "LETRA_SIZE_MIENTRAS"
            sym.COLOR_TEXTO_BLOQUE -> "COLOR_TEXTO_BLOQUE"
            sym.COLOR_BLOQUE -> "COLOR_BLOQUE"
            sym.FIGURA_BLOQUE -> "FIGURA_BLOQUE"
            sym.LETRA_BLOQUE -> "LETRA_BLOQUE"
            sym.LETRA_SIZE_BLOQUE -> "LETRA_SIZE_BLOQUE"
            else -> ""
        }
    }

    private fun parseValueByKey(keySym: Int, rawValue: String): Any {
        return when (keySym) {
            sym.COLOR_TEXTO_SI,
            sym.COLOR_SI,
            sym.COLOR_TEXTO_MIENTRAS,
            sym.COLOR_MIENTRAS,
            sym.COLOR_TEXTO_BLOQUE,
            sym.COLOR_BLOQUE -> parseColor(rawValue)

            sym.LETRA_SIZE_SI,
            sym.LETRA_SIZE_MIENTRAS,
            sym.LETRA_SIZE_BLOQUE -> rawValue.toFloatOrNull() ?: 40f

            else -> rawValue
        }
    }

    private fun parseColor(rawValue: String): Int {
        val str = rawValue.trim().replace(" ", "")
        return try {
            when {
                str.startsWith("H", ignoreCase = true) && str.length > 1 -> {
                    Color.parseColor("#" + str.substring(1))
                }

                str.contains(",") -> {
                    val parts = str.split(",")
                    if (parts.size != 3) return Color.BLACK
                    val r = parts[0].toIntOrNull()?.coerceIn(0, 255) ?: return Color.BLACK
                    val g = parts[1].toIntOrNull()?.coerceIn(0, 255) ?: return Color.BLACK
                    val b = parts[2].toIntOrNull()?.coerceIn(0, 255) ?: return Color.BLACK
                    Color.rgb(r, g, b)
                }

                else -> Color.parseColor(str)
            }
        } catch (_: IllegalArgumentException) {
            Color.BLACK
        }
    }

}
