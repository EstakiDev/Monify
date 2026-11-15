package dev.estaki.data.db.datasourceImpl

import dev.estaki.data.db.dao.BankAccountDao
import dev.estaki.data.db.dao.SmsDao
import dev.estaki.data.mapper.toDbEntity
import dev.estaki.data.mapper.toDomainModel
import dev.estaki.domain.models.BankCardModel
import dev.estaki.domain.models.SmsRawModel
import dev.estaki.domain.validator.SmsValidator
import dev.estaki.domain.repo.datasource.BankAccountDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class BankAccountLocalDataSourceImpl(private val bankAccountDao: BankAccountDao, private val smsDao: SmsDao) :
    BankAccountDataSource.Local {
    override suspend fun readAll(): Flow<List<BankCardModel>> =
        flow {
            val supportedAccounts = smsDao.readAll().asSequence()
                .filter { sms -> sms.isModified || SmsValidator.isBankSms(SmsRawModel(
                    sms.id?.toString().orEmpty(), sms.smsSender, sms.smsBody,
                    sms.transactionDate, sms.transactionDateTime.toString()
                )) }
                .map { it.bankAccountNumber }
                .filter { account -> account.count(Char::isDigit) >= 4 }
                .toSet()
            emit(bankAccountDao.readAll().map { it.toDomainModel() }.filter { card ->
                card.isItFromSms != true || (!card.bankCardNumber.isNullOrBlank() && card.bankCardNumber != "null") ||
                    card.bankAccountNumber in supportedAccounts
            })
        }.flowOn(Dispatchers.IO)

    override suspend fun read(id: Long): Flow<BankCardModel> =
        flow { emit(bankAccountDao.read(id).toDomainModel()) }.flowOn(Dispatchers.IO)

    override suspend fun deleteById(id: Long): Flow<Int> =
    flow { emit(bankAccountDao.deleteById(id)) }.flowOn(Dispatchers.IO)

    override suspend fun delete(entity: BankCardModel): Flow<Int> =
        flow { emit(bankAccountDao.delete(entity.toDbEntity())) }.flowOn(Dispatchers.IO)

    override suspend fun deleteAll(): Flow<Int> =
        flow { emit(bankAccountDao.deleteAll()) }.flowOn(Dispatchers.IO)


    override suspend fun upsert(entity: BankCardModel) =
        withContext(Dispatchers.IO){
            bankAccountDao.upsert(entity.toDbEntity())
        }

    override suspend fun add(entity: BankCardModel) {
        upsert(entity)
    }

    override suspend fun addAll(item: List<BankCardModel>): Flow<List<Long>> =
        flow {
            emit(
                bankAccountDao.mergeSmsAccounts(item.map { it.toDbEntity() }
                ))
        }.flowOn(Dispatchers.IO)

    override suspend fun getAllCount(): Flow<Long> =
        flow {
            emit(
                bankAccountDao.getAllCount()
            )
        }.flowOn(Dispatchers.IO)

    override suspend fun getByBankAccountNumber(accountNumber: String): Flow<BankCardModel> =
        flow {
            emit(
                bankAccountDao.readByBankAccountNumber(accountNumber).toDomainModel()
            )
        }.flowOn(Dispatchers.IO)

    override suspend fun getAllBankAccountNumber(): Flow<List<BankCardModel>> = readAll()


}
