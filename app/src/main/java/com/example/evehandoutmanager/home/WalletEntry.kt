package com.example.evehandoutmanager.home

import com.google.gson.annotations.SerializedName

data class WalletEntry (
    @SerializedName("amount"          ) var amount        : Double,
    @SerializedName("balance"         ) var balance       : Double? = null,
    @SerializedName("date"            ) var date          : String,
    @SerializedName("description"     ) var description   : String? = null,
    @SerializedName("first_party_id"  ) var firstPartyId  : Int,
    @SerializedName("id"              ) var id            : Long,
    @SerializedName("reason"          ) var reason        : String? = null,
    @SerializedName("ref_type"        ) var refType       : String,
    @SerializedName("second_party_id" ) var secondPartyId : Int,
)
