package app.vercors.website.data

import app.vercors.website.GITHUB_REPO
import app.vercors.website.data.dto.GitHubRelease
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

// /releases/latest skips pre-releases, which every release currently is
suspend fun fetchLatestRelease(): Release? {
    val response = window.fetch("https://api.github.com/repos/$GITHUB_REPO/releases?per_page=10").await()
    if (!response.ok) return null
    val releases = json.decodeFromString<List<GitHubRelease>>(response.text().await())
    return releases.firstOrNull { !it.draft }?.toRelease()
}
