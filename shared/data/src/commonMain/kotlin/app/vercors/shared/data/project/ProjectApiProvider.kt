package app.vercors.shared.data.project

import app.vercors.meta.client.project.ProjectApi
import app.vercors.meta.client.project.createProjectApi
import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.core.annotation.Single

@Single
internal fun provideProjectApi(ktorfit: Ktorfit): ProjectApi = ktorfit.createProjectApi()
