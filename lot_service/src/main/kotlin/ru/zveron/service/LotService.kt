//package ru.zveron.service
//
//import org.springframework.stereotype.Service
//import org.springframework.transaction.annotation.Transactional
//import ru.zveron.constant.ClosingLotReason
//import ru.zveron.constant.Gender
//import ru.zveron.constant.LotStatus
//import ru.zveron.constant.initializtion.ProfileInitializationType
//import ru.zveron.domain.WaysOfCommunicatingDTO
//import ru.zveron.domain.lot.LotWaterfallDTO
//import ru.zveron.domain.request.lot.CreateLotRequest
//import ru.zveron.domain.request.lot.EditLotRequest
//import ru.zveron.domain.request.lot.LotParameterRequestDTO
//import ru.zveron.domain.request.lot.LotPhotoRequestDTO
//import ru.zveron.domain.response.profile.ProfilePreviewDTO
//import ru.zveron.entity.Address
//import ru.zveron.entity.Lot
//import ru.zveron.entity.lot.Category
//import ru.zveron.entity.lot.Lot
//import ru.zveron.entity.lot.LotForm
//import ru.zveron.entity.lot.LotParameter
//import ru.zveron.entity.lot.LotPhoto
//import ru.zveron.entity.lot.LotStatistics
//import ru.zveron.entity.lot.Parameter
//import ru.zveron.entity.lot.PossibleCustomer
//import ru.zveron.entity.profile.Profile
//import ru.zveron.exception.LotException
//import ru.zveron.repo.lot.LotParameterRepository
//import ru.zveron.repo.lot.LotRepository
//import ru.zveron.repo.lot.LotWaterfallRepository
//import ru.zveron.repository.LotRepository
//import ru.zveron.service.ImageService
//import ru.zveron.service.profile.ProfileService
//import ru.zveron.util.Validate.Companion.validateOrders
//import ru.zveron.util.mapper.ProfileMapper.toPreview
//import ru.zveron.util.search.ConditionsSearch
//import java.time.Instant
//
//@Service
//class LotService(
//    val lotRepository: LotRepository,
////    val parameterService: ParameterService,
//    val lotParameterRepository: LotParameterRepository,
//    val imageService: ImageService,
//    val profileService: ProfileService,
//    val categoryService: CategoryService,
//    val possibleCustomerService: PossibleCustomerService,
//    val lotWaterfallRepository: LotWaterfallRepository
//) {
//
//    fun setSellerToNullByItsId(id: Long) = lotRepository.setSellerToNullByItsId(id)
//
//    fun delete(lot: Lot) = lotRepository.delete(lot)
//
//    @Transactional
//    fun createLot(
//        request: CreateLotRequest,
//        seller: Profile,
//        lotForm: LotForm,
//        category: Category,
//        childOfRootCategory: Category,
//        address: Address
//    ): Lot {
//        val parametersMap = validateLotAndGetParametersMap(
//            contact = request.contact,
//            seller = seller,
//            photos = request.photos,
//            rootCategoryId = childOfRootCategory.parent!!.id,
//            lotForm = lotForm,
//            childOfRootCategory = childOfRootCategory,
//            gender = request.gender,
//            parameters = request.parameters
//        )
//
//        var lot = request.toLot(seller, lotForm, category, address)
//        with(lot) {
//            // Инициализируем по умолчанию статистику с нулевыми параметрами
//            statistics = LotStatistics(lot = lot)
//            photos = request.photos
//                .map {
//                    LotPhoto(
//                        lot = lot,
//                        image = imageService.getByIdOrElseThrow(it.id),
//                        order_photo = it.order
//                    )
//                }
//                .toMutableList()
//        }
//
//        lot = lotRepository.save(lot)
//
//        // Такой отдельный маневр пришлось совершить, поскольку мне нужно знать id для лота :(
//        lot.parameters = request.parameters.map {
//            LotParameter(
//                id = LotParameter.LotParameterKey(it.id, lot.id),
//                value = it.value,
//                parameter = parametersMap[it.id]!!,
//                lot = lot
//            )
//        }.toMutableList()
//
//        lotParameterRepository.saveAll(lot.parameters)
//
//        return lot
//    }
//
//    private fun validateLotAndGetParametersMap(
//        contact: WaysOfCommunicatingDTO,
//        seller: Profile,
//        photos: List<LotPhotoRequestDTO>,
//        rootCategoryId: Long,
//        lotForm: LotForm,
//        childOfRootCategory: Category,
//        gender: Gender?,
//        parameters: List<LotParameterRequestDTO>
//    ): Map<Long, Parameter> {
//        // Проверка количества выбранных способов контактов
//        contact.validateWaysOfCommunicationOrThrow()
//        // Проверить, что выбранные контакты корректны для этого продавца
//        validateContactsSeller(contact, seller)
//
//        // Проверка порядка фотографий
//        validatePhotos(photos)
//        validateGender(rootCategoryId, gender)
//        // Проверка корректных выбранных значений для параметров
//        return parameterService.validateValueForParameters(parameters, lotForm, childOfRootCategory)
//    }
//
//    fun getLotByIdOrElseThrow(id: Long): Lot =
//        lotRepository.findById(id)
//            .orElseThrow { LotException("Объявления с id: $id не существует") }
//
//    private fun CreateLotRequest.toLot(
//        seller: Profile,
//        lotForm: LotForm,
//        category: Category,
//        address: Address
//    ) = Lot(
//        title = this.title,
//        description = this.description,
//        price = this.price,
//        lotForm = lotForm,
//        dateCreation = Instant.now(),
//        status = LotStatus.ACTIVE.name,
//        seller = seller,
//        category = category,
//        waysOfCommunicating = this.contact,
//        address = address,
//        gender = gender
//    )
//
//    private fun WaysOfCommunicatingDTO.validateWaysOfCommunicationOrThrow() {
//        if (!this.validateNoMoreTwoWays()) {
//            throw LotException("Нельзя выбрать более, чем 2 контакта")
//        }
//    }
//
//    fun validatePhotos(photos: List<LotPhotoRequestDTO>) {
//        if (photos.isEmpty()) {
//            throw LotException("Необходимо указать как минимум одну фотографию")
//        }
//        if (!photos.validateOrders()) {
//            throw LotException("Порядок фотографий не последовательный: 0, 1, 2...")
//        }
//        photos.forEach {
//            if (it.id == 1L) {
//                throw LotException("Ни одна из фотографий не должна быть фотографией по умолчанию")
//            }
//        }
//    }
//
//    /**
//     * Проверка, что параметр "гендер" есть для объявлений в сфере животных, иначе null.
//     */
//    fun validateGender(rootCategoryId: Long, gender: Gender?) {
//        if (rootCategoryId == 1L) {
//            gender
//                ?: throw LotException("В категории 'животные' объявление должен содержать поле 'гендер'")
//        } else if (gender != null) {
//            throw LotException("В категории 'товары для животных' объявление НЕ должен содержать поле 'гендер'")
//        }
//    }
//
//    private fun validateContactsSeller(
//        waysOfCommunicatingDTO: WaysOfCommunicatingDTO,
//        seller: Profile
//    ) {
//        val contact = seller.contact!!
//
//        if (waysOfCommunicatingDTO.phone && contact.phone == null) {
//            throw LotException("Продавец выбрал телефонный контакт, которого у него не существует")
//        }
//
//        if (waysOfCommunicatingDTO.facebook && contact.facebookRef == null) {
//            throw LotException("Продавец выбрал контакт FB, которого у него не существует")
//        }
//
//        if (waysOfCommunicatingDTO.vk && contact.vkRef == null) {
//            throw LotException("Продавец выбрал контакт VK, которого у него не существует")
//        }
//
//        if (waysOfCommunicatingDTO.email && contact.gmail == null && contact.additionEmail == null) {
//            throw LotException("Продавец выбрал контакт почты, которой у него не существует")
//        }
//    }
//
//    fun getWaterfallLots(
//        conditionsSearch: ConditionsSearch
//    ): List<LotWaterfallDTO> = lotWaterfallRepository.findAll(conditionsSearch)
//
//    @Transactional
//    fun addPossibleCustomer(profileId: Long, lotId: Long) {
//        val profile = profileService.getProfileByIdOrElseThrow(profileId)
//        val lot = getLotByIdOrElseThrow(lotId)
//
//        if (lot.seller == profile) {
//            throw LotException("Нельзя быть покупателем собственного объявления")
//        }
//
//        if (lot.possibleCustomers.any { it.profile == profile }) {
//            return
//        }
//
//        possibleCustomerService.save(
//            PossibleCustomer(
//                PossibleCustomer.PossibleCustomerKey(lotId, profileId),
//                lot,
//                profile,
//                Instant.now()
//            )
//        )
//    }
//
//    @Transactional
//    fun getPossibleCustomers(customerId: Long, lotId: Long): List<ProfilePreviewDTO> {
//        val lot = getLotByIdOrElseThrow(lotId)
//
//        if (lot.seller?.id != customerId) {
//            throw LotException(
//                "Невозможно просматривать потенциальных покупателей чужого объявления. " +
//                        "Объявление с id: $lotId не принадлежит авторизованному пользователю"
//            )
//        }
//
//        return lot.possibleCustomers.map { it.profile.toPreview() }
//    }
//
//    @Transactional
//    fun closeLot(sellerId: Long, lot: Lot, reason: ClosingLotReason, customerId: Long?) {
//        if (sellerId == customerId) {
//            throw LotException("Нельзя быть покупателем собственного объявления")
//        }
//
//        if (lot.status != LotStatus.ACTIVE.name) {
//            throw LotException("Неактивный лот нельзя закрыть")
//        }
//
//        if (reason == ClosingLotReason.SOLD_ON_WEBSITE && customerId == null) {
//            throw LotException("При закрытии объявления, проданного на этом сайте, необходимо указать идентификатор покупателя")
//        }
//
//        when (reason) {
//            ClosingLotReason.SOLD_ON_WEBSITE -> {
//                val customer = profileService.getProfileByIdOrElseThrow(
//                    customerId!!,
//                    initType = ProfileInitializationType.PURCHASES
//                )
//                customer.purchases.add(lot)
//                profileService.save(customer)
//                lot.status = LotStatus.CLOSED.name
//            }
//            else -> lot.status = LotStatus.CANCELED.name
//        }
//        lot.possibleCustomers.clear()
//
//        lotRepository.save(lot)
//    }
//
//    fun editLot(
//        lot: Lot,
//        request: EditLotRequest,
//        childOfRootCategory: Category
//    ): Lot {
//        val parametersMap = validateLotAndGetParametersMap(
//            contact = request.contact,
//            seller = lot.seller!!,
//            photos = request.photos,
//            rootCategoryId = childOfRootCategory.parent!!.id,
//            lotForm = lot.lotForm,
//            childOfRootCategory = childOfRootCategory,
//            gender = lot.gender,
//            parameters = request.parameters
//        )
//
//        with(lot) {
//            title = request.title
//            description = request.description
//            price = request.price
//            waysOfCommunicating = request.contact
//            parameters = request.parameters.map {
//                LotParameter(
//                    id = LotParameter.LotParameterKey(it.id, lot.id),
//                    value = it.value,
//                    parameter = parametersMap[it.id]!!,
//                    lot = lot
//                )
//            }.toMutableList()
//            photos = request.photos
//                .map {
//                    LotPhoto(
//                        lot = lot,
//                        image = imageService.getByIdOrElseThrow(it.id),
//                        order_photo = it.order
//                    )
//                }
//                .toMutableList()
//        }
//
//        return lotRepository.saveAndFlush(lot)
//    }
//}