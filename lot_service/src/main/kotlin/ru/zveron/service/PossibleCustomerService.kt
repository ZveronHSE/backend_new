package ru.zveron.service

import org.springframework.stereotype.Service
import ru.zveron.entity.PossibleCustomer
import ru.zveron.repository.PossibleCustomerRepository
import java.time.Instant

@Service
class PossibleCustomerService(
    private val possibleCustomerRepository: PossibleCustomerRepository
) {

    fun save(profileId: Long, lotId: Long, possibleCustomer: PossibleCustomer) {
//        val possibleCustomer = PossibleCustomer(
//            PossibleCustomer.PossibleCustomerKey(lotId, profileId),
//            lot,
//            Instant.now()
//        )
        possibleCustomerRepository.save(possibleCustomer)
    }

    fun deleteByCustomerId(id: Long) = possibleCustomerRepository.deleteByCustomerId(id)

    fun deleteAllByDateOfConversationBeginningLessThan(date: Instant) =
        possibleCustomerRepository.deleteAllByDateOfConversationBeginningLessThan(date)
}