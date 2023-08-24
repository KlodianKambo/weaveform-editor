package com.klodian.kambo.weaveformeditor.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.klodian.kambo.weaveformeditor.ui.R
import com.klodian.kambo.weaveformeditor.ui.databinding.FragmentMainBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader


@AndroidEntryPoint
class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel: MainViewModel by viewModels()

    private lateinit var binding: FragmentMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Specify the initial directory as the Downloads directory
        // Register the activity result launcher
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
        }.launch("*/*")

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

    }

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

}