package ru.zveron.service.presentation.api

import ru.zveron.exception.FavoritesException
import kotlin.coroutines.CoroutineContext

interface SecuredService {

    /**
     * @throws FavoritesException если id авторизованного пользователя отсутсвует в метадате
     */
    fun CoroutineContext.getAuthorizedProfileId(): Long
}