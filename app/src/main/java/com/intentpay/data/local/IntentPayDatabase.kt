package com.intentpay.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.intentpay.categorization.normalizeMerchantKey

@Database(entities = [TransactionEntity::class, BudgetEntity::class, MerchantRuleEntity::class], version = 3, exportSchema = false)
abstract class IntentPayDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun merchantRuleDao(): MerchantRuleDao

    companion object {
        /**
         * Normalizes merchant_rules.merchantPattern and collapses rows that share a normalized key:
         * the row with the highest timesUsed survives and absorbs the others' usage. Idempotent.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                data class Row(
                    val id: Long, val key: String, val timesUsed: Int,
                    val lastUsed: Long, val byUser: Boolean
                )

                val rows = mutableListOf<Row>()
                db.query("SELECT id, merchantPattern, timesUsed, lastUsedMillis, createdByUser FROM merchant_rules").use { c ->
                    while (c.moveToNext()) {
                        rows += Row(
                            c.getLong(0), normalizeMerchantKey(c.getString(1) ?: ""),
                            c.getInt(2), c.getLong(3), c.getInt(4) != 0
                        )
                    }
                }

                db.beginTransaction()
                try {
                    rows.groupBy { it.key }.forEach { (key, group) ->
                        if (key.isEmpty()) {
                            group.forEach { db.execSQL("DELETE FROM merchant_rules WHERE id = ?", arrayOf<Any>(it.id)) }
                            return@forEach
                        }
                        val winner = group.maxWith(compareBy<Row> { it.timesUsed }.thenBy { it.lastUsed })
                        // Delete losers first so the unique index on merchantPattern is never violated.
                        group.filter { it.id != winner.id }.forEach {
                            db.execSQL("DELETE FROM merchant_rules WHERE id = ?", arrayOf<Any>(it.id))
                        }
                        db.execSQL(
                            "UPDATE merchant_rules SET merchantPattern = ?, timesUsed = ?, lastUsedMillis = ?, createdByUser = ? WHERE id = ?",
                            arrayOf<Any>(
                                key, group.sumOf { it.timesUsed }, group.maxOf { it.lastUsed },
                                if (group.any { it.byUser }) 1 else 0, winner.id
                            )
                        )
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }

        @Volatile
        private var INSTANCE: IntentPayDatabase? = null

        fun getDatabase(context: Context): IntentPayDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IntentPayDatabase::class.java,
                    "intentpay_database"
                )
                .addMigrations(MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
