package com.example.kafiesta.domain

import com.google.gson.annotations.SerializedName


data class ListNetworktest (
    val status: String,
    val message: String,
    val result: Resulttest
)


data class Resulttest (
    @SerializedName("current_page")
    val currentPage: Long,

    val data: List<Producttest>,

    @SerializedName("first_page_url")
    val firstPageURL: String,

    val from: Long,

    @SerializedName("last_page")
    val lastPage: Long,

    @SerializedName("last_page_url")
    val lastPageURL: String,

    val links: List<Link>,

    @SerializedName("next_page_url")
    val nextPageURL: String? = null,

    val path: String,

    @SerializedName("per_page")
    val perPage: String,

    @SerializedName("prev_page_url")
    val prevPageURL: String? = null,

    val to: Long,
    val total: Long
)


data class Producttest (
    val id: Long,

    @SerializedName("user_id")
    val userID: Long,

    val name: String,
    val description: String,

    @SerializedName("image_url")
    val imageURL: String? = null,

    val price: String,
    val tags: String? = null,
    val status: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String
)

data class Link (
    val url: String? = null,
    val label: String,
    val active: Boolean
)

fun Resulttest.asDomainModel(): ResultDomaintest {
    return ResultDomaintest(
        currentPage = currentPage,
        data = data.map { it.asDomainModel() },
        firstPageURL = firstPageURL,
        from = from,
        lastPage = lastPage,
        lastPageURL = lastPageURL,
        links = links.map { it.asDomainModel() },
        nextPageURL = nextPageURL,
        path = path,
        perPage = perPage,
        prevPageURL = prevPageURL,
        to = to,
        total = total,
    )

}

fun Producttest.asDomainModel(): ProductDomaintest {
    return ProductDomaintest(
        id = id,
        userID = userID,
        name = name,
        description = description,
        imageURL = imageURL,
        price = price,
        tags = tags,
        status = status,
    )

}

fun Link.asDomainModel(): LinkDomaintest {
    return LinkDomaintest(
        url = url,
        label = label,
        active = active
    )

}