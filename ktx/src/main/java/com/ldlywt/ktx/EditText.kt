package com.ldlywt.ktx


import android.widget.EditText

val EditText.value
    get() = text?.toString() ?: ""