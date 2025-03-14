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

package app.vercors.launcher.instance.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import app.vercors.launcher.instance.domain.InstanceId
import app.vercors.launcher.instance.presentation.details.InstanceDetailsScreen
import app.vercors.launcher.instance.presentation.list.InstanceListScreen
import kotlinx.serialization.Serializable

@Serializable
data object InstanceGraph

@Serializable
internal data object InstanceListRoute

@Serializable
internal data class InstanceDetailsRoute(val instanceId: InstanceId)

fun NavController.navigateToInstanceList(builder: NavOptionsBuilder.() -> Unit = {}) = navigate(
    route = InstanceListRoute,
    builder = builder
)

fun NavController.navigateToInstanceDetails(instanceId: InstanceId, builder: NavOptionsBuilder.() -> Unit = {}) = navigate(
    route = InstanceDetailsRoute(instanceId),
    builder = builder
)

fun NavGraphBuilder.instanceSection() {
    navigation<InstanceGraph>(startDestination = InstanceListRoute) {
        composable<InstanceListRoute> {
            InstanceListScreen()
        }
        composable<InstanceDetailsRoute> {
            val route: InstanceDetailsRoute = it.toRoute()
            InstanceDetailsScreen(route.instanceId)
        }
    }
}