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


internal class SaveNewSoundUseCase @Inject constructor(private val fileRepository: FileRepository) :
    SaveNewSound {

    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    override suspend operator fun invoke(newWeave: List<WeaveFrequency>): Result<File> {
        val fileName = "audio_track_${dateFormat.format(Date())}.txt"
        return runCatching { fileRepository.saveCoordinatesToFile(newWeave, fileName) }
    }
}