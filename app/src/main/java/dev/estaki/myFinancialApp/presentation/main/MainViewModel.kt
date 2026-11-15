package dev.estaki.myFinancialApp.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.estaki.domain.error.MyCustomSnackBarType
import dev.estaki.domain.models.CategoryModel
import dev.estaki.domain.models.TransactionType
import dev.estaki.kt_pure_utils.toAmountOrNull
import dev.estaki.domain.usecases.CacheCategoryToDb
import dev.estaki.domain.usecases.GetAllBankCardFromTbBankCard
import dev.estaki.domain.usecases.GetAllCategoryCount
import dev.estaki.domain.usecases.GetAllSmsByBankAccountNumber
import dev.estaki.domain.usecases.GetFirstOpenApp
import dev.estaki.domain.usecases.SaveFirstAppOpen
import dev.estaki.myFinancialApp.presentation.actions.MainScreenActions
import dev.estaki.myFinancialApp.presentation.states.MainScreenState
import dev.estaki.ui_utils.SnackBarController
import dev.estaki.ui_utils.SnackBarEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getAllSmsByBankAccountNumberUseCase: GetAllSmsByBankAccountNumber,
    private val getAllCategoryCount: GetAllCategoryCount,
    private val cacheCategoryToDb: CacheCategoryToDb,
    private val getFirstOpenApp: GetFirstOpenApp,
    private val saveFirstAppOpen: SaveFirstAppOpen,
    private val getAllBankCardFromTbBankCard: GetAllBankCardFromTbBankCard
) : ViewModel() {
    private val _mainScreenState = MutableStateFlow<MainScreenState>(MainScreenState())
    val mainScreenState = _mainScreenState.asStateFlow()
    private var smsLoadJob: Job? = null
    private var cardsLoadJob: Job? = null

    init {
        getAllCategory()
        if (getFirstOpenApp() <= 0) {
            saveFirstOpenApp(Date())
        }
    }

    fun onAction(action: MainScreenActions) {
        when (action) {
            is MainScreenActions.LoadCardsFromDb -> {
                cardsLoadJob?.cancel()
                _mainScreenState.update { it.copy(isLoadingCards = true, isError = false) }
                cardsLoadJob = viewModelScope.launch {
                    getAllBankCardFromTbBankCard.invoke().catch {
                        it.printStackTrace()
                        SnackBarController.sendEvent(
                            SnackBarEvent(
                                "متاسفانه عملیات مورد نظر با خطا مواجه شد!",
                                type = MyCustomSnackBarType.ERROR
                            )
                        )
                        _mainScreenState.update { state ->
                            state.copy(isLoadingCards = false, isLoading = false, isError = true, errorMessage = it.message)
                        }
                    }.collect {
                        if (it.isEmpty()) smsLoadJob?.cancel()
                        _mainScreenState.update { state ->
                            state.copy(
                                listBankAccountNumber = it,
                                isLoadingCards = false,
                                hasLoadedCards = true,
                                isLoading = if (it.isEmpty()) false else state.isLoading,
                                smsList = if (it.isEmpty()) emptyList() else state.smsList,
                                isError = false
                            )}
                    }
                }
            }

            is MainScreenActions.OpenSms -> Unit
            is MainScreenActions.ReloadSmsByScrollCards -> {
                getAllSmsByBankAccountNumber(action.bankAccountNumber)
            }
        }
    }

    private fun getAllSmsByBankAccountNumber(bankAccountNumber: String) {
        smsLoadJob?.cancel()
        if (bankAccountNumber.isBlank()) {
            _mainScreenState.update { it.copy(smsList = emptyList(), currentBankAccountNumber = "", isLoading = false, incomeTotal = "0", expenseTotal = "0", hasUnparsedAmounts = false) }
            return
        }
        _mainScreenState.update { state ->
            state.copy(isLoading = state.currentBankAccountNumber != bankAccountNumber || state.smsList.isEmpty(), isError = false)
        }
        smsLoadJob = viewModelScope.launch {
            getAllSmsByBankAccountNumberUseCase.invoke(bankAccountNumber).catch{
                it.printStackTrace()
                SnackBarController.sendEvent(
                    SnackBarEvent(
                        "متاسفانه عملیات مورد نظر با خطا مواجه شد!",
                        type = MyCustomSnackBarType.ERROR
                    )
                )
                _mainScreenState.update { state ->
                    state.copy(
                        smsList = emptyList(),
                        isLoading = false,
                        isError = true,
                        errorMessage = it.message
                    )
                }
                it.printStackTrace()
            }.collect { smsList ->
                val totals = withContext(Dispatchers.Default) {
                    var income = BigInteger.ZERO
                    var expense = BigInteger.ZERO
                    var hasUnparsedAmounts = false
                    smsList.forEach { sms ->
                        val amount = sms.transactionAmount.toAmountOrNull()?.abs()
                        if (amount == null) hasUnparsedAmounts = true
                        else if (sms.transactionType == TransactionType.DEPOSIT) income += amount
                        else expense += amount
                    }
                    Triple(income.toString(), expense.toString(), hasUnparsedAmounts)
                }
                _mainScreenState.update { state ->
                    state.copy(
                        isLoading = false,
                        smsList = smsList,
                        incomeTotal = totals.first,
                        expenseTotal = totals.second,
                        hasUnparsedAmounts = totals.third,
                        currentBankAccountNumber = bankAccountNumber
                    )
                }
            }
        }
    }

    fun getBankAccountNumber() {
        viewModelScope.launch {
            getAllCategoryCount.invoke().catch {
                it.printStackTrace()
                SnackBarController.sendEvent(
                    SnackBarEvent(
                        "متاسفانه عملیات مورد نظر با خطا مواجه شد!",
                        type = MyCustomSnackBarType.ERROR
                    )
                )
            }.collect { count ->
                if (count < 1)
                    addCategoryToDb()
            }
        }
    }

    fun getAllCategory() {
        viewModelScope.launch {
            getAllCategoryCount.invoke().catch {
                it.printStackTrace()
                SnackBarController.sendEvent(
                    SnackBarEvent(
                        "متاسفانه عملیات مورد نظر با خطا مواجه شد!",
                        type = MyCustomSnackBarType.ERROR
                    )
                )
            }.collect { count ->
                if (count < 1)
                    addCategoryToDb()
            }
        }
    }

    private suspend fun addCategoryToDb() {

        val categoryList = mutableListOf<CategoryModel>()
        categoryList.add(CategoryModel(1, "خرید خوراکی"))
        categoryList.add(CategoryModel(2, "خرید شارژ"))
        categoryList.add(CategoryModel(3, "بسته اینترنت"))
        categoryList.add(CategoryModel(4, "خودرو"))
        categoryList.add(CategoryModel(5, "لوازم جانبی موبایل"))
        categoryList.add(CategoryModel(6, "پوشاک"))
        categoryList.add(CategoryModel(7, "اجاره خانه"))
        categoryList.add(CategoryModel(8, "پرداخت قبض"))
        categoryList.add(CategoryModel(9, " هزینه سفر"))
        categoryList.add(CategoryModel(10, "بلیط سفر"))
        categoryList.add(CategoryModel(11, "تفریح با دوستان"))
        categoryList.add(CategoryModel(12, "شام بیرون از منزل"))
        categoryList.add(CategoryModel(13, "ناهار بیرون از منزل"))
        categoryList.add(CategoryModel(14, "ورزش و باشگاه"))
        categoryList.add(CategoryModel(15, "آرایشگاه و خدمات زیبایی"))
        categoryList.add(CategoryModel(16, "خرید لوازم آرایشی و بهداشتی"))
        categoryList.add(CategoryModel(17, "لوازم جانبی کامپیوتر و لپ تاپ"))
        categoryList.add(CategoryModel(18, "تاکسی اینترنتی"))
        categoryList.add(CategoryModel(19, "سفارش غذا"))
        categoryList.add(CategoryModel(20, "خرید سوپرمارکتی"))
        categoryList.add(CategoryModel(21, "دیت"))
        categoryList.add(CategoryModel(22, "سینما"))

        cacheCategoryToDb.invoke(categoryList).catch {
            it.printStackTrace()
            SnackBarController.sendEvent(
                SnackBarEvent(
                    "متاسفانه عملیات مورد نظر با خطا مواجه شد!",
                    type = MyCustomSnackBarType.ERROR
                )
            )
        }.collect {
            Timber.tag("TAG").d("addCategoryToDb: Success $it")
        }
    }

    private fun getFirstOpenApp(): Long {
        return getFirstOpenApp.invoke()
    }

    private fun saveFirstOpenApp(data: Date) {
        saveFirstAppOpen.invoke(data)
    }

}
