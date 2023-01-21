package ru.zveron.apigateway.config

import io.grpc.NameResolverRegistry
import net.devh.boot.grpc.client.nameresolver.DiscoveryClientResolverFactory
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

@Configuration
class EurekaConfig(
    private val client: EurekaDiscoveryClient,
) {

    @PostConstruct
    fun eurekaDiscoveryClientFactory() = NameResolverRegistry
        .getDefaultRegistry()
        .register(DiscoveryClientResolverFactory(client))
}
