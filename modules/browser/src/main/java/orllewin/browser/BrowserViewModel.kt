package orllewin.browser

import android.net.Uri
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import oppen.gemini.browser.UIState
import orllewin.extensions.mainThread
import orllewin.gemini.Gemini
import orllewin.gemini.Header
import orllewin.gemini.Request
import orllewin.gemini.Response
import orllewin.gemini.UriHandler
import orllewin.logger.Logger
import orllewin.settings.Settings
import kotlin.concurrent.thread

class BrowserViewModel : ViewModel() {

    /**
     * This UriHandler object resolves any tapped links, which could be relative, before acting on them
     * It's the only state in the browser and should always be populated with the current Gemtext address
     */
    val uriHandler = UriHandler(Settings.DEFAULT_HOME_CAPSULE)

    var localGemtext: String? = null

    private val _uiState = MutableStateFlow<UIState>(UIState.GeminiResponse("", Response.Empty()))
    val uiState: StateFlow<UIState> = _uiState

    var showProgress = MutableLiveData(View.GONE)

    var currentAddress: String = ""
    var currentTitle: String = ""

    private val history = arrayListOf<String>()

    fun start(address: String){
        Logger.log("start: $address")
        navigation(address)
    }

    fun fetchImage(uri: Uri, onImageReady: (cacheUri: Uri) -> Unit){
        Logger.log("fetch image: $uri")
        val imageOuri = uriHandler.copy()
        imageOuri.resolve(uri.toString())

        showProgress.value = View.VISIBLE

        thread {
            Gemini.request(Request.Image(imageOuri.toUri())) { response ->
                mainThread {
                    showProgress.value = View.GONE
                    onImageReady((response as Response.Image).file)
                }
            }
        }
    }

    fun adapterNavigation(uri: Uri){
        Logger.log("adapter navigation: $uri")
        uriHandler.resolve(uri.toString())
        navigation(uriHandler.toString())
    }

    fun navigateRedirect(redirect: String){
        uriHandler.resolve(redirect)
        navigation(uriHandler.toString())
    }

    fun navigation(address: String?) {
        if(address == null) {
            Logger.log("Navigation request with null address")
            return
        }

        if(address == Settings.DEFAULT_HOME_CAPSULE){
            showOnboardingGemtext()
        }

        Logger.log("navigation: $address")

        showProgress.value = View.VISIBLE

        thread{
            Gemini.request(address) { response ->
                mainThread {
                    when (response) {
                        is Response.Gemtext -> {
                            Logger.log("navigation Response.Gemtext")
                            parseResponse(address, response)
                        }
                        is Response.Binary -> {
                            Logger.log("navigation Response.Binary: ${response.header.meta}")
                            //todo - SAF dialog to save to filesystem
                        }
                        is Response.Empty -> {
                            Logger.log("navigation Response.Empty: ${response.request?.uri}")
                        }
                        is Response.Error -> {
                            Logger.log("navigation Response.Error: ${response.error}")
                        }
                        is Response.File -> {
                            Logger.log("navigation Response.File: ${response.header.meta}")
                            _uiState.value = UIState.Binary(address)
                            _uiState.value = UIState.ResetState
                        }
                        is Response.IdentityRequired -> {
                            Logger.log("navigation Response.IdentityRequired")
                        }
                        is Response.Image -> {
                            //This block should never execute, handled elsewhere
                            Logger.log("navigation Response.Image: ${response.file}")
                        }
                        is Response.Input -> {
                            Logger.log("navigation Response.Input")

                        }
                        is Response.Redirect -> {
                            Logger.log("navigation Response.Redirect")
                        }
                        is Response.Text -> {
                            Logger.log("navigation Response.Text")
                        }
                        else -> {
                            //NOOP
                            Logger.log("navigation Response - unknown")
                        }
                    }
                    _uiState.value = UIState.GeminiResponse(address, response)
                }
            }
        }
    }

    private fun addToHistory(address: String?) {
        if(address == null) return
        if(history.isEmpty() || address != history.last()) history.add(address)
    }

    private fun parseResponse(address: String, response: Response.Gemtext){
        showProgress.value = View.GONE
        val host = response.request?.uri?.host
        currentAddress = address
        addToHistory(currentAddress)
        uriHandler.update(address)
    }

    /**
     * View binding method for browser_fragment: SwipeRefreshLayout
     */
    fun onSwipeRefresh() {
        Logger.log("swipe refresh")
        navigation(uriHandler.toString())
    }

    /**
     * Because we're misusing the response state as a general event-bus from ui xml to the view we
     * need a follow-up dummy event to clear the state.
     */
    fun goHome(){
        Logger.log("go home")
        history.clear()
        _uiState.value = UIState.GoHome
        _uiState.value = UIState.ResetState
    }
    fun mainMenu(){
        _uiState.value = UIState.MainMenu
        _uiState.value = UIState.ResetState
    }

    fun goBookmarks(){
        _uiState.value = UIState.GoBookmarks
        _uiState.value = UIState.ResetState
    }

    fun goHistory(){
        _uiState.value = UIState.GoHistory
        _uiState.value = UIState.ResetState
    }

    fun goIdentity(){
        _uiState.value = UIState.GoIdentity
        _uiState.value = UIState.ResetState
    }

    fun canGoBack(): Boolean = history.size > 1
    fun goBack(): String {
        return when (history.size) {
            1 -> history.first()
            else -> {
                val previous = history[history.size-2]
                history.removeLast()
                previous
            }
        }
    }

    fun preload(address: String?, gemtext: String?) {
        when (gemtext) {
            null -> {
                Logger.logWithTime("preload state from clicked link: $address")
                navigation(address)
            }
            else -> {
                Logger.logWithTime("preload content from $address")
                val dummyRequest = Request.Simple(Uri.parse(address))
                val dummyHeader = Header(123, "")
                val dummyResponse = Response.Gemtext(dummyRequest, dummyHeader, gemtext.lines())
                parseResponse(address ?: "", dummyResponse)
                _uiState.value = UIState.GeminiResponse(address ?: "", dummyResponse)
            }
        }
    }

    private fun showOnboardingGemtext(){
        preload(Settings.DEFAULT_HOME_CAPSULE, localGemtext)
    }

    fun setLocalHomeContent(_localGemtext: String) {
        localGemtext = _localGemtext
    }
}