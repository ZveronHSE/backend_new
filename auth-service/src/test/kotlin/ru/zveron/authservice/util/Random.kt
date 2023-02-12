package ru.zveron.authservice.util

import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.apache.commons.lang3.RandomStringUtils.randomNumeric
import org.apache.commons.lang3.RandomUtils
import ru.zveron.authservice.component.jwt.model.AccessToken
import ru.zveron.authservice.component.jwt.model.DecodedToken
import ru.zveron.authservice.component.jwt.model.MobileTokens
import ru.zveron.authservice.component.jwt.model.RefreshToken
import ru.zveron.authservice.persistence.entity.SessionEntity
import ru.zveron.authservice.persistence.model.MobilePhoneLoginStateContext
import ru.zveron.authservice.service.mapper.ServiceMapper.toContext
import ru.zveron.authservice.service.model.LoginByPhoneInitRequest
import ru.zveron.authservice.service.model.LoginByPhoneVerifyRequest
import ru.zveron.authservice.service.model.PhoneNumber
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
    fingerprint = randomDeviceFp(),
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

fun randomCode(): String = randomNumeric(4)

fun randomLoginVerifyRequest() = LoginByPhoneVerifyRequest(
    code = randomCode(),
    sessionId = UUID.randomUUID(),
    fingerprint = randomDeviceFp(),
)

fun randomLoginFlowContext() = MobilePhoneLoginStateContext(
    phoneNumber = randomPhoneNumber().toContext(),
    code = randomCode(),
    fingerprint = randomDeviceFp(),
)

fun randomApigPhone() = "7925${randomNumeric(7)}"

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

fun randomPassword() = randomAlphanumeric(10).toByteArray()

fun randomHash(): String = randomAlphanumeric(32)

fun randomPersistencePhone() = ru.zveron.authservice.persistence.model.PhoneNumber(countryCode = "7", randomNumeric(10))

inline fun <reified T : Enum<T>> randomEnum() = enumValues<T>().random()
