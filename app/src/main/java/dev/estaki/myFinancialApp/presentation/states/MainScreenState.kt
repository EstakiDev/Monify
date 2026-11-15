package dev.estaki.myFinancialApp.presentation.states

import dev.estaki.domain.models.BankCardModel
import dev.estaki.domain.models.SmsModel

data class MainScreenState(
    val smsList:List<SmsModel> = emptyList(),
    val incomeTotal: String = "0",
    val expenseTotal: String = "0",
    val hasUnparsedAmounts: Boolean = false,
    val listBankAccountNumber:List<BankCardModel> = emptyList(),
    val currentBankAccountNumber: String = "",
    val isLoadingCards: Boolean = true,
    val hasLoadedCards: Boolean = false,
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val errorMessage: String? = null
)
