package ru.zveron.client.profile

import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Service
import ru.zveron.contract.profile.ProfileServiceInternalGrpcKt
import ru.zveron.contract.profile.getProfilesSummaryRequest

@Service
class ProfileGrpcClient : ProfileClient {

    @GrpcClient("profile-service")
    lateinit var service: ProfileServiceInternalGrpcKt.ProfileServiceInternalCoroutineStub

    override suspend fun getProfilesSummary(ids: List<Long>) =
        service.getProfilesSummary(getProfilesSummaryRequest {
            this.ids.addAll(ids)
        })
}