package com.example.osmdroidexample

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.osmdroidexample.database.AppDao
import com.example.osmdroidexample.database.AppDatabase
import com.example.osmdroidexample.database.entities.MapArea
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var appDao: AppDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
        appDao = db.appDatabaseDao
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetMapArea() {
        val mapArea =
            MapArea(mapAreaName = "Oslo")

        val id = appDao.insert(mapArea)

        val resultMapArea = appDao.getMapArea(id)

        Assert.assertEquals("Oslo", resultMapArea?.mapAreaName)
        Assert.assertEquals("map_area_Oslo.sqlite", resultMapArea?.getSqliteFilename())

    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetMapAreaByName() {
        val mapArea =
            MapArea(mapAreaName = "Oslo")

        val id = appDao.insert(mapArea)

        val resultMapArea = appDao.getMapArea("Oslo")

        Assert.assertEquals("Oslo", resultMapArea?.mapAreaName)
        Assert.assertEquals("map_area_Oslo.sqlite", resultMapArea?.getSqliteFilename())

    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetMapAreas() {
        val mapArea =
            MapArea(mapAreaName = "Oslo")

        val mapArea2 =
            MapArea(mapAreaName = "Bergen")

        appDao.insert(mapArea)
        appDao.insert(mapArea2)

        val resultMapAreas = appDao.getMapAreas()

        Assert.assertEquals(2, resultMapAreas.size)

    }
}