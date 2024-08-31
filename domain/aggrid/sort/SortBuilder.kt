package domain.aggrid.sort

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort

object SortBuilder {
    private val LOGGER: Logger = LoggerFactory.getLogger(SortBuilder::class.java)

    fun build(sortModels: List<SortModel>): Sort =
        Sort
            .by(
                sortModels.map {
                    Sort.Order(
                        if (it.sort == "asc") Sort.Direction.ASC else Sort.Direction.DESC,
                        it.colId
                    )
                }
            ).also {
                LOGGER.info("Created Sort with sort model: $sortModels")
            }
}
