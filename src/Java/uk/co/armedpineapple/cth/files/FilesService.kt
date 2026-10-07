package uk.co.armedpineapple.cth.files

import android.content.Context
import android.os.storage.StorageManager
import com.lazygeniouz.dfc.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.withContext
import uk.co.armedpineapple.cth.GameConfiguration
import uk.co.armedpineapple.cth.Loggable
import uk.co.armedpineapple.cth.warn
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipFile

/**
 * Files service for CorsixTH engine assets and Theme Hospital proprietary data.
 *
 * Engine data (`game.zip`) and Theme Hospital data are kept separate:
 * - engine → [GameConfiguration.cthFiles]
 * - Theme Hospital → [GameConfiguration.thFiles]
 */
class FilesService(val ctx: Context) : Loggable {

    private val storageManager: StorageManager = ctx.getSystemService(StorageManager::class.java)

    data class DeterminateFileOperationProgress(val progress: Long, val max: Long)
    data class EstimatedFileOperationProgress(val progress: Float)

    /**
     * Gets all the save game files.
     *
     * @param config The configuration
     * @return An array of save game files
     */
    fun getSaveGameFiles(config: GameConfiguration): Array<out File> {
        return getSaveDirectoryContents(config.saveFiles)
    }

    /**
     * Gets all the autosave game files.
     *
     * @param config The configuration
     * @return An array of save game files
     */
    fun getAutoSaveGameFiles(config: GameConfiguration): Array<out File> {
        return getSaveDirectoryContents(config.autosaveFiles)
    }

    /**
     * Checks whether a usable CorsixTH engine payload is installed.
     *
     * Marker files cover both 0.70.1 essentials and Android-specific Lua/UI assets.
     */
    fun hasGameFiles(config: GameConfiguration): Boolean {
        return ENGINE_MARKER_FILES.all { relative ->
            File(config.cthFiles, relative).isFile
        }
    }

    /**
     * Whether the installed engine payload should be replaced from `game.zip`.
     *
     * Uses marker presence plus [EXPECTED_ENGINE_API_VERSION] from `Lua/api_version.lua`.
     */
    fun needsEngineDataUpgrade(config: GameConfiguration): Boolean {
        if (!hasGameFiles(config)) {
            return true
        }
        val installedApi = readInstalledApiVersion(config) ?: return true
        if (installedApi != EXPECTED_ENGINE_API_VERSION) {
            warn {
                "Engine API $installedApi != expected $EXPECTED_ENGINE_API_VERSION; upgrade required"
            }
            return true
        }
        return false
    }

    /**
     * Gets whether the demo game files are installed.
     *
     * @param config The configuration
     * @return Whether the demo game files are installed.
     */
    fun isDemoVersion(config: GameConfiguration): Boolean {
        return File(config.thFiles, "DATAM/DEMO.DAT").exists()
    }

    /**
     * Checks whether the original TH files are installed in the location given in the config.
     *
     * This doesn't do a thorough integrity check and is only indicative of missing files.
     *
     * @param config The configuration
     * @return whether the original TH files are installed
     */
    fun hasOriginalFiles(config: GameConfiguration): Boolean {
        return hasThemeHospitalLayout(config.thFiles)
    }

    /**
     * True when [root] looks like a Theme Hospital install (full or demo).
     */
    fun hasThemeHospitalLayout(root: File): Boolean {
        return TH_MARKER_FILES.all { relative -> File(root, relative).isFile }
    }

    /**
     * True when a SAF document tree looks like Theme Hospital data.
     */
    fun hasThemeHospitalLayout(root: DocumentFileCompat): Boolean {
        return TH_MARKER_FILES.all { relative -> documentHasRelativeFile(root, relative) }
    }

    /**
     * Resolves the Theme Hospital root inside a SAF tree.
     *
     * Accepts the selected folder itself, or a single nesting level used by some GOG/folder layouts.
     */
    fun resolveThemeHospitalDocumentRoot(root: DocumentFileCompat): DocumentFileCompat? {
        if (hasThemeHospitalLayout(root)) {
            return root
        }
        root.listFiles().forEach { child ->
            if (child.isDirectory() && hasThemeHospitalLayout(child)) {
                return child
            }
        }
        return null
    }

    /**
     * Checks whether the music library is installed in the location given in the config.
     *
     * This doesn't do a thorough integrity check and is only indicative of missing files.
     *
     * @param config The configuration
     * @return whether the music library is installed
     */
    fun hasMusicLibrary(config: GameConfiguration): Boolean {
        return File(config.musicLib, "timidity.cfg").exists()
    }

    /**
     * Installs the CorsixTH game files.
     *
     * @param config The configuration that determines the installation location.
     * @param progress An optional channel to send progress updates to.
     */
    suspend fun installGameFiles(
        config: GameConfiguration, progress: SendChannel<DeterminateFileOperationProgress>? = null
    ) {
        val assetOut = File(ctx.cacheDir, ENGINE_ZIP_FILE)
        copyAsset(ENGINE_ZIP_FILE, ctx, assetOut)

        try {
            val target = config.cthFiles
            extractZipFile(assetOut, target, progress)
            writeEngineStamp(config)
        } finally {
            assetOut.delete()
        }
    }

    /**
     * Installs the music library.
     *
     * @param config The configuration that determines the installation location.
     * @param progress An optional channel to send progress updates to.
     */
    suspend fun installMusicLibrary(
        config: GameConfiguration, progress: SendChannel<DeterminateFileOperationProgress>? = null
    ) {
        val assetOut = File(ctx.cacheDir, MUSIC_ZIP_FILE)
        copyAsset(MUSIC_ZIP_FILE, ctx, assetOut)

        try {
            val target = config.musicLib
            extractZipFile(assetOut, target, progress)
        } finally {
            assetOut.delete()
        }
    }

    /**
     * Removes the original game files entirely.
     *
     * @param config The configuration the determines the installation location.
     */
    fun nukeOriginalFiles(config: GameConfiguration) {
        val target = config.thFiles
        if (target.exists()) target.deleteRecursively()
    }

    /**
     * Installs the original game files.
     *
     * @param source The source document tree (already resolved to Theme Hospital root)
     * @param config The configuration that determines the installation location.
     * @param progress An optional channel to send progress updates to.
     */
    suspend fun installOriginalFiles(
        source: DocumentFileCompat,
        config: GameConfiguration,
        progress: SendChannel<EstimatedFileOperationProgress>? = null
    ) {
        nukeOriginalFiles(config)
        copyDirectoryTree(source, config.thFiles, progress)
    }

    /**
     * After a zip extract into [GameConfiguration.thFiles], promote a nested Theme Hospital
     * root (common with demo zips) so markers sit directly under `themehospital/`.
     *
     * @return true if Theme Hospital markers are present after normalization
     */
    fun normalizeThemeHospitalInstall(config: GameConfiguration): Boolean {
        if (hasOriginalFiles(config)) {
            return true
        }

        val root = config.thFiles
        if (!root.isDirectory) {
            return false
        }

        val nested = root.listFiles()
            ?.firstOrNull { it.isDirectory && hasThemeHospitalLayout(it) }
            ?: return false

        val staging = File(root.parentFile, "${root.name}.staging")
        if (staging.exists()) {
            staging.deleteRecursively()
        }
        if (!nested.renameTo(staging)) {
            return false
        }
        root.deleteRecursively()
        if (!staging.renameTo(root)) {
            // Best-effort restore if rename fails mid-flight.
            staging.renameTo(nested)
            return false
        }
        return hasOriginalFiles(config)
    }

    /**
     * Gets a File corresponding to the given save name.
     *
     * @param saveName The save name.
     * @param config The configuration that determines the save file locations.
     * @return A file for the given save name.
     */
    fun getSaveFile(saveName: String, config: GameConfiguration): File {
        return if (saveName.startsWith("Autosave")) {
            File(config.autosaveFiles, saveName)
        } else {
            File(config.saveFiles, saveName)
        }
    }

    private fun readInstalledApiVersion(config: GameConfiguration): Int? {
        val apiFile = File(config.cthFiles, "Lua/api_version.lua")
        if (!apiFile.isFile) {
            return null
        }
        val text = runCatching { apiFile.readText() }.getOrNull() ?: return null
        val match = Regex("""return\s+(\d+)\s*;""").find(text) ?: return null
        return match.groupValues[1].toIntOrNull()
    }

    private fun writeEngineStamp(config: GameConfiguration) {
        val stamp = File(config.cthFiles, ENGINE_STAMP_FILE)
        stamp.parentFile?.mkdirs()
        stamp.writeText(
            "api=$EXPECTED_ENGINE_API_VERSION\n" +
                "package=0.70.1\n"
        )
    }

    private fun documentHasRelativeFile(root: DocumentFileCompat, relativePath: String): Boolean {
        var current: DocumentFileCompat = root
        val parts = relativePath.split('/')
        for ((index, part) in parts.withIndex()) {
            val next = current.listFiles().firstOrNull {
                it.name.equals(part, ignoreCase = true)
            } ?: return false
            if (index == parts.lastIndex) {
                return !next.isDirectory()
            }
            if (!next.isDirectory()) {
                return false
            }
            current = next
        }
        return false
    }

    private suspend fun copyDirectoryTree(
        root: DocumentFileCompat,
        destinationDirectory: File,
        progress: SendChannel<EstimatedFileOperationProgress>? = null
    ) {
        // Create the destination directory if it doesn't exist
        destinationDirectory.mkdirs()

        if (root.isDirectory()) {
            val contents = root.listFiles()
            val totalContents = contents.size
            var currentFile = 0
            contents.forEach { file ->
                if (file.isDirectory()) {
                    val newDestination = File(destinationDirectory, file.name)
                    val childProgress =
                        if (progress == null) null else Channel<EstimatedFileOperationProgress>(
                            Channel.CONFLATED
                        ) { p ->
                            // Report the progress back up the chain.
                            progress.trySend(EstimatedFileOperationProgress((currentFile + p.progress) / totalContents.toFloat()))
                        }

                    // If child is a directory, recursively copy its contents to the new destination
                    copyDirectoryTree(file, newDestination, childProgress)
                } else {
                    // If child is a file, directly copy it to the destination directory
                    copyFile(file, destinationDirectory)
                }
                currentFile++
                progress?.trySend(EstimatedFileOperationProgress(currentFile.toFloat() / totalContents.toFloat()))
            }
        } else {
            // If the root itself is a file, copy it to the destination directory
            copyFile(root, destinationDirectory)
            progress?.trySend(EstimatedFileOperationProgress(1f))
        }
    }

    private suspend fun copyFile(
        file: DocumentFileCompat,
        destinationDirectory: File,
    ) {
        val sourceUri = file.uri
        ctx.contentResolver.openInputStream(sourceUri)?.use { input ->
            withContext(Dispatchers.IO) {
                val outputFile = File(destinationDirectory, file.name)
                copyStreamToFile(outputFile, input.available().toLong(), input)
            }
        }
    }

    suspend fun extractZipFile(
        source: File, target: File, progress: SendChannel<DeterminateFileOperationProgress>?
    ) {
        withContext(Dispatchers.IO) {
            ZipFile(source).use { zipFile ->
                val zipEntries = zipFile.entries()

                val entriesCount = zipFile.size().toLong()
                var currentEntryIndex = 0L
                while (zipEntries.hasMoreElements()) {
                    val zipEntry = zipEntries.nextElement()
                    currentEntryIndex++
                    val outputFile = File(target, zipEntry.name)
                    outputFile.parentFile?.mkdirs()

                    if (zipEntry.isDirectory) {

                        if (!outputFile.isDirectory) {
                            outputFile.mkdirs()
                        }
                    } else {
                        zipFile.getInputStream(zipEntry).use { zin ->
                            copyStreamToFile(outputFile, zipEntry.size, zin)
                        }
                    }
                    progress?.send(
                        DeterminateFileOperationProgress(
                            currentEntryIndex, entriesCount
                        )
                    )
                }
            }
        }
    }

    /**
     * Copies a stream to a file
     *
     * @param outputFile The target file
     * @param size The size of the input
     * @param input The input stream to copy
     * @param progress An optional progress channel
     */
    fun copyStreamToFile(
        outputFile: File,
        size: Long,
        input: InputStream,
        progress: SendChannel<DeterminateFileOperationProgress>? = null
    ) {
        FileOutputStream(outputFile).use { fout ->
            allocateStorage(size, fout.fd, storageManager)
            copyStreamTo(input, fout, size, progress)
        }
    }

    private fun copyStreamTo(
        input: InputStream,
        out: OutputStream,
        length: Long? = null,
        progress: SendChannel<DeterminateFileOperationProgress>? = null,
        bufferSize: Int = DEFAULT_BUFFER_SIZE
    ): Long {
        var bytesCopied: Long = 0
        val buffer = ByteArray(bufferSize)
        var bytes = input.read(buffer)
        while (bytes >= 0) {
            out.write(buffer, 0, bytes)
            bytesCopied += bytes
            length?.let { l ->
                progress?.trySend(DeterminateFileOperationProgress(bytesCopied, l))
            }
            bytes = input.read(buffer)
        }
        return bytesCopied
    }

    /**
     * Copies an asset to a file.
     *
     * @param asset The asset name
     * @param ctx The context
     * @param target The target location
     */
    fun copyAsset(
        asset: String, ctx: Context, target: File
    ) {
        ctx.assets.open(asset).use { assetInputStream ->
            target.outputStream().use { assetOutStream ->
                allocateStorage(
                    bytes = assetInputStream.available().toLong(),
                    fd = assetOutStream.fd,
                    storageMgr = storageManager
                )
                assetInputStream.copyTo(assetOutStream)
            }
        }
    }

    private fun allocateStorage(bytes: Long, fd: FileDescriptor, storageMgr: StorageManager) {
        if (storageMgr.isAllocationSupported(fd) && bytes > 0) {
            try {
                storageMgr.allocateBytes(fd, bytes)
            } catch (e: IOException) {
                warn { "Tried to allocate $bytes but failed." }
            }
        }
    }

    private fun getSaveDirectoryContents(root: File): Array<out File> {
        if (root.exists()) {
            return root.listFiles { f -> f.isFile && f.extension.lowercase() == SAVE_GAME_EXTENSION }
                ?: arrayOf()
        }
        return arrayOf()
    }

    companion object {
        private const val ENGINE_ZIP_FILE = "game.zip"
        private const val MUSIC_ZIP_FILE = "timidity.zip"
        private const val ENGINE_STAMP_FILE = ".cth_engine_stamp"

        /** Must match `CorsixTH/Lua/api_version.lua` for the packaged 0.70.1 tree. */
        const val EXPECTED_ENGINE_API_VERSION = 2717

        private val ENGINE_MARKER_FILES = arrayOf(
            "CorsixTH.lua",
            "Lua/app.lua",
            "Lua/api_version.lua",
            "Lua/earthquake.lua",
            "Lua/endconditions.lua",
            "Lua/dialogs/android_menu_button.lua",
            "Lua/dialogs/resizables/android_menu.lua",
            "Bitmap/android.dat",
        )

        private val TH_MARKER_FILES = arrayOf(
            "QDATA/AREA01V.DAT",
            "DATA/LANG-0.DAT",
        )

        const val SAVE_GAME_EXTENSION = "sav"
        const val SAVE_GAME_FILE_SUFFIX = ".$SAVE_GAME_EXTENSION"
    }
}
