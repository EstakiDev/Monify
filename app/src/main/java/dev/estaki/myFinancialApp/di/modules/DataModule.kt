package dev.estaki.myFinancialApp.di.modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.estaki.data.db.dao.BankAccountDao
import dev.estaki.data.db.dao.CategoryDao
import dev.estaki.data.db.dao.SmsDao
import dev.estaki.data.db.datasourceImpl.BankAccountLocalDataSourceImpl
import dev.estaki.data.db.datasourceImpl.CategoryLocalDataSourceImpl
import dev.estaki.data.db.datasourceImpl.SmsLocalDatasourceImpl
import dev.estaki.data.db.repositoryImpl.BankAccountRepositoryImpl
import dev.estaki.data.db.repositoryImpl.CategoryRepositoryImpl
import dev.estaki.data.db.repositoryImpl.SmsRepositoryImpl
import dev.estaki.domain.repo.datasource.BankAccountDataSource
import dev.estaki.domain.repo.datasource.CategoryDataSource
import dev.estaki.domain.repo.datasource.SmsDataSource
import dev.estaki.domain.repo.reposities.BankAccountRepository
import dev.estaki.domain.repo.reposities.CategoryRepository
import dev.estaki.domain.repo.reposities.SmsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {

    @Provides
    @Singleton
    fun provideSmsRepository(
        smsLocalDs: SmsDataSource.Local
    ): SmsRepository {
        return SmsRepositoryImpl(smsLocalDs)
    }


    @Provides
    @Singleton
    fun provideSmsLocalDataSource(smsDao: SmsDao): SmsDataSource.Local {
        return SmsLocalDatasourceImpl(smsDao)
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(
        categoryLocalDs: CategoryDataSource.Local
    ): CategoryRepository {
        return CategoryRepositoryImpl(categoryLocalDs)
    }

    @Provides
    @Singleton
    fun provideCategoryLocalDataSource(categoryDao: CategoryDao): CategoryDataSource.Local {
        return CategoryLocalDataSourceImpl(categoryDao)
    }

    @Provides
    @Singleton
    fun provideBankAccountRepository(
        bankAccountLocalDs: BankAccountDataSource.Local
    ): BankAccountRepository {
        return BankAccountRepositoryImpl(bankAccountLocalDs)
    }


    @Provides
    @Singleton
    fun provideBankAccountLocalDataSource(bankAccountDao: BankAccountDao): BankAccountDataSource.Local {
        return BankAccountLocalDataSourceImpl(bankAccountDao)
    }


}
