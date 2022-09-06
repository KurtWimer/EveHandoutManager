package com.example.evehandoutmanager.network

import com.google.gson.annotations.SerializedName

data class Portrait(
    @SerializedName("px128x128" ) var px128x128 : String? = null,
    @SerializedName("px256x256" ) var px256x256 : String? = null,
    @SerializedName("px512x512" ) var px512x512 : String? = null,
    @SerializedName("px64x64"   ) var px64x64   : String? = null)
