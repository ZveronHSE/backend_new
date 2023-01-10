package ru.zveron.service

import io.grpc.Status
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.zveron.client.parameter.ParameterClient
import ru.zveron.contract.lot.CloseLotRequest
import ru.zveron.contract.lot.ClosingLotReason
import ru.zveron.contract.lot.CreateLotRequest
import ru.zveron.contract.lot.EditLotRequest
import ru.zveron.contract.lot.WaterfallRequest
import ru.zveron.contract.lot.model.CommunicationChannel
import ru.zveron.contract.lot.model.Photo
import ru.zveron.entity.Lot
import ru.zveron.entity.LotParameter
import ru.zveron.entity.LotPhoto
import ru.zveron.entity.LotStatistics
import ru.zveron.exception.LotException
import ru.zveron.mapper.ConditionsMapper
import ru.zveron.mapper.LotMapper.toGender
import ru.zveron.mapper.SellerMapper.toChannelType
import ru.zveron.model.SellerProfile
import ru.zveron.model.SummaryLot
import ru.zveron.model.constant.LotStatus
import ru.zveron.repository.LotRepository
import ru.zveron.repository.WaterfallRepository
import ru.zveron.util.LotValidation.validate
import ru.zveron.util.LotValidation.validateContacts
import ru.zveron.util.ValidateUtils.validatePositive
import java.time.Instant

@Service
class LotService(
    private val lotRepository: LotRepository,
    private val waterfallRepository: WaterfallRepository,
    private val parameterClient: ParameterClient,
) {
    fun getLotById(id: Long): Lot {
        id.validatePositive("lotId")

        return lotRepository.findByIdOrThrow(id)
    }

    fun getLotsByIds(ids: List<Long>): List<Lot> {
        ids.forEach {
            it.validatePositive("lotId")
        }


        return lotRepository.findAllById(ids)
    }


    fun getLotsBySellerId(sellerId: Long): List<Lot> {
        sellerId.validatePositive("sellerId")

        return lotRepository.findAllBySellerIdOrderByDateCreationDesc(sellerId)
    }

    suspend fun getWaterfall(request: WaterfallRequest, sellerId: Long?): List<SummaryLot> {
        // Для категорий будет специфичный кейс - для него предусмотрена выборка с углублением вниз по дереву
        val categories = request.takeIf { it.hasCategoryId() }
            ?.let {
                parameterClient.getTreeByCategory(it.categoryId)
            }

        val conditions = ConditionsMapper.parse(request, categories, sellerId)

        return waterfallRepository.findAll(conditions)
    }

    @Transactional
    fun createLot(request: CreateLotRequest, seller: SellerProfile, addressId: Long): Lot {
        validateLotProperties(request.photosList, request.communicationChannelList, seller)

        val lot = with(request) {
            Lot(
                title = title,
                description = description,
                price = price,
                lotFormId = lotFormId,
                dateCreation = Instant.now(),
                status = LotStatus.ACTIVE,
                gender = gender.toGender(),
                sellerId = seller.id,
                categoryId = categoryId,
                channelType = communicationChannelList.toChannelType(),
                addressId = addressId
            )
        }

        with(lot) {
            // Инициализируем по умолчанию статистику с нулевыми параметрами
            statistics = LotStatistics(lot = lot)
            photos = request.photosList
                .map { LotPhoto(lot = lot, imageId = it.id, order = it.order) }
            parameters = request.parametersMap.map {
                LotParameter(
                    id = LotParameter.LotParameterKey(it.key, lot.id),
                    value = it.value,
                    lot = lot
                )
            }
        }

        return lotRepository.save(lot)
    }


    @Transactional
    fun editLot(lot: Lot, request: EditLotRequest, seller: SellerProfile): Lot {
        validateLotProperties(request.photosList, request.communicationChannelList, seller)

        with(lot) {
            title = request.title
            description = request.description
            price = request.price
            channelType = request.communicationChannelList.toChannelType()
            parameters = request.parametersMap.map {
                LotParameter(
                    id = LotParameter.LotParameterKey(it.key, lot.id),
                    value = it.value,
                    lot = lot
                )
            }
            photos = request.photosList.map {
                LotPhoto(
                    lot = lot,
                    imageId = it.id,
                    order = it.order
                )
            }
        }

        return lotRepository.saveAndFlush(lot)
    }

    fun closeLot(lot: Lot, request: CloseLotRequest) {
        if (lot.status != LotStatus.ACTIVE) {
            throw LotException(Status.INTERNAL, "you cant close inactive lot")
        }

        when (request.closingLotReason) {
            ClosingLotReason.SOLD -> {
                if (!request.hasCustomerId()) {
                    throw LotException(Status.INVALID_ARGUMENT, "cant closing lot without field customer id")
                }

                if (request.customerId == lot.sellerId) {
                    throw LotException(Status.INVALID_ARGUMENT, "cant choose myself as customer, because it your lot")
                }
                // TODO add logic for possible customers -> add reviews ZV-300
                lot.status = LotStatus.CLOSED
            }
            else ->   lot.status = LotStatus.CANCELED
        }


        lotRepository.save(lot)
    }

    private fun validateLotProperties(
        photos: List<Photo>,
        communicationChannel: List<CommunicationChannel>,
        seller: SellerProfile
    ) {
        seller.validateContacts(communicationChannel)
        photos.validate()
        // TODO validate gender ZV-301
    }
}