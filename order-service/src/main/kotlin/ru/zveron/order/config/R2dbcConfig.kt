package ru.zveron.order.config

import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.PostgresqlConnectionFactoryProvider
import io.r2dbc.spi.ConnectionFactoryOptions
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.r2dbc.convert.EnumWriteSupport
import ru.zveron.order.persistence.model.constant.ServiceDeliveryType
import ru.zveron.order.persistence.model.constant.ServiceType
import ru.zveron.order.persistence.model.constant.Status

@Configuration
class R2dbcConfig(
    private val properties: R2dbcProperties,
) {

//    /**
//     * Postgresql enum types map, where:
//     *
//     * key - postgres enum type name
//     * value - kotlin enum class
//     */
//    val customEnumTypes = hashMapOf<String, KClass<*>>(
//        "status" to Status::class,
//        "service_type" to ServiceType::class,
//        "service_delivery_type" to ServiceDeliveryType::class,
//    ).mapValues {
//        @Suppress("UNCHECKED_CAST")
//        it.value as KClass<Enum<*>>
//    }

    @Bean(destroyMethod = "dispose")
    fun connectionPool(): ConnectionPool {
        ConnectionFactoryOptions.builder()
        val options = ConnectionFactoryBuilder
            .withUrl(properties.url)
            .username(properties.username)
            .password(properties.password)
            .buildOptions()

        val pgConfigurationBuilder = PostgresqlConnectionFactoryProvider.builder(options)

//        // Only register enum codec when enum types is not empty. Otherwise, r2dbc fails to connect with syntax error:
//        // empty brackets ("... IN ()")
//        if (customEnumTypes.isNotEmpty()) {
//            pgConfigurationBuilder.codecRegistrar(buildEnumCodecRegistrar())
//        }

        val connectionFactory = PostgresqlConnectionFactory(pgConfigurationBuilder.build())

        return ConnectionPoolConfiguration.builder(connectionFactory)
            .buildConfigurationBy(properties.pool)
            .let { ConnectionPool(it) }
    }

//    @Bean
//    fun r2dbcCustomConversions(): R2dbcCustomConversions = R2dbcCustomConversions(listOf(EnumToStringConverter()))

    private fun getR2dbcEnumConverters(): List<EnumWriteSupport<*>> {
        return listOf(
            getR2dbcEnumConverter<ServiceType>(),
            getR2dbcEnumConverter<Status>(),
            getR2dbcEnumConverter<ServiceDeliveryType>(),
        )
    }

    private fun ConnectionPoolConfiguration.Builder.buildConfigurationBy(
        props: R2dbcProperties.Pool,
    ): ConnectionPoolConfiguration {
        maxIdleTime(props.maxIdleTime)

        props.maxLifeTime?.let { maxLifeTime(it) }
        props.maxAcquireTime?.let { maxAcquireTime(it) }
        props.maxCreateConnectionTime?.let { maxCreateConnectionTime(it) }

        initialSize(props.initialSize)
        maxSize(props.maxSize)

        props.validationQuery?.let { validationQuery(it) }
        validationDepth(props.validationDepth)

        return build()
    }

//    private fun buildEnumCodecRegistrar(): CodecRegistrar {
//        val builder = EnumCodec.builder()
//        customEnumTypes.map { (pgEnumName, kClass) -> builder.withEnum(pgEnumName, kClass.java) }
//        return builder.build()
//    }

    /**
     * Postgresql enum types converter
     */
    private inline fun <reified E : Enum<E>> getR2dbcEnumConverter() = object : EnumWriteSupport<E>() {}
}


class EnumToStringConverter : Converter<Status, String> {
    override fun convert(source: Status): String {
        return source.name
    }
}

