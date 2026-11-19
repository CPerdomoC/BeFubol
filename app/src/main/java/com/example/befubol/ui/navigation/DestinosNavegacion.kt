package com.example.befubol.ui.navigation

sealed class Destino(val ruta: String) {
    object Equipos : Destino("equipos")
    object PrepararPartido : Destino("preparar_partido")
    object PartidoEnCurso : Destino("partido_en_curso")
    object Penaltis : Destino("penaltis")
    object Resumen : Destino("resumen")
    object Historial : Destino("historial")
}
