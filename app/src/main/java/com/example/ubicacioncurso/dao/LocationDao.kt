package com.example.ubicacioncurso.dao

import androidx.room.*
import com.example.ubicacioncurso.entity.Location

@Dao
interface LocationDao {
    @Insert
    fun insert(location: Location)

    @Query("select * from location_table")
    fun query(): List<Location>
}