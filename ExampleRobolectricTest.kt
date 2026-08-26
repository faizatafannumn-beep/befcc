package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.UserRole
import com.example.data.repository.BefccRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: BefccRepository
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = BefccRepository(database, testScope)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("BEFCC", appName)
    }

    @Test
    fun `official contact number is unconfigured initially`() = runTest {
        val initialSetting = repository.getOfficialContactNumberFlow().first()
        assertNull(initialSetting)
    }

    @Test
    fun `admin can update official contact number dynamically`() = runTest {
        // Log in as Super Admin Leader Maruf Hossain
        val loginResult = repository.login("maruf", "admin123")
        assertTrue(loginResult.isSuccess)
        val adminUser = loginResult.getOrNull()
        assertTrue(adminUser?.isAdminOrLeader == true)

        // Update official contact number
        val updateResult = repository.updateOfficialContactNumber("+8801700000000")
        assertTrue(updateResult.isSuccess)

        // Verify updated in database
        val updatedSetting = repository.getOfficialContactNumberFlow().first()
        assertEquals("+8801700000000", updatedSetting?.value)
    }

    @Test
    fun `leader login grants full admin access and ability to create tournament`() = runTest {
        val loginResult = repository.login("maruf", "admin123")
        assertTrue(loginResult.isSuccess)
        val leader = loginResult.getOrNull()
        assertEquals(UserRole.SUPER_ADMIN, leader?.role)
        assertTrue(leader?.isAdminOrLeader == true)

        // Leader creates a tournament
        repository.createTournament(
            name = "BEFCC Pro Championship 2026",
            type = "Group Stage + Knockout",
            playerLimit = 16,
            entryFee = 100.0,
            prizePool = "৳5000",
            teamType = com.example.data.model.TournamentTeamType.NATIONAL_TEAMS,
            availableTeams = "Bangladesh, Brazil, Argentina, France",
            rules = "Standard 10-min match",
            startDate = "2026-09-01",
            endDate = "2026-09-10"
        )

        val tournaments = repository.getAllTournaments().first()
        val created = tournaments.find { it.name == "BEFCC Pro Championship 2026" }
        assertTrue(created != null)
        assertEquals(16, created?.playerLimit)
    }

    @Test
    fun `player cannot update official contact number`() = runTest {
        // Register and login as Player
        val registerResult = repository.registerUser(
            fullName = "Tamim Iqbal",
            username = "tamim_player",
            email = "tamim@example.com",
            inGameUsername = "tamim_ef",
            favoriteTeam = "Arsenal",
            divisionRank = "Division 1"
        )
        assertTrue(registerResult.isSuccess)

        val playerUser = registerResult.getOrNull()
        assertEquals(UserRole.PLAYER, playerUser?.role)
        assertFalse(playerUser?.isAdminOrLeader == true)

        // Attempting to update official contact number must fail
        val updateResult = repository.updateOfficialContactNumber("+8801999999999")
        assertFalse(updateResult.isSuccess)
        assertTrue(updateResult.exceptionOrNull()?.message?.contains("Access denied") == true)
    }
}

