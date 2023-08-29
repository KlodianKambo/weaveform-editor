package com.klodian.kambo.weaveformeditor.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.klodian.kambo.weaveformeditor.ui.R
import com.klodian.kambo.weaveformeditor.ui.databinding.FragmentMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel: MainViewModel by viewModels()

    private lateinit var binding: FragmentMainBinding

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.saveNewSoundTrack(binding.weaveform.getSelectedRangeValues())
            }
        }

    private val fetchFileResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { fileUri ->
            // Now you can work with the selected file URI
            if (fileUri != null) {
                val inputStream = requireContext().contentResolver.openInputStream(fileUri)
                viewModel.parseForSoundTrackWeaveFrequency(inputStream)
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
                viewModel.saveNewSoundTrack(binding.weaveform.getSelectedRangeValues())
            }
        }

        binding.fetchFileFab.setOnClickListener {
            fetchFileResult.launch("*/*")
        }

        lifecycleScope.launch {
            viewLifecycleOwner
                .repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.weaveFrequencyListFlow.collect {
                        binding.weaveform.setCoordinates(it)
                    }
                }
        }

        lifecycleScope.launch {
            viewLifecycleOwner
                .repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.infoFlow.collect { error ->
                        error?.let {
                            binding.infoLayout.isVisible = true
                            binding.infoIv.setImageResource(it.infoIconResId)
                            binding.infoTv.setText(it.infoMessageResId)
                        } ?: run {
                            binding.infoLayout.isVisible = false
                        }
                    }
                }
        }

    }
}