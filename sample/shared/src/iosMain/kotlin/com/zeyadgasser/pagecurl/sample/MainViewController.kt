package com.zeyadgasser.pagecurl.sample

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/** Entry point for the iOS sample app — wraps [DemoApp] in a Compose UIViewController. */
fun MainViewController(): UIViewController = ComposeUIViewController { DemoApp() }
