package com.klodian.kambo.weaveformeditor.domain

import com.klodian.kambo.weaveformeditor.domain.entities.WeaveFrequency
import com.klodian.kambo.weaveformeditor.domain.repositories.FileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import java.io.File
import java.io.InputStream
import kotlin.test.assertEquals

class SaveNewSoundUseCaseTests {

    private val fileName = "test_file.txt"
    private val getFileName = object : GetFileName {
        override fun invoke(): String = fileName
    }

    private val fileRepo = object : FileRepository {
        override suspend fun getWeaveFrequencyListFromFile(inputStream: InputStream): List<WeaveFrequency> {
            return emptyList()
        }

        override suspend fun saveCoordinatesToFile(
            coordinates: List<WeaveFrequency>,
            fileName: String
        ): File {
            return File(fileName)
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun when_weave_is_empty_assert_failure() = runBlockingTest {
        val saveNewSound = SaveNewSoundUseCase(getFileName, fileRepo)

        saveNewSound(listOf())
            .onSuccess {
                throw RuntimeException("Test failed, onSuccess should not be called")
            }
            .onFailure {
                assert(it is RuntimeException)
            }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun when_weave_is_not_empty_assert_success() = runBlockingTest {
        val saveNewSound = SaveNewSoundUseCase(getFileName, fileRepo)

        saveNewSound(listOf(WeaveFrequency(-1f, 1f)))
            .onSuccess {
                assertEquals(fileName, it.name)
            }
            .onFailure {
                throw RuntimeException("Test failed, onFailure should not be called")
            }
    }


    private val errorRepo = object : FileRepository {
        override suspend fun getWeaveFrequencyListFromFile(inputStream: InputStream): List<WeaveFrequency> {
            return emptyList()
        }

        override suspend fun saveCoordinatesToFile(
            coordinates: List<WeaveFrequency>,
            fileName: String
        ): File {
            throw RuntimeException("Any error")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun when_repo_throws_use_case_fails_gracefully() = runBlockingTest {
        val saveNewSound = SaveNewSoundUseCase(getFileName, errorRepo)

        saveNewSound(listOf(WeaveFrequency(-1f, 1f)))
            .onSuccess {
                throw RuntimeException("Test failed, onSuccess should not be called")
            }
            .onFailure {
                assert(it is RuntimeException)
            }
    }
}