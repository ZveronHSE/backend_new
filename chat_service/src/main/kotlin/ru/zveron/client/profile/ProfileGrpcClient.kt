package ru.zveron.client.profile

import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component
import ru.zveron.contract.profile.ProfileServiceInternalGrpcKt
import ru.zveron.contract.profile.ProfileSummary
import ru.zveron.contract.profile.existsByIdRequest
import ru.zveron.contract.profile.getProfilesSummaryRequest

@Component
class ProfileGrpcClient : ProfileClient {

    @GrpcClient("profile-client")
    lateinit var client: ProfileServiceInternalGrpcKt.ProfileServiceInternalCoroutineStub

    override suspend fun getProfilesSummary(ids: List<Long>): MutableList<ProfileSummary> = client
        .getProfilesSummary(getProfilesSummaryRequest { this.ids.addAll(ids) })
        .profilesList

    override suspend fun existsById(id: Long): Boolean = client
        .existsById(existsByIdRequest { this.id = id })
        .exists
}