package com.example.evehandoutmanager.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    lateinit var _Handouts : MutableLiveData<List<Handout>>
}