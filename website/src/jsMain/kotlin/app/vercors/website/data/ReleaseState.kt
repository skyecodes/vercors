package app.vercors.website.data

sealed interface ReleaseState {
    data object Loading : ReleaseState
    data object Unavailable : ReleaseState
    class Loaded(val release: Release) : ReleaseState
}
