package dev.estaki.data.db.datasourceImpl

import dev.estaki.data.db.dao.SmsDao
import dev.estaki.data.mapper.toDbEntity
import dev.estaki.data.mapper.toDomainModel
import dev.estaki.domain.models.BankCardModel
import dev.estaki.domain.models.SmsModel
import dev.estaki.domain.models.SmsRawModel
import dev.estaki.domain.validator.SmsValidator
import dev.estaki.domain.repo.datasource.SmsDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class SmsLocalDatasourceImpl(
    private val smsDao: SmsDao
) : SmsDataSource.Local {
    override suspend fun readAll(): Flow<List<SmsModel>> =
        flow { emit(smsDao.readAll().map { it.toDomainModel() }) }.flowOn(Dispatchers.IO)

    override suspend fun read(id: Long): Flow<SmsModel> =
        flow { emit(
            smsDao.read(id).toDomainModel()
        ) }.flowOn(Dispatchers.IO)

    override suspend fun deleteById(id: Long): Flow<Int> =
        flow { emit(smsDao.deleteById(id)) }.flowOn(Dispatchers.IO)

    override suspend fun delete(entity: SmsModel): Flow<Int> = flow { emit(
        smsDao.delete(entity.toDbEntity())
    ) }.flowOn(Dispatchers.IO)

    override suspend fun deleteAll(): Flow<Int> = flow { emit(smsDao.deleteAll()) }.flowOn(Dispatchers.IO)

    override suspend fun upsert(entity: SmsModel) {
        withContext(Dispatchers.IO) {
            val en = entity.toDbEntity()
            smsDao.upsert(en)
        }
    }

    override suspend fun add(entity: SmsModel) = upsert(entity)

    override suspend fun addAll(item: List<SmsModel>): Flow<List<Long>> =
        flow { emit(smsDao.insertAll(item.map { it.toDbEntity() })) }.flowOn(Dispatchers.IO)


    override suspend fun getAllCount(): Flow<Long> = flow { emit(smsDao.getCount()) }.flowOn(Dispatchers.IO)

    override suspend fun getSmsByBankAccountNumber(accountNumber: String): Flow<List<SmsModel>> =
        flow {
            emit(smsDao.readByBankAccountNumber(accountNumber)
                .map { it.toDomainModel() }
                .mapNotNull { it.withCurrentClassification() })
        }.flowOn(Dispatchers.IO)

    private fun SmsModel.withCurrentClassification(): SmsModel? {
        if (isModified) return this
        val transaction = SmsValidator.classify(
            SmsRawModel(id?.toString().orEmpty(), smsSender, smsBody, transactionDate, transactionDateTime.toString())
        ) ?: return null
        return copy(transactionType = transaction.type, transactionAmount = transaction.amount)
    }

    override suspend fun getAllBankAccountNumber(): Flow<List<BankCardModel>> =
        flow {
            emit(smsDao.readAllBankAccountNumber().map { account ->
                BankCardModel(
                    id = account.bankAccountNumber.hashCode(),
                    bankName = account.bankName,
                    bankAccountNumber = account.bankAccountNumber,
                    bankCardBalance = account.bankCardBalance,
                    bankCardNumber = null,
                    isItFromSms = true
                )
            })
        }.flowOn(Dispatchers.IO)

    override suspend fun setSmsWasSaw(smsId: Long) {
        withContext(Dispatchers.IO) {
            smsDao.setSmsWasSaw(smsId)
        }
    }

    override suspend fun setIsRemoved(smsId: Long): Flow<Int> =
    flow { emit(smsDao.setIsRemoved(smsId))}.flowOn(Dispatchers.IO)
}
