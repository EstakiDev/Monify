package dev.estaki.domain.validator

import dev.estaki.domain.models.SmsRawModel
import dev.estaki.domain.models.TransactionType
import java.util.Locale

/** Classifies completed transactions only when the message contains supporting account evidence. */
object SmsValidator {
    data class Transaction(val type: TransactionType, val amount: String)

    private const val NUMBER = "[0-9]+(?:[,،٬][0-9]{3})*"
    private val debit = Regex("برداشت|خرید|پرداخت|کسر|withdraw(?:al|n)?|purchase|debited|\\bdebit\\b", RegexOption.IGNORE_CASE)
    private val credit = Regex("واریز|واريز|بستانکار|credited|\\bcredit\\b|deposit(?:ed)?", RegexOption.IGNORE_CASE)
    private val accountEvidence = Regex("(?<![\\p{L}])حساب(?![\\p{L}])|کارت|سپرده|مانده|موجودی|بانک|شبا|account|card|balance|bank", RegexOption.IGNORE_CASE)
    private val excluded = Regex(
        "رمز پویا|رمز یک ?بار|رمز دوم|رمز ورود|کد تایید|کد تأیید|کد فعال ?سازی|" +
            "ناموفق|موفق نبود|انجام نشد|انجام نشده|رد شد|لغو شد|لغو گردید|" +
            "امکان واریز|امکان برداشت|امکان خرید|برای واریز|برای برداشت|جهت واریز|جهت برداشت|" +
            "جایزه|قرعه کشی|تبریک|تخفیف|تبلیغ|" +
            "(?:برداشت|واریز|کسر|پرداخت).{0,100}خواهد (?:شد|گردید)|" +
            "در غیر این صورت.{0,100}(?:کسر|برداشت)|پرداخت (?:گردد|کنید|نمایید|شود)|جهت پرداخت|" +
            "one.time password|verification code|\\botp\\b|login code|failed|unsuccessful|declined|cancelled|canceled",
        RegexOption.IGNORE_CASE
    )
    private val labeledAmount = Regex("(?:مبلغ(?:\\s*تراکنش)?|amount)\\s*[:：]?\\s*($NUMBER)(?![0-9]|[,،٬][0-9]|[/:٫])", RegexOption.IGNORE_CASE)
    private val directionAmount = Regex("(?:واریز|برداشت|خرید|پرداخت|کسر|deposit|withdrawal|purchase|payment)\\s*(?:مبلغ)?\\s*[:：]?\\s*([+-]?)\\s*($NUMBER)(?![0-9]|[,،٬][0-9]|[/:٫])", RegexOption.IGNORE_CASE)
    private val signedAmount = Regex("(?<![0-9])([+-])\\s*($NUMBER)(?![0-9,،٬])")
    private val outgoingTransfer = Regex("از حساب.{0,120}به (?:کارت|حساب).{0,160}واریز", RegexOption.IGNORE_CASE)

    fun isBankSms(smsRawModel: SmsRawModel): Boolean = classify(smsRawModel) != null

    fun classify(sms: SmsRawModel): Transaction? {
        val body = normalize(sms.description)
        if (body.isBlank() || excluded.containsMatchIn(body) || !accountEvidence.containsMatchIn(body)) return null

        val labeled = labeledAmount.find(body)
        val directional = directionAmount.find(body)
        val signed = signedAmount.find(body)
        val amount = labeled?.groupValues?.get(1)
            ?: directional?.groupValues?.get(2)
            ?: signed?.groupValues?.get(2)
            ?: return null
        val digits = amount.filter(Char::isDigit)
        if (digits.isEmpty() || digits.all { it == '0' }) return null

        val hasDebit = debit.containsMatchIn(body)
        val hasCredit = credit.containsMatchIn(body)
        val sign = when {
            labeled != null -> null
            directional != null -> directional.groupValues[1].takeIf(String::isNotEmpty)
            else -> signed?.groupValues?.get(1)
        }
        val type = when {
            hasDebit && hasCredit -> return null
            hasDebit && sign == "+" || hasCredit && sign == "-" -> return null
            hasDebit -> TransactionType.WITHDRAW
            hasCredit && outgoingTransfer.containsMatchIn(body) -> TransactionType.WITHDRAW
            hasCredit -> TransactionType.DEPOSIT
            sign == "-" -> TransactionType.WITHDRAW
            sign == "+" -> TransactionType.DEPOSIT
            else -> return null
        }
        return Transaction(type, digits)
    }

    internal fun normalize(value: String): String = value
        .map { char ->
            when (char) {
                in '۰'..'۹' -> '0' + (char - '۰')
                in '٠'..'٩' -> '0' + (char - '٠')
                'ي' -> 'ی'
                'ك' -> 'ک'
                '\u200c', '\u200f', '\u202a', '\u202c' -> ' '
                else -> char
            }
        }
        .joinToString("")
        .lowercase(Locale.ROOT)
        .replace(Regex("\\s+"), " ")
        .trim()
}
