package com.klodian.kambo.weaveformeditor.domain

import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

interface GetFileName {
    operator fun invoke(): String
}

internal class GetFileNameUseCase @Inject constructor():  GetFileName {
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    override operator fun invoke(): String {
        return "audio_track_${dateFormat.format(Date())}.txt"
    }
}