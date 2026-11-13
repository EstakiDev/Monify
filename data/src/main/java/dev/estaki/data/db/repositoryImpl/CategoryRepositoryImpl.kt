package dev.estaki.data.db.repositoryImpl

import dev.estaki.domain.models.CategoryModel
import dev.estaki.domain.repo.datasource.CategoryDataSource
import dev.estaki.domain.repo.reposities.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CategoryRepositoryImpl(
    private val localDS: CategoryDataSource.Local
) : CategoryRepository {
    override suspend fun readAll(): Flow<List<CategoryModel>> =
        localDS.readAll()

    override suspend fun read(id: Long): Flow<CategoryModel> = localDS.read(id)

    override suspend fun deleteById(id: Long): Flow<Int> = localDS.deleteById(id)

    override suspend fun delete(entity: CategoryModel): Flow<Int> = localDS.delete(entity)

    override suspend fun deleteAll(): Int = localDS.deleteAll().first()

    override suspend fun upsert(entity: CategoryModel) {
        localDS.upsert(entity)
    }

    override suspend fun add(id: CategoryModel) {
        localDS.add(id)
    }

    override suspend fun addAll(item: List<CategoryModel>): Flow<List<Long>> =
        localDS.addAll(item)


    override suspend fun getAllCount(): Flow<Long> =
        localDS.getAllCount()

}
