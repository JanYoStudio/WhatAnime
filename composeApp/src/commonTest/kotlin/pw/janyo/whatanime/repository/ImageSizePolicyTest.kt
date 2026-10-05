package pw.janyo.whatanime.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ImageSizePolicyTest {
    @Test fun exact25MiBIsAcceptedButNextByteIsRejected() {
        assertNull(imageSizeFailure(25L * 1024 * 1024))
        assertEquals(SearchFailure.TooLarge, imageSizeFailure(25L * 1024 * 1024 + 1))
    }

    @Test fun unavailableSizeIsNotMistakenForASmallValidFile() {
        assertEquals(SearchFailure.FileUnavailable, imageSizeFailure(-1))
    }
}
