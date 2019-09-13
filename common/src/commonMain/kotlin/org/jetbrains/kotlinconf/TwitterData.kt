package org.jetbrains.kotlinconf

import kotlinx.serialization.*


@Serializable
class FeedData(
    val statuses: List<FeedPost> = emptyList()
)

@Serializable
class FeedPost(
    val created_at: String,
    val text: String,
    val user: FeedUser
)

@Serializable
class FeedUser(
    val name: String,
    val profile_image_url_https: String,
    val screen_name: String
)