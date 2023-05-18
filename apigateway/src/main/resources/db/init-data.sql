truncate public.method_metadata;

insert into public.method_metadata (alias, service_name, grpc_service_name, grpc_method_name, access_scope)
values ('uploadImage', 'object-storage', 'BucketService', 'UploadImage', 'ANY'),
       ('authPerformLogout', 'auth-service', 'AuthServiceExternal', 'PerformLogout', 'BUYER'),
       ('objectStorageUploadImage', 'object-storage', 'BucketService', 'UploadImage', 'ANY'),
       ('blacklistAdd', 'blacklist-service', 'BlacklistServiceExternal', 'AddToBlacklist', 'BUYER'),
       ('blacklistGet', 'blacklist-service', 'BlacklistServiceExternal', 'GetBlacklist', 'BUYER'),
       ('blacklistDeleteOne', 'blacklist-service', 'BlacklistServiceExternal', 'DeleteFromBlacklist', 'BUYER'),
       ('lotFavoritesAdd', 'favorites-service', 'LotFavoritesServiceExternal', 'AddToFavorites', 'BUYER'),
       ('lotFavoritesDelete', 'favorites-service', 'LotFavoritesServiceExternal', 'RemoveFromFavorites', 'BUYER'),
       ('lotFavoritesGet', 'favorites-service', 'LotFavoritesServiceExternal', 'GetFavoriteLots', 'BUYER'),
       ('lotFavoritesDeleteByStatusAndCategory', 'favorites-service', 'LotFavoritesServiceExternal',
        'DeleteAllByStatusAndCategory', 'BUYER'),
       ('lotFavoritesDeleteByCategory', 'favorites-service', 'LotFavoritesServiceExternal', 'DeleteAllByCategory',
        'BUYER'),
       ('profileFavoriteAdd', 'favorites-service', 'ProfileFavoritesServiceExternal', 'AddToFavorites', 'BUYER'),
       ('profileFavoriteRemove', 'favorites-service', 'ProfileFavoritesServiceExternal', 'RemoveFromFavorites',
        'BUYER'),
       ('profileFavoriteGet', 'favorites-service', 'ProfileFavoritesServiceExternal', 'GetFavoriteProfiles', 'BUYER'),
       ('lotFormsGet', 'parameter-service', 'ParameterExternalService', 'getLotForms', 'ANY'),
       ('parametersGet', 'parameter-service', 'ParameterExternalService', 'getParameters', 'ANY'),
       ('categoryChildrenGet', 'parameter-service', 'CategoryExternalService', 'getChildren', 'ANY'),
       ('categoryRootGet', 'parameter-service', 'CategoryExternalService', 'getRoot', 'ANY'),
       ('profileGetPage', 'profile-service', 'ProfileServiceExternal', 'GetProfilePage', 'ANY'),
       ('profileGetInfo', 'profile-service', 'ProfileServiceExternal', 'GetProfileInfo', 'BUYER'),
       ('profileSetInfo', 'profile-service', 'ProfileServiceExternal', 'SetProfileInfo', 'BUYER'),
       ('profileGetSettings', 'profile-service', 'ProfileServiceExternal', 'GetSettings', 'BUYER'),
       ('profileSetSettings', 'profile-service', 'ProfileServiceExternal', 'SetSettings', 'BUYER'),
       ('profileGetChannelTypes', 'profile-service', 'ProfileServiceExternal', 'GetChannelTypes', 'BUYER'),
       ('profileGetLinks', 'profile-service', 'ProfileServiceExternal', 'GetLinks', 'BUYER'),
       ('profileDelete', 'profile-service', 'ProfileServiceExternal', 'DeleteProfile', 'BUYER'),
       ('authPhoneLoginInit', 'auth-service', 'AuthServiceExternal', 'PhoneLoginInit', 'ANY'),
       ('authPhoneLoginVerify', 'auth-service', 'AuthServiceExternal', 'PhoneLoginVerify', 'ANY'),
       ('authIssueNewTokens', 'auth-service', 'AuthServiceExternal', 'IssueNewTokens', 'ANY'),
       ('cardLotGet', 'lot-service', 'LotExternalProtoService', 'GetCardLot', 'ANY'),
       ('lotClose', 'lot-service', 'LotExternalProtoService', 'CloseLot', 'BUYER'),
       ('lotCreate', 'lot-service', 'LotExternalProtoService', 'CreateLot', 'BUYER'),
       ('lotEdit', 'lot-service', 'LotExternalProtoService', 'EditLot', 'BUYER'),
       ('waterfallGet', 'lot-service', 'LotExternalProtoService', 'GetWaterfall', 'ANY'),
       ('authRegisterByPhone', 'auth-service', 'AuthServiceExternal', 'RegisterByPhone', 'ANY'),
       ('authLoginByPassword', 'auth-service', 'AuthServiceExternal', 'LoginByPassword', 'ANY'),
       ('authLoginBySocialMedia', 'auth-service', 'AuthServiceExternal', 'LoginBySocial', 'ANY'),
       ('addressGetSubwaysByCity', 'address-service', 'SubwayStationExternalService', 'GetSubwayStationsByCity', 'ANY'),
       ('orderGetOrder', 'order-service', 'OrderServiceExternal', 'GetOrder', 'ANY'),
       ('orderGetWaterfall', 'order-service', 'OrderWaterfallServiceExternal', 'GetWaterfall', 'ANY'),
       ('authLoginBySocialMedia', 'auth-service', 'AuthServiceExternal', 'LoginBySocial', 'ANY'),
       ('profileCreateAnimal', 'profile-service', 'AnimalGrpcExternal', 'CreateAnimal', 'BUYER'),
       ('profileGetAnimal', 'profile-service', 'AnimalGrpcExternal', 'GetAnimal', 'BUYER'),
       ('profileGetAnimalsByProfile', 'profile-service', 'AnimalGrpcExternal', 'GetAnimalsByProfile', 'BUYER'),
       ('specialistEditName', 'specialist-service', 'ManageSpecialistExternalService', 'EditName', 'BUYER'),
       ('specialistEditDescription', 'specialist-service', 'ManageSpecialistExternalService', 'EditDescription',
        'BUYER'),
       ('specialistAddEducation', 'specialist-service', 'ManageSpecialistExternalService', 'AddEducation', 'BUYER'),
       ('specialistEditEducation', 'specialist-service', 'ManageSpecialistExternalService', 'EditEducation', 'BUYER'),
       ('specialistDeleteEducation', 'specialist-service', 'ManageSpecialistExternalService', 'DeleteEducation',
        'BUYER'),
       ('specialistAddWorkExperience', 'specialist-service', 'ManageSpecialistExternalService', 'AddWorkExperience',
        'BUYER'),
       ('specialistEditWorkExperience', 'specialist-service', 'ManageSpecialistExternalService', 'EditWorkExperience',
        'BUYER'),
       ('specialistDeleteWorkExperience', 'specialist-service', 'ManageSpecialistExternalService',
        'DeleteWorkExperience', 'BUYER'),
       ('specialistAddAchievement', 'specialist-service', 'ManageSpecialistExternalService', 'AddAchievement', 'BUYER'),
       ('specialistEditAchievement', 'specialist-service', 'ManageSpecialistExternalService', 'EditAchievement',
        'BUYER'),
       ('specialistDeleteAchievement', 'specialist-service', 'ManageSpecialistExternalService', 'DeleteAchievement',
        'BUYER'),
       ('specialistAddOther', 'specialist-service', 'ManageSpecialistExternalService', 'AddOther', 'BUYER'),
       ('specialistEditOther', 'specialist-service', 'ManageSpecialistExternalService', 'EditOther', 'BUYER'),
       ('specialistDeleteOther', 'specialist-service', 'ManageSpecialistExternalService', 'DeleteOther', 'BUYER'),
       ('specialistAddService', 'specialist-service', 'ManageSpecialistExternalService', 'AddService', 'BUYER'),
       ('specialistEditService', 'specialist-service', 'ManageSpecialistExternalService', 'EditService', 'BUYER'),
       ('specialistDeleteService', 'specialist-service', 'ManageSpecialistExternalService', 'DeleteService', 'BUYER'),
       ('specialistEditDocuments', 'specialist-service', 'ManageSpecialistExternalService', 'EditDocuments', 'BUYER'),
       ('profileCreateAnimal', 'profile-service', 'AnimalGrpcExternal', 'CreateAnimal', 'BUYER'),
       ('profileGetAnimal', 'profile-service', 'AnimalGrpcExternal', 'GetAnimal', 'BUYER'),
       ('profileGetAnimalsByProfile', 'profile-service', 'AnimalGrpcExternal', 'GetAnimalsByProfile', 'BUYER'),
       ('orderGetCustomerInfo', 'order-service', 'OrderCustomerServiceExternal', 'GetCustomer', 'ANY'),
       ('orderCreateOrder', 'order-service', 'OrderServiceExternal', 'CreateOrder', 'ANY'),
       ('orderFilteredCount', 'order-service', 'OrderWaterfallServiceExternal', 'GetFilteredCount', 'ANY'),
       ('orderGetByProfile', 'order-service', 'OrderServiceExternal', 'GetOrdersByProfile', 'ANY')

ON CONFLICT (alias) DO NOTHING;