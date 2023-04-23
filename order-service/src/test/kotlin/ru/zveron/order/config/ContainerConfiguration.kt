package ru.zveron.order.config

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@ExtendWith(TestContainerExtension::class)
abstract class ContainerConfiguration {

    companion object {
        @DynamicPropertySource
        @JvmStatic
        fun properties(registry: DynamicPropertyRegistry) {
            TestContainerExtension.registerStaticProperties(registry)
        }
    }
}
