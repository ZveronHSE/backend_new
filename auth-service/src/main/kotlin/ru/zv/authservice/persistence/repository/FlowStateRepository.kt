package ru.zv.authservice.persistence.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.zv.authservice.persistence.entity.FlowState

interface FlowStateRepository : CoroutineCrudRepository<FlowState, Long> {

}