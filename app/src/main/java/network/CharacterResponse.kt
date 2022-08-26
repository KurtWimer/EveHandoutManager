package network

import com.google.gson.annotations.SerializedName

data class CharacterResponse(
    @SerializedName("alliance_id"     ) var allianceId     : Int?    = null,
    @SerializedName("birthday"        ) var birthday       : String? = null,
    @SerializedName("bloodline_id"    ) var bloodlineId    : Int?    = null,
    @SerializedName("corporation_id"  ) var corporationId  : Int?    = null,
    @SerializedName("description"     ) var description    : String? = null,
    @SerializedName("gender"          ) var gender         : String? = null,
    @SerializedName("name"            ) var name           : String? = null,
    @SerializedName("race_id"         ) var raceId         : Int?    = null,
    @SerializedName("security_status" ) var securityStatus : Int?    = null
)
