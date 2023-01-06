package ru.zveron.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.DataBaseApplicationTest
import ru.zveron.repository.ParameterFromTypeRepository

internal class ParameterServiceTest : DataBaseApplicationTest() {
    @Autowired
    lateinit var parameterService: ParameterService

    @Autowired
    lateinit var lotFormService: LotFormService

    @Autowired
    lateinit var categoryService: CategoryService

    @Autowired
    lateinit var parameterFromTypeRepository: ParameterFromTypeRepository

    @BeforeEach
    fun `Init data for lot form, category, parameters`() {

    }

    @Test
    fun `GetParametersByCategory `() {

    }
}