package com.cunoc.compi1.compiler

import com.cunoc.compi1.nodos.Configuracion
import com.cunoc.compi1.nodos.Nodo

// Esta jerarquía representa el contrato de salida del proceso de análisis.
sealed class AnalyzerResult

// Esta clase encapsula un resultado exitoso con nodos listos para dibujar
// y configuraciones ya indexadas por clave.
data class AnalyzerSuccess(
    val nodos: List<Nodo>,
    val configuraciones: Map<String, Map<Int, Configuracion>>
) : AnalyzerResult()

// Esta clase encapsula un resultado fallido con un mensaje listo para mostrar en UI.
data class AnalyzerError(val message: String) : AnalyzerResult()
