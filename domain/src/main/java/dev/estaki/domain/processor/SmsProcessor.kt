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
        if (!SmsValidator.isBankSms(raw)) return null
        val body = SmsValidator.normalize(raw.description)
        val labeledAmount = labeledAmountRegex.find(body)?.groupValues?.get(1)
            ?: directionalAmountRegex.find(body)?.groupValues?.get(1)
            ?: englishAmountRegex.find(body)?.groupValues?.get(1)
        val amountMatch = labeledAmount?.let { amountRegex.find(it) }
            ?: amountRegex.findAll(body).firstOrNull { match ->
                val contextBefore = body.substring(maxOf(0, match.range.first - 14), match.range.first)
                !contextBefore.contains("مانده") && !contextBefore.contains("موجودی") &&
                    !contextBefore.contains("کارت") && !contextBefore.contains("حساب")
            }
            ?: return null
        val amount = amountMatch.value.replace(Regex("[,،٬]"), "")
        val type = if (withdrawalWords.any(body::contains) || englishWithdrawalWords.any(body::contains) || amountMatch.range.first > 0 && body[amountMatch.range.first - 1] == '-') {
            TransactionType.WITHDRAW
        } else {
            TransactionType.DEPOSIT
        }
        val balance = (balanceRegex.find(body)?.groupValues?.getOrNull(1)
            ?: englishBalanceRegex.find(body)?.groupValues?.getOrNull(1))?.replace(Regex("[,،٬]"), "") ?: "-"
        val account = (accountRegex.find(body)?.groupValues?.getOrNull(1)
            ?: englishAccountRegex.find(body)?.groupValues?.getOrNull(1))?.ifBlank { "-" } ?: "-"
        val time = timeRegex.find(body)?.value ?: "-"
        val bankName = raw.senderName.takeIf { it.any(Char::isLetter) } ?: body.lineSequence().firstOrNull().orEmpty()
        val epoch = raw.receiveDateTime.toLongOrNull() ?: 0L

        return SmsModel(
            id = stableId(raw),
            bankName = bankName,
            bankAccountNumber = account,
            transactionType = type,
            transactionAmount = amount,
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
        val englishWithdrawalWords = listOf("withdraw", "purchase", "payment", "debited", "debit")
        val englishAmountRegex = Regex("(?:amount|deposit|withdrawal|purchase|payment)\\s*[:：]?\\s*([\\d,]+)")
        val englishBalanceRegex = Regex("balance\\s*[:：]?\\s*([\\d,]+)")
        val englishAccountRegex = Regex("(?:card|account)\\s*[:：]?\\s*([\\d* -]{4,})")
        val withdrawalWords = listOf("برداشت", "خرید", "پرداخت", "انتقال از")
        val amountRegex = Regex("(?<!\\d)\\d{1,3}(?:[,،٬]\\d{3})+(?!\\d)|(?<!\\d)\\d{4,}(?!\\d)")
        val labeledAmountRegex = Regex("مبلغ(?:\\s*تراکنش)?\\s*[:：]?\\s*([\\d,،٬]+)")
        val directionalAmountRegex = Regex("(?:واریز|برداشت|خرید|پرداخت)\\s*[:：]\\s*([\\d,،٬]+)")
        val balanceRegex = Regex("(?:مانده|موجودی)(?:\\s*(?:حساب|:))?\\s*[:：]?\\s*([\\d,،٬]+)")
        val accountRegex = Regex("(?:کارت|حساب|شماره کارت|شماره حساب)\\s*[:：]?\\s*([\\d*۰-۹٠-٩-]{4,})")
        val timeRegex = Regex("(?<!\\d)(?:[01]?\\d|2[0-3]):[0-5]\\d(?::[0-5]\\d)?(?!\\d)")
    }
}
