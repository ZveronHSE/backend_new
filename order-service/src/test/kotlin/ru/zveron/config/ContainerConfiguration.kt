package ru.zveron.config

import io.opentelemetry.api.GlobalOpenTelemetry
import org.junit.jupiter.api.BeforeAll
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

        @JvmStatic
        @BeforeAll
        fun setUp() {
            GlobalOpenTelemetry.resetForTest()
        }
    }
}
