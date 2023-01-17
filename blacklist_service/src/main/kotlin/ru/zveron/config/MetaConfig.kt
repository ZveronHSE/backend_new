package ru.zveron.config


import mu.KLogging
import net.devh.boot.grpc.server.service.GrpcServiceDiscoverer
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaRegistration
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@ConditionalOnClass(EurekaRegistration::class)
class DiscoveryMetadataConfig(
    private val registry: GrpcServiceDiscoverer,
    private val infoManager: EurekaRegistration,
) {

    companion object : KLogging()

    @PostConstruct
    fun registerServiceMethods() {
        registry.findGrpcServices().flatMap { it.definition.serviceDescriptor.methods }.forEach {
            infoManager.instanceConfig.metadataMap[it.bareMethodName] = it.fullMethodName
        }
    }
}