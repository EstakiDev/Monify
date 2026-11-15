package dev.estaki.domain.processor

import dev.estaki.domain.models.SmsModel
import dev.estaki.domain.models.SmsRawModel
import dev.estaki.domain.models.TransactionType
import dev.estaki.domain.validator.SmsValidator
import java.nio.ByteBuffer
import java.security.MessageDigest

/** Converts transaction notification SMS messages into the app's transaction model. */
class SmsProcessor(private val smsRawModelList: List<SmsRawModel>) {
    fun execute(): List<SmsModel> = smsRawModelList.mapNotNull(::parse)

    private fun parse(raw: SmsRawModel): SmsModel? {
        val transaction = SmsValidator.classify(raw) ?: return null
        val body = SmsValidator.normalize(raw.description)
        val accountBody = raw.description.lineSequence().joinToString("\n") { SmsValidator.normalize(it) }
        val sourceAccountBody = if (transaction.type == TransactionType.WITHDRAW &&
            outgoingAccountTransfer.containsMatchIn(accountBody)
        ) accountBody.substringBefore("به کارت") else accountBody
        val balance = (balanceRegex.find(body)?.groupValues?.getOrNull(1)
            ?: englishBalanceRegex.find(body)?.groupValues?.getOrNull(1))?.replace(Regex("[,،٬]"), "") ?: "-"
        val account = (accountRegex.find(sourceAccountBody)?.groupValues?.getOrNull(1)
            ?: englishAccountRegex.find(sourceAccountBody)?.groupValues?.getOrNull(1))?.ifBlank { "-" } ?: "-"
        val time = timeRegex.find(body)?.value ?: "-"
        val bankName = raw.senderName.takeIf { it.any(Char::isLetter) } ?: body.lineSequence().firstOrNull().orEmpty()
        val epoch = raw.receiveDateTime.toLongOrNull() ?: 0L

        return SmsModel(
            id = stableId(raw),
            bankName = bankName,
            bankAccountNumber = account,
            transactionType = transaction.type,
            transactionAmount = transaction.amount,
            transactionDate = raw.receiveDate.ifBlank { "-" },
            transactionDateTime = epoch,
            transactionTime = time,
            bankCardBalance = balance,
            categoryIds = emptyList(),
            description = null,
            smsSender = raw.senderName,
            smsBody = raw.description
        )
    }

    private fun stableId(raw: SmsRawModel): Long {
        val identity = "${raw.senderName}\u0000${raw.receiveDateTime}\u0000${raw.description}"
        val digest = MessageDigest.getInstance("SHA-256").digest(identity.toByteArray(Charsets.UTF_8))
        return ByteBuffer.wrap(digest, 0, Long.SIZE_BYTES).long and Long.MAX_VALUE
    }

    private companion object {
        val outgoingAccountTransfer = Regex("از حساب.{0,120}به کارت", RegexOption.DOT_MATCHES_ALL)
        val englishBalanceRegex = Regex("balance\\s*[:：]?\\s*([\\d,]+)")
        val englishAccountRegex = Regex("(?:card|account)[ \\t]*[:：]?[ \\t]*([\\d* -]{4,})")
        val balanceRegex = Regex("(?:مانده|موجودی)(?:\\s*(?:حساب|:))?\\s*[:：]?\\s*([\\d,،٬]+)")
        val accountRegex = Regex("(?:شماره کارت|شماره حساب|کارت|حساب)[ \\t]*[:：]?[ \\t]*([\\d*×-]{4,}(?: +[\\d*×-]+)*)")
        val timeRegex = Regex("(?<!\\d)(?:[01]?\\d|2[0-3]):[0-5]\\d(?::[0-5]\\d)?(?!\\d)")
    }
}
