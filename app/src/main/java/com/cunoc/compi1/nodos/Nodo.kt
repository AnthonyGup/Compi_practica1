package com.cunoc.compi1.nodos

// Esta clase sellada define la base de todos los nodos del diagrama
// y restringe los tipos válidos a este archivo.
sealed class Nodo {
    abstract val tipo: String
}

// Estas clases representan nodos de inicio y fin del flujo.
data class InicioNodo(val id: Int = 0) : Nodo() {
    override val tipo = "INICIO"
}

data class FinNodo(val id: Int = 0) : Nodo() {
    override val tipo = "FIN"
}

// Estas clases modelan declaraciones y asignaciones de variables.
data class DeclaracionNodo(val nombre: String, val valor: String?) : Nodo() {
    override val tipo = "DECLARACION"
}

data class AsignacionNodo(val nombre: String, val expresion: String) : Nodo() {
    override val tipo = "ASIGNACION"
}

// Estas clases modelan instrucciones de entrada y salida.
data class MostrarNodo(val mensaje: String) : Nodo() {
    override val tipo = "MOSTRAR"
}

data class LeerNodo(val variable: String) : Nodo() {
    override val tipo = "LEER"
}

// Estas clases modelan estructuras de control con bloques anidados.
data class SiNodo(val condicion: String, val bloque: List<Nodo>) : Nodo() {
    override val tipo = "SI"
}

data class MientrasNodo(val condicion: String, val bloque: List<Nodo>) : Nodo() {
    override val tipo = "MIENTRAS"
}
