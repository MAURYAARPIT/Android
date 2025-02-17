package com.iitp.anwesha.home

import java.io.Serializable

data class EventList(
    val id: String? = null,
    val name: String? = null,
    val organizer: List<List<String>>? = null,
    val venue: String? = null,
    val description: String? = null,
    val start_time: String? = null,
    val end_time: String? = null,
    val prize: String? = null,
    val registration_fee: String? = null,
    val registration_deadline: String? = null,
    val video: String? = null,
    val poster: String? = null,
    val tags: String? = null,
    val max_team_size: Int? = null,
    val min_team_size: Int? = null,
    val is_active: Boolean? = null,
    val is_online: Boolean? = null,
    val registration_link: String? = null,
    val is_solo : Boolean? = null

): Serializable

