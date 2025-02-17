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

package app.vercors.launcher.app.navigation

import app.vercors.launcher.account.presentation.navigation.AccountGraph
import app.vercors.launcher.core.resources.*
import app.vercors.launcher.home.presentation.navigation.HomeGraph
import app.vercors.launcher.instance.presentation.navigation.InstanceGraph
import app.vercors.launcher.project.presentation.navigation.ProjectGraph
import app.vercors.launcher.settings.presentation.navigation.SettingsGraph
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class TopLevelDestination(val route: Any, val icon: DrawableResource, val title: StringResource) {
    Home(HomeGraph, appDrawable { house }, appString { home }),
    Instances(InstanceGraph, appDrawable { hard_drive }, appString { instances }),
    Projects(ProjectGraph, appDrawable { folder_cog }, appString { projects }),
    Accounts(AccountGraph, appDrawable { user }, appString { accounts }),
    Settings(SettingsGraph, appDrawable { settings }, appString { settings }),
}