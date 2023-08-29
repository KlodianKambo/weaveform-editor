package com.klodian.kambo.weaveformeditor.data

import com.klodian.kambo.weaveformeditor.domain.entities.WeaveFrequency
import com.klodian.kambo.weaveformeditor.domain.repositories.FileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject

class FileRepositoryImpl @Inject constructor(
    private val savingFileFolderPath: File,
    private val coroutineDispatcher: CoroutineDispatcher
) : FileRepository {

    override suspend fun getWeaveFrequencyListFromFile(inputStream: InputStream?): List<WeaveFrequency> =
        withContext(coroutineDispatcher) {
            if (inputStream != null) {
                return@withContext parseCoordinates(
                    inputStream,
                    coroutineDispatcher
                )
            }
            throw RuntimeException("Something went wrong")
        }


    private suspend fun parseCoordinates(
        inputStream: InputStream,
        coroutineDispatcher: CoroutineDispatcher
    ): List<WeaveFrequency> = withContext(coroutineDispatcher) {

        val coordinates: MutableList<WeaveFrequency> = ArrayList()

        val reader = BufferedReader(InputStreamReader(inputStream))

        var line: String? = reader.readLine()
            ?: run {
                reader.close()
                throw RuntimeException("Empty file")
            }

        while (!line.isNullOrEmpty()) {

            yield()

            val partsPerLine = line
                .split(" ".toRegex(), 16)
                .dropLastWhile { it.isEmpty() }

            if (partsPerLine.size == 2) {
                try {

                    val minValue = partsPerLine[0].toFloat()
                    val maxValue = partsPerLine[1].toFloat()

                    coordinates.add(WeaveFrequency(minValue, maxValue))
                    line = reader.readLine()

                } catch (e: NumberFormatException) {
                    reader.close()
                    throw e
                }
            } else {
                reader.close()
                throw IllegalArgumentException("Error, file has parts per line = ${partsPerLine.size}. Expected is 2")
            }
        }

        reader.close()
        coordinates
    }

    override suspend fun saveCoordinatesToFile(
        coordinates: List<WeaveFrequency>,
        fileName: String
    ): File = withContext(coroutineDispatcher) {

        if (!savingFileFolderPath.exists()) {
            savingFileFolderPath.mkdir()
        }

        val file = File(savingFileFolderPath, fileName)

        file.bufferedWriter().use { writer ->
            for (coordinate in coordinates) {
                yield()
                writer.write("${coordinate.minValue} ${coordinate.maxValue}\n")
            }
        }

        file
    }
}