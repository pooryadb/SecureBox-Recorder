package ir.romroid.secureboxrecorder.presentation.box

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.net.toFile
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ir.romroid.secureboxrecorder.base.component.BaseFragment
import ir.romroid.secureboxrecorder.databinding.FragmentWebViewBinding
import ir.romroid.secureboxrecorder.domain.model.FileModel
import ir.romroid.secureboxrecorder.domain.model.FileType
import ir.romroid.secureboxrecorder.ext.getBackStackLiveData
import ir.romroid.secureboxrecorder.ext.logD
import ir.romroid.secureboxrecorder.ext.logE
import ir.romroid.secureboxrecorder.utils.BACK_FROM_OPEN_FILE

@AndroidEntryPoint
class WebViewFragment : BaseFragment<FragmentWebViewBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentWebViewBinding
        get() = FragmentWebViewBinding::inflate

    private val fileManagerVM by viewModels<BoxViewModel>()

    private val args by navArgs<WebViewFragmentArgs>()

    override fun viewHandler(view: View, savedInstanceState: Bundle?) {

        binding?.apply {

            webView.apply {
                webViewClient = webClient

                settings.apply {
                    javaScriptEnabled = true

                    allowFileAccess = true

                    builtInZoomControls = true

                    loadWithOverviewMode = true
                    useWideViewPort = true
                }
            }

            if (isRestoredFromBackStack.not())
                fileManagerVM.getFile(args.fileModel.uri)
        }
    }

    private val webClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return false
        }

        override fun onPageFinished(view: WebView?, url: String?) {
//            view?.zoomBy(0.02f)
            super.onPageFinished(view, url)
        }

        override fun onLoadResource(view: WebView?, url: String?) {
            super.onLoadResource(view, url)
        }
    }

    override fun initObservers() {
        super.initObservers()

        fileManagerVM.liveTempFile.observe(this) {
            openFile(it)
        }
    }

    override fun initBackStackObservers() {
        super.initBackStackObservers()

        findNavController().getBackStackLiveData<Unit?>(BACK_FROM_OPEN_FILE)
            ?.observe(this) {
                "backFrom OpenFileDialog".logD(TAG)
                findNavController().navigateUp()
            }
    }

    private fun openFile(fileModel: FileModel) {
        fileModel.logE("$TAG openFile")

        when {
            (fileModel.type == FileType.Document && fileModel.uri.toFile().extension == "pdf")
                    || fileModel.type == FileType.Other ->
                findNavController().navigate(
                    WebViewFragmentDirections.actionWebViewFragmentToOpenFileDialog(
                        fileModel
                    )
                )

            else -> binding?.webView?.loadUrl(fileModel.uri.path!!)
        }
    }

    override fun onDestroy() {
        "onDestroy".logD(TAG)
        // FIXME: onDestroy isn't best approach
        fileManagerVM.clearTemp()
        binding?.webView?.destroy()
        super.onDestroy()
    }

    private companion object {
        const val TAG = "WebViewFragment"
    }
}