package com.rs.mymap.data.model

import com.google.gson.annotations.SerializedName

data class DirectionsResponse(
    @SerializedName("routes")
    val routes: List<Route>
)

data class Route(
    @SerializedName("overview_polyline")
    val overviewPolyline: OverviewPolyline,
    @SerializedName("legs")
    val legs: List<Leg>
)

data class Leg(
    @SerializedName("distance")
    val distance: Distance,
    @SerializedName("duration")
    val duration: Duration
)

data class Distance(
    @SerializedName("text")
    val text: String
)

data class Duration(
    @SerializedName("text")
    val text: String
)

data class OverviewPolyline(
    @SerializedName("points")
    val points: String
)
