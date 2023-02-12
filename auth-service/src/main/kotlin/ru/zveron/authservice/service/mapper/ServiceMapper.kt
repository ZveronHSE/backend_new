package ru.zveron.authservice.service.mapper

import ru.zveron.authservice.service.model.LoginByPhoneInitRequest
import ru.zveron.authservice.service.model.PhoneNumber
import ru.zveron.authservice.webclient.model.GetVerificationCodeRequest

object ServiceMapper {

    fun PhoneNumber.toContext() =
        ru.zveron.authservice.persistence.model.PhoneNumber(
            countryCode = countryCode.toString(),
            phone = phone.toString()
        )

    fun LoginByPhoneInitRequest.toClientRequest() =
        GetVerificationCodeRequest(phoneNumber = phoneNumber.toClientPhone())

    fun ru.zveron.authservice.persistence.model.PhoneNumber.toProfileClientRequest() = "$countryCode$phone"
}
