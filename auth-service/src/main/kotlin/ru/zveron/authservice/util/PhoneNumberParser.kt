package ru.zveron.authservice.util

import com.google.i18n.phonenumbers.PhoneNumberUtil
import io.grpc.Status
import ru.zveron.authservice.exception.AuthException
import ru.zveron.authservice.service.model.PhoneNumber

object PhoneNumberParser {

    private const val RU_REGION = "RU"

    private val phoneNumberUtils = PhoneNumberUtil.getInstance()
    fun stringToServicePhone(phone: String): PhoneNumber {
        phone.takeIf { phoneNumberUtils.isPossibleNumber(phone, RU_REGION) }
            ?.let {
                val parsedPhone = phoneNumberUtils.parse(phone, RU_REGION)
                return PhoneNumber(parsedPhone.countryCode, parsedPhone.nationalNumber)
            }
            ?: throw AuthException("Failed to parse phone number=$phone", Status.Code.INVALID_ARGUMENT)
    }
}
