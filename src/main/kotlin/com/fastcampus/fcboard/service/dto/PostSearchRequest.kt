package com.fastcampus.fcboard.service.dto

import org.springframework.web.bind.annotation.RequestParam

data class PostSearchRequest(
    @RequestParam
    val title: String?,
    @RequestParam
    val createdBy: String?,
)

fun PostSearchRequest.toDto() = PostSearchRequestDto(
    title = title,
    createdBy = createdBy
)
