/*
 * Copyright (c) 2024-2025 skyecodes
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

package app.vercors.launcher.home.presentation

import app.vercors.launcher.core.presentation.mvi.MviViewModel
import app.vercors.launcher.home.domain.HomeRepository
import app.vercors.launcher.home.domain.HomeSection
import app.vercors.launcher.home.domain.HomeSectionType
import app.vercors.launcher.project.domain.ProjectType
import kotlinx.coroutines.Job
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class HomeViewModel(
    private val homeRepository: HomeRepository,
) : MviViewModel<HomeUiState, HomeUiIntent, HomeUiEffect>(HomeUiState()) {
    private var sectionsJob: Job? = null

    override fun onStart() {
        super.onStart()
        sectionsJob?.cancel()
        sectionsJob = collectInScope(homeRepository.observeSections()) {
            onIntent(UpdateSections(it))
        }
    }

    override fun onStop(cause: Throwable?) {
        super.onStop(cause)
        sectionsJob?.cancel()
    }

    override fun ReductionState.reduce(intent: HomeUiIntent) {
        when (intent) {
            is HomeUiIntent.ExpandSection -> when (intent.type) {
                HomeSectionType.JumpBackIn -> effect(HomeUiEffect.NavigateToInstanceList)
                HomeSectionType.PopularMods -> effect(HomeUiEffect.NavigateToProjectList(ProjectType.Mod))
                HomeSectionType.PopularModpacks -> effect(HomeUiEffect.NavigateToProjectList(ProjectType.Modpack))
                HomeSectionType.PopularResourcePacks -> effect(HomeUiEffect.NavigateToProjectList(ProjectType.ResourcePack))
                HomeSectionType.PopularShaderPacks -> effect(HomeUiEffect.NavigateToProjectList(ProjectType.ShaderPack))
            }
            HomeUiIntent.CreateInstance -> effect(HomeUiEffect.CreateInstance)
            is HomeUiIntent.InstallProject -> { /* TODO */
            }

            is HomeUiIntent.LaunchOrStopInstance -> { /* TODO */
            }

            is HomeUiIntent.ShowInstance -> effect(HomeUiEffect.NavigateToInstanceDetails(intent.instanceId))
            is HomeUiIntent.ShowProject -> effect(HomeUiEffect.NavigateToProjectDetails(intent.projectId))
            is UpdateSections -> update { HomeUiState(intent.sections.map { it.toUi() }) }
        }
    }


    @JvmInline
    private value class UpdateSections(val sections: List<HomeSection>) : HomeUiIntent
}