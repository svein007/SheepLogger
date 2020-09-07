package com.example.osmdroidexample.database.entities

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.osmdroidexample.R
import kotlin.math.max

@Entity(tableName = "counter_table")
data class Counter (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "counter_id")
    var counterId: Long = 0L,

    @ColumnInfo(name = "counter_type")
    val counterType: CountType,

    @ColumnInfo(name = "counter_value")
    var counterValue: Int = 0,

    @ColumnInfo(name = "counter_owner_observation_id")
    var counterOwnerObservationId: Long
) {

    fun inc() {
        counterValue += 1
    }

    fun dec() {
        counterValue = max(0, counterValue - 1)
    }

    fun getStr(context: Context): String {
        return counterType.str(context)
    }

    // TODO: slips-farge => antall
    fun sheepChildCount(): Long {
        return 0
    }

    enum class CountType {
        SHEEP, LAMB, BLACK, GREY, WHITE, RED_TIE, BLUE_TIE, YELLOW_TIE, GREEN_TIE;
        fun next(): CountType {
            return values()[(this.ordinal+1) % values().size]
        }
        fun prev(): CountType {
            return values()[(this.ordinal-1+values().size) % values().size]
        }
        fun str(context: Context): String {
            return when (this) {
                SHEEP -> context.resources.getString(R.string.sheep)
                LAMB -> context.resources.getString(R.string.lamb)
                BLACK -> context.resources.getString(R.string.black)
                GREY -> context.resources.getString(R.string.grey)
                WHITE -> context.resources.getString(R.string.white)
                RED_TIE -> context.resources.getString(R.string.red_tie)
                BLUE_TIE -> context.resources.getString(R.string.blue_tie)
                YELLOW_TIE -> context.resources.getString(R.string.yellow_tie)
                GREEN_TIE -> context.resources.getString(R.string.green_tie)
            }
        }
    }

}

