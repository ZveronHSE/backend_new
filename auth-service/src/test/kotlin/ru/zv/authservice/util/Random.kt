package ru.zv.authservice.util

import org.apache.commons.lang3.RandomStringUtils.randomNumeric
import org.apache.commons.lang3.RandomUtils
import ru.zv.authservice.component.jwt.AccessToken
import ru.zv.authservice.component.jwt.Constants
import ru.zv.authservice.component.jwt.MobileTokens
import ru.zv.authservice.component.jwt.RefreshToken
import ru.zv.authservice.component.jwt.model.DecodedToken
import ru.zv.authservice.persistence.entity.SessionEntity
import ru.zv.authservice.persistence.model.MobilePhoneLoginStateContext
import ru.zv.authservice.service.dto.LoginByPhoneInitRequest
import ru.zv.authservice.service.dto.LoginByPhoneVerifyRequest
import ru.zv.authservice.service.dto.PhoneNumber
import ru.zv.authservice.service.dto.toContext
import ru.zveron.contract.auth.phoneLoginInitRequest
import ru.zveron.contract.auth.phoneLoginVerifyRequest
import java.time.Instant
import java.util.UUID

fun randomDeviceFp() = "device-fp-${UUID.randomUUID()}"

fun randomPhoneNumber() = PhoneNumber(
    countryCode = 7,
    phone = RandomUtils.nextLong(900_000_00_00, 999_999_99_99),
)

fun randomLoginInitRequest() = LoginByPhoneInitRequest(
    phoneNumber = randomPhoneNumber(),
    deviceFp = randomDeviceFp(),
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
    deviceFp = randomDeviceFp(),
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

fun randomTokens() = MobileTokens(
    refreshToken = randomRefreshToken(),
    accessToken = randomAccessToken(),
)

fun randomRefreshToken() = RefreshToken(UUID.randomUUID().toString(), Instant.now().plusSeconds(1000L))

fun randomAccessToken() = AccessToken(UUID.randomUUID().toString(), Instant.now().plusSeconds(10_000L))

fun randomDecodedToken() = DecodedToken(
    profileId = randomId(),
    tokenType = randomEnum(),
    issuer = Constants.ZV_ISSUER,
    expiresAt = Instant.now(),
    sessionId = UUID.randomUUID(),
    tokenIdentifier = UUID.randomUUID(),
)

fun randomSessionEntity() = SessionEntity(
    id = UUID.randomUUID(),
    tokenIdentifier = UUID.randomUUID(),
    fingerprint = randomDeviceFp(),
    profileId = randomId(),
    expiresAt = Instant.now(),
)

inline fun <reified T : Enum<T>> randomEnum() = enumValues<T>().random()