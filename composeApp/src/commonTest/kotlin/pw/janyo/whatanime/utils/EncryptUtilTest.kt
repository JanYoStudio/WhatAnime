package pw.janyo.whatanime.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class EncryptUtilTest {
    @Test
    fun md5MatchesKnownVectorsAndKeepsLeadingZeroes() {
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", "".md5())
        assertEquals("900150983cd24fb0d6963f7d28e17f72", "abc".md5())
        assertEquals("0cc175b9c0f1b6a831c399e269772661", "a".md5())
    }

    @Test
    fun shaAlgorithmsMatchKnownAbcVectors() {
        assertEquals("a9993e364706816aba3e25717850c26c9cd0d89d", "abc".sha1())
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", "abc".sha256())
        assertEquals(
            "cb00753f45a35e8bb5a03d699ac65007272c32ab0eded1631a8b605a43ff5bed" +
                "8086072ba1e7cc2358baeca134c825a7",
            "abc".sha384(),
        )
        assertEquals(
            "ddaf35a193617abacc417349ae20413112e6fa4e89a97ea20a9eeee64b55d39a" +
                "2192992a274fc1a836ba3c23a3feebbd454d4423643ce80e2a9ac94fa54ca49f",
            "abc".sha512(),
        )
    }
}
