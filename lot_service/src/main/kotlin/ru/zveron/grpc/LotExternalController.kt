package ru.zveron.grpc

import com.google.protobuf.Empty
import com.google.protobuf.empty
import io.grpc.Status
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.client.address.AddressClient
import ru.zveron.client.favorite.LotFavoriteClient
import ru.zveron.client.parameter.ParameterClient
import ru.zveron.client.profile.ProfileClient
import ru.zveron.contract.lot.CardLot
import ru.zveron.contract.lot.CardLotRequest
import ru.zveron.contract.lot.CloseLotRequest
import ru.zveron.contract.lot.CreateLotRequest
import ru.zveron.contract.lot.EditLotRequest
import ru.zveron.contract.lot.LotExternalProtoServiceGrpcKt
import ru.zveron.contract.lot.WaterfallRequest
import ru.zveron.contract.lot.WaterfallResponse
import ru.zveron.contract.parameter.internal.InfoCategory
import ru.zveron.exception.LotException
import ru.zveron.library.grpc.util.GrpcUtils
import ru.zveron.mapper.CardLotBuilder.Companion.buildCardLot
import ru.zveron.mapper.LotMapper
import ru.zveron.model.Address
import ru.zveron.model.SellerProfile
import ru.zveron.service.LotService
import ru.zveron.service.LotStatisticsService
import kotlin.coroutines.coroutineContext

@GrpcService
class LotExternalController(
    private val lotService: LotService,
    private val lotStatisticsService: LotStatisticsService,
    private val lotFavoriteClient: LotFavoriteClient,
    private val profileClient: ProfileClient,
    private val parameterClient: ParameterClient,
    private val addressClient: AddressClient
) : LotExternalProtoServiceGrpcKt.LotExternalProtoServiceCoroutineImplBase() {

    override suspend fun createLot(request: CreateLotRequest): CardLot {
        val userId = GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = true).profileId!!

        var seller: SellerProfile? = null
        var address: Address? = null
        var category: InfoCategory? = null
        var parameters: Map<Int, String> = mapOf()
        coroutineScope {
            val clients = mutableListOf(
                async {
                    seller = profileClient.getProfileWithContacts(userId)
                },
                async {
                    parameterClient.validateParameters(
                        categoryId = request.categoryId,
                        lotFormId = request.lotFormId,
                        parameters = request.parametersMap
                    )
                },
                async {
                    address = addressClient.saveAddressIfNotExists(request.address)
                },
                async {
                    category = parameterClient.getInfoAboutCategory(request.categoryId)
                },
                async {
                    parameters = parameterClient.getParametersById(request.parametersMap.keys.toList())
                }
                // TODO validating images id for existing by image service ZV-307
            )

            return@coroutineScope clients.awaitAll()
        }

        val lot = lotService.createLot(request, seller!!, address!!.id, category!!)

        return buildCardLot {
            this.lot = lot
            this.seller = seller!!
            isOwnLot = true
            this.address = address!!
            parametersMap = parameters
        }
    }

    override suspend fun editLot(request: EditLotRequest): CardLot {
        val userId = GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = true).profileId!!

        var lot = lotService.getLotById(request.id)

        if (lot.sellerId != userId) {
            throw LotException(
                Status.PERMISSION_DENIED,
                "User with id=$userId cant edit foreign lot with id: ${lot.id}"
            )
        }

        var seller: SellerProfile? = null
        var address: Address? = null
        var parameters: Map<Int, String> = mapOf()
        coroutineScope {
            val clients = mutableListOf(
                async {
                    seller = profileClient.getProfileWithContacts(userId)
                },
                async {
                    parameterClient.validateParameters(
                        categoryId = lot.categoryId,
                        lotFormId = lot.lotFormId,
                        parameters = request.parametersMap
                    )
                },
                async {
                    address = addressClient.getAddressById(lot.addressId)
                },
                async {
                    parameters = parameterClient.getParametersById(lot.parameters.map { it.id.parameter })
                }
                // TODO validating images id for existing by image service ZV-307
            )

            return@coroutineScope clients.awaitAll()
        }

        lot = lotService.editLot(lot, request, seller!!)

        return buildCardLot {
            this.lot = lot
            this.seller = seller!!
            isOwnLot = true
            this.address = address
            parametersMap = parameters
        }
    }


    override suspend fun closeLot(request: CloseLotRequest): Empty {
        val userId = GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = true).profileId!!

        val lot = lotService.getLotById(request.id)

        if (lot.sellerId != userId) {
            throw LotException(
                Status.PERMISSION_DENIED,
                "User with id=$userId cant edit foreign lot with id: ${lot.id}"
            )
        }

        lotService.closeLot(lot, request)
        return empty { }
    }


    override suspend fun getCardLot(request: CardLotRequest): CardLot {
        val lotId = request.id
        val userId = GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = false).profileId

        val lot = lotService.getFullLotById(lotId)


        var isFavoriteLot = false
        var isOwnLot = false
        var seller: SellerProfile? = null
        var address: Address? = null
        var parameters: Map<Int, String> = mapOf()
        coroutineScope {
            // TODO подумать как тут лучше сделать(реально ли запускать в любом случае, ибо может быть ряд ошибок ниже
            launch {
                lotStatisticsService.incrementViewCounter(lotId)
            }

            val clients = mutableListOf(
                async {
                    seller = lot.sellerId?.let { profileClient.getProfileWithContacts(it) }
                },
                async {
                    address = addressClient.getAddressById(lot.addressId)
                },
                async {
                    parameters = parameterClient.getParametersById(lot.parameters.map { it.id.parameter })
                }
                // TODO ZV-323 add get name for parameters id
            )

            // Если пользователь авторизован:
            if (userId != null && userId != 0L) {
                isOwnLot = lot.sellerId == userId

                clients.add(async {
                    isFavoriteLot = lotFavoriteClient.checkLotIsFavorite(lotId, userId)
                })
            }

            clients.awaitAll()
        }

        return buildCardLot {
            this.lot = lot
            this.seller = seller
            this.isOwnLot = isOwnLot
            this.isFavoriteLot = isFavoriteLot
            this.address = address
            parametersMap = parameters
        }
    }

    override suspend fun getWaterfall(request: WaterfallRequest): WaterfallResponse {
        // Если нет запросов по сортировке, то пагинация и выдача не может работать корректно
        if (request.sortCase == WaterfallRequest.SortCase.SORT_NOT_SET) {
            throw LotException(
                Status.INVALID_ARGUMENT,
                "didn't get any sorts, filter and pagination won't work without it"
            )
        }

        val userId = GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = false).profileId

        if (request.pageSize < 1) {
            throw LotException(Status.INVALID_ARGUMENT, "for parameter pageSize value can't ")
        }

        val lots = lotService.getWaterfall(request, userId)

        val favorites = userId?.takeIf { it > 0 }
            ?.let { lotFavoriteClient.checkLotsAreFavorites(lots.map { it.id }, userId) }


        return LotMapper.buildWaterfallResponse(lots, favorites)
    }
}