package pw.janyo.whatanime.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AnimationTest {

    @Test
    fun testParseSearchAnimeResult_withNullIdMal() {
        val jsonString = """
            {
                "frameCount": 745506,
                "error": "",
                "result": [
                    {
                        "anilist": {
                            "id": 99939,
                            "idMal": null, 
                            "title": {
                                "native": "ネコぱらOVA",
                                "romaji": "Nekopara OVA",
                                "english": null
                            },
                            "synonyms": ["Neko Para OVA"],
                            "isAdult": false
                        },
                        "filename": "Nekopara - OVA (BD 1280x720 x264 AAC).mp4",
                        "episode": null,
                        "from": 97.75,
                        "to": 98.92,
                        "similarity": 0.9440424588727485,
                        "video": "https://api.trace.moe/video/99939/Nekopara%20-%20OVA%20(BD%201280x720%20x264%20AAC).mp4?t=98.335&now=1653892514&token=xxxxxxxxxxxxxx",
                        "image": "https://api.trace.moe/image/99939/Nekopara%20-%20OVA%20(BD%201280x720%20x264%20AAC).mp4.jpg?t=98.335&now=1653892514&token=xxxxxxxxxxxxxx"
                    }
                ]
            }
        """.trimIndent()

        var parsedResult: SearchAnimeResult? = null
        var exception: Exception? = null
        
        try {
            parsedResult = searchAnimeResultAdapter.fromJson(jsonString)
        } catch (e: Exception) {
            exception = e
        }

        assertNull("Parsing should not throw an exception", exception)
        assertNotNull("Parsed result should not be null", parsedResult)
        assertEquals("Result list should contain one item", 1, parsedResult?.result?.size)
        
        val firstResultItem = parsedResult?.result?.get(0)
        assertNotNull("First result item should not be null", firstResultItem)
        
        val aniList = firstResultItem?.aniList
        assertNotNull("AniList object should not be null", aniList)
        assertNull("idMal should be null", aniList?.idMal)
        assertEquals("AniList ID should be 99939L", 99939L, aniList?.id) // Verify other fields are parsed
    }
}
