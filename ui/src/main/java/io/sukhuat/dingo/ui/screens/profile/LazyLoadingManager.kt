package io.sukhuat.dingo.ui.screens.profile

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.sukhuat.dingo.domain.model.Achievement
import io.sukhuat.dingo.domain.model.ProfileError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for implementing lazy loading of large datasets with pagination
 */
@Singleton
class LazyLoadingManager @Inject constructor() {

    companion object {
        private const val DEFAULT_PAGE_SIZE = 20
        private const val PREFETCH_THRESHOLD = 5
    }

    /**
     * State for paginated data loading
     */
    data class PaginatedState<T>(
        val items: List<T> = emptyList(),
        val isLoading: Boolean = false,
        val isLoadingMore: Boolean = false,
        val hasMoreItems: Boolean = true,
        val error: ProfileError? = null,
        val currentPage: Int = 0,
        val pageSize: Int = DEFAULT_PAGE_SIZE
    )

    /**
     * Create a paginated achievements loader
     */
    @Composable
    fun rememberPaginatedAchievements(
        loadPage: suspend (page: Int, pageSize: Int) -> List<Achievement>,
        lazyListState: LazyListState = remember { LazyListState() }
    ): PaginatedState<Achievement> {
        var state by remember {
            mutableStateOf(PaginatedState<Achievement>())
        }

        // Detect when we're near the end of the list
        val shouldLoadMore by remember {
            derivedStateOf {
                val layoutInfo = lazyListState.layoutInfo
                val totalItems = layoutInfo.totalItemsCount
                val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

                !state.isLoadingMore && state.hasMoreItems && totalItems > 0 && lastVisibleIndex >= totalItems - PREFETCH_THRESHOLD
            }
        }

        val coroutineScope = rememberCoroutineScope()

        // Load initial data
        LaunchedEffect(Unit) {
            if (state.items.isEmpty() && !state.isLoading) {
                loadInitialData(state, loadPage) { newState ->
                    state = newState
                }
            }
        }

        // Load more data when needed
        LaunchedEffect(shouldLoadMore) {
            if (shouldLoadMore) {
                loadMoreData(state, loadPage) { newState ->
                    state = newState
                }
            }
        }

        return state
    }

    /**
     * Create a memory-efficient list renderer for achievements
     */
    @Composable
    fun rememberMemoryEfficientRenderer(): MemoryEfficientRenderer {
        return remember { MemoryEfficientRenderer() }
    }

    /**
     * Load initial page of data
     */
    private suspend fun loadInitialData(
        currentState: PaginatedState<Achievement>,
        loadPage: suspend (page: Int, pageSize: Int) -> List<Achievement>,
        updateState: (PaginatedState<Achievement>) -> Unit
    ) {
        try {
            updateState(currentState.copy(isLoading = true, error = null))

            val items = loadPage(0, currentState.pageSize)

            updateState(
                currentState.copy(
                    items = items,
                    isLoading = false,
                    hasMoreItems = items.size >= currentState.pageSize,
                    currentPage = 0
                )
            )
        } catch (e: Exception) {
            val error = if (e is ProfileError) e else ProfileError.UnknownError(e)
            updateState(
                currentState.copy(
                    isLoading = false,
                    error = error
                )
            )
        }
    }

    /**
     * Load next page of data
     */
    private suspend fun loadMoreData(
        currentState: PaginatedState<Achievement>,
        loadPage: suspend (page: Int, pageSize: Int) -> List<Achievement>,
        updateState: (PaginatedState<Achievement>) -> Unit
    ) {
        try {
            updateState(currentState.copy(isLoadingMore = true, error = null))

            val nextPage = currentState.currentPage + 1
            val newItems = loadPage(nextPage, currentState.pageSize)

            updateState(
                currentState.copy(
                    items = currentState.items + newItems,
                    isLoadingMore = false,
                    hasMoreItems = newItems.size >= currentState.pageSize,
                    currentPage = nextPage
                )
            )
        } catch (e: Exception) {
            val error = if (e is ProfileError) e else ProfileError.UnknownError(e)
            updateState(
                currentState.copy(
                    isLoadingMore = false,
                    error = error
                )
            )
        }
    }

    /**
     * Retry loading data after an error
     */
    fun retryLoading(
        currentState: PaginatedState<Achievement>,
        loadPage: suspend (page: Int, pageSize: Int) -> List<Achievement>,
        updateState: (PaginatedState<Achievement>) -> Unit,
        coroutineScope: CoroutineScope
    ) {
        coroutineScope.launch {
            if (currentState.items.isEmpty()) {
                loadInitialData(currentState, loadPage, updateState)
            } else {
                loadMoreData(currentState, loadPage, updateState)
            }
        }
    }

    /**
     * Reset pagination state
     */
    fun resetPagination(): PaginatedState<Achievement> {
        return PaginatedState()
    }

    /**
     * Create a virtualized list state for very large datasets
     */
    fun createVirtualizedState(totalItems: Int): VirtualizedListState {
        return VirtualizedListState(
            totalItems = totalItems,
            visibleRange = 0..minOf(totalItems - 1, 50), // Show first 50 items initially
            bufferSize = 10
        )
    }
}

/**
 * Memory-efficient renderer for large lists
 */
class MemoryEfficientRenderer {
    private val itemCache = mutableMapOf<Int, Any>()
    private val maxCacheSize = 100

    /**
     * Get or create item for rendering
     */
    fun <T> getOrCreateItem(index: Int, factory: (Int) -> T): T {
        @Suppress("UNCHECKED_CAST")
        return itemCache.getOrPut(index) {
            // Clean cache if it gets too large
            if (itemCache.size > maxCacheSize) {
                cleanCache()
            }
            factory(index) as Any
        } as T
    }

    /**
     * Clean old items from cache
     */
    private fun cleanCache() {
        val itemsToRemove = itemCache.size - (maxCacheSize * 0.8).toInt()
        if (itemsToRemove > 0) {
            itemCache.keys.take(itemsToRemove).forEach { key ->
                itemCache.remove(key)
            }
        }
    }

    /**
     * Clear all cached items
     */
    fun clearCache() {
        itemCache.clear()
    }
}

/**
 * State for virtualized lists that can handle very large datasets
 */
data class VirtualizedListState(
    val totalItems: Int,
    val visibleRange: IntRange,
    val bufferSize: Int = 10
) {
    /**
     * Get the range of items that should be rendered
     */
    fun getRenderRange(firstVisibleIndex: Int, visibleItemCount: Int): IntRange {
        val start = maxOf(0, firstVisibleIndex - bufferSize)
        val end = minOf(totalItems - 1, firstVisibleIndex + visibleItemCount + bufferSize)
        return start..end
    }

    /**
     * Check if an item should be rendered
     */
    fun shouldRenderItem(index: Int, firstVisibleIndex: Int, visibleItemCount: Int): Boolean {
        val renderRange = getRenderRange(firstVisibleIndex, visibleItemCount)
        return index in renderRange
    }
}

/**
 * Performance monitoring for lazy loading
 */
class LazyLoadingPerformanceMonitor {
    private var loadStartTime: Long = 0
    private var itemsLoaded: Int = 0

    fun startLoading() {
        loadStartTime = System.currentTimeMillis()
    }

    fun endLoading(itemCount: Int) {
        val loadTime = System.currentTimeMillis() - loadStartTime
        itemsLoaded += itemCount

        // Log performance metrics (in a real app, you'd send this to analytics)
        println("Lazy loading performance: $itemCount items loaded in ${loadTime}ms")
        println("Average load time per item: ${if (itemCount > 0) loadTime / itemCount else 0}ms")
    }

    fun getTotalItemsLoaded(): Int = itemsLoaded

    fun reset() {
        loadStartTime = 0
        itemsLoaded = 0
    }
}
