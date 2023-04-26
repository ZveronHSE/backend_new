package ru.zveron.controller.external

import com.google.protobuf.Empty
import com.google.protobuf.Int64Value
import com.google.protobuf.StringValue
import com.google.protobuf.empty
import com.google.protobuf.stringValue
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.specialist.manage.Documents
import ru.zveron.contract.specialist.manage.EditAchievementRequest
import ru.zveron.contract.specialist.manage.EditEducationRequest
import ru.zveron.contract.specialist.manage.EditNameRequest
import ru.zveron.contract.specialist.manage.EditOtherRequest
import ru.zveron.contract.specialist.manage.EditServiceRequest
import ru.zveron.contract.specialist.manage.EditWorkExperienceRequest
import ru.zveron.contract.specialist.manage.FullAchievement
import ru.zveron.contract.specialist.manage.FullEducation
import ru.zveron.contract.specialist.manage.FullOther
import ru.zveron.contract.specialist.manage.FullService
import ru.zveron.contract.specialist.manage.FullWorkExperience
import ru.zveron.contract.specialist.manage.GetProfileResponse
import ru.zveron.contract.specialist.manage.InfoEntity
import ru.zveron.contract.specialist.manage.ManageSpecialistExternalServiceGrpcKt
import ru.zveron.library.grpc.util.GrpcUtils
import ru.zveron.service.ManageSpecialistService
import ru.zveron.service.SpecialistService
import kotlin.coroutines.coroutineContext

@GrpcService
class ManageSpecialistController(
    private val manageSpecialistService: ManageSpecialistService,
    private val specialistService: SpecialistService
) : ManageSpecialistExternalServiceGrpcKt.ManageSpecialistExternalServiceCoroutineImplBase() {

    override suspend fun getProfile(request: Empty): GetProfileResponse {
        val specialistID = GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = true).profileId!!

        return specialistService.getProfileSpecialist(specialistID)
    }

    override suspend fun addAchievement(request: FullAchievement): InfoEntity {
        val specialistID = GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = true).profileId!!

        return manageSpecialistService.addAchievement(specialistID, request)
    }

    override suspend fun editAchievement(request: EditAchievementRequest): InfoEntity {
        val specialistID = GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = true).profileId!!

        return manageSpecialistService.editAchievement(specialistID, request)
    }

    override suspend fun deleteAchievement(request: Int64Value): Empty {
        val specialistID = GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = true).profileId!!

        manageSpecialistService.deleteAchievement(specialistID, request.value)

        return empty {}
    }


    override suspend fun addEducation(request: FullEducation): InfoEntity {
        val specialistID = GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = true).profileId!!

        return manageSpecialistService.addEducation(specialistID, request)
    }

    override suspend fun editEducation(request: EditEducationRequest): InfoEntity {
        val specialistID = GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = true).profileId!!

        return manageSpecialistService.editEducation(specialistID, request)
    }

    override suspend fun deleteEducation(request: Int64Value): Empty {
        val specialistID = GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = true).profileId!!

        manageSpecialistService.deleteEducation(specialistID, request.value)

        return empty {}
    }


    override suspend fun addOther(request: FullOther): InfoEntity {
        val specialistID = GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = true).profileId!!

        return manageSpecialistService.addOther(specialistID, request)
    }

    override suspend fun editOther(request: EditOtherRequest): InfoEntity {
        val specialistID = GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = true).profileId!!

        return manageSpecialistService.editOther(specialistID, request)
    }

    override suspend fun deleteOther(request: Int64Value): Empty {
        val specialistID = GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = true).profileId!!

        manageSpecialistService.deleteOther(specialistID, request.value)
        return empty {}
    }

    override suspend fun addService(request: FullService): InfoEntity {
        val specialistID = GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = true).profileId!!

        return manageSpecialistService.addService(specialistID, request)
    }

    override suspend fun editService(request: EditServiceRequest): InfoEntity {
        val specialistID = GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = true).profileId!!

        return manageSpecialistService.editService(specialistID, request)
    }

    override suspend fun deleteService(request: Int64Value): Empty {
        val specialistID = GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = true).profileId!!

        manageSpecialistService.deleteService(specialistID, request.value)
        return empty {}
    }

    override suspend fun addWorkExperience(request: FullWorkExperience): InfoEntity {
        val specialistID = GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = true).profileId!!

        return manageSpecialistService.addWorkExperience(specialistID, request)
    }

    override suspend fun editWorkExperience(request: EditWorkExperienceRequest): InfoEntity {
        val specialistID = GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = true).profileId!!

        return manageSpecialistService.editWorkExperience(specialistID, request)
    }

    override suspend fun deleteWorkExperience(request: Int64Value): Empty {
        val specialistID = GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = true).profileId!!

        manageSpecialistService.deleteWorkExperience(specialistID, request.value)
        return empty {}
    }

    override suspend fun editDocuments(request: Documents): Documents {
        val specialistID = GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = true).profileId!!

        return manageSpecialistService.editDocuments(specialistID, request)
    }

    override suspend fun editDescription(request: StringValue): StringValue {
        val specialistID = GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = true).profileId!!

        val description = manageSpecialistService.editDescription(specialistID, request.value)

        return stringValue {
            value = description
        }
    }

    override suspend fun editName(request: EditNameRequest): StringValue {
        val specialistID = GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = true).profileId!!

        val nameSpecialist = manageSpecialistService.editName(specialistID, request)

        return stringValue {
            value = nameSpecialist.toString()
        }
    }
}