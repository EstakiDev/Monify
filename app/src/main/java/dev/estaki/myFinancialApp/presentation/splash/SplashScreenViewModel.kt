package dev.estaki.myFinancialApp.presentation.splash

import android.content.ContentResolver
import android.provider.Telephony
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.estaki.domain.error.MyCustomSnackBarType
import dev.estaki.domain.models.BankCardModel
import dev.estaki.domain.models.SmsModel
import dev.estaki.domain.models.SmsRawModel
import dev.estaki.domain.processor.SmsProcessor
import dev.estaki.domain.usecases.CacheAllBankAccountToDb
import dev.estaki.domain.usecases.CacheSmsToDb
import dev.estaki.domain.usecases.CountAllBankAccountInDb
import dev.estaki.domain.usecases.GetAllBankAccountNumberFromTbSms
import dev.estaki.domain.usecases.GetAllSms
import dev.estaki.myFinancialApp.convertToTime
import dev.estaki.myFinancialApp.presentation.actions.SplashScreenActions
import dev.estaki.myFinancialApp.presentation.states.SplashScreenState
import dev.estaki.ui_utils.SnackBarController
import dev.estaki.ui_utils.SnackBarEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    private val cashSmsToDb: CacheSmsToDb,
    private val getAllBankAccountNumberFromTbSms: GetAllBankAccountNumberFromTbSms,
    private val cacheAllBankAccountToDb: CacheAllBankAccountToDb,
    private val countAllBankAccountInDb: CountAllBankAccountInDb,
    private val getAllSms: GetAllSms
) : ViewModel() {
    private val _splashScreenState = MutableStateFlow(SplashScreenState())
    val splashScreenState = _splashScreenState.asStateFlow()

    fun onAction(action: SplashScreenActions) {
        when (action) {

            is SplashScreenActions.ExtractSmsFromContentResolver -> {
                viewModelScope.launch {
                    if (_splashScreenState.value.mustGetPermissions)
                        _splashScreenState.update { it.copy(mustGetPermissions = false) }
                    readSms(action.contentResolver)
                }
            }

            SplashScreenActions.MustGoToMainScreen -> {
                _splashScreenState.update {
                    it.copy(
                        isLoading = false,
                        isFinishedAndGoToMainScreen = true
                    )
                }
            }
        }
    }


    private suspend fun readSms(contentResolver: ContentResolver) {
        try {
            val smsList = withContext(Dispatchers.IO) {
                val messages = ArrayList<SmsRawModel>()
                contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                    arrayOf(Telephony.Sms._ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE),
                    null,
                    null,
                    Telephony.Sms.DEFAULT_SORT_ORDER
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
                    val addressColumn = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                    val bodyColumn = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
                    val dateColumn = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
                    while (cursor.moveToNext()) {
                        val date = cursor.getString(dateColumn)
                        messages += SmsRawModel(
                            _id = cursor.getString(idColumn),
                            senderName = cursor.getString(addressColumn).orEmpty(),
                            description = cursor.getString(bodyColumn).orEmpty(),
                            receiveDate = date,
                            receiveDateTime = date
                        )
                    }
                }
                messages
            }

            withContext(Dispatchers.Default) {
                val transactions = SmsProcessor(smsList).execute().map { sms ->
                    sms.copy(transactionDate = sms.transactionDateTime.toString().convertToTime())
                }
                cacheSmsToDb(transactions)
                cacheBankAccountToDb()
            }
            onAction(SplashScreenActions.MustGoToMainScreen)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Timber.e(exception, "Unable to synchronize bank SMS messages")
            onAction(SplashScreenActions.MustGoToMainScreen)
        }
    }

    private suspend fun cacheBankAccountToDb() {
        getAllBankAccountNumberFromTbSms.invoke().catch {
            it.printStackTrace()
            SnackBarController.sendEvent(
                SnackBarEvent(
                    "متاسفانه عملیات مورد نظر با خطا مواجه شد!",
                    type = MyCustomSnackBarType.ERROR
                )
            )
        }.collect { listOfNewBankAccount ->

            countAllBankAccountInDb.invoke().catch {
                it.printStackTrace()
                SnackBarController.sendEvent(
                    SnackBarEvent(
                        "متاسفانه عملیات مورد نظر با خطا مواجه شد!",
                        type = MyCustomSnackBarType.ERROR
                    )
                )
            }.collect { list ->
                val mutableListOfOldBankAccount = list.toMutableList()
                val mutableListOfNewBankAccount = listOfNewBankAccount.toMutableList()

                val newList = mutableListOfNewBankAccount.map { new ->
                    val existing = mutableListOfOldBankAccount.find { item ->
                        item.bankAccountNumber == new.bankAccountNumber
                    }
                    if (existing != null && (existing.isItFromSms != true ||
                            (!existing.bankCardNumber.isNullOrBlank() && existing.bankCardNumber != "null"))) {
                        existing
                    } else {
                        new.copy(bankCardNumber = existing?.bankCardNumber, isItFromSms = true)
                    }
                }

                Timber.tag("TAG").d("getAllBankAccountNumberFromTbSms: done ")
                cacheAllBankAccountToDb.invoke(
                    newList
                ).catch {
                    it.printStackTrace()
                    SnackBarController.sendEvent(
                        SnackBarEvent(
                            "متاسفانه عملیات مورد نظر با خطا مواجه شد!",
                            type = MyCustomSnackBarType.ERROR
                        )
                    )
                }.collect {
                    Timber.tag("TAG").d("cacheBankAccountToDb: done")
                }

            }


        }
    }


    private suspend fun getSavedSmsInDb(): List<SmsModel> {
        var result = listOf<SmsModel>()
        getAllSms.invoke().catch {
            it.printStackTrace()
            SnackBarController.sendEvent(
                SnackBarEvent(
                    "متاسفانه عملیات مورد نظر با خطا مواجه شد!",
                    type = MyCustomSnackBarType.ERROR
                )
            )
        }.collect { smsList ->
            result = smsList
            Timber.tag("TAG").d("getSavedSmsInDb: done")
        }
        return result
    }

    private suspend fun cacheSmsToDb(smsList: List<SmsModel>) {
        Timber.tag("TAG").d("parseSmsToModel --;;;")
        val smsListInDb = getSavedSmsInDb()
        val knownMessages = smsListInDb
            .mapTo(HashSet<Triple<String, String, Long>>()) { it.fingerprint() }
        val newMessages = smsList.filter { knownMessages.add(it.fingerprint()) }

        cashSmsToDb.invoke(newMessages).catch {
            it.printStackTrace()
            SnackBarController.sendEvent(
                SnackBarEvent(
                    "متاسفانه عملیات مورد نظر با خطا مواجه شد!",
                    type = MyCustomSnackBarType.ERROR
                )
            )
        }.collect {
            Timber.tag("TAG").d("parseSmsToModel: cashSmsToDb done $it")
        }
    }

    private fun SmsModel.fingerprint(): Triple<String, String, Long> =
        Triple(smsSender, smsBody, transactionDateTime)

}
