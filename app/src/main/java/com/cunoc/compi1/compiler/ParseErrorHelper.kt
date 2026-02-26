package com.cunoc.compi1.compiler

object ParseErrorHelper {

    // Este método construye un reporte acotado de errores y agrega contexto de línea
    // para facilitar el diagnóstico sin saturar la salida.
    fun construirReporteDetalladoError(
        titulo: String,
        errores: List<String>,
        codigo: String,
        recomendacion: String
    ): String {
        val maxErrores = 5
        val mensaje = StringBuilder()
        mensaje.append(titulo)
        mensaje.append(" (")
        mensaje.append(errores.size)
        mensaje.append(")")

        var index = 0
        while (index < errores.size && index < maxErrores) {
            val error = errores[index]
            mensaje.append("\n").append(index + 1).append(") ").append(error)

            val contexto = extraerContexto(error, codigo)
            if (contexto != null) {
                mensaje.append("\n   -> ").append(contexto)
            }
            index++
        }

        if (errores.size > maxErrores) {
            mensaje.append("\n...")
            mensaje.append("\nSe omitieron ")
            mensaje.append(errores.size - maxErrores)
            mensaje.append(" errores adicionales")
        }

        mensaje.append("\nSugerencia: ").append(recomendacion)
        return mensaje.toString()
    }

    // Este método resume una excepción interna y conserva el origen más cercano
    // del fallo para soporte técnico.
    fun construirReporteExcepcion(e: Exception, codigo: String): String {
        val frame = if (e.stackTrace.isNotEmpty()) e.stackTrace[0] else null
        val origen = if (frame != null) {
            "${frame.className}.${frame.methodName}(${frame.fileName}:${frame.lineNumber})"
        } else {
            "No disponible"
        }

        val lineasCodigo = codigo.lines()
        var primeraLinea = "Entrada vacia"
        if (lineasCodigo.isNotEmpty()) {
            primeraLinea = lineasCodigo[0]
            if (primeraLinea.length > 120) {
                primeraLinea = primeraLinea.substring(0, 120)
            }
        }

        val mensaje = StringBuilder()
        mensaje.append("Fallo interno durante el analisis")
        mensaje.append("\nTipo: ")
        mensaje.append(e::class.java.simpleName)
        mensaje.append("\nMensaje: ")
        mensaje.append(e.message ?: "Sin detalle")
        mensaje.append("\nOrigen: ")
        mensaje.append(origen)
        mensaje.append("\nContexto de entrada: ")
        mensaje.append(primeraLinea)
        return mensaje.toString()
    }

    // Este método intenta recuperar la línea exacta asociada a un error textual
    // usando extracción por expresión regular.
    private fun extraerContexto(error: String, codigo: String): String? {
        val lineRegex = Regex("L[ií]nea:\\s*(\\d+)", RegexOption.IGNORE_CASE)
        val lineNumber = lineRegex.find(error)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: return null

        val lineas = codigo.lines()
        if (lineNumber <= 0 || lineNumber > lineas.size) return null

        return "Linea $lineNumber: ${lineas[lineNumber - 1].trim()}"
    }
}