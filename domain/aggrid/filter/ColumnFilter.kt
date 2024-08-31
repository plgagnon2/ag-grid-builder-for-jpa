package domain.aggrid.filter

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "filterType"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = TextAdvancedFilterModel::class, name = "text"),
    JsonSubTypes.Type(value = NumberAdvancedFilterModel::class, name = "number"),
    JsonSubTypes.Type(value = BooleanAdvancedFilterModel::class, name = "boolean"),
    JsonSubTypes.Type(value = DateAdvancedFilterModel::class, name = "date"),
    JsonSubTypes.Type(value = ObjectAdvancedFilterModel::class, name = "object")
)
sealed class ColumnAdvancedFilterModel {
    val operator: JoinOperator? = null
    val conditions: List<ColumnAdvancedFilterModel>? = null
}

data class TextAdvancedFilterModel(
    val filterType: String = "text",
    val type: TextAdvancedFilterModelType? = null,
    val filter: String? = null
) : ColumnAdvancedFilterModel()

data class NumberAdvancedFilterModel(
    val filterType: String = "number",
    val type: NumberAdvancedFilterModelType? = null,
    val filter: Number? = null,
    val filterTo: Number? = null
) : ColumnAdvancedFilterModel()

data class BooleanAdvancedFilterModel(
    val filterType: String = "boolean",
    val type: BooleanAdvancedFilterModelType? = null
) : ColumnAdvancedFilterModel()

data class DateAdvancedFilterModel(
    val filterType: String = "date",
    val type: DateAdvancedFilterModelType? = null,
    val dateFrom: String? = null,
    val dateTo: String? = null
) : ColumnAdvancedFilterModel()

data class ObjectAdvancedFilterModel(
    val filterType: String = "object",
    val type: TextAdvancedFilterModelType? = null,
    val filter: String? = null
) : ColumnAdvancedFilterModel()

enum class JoinOperator {
    AND,
    OR
}

enum class TextAdvancedFilterModelType {
    @JsonProperty("equals")
    EQUALS,

    @JsonProperty("notEqual")
    NOT_EQUAL,

    @JsonProperty("contains")
    CONTAINS,

    @JsonProperty("notContains")
    NOT_CONTAINS,

    @JsonProperty("startsWith")
    STARTS_WITH,

    @JsonProperty("endsWith")
    ENDS_WITH,

    @JsonProperty("blank")
    BLANK,

    @JsonProperty("notBlank")
    NOT_BLANK
}

enum class NumberAdvancedFilterModelType {
    @JsonProperty("equals")
    EQUALS,

    @JsonProperty("notEqual")
    NOT_EQUAL,

    @JsonProperty("lessThan")
    LESS_THAN,

    @JsonProperty("lessThanOrEqual")
    LESS_THAN_OR_EQUAL,

    @JsonProperty("greaterThan")
    GREATER_THAN,

    @JsonProperty("greaterThanOrEqual")
    GREATER_THAN_OR_EQUAL,

    @JsonProperty("blank")
    BLANK,

    @JsonProperty("notBlank")
    NOT_BLANK,

    @JsonProperty("inRange")
    IN_RANGE
}

enum class DateAdvancedFilterModelType {
    @JsonProperty("equals")
    EQUALS,

    @JsonProperty("notEqual")
    NOT_EQUAL,

    @JsonProperty("lessThan")
    LESS_THAN,

    @JsonProperty("lessThanOrEqual")
    LESS_THAN_OR_EQUAL,

    @JsonProperty("greaterThan")
    GREATER_THAN,

    @JsonProperty("greaterThanOrEqual")
    GREATER_THAN_OR_EQUAL,

    @JsonProperty("blank")
    BLANK,

    @JsonProperty("notBlank")
    NOT_BLANK,

    @JsonProperty("inRange")
    IN_RANGE
}

enum class BooleanAdvancedFilterModelType {
    @JsonProperty("true")
    TRUE,

    @JsonProperty("false")
    FALSE
}
