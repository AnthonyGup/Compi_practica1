package com.cunoc.compi1.nodos

// Clase base sellada: garantiza que todos los tipos de nodos estén definidos aquí
sealed class Nodo {
    abstract val tipo: String
}

// Nodos básicos
data class InicioNodo(val id: Int = 0) : Nodo() {
    override val tipo = "INICIO"
}

data class FinNodo(val id: Int = 0) : Nodo() {
    override val tipo = "FIN"
}

// Variables y asignaciones
data class DeclaracionNodo(val nombre: String, val valor: String?) : Nodo() {
    override val tipo = "DECLARACION"
}

data class AsignacionNodo(val nombre: String, val expresion: String) : Nodo() {
    override val tipo = "ASIGNACION"
}

// Entrada/Salida
data class MostrarNodo(val mensaje: String) : Nodo() {
    override val tipo = "MOSTRAR"
}

data class LeerNodo(val variable: String) : Nodo() {
    override val tipo = "LEER"
}

// Control de flujo
data class SiNodo(val condicion: String, val bloque: List<Nodo>) : Nodo() {
    override val tipo = "SI"
}

data class MientrasNodo(val condicion: String, val bloque: List<Nodo>) : Nodo() {
    override val tipo = "MIENTRAS"
}
