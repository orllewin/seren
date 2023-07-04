package orllewin.browser

import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import orllewin.filter.db.Filter
import orllewin.filter.db.Filters
import orllewin.filter.ui.FilterDialog
import orllewin.extensions.*
import orllewin.settings.Settings
import orllewin.gemini.Gemini
import orllewin.gemini.Response
import oppen.gemini.browser.AddressEditorActionListener
import oppen.gemini.browser.UIState
import orllewin.browser.databinding.BrowserFragmentBinding

import orllewin.logger.Logger
import orllewin.file_io.Mime
import orllewin.file_io.SafIO
import orllewin.browser.*
import orllewin.gemini.downloader.Downloader
import orllewin.gemini.gemtext.Gemtext
import orllewin.gemini.gemtext.adapter.AdapterConfig
import orllewin.gemini.gemtext.adapter.GemtextAdapter
import orllewin.gemini.gemtext.adapter.GemtextLinesAdapter
import orllewin.gemini.UriHandler
import orllewin.gemini.viewer.ViewerActivity
import orllewin.history.db.History
import orllewin.imageviewer.ImageViewerActivity
import orllewin.lib.resources.SerenDialog
import orllewin.settings.PreferenceActivity


class BrowserFragment: Fragment() {

    private lateinit var viewModel: BrowserViewModel
    private lateinit var binding: BrowserFragmentBinding
    private lateinit var history: History
    private var uiStateJob: Job? = null
    private val overflow = BrowserOverflow()
    private var autoloadImages = false

    private val safIO = SafIO()
    private lateinit var filters: Filters

    private var scheduleScroll = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BrowserFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    lateinit var adapter: GemtextAdapter
    lateinit var nuAdapter: GemtextLinesAdapter

    private val stateMap = HashMap<String, Parcelable?>()
    private fun putScrollPosition(){
        //Recycler scroll position
        val currentUri = viewModel.uriHandler.toUri()
        val state: Parcelable? = (binding.recycler.layoutManager as LinearLayoutManager).onSaveInstanceState()
        stateMap[currentUri.toString()] = state
    }

    private fun restoreScrollPosition(){
        val address = viewModel.uriHandler.toUri().toString()
        when{
            stateMap.containsKey(address) -> {
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.recycler.layoutManager?.onRestoreInstanceState(stateMap[address])
                    binding.appBar.setExpanded(false)
                }, 100)

            }
        }
    }

    private fun inlineImage(position: Int, uri: Uri) {
        adapter.loadImage(position, uri)
    }

    private fun showLinkMenu(uri: Uri, view: View) {
        LinkMenu.show(requireContext(), uri, view) { actionId ->
            when (actionId) {
                R.id.menu_action_link_label -> viewModel.adapterNavigation(uri)
                R.id.menu_action_copy_link -> {
                    Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, uri.toString())
                        type = "text/plain"
                        val shareIntent = Intent.createChooser(this, null)
                        startActivity(shareIntent)
                    }
                }
                R.id.menu_add_to_filters -> {
                    FilterDialog(requireContext(), FilterDialog.mode_new, "$uri", Filter.TYPE_HIDE) { _, _ ->
                        mainThread {
                            refreshFilters()
                        }
                    }.show()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        safIO.registerForFileCreation(requireActivity())
        filters = Filters(requireContext())

        //Hack so we can preload baked-in home content
        Gemini.doNothingAddress = Settings.DEFAULT_HOME_CAPSULE

        adapter = GemtextAdapter(requireContext()){ uri, isLongTap, view, position ->
            when {
                isLongTap -> {
                    when {
                        uri.isImage() && !uri.isWeb() -> viewModel.fetchImage(uri){ imageUri ->
                            inlineImage(position, imageUri)
                        }
                        else -> showLinkMenu(uri, view)
                    }
                }
                else -> {
                    when {
                        uri.isWeb() -> {
                            Intent(Intent.ACTION_VIEW).run {
                                data = uri
                                startActivity(this)
                            }
                        }
                        uri.isApp() -> {
                            when(uri.toString()){
                                "seren://bookmarks" ->  overflow.gotoBookmarks(requireContext())
                                "seren://history" ->  overflow.gotoHistory(requireContext())
                                "seren://identities" ->  overflow.gotoIdentities(requireContext(), null)
                                "seren://settings" ->  startActivity(Intent(context, PreferenceActivity::class.java))
                            }
                        }
                        uri.isImage() -> viewModel.fetchImage(uri){ imageUri ->
                            startActivity(ImageViewerActivity.createIntent(requireContext(), imageUri))
                        }
                        else -> {
                            putScrollPosition()
                            viewModel.adapterNavigation(uri)
                        }
                    }
                }
            }
        }

        overflow.registerActivityLauncher(requireActivity() as AppCompatActivity)
        history = History(requireContext())

        Gemini.initialiseDownloadManager(DownloadManager(requireContext()))

        viewModel = ViewModelProvider(this).get(BrowserViewModel::class.java)
        binding.model = viewModel

        //todo - see if there's alternative content in prefs
        val defaultHomeContent = requireActivity().assets.open("home.gmi").bufferedReader().use { it.readText() }
        viewModel.setLocalHomeContent(defaultHomeContent)

        //If screen is portrait (likely a phone) hide the extended menu items meant for desktop use
        if(Resources.getSystem().displayMetrics.widthPixels < Resources.getSystem().displayMetrics.heightPixels ){
            binding.desktopMenuItems.hide()
        }

        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter

        binding.addressEdit.run {
            //Hardware keyboard support for Chromebooks:
            setOnKeyListener { _, keyCode, event ->
                return@setOnKeyListener when {
                    KeyHelper.isEnter(event, keyCode) -> {
                        hideKeyboard()
                        clearFocus()
                        UriHandler.inferAction(text.toString().trim()){ address ->
                            viewModel.navigation(address)
                        }
                        true
                    }
                    KeyHelper.isFocusAddress(event, keyCode) -> {
                        binding.addressEdit.requestFocus()
                        binding.addressEdit.selectAll()
                        true
                    }
                    KeyHelper.isNewAddress(event, keyCode) -> {
                        binding.addressEdit.clear()
                        binding.addressEdit.requestFocus()
                        true
                    }
                    KeyHelper.isReload(event, keyCode) -> {
                        binding.pullToRefresh.isRefreshing = true
                        viewModel.onSwipeRefresh()
                        true
                    }
                    KeyHelper.isViewSource(event, keyCode) -> {
                        goViewSource()
                        true
                    }
                    KeyHelper.isQuit(event, keyCode) -> {
                        requireActivity().finish()
                        true
                    }
                    else -> false
                }
            }
            setOnEditorActionListener(AddressEditorActionListener(this){ address ->
                viewModel.navigation(address)
            })
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            when {
                viewModel.canGoBack() -> {
                    scheduleScroll = true
                    viewModel.navigation(viewModel.goBack())
                }
                else ->{
                    this.remove()
                    requireActivity().finish()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        uiStateJob = lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                when (uiState) {
                    UIState.ResetState -> {
                        //NOOP
                    }
                    is UIState.ShowLog -> {

                    }
                    is UIState.GeminiResponse -> {
                        binding.pullToRefresh.isRefreshing = false
                        when (uiState.response) {
                            is Response.Binary -> {
                            }
                            is Response.Empty -> {
                                println("Empty response...")
                                binding.progress.hide()
                            }
                            is Response.Error -> {
                                binding.progress.hide()
                                SerenDialog(getString(R.string.error), uiState.response.error, getString(R.string.close), {})
                                    .show(childFragmentManager, "gemini_error_dialog")
                            }
                            is Response.File -> {
                            }
                            is Response.Gemtext -> {
                                if(uiState.address.isNotBlank()){
                                    binding.addressEdit.setText(uiState.address)

                                    when {
                                        uiState.address != Settings.DEFAULT_HOME_CAPSULE -> {
                                            val title = Gemtext.findTitle(uiState.response.lines)
                                            history.add(title, uiState.address)
                                        }
                                    }
                                }
                                adapter.render(
                                    lines = uiState.response.lines,
                                    hideAsciiArt = Settings.hideAsciiArt(),
                                    removeEmoji = Settings.hideEmoji(),
                                    autoloadImages = autoloadImages,
                                    address = uiState.response.request?.uri.toString())

                                if(scheduleScroll){
                                    restoreScrollPosition()
                                }else{
                                    binding.recycler.scheduleLayoutAnimation()
                                }
                                scheduleScroll = false
                            }
                            is Response.Input -> {
                                binding.progress.hide()
                                InputDialog(requireContext(), uiState.response.header.meta){ input ->
                                    //todo - really should copy the port here too, for most users it wont matter though:
                                    val originalRequest = uiState.response.request
                                    viewModel.navigation("${originalRequest?.uri.toString()}?$input")
                                }.show()
                            }
                            is Response.Redirect -> viewModel.navigateRedirect(uiState.response.redirect.toString())
                            is Response.Text -> {
                            }
                            is Response.IdentityRequired -> {
                                binding.progress.hide()
                                SerenDialog(
                                    getString(R.string.identity_required),
                                    uiState.response.header.meta,
                                    getString(R.string.cancel),
                                    {},
                                    getString(R.string.set_identity)
                                ) {
                                    overflow.gotoIdentities(requireContext(), uiState.response.uri.host)
                                }.show(childFragmentManager, "identity_required_dialog")
                            }
                            is Response.Image -> {
                                //handled elsehwere, delete?
                            }
                            else -> {}
                        }

                        binding.recycler.post {
                            binding.recycler.scrollToPosition(0)
                            binding.appBar.setExpanded(true, true)
                        }
                    }
                    UIState.GoHome -> viewModel.navigation(Settings.homeCapsule(requireContext()))
                    UIState.MainMenu -> {
                        overflow.show(
                            context = requireContext(),
                            root = binding.root,
                            anchor = binding.more,
                            viewModel = viewModel,
                            onPrefs = {
                                startActivity(PreferenceActivity.createIntent(requireContext(), viewModel.localGemtext))
                            },
                            onViewSource = this@BrowserFragment::goViewSource,
                            onShare = {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, viewModel.uriHandler.toString())
                                    type = "text/plain"
                                }

                                val shareIntent = Intent.createChooser(sendIntent, null)
                                startActivity(shareIntent)
                            }){
                            binding.addressEdit.clear()
                            binding.addressEdit.requestFocus()
                            binding.addressEdit.showKeyboard()
                        }
                        binding.addressEdit.hideKeyboard()
                    }
                    UIState.GoBookmarks -> overflow.gotoBookmarks(requireContext())
                    UIState.GoHistory -> overflow.gotoHistory(requireContext())
                    UIState.GoIdentity -> overflow.gotoIdentities(requireContext(), null)
                    is UIState.Binary -> {
                        binding.progress.hide()
                        val address = uiState.address
                        SerenDialog(
                            title = "Binary Download",
                            content = "Download $address",
                            negative = "Cancel",
                            onNegativeAction = {},
                            positive = "Download",
                            onPositiveAction = {
                                val filename = address.filename()
                                safIO.newFile(filename, Mime.fromFilename(filename)) { safUri ->
                                    Downloader(requireContext()).enqueue(address, safUri)
                                }
                            }
                        ).show(childFragmentManager, "binary_download_dialog")
                    }
                }
            }
        }
    }

    private fun goViewSource() {
        val title = viewModel.currentTitle
        val address = viewModel.currentAddress
        val content = adapter.getRaw()
        startActivity(ViewerActivity.createIntent(requireContext(), title, address, content))
    }

    override fun onResume() {
        super.onResume()

        autoloadImages = Settings.prefs.getBoolean(getString(R.string.prefs_experimental_autoload_images), false)
        val experimentalMode = Settings.prefs.getString(getString(R.string.prefs_experimental_image_mode), "none")

        val colourH1Blocks = Settings.prefs.getBoolean(getString(R.string.prefs_h1_accent_coloured), true)
        val hideCodeBlocks = Settings.prefs.getBoolean(getString(R.string.prefs_collapse_code_blocks), false)
        val showInlineIcons = Settings.prefs.getBoolean(getString(R.string.prefs_inline_link_icons), true)
        val fullWidthButtons = Settings.prefs.getBoolean(getString(R.string.prefs_full_width_buttons), false)
        val fullWidthButtonColour = Settings.prefs.getString(getString(R.string.prefs_button_colour), null)
        val headerTypeface = Settings.prefs.getString(getString(R.string.prefs_sans_headers_3), "google_sans")
        val contentTypeface = Settings.prefs.getString(getString(R.string.prefs_sans_content_3), "google_sans")

        val screenBackground = Settings.prefs.getString(getString(R.string.prefs_background_colour), null)
        val homeColour = Settings.prefs.getString(getString(R.string.prefs_home_icon_colour), Settings.DEFAULT_ACCENT_COLOUR)

        val duotoneBackground: Int? = when {
            screenBackground != null -> Color.parseColor(screenBackground)
            else -> null
        }

        val duotoneColour = Settings.prefs.getString(getString(R.string.prefs_duotone_colour), "#1d1d1d")
        val duotoneForeground: Int = Color.parseColor(duotoneColour)

        val adapterConfig = AdapterConfig.Builder()
            .colourLargeHeaders(Settings.colourLargeHeaders())
            .homeColour(Settings.homeColour())
            .remapBoldUnicodeChars(Settings.remapBoldUnicode())
            .build()

        adapter.updateAdapterViewSettings(
            adapterConfig,
            colourH1Blocks,
            hideCodeBlocks,
            showInlineIcons,
            fullWidthButtons,
            fullWidthButtonColour,
            headerTypeface!!,
            contentTypeface!!,
            experimentalMode!!,
            duotoneBackground,
            duotoneForeground,
            Color.parseColor(homeColour))

        Settings.prefs.getString(getString(R.string.prefs_background_colour), null)?.let { backgroundColour ->
            try {
                val longColour = java.lang.Long.parseLong(backgroundColour.substring(1), 16)
                binding.root.background = ColorDrawable(Color.parseColor(backgroundColour))
            }catch (nfe: NumberFormatException){
                Logger.log("Error parsing background colour in BrowserFragment: $backgroundColour")
            }

        }

        Settings.prefs.getString(getString(R.string.prefs_home_icon_colour), Settings.DEFAULT_ACCENT_COLOUR)?.let { homeColour ->
            try {
                val longColour = java.lang.Long.parseLong(homeColour.substring(1), 16)
                val colour = Color.parseColor(homeColour)
                binding.home.setColorFilter(colour)
                binding.progress.indeterminateTintList = ColorStateList.valueOf(colour)
            }catch (nfe: NumberFormatException){
                Logger.log("Error parsing home colour in BrowserFragment: $homeColour")
            }
        }

        refreshFilters()
    }

    private fun refreshFilters(){
        filters.get { filters ->
            mainThread {
                adapter.setFilters(filters)
                adapter.render()
            }
        }
    }

    override fun onStop() {
        uiStateJob?.cancel()
        super.onStop()
    }

    fun defaultStart() = viewModel.start(Settings.homeCapsule(requireContext()))
    fun preload(address: String?, gemtext: String?) = viewModel.preload(address, gemtext)
}