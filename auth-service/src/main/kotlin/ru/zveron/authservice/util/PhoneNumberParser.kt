package ru.zveron.authservice.util

import com.google.i18n.phonenumbers.PhoneNumberUtil
import io.grpc.Status
import ru.zveron.authservice.exception.AuthException
import ru.zveron.authservice.service.dto.PhoneNumber

object PhoneNumberParser {

    private val phoneNumberUtils = PhoneNumberUtil.getInstance()
    fun stringToServicePhone(phone: String): PhoneNumber {
        phone.takeIf { phoneNumberUtils.isPossibleNumber(phone, "RU") }?.let {
            val parsedPhone = phoneNumberUtils.parse(phone, "RU")
            return PhoneNumber(parsedPhone.countryCode, parsedPhone.nationalNumber)
        } ?: throw AuthException("Failed to parse phone number", Status.Code.INVALID_ARGUMENT)
    }
}
