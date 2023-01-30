package ru.zveron.authservice.service

import ru.zveron.authservice.service.model.LoginByPhoneInitRequest
import ru.zveron.authservice.service.model.PhoneNumber
import ru.zveron.authservice.webclient.dto.GetVerificationCodeRequest

object ServiceMapper {

    fun PhoneNumber.toContext() =
        ru.zveron.authservice.persistence.model.PhoneNumber(countryCode = countryCode.toString(), phone = phone.toString())

    fun LoginByPhoneInitRequest.toClientRequest() = GetVerificationCodeRequest(phoneNumber = phoneNumber.toClientPhone())
}
