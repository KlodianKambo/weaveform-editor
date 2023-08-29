package com.klodian.kambo.weaveformeditor.domain

import com.klodian.kambo.weaveformeditor.domain.entities.WeaveFrequency
import com.klodian.kambo.weaveformeditor.domain.repositories.FileRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

interface SaveNewSound {
    suspend operator fun invoke(newWeave: List<WeaveFrequency>): Result<File>
}


internal class SaveNewSoundUseCase @Inject constructor(
    private val getGetFileName: GetFileName,
    private val fileRepository: FileRepository
) :
    SaveNewSound {


    override suspend operator fun invoke(newWeave: List<WeaveFrequency>): Result<File> {

        if (newWeave.isEmpty()) return Result.failure(RuntimeException("Empty frequencies"))

        return runCatching {
            fileRepository.saveCoordinatesToFile(newWeave, getGetFileName())
        }
    }
}