package app.vercors.shared.backend

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class CacheTest {

    @Test
    fun `success response gets a public Cache-Control header`() = testApplication {
        routing {
            get("/ok") {
                call.response.cache(1.hours)
                call.respond(HttpStatusCode.OK)
            }
        }

        val response = client.get("/ok")

        assertEquals("max-age=3600, public", response.headers[HttpHeaders.CacheControl])
    }

    @Test
    fun `redirect response keeps both Location and Cache-Control headers`() = testApplication {
        routing {
            get("/redirect") {
                call.response.cache(1.hours)
                call.respondRedirect("https://example.com")
            }
        }
        val client = createClient { followRedirects = false }

        val response = client.get("/redirect")

        assertEquals(HttpStatusCode.Found, response.status)
        assertEquals("https://example.com", response.headers[HttpHeaders.Location])
        assertEquals("max-age=3600, public", response.headers[HttpHeaders.CacheControl])
    }

    @Test
    fun `sibling cache blocks with different durations can coexist`() = testApplication {
        routing {
            get("/hourly") {
                call.response.cache(1.hours)
                call.respond(HttpStatusCode.OK)
            }
            get("/minutely") {
                call.response.cache(1.minutes)
                call.respond(HttpStatusCode.OK)
            }
        }

        assertEquals("max-age=3600, public", client.get("/hourly").headers[HttpHeaders.CacheControl])
        assertEquals("max-age=60, public", client.get("/minutely").headers[HttpHeaders.CacheControl])
    }
}
