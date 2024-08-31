package domain.aggrid

import domain.aggrid.filter.ColumnAdvancedFilterModel
import domain.aggrid.sort.SortModel

data class ServerSideGetRowsRequest(
    val startRow: Int?,
    val endRow: Int?,
    val sortModel: List<SortModel>,
    val filterModel: Map<String, ColumnAdvancedFilterModel>,
    // PLG: All of these below are unsupported atm
    val rowGroupCols: List<ColumnVO>,
    val valueCols: List<ColumnVO>,
    val pivotCols: List<ColumnVO>,
    val pivotMode: Boolean,
    val groupKeys: List<String>
)

data class ColumnVO(
    val id: String,
    val displayName: String,
    val field: String
)
