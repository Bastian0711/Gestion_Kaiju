package com.example.appkaiju.stock.model

enum class Rol { ADMIN, CLIENTE}

enum class TipoProducto { VESTUARIO, TECNOLOGIA, INSUMO_PERECIBLE, GENERAL }

enum class TipoMovimiento { ENTRADA, SALIDA, AJUSTE, VENTA, ANULACION_VENTA }

enum class EstadoVenta { ACTIVA, ANULADA }


enum class EstadoStock { NORMAL, BAJO, AGOTADO }