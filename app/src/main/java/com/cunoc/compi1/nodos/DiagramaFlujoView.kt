package com.cunoc.compi1.nodos

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.view.View

// Esta vista renderiza el diagrama de flujo aplicando estilos por índice de nodo.
class DiagramaFlujoView(context: Context) : View(context) {

    var nodos: List<Nodo> = emptyList()
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    var configuraciones: Map<String, Map<Int, Configuracion>> = emptyMap()
        set(value) {
            field = value
            invalidate()
        }

    private val shapePaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 40f
        style = Paint.Style.FILL
        strokeWidth = 1f
    }

    private val topPadding = 100
    private val nodeHeight = 100
    private val nodeSpacing = 50
    private val bottomPadding = 100

    // Este método calcula el alto requerido para mostrar todos los nodos
    // y habilita desplazamiento vertical cuando excede el espacio visible.
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val contentHeight = topPadding + (nodos.size * (nodeHeight + nodeSpacing)) + bottomPadding
        val resolvedHeight = resolveSize(contentHeight, heightMeasureSpec)
        setMeasuredDimension(width, resolvedHeight)
    }

    // Este método dibuja cada nodo en orden vertical y aplica configuración
    // tipográfica, color y figura de acuerdo con la clave correspondiente.
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var y = 100f

        for ((i, nodo) in nodos.withIndex()) {
            when (nodo) {
                is InicioNodo -> {
                    aplicarDefault(i + 1)
                    canvas.drawOval(100f, y, 400f, y + 100f, shapePaint)
                    aplicarConfig("COLOR_TEXTO_BLOQUE", i + 1) { textPaint.color = it as Int }
                    canvas.drawText("INICIO", 200f, y + 60f, textPaint)
                }

                is FinNodo -> {
                    aplicarDefault(i + 1)
                    canvas.drawOval(100f, y, 400f, y + 100f, shapePaint)
                    aplicarConfig("COLOR_TEXTO_BLOQUE", i + 1) { textPaint.color = it as Int }
                    canvas.drawText("FIN", 220f, y + 60f, textPaint)
                }

                is DeclaracionNodo -> {
                    aplicarDefault(i + 1)
                    aplicarConfig("COLOR_BLOQUE", i + 1) { shapePaint.color = it as Int }
                    aplicarConfig("LETRA_SIZE_BLOQUE", i + 1) { textPaint.textSize = it as Float }
                    aplicarConfig("LETRA_BLOQUE", i + 1) { setTypeface(it.toString()) }
                    dibujarFiguraBloque(canvas, y, obtenerFigura("FIGURA_BLOQUE", i + 1, "RECTANGULO"))
                    aplicarConfig("COLOR_TEXTO_BLOQUE", i + 1) { textPaint.color = it as Int }

                    canvas.drawText(
                        "VAR ${nodo.nombre} = ${nodo.valor ?: ""}",
                        120f,
                        y + 60f,
                        textPaint
                    )
                }

                is AsignacionNodo -> {
                    aplicarDefault(i + 1)
                    aplicarConfig("COLOR_BLOQUE", i + 1) { shapePaint.color = it as Int }
                    aplicarConfig("LETRA_SIZE_BLOQUE", i + 1) { textPaint.textSize = it as Float }
                    aplicarConfig("LETRA_BLOQUE", i + 1) { setTypeface(it.toString()) }
                    dibujarFiguraBloque(canvas, y, obtenerFigura("FIGURA_BLOQUE", i + 1, "RECTANGULO"))
                    aplicarConfig("COLOR_TEXTO_BLOQUE", i + 1) { textPaint.color = it as Int }

                    canvas.drawText("${nodo.nombre} = ${nodo.expresion}", 120f, y + 60f, textPaint)
                }

                is MostrarNodo -> {
                    aplicarDefault(i + 1)
                    aplicarConfig("COLOR_BLOQUE", i + 1) { shapePaint.color = it as Int }
                    aplicarConfig("LETRA_SIZE_BLOQUE", i + 1) { textPaint.textSize = it as Float }
                    aplicarConfig("LETRA_BLOQUE", i + 1) { setTypeface(it.toString()) }
                    dibujarFiguraBloque(canvas, y, obtenerFigura("FIGURA_BLOQUE", i + 1, "RECTANGULO"))
                    aplicarConfig("COLOR_TEXTO_BLOQUE", i + 1) { textPaint.color = it as Int }

                    canvas.drawText("MOSTRAR ${nodo.mensaje}", 120f, y + 60f, textPaint)
                }

                is LeerNodo -> {
                    aplicarDefault(i + 1)
                    aplicarConfig("COLOR_BLOQUE", i + 1) { shapePaint.color = it as Int }
                    aplicarConfig("LETRA_SIZE_BLOQUE", i + 1) { textPaint.textSize = it as Float }
                    aplicarConfig("LETRA_BLOQUE", i + 1) { setTypeface(it.toString()) }
                    dibujarFiguraBloque(canvas, y, obtenerFigura("FIGURA_BLOQUE", i + 1, "RECTANGULO"))
                    aplicarConfig("COLOR_TEXTO_BLOQUE", i + 1) { textPaint.color = it as Int }

                    canvas.drawText("LEER ${nodo.variable}", 120f, y + 60f, textPaint)
                }

                is SiNodo -> {
                    aplicarDefault(i + 1)
                    aplicarConfig("COLOR_SI", i + 1) { shapePaint.color = it as Int }
                    aplicarConfig("LETRA_SIZE_SI", i + 1) { textPaint.textSize = it as Float }
                    aplicarConfig("LETRA_SI", i + 1) { setTypeface(it.toString()) }
                    dibujarFiguraSi(canvas, y, obtenerFigura("FIGURA_SI", i + 1, "ROMBO"))
                    aplicarConfig("COLOR_TEXTO_SI", i + 1) { textPaint.color = it as Int }

                    canvas.drawText("SI ${nodo.condicion}", 120f, y + 60f, textPaint)
                }

                is MientrasNodo -> {
                    aplicarDefault(i + 1)
                    aplicarConfig("COLOR_MIENTRAS", i + 1) { shapePaint.color = it as Int }
                    aplicarConfig("LETRA_SIZE_MIENTRAS", i + 1) { textPaint.textSize = it as Float }
                    aplicarConfig("LETRA_MIENTRAS", i + 1) { setTypeface(it.toString()) }
                    dibujarFiguraMientras(canvas, y, obtenerFigura("FIGURA_MIENTRAS", i + 1, "RECTANGULO"))
                    aplicarConfig("COLOR_TEXTO_MIENTRAS", i + 1) { textPaint.color = it as Int }

                    canvas.drawText("MIENTRAS ${nodo.condicion}", 120f, y + 60f, textPaint)
                }
            }
            y += 150f
        }
    }

    // Este método aplica una configuración puntual si existe para el índice solicitado.
    private fun aplicarConfig(key: String, indice: Int, apply: (Any) -> Unit) {
        val cfg = configuraciones[key]?.get(indice)
        if (cfg != null) {
            apply(cfg.valor)
        }
    }

    // Este método restablece el estilo base antes de aplicar ajustes específicos.
    private fun aplicarDefault(indice: Int) {
        shapePaint.color = Color.BLACK
        shapePaint.style = Paint.Style.STROKE
        shapePaint.strokeWidth = 4f

        textPaint.color = Color.BLACK
        textPaint.textSize = 40f
        textPaint.typeface = Typeface.DEFAULT
    }

    // Este método resuelve la figura del nodo con fallback cuando no hay valor configurado.
    private fun obtenerFigura(key: String, indice: Int, figuraPorDefecto: String): String {
        val cfg = configuraciones[key]?.get(indice)?.valor?.toString()
        return if (cfg.isNullOrBlank()) figuraPorDefecto else cfg
    }

    // Este método traduce el nombre lógico de fuente a una familia disponible en Android.
    private fun setTypeface(nombre: String) {
        textPaint.typeface = when (nombre.uppercase()) {
            "ARIAL" -> Typeface.SANS_SERIF
            "TIMES_NEW_ROMAN" -> Typeface.SERIF
            "COMIC_SANS" -> Typeface.MONOSPACE
            "VERDANA" -> Typeface.SANS_SERIF
            else -> Typeface.DEFAULT
        }
    }

    // Este método dibuja la figura del nodo condicional según el tipo configurado.
    private fun dibujarFiguraSi(canvas: Canvas, y: Float, figura: String) {
        when (figura.uppercase()) {
            "ROMBO" -> {
                val path = Path().apply {
                    moveTo(250f, y)
                    lineTo(400f, y + 50f)
                    lineTo(250f, y + 100f)
                    lineTo(100f, y + 50f)
                    close()
                }
                canvas.drawPath(path, shapePaint)
            }

            "RECTANGULO" -> canvas.drawRect(100f, y, 400f, y + 100f, shapePaint)
            "CIRCULO" -> canvas.drawCircle(250f, y + 50f, 50f, shapePaint)
            "ELIPSE" -> canvas.drawOval(100f, y, 400f, y + 100f, shapePaint)
            else -> canvas.drawRect(100f, y, 400f, y + 100f, shapePaint)
        }
    }

    // Este método dibuja la figura del nodo de ciclo según el tipo configurado.
    private fun dibujarFiguraMientras(canvas: Canvas, y: Float, figura: String) {
        when (figura.uppercase()) {
            "CIRCULO" -> canvas.drawCircle(250f, y + 50f, 50f, shapePaint)
            "ELIPSE" -> canvas.drawOval(100f, y, 400f, y + 100f, shapePaint)
            "RECTANGULO" -> canvas.drawRect(100f, y, 400f, y + 100f, shapePaint)
            "ROMBO" -> {
                val path = Path().apply {
                    moveTo(250f, y)
                    lineTo(400f, y + 50f)
                    lineTo(250f, y + 100f)
                    lineTo(100f, y + 50f)
                    close()
                }
                canvas.drawPath(path, shapePaint)
            }
            else -> canvas.drawRect(100f, y, 400f, y + 100f, shapePaint)
        }
    }

    // Este método dibuja figuras para nodos de bloque general (declaración, asignación y E/S).
    private fun dibujarFiguraBloque(canvas: Canvas, y: Float, figura: String) {
        when (figura.uppercase()) {
            "RECTANGULO" -> canvas.drawRect(100f, y, 400f, y + 100f, shapePaint)
            "PARALELOGRAMO" -> {
                val path = Path().apply {
                    moveTo(120f, y)
                    lineTo(400f, y)
                    lineTo(380f, y + 100f)
                    lineTo(100f, y + 100f)
                    close()
                }
                canvas.drawPath(path, shapePaint)
            }

            "RECTANGULO_REDONDEADO" -> canvas.drawRoundRect(
                100f, y, 400f, y + 100f,
                20f, 20f, shapePaint
            )

            "ELIPSE" -> canvas.drawOval(100f, y, 400f, y + 100f, shapePaint)
            "CIRCULO" -> canvas.drawCircle(250f, y + 50f, 50f, shapePaint)
            "ROMBO" -> {
                val path = Path().apply {
                    moveTo(250f, y)
                    lineTo(400f, y + 50f)
                    lineTo(250f, y + 100f)
                    lineTo(100f, y + 50f)
                    close()
                }
                canvas.drawPath(path, shapePaint)
            }
            else -> canvas.drawRect(100f, y, 400f, y + 100f, shapePaint)
        }
    }
}


