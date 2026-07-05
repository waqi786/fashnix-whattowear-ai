package com.fashnix.app.domain

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.min

/**
 * FashnixClassifier: The Neural Heart of the App.
 * Optimized for multi-head classification with high precision.
 */
class FashnixClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null
    
    private var categoryLabels: List<String> = emptyList()
    private var colourLabels: List<String> = emptyList()
    private var occasionLabels: List<String> = emptyList()
    private var genderLabels: List<String> = emptyList()
    private var outputHeads: Map<Int, String> = emptyMap()

    private val TAG = "Fashnix_AI"

    init {
        initializeEngine()
    }

    private fun initializeEngine() {
        try {
            val modelBuffer: MappedByteBuffer = loadModelFile("model.tflite")
            val options = Interpreter.Options().apply {
                val compatList = CompatibilityList()
                if (compatList.isDelegateSupportedOnThisDevice) {
                    gpuDelegate = GpuDelegate(compatList.bestOptionsForThisDevice)
                    addDelegate(gpuDelegate)
                } else {
                    setNumThreads(4)
                }
            }
            interpreter = try {
                Interpreter(modelBuffer, options)
            } catch (gpuFailure: Exception) {
                Log.w(TAG, "GPU delegate failed. Falling back to CPU inference.", gpuFailure)
                gpuDelegate?.close()
                gpuDelegate = null
                Interpreter(modelBuffer, Interpreter.Options().apply { setNumThreads(4) })
            }
            loadLabels()
            mapOutputHeads()
            Log.i(TAG, "Neural Engine operational.")
        } catch (e: Exception) {
            Log.e(TAG, "Engine init failure", e)
        }
    }

    private fun loadLabels() {
        try {
            // Updated to ensure labels are trimmed and cleaned
            categoryLabels = FileUtil.loadLabels(context, "category_labels.txt").map { it.trim() }
            colourLabels = FileUtil.loadLabels(context, "colour_labels.txt").map { it.trim() }
            occasionLabels = FileUtil.loadLabels(context, "occasion_labels.txt").map { it.trim() }
            genderLabels = FileUtil.loadLabels(context, "gender_labels.txt").map { it.trim() }
        } catch (e: Exception) {
            Log.e(TAG, "Label load failure", e)
        }
    }

    private fun mapOutputHeads() {
        val interp = interpreter ?: return
        val mapped = mutableMapOf<Int, String>()
        val usedHeads = mutableSetOf<String>()

        for (index in 0 until interp.outputTensorCount) {
            val tensor = interp.getOutputTensor(index)
            val name = tensor.name()
            val classCount = tensor.shape().lastOrNull() ?: 0
            val head = resolveHead(name, classCount, usedHeads)
            mapped[index] = head
            usedHeads.add(head)
            Log.i(TAG, "Output[$index] name=$name shape=${tensor.shape().contentToString()} mapped=$head")
        }

        outputHeads = mapped
    }

    private fun resolveHead(name: String, classCount: Int, usedHeads: Set<String>): String {
        val normalized = name.lowercase()
        return when {
            normalized.contains("category") -> "category"
            normalized.contains("colour") || normalized.contains("color") -> "colour"
            normalized.contains("occasion") -> "occasion"
            normalized.contains("gender") -> "gender"
            classCount == categoryLabels.size && "category" !in usedHeads -> "category"
            classCount == occasionLabels.size && "occasion" !in usedHeads -> "occasion"
            classCount == colourLabels.size && "colour" !in usedHeads -> "colour"
            classCount == genderLabels.size && "gender" !in usedHeads -> "gender"
            else -> "unknown_$classCount"
        }
    }

    private fun loadModelFile(modelPath: String): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        return inputStream.channel.map(
            FileChannel.MapMode.READ_ONLY,
            assetFileDescriptor.startOffset,
            assetFileDescriptor.declaredLength
        )
    }

    suspend fun classify(bitmap: Bitmap): ClassificationResult = withContext(Dispatchers.Default) {
        val interp = interpreter ?: return@withContext errorResult()

        // Elite Image Processing
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0.0f, 255.0f)) 
            .build()
        
        val squareBitmap = centerCropSquare(bitmap)
        val inputBitmap = if (squareBitmap.config == Bitmap.Config.ARGB_8888) {
            squareBitmap
        } else {
            squareBitmap.copy(Bitmap.Config.ARGB_8888, false)
        }
        val tensorImage = TensorImage.fromBitmap(inputBitmap)
        val processedImage = imageProcessor.process(tensorImage)
        
        val outputs = mutableMapOf<Int, Any>()
        val buffersByHead = mutableMapOf<String, Array<FloatArray>>()

        for (index in 0 until interp.outputTensorCount) {
            val classCount = interp.getOutputTensor(index).shape().lastOrNull() ?: 0
            val buffer = Array(1) { FloatArray(classCount) }
            outputs[index] = buffer
            outputHeads[index]?.let { head -> buffersByHead[head] = buffer }
        }

        try {
            interp.runForMultipleInputsOutputs(arrayOf(processedImage.buffer), outputs)
        } catch (e: Exception) {
            Log.e(TAG, "Inference error", e)
            return@withContext errorResult()
        }

        val categoryRes = getTopResult(buffersByHead["category"]?.get(0), categoryLabels)
        val colourRes = getTopResult(buffersByHead["colour"]?.get(0), colourLabels)
        val occasionRes = getTopResult(buffersByHead["occasion"]?.get(0), occasionLabels)
        val genderRes = getTopResult(buffersByHead["gender"]?.get(0), genderLabels)

        ClassificationResult(
            category = categoryRes.first,
            categoryConfidence = categoryRes.second,
            colour = colourRes.first,
            colourConfidence = colourRes.second,
            isColourOther = false,
            occasion = occasionRes.first,
            occasionConfidence = occasionRes.second,
            gender = genderRes.first,
            genderConfidence = genderRes.second
        )
    }

    private fun getTopResult(probs: FloatArray?, labels: List<String>): Pair<String, Float> {
        if (probs == null || probs.isEmpty() || labels.isEmpty()) return Pair("Undetermined", 0f)
        val scores = normalizeScores(probs)
        var maxIdx = 0
        var maxVal = -1f
        for (i in scores.indices) {
            if (scores[i] > maxVal) {
                maxVal = scores[i]
                maxIdx = i
            }
        }
        return Pair(labels.getOrElse(maxIdx) { "Unknown" }, maxVal)
    }

    private fun normalizeScores(values: FloatArray): FloatArray {
        val sum = values.sum()
        val looksLikeProbabilities = values.all { it in 0f..1f } && sum in 0.85f..1.15f
        if (looksLikeProbabilities) return values

        val max = values.maxOrNull() ?: 0f
        val expValues = values.map { kotlin.math.exp((it - max).toDouble()).toFloat() }
        val expSum = expValues.sum().takeIf { it > 0f } ?: return values
        return FloatArray(values.size) { index -> expValues[index] / expSum }
    }

    private fun centerCropSquare(bitmap: Bitmap): Bitmap {
        val side = min(bitmap.width, bitmap.height)
        if (side <= 0) return bitmap
        val left = (bitmap.width - side) / 2
        val top = (bitmap.height - side) / 2
        return Bitmap.createBitmap(bitmap, left, top, side, side)
    }

    private fun errorResult() = ClassificationResult("Engine Error", 0f, "Unknown", 0f, false, "Unknown", 0f, "Unknown", 0f)

    fun close() {
        interpreter?.close()
        gpuDelegate?.close()
    }
}
