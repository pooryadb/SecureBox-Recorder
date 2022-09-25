package ir.romroid.secureboxrecorder.presentation.fileManager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.net.toFile
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ir.romroid.secureboxrecorder.base.component.BaseFragment
import ir.romroid.secureboxrecorder.databinding.FragmentWebViewBinding
import ir.romroid.secureboxrecorder.domain.model.FileModel
import ir.romroid.secureboxrecorder.domain.model.FileType
import ir.romroid.secureboxrecorder.ext.logD
import ir.romroid.secureboxrecorder.ext.logE

@AndroidEntryPoint
class WebViewFragment : BaseFragment<FragmentWebViewBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentWebViewBinding
        get() = FragmentWebViewBinding::inflate

    private val fileManagerVM by viewModels<FileManagerViewModel>()

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

                openFile(args.fileModel)
            }
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

    private fun openFile(fileModel: FileModel) {
        fileModel.logE("$TAG openFile")

        when (fileModel.type) {
            FileType.Text -> {
                if (fileModel.uri.toFile().extension == "pdf") {
                    binding?.webView?.loadUrl("http://docs.google.com/gview?embedded=true&url=" + fileModel.uri)
                }
            }
            else -> binding?.webView?.loadUrl(fileModel.uri.path!!)
        }
    }

    override fun onDestroy() {
        "onDestroy".logD(TAG)
        // FIXME: onDestroy not work properly
        fileManagerVM.clearTemp()
        binding?.webView?.destroy()
        super.onDestroy()
    }

    private val TAG = "WebViewFragment"
}