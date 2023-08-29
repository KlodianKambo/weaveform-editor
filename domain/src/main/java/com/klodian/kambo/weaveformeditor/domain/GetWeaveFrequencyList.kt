package com.klodian.kambo.weaveformeditor.domain

import com.klodian.kambo.weaveformeditor.domain.entities.WeaveFrequency
import com.klodian.kambo.weaveformeditor.domain.repositories.FileRepository
import java.io.InputStream
import javax.inject.Inject

interface GetWeaveFrequencyList {
    suspend operator fun invoke(inputStream: InputStream?): Result<List<WeaveFrequency>>
}

internal class GetWeaveFrequencyListUseCase @Inject constructor(
    private val fileRepository: FileRepository
) : GetWeaveFrequencyList {

    override suspend operator fun invoke(inputStream: InputStream?): Result<List<WeaveFrequency>> {
        return runCatching { fileRepository.getWeaveFrequencyListFromFile(inputStream) }
    }
}