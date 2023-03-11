package ru.zveron.client.profile

import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Service
import ru.zveron.contract.profile.ProfileServiceInternalGrpcKt
import ru.zveron.contract.profile.ProfileSummary
import ru.zveron.contract.profile.existsByIdRequest
import ru.zveron.contract.profile.getProfilesSummaryRequest

@Service
class ProfileGrpcClient : ProfileClient {

    @GrpcClient("profile-service")
    lateinit var service: ProfileServiceInternalGrpcKt.ProfileServiceInternalCoroutineStub

    override suspend fun getProfilesSummary(ids: List<Long>): MutableList<ProfileSummary> = service
        .getProfilesSummary(getProfilesSummaryRequest { this.ids.addAll(ids) })
        .profilesList

    override suspend fun existsById(id: Long): Boolean = service
        .existsById(existsByIdRequest { this.id = id })
        .exists
}