package dev.estaki.domain.usecases

import dev.estaki.domain.models.SmsModel
import dev.estaki.domain.repo.reposities.SmsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SetIsRemovedSms(
    private val smsRepository: SmsRepository
) {
    suspend operator fun invoke(smsModel: SmsModel): Flow<Int> {
        val smsId = smsModel.id ?: return flowOf(0)
        return smsRepository.setIsRemoved(smsId)
    }


}
