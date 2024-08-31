package domain.aggrid.paging

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

object PageRequestBuilder {
    private val LOGGER: Logger = LoggerFactory.getLogger(PageRequestBuilder::class.java)

    fun build(
        startRow: Int?,
        endRow: Int?,
        sort: Sort
    ): PageRequest? {
        if (startRow == null || endRow == null) {
            LOGGER.error("Start row or end row cannot be null: startRow=$startRow, endRow=$endRow")
            return null
        }

        if (startRow >= endRow) {
            LOGGER.error("Start row must be less than end row: startRow=$startRow, endRow=$endRow")
            return null
        }

        val pageSize = endRow - startRow
        if (pageSize <= 0) {
            LOGGER.error("Page size must be greater than zero: pageSize=$pageSize")
            return null
        }

        return PageRequest.of(startRow / pageSize, pageSize, sort).also {
            LOGGER.info("Created PageRequest with pageSize=$pageSize from $startRow to $endRow with sort=$sort")
        }
    }
}
