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

package app.vercors.launcher.project.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import app.vercors.launcher.project.domain.ProjectId
import app.vercors.launcher.project.domain.ProjectProvider
import app.vercors.launcher.project.domain.toProvider
import app.vercors.launcher.project.presentation.details.ProjectDetailsScreen
import app.vercors.launcher.project.presentation.list.ProjectListScreen
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data object ProjectGraph

@Serializable
internal data object ProjectListRoute

@Serializable
internal data class ProjectDetailsRoute(val provider: String, val id: String)

fun NavController.navigateToProjectList(builder: NavOptionsBuilder.() -> Unit = {}) = navigate(
    route = ProjectListRoute,
    builder = builder
)

fun NavController.navigateToProjectDetails(projectId: ProjectId, builder: NavOptionsBuilder.() -> Unit = {}) = navigate(
    route = ProjectDetailsRoute(projectId.provider.id, projectId.id),
    builder = builder
)

fun NavGraphBuilder.projectSection() {
    navigation<ProjectGraph>(startDestination = ProjectListRoute) {
        composable<ProjectListRoute> {
            ProjectListScreen()
        }
        composable<ProjectDetailsRoute> {
            val route: ProjectDetailsRoute = it.toRoute()
            ProjectDetailsScreen(ProjectId(route.provider.toProvider(), route.id))
        }
    }
}