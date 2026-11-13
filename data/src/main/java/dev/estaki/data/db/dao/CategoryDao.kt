package dev.estaki.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import dev.estaki.data.entities.CategoryEntity

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertCategory(categoryEntity: List<CategoryEntity>):List<Long>

    @Query("SELECT * FROM tb_category")
    fun selectAll():List<CategoryEntity>

    @Query("SELECT COUNT(*) FROM tb_category")
    fun getCategoryCount():Long

    @Query("SELECT * FROM tb_category WHERE id = :id")
    fun read(id: Long): CategoryEntity

    @Query("DELETE FROM tb_category WHERE id = :id")
    fun deleteById(id: Long): Int

    @Delete
    fun delete(category: CategoryEntity): Int

    @Query("DELETE FROM tb_category")
    fun deleteAll(): Int

    @Upsert
    fun upsert(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(category: CategoryEntity)
}
