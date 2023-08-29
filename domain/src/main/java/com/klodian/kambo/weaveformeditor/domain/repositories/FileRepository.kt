package com.klodian.kambo.weaveformeditor.domain.repositories

import com.klodian.kambo.weaveformeditor.domain.entities.WeaveFrequency
import java.io.File
import java.io.InputStream

interface FileRepository {
    suspend fun getWeaveFrequencyListFromFile(inputStream: InputStream): List<WeaveFrequency>
    suspend fun saveCoordinatesToFile(coordinates: List<WeaveFrequency>, fileName: String): File
}