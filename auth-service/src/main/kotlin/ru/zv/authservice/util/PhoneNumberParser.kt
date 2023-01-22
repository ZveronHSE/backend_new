package ru.zv.authservice.util

import com.google.i18n.phonenumbers.PhoneNumberUtil
import io.grpc.Status
import ru.zv.authservice.service.AuthException
import ru.zv.authservice.service.dto.PhoneNumber

object PhoneNumberParser {

    private val phoneNumberUtils = PhoneNumberUtil.getInstance()
    fun stringToServicePhone(phone: String): PhoneNumber {
        phone.takeIf { phoneNumberUtils.isPossibleNumber(phone, "RU") }?.let {
            val parsedPhone = phoneNumberUtils.parse(phone, "RU")
            return PhoneNumber(parsedPhone.countryCode, parsedPhone.nationalNumber)
        } ?: throw AuthException("Failed to parse phone number", Status.Code.INVALID_ARGUMENT)
    }
}