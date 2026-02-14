package com.example.nfc.ui.scanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.nfc.databinding.FragmentScannerBinding
import com.example.nfc.utils.PermissionUtils
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScannerFragment : Fragment() {
    
    private var _binding: FragmentScannerBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ScannerViewModel by viewModels()
    
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalysis: ImageAnalysis? = null
    private lateinit var cameraExecutor: ExecutorService
    
    private val barcodeScanner = BarcodeScanning.getClient()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScannerBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        cameraExecutor = Executors.newSingleThreadExecutor()
        
        setupObservers()
        setupClickListeners()
        
        if (PermissionUtils.hasCameraPermission(requireContext())) {
            startCamera()
        } else {
            requestCameraPermission()
        }
    }
    
    private fun setupObservers() {
        viewModel.scanResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                // Navegar para tela de detalhes da NFC-e
                val bundle = Bundle().apply {
                    putString("chaveAcesso", it)
                }
                try {
                    findNavController().navigate(
                        com.example.nfc.R.id.action_scanner_to_invoice_detail,
                        bundle
                    )
                } catch (e: Exception) {
                    // Fallback navigation
                    findNavController().navigate(
                        com.example.nfc.R.id.action_scanner_to_invoice_detail,
                        Bundle().apply { putString("chaveAcesso", it) }
                    )
                }
                viewModel.clearScanResult()
            }
        }
        
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
        
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.validationMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnFlash.setOnClickListener {
            toggleFlash()
        }
        
        binding.btnGallery.setOnClickListener {
            // TODO: Implementar seleção de imagem da galeria
            Toast.makeText(requireContext(), "Funcionalidade em desenvolvimento", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun requestCameraPermission() {
        PermissionUtils.requestCameraPermission(
            requireContext(),
            onGranted = { startCamera() },
            onDenied = {
                Toast.makeText(
                    requireContext(),
                    "Permissão de câmera necessária para escanear códigos",
                    Toast.LENGTH_LONG
                ).show()
                findNavController().navigateUp()
            }
        )
    }
    
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }
    
    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: return
        
        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(binding.previewView.surfaceProvider)
        
        imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { barcodes ->
                    processBarcodes(barcodes)
                })
            }
        
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalysis
            )
        } catch (exc: Exception) {
            Toast.makeText(requireContext(), "Erro ao iniciar câmera", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun processBarcodes(barcodes: List<Barcode>) {
        for (barcode in barcodes) {
            barcode.rawValue?.let { rawValue ->
                viewModel.processScannedCode(rawValue)
                return // Processar apenas o primeiro código encontrado
            }
        }
    }
    
    private fun toggleFlash() {
        // TODO: Implementar controle do flash
        Toast.makeText(requireContext(), "Flash em desenvolvimento", Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        barcodeScanner.close()
        _binding = null
    }
}

private class BarcodeAnalyzer(
    private val onBarcodeDetected: (List<Barcode>) -> Unit
) : ImageAnalysis.Analyzer {
    
    private val scanner = BarcodeScanning.getClient()
    
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    onBarcodeDetected(barcodes)
                }
                .addOnFailureListener {
                    // Handle failure
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
