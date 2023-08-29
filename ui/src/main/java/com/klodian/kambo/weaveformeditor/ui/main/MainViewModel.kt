package com.klodian.kambo.weaveformeditor.ui.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klodian.kambo.weaveformeditor.domain.GetWeaveFrequencyList
import com.klodian.kambo.weaveformeditor.domain.SaveNewSound
import com.klodian.kambo.weaveformeditor.domain.entities.WeaveFrequency
import com.klodian.kambo.weaveformeditor.ui.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.InputStream
import javax.inject.Inject

data class UiWeaveFrequency(
    val minValue: Float,
    val maxValue: Float
)

data class UiInfo(
    @StringRes val infoMessageResId: Int,
    @DrawableRes val infoIconResId: Int
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getWeaveFrequencyList: GetWeaveFrequencyList,
    private val saveNewSound: SaveNewSound
) : ViewModel() {

    private val _weaveFrequencyListFlow = MutableStateFlow(emptyList<UiWeaveFrequency>())
    val weaveFrequencyListFlow = _weaveFrequencyListFlow

    private val welcomeUiInfo = UiInfo(
        R.string.welcome,
        R.drawable.ic_audio_file
    )

    private val _infoFlow = MutableStateFlow<UiInfo?>(welcomeUiInfo)
    val infoFlow = _infoFlow

    private val _loadingFlow = MutableStateFlow(false)
    val loadingFlow = _loadingFlow

    fun parseForSoundTrackWeaveFrequency(inputStream: InputStream?) {
        viewModelScope.launch {

            _loadingFlow.emit(true)

            getWeaveFrequencyList(inputStream)
                .onSuccess {
                    _infoFlow.emit(null)
                    _weaveFrequencyListFlow.emit(it.map {
                        UiWeaveFrequency(
                            it.minValue,
                            it.maxValue
                        )
                    })
                }
                .onFailure {
                    _weaveFrequencyListFlow.emit(emptyList())
                    _infoFlow.emit(
                        UiInfo(
                            R.string.error_generic,
                            R.drawable.ic_error
                        )
                    )
                }

            _loadingFlow.emit(false)
        }
    }

    fun saveNewSoundTrack(selectedRangeValues: List<UiWeaveFrequency>) {
        viewModelScope
            .launch {
                _loadingFlow.emit(true)

                selectedRangeValues
                    .map { WeaveFrequency(it.minValue, it.maxValue) }
                    .let { saveNewSound(it) }
                    .onFailure {
                        _infoFlow.emit(
                            UiInfo(
                                R.string.error_generic,
                                R.drawable.ic_error
                            )
                        )
                    }

                _loadingFlow.emit(false)
            }
    }
}