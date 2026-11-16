package dev.estaki.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import dev.estaki.data.entities.BankAccountEntity
import dev.estaki.domain.models.BankCardModel
import kotlinx.coroutines.flow.Flow

@Dao
interface BankAccountDao {

    @Query("SELECT COUNT(*) FROM tb_bank_account")
    fun getAllCount():Long

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insertAll(smsList :List<BankAccountEntity>):List<Long>

    @Transaction
    fun mergeSmsAccounts(accounts: List<BankAccountEntity>): List<Long> {
        val existingAccounts = readAll().associateBy { it.bankAccountNumber }
        val accountsToUpdate = accounts.mapNotNull { account ->
            val existing = existingAccounts[account.bankAccountNumber]
            if (existing != null && (existing.isItFromSms != true ||
                    (!existing.bankCardNumber.isNullOrBlank() && existing.bankCardNumber != "null"))) {
                null
            } else {
                account.copy(bankCardNumber = existing?.bankCardNumber, isItFromSms = true)
            }
        }
        return insertAll(accountsToUpdate)
    }

    @Query("SELECT * FROM tb_bank_account ORDER BY id DESC")
    fun readAll():List<BankAccountEntity>

    @Query("SELECT * FROM tb_bank_account ORDER BY id DESC")
    fun observeAll(): Flow<List<BankAccountEntity>>

    @Query("SELECT * FROM tb_bank_account WHERE id=:id")
    fun read(id: Long): BankAccountEntity

    @Query("SELECT * FROM tb_bank_account WHERE bankAccountNumber=:bankAccountNumber")
    fun readByBankAccountNumber(bankAccountNumber: String): BankAccountEntity

    @Query("SELECT * \n" +
            "  FROM tb_bank_account \n" +
            " GROUP BY bankAccountNumber;")
    fun readAllBankAccountNumber(): List<BankCardModel>

    @Upsert
    fun upsert(bankAccountEntity: BankAccountEntity)

    @Delete
    fun delete(bankAccountEntity: BankAccountEntity):Int

    @Query("DELETE FROM tb_bank_account WHERE id = :id")
    fun deleteById(id: Long):Int

    @Query("DELETE FROM tb_bank_account")
    fun deleteAll():Int

}
