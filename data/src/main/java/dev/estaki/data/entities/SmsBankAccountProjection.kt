package dev.estaki.data.entities

/** Latest account details projected from transaction messages. */
data class SmsBankAccountProjection(
    val bankAccountNumber: String,
    val bankName: String,
    val bankCardBalance: String
)
