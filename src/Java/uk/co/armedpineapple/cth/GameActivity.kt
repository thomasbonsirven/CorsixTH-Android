package uk.co.armedpineapple.cth

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.Keep
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.libsdl.app.CthImeBridge
import org.libsdl.app.SDLActivity
import org.libsdl.app.SDLSurface
import uk.co.armedpineapple.cth.files.FilesService
import uk.co.armedpineapple.cth.files.SaveGameContract
import uk.co.armedpineapple.cth.persistence.saves.SaveData
import uk.co.armedpineapple.cth.settings.SettingsActivity
import uk.co.armedpineapple.cth.setup.SetupActivity
import uk.co.armedpineapple.cth.stats.StatisticsService
import java.io.File

class GameActivity : SDLActivity(), Loggable {
    @Keep
    private external fun startLogger()

    @Keep
    private external fun nativeSave(saveName: String)

    @Keep
    private external fun nativeLoad(saveName: String)

    @Keep
    private external fun nativeUpdateConfig(config: GameConfiguration)

    private val configuration: GameConfiguration
        get() = (application as CTHApplication).configuration


    private val filesService: FilesService get() = (application as CTHApplication).filesService

    private val statisticsService: StatisticsService by lazy {
        StatisticsService((application as CTHApplication).statsDatabase)
    }

    private var playGamesService: PlayGamesService? = null

    /** Real EditText used to attach the soft keyboard (SurfaceView steals DummyEdit focus). */
    private var imeEdit: EditText? = null
    private var imeBridgeText: String = ""
    private var imeWatcher: TextWatcher? = null

    @get:Keep
    val gameEventHandler by lazy {
        GameEventHandler(statisticsService)
    }

    private val saveDao get() = (application as CTHApplication).gameDatabase.saveDao()

    private val loadGameLauncher: ActivityResultLauncher<Boolean> =
        registerForActivityResult(SaveGameContract()) { o -> doLoad(o) }
    private val saveGameLauncher: ActivityResultLauncher<Boolean> =
        registerForActivityResult(SaveGameContract()) { o -> doSave(o) }

    override fun onCreate(savedInstanceState: Bundle?) {
        singleton = this
        if (!BuildConfig.COMMUNITY_BUILD) {
            playGamesService = PlayGamesService(this, statisticsService)
        }

        val filesService = FilesService(this)

        // Check whether the setup installation has run and the original TH files are available.
        // If not, redirect over to the setup activity.
        if (!filesService.hasOriginalFiles(configuration)) {
            finishAndRemoveTask()
            val intent = Intent(this, SetupActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            super.onCreate(savedInstanceState)
            return;
        }

        // Install / upgrade CorsixTH engine payload from assets/game.zip when needed.
        var installJob: Job? = null
        val needsEngineInstall =
            (application as CTHApplication).isFirstLaunchForVersion ||
                filesService.needsEngineDataUpgrade(configuration) ||
                BuildConfig.ALWAYS_UPGRADE
        if (needsEngineInstall) {
            Toast.makeText(this, getString(R.string.upgrading), Toast.LENGTH_SHORT).show()

            val target = configuration.cthFiles
            if (target.exists()) target.deleteRecursively()

            installJob = CoroutineScope(Dispatchers.IO).launch {
                filesService.installGameFiles(configuration)
            }
        }

        // Install the music library in the background
        var musicInstallJob: Job? = null
        if (!filesService.hasMusicLibrary(configuration)) {
            val target = configuration.musicLib
            if (target.exists()) target.deleteRecursively()

            musicInstallJob = CoroutineScope(Dispatchers.IO).launch {
                filesService.installMusicLibrary(configuration)
            }
        }

        var fontInstallJob: Job? = null
        if (!configuration.unicodeFont.exists()) {
            fontInstallJob = CoroutineScope(Dispatchers.IO).launch {
                configuration.unicodeFont.parentFile?.mkdirs()
                filesService.copyAsset(
                    "DroidSansFallbackFull.ttf", singleton, configuration.unicodeFont
                )
            }
        }

        // Upgrading the game files will nuke the installation directory, so make sure that
        // the latest configuration exists in there.
        configuration.persist()

        super.onCreate(savedInstanceState)
        nativeSetenv("TIMIDITY_CFG", File(configuration.musicLib, "timidity.cfg").absolutePath);

        startLogger()

        // Make sure the game file installation installation has completed before moving on.
        if (installJob != null || musicInstallJob != null || fontInstallJob != null) {
            runBlocking {
                installJob?.join()
                musicInstallJob?.join()
                fontInstallJob?.join()
            }
        }
    }

    private fun launchLoadGamePicker() {
        loadGameLauncher.launch(true)
    }

    private fun launchSaveGamePicker() {
        saveGameLauncher.launch(false)
    }
    override fun createSDLSurface(context: Context?): SDLSurface {
        return GameSurface(context)
    }

    private fun doLoad(saveName: String?) {
        saveName?.let { save ->
            val savePath = filesService.getSaveFile(save, configuration)

            if (savePath.exists()) {
                nativeLoad(savePath.absolutePath)
            }
        }
    }

    private fun doSave(saveName: String?) {
        if (saveName != null) {
            val savePath = File(configuration.saveFiles, saveName)
            nativeSave(savePath.absolutePath)
        }
    }

    private fun updateSaveGameDatabase(
        filePath: String, rep: Int, money: Long, level: String, screenshot: String
    ) {
        val fileName = File(filePath).name
        CoroutineScope(Dispatchers.IO).launch {
            saveDao.upsert(
                SaveData(
                    saveName = fileName,
                    screenshotPath = screenshot,
                    rep = rep,
                    money = money,
                    levelName = level
                )
            )
        }
    }

    @Override
    override fun getMainSharedObject(): String {
        return getContext().applicationInfo.nativeLibraryDir + "/" + "libappmain.so"
    }

    @Override
    override fun getLibraries(): Array<String>? {
        return arrayOf(
            "SDL2", "SDL2_mixer", "appmain"
        )
    }

    @Override
    override fun getMainFunction(): String {
        return "SDL_main"
    }

    override fun getArguments(): Array<String> {
        return arrayOf(
            "--interpreter=${configuration.cthLaunchScript.absolutePath}",
            "--config-file=${configuration.gameConfigFile.absolutePath}"
        )
    }

    fun updateGameConfig() {
        Log.i("GameActivity", "Updating game configuration")

        nativeUpdateConfig(configuration)

        Log.i("GameActivity", "Updated game config")
    }

    override fun setOrientationBis(w: Int, h: Int, resizable: Boolean, hint: String?) {
        if (configuration.allowPortrait) {
            super.setOrientationBis(w, h, resizable, hint);
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }

    /**
     * Force the Android soft keyboard for CorsixTH textboxes.
     * Immersive SDL surfaces normally keep focus and block the IME.
     */
    private fun forceShowSoftKeyboard() {
        Log.i(TAG, "forceShowSoftKeyboard")
        try {
            File(filesDir, "ime_show_marker.txt").writeText(
                "show@${System.currentTimeMillis()}\n"
            )
        } catch (_: Exception) {
        }
        // Block SDLActivity from re-asserting immersive sticky (steals IME focus).
        CthImeBridge.setScreenKeyboardShown(true)
        mFullscreenModeActive = false

        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        if (Build.VERSION.SDK_INT >= 30) {
            window.insetsController?.show(
                WindowInsets.Type.ime() or WindowInsets.Type.systemBars()
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }

        mSurface?.isFocusable = false
        mSurface?.isFocusableInTouchMode = false
        mSurface?.clearFocus()

        val edit = ensureImeEdit()
        edit.visibility = View.VISIBLE
        edit.isFocusable = true
        edit.isFocusableInTouchMode = true
        edit.requestFocus()

        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        fun tryShow() {
            edit.requestFocus()
            imm.restartInput(edit)
            val shown = imm.showSoftInput(edit, InputMethodManager.SHOW_FORCED)
            if (!shown) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            }
            if (Build.VERSION.SDK_INT >= 30) {
                window.insetsController?.show(WindowInsets.Type.ime())
                edit.windowInsetsController?.show(WindowInsets.Type.ime())
            }
        }

        edit.post { tryShow() }
        edit.postDelayed({ tryShow() }, 100)
        edit.postDelayed({ tryShow() }, 350)
    }

    private fun ensureImeEdit(): EditText {
        imeEdit?.let { return it }

        // Custom EditText: when empty, still forward Backspace to CorsixTH so the
        // pre-filled name (e.g. "PLAYER") can be erased — the IME buffer alone
        // only knows about characters typed after the keyboard opened.
        val edit = object : EditText(this) {
            override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
                val base = super.onCreateInputConnection(outAttrs) ?: return null
                return object : InputConnectionWrapper(base, true) {
                    private fun forwardBackspaces(count: Int): Boolean {
                        if ((text?.length ?: 0) != 0 || count <= 0) {
                            return false
                        }
                        repeat(count) { CthImeBridge.backspace() }
                        return true
                    }

                    override fun deleteSurroundingText(
                        beforeLength: Int,
                        afterLength: Int
                    ): Boolean {
                        if (forwardBackspaces(beforeLength)) {
                            return true
                        }
                        return super.deleteSurroundingText(beforeLength, afterLength)
                    }

                    override fun deleteSurroundingTextInCodePoints(
                        beforeLength: Int,
                        afterLength: Int
                    ): Boolean {
                        if (forwardBackspaces(beforeLength)) {
                            return true
                        }
                        return super.deleteSurroundingTextInCodePoints(beforeLength, afterLength)
                    }

                    override fun sendKeyEvent(event: KeyEvent): Boolean {
                        if (event.action == KeyEvent.ACTION_DOWN &&
                            event.keyCode == KeyEvent.KEYCODE_DEL &&
                            forwardBackspaces(1)
                        ) {
                            return true
                        }
                        return super.sendKeyEvent(event)
                    }
                }
            }
        }.apply {
            setBackgroundColor(Color.TRANSPARENT)
            setTextColor(Color.TRANSPARENT)
            setHintTextColor(Color.TRANSPARENT)
            alpha = 0.01f
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI or
                EditorInfo.IME_FLAG_NO_FULLSCREEN or
                EditorInfo.IME_ACTION_DONE
            isSingleLine = true
            // Keep a non-zero hit target so the IME can attach on modern Android.
            minimumHeight = (48 * resources.displayMetrics.density).toInt()
        }

        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            (48 * resources.displayMetrics.density).toInt()
        )
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString() ?: ""
                syncImeTextToSdl(text)
            }
        }
        imeWatcher = watcher
        edit.addTextChangedListener(watcher)
        edit.setOnEditorActionListener { _, _, _ ->
            forceHideSoftKeyboard()
            true
        }

        mLayout.addView(edit, params)
        imeEdit = edit
        imeBridgeText = ""
        return edit
    }

    private fun syncImeTextToSdl(text: String) {
        val previous = imeBridgeText
        var match = 0
        val max = minOf(previous.length, text.length)
        while (match < max && previous[match] == text[match]) {
            match++
        }
        for (i in match until previous.length) {
            CthImeBridge.backspace()
        }
        if (match < text.length) {
            CthImeBridge.commitText(text.substring(match))
        }
        imeBridgeText = text
    }

    private fun forceHideSoftKeyboard() {
        Log.i(TAG, "forceHideSoftKeyboard")
        CthImeBridge.setScreenKeyboardShown(false)

        if (Build.VERSION.SDK_INT >= 30) {
            window.insetsController?.hide(WindowInsets.Type.ime())
        }
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val edit = imeEdit
        if (edit != null) {
            imm.hideSoftInputFromWindow(edit.windowToken, 0)
            edit.clearFocus()
            edit.visibility = View.GONE
            imeWatcher?.let { edit.removeTextChangedListener(it) }
            edit.setText("")
            imeWatcher?.let { edit.addTextChangedListener(it) }
            imeBridgeText = ""
        } else {
            val token = currentFocus?.windowToken ?: window.decorView.windowToken
            if (token != null) {
                imm.hideSoftInputFromWindow(token, 0)
            }
        }

        window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        mFullscreenModeActive = true

        mSurface?.isFocusable = true
        mSurface?.isFocusableInTouchMode = true
        mSurface?.requestFocus()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    companion object {
        private const val TAG = "GameActivity"

        @JvmStatic
        lateinit var singleton: GameActivity

        @Keep
        @JvmStatic
        fun showSettings() {
            Log.i(TAG, "Showing settings")

            val intent = Intent(singleton, SettingsActivity::class.java)
            singleton.startActivity(intent);
        }

        @Keep
        @JvmStatic
        fun showLoad() {
            singleton.launchLoadGamePicker()
        }

        @Keep
        @JvmStatic
        fun showSave() {
            singleton.launchSaveGamePicker()
        }

        @Keep
        @JvmStatic
        fun showSoftKeyboard() {
            try {
                val activity = singleton
                activity.runOnUiThread {
                    try {
                        activity.forceShowSoftKeyboard()
                    } catch (t: Throwable) {
                        Log.e(TAG, "forceShowSoftKeyboard failed", t)
                        Toast.makeText(activity, "IME error: ${t.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (t: Throwable) {
                Log.e(TAG, "showSoftKeyboard failed", t)
            }
        }

        @Keep
        @JvmStatic
        fun hideSoftKeyboard() {
            try {
                val activity = singleton
                activity.runOnUiThread {
                    try {
                        activity.forceHideSoftKeyboard()
                    } catch (t: Throwable) {
                        Log.e(TAG, "forceHideSoftKeyboard failed", t)
                    }
                }
            } catch (t: Throwable) {
                Log.e(TAG, "hideSoftKeyboard failed", t)
            }
        }

        @Keep
        @JvmStatic
        fun signIn() {
            singleton.playGamesService?.signIn()
        }

        @Keep
        @JvmStatic
        fun showAchievements() {
            singleton.playGamesService?.showAchievements()
        }

        @Keep
        @JvmStatic
        fun onSaveGameChanged(
            fileName: ByteArray, rep: Int, money: Long, level: ByteArray, screenshot: ByteArray
        ) {
            singleton.updateSaveGameDatabase(
                filePath = fileName.toUtf8String(),
                rep = rep,
                money = money,
                level = level.toUtf8String(),
                screenshot = screenshot.toUtf8String()
            )
        }

        @Keep
        @JvmStatic
        fun onGameError(handler: ByteArray?, stack: ByteArray?) {
            Firebase.crashlytics.recordException(
                if (handler != null) {
                    NativeLuaHandlerException(handler, stack)
                } else {
                    NativeLuaException(stack, "Game Error")
                }
            )
        }
    }
}