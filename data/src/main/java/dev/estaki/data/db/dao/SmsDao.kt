package dev.estaki.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import dev.estaki.data.entities.SmsBankAccountProjection
import dev.estaki.data.entities.SmsEntity
import dev.estaki.domain.models.BankCardModel
import dev.estaki.domain.models.SmsModel
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsDao {

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    fun insertAll(smsList: List<SmsEntity>): List<Long>

    @Query("SELECT * FROM tb_sms ORDER BY id DESC")
    fun readAll(): List<SmsEntity>

    @Query("SELECT * FROM tb_sms ORDER BY id DESC")
    fun observeAll(): Flow<List<SmsEntity>>

    @Query("SELECT * FROM tb_sms WHERE id=:id")
    fun read(id: Long): SmsEntity

    @Query("DELETE FROM tb_sms WHERE id = :id")
    fun deleteById(id: Long): Int

    @Query("DELETE FROM tb_sms")
    fun deleteAll(): Int

    @Query("SELECT COUNT(*) FROM tb_sms")
    fun getCount(): Long

    @Query("SELECT * FROM tb_sms WHERE bankAccountNumber=:bankAccountNumber AND isRemoved = 0 ORDER BY transactionDateTime DESC")
    fun readByBankAccountNumber(bankAccountNumber: String): Flow<List<SmsEntity>>

    @Query("""
        SELECT account.bankAccountNumber, account.bankName, account.bankCardBalance
        FROM tb_sms AS account
        WHERE account.id = (
            SELECT candidate.id
            FROM tb_sms AS candidate
            WHERE candidate.bankAccountNumber = account.bankAccountNumber
            ORDER BY candidate.transactionDateTime DESC, candidate.id DESC
            LIMIT 1
        )
        ORDER BY account.transactionDateTime DESC
    """)
    fun readAllBankAccountNumber(): List<SmsBankAccountProjection>

    @Query("UPDATE tb_sms SET isSeen = 1 WHERE id=:smsId")
    fun setSmsWasSaw(smsId: Long)

    @Upsert
    fun upsert(sms: SmsEntity)

    @Delete
    fun delete(sms: SmsEntity):Int

    @Query("UPDATE tb_sms SET isRemoved = 1 WHERE id = :smsId")
    fun setIsRemoved(smsId: Long): Int

}
