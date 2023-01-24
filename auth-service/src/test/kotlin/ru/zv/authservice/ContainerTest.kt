package ru.zv.authservice

import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import ru.zv.authservice.config.JsonTestConfig
import ru.zv.authservice.persistence.FlowStateStorage
import ru.zv.authservice.util.randomLoginFlowContext

@Import(value = [FlowStateStorage::class, JsonTestConfig::class])
class ContainerTest(
) : BaseAuthTest() {

    companion object : KLogging()


    @Autowired
    lateinit var storage: FlowStateStorage


    @Test
    fun `container loads`(): Unit = runBlocking {
//        val stateStorage = FlowStateStorage(flowStateRepository, ObjectMapper().findAndRegisterModules())
        val temp = storage.createContext(randomLoginFlowContext())
        logger.info { temp }
    }
}