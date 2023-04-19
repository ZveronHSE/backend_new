package ru.zveron.order.persistence.repository

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import mu.KLogging
import org.jooq.DSLContext
import org.jooq.kotlin.coroutines.transactionCoroutine
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import ru.zveron.order.persistence.jooq.models.ORDER_LOT


@Component
class WaterfallStorage(
        private val ctx: DSLContext,
) : OrderWaterfallRepository {

    companion object : KLogging()

    override suspend fun findAll(lastId: Long, pageSize: Int) {

        val result = Flux.from(ctx.select(ORDER_LOT.ID)
                .from(ORDER_LOT)
                .orderBy(ORDER_LOT.ID)
                .seek(lastId)
                .limit(pageSize)
        )
                .log()
                .map { r -> r.get(ORDER_LOT.ID) }
                .collectList()
                .block()

        logger.debug { "result: $result" }
    }

}

data class OrderLotWrapper(
        val id: Long,
        val animalId: Long,
        val price: String,
        val createdAt: String,
)