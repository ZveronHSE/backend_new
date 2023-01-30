package ru.zveron.authservice.util

import org.apache.commons.lang3.RandomStringUtils.randomNumeric
import org.apache.commons.lang3.RandomUtils
import ru.zveron.authservice.persistence.model.MobilePhoneLoginStateContext
import ru.zveron.authservice.service.ServiceMapper.toContext
import ru.zveron.authservice.service.model.LoginByPhoneInitRequest
import ru.zveron.authservice.service.model.LoginByPhoneVerifyRequest
import ru.zveron.authservice.service.model.PhoneNumber
import ru.zveron.contract.auth.phoneLoginInitRequest
import ru.zveron.contract.auth.phoneLoginVerifyRequest
import java.util.UUID

fun randomDeviceFp() = "device-fp-${UUID.randomUUID()}"

fun randomPhoneNumber() = PhoneNumber(
    countryCode = 7,
    phone = RandomUtils.nextLong(900_000_00_00, 999_999_99_99),
)

fun randomLoginInitRequest() = LoginByPhoneInitRequest(
    phoneNumber = randomPhoneNumber(),
    deviceFingerprint = randomDeviceFp(),
)

fun randomLoginInitApigRequest() = phoneLoginInitRequest {
    this.deviceFp = randomDeviceFp()
    this.phoneNumber = randomApigPhone()
}

fun randomLoginVerifyApigRequest(
    code: String = randomCode(),
    fp: String = randomDeviceFp(),
    session: String = UUID.randomUUID().toString(),
) = phoneLoginVerifyRequest {
    this.code = code
    this.deviceFp = fp
    this.sessionId = session
}

fun randomCode() = randomNumeric(4)

fun randomLoginVerifyRequest() = LoginByPhoneVerifyRequest(
    code = randomCode(),
    sessionId = UUID.randomUUID(),
    deviceFingerprint = randomDeviceFp(),
)

fun randomLoginFlowContext() = MobilePhoneLoginStateContext(
    phoneNumber = randomPhoneNumber().toContext(),
    code = randomCode(),
    deviceFp = randomDeviceFp(),
)

fun randomApigPhone() = "7${randomNumeric(10)}"

fun randomId() = RandomUtils.nextLong()

fun randomName() = "name-${UUID.randomUUID()}"

fun randomSurname() = "surname-${UUID.randomUUID()}"