package app.vercors.feature.project.domain.usecase

import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceIconError
import app.vercors.feature.instance.domain.usecase.SetInstanceIconFromUrlUseCase
import app.vercors.feature.project.domain.model.ProjectDetailsError
import app.vercors.feature.project.domain.model.ProjectId
import arrow.core.raise.recover
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

// Best-effort: a missing logo must never fail the install
@Inject
class ApplyProjectIconUseCase(
    private val getProject: GetProjectUseCase,
    private val setInstanceIconFromUrl: SetInstanceIconFromUrlUseCase,
) {
    suspend operator fun invoke(instance: Instance, projectId: ProjectId) {
        recover({
            val logoUrl = getProject(projectId).logoUrl ?: return
            recover({ setInstanceIconFromUrl(instance, logoUrl) }) { _: InstanceIconError ->
                logger.warn { "Failed to set instance icon for ${instance.id} from $logoUrl" }
            }
        }) { error: ProjectDetailsError ->
            logger.warn { "Failed to fetch project details for instance icon (${instance.id}): $error" }
        }
    }
}
