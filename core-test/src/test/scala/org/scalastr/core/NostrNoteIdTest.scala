package org.scalastr.core

import org.bitcoins.crypto.Sha256Digest
import org.bitcoins.testkitcore.util.BitcoinSUnitTest

class NostrNoteIdTest extends BitcoinSUnitTest {

  it must "parse a note id" in {
    val noteId =
      "note1mkwrdfwfayc3z39hw8k2k73pgqygd8xj0t4fpqtqvj7maqc8mcusp5pvth"
    val hash =
      "dd9c36a5c9e9311144b771ecab7a214008869cd27aea90816064bdbe8307de39"

    val parsed = NostrNoteId.fromString(noteId)
    assert(parsed.id.hex == hash)
  }

  it must "create a note id" in {
    val noteId =
      "note1mkwrdfwfayc3z39hw8k2k73pgqygd8xj0t4fpqtqvj7maqc8mcusp5pvth"
    val hash =
      "dd9c36a5c9e9311144b771ecab7a214008869cd27aea90816064bdbe8307de39"

    val nostrNoteId = NostrNoteId(Sha256Digest(hash))
    assert(nostrNoteId.toString == noteId)
  }
}
