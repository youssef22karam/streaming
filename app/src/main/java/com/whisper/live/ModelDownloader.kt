package com.whisper.live

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ModelDownloader {

    enum class EngineType { VOSK, SHERPA_ONLINE, SHERPA_OFFLINE }
    enum class ArchiveType { ZIP, TAR_BZ2 }

    data class LanguageOption(
        val code: String,
        val name: String,
        val flag: String,
    )

    data class ModelInfo(
        val id: String,
        val displayName: String,
        val langCode: String,
        val supportedLangCodes: Set<String> = emptySet(),
        val url: String,
        val dirName: String,
        val sizeMb: Int,
        val engineType: EngineType,
        val modelType: String,
        val archiveType: ArchiveType,
        val sherpaPresetType: Int? = null,
    )

    private const val VOSK_BASE = "https://alphacephei.com/vosk/models"
    private const val SHERPA_RELEASE_BASE =
        "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models"

    val languageOptions: List<LanguageOption> = listOf(
        LanguageOption("en", "English", "US"),
        LanguageOption("ar", "Arabic", "SA"),
        LanguageOption("bn", "Bengali", "BD"),
        LanguageOption("de", "German", "DE"),
        LanguageOption("es", "Spanish", "ES"),
        LanguageOption("fr", "French", "FR"),
        LanguageOption("id", "Indonesian", "ID"),
        LanguageOption("ja", "Japanese", "JP"),
        LanguageOption("ko", "Korean", "KR"),
        LanguageOption("ru", "Russian", "RU"),
        LanguageOption("th", "Thai", "TH"),
        LanguageOption("uk", "Ukrainian", "UA"),
        LanguageOption("vi", "Vietnamese", "VN"),
        LanguageOption("zh", "Chinese", "CN"),
        LanguageOption("multi", "Multilingual", "GL"),
        LanguageOption("all", "All Languages", "ALL"),
    )

    val models: List<ModelInfo> = buildList {
        // Streaming + Moonshine model catalog.
        // Source: https://github.com/k2-fsa/sherpa-onnx/releases/tag/asr-models
        add(sherpaOnline(
            id = "sh_stream_multi_8lang",
            title = "Streaming Zipformer Multi (AR/EN/ID/JA/RU/TH/VI/ZH)",
            lang = "multi",
            supportedLangCodes = setOf("ar", "en", "id", "ja", "ru", "th", "vi", "zh"),
            dir = "sherpa-onnx-streaming-zipformer-ar_en_id_ja_ru_th_vi_zh-2025-02-10",
            size = 247,
            presetType = null,
            type = "transducer",
        ))
        add(sherpaOnline(
            id = "sh_stream_en_kroko",
            title = "Streaming Zipformer EN Kroko",
            lang = "en",
            dir = "sherpa-onnx-streaming-zipformer-en-kroko-2025-08-06",
            size = 55,
            presetType = 21,
            type = "transducer",
        ))
        add(sherpaOnline(
            id = "sh_stream_en_nemo_80ms",
            title = "NeMo Streaming Fast Conformer EN 80ms",
            lang = "en",
            dir = "sherpa-onnx-nemo-streaming-fast-conformer-ctc-en-80ms",
            size = 140,
            presetType = 11,
            type = "nemo_ctc",
        ))
        add(sherpaOnline(
            id = "sh_stream_en_nemotron",
            title = "Nemotron Streaming EN 0.6B (high accuracy)",
            lang = "en",
            dir = "sherpa-onnx-nemotron-speech-streaming-en-0.6b-int8-2026-01-14",
            size = 447,
            presetType = 28,
            type = "transducer",
        ))
        add(sherpaOnline(
            id = "sh_stream_de_kroko",
            title = "Streaming Zipformer DE Kroko",
            lang = "de",
            dir = "sherpa-onnx-streaming-zipformer-de-kroko-2025-08-06",
            size = 55,
            presetType = 24,
            type = "transducer",
        ))
        add(sherpaOnline(
            id = "sh_stream_es_kroko",
            title = "Streaming Zipformer ES Kroko",
            lang = "es",
            dir = "sherpa-onnx-streaming-zipformer-es-kroko-2025-08-06",
            size = 119,
            presetType = 22,
            type = "transducer",
        ))
        add(sherpaOnline(
            id = "sh_stream_fr_kroko",
            title = "Streaming Zipformer FR Kroko",
            lang = "fr",
            dir = "sherpa-onnx-streaming-zipformer-fr-kroko-2025-08-06",
            size = 55,
            presetType = 23,
            type = "transducer",
        ))
        add(sherpaOnline(
            id = "sh_stream_bn_vosk",
            title = "Streaming Zipformer Bengali",
            lang = "bn",
            dir = "sherpa-onnx-streaming-zipformer-bn-vosk-2026-02-09",
            size = 83,
            presetType = 29,
            type = "transducer",
        ))
        add(sherpaOnline(
            id = "sh_stream_ko_zipformer",
            title = "Streaming Zipformer Korean",
            lang = "ko",
            dir = "sherpa-onnx-streaming-zipformer-korean-2024-06-16",
            size = 399,
            presetType = 14,
            type = "transducer",
        ))
        add(sherpaOnline(
            id = "sh_stream_ru_small",
            title = "Streaming Zipformer RU (small int8)",
            lang = "ru",
            dir = "sherpa-onnx-streaming-zipformer-small-ru-vosk-int8-2025-08-16",
            size = 23,
            presetType = 25,
            type = "transducer",
        ))
        add(sherpaOnline(
            id = "sh_stream_ru_tone",
            title = "Streaming T-one Russian",
            lang = "ru",
            dir = "sherpa-onnx-streaming-t-one-russian-2025-09-08",
            size = 123,
            presetType = null,
            type = "ctc",
        ))
        add(sherpaOnline(
            id = "sh_stream_zh_small_ctc",
            title = "Streaming Zipformer Small CTC ZH (int8)",
            lang = "zh",
            dir = "sherpa-onnx-streaming-zipformer-small-ctc-zh-int8-2025-04-01",
            size = 20,
            presetType = 15,
            type = "ctc",
        ))
        add(sherpaOnline(
            id = "sh_stream_zh_ctc",
            title = "Streaming Zipformer CTC ZH (int8)",
            lang = "zh",
            dir = "sherpa-onnx-streaming-zipformer-ctc-zh-int8-2025-06-30",
            size = 122,
            presetType = 17,
            type = "ctc",
        ))
        add(sherpaOnline(
            id = "sh_stream_zh_xlarge_ctc",
            title = "Streaming Zipformer CTC ZH xlarge (int8)",
            lang = "zh",
            dir = "sherpa-onnx-streaming-zipformer-ctc-zh-xlarge-int8-2025-06-30",
            size = 563,
            presetType = null,
            type = "ctc",
        ))

        // Moonshine models (offline).
        add(sherpaOffline(
            id = "sh_moon_base_ar_q",
            title = "Moonshine Base Arabic (quantized)",
            lang = "ar",
            dir = "sherpa-onnx-moonshine-base-ar-quantized-2026-02-27",
            size = 114,
            presetType = null,
            type = "moonshine",
        ))
        add(sherpaOffline(
            id = "sh_moon_base_en_q",
            title = "Moonshine Base English (quantized)",
            lang = "en",
            dir = "sherpa-onnx-moonshine-base-en-quantized-2026-02-27",
            size = 106,
            presetType = null,
            type = "moonshine",
        ))
        add(sherpaOffline(
            id = "sh_moon_tiny_en_q",
            title = "Moonshine Tiny English (quantized)",
            lang = "en",
            dir = "sherpa-onnx-moonshine-tiny-en-quantized-2026-02-27",
            size = 29,
            presetType = null,
            type = "moonshine",
        ))
        add(sherpaOffline(
            id = "sh_moon_base_es_q",
            title = "Moonshine Base Spanish (quantized)",
            lang = "es",
            dir = "sherpa-onnx-moonshine-base-es-quantized-2026-02-27",
            size = 49,
            presetType = null,
            type = "moonshine",
        ))
        add(sherpaOffline(
            id = "sh_moon_base_ja_q",
            title = "Moonshine Base Japanese (quantized)",
            lang = "ja",
            dir = "sherpa-onnx-moonshine-base-ja-quantized-2026-02-27",
            size = 99,
            presetType = null,
            type = "moonshine",
        ))
        add(sherpaOffline(
            id = "sh_moon_tiny_ja_q",
            title = "Moonshine Tiny Japanese (quantized)",
            lang = "ja",
            dir = "sherpa-onnx-moonshine-tiny-ja-quantized-2026-02-27",
            size = 46,
            presetType = null,
            type = "moonshine",
        ))
        add(sherpaOffline(
            id = "sh_moon_tiny_ko_q",
            title = "Moonshine Tiny Korean (quantized)",
            lang = "ko",
            dir = "sherpa-onnx-moonshine-tiny-ko-quantized-2026-02-27",
            size = 47,
            presetType = null,
            type = "moonshine",
        ))
        add(sherpaOffline(
            id = "sh_moon_base_uk_q",
            title = "Moonshine Base Ukrainian (quantized)",
            lang = "uk",
            dir = "sherpa-onnx-moonshine-base-uk-quantized-2026-02-27",
            size = 106,
            presetType = null,
            type = "moonshine",
        ))
        add(sherpaOffline(
            id = "sh_moon_base_vi_q",
            title = "Moonshine Base Vietnamese (quantized)",
            lang = "vi",
            dir = "sherpa-onnx-moonshine-base-vi-quantized-2026-02-27",
            size = 102,
            presetType = null,
            type = "moonshine",
        ))
        add(sherpaOffline(
            id = "sh_moon_base_zh_q",
            title = "Moonshine Base Chinese (quantized)",
            lang = "zh",
            dir = "sherpa-onnx-moonshine-base-zh-quantized-2026-02-27",
            size = 95,
            presetType = null,
            type = "moonshine",
        ))
    }

    fun languageByCode(code: String): LanguageOption {
        val normalized = code.lowercase(Locale.US)
        return languageOptions.firstOrNull { it.code == normalized }
            ?: LanguageOption(normalized, normalized.uppercase(Locale.US), normalized.uppercase(Locale.US))
    }

    fun modelsForLanguage(langCode: String): List<ModelInfo> {
        val selected = langCode.lowercase(Locale.US)
        if (selected == "all") return models
        return models.filter { modelSupportsLanguage(it, selected) }
    }

    fun groupByEngine(models: List<ModelInfo>): Map<EngineType, List<ModelInfo>> =
        models.groupBy { it.engineType }

    fun modelSupportsLanguage(model: ModelInfo, selectedLanguage: String): Boolean {
        val selected = selectedLanguage.lowercase(Locale.US)
        val modelLangs = expandedModelLangCodes(model)

        return when (selected) {
            "all" -> true
            "multi" -> modelLangs.size > 1 || model.langCode.lowercase(Locale.US) == "multi"
            else -> modelLangs.contains(selected)
        }
    }

    private fun expandedModelLangCodes(model: ModelInfo): Set<String> {
        if (model.supportedLangCodes.isNotEmpty()) {
            return model.supportedLangCodes.map { it.lowercase(Locale.US) }.toSet()
        }

        val code = model.langCode.lowercase(Locale.US)
        if (!code.contains('-')) return setOf(code)
        return code.split('-').map { it.trim() }.filter { it.isNotBlank() }.toSet()
    }

    fun download(
        model: ModelInfo,
        filesDir: File,
        onProgress: (Int) -> Unit,
        onComplete: (File) -> Unit,
        onError: (String) -> Unit,
    ) {
        val alreadyDownloadedDir = findExistingModelDir(model, filesDir)
        if (alreadyDownloadedDir != null) {
            onComplete(alreadyDownloadedDir)
            return
        }
        val expectedDir = File(filesDir, model.dirName)

        val archiveExt = when (model.archiveType) {
            ArchiveType.ZIP -> ".zip"
            ArchiveType.TAR_BZ2 -> ".tar.bz2"
        }

        val archiveFile = File(filesDir, "${model.dirName}$archiveExt")
        val partialFile = File(filesDir, "${archiveFile.name}.part")

        val existingDirs = filesDir.listFiles()?.filter { it.isDirectory }?.map { it.name }?.toSet() ?: emptySet()

        val client = OkHttpClient.Builder().followRedirects(true).build()
        val resumeFrom = if (partialFile.exists()) partialFile.length() else 0L

        val reqBuilder = Request.Builder().url(model.url)
        if (resumeFrom > 0L) {
            reqBuilder.addHeader("Range", "bytes=$resumeFrom-")
        }
        val request = reqBuilder.build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError(e.message ?: "Network error")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code != 200 && response.code != 206) {
                    onError("HTTP ${response.code}")
                    return
                }

                val body = response.body ?: run {
                    onError("Empty response")
                    return
                }

                try {
                    // 206 => resume append is active
                    // 200 with Range requested => server ignored Range, restart from zero
                    val append = resumeFrom > 0L && response.code == 206
                    var downloaded = if (append) resumeFrom else 0L
                    if (!append && partialFile.exists()) {
                        partialFile.delete()
                    }

                    val bodyLen = body.contentLength()
                    val totalLen = if (bodyLen > 0L) downloaded + bodyLen else -1L
                    if (totalLen > 0L) {
                        onProgress(((downloaded * 100L) / totalLen).toInt().coerceIn(0, 100))
                    }

                    FileOutputStream(partialFile, append).use { out ->
                        body.byteStream().use { input ->
                            val buf = ByteArray(32 * 1024)
                            var n: Int
                            while (input.read(buf).also { n = it } != -1) {
                                out.write(buf, 0, n)
                                downloaded += n
                                if (totalLen > 0L) {
                                    onProgress(((downloaded * 100L) / totalLen).toInt().coerceIn(0, 100))
                                }
                            }
                        }
                    }

                    if (archiveFile.exists()) archiveFile.delete()
                    if (!partialFile.renameTo(archiveFile)) {
                        throw IOException("Failed to finalize download file")
                    }

                    when (model.archiveType) {
                        ArchiveType.ZIP -> extractZip(archiveFile, filesDir)
                        ArchiveType.TAR_BZ2 -> extractTarBz2(archiveFile, filesDir)
                    }

                    archiveFile.delete()

                    val extracted = resolveExtractedDir(filesDir, model.dirName, existingDirs)
                    if (extracted.isDirectory) {
                        val finalDir = normalizeExtractedModelDir(expectedDir, extracted)
                        onComplete(finalDir)
                    } else {
                        onError("Extraction finished but model folder was not found")
                    }
                } catch (e: Exception) {
                    // Keep *.part so next download attempt can resume.
                    onError(e.message ?: "Download error")
                }
            }
        })
    }

    fun importFromZip(
        inputStream: InputStream,
        filesDir: File,
        onProgress: (Int) -> Unit,
        onComplete: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        Thread {
            try {
                val tmp = File(filesDir, "import_tmp_${System.currentTimeMillis()}.zip")
                FileOutputStream(tmp).use { inputStream.copyTo(it) }
                onProgress(30)

                var modelName: String? = null
                ZipInputStream(tmp.inputStream()).use { zis ->
                    val entry = zis.nextEntry
                    if (entry != null) {
                        modelName = entry.name.split('/')[0]
                    }
                }

                if (modelName.isNullOrBlank()) {
                    tmp.delete()
                    onError("Invalid ZIP: could not detect model name")
                    return@Thread
                }

                val destDir = File(filesDir, modelName!!)
                if (destDir.exists()) destDir.deleteRecursively()

                onProgress(55)
                extractZip(tmp, filesDir)
                onProgress(95)
                tmp.delete()

                if (destDir.isDirectory) onComplete(modelName!!)
                else onError("Extraction failed")
            } catch (e: Exception) {
                onError(e.message ?: "Import error")
            }
        }.start()
    }

    fun exportToZip(
        model: ModelInfo,
        filesDir: File,
        outputStream: java.io.OutputStream,
        onComplete: () -> Unit,
        onError: (String) -> Unit,
    ) {
        Thread {
            try {
                val modelDir = getModelPath(model, filesDir)
                if (!modelDir.isDirectory) {
                    onError("Model not found")
                    return@Thread
                }

                ZipOutputStream(outputStream.buffered()).use { zos ->
                    modelDir.walkTopDown().forEach { file ->
                        if (file.isFile) {
                            val rel = file.relativeTo(modelDir).path.replace('\\', '/')
                            val entryName = "${model.dirName}/$rel"
                            zos.putNextEntry(ZipEntry(entryName))
                            file.inputStream().use { it.copyTo(zos) }
                            zos.closeEntry()
                        }
                    }
                }
                onComplete()
            } catch (e: Exception) {
                onError(e.message ?: "Export error")
            }
        }.start()
    }

    fun getModelPath(model: ModelInfo, filesDir: File): File =
        findExistingModelDir(model, filesDir) ?: File(filesDir, model.dirName)

    fun isDownloaded(model: ModelInfo, filesDir: File): Boolean =
        findExistingModelDir(model, filesDir) != null

    fun matchByFolder(folderName: String): ModelInfo? =
        models.firstOrNull { it.dirName == folderName }

    private fun resolveExtractedDir(
        filesDir: File,
        expectedDirName: String,
        existingDirs: Set<String>,
    ): File {
        val expected = File(filesDir, expectedDirName)
        if (expected.isDirectory) return expected

        val newDirs = filesDir.listFiles()
            ?.filter { it.isDirectory && it.name !in existingDirs }
            .orEmpty()

        newDirs.firstOrNull { it.name == expectedDirName }?.let { return it }
        newDirs.firstOrNull { it.name.startsWith(expectedDirName) }?.let { return it }
        if (newDirs.size == 1) return newDirs.first()

        return expected
    }

    private fun findExistingModelDir(model: ModelInfo, filesDir: File): File? {
        val expected = File(filesDir, model.dirName)
        if (expected.isDirectory) return expected

        val dirs = filesDir.listFiles()?.filter { it.isDirectory }.orEmpty()
        dirs.firstOrNull { it.name.equals(model.dirName, ignoreCase = true) }?.let { return it }
        dirs.firstOrNull { it.name.startsWith(model.dirName, ignoreCase = true) }?.let { return it }
        dirs.firstOrNull { model.dirName.startsWith(it.name, ignoreCase = true) }?.let { return it }
        return null
    }

    private fun normalizeExtractedModelDir(expectedDir: File, extractedDir: File): File {
        if (expectedDir.canonicalPath == extractedDir.canonicalPath) {
            return expectedDir
        }

        if (expectedDir.exists()) {
            expectedDir.deleteRecursively()
        }
        expectedDir.parentFile?.mkdirs()

        if (extractedDir.renameTo(expectedDir)) {
            return expectedDir
        }

        // Fallback when rename fails across boundaries: copy then delete source.
        extractedDir.copyRecursively(expectedDir, overwrite = true)
        extractedDir.deleteRecursively()
        return expectedDir
    }

    private fun extractZip(zipFile: File, destParentDir: File) {
        ZipInputStream(zipFile.inputStream().buffered()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val target = safeTarget(destParentDir, entry.name)
                if (entry.isDirectory) {
                    target.mkdirs()
                } else {
                    target.parentFile?.mkdirs()
                    FileOutputStream(target).use { zis.copyTo(it) }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }

    private fun extractTarBz2(archiveFile: File, destParentDir: File) {
        archiveFile.inputStream().buffered().use { fis ->
            BZip2CompressorInputStream(fis).use { bzip ->
                TarArchiveInputStream(bzip).use { tar ->
                    var entry: TarArchiveEntry? = tar.nextTarEntry
                    while (entry != null) {
                        val target = safeTarget(destParentDir, entry.name)
                        if (entry.isDirectory) {
                            target.mkdirs()
                        } else {
                            target.parentFile?.mkdirs()
                            FileOutputStream(target).use { out -> tar.copyTo(out) }
                        }
                        entry = tar.nextTarEntry
                    }
                }
            }
        }
    }

    private fun safeTarget(destParentDir: File, entryName: String): File {
        val target = File(destParentDir, entryName)
        val destPath = destParentDir.canonicalPath + File.separator
        val targetPath = target.canonicalPath
        if (!targetPath.startsWith(destPath)) {
            throw SecurityException("Blocked archive path traversal: $entryName")
        }
        return target
    }

    private fun vosk(
        id: String,
        title: String,
        lang: String,
        dir: String,
        size: Int,
    ): ModelInfo {
        return ModelInfo(
            id = id,
            displayName = title,
            langCode = lang,
            url = "$VOSK_BASE/$dir.zip",
            dirName = dir,
            sizeMb = size,
            engineType = EngineType.VOSK,
            modelType = "vosk",
            archiveType = ArchiveType.ZIP,
            sherpaPresetType = null,
        )
    }

    private fun sherpaOnline(
        id: String,
        title: String,
        lang: String,
        supportedLangCodes: Set<String> = emptySet(),
        dir: String,
        size: Int,
        presetType: Int?,
        type: String,
    ): ModelInfo {
        return ModelInfo(
            id = id,
            displayName = title,
            langCode = lang,
            supportedLangCodes = supportedLangCodes,
            url = "$SHERPA_RELEASE_BASE/$dir.tar.bz2",
            dirName = dir,
            sizeMb = size,
            engineType = EngineType.SHERPA_ONLINE,
            modelType = type,
            archiveType = ArchiveType.TAR_BZ2,
            sherpaPresetType = presetType,
        )
    }

    private fun sherpaOffline(
        id: String,
        title: String,
        lang: String,
        dir: String,
        size: Int,
        presetType: Int?,
        type: String,
    ): ModelInfo {
        return ModelInfo(
            id = id,
            displayName = title,
            langCode = lang,
            url = "$SHERPA_RELEASE_BASE/$dir.tar.bz2",
            dirName = dir,
            sizeMb = size,
            engineType = EngineType.SHERPA_OFFLINE,
            modelType = type,
            archiveType = ArchiveType.TAR_BZ2,
            sherpaPresetType = presetType,
        )
    }
}
