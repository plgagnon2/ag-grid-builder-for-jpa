# ag-grid-builder-for-jpa

# Usage

```kotlin

interface MyRepository : JpaRepository<T, UUID>, JpaSpecificationExecutor<T> {

...

@Service
class SomeService(
    private val someJpaRepository: MyRepository
) {
    fun fetch(request: ServerSideGetRowsRequest): SomeResponseDTO {
        val specifications = SpecificationBuilder.build<T>(request.filterModel)
        val sort = SortBuilder.build(request.sortModel)
        val pageRequest = PageRequestBuilder.build(request.startRow, request.endRow, sort)

        if (pageRequest == null) {
            val results = someJpaRepository.findAll(specifications, sort)
            return SomeResponseDTO(
                rowData = results,
                rowCount = results.size.toLong()
            )
        }

        val pagedResults = someJpaRepository.findAll(specifications, pageRequest)
        return SomeResponseDTO(
            rowData = pagedResults.content,
            rowCount = pagedResults.totalElements
        )
    }
}
```