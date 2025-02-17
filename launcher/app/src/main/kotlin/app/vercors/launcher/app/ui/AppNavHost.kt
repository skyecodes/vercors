/*
 * Copyright (c) 2024 skyecodes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package app.vercors.launcher.app.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import app.vercors.launcher.account.presentation.navigation.accountSection
import app.vercors.launcher.app.viewmodel.AppDialog
import app.vercors.launcher.app.viewmodel.AppUiIntent
import app.vercors.launcher.core.presentation.ui.AppAnimations
import app.vercors.launcher.core.presentation.ui.defaultEnterAnimation
import app.vercors.launcher.core.presentation.ui.defaultExitAnimation
import app.vercors.launcher.home.presentation.navigation.homeSection
import app.vercors.launcher.instance.presentation.navigation.instanceSection
import app.vercors.launcher.instance.presentation.navigation.navigateToInstanceDetails
import app.vercors.launcher.instance.presentation.navigation.navigateToInstanceList
import app.vercors.launcher.project.presentation.navigation.navigateToProjectDetails
import app.vercors.launcher.project.presentation.navigation.navigateToProjectList
import app.vercors.launcher.project.presentation.navigation.projectSection
import app.vercors.launcher.settings.presentation.navigation.settingsSection
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Any,
    onIntent: (AppUiIntent) -> Unit
) {
    val animations = AppAnimations.current

    LaunchedEffect(Unit) {
        navController.currentBackStack.collect { entries ->
            logger.debug { "Back stack updated: " + entries.mapNotNull { it.destination.route }.joinToString(", ") }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { if (animations) defaultEnterAnimation else EnterTransition.None },
        exitTransition = { if (animations) defaultExitAnimation else ExitTransition.None },
    ) {
        homeSection(
            onNavigateToInstanceDetails = { navController.navigateToInstanceDetails(it) },
            onNavigateToProjectDetails = { navController.navigateToProjectDetails(it) },
            onCreateInstance = { onIntent(AppUiIntent.OpenDialog(AppDialog.CreateInstance)) },
            onNavigateToInstanceList = { navController.navigateToInstanceList() },
            onNavigateToProjectList = { navController.navigateToProjectList() }, // TODO include project type
        )
        instanceSection()
        projectSection()
        accountSection()
        settingsSection()
    }
}