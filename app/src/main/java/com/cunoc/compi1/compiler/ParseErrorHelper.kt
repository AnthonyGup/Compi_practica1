package com.cunoc.compi1.compiler

object ParseErrorHelper {

    // Este método construye un reporte acotado de errores y agrega contexto de línea
    // para facilitar el diagnóstico sin saturar la salida.
    fun buildDetailedErrorReport(
        titulo: String,
        errores: List<String>,
        codigo: String,
        recomendacion: String
    ): String {
        val maxErrores = 5
        val mensaje = StringBuilder()
            .append(titulo)
            .append(" (")
            .append(errores.size)
            .append(")")

        errores.take(maxErrores).forEachIndexed { index, error ->
            mensaje.append("\n").append(index + 1).append(") ").append(error)

            val contexto = extractContext(error, codigo)
            if (contexto != null) {
                mensaje.append("\n   -> ").append(contexto)
            }
        }

        if (errores.size > maxErrores) {
            mensaje
                .append("\n...")
                .append("\nSe omitieron ")
                .append(errores.size - maxErrores)
                .append(" errores adicionales")
        }

        mensaje.append("\nSugerencia: ").append(recomendacion)
        return mensaje.toString()
    }

    // Este método resume una excepción interna y conserva el origen más cercano
    // del fallo para soporte técnico.
    fun buildExceptionReport(e: Exception, codigo: String): String {
        val frame = e.stackTrace.firstOrNull()
        val origen = if (frame != null) {
            "${frame.className}.${frame.methodName}(${frame.fileName}:${frame.lineNumber})"
        } else {
            "No disponible"
        }

        val primeraLinea = codigo.lineSequence().firstOrNull()?.take(120) ?: "Entrada vacia"

        return StringBuilder()
            .append("Fallo interno durante el analisis")
            .append("\nTipo: ").append(e::class.java.simpleName)
            .append("\nMensaje: ").append(e.message ?: "Sin detalle")
            .append("\nOrigen: ").append(origen)
            .append("\nContexto de entrada: ").append(primeraLinea)
            .toString()
    }

    // Este método intenta recuperar la línea exacta asociada a un error textual
    // usando extracción por expresión regular.
    private fun extractContext(error: String, codigo: String): String? {
        val lineRegex = Regex("L[ií]nea:\\s*(\\d+)", RegexOption.IGNORE_CASE)
        val lineNumber = lineRegex.find(error)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: return null

        val lineas = codigo.lines()
        if (lineNumber <= 0 || lineNumber > lineas.size) return null

        return "Linea $lineNumber: ${lineas[lineNumber - 1].trim()}"
    }
}