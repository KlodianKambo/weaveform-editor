package com.klodian.kambo.weaveformeditor.data

import com.klodian.kambo.weaveformeditor.domain.entities.WeaveFrequency
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class FileRepositoryImplTests {

    private val downloadFolder = File("downloads")

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcher = StandardTestDispatcher(TestCoroutineScheduler())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val fileRepositoryImpl = FileRepositoryImpl(downloadFolder, dispatcher)

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun when_file_content_is_correct_then_return_weavefrequencylist() = runTest(dispatcher) {

        val expected = listOf(WeaveFrequency(-1.1f, 1f), WeaveFrequency(-0.6f, 0.5f))
        val testFile = File("test.txt")
        testFile.writeText("-1.1 1.0\n-0.6 0.5\n")

        val weaveFrequencyListResult =
            fileRepositoryImpl.getWeaveFrequencyListFromFile(testFile.inputStream())

        // assert same content as expected
        assertEquals(expected, weaveFrequencyListResult)

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun when_file_content_is_empty_then_return_failure() = runTest(dispatcher) {

        val testFile = File("test.txt")
        testFile.writeText("")

        runCatching {
            fileRepositoryImpl.getWeaveFrequencyListFromFile(testFile.inputStream())
        }.onSuccess {
            // assert same content as expected
            throw RuntimeException("Test failed, the file is correctly formed")
        }.onFailure {
            assert(it is RuntimeException)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun when_input_stream_is_null_then_return_failure() = runTest(dispatcher) {
        runCatching { fileRepositoryImpl.getWeaveFrequencyListFromFile(null) }
            .onSuccess {
                // assert same content as expected
                throw RuntimeException("Test failed, the file is correctly formed")
            }.onFailure {
                assert(it is RuntimeException)
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun when_parts_per_line_greater_than_2_then_return_failure() = runTest(dispatcher) {
        val testFile = File("test.txt")
        // add more parts
        testFile.writeText((1..10).joinToString(separator = " ") { "A" })

        runCatching { fileRepositoryImpl.getWeaveFrequencyListFromFile(testFile.inputStream()) }
            .onSuccess {
                // assert same content as expected
                throw RuntimeException("Test failed, the file is correctly formed")
            }.onFailure {
                assert(it is IllegalArgumentException)
                print(it)
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun when_parts_per_line_equals_1_then_return_failure() = runTest(dispatcher) {
        val testFile = File("test.txt")

        testFile.writeText("A")

        runCatching { fileRepositoryImpl.getWeaveFrequencyListFromFile(testFile.inputStream()) }
            .onSuccess {
                // assert same content as expected
                throw RuntimeException("Test failed, the file is correctly formed")
            }.onFailure {
                assert(it is IllegalArgumentException)
                print(it)
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun when_parts_per_line_are_2_but_not_floats_then_return_number_format_exception() =
        runTest(dispatcher) {

            val testFile = File("test.txt")
            testFile.writeText("A 1\n")

            runCatching { fileRepositoryImpl.getWeaveFrequencyListFromFile(testFile.inputStream()) }
                .onSuccess {
                    // assert same content as expected
                    throw RuntimeException("Test failed, the file is correctly formed")
                }.onFailure {
                    assert(it is NumberFormatException)
                }
        }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun when_save_then_assert() = runTest(dispatcher) {

        val fileInputWeave = listOf(WeaveFrequency(-1.1f, 1f), WeaveFrequency(-0.6f, 0.5f))

        val expectedFileName = "test.txt"

        val savedFile = fileRepositoryImpl.saveCoordinatesToFile(fileInputWeave, expectedFileName)

        assertEquals("$downloadFolder/$expectedFileName", savedFile.path)

        val weaveList = fileRepositoryImpl.getWeaveFrequencyListFromFile(savedFile.inputStream())
        assertEquals(weaveList, fileInputWeave)

        downloadFolder.deleteRecursively()
    }
}