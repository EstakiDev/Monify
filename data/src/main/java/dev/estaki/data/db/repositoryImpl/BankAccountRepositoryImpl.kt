package dev.estaki.data.db.repositoryImpl

import dev.estaki.domain.models.BankCardModel
import dev.estaki.domain.repo.datasource.BankAccountDataSource
import dev.estaki.domain.repo.reposities.BankAccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class BankAccountRepositoryImpl(
    private val localDS: BankAccountDataSource.Local
) : BankAccountRepository {
    override suspend fun getByBankAccountNumber(accountNumber: String): Flow<BankCardModel> =
        localDS.getByBankAccountNumber(accountNumber)


    override suspend fun getAllBankAccountNumber(): Flow<List<BankCardModel>> =
        localDS.getAllBankAccountNumber()

    override suspend fun readAll(): Flow<List<BankCardModel>> =
        localDS.readAll()

    override suspend fun read(id: Long): Flow<BankCardModel> = localDS.read(id)

    override suspend fun deleteById(id: Long): Flow<Int> = localDS.deleteById(id)
    override suspend fun delete(entity: BankCardModel): Flow<Int> =localDS.delete(entity)

    override suspend fun deleteAll(): Int = localDS.deleteAll().first()

    override suspend fun upsert(entity: BankCardModel) {
       localDS.upsert(entity)
    }

    override suspend fun add(entity: BankCardModel) {
        localDS.add(entity)
    }

    override suspend fun addAll(item: List<BankCardModel>): Flow<List<Long>> = localDS.addAll(item)

    override suspend fun getAllCount(): Flow<Long> = localDS.getAllCount()


}
