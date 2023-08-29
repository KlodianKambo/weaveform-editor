package com.klodian.kambo.weaveformeditor.domain

import com.klodian.kambo.weaveformeditor.domain.entities.WeaveFrequency
import com.klodian.kambo.weaveformeditor.domain.repositories.FileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import java.io.File
import java.io.InputStream
import kotlin.test.assertEquals

class GetWeaveFrequencyUseCaseTest {


    private val errorRepo = object : FileRepository {
        override suspend fun getWeaveFrequencyListFromFile(inputStream: InputStream): List<WeaveFrequency> {
            throw RuntimeException("Any error")
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

        val testFile = File("test1.txt")
        testFile.writeText("-1.1 1.0\n-0.6 0.5\n")

        val getWeaveFrequencyListUseCase = GetWeaveFrequencyListUseCase(errorRepo)

        getWeaveFrequencyListUseCase(testFile.inputStream())
            .onSuccess {
                throw RuntimeException("Test failed, onSuccess should not be called")
            }
            .onFailure {
                assert(it is RuntimeException)
            }

        testFile.deleteRecursively()
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun when_empty_input_stream_use_case_fails_gracefully() = runBlockingTest {

        val getWeaveFrequencyListUseCase = GetWeaveFrequencyListUseCase(errorRepo)

        getWeaveFrequencyListUseCase(null)
            .onSuccess {
                throw RuntimeException("Test failed, onSuccess should not be called")
            }
            .onFailure {
                assert(it is RuntimeException)
            }
    }

    private val expectedList = listOf(WeaveFrequency(-1f, 1f), WeaveFrequency(-1f, 1f))
    private val successRepo = object : FileRepository {
        override suspend fun getWeaveFrequencyListFromFile(inputStream: InputStream): List<WeaveFrequency> {
            return expectedList
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
    fun when_correct_input_stream_use_case_success() = runBlockingTest {

        val getWeaveFrequencyListUseCase = GetWeaveFrequencyListUseCase(successRepo)

        val mockedCorrectInput = File("test2.txt")
        mockedCorrectInput.writeText("-1.1 1.0\n-0.6 0.5\n")

        getWeaveFrequencyListUseCase( mockedCorrectInput .inputStream())
            .onSuccess {
                assertEquals(it, expectedList)
            }
            .onFailure {
                throw RuntimeException("Test failed, onFailure should not be called")
            }

        mockedCorrectInput.deleteRecursively()
    }
}