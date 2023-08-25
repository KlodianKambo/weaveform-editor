package com.klodian.kambo.weaveformeditor.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.klodian.kambo.weaveformeditor.ui.R
import com.klodian.kambo.weaveformeditor.ui.databinding.FragmentMainBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel: MainViewModel by viewModels()

    private lateinit var binding: FragmentMainBinding
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                saveCoordinatesToFile(binding.weaveform.getSelectedRangeValues())
            }
        }

    private val fetchFileResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { fileUri ->

            // Now you can work with the selected file URI
            if (fileUri != null) {
                try {
                    val inputStream: InputStream? =
                        requireActivity().contentResolver.openInputStream(fileUri)

                    inputStream?.let {
                        val coordinates = parseCoordinates(inputStream)
                        binding.weaveform.setCoordinates(coordinates)
                        for (coordinate in coordinates) {
                            Log.d("Coordinate", "X: ${coordinate.first}, Y: ${coordinate.second}")
                        }
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMainBinding.bind(
            inflater.inflate(R.layout.fragment_main, container, false)
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.saveSliceFab.setOnClickListener {
            // Check if permission is granted
            val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(
                    requireContext().applicationContext,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request permission using the launcher
                requestPermissionLauncher.launch(permission)
            } else {
                saveCoordinatesToFile(binding.weaveform.getSelectedRangeValues())
            }
        }

        binding.fetchFileFab.setOnClickListener {
            fetchFileResult.launch("*/*")
        }
    }

    // TODO move in data layer
    private fun parseCoordinates(inputStream: InputStream): List<Pair<Float, Float>> {
        val coordinates: MutableList<Pair<Float, Float>> = ArrayList()

        val reader = BufferedReader(InputStreamReader(inputStream))
        var line: String? = reader.readLine()
        while (!line.isNullOrEmpty()) {
            val parts = line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            if (parts.size == 2) {
                try {
                    val x = parts[0].toFloat()
                    val y = parts[1].toFloat()
                    coordinates.add(Pair(x, y))
                    line = reader.readLine()
                } catch (e: NumberFormatException) {
                    // Handle parsing errors
                    e.printStackTrace()
                }
            }
        }

        reader.close()
        return coordinates
    }

    private fun saveCoordinatesToFile(coordinates: List<Pair<Float, Float>>) {
        val downloadsFolder =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsFolder, "audio_track_${dateFormat.format(Date())}.txt")
        file.bufferedWriter().use { writer ->
            for (coordinate in coordinates) {
                writer.write("${coordinate.first} ${coordinate.second}\n")
            }
        }
    }

}