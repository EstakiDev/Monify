package dev.estaki.myFinancialApp.presentation.detailScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.estaki.domain.error.MyCustomSnackBarType
import dev.estaki.domain.usecases.GetAllBankCardFromTbBankCard
import dev.estaki.domain.usecases.GetAllCategoryList
import dev.estaki.domain.usecases.GetSingleSms
import dev.estaki.domain.usecases.SetIsRemovedSms
import dev.estaki.domain.usecases.SetSmsWasSaw
import dev.estaki.domain.usecases.UpsertSms
import dev.estaki.myFinancialApp.presentation.actions.TransactionDetailScreenActions
import dev.estaki.myFinancialApp.presentation.states.TransactionDetailScreenState
import dev.estaki.ui_utils.SnackBarController
import dev.estaki.ui_utils.SnackBarEvent
import dev.estaki.ui_utils.localized
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TransactionDetailScreenViewModel @Inject constructor(
    private val getAllCategoryList: GetAllCategoryList,
    private val getSingleSmsUseCase: GetSingleSms,
    private val saveSmsUseCase: UpsertSms,
    private val setIsRemovedSms: SetIsRemovedSms,
    private val setSmsWasSawUseCase: SetSmsWasSaw,
    private val getAllBankCardFromTbBankCard: GetAllBankCardFromTbBankCard
) : ViewModel() {
    private val mutableState = MutableStateFlow(TransactionDetailScreenState())
    val state = mutableState.asStateFlow()

    fun onAction(action: TransactionDetailScreenActions) {
        when (action) {
            is TransactionDetailScreenActions.LoadTransaction -> loadTransaction(action.smsId)
            is TransactionDetailScreenActions.SaveTransaction -> performChange {
                saveSmsUseCase(action.smsModel)
            }
            is TransactionDetailScreenActions.DeleteTransaction -> performChange {
                check(setIsRemovedSms(action.smsModel).first() > 0)
            }
        }
    }

    private fun loadTransaction(id: Long) {
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, isError = false) }
            try {
                val accounts = getAllBankCardFromTbBankCard().first()
                val categories = getAllCategoryList().first()
                val sms = if (id == 0L) null else getSingleSmsUseCase(id).first()
                mutableState.update {
                    it.copy(bankCardList = accounts, categoryList = categories, smsModel = sms, isLoading = false)
                }
                if (id != 0L) {
                    try {
                        setSmsWasSawUseCase(id)
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: Exception) {
                        Timber.w(error, "Unable to mark transaction as seen")
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Timber.e(error, "Unable to load transaction")
                mutableState.update { it.copy(isLoading = false, isError = true) }
            }
        }
    }

    private fun performChange(change: suspend () -> Unit) {
        if (state.value.isLoading) return
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true) }
            try {
                change()
                mutableState.update { it.copy(isLoading = false, operationCompleted = true) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Timber.e(error, "Unable to save transaction change")
                mutableState.update { it.copy(isLoading = false) }
                SnackBarController.sendEvent(
                    SnackBarEvent(localized("متاسفانه عملیات مورد نظر با خطا مواجه شد!"), type = MyCustomSnackBarType.ERROR)
                )
            }
        }
    }
}
