package dev.estaki.domain.validator

import dev.estaki.domain.models.SmsRawModel

/** Heuristic classifier for transaction alerts. It deliberately does not depend on sender IDs,
 * which vary between banks, operators, and messaging aggregators. */
object SmsValidator {
    private val transactionKeywords = listOf(
        "تراکنش", "خرید", "برداشت", "واریز", "انتقال", "کارت به کارت", "کارتبهکارت",
        "مانده", "موجودی", "پرداخت", "پایا", "ساتنا", "شبا"
    )
    private val nonTransactionKeywords = listOf(
        "رمز پویا", "رمز یکبار", "رمز یک بار", "رمز دوم", "کد تایید", "کد تأیید",
        "کد فعالسازی", "کد فعال سازی", "رمز ورود", "خوش آمدید", "خوشآمدید"
    )
    private val amountPattern = Regex("(?<!\\d)\\d{1,3}(?:[,،٬]\\d{3})+(?!\\d)|(?<!\\d)\\d{4,}(?!\\d)")

    fun isBankSms(smsRawModel: SmsRawModel): Boolean {
        val normalized = normalize(smsRawModel.description)
        if (normalized.isBlank() || nonTransactionKeywords.any(normalized::contains)) return false

        val hasTransactionKeyword = transactionKeywords.any(normalized::contains)
        val hasAmount = amountPattern.containsMatchIn(normalized)
        val hasBalance = normalized.contains("مانده") || normalized.contains("موجودی")
        val hasDirection = normalized.contains("واریز") || normalized.contains("برداشت")

        // Requiring a transaction cue and a numeric amount/balance excludes OTP and marketing
        // messages while allowing sender IDs from all domestic banks and payment providers.
        return hasTransactionKeyword && hasAmount && (hasDirection || hasBalance || normalized.contains("تراکنش"))
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
        .lowercase()
        .replace(Regex("\\s+"), " ")
        .trim()
}
