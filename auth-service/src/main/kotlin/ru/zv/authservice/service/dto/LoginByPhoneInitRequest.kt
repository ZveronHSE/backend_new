package ru.zv.authservice.service.dto

import ru.zv.authservice.webclient.dto.GetVerificationCodeRequest

data class LoginByPhoneInitRequest(
    val phoneNumber: PhoneNumber,
    val deviceFp: String,
)

fun LoginByPhoneInitRequest.toClientRequest() = GetVerificationCodeRequest(phoneNumber = phoneNumber.toClientPhone())
