package domain.aggrid.filter

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

object SpecificationBuilder {
    private val LOGGER: Logger = LoggerFactory.getLogger(SpecificationBuilder::class.java)

    fun <T> build(filterModel: Map<String, ColumnAdvancedFilterModel>): Specification<T> =
        createFilterSpecification<T>(filterModel)?.also {
            LOGGER.info("Created Specification with filters: $filterModel")
        } ?: Specification.where(null)

    private fun <T> createFilterSpecification(filterModel: Map<String, ColumnAdvancedFilterModel>?): Specification<T>? =
        filterModel?.let {
            Specification { root, criteriaQuery, criteriaBuilder ->
                val predicates = it.mapNotNull { (column, filter) ->
                    createPredicate(column, filter, root, criteriaQuery, criteriaBuilder)
                }
                criteriaBuilder.and(*predicates.toTypedArray())
            }
        }

    private fun <T> createPredicate(
        column: String,
        filterModel: ColumnAdvancedFilterModel,
        root: Root<T>,
        criteriaQuery: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? =
        try {
            if (filterModel.conditions != null) {
                handleConditions(column, filterModel.operator, filterModel.conditions, root, criteriaQuery, criteriaBuilder)
            }

            when (filterModel) {
                is TextAdvancedFilterModel ->
                    handleTextFilterModel(column, filterModel.type, filterModel.filter, root, criteriaBuilder)
                is NumberAdvancedFilterModel ->
                    handleNumberFilterModel(column, filterModel.type, filterModel.filter, filterModel.filterTo, root, criteriaBuilder)
                is BooleanAdvancedFilterModel ->
                    handleBooleanFilterModel(column, filterModel.type, root, criteriaBuilder)
                is DateAdvancedFilterModel ->
                    handleDateFilterModel(column, filterModel.type, filterModel.dateFrom, filterModel.dateTo, root, criteriaBuilder)
                is ObjectAdvancedFilterModel ->
                    handleObjectFilterModel(column, filterModel.type, filterModel.filter, root, criteriaBuilder)
            }
        } catch (e: Exception) {
            LOGGER.error("Error creating predicate for column: $column with filter: $filterModel", e)
            null
        }

    private fun <T> handleConditions(
        column: String,
        operator: JoinOperator?,
        conditions: List<ColumnAdvancedFilterModel>,
        root: Root<T>,
        criteriaQuery: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
        val predicates = conditions.mapNotNull { createPredicate(column, it, root, criteriaQuery, criteriaBuilder) }
        return when (operator) {
            JoinOperator.AND -> criteriaBuilder.and(*predicates.toTypedArray())
            JoinOperator.OR -> criteriaBuilder.or(*predicates.toTypedArray())
            else -> null
        }
    }

    private fun <T> handleTextFilterModel(
        column: String,
        type: TextAdvancedFilterModelType?,
        value: String?,
        root: Root<T>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
        if (value == null) return null

        val path = root.get<String>(column)
        return when (type) {
            TextAdvancedFilterModelType.EQUALS -> criteriaBuilder.equal(path, value)
            TextAdvancedFilterModelType.NOT_EQUAL -> criteriaBuilder.notEqual(path, value)
            TextAdvancedFilterModelType.CONTAINS -> criteriaBuilder.like(path, "%$value%")
            TextAdvancedFilterModelType.NOT_CONTAINS -> criteriaBuilder.notLike(path, "%$value%")
            TextAdvancedFilterModelType.STARTS_WITH -> criteriaBuilder.like(path, "$value%")
            TextAdvancedFilterModelType.ENDS_WITH -> criteriaBuilder.like(path, "%$value")
            TextAdvancedFilterModelType.BLANK -> criteriaBuilder.isNull(path)
            TextAdvancedFilterModelType.NOT_BLANK -> criteriaBuilder.isNotNull(path)
            else -> null
        }
    }

    private fun <T> handleNumberFilterModel(
        column: String,
        type: NumberAdvancedFilterModelType?,
        valueFrom: Number?,
        valueTo: Number?,
        root: Root<T>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
        if (valueFrom == null) return null

        val path = root.get<Number>(column)
        return when (type) {
            NumberAdvancedFilterModelType.EQUALS -> criteriaBuilder.equal(path, valueFrom)
            NumberAdvancedFilterModelType.NOT_EQUAL -> criteriaBuilder.notEqual(path, valueFrom)
            NumberAdvancedFilterModelType.LESS_THAN -> criteriaBuilder.lt(path, valueFrom)
            NumberAdvancedFilterModelType.LESS_THAN_OR_EQUAL -> criteriaBuilder.le(path, valueFrom)
            NumberAdvancedFilterModelType.GREATER_THAN -> criteriaBuilder.gt(path, valueFrom)
            NumberAdvancedFilterModelType.GREATER_THAN_OR_EQUAL -> criteriaBuilder.ge(path, valueFrom)
            NumberAdvancedFilterModelType.BLANK -> criteriaBuilder.isNull(path)
            NumberAdvancedFilterModelType.NOT_BLANK -> criteriaBuilder.isNotNull(path)
            NumberAdvancedFilterModelType.IN_RANGE -> {
                val lowerBound = criteriaBuilder.ge(path, valueFrom)
                val upperBound = valueTo
                    ?.let { criteriaBuilder.le(path, valueTo) }
                    ?: criteriaBuilder.conjunction()

                criteriaBuilder.and(lowerBound, upperBound)
            }
            else -> null
        }
    }

    private fun <T> handleBooleanFilterModel(
        column: String,
        type: BooleanAdvancedFilterModelType?,
        root: Root<T>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
        val path = root.get<Boolean>(column)
        val booleanValue = type == BooleanAdvancedFilterModelType.TRUE

        return criteriaBuilder.equal(path, booleanValue)
    }

    private fun <T> handleDateFilterModel(
        column: String,
        type: DateAdvancedFilterModelType?,
        dateFrom: String?,
        dateTo: String?,
        root: Root<T>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
        if (dateFrom == null) return null

        val path = root.get<LocalDateTime>(column)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val parsedDateFrom = LocalDateTime.parse(dateFrom, formatter)

        return when (type) {
            DateAdvancedFilterModelType.EQUALS -> criteriaBuilder.equal(path, parsedDateFrom)
            DateAdvancedFilterModelType.NOT_EQUAL -> criteriaBuilder.notEqual(path, parsedDateFrom)
            DateAdvancedFilterModelType.LESS_THAN -> criteriaBuilder.lessThan(path, parsedDateFrom)
            DateAdvancedFilterModelType.LESS_THAN_OR_EQUAL -> criteriaBuilder.lessThanOrEqualTo(path, parsedDateFrom)
            DateAdvancedFilterModelType.GREATER_THAN -> criteriaBuilder.greaterThan(path, parsedDateFrom)
            DateAdvancedFilterModelType.GREATER_THAN_OR_EQUAL -> criteriaBuilder.greaterThanOrEqualTo(path, parsedDateFrom)
            DateAdvancedFilterModelType.BLANK -> criteriaBuilder.isNull(path)
            DateAdvancedFilterModelType.NOT_BLANK -> criteriaBuilder.isNotNull(path)
            DateAdvancedFilterModelType.IN_RANGE -> {
                val lowerBound = criteriaBuilder.greaterThanOrEqualTo(path, parsedDateFrom)
                val upperBound = dateTo?.let {
                    criteriaBuilder.lessThanOrEqualTo(path, LocalDateTime.parse(it, formatter))
                } ?: criteriaBuilder.conjunction()

                criteriaBuilder.and(lowerBound, upperBound)
            }
            else -> null
        }
    }

    private fun <T> handleObjectFilterModel(
        column: String,
        type: TextAdvancedFilterModelType?,
        value: String?,
        root: Root<T>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
        if (value == null) return null

        return when (type) {
            TextAdvancedFilterModelType.EQUALS -> criteriaBuilder.equal(root.get<Any>(column), value)
            TextAdvancedFilterModelType.NOT_EQUAL -> criteriaBuilder.notEqual(root.get<Any>(column), value)
            TextAdvancedFilterModelType.CONTAINS -> criteriaBuilder.like(root.get(column), "%$value%")
            TextAdvancedFilterModelType.NOT_CONTAINS -> criteriaBuilder.notLike(root.get(column), "%$value%")
            TextAdvancedFilterModelType.STARTS_WITH -> criteriaBuilder.like(root.get(column), "$value%")
            TextAdvancedFilterModelType.ENDS_WITH -> criteriaBuilder.like(root.get(column), "%$value")
            TextAdvancedFilterModelType.BLANK -> criteriaBuilder.isNull(root.get<Any>(column))
            TextAdvancedFilterModelType.NOT_BLANK -> criteriaBuilder.isNotNull(root.get<Any>(column))
            else -> null
        }
    }
}
