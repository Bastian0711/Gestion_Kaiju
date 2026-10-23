package com.example.appkaiju.stock.ui.components

import java.util.Locale

fun Int.comoClp(): String =
    "\$" + String.format(Locale.forLanguageTag("es-CL"), "%,d", this)