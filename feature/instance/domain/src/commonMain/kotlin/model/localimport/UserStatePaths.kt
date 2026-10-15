package app.vercors.feature.instance.domain.model.localimport

private val USER_STATE_DIRS = setOf("saves", "screenshots", "logs", "crash-reports")
private val USER_STATE_FILES = setOf("options.txt", "servers.dat")

// A shared pack can carry the sender's worlds and settings: an update must never overwrite the receiver's
fun isUserStatePath(path: String): Boolean {
    val firstSegment = path.substringBefore('/')
    return if ('/' in path) firstSegment in USER_STATE_DIRS else path in USER_STATE_FILES
}
