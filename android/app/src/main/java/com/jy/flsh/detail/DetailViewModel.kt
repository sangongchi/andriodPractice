package com.jy.flsh.detail

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel

class DetailViewModel : ViewModel() {

    val title = ObservableField<String>()

    val detail = ObservableField<String>()
}
