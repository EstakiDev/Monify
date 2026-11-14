package dev.estaki.domain.processor

import dev.estaki.domain.models.SmsRawModel
import dev.estaki.domain.models.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsProcessorTest {
    private fun parse(body: String) = SmsProcessor(
        listOf(SmsRawModel("1", "BANK", body, "1403/01/01", "1710000000000"))
    ).execute()

    @Test
    fun parsesActualDepositsAndWithdrawals() {
        val deposit = parse("بانک نمونه\nواریز: ۱,۲۵۰,۰۰۰ ریال\nحساب: ۱۲۳۴\nمانده: ۵,۰۰۰,۰۰۰").single()
        assertEquals(TransactionType.DEPOSIT, deposit.transactionType)
        assertEquals("1250000", deposit.transactionAmount)

        val withdrawal = parse("برداشت از حساب 1234 مبلغ: 250,000 ریال مانده: 1,000,000").single()
        assertEquals(TransactionType.WITHDRAW, withdrawal.transactionType)
        assertEquals("250000", withdrawal.transactionAmount)

        val signed = parse("حساب: 1234\n-500,000\nمانده: 1,500,000").single()
        assertEquals(TransactionType.WITHDRAW, signed.transactionType)
        assertEquals("500000", signed.transactionAmount)
    }

    @Test
    fun rejectsMessagesWithoutCompletedTransactionEvidence() {
        listOf(
            "بانک نمونه: موجودی حساب 1234 مبلغ 2,000,000 ریال است",
            "بانک نمونه: تراکنش حساب 1234 در تاریخ 1403/01/01",
            "بانک نمونه: برای واریز به حساب 1234 مبلغ 500,000 ریال اقدام کنید",
            "بانک نمونه: برداشت مبلغ 500,000 ریال از حساب 1234 ناموفق بود",
            "بانک نمونه: رمز پویا 123456 برای خرید به مبلغ 500,000 ریال از کارت 1234",
            "فروشگاه: خرید به مبلغ 500,000 ریال ثبت شد",
            "بانک نمونه: تاریخ برداشت وجه از حساب 1234، 1403/01/01 است",
            "Bank account 1234 balance 1,000,000; transaction declined"
        ).forEach { assertTrue("Unexpected transaction: $it", parse(it).isEmpty()) }
    }

    @Test
    fun rejectsAmbiguousAndConflictingDirections() {
        assertTrue(parse("حساب 1234 واریز و برداشت مبلغ 500,000").isEmpty())
        assertTrue(parse("حساب 1234 واریز: -500,000").isEmpty())
    }
}
