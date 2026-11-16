package com.example.base.data.mapper

import com.example.base.data.api.practice.CategoryDto
import com.example.base.data.api.practice.PaginationDto
import com.example.base.data.api.practice.PracticeApiListResponse
import com.example.base.data.api.practice.ProductDto
import com.example.base.domain.model.Category
import com.example.base.domain.model.PaginationMetadata
import com.example.base.domain.model.PagedResult
import com.example.base.domain.model.Product

/**
 * Mapper between Practice API DTOs and pure domain catalog models.
 */
object ProductMapper {

    fun mapProduct(dto: ProductDto): Product {
        val priceAmount = dto.price?.amount ?: 0L
        val originalPriceAmount = dto.originalPrice?.amount
        val catId = dto.category?.id ?: dto.categoryId.orEmpty()
        val catName = dto.category?.name ?: dto.categoryName
        val isInStock = dto.inStock
            ?: dto.inventory?.inStock
            ?: ((dto.inventory?.quantity ?: 0) > 0)
        val invCount = dto.inventory?.quantity
        val ratingValue = dto.rating?.average ?: 0.0
        val ratingCnt = dto.rating?.count
        val tagList = dto.tags.orEmpty()
        val attrMap = dto.attributes.orEmpty()

        return Product(
            id = dto.id,
            name = dto.name,
            description = dto.description,
            price = priceAmount,
            originalPrice = originalPriceAmount,
            categoryId = catId,
            categoryName = catName,
            inStock = isInStock,
            inventoryCount = invCount,
            rating = ratingValue,
            ratingCount = ratingCnt,
            tags = tagList,
            attributes = attrMap,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt
        )
    }

    fun mapCategory(dto: CategoryDto): Category {
        return Category(
            id = dto.id,
            name = dto.name,
            slug = dto.slug,
            description = dto.description
        )
    }

    fun mapPagination(dto: PaginationDto?): PaginationMetadata {
        return if (dto != null) {
            PaginationMetadata(
                page = dto.page,
                pageSize = dto.pageSize,
                totalItems = dto.totalItems,
                totalPages = dto.totalPages,
                hasNextPage = dto.hasNextPage,
                hasPreviousPage = dto.hasPreviousPage
            )
        } else {
            PaginationMetadata(
                page = 1,
                pageSize = 10,
                totalItems = 0,
                totalPages = 0,
                hasNextPage = false,
                hasPreviousPage = false
            )
        }
    }

    fun mapPagedProducts(response: PracticeApiListResponse<ProductDto>): PagedResult<Product> {
        val products = response.data.items.map(::mapProduct)
        val pagination = mapPagination(response.meta?.pagination)
        return PagedResult(
            items = products,
            pagination = pagination
        )
    }
}
