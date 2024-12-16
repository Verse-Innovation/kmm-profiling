package io.verse.profiling.reporter

import io.tagd.arch.access.crosscutting
import io.tagd.arch.domain.crosscutting.codec.JsonCodec
import io.tagd.langx.datatype.asKClass
import io.verse.storage.core.sql.Cursor
import io.verse.storage.core.sql.TableBinding
import io.verse.storage.core.sql.model.SQLiteTable
import io.verse.storage.core.sql.model.binding.SQLiteRowBinding

class ReportTableBinding<P, R : Report<P>>(private val reportingType: String) :
    TableBinding<R> {

    override val tableName: String = TABLE_NAME

    override lateinit var table: SQLiteTable

    override fun initWith(table: SQLiteTable) {
        this.table = table
    }

    override fun bindRecord(record: R): SQLiteRowBinding {
        val jsonCodec: JsonCodec<*>? = crosscutting()
        val reportJson = try {
            jsonCodec?.toJson(record)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
        val columnBindings = listOf(
            bindColumn(COLUMN_REPORT_ID, record.identifier.value),
            bindColumn(COLUMN_REPORTING_TYPE, reportingType),
            bindColumn(COLUMN_CLASS, record::class.qualifiedName),
            bindColumn(COLUMN_JSON, reportJson),
            bindColumn(COLUMN_CREATED_AT, record.createdAt.millisSince1970.millis),
        )
        val compositeKeyBiding = listOf(
            bindColumn(COLUMN_REPORT_ID, record.identifier.value)
        )
        return SQLiteRowBinding(compositeKeyBiding, columnBindings)
    }

    override fun ckColumnValueSet(record: R): Array<String> {
        return arrayOf(record.identifier.value)
    }

    override fun getRecord(cursor: Cursor): R {
        @Suppress("UNCHECKED_CAST")
        return try {
            val json =
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_JSON))
            val clazz =
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASS))

            val jsonCodec: JsonCodec<*>? = crosscutting()
            jsonCodec?.fromJson(json, clazz.asKClass()) as R
        } catch (e: Exception) {
            e.printStackTrace()
            Report.NULL as R
        }
    }

    companion object {

        const val TABLE_NAME = "Report"
        const val COLUMN_REPORT_ID = "report_id"
        const val COLUMN_REPORTING_TYPE = "reporting_type"
        const val COLUMN_CLASS = "clazz"
        const val COLUMN_JSON = "json"
        const val COLUMN_CREATED_AT = "created_at"
    }
}