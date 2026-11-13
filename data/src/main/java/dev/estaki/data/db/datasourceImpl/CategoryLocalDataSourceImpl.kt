package dev.estaki.data.db.datasourceImpl

import dev.estaki.data.db.dao.CategoryDao
import dev.estaki.data.mapper.toDbEntity
import dev.estaki.data.mapper.toDomainModel
import dev.estaki.domain.models.CategoryModel
import dev.estaki.domain.repo.datasource.CategoryDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class CategoryLocalDataSourceImpl(private val categoryDao: CategoryDao):CategoryDataSource.Local {
    override suspend fun readAll(): Flow<List<CategoryModel>> = flow { emit(categoryDao.selectAll().map { it.toDomainModel() }) }.flowOn(Dispatchers.IO)

    override suspend fun read(id: Long): Flow<CategoryModel> =
        flow { emit(categoryDao.read(id).toDomainModel()) }.flowOn(Dispatchers.IO)

    override suspend fun deleteById(id: Long): Flow<Int> =
        flow { emit(categoryDao.deleteById(id)) }.flowOn(Dispatchers.IO)

    override suspend fun delete(entity: CategoryModel): Flow<Int> =
        flow { emit(categoryDao.delete(entity.toDbEntity())) }.flowOn(Dispatchers.IO)

    override suspend fun deleteAll(): Flow<Int> = flow { emit(categoryDao.deleteAll()) }.flowOn(Dispatchers.IO)

    override suspend fun upsert(entity: CategoryModel) = kotlinx.coroutines.withContext(Dispatchers.IO) {
        categoryDao.upsert(entity.toDbEntity())
    }

    override suspend fun add(id: CategoryModel) = kotlinx.coroutines.withContext(Dispatchers.IO) {
        categoryDao.insert(id.toDbEntity())
    }

    override suspend fun addAll(item: List<CategoryModel>) =
        flow { emit(categoryDao.insertCategory(item.map { it.toDbEntity() })) }.flowOn(Dispatchers.IO)

    override suspend fun getAllCount(): Flow<Long> =
        flow { emit(categoryDao.getCategoryCount()) }.flowOn(Dispatchers.IO).flowOn(Dispatchers.IO)


}
