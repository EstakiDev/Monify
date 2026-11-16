package dev.estaki.myFinancialApp.presentation.smsimport

import android.content.Context
import android.provider.Telephony
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.estaki.domain.models.SmsModel
import dev.estaki.domain.models.SmsRawModel
import dev.estaki.domain.models.TransactionType
import dev.estaki.domain.processor.SmsProcessor
import dev.estaki.domain.usecases.CacheAllBankAccountToDb
import dev.estaki.domain.usecases.CacheSmsToDb
import dev.estaki.domain.usecases.GetAllBankAccountNumberFromTbSms
import dev.estaki.domain.usecases.GetAllSms
import dev.estaki.myFinancialApp.convertToTime
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal data class SenderSummary(val address: String, val count: Int)
internal data class ImportMessage(val raw: SmsRawModel, val transaction: SmsModel?, val imported: Boolean)
internal data class SmsImportState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val senders: List<SenderSummary> = emptyList(),
    val sender: String? = null,
    val messages: List<ImportMessage> = emptyList(),
    val error: Boolean = false,
    val lastImportedCount: Int? = null
)

@HiltViewModel
internal class SmsImportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cacheSms: CacheSmsToDb,
    private val getAllSms: GetAllSms,
    private val getAccounts: GetAllBankAccountNumberFromTbSms,
    private val cacheAccounts: CacheAllBankAccountToDb
) : ViewModel() {
    private val mutableState = MutableStateFlow(SmsImportState())
    val state = mutableState.asStateFlow()
    private var messagesBySender: Map<String, List<SmsRawModel>> = emptyMap()
    private var knownMessages = emptySet<Triple<String, String, Long>>()
    private var loadJob: Job? = null

    fun loadSenders() {
        loadJob?.cancel()
        mutableState.update { it.copy(isLoading = true, error = false) }
        loadJob = viewModelScope.launch {
            try {
                val messages = withContext(Dispatchers.IO) {
                    val result = mutableListOf<SmsRawModel>()
                    val cursor = context.contentResolver.query(
                        Telephony.Sms.Inbox.CONTENT_URI,
                        arrayOf(Telephony.Sms._ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE),
                        null, null, Telephony.Sms.DEFAULT_SORT_ORDER
                    ) ?: error("SMS provider unavailable")
                    cursor.use {
                        val id = it.getColumnIndexOrThrow(Telephony.Sms._ID)
                        val address = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                        val body = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
                        val date = it.getColumnIndexOrThrow(Telephony.Sms.DATE)
                        while (it.moveToNext()) {
                            val sender = it.getString(address).orEmpty()
                            if (sender.isBlank()) continue
                            val time = it.getString(date).orEmpty()
                            result.add(SmsRawModel(it.getString(id).orEmpty(), sender, it.getString(body).orEmpty(), time, time))
                        }
                    }
                    result
                }
                knownMessages = getAllSms().first().mapTo(mutableSetOf()) { it.fingerprint() }
                messagesBySender = messages.groupBy { it.senderName }
                val senders = messagesBySender.map { SenderSummary(it.key, it.value.size) }
                mutableState.update { it.copy(isLoading = false, senders = senders, sender = null, messages = emptyList()) }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableState.update { it.copy(isLoading = false, error = true) }
            }
        }
    }

    fun selectSender(sender: String) {
        loadJob?.cancel()
        mutableState.update { it.copy(sender = sender, isLoading = true, error = false, lastImportedCount = null) }
        loadJob = viewModelScope.launch {
            val messages = withContext(Dispatchers.Default) {
                messagesBySender[sender].orEmpty().map { raw ->
                    val transaction = SmsProcessor(listOf(raw)).execute().firstOrNull()?.copy(
                        transactionDate = raw.receiveDateTime.convertToTime()
                    )
                    ImportMessage(raw, transaction, raw.fingerprint() in knownMessages)
                }
            }
            mutableState.update { it.copy(messages = messages, isLoading = false) }
        }
    }

    fun backToSenders() {
        loadJob?.cancel()
        mutableState.update { it.copy(sender = null, messages = emptyList(), isLoading = false, lastImportedCount = null, error = false) }
    }

    fun importRecognized() {
        save(state.value.messages.filter { !it.imported }.mapNotNull { message ->
            message.transaction?.takeIf { it.bankAccountNumber.count(Char::isDigit) >= 4 }
        })
    }

    fun importManual(raw: SmsRawModel, bankName: String, account: String, type: TransactionType, amount: String) {
        val transaction = SmsProcessor.manualTransaction(raw, bankName, account, type, amount).copy(
            transactionDate = raw.receiveDateTime.convertToTime()
        )
        save(listOf(transaction))
    }

    private fun save(transactions: List<SmsModel>) {
        if (state.value.isSaving || transactions.isEmpty()) return
        mutableState.update { it.copy(isSaving = true, error = false, lastImportedCount = null) }
        viewModelScope.launch {
            try {
                val ids = cacheSms(transactions).first()
                knownMessages = getAllSms().first().mapTo(mutableSetOf()) { it.fingerprint() }
                val count = ids.count { it != -1L }
                mutableState.update { state ->
                    state.copy(messages = state.messages.map { it.copy(imported = it.raw.fingerprint() in knownMessages) }, lastImportedCount = count)
                }
                val accounts = getAccounts().first()
                cacheAccounts(accounts).first()
                mutableState.update { it.copy(isSaving = false) }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableState.update { it.copy(isSaving = false, error = true) }
            }
        }
    }

    private fun SmsModel.fingerprint() = Triple(smsSender, smsBody, transactionDateTime)
    private fun SmsRawModel.fingerprint() = Triple(senderName, description, receiveDateTime.toLongOrNull() ?: 0L)
}
