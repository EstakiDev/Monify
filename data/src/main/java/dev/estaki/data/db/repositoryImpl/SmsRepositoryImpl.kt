package dev.estaki.data.db.repositoryImpl

import dev.estaki.domain.models.BankCardModel
import dev.estaki.domain.models.SmsModel
import dev.estaki.domain.repo.datasource.SmsDataSource
import dev.estaki.domain.repo.reposities.SmsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SmsRepositoryImpl(
    private val localDS: SmsDataSource.Local
) : SmsRepository {
    override suspend fun readAll(): Flow<List<SmsModel>> =
        localDS.readAll()

    override suspend fun read(id: Long): Flow<SmsModel> =
        localDS.read(id)

    override suspend fun deleteById(id: Long): Flow<Int> = localDS.deleteById(id)

    override suspend fun delete(entity: SmsModel): Flow<Int> = localDS.delete(entity)

    override suspend fun deleteAll(): Int = localDS.deleteAll().first()

    override suspend fun upsert(entity: SmsModel) {
        localDS.upsert(entity)
    }

    override suspend fun add(id: SmsModel) {
        localDS.add(id)
    }

    override suspend fun addAll(item: List<SmsModel>): Flow<List<Long>> =
        localDS.addAll(item)


    override suspend fun getAllCount(): Flow<Long> = localDS.getAllCount()

    override suspend fun getSmsByBankAccountNumber(accountNumber: String): Flow<List<SmsModel>> =
        localDS.getSmsByBankAccountNumber(accountNumber)

    override suspend fun getAllBankAccountNumber(): Flow<List<BankCardModel>> =
        localDS.getAllBankAccountNumber()

    override suspend fun setSmsWasSaw(smsId: Long) {
        localDS.setSmsWasSaw(smsId)
    }

    override suspend fun setIsRemoved(smsId: Long): Flow<Int> {
        return localDS.setIsRemoved(smsId)
    }


}
