package com.boon4681.android_student.viewmodel

import com.boon4681.android_student.domain.model.SessionExpiredException
import com.boon4681.android_student.domain.usecase.AddStudentUseCase
import com.boon4681.android_student.domain.usecase.DeleteStudentUseCase
import com.boon4681.android_student.domain.usecase.GetStudentsUseCase
import com.boon4681.android_student.domain.usecase.RefreshStudentsUseCase
import com.boon4681.android_student.domain.usecase.SyncStudentsUseCase
import com.boon4681.android_student.domain.usecase.UpdateStudentUseCase
import com.boon4681.android_student.fake.FakeNetworkMonitor
import com.boon4681.android_student.fake.FakeStudentRepository
import com.boon4681.android_student.fake.FakeSyncRequester
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class StudentViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val syncRequester = FakeSyncRequester()

    private fun createViewModel(
        repository: FakeStudentRepository,
        networkMonitor: FakeNetworkMonitor = FakeNetworkMonitor()
    ): StudentViewModel {
        return StudentViewModel(
            getStudentsUseCase = GetStudentsUseCase(repository),
            refreshStudentsUseCase = RefreshStudentsUseCase(repository),
            syncStudentsUseCase = SyncStudentsUseCase(repository),
            addStudentUseCase = AddStudentUseCase(repository),
            updateStudentUseCase = UpdateStudentUseCase(repository),
            deleteStudentUseCase = DeleteStudentUseCase(repository),
            networkMonitor = networkMonitor,
            syncRequester = syncRequester
        )
    }

    @Test
    fun `adding a student requests background sync after saving`() = runTest {
        val repository = FakeStudentRepository(initial = emptyList())
        val viewModel = createViewModel(repository)

        viewModel.addStudent("66007002", "Offline Add", "CS")

        assertEquals(1, repository.students.value.size)
        assertEquals(1, syncRequester.requests)
    }

    @Test
    fun `invalid student is not saved and does not request sync`() = runTest {
        val repository = FakeStudentRepository(initial = emptyList())
        val viewModel = createViewModel(repository)

        viewModel.addStudent("66007002", "", "CS")

        assertEquals(0, repository.students.value.size)
        assertEquals(0, syncRequester.requests)
        assertEquals("Name is required", viewModel.errorMessage)
    }

    @Test
    fun `students state comes from GetStudentsUseCase`() = runTest {
        val viewModel = createViewModel(FakeStudentRepository())
        backgroundScope.launch(dispatcher) { viewModel.students.collect {} }

        assertEquals(1, viewModel.students.value.size)
        assertEquals("สมชาย", viewModel.students.value.first().name)
    }

    @Test
    fun `add then sync marks student as synced`() = runTest {
        val repository = FakeStudentRepository(initial = emptyList())
        val viewModel = createViewModel(repository)
        backgroundScope.launch(dispatcher) { viewModel.students.collect {} }

        viewModel.addStudent("66007001", "Lab7 Test", "CS")
        assertEquals("PENDING", viewModel.students.value.single().syncStatus)

        viewModel.syncStudents()

        assertEquals(1, repository.syncCalls)
        assertEquals(1, repository.refreshCalls)
        assertEquals("SYNCED", viewModel.students.value.single().syncStatus)
        assertFalse(viewModel.isLoading)
    }

    @Test
    fun `offline refresh keeps local data and shows message`() = runTest {
        val repository = FakeStudentRepository()
        repository.errorToThrow = IOException("offline")
        val viewModel = createViewModel(repository)
        backgroundScope.launch(dispatcher) { viewModel.students.collect {} }

        viewModel.loadStudents()

        assertEquals(1, viewModel.students.value.size)
        assertEquals("เชื่อมต่อ Server ไม่ได้ แสดงข้อมูลในเครื่อง", viewModel.errorMessage)
        assertFalse(viewModel.sessionExpired)
    }

    @Test
    fun `expired session asks screen to log out`() = runTest {
        val repository = FakeStudentRepository()
        repository.errorToThrow = SessionExpiredException()
        val viewModel = createViewModel(repository)

        viewModel.syncStudents()

        assertTrue(viewModel.sessionExpired)
        assertNull(viewModel.errorMessage)
    }

    @Test
    fun `search filters by major`() = runTest {
        val repository = FakeStudentRepository()
        val viewModel = createViewModel(repository)
        backgroundScope.launch(dispatcher) { viewModel.students.collect {} }
        viewModel.addStudent("66007001", "Lab7 Test", "Computer Science")

        viewModel.onQueryChange("computer")
        val result = viewModel.filterStudents(viewModel.students.value)

        assertEquals(listOf("Lab7 Test"), result.map { it.name })
    }

    @Test
    fun `online state comes from network monitor`() = runTest {
        val networkMonitor = FakeNetworkMonitor(online = false)
        val viewModel = createViewModel(FakeStudentRepository(), networkMonitor)
        assertFalse(viewModel.isOnline)

        networkMonitor.isOnline.value = true

        assertTrue(viewModel.isOnline)
    }
}
