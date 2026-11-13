package dev.estaki.domain.repo.datasource

import dev.estaki.domain.base.BaseDataSource
import dev.estaki.domain.models.CategoryModel

interface CategoryDataSource {
    interface Local: BaseDataSource<CategoryModel, Int, Long>

}
