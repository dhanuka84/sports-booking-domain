package com.sportsbook.bettingservice.outbox

import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificDatumWriter
import org.apache.avro.specific.SpecificRecord
import java.io.ByteArrayOutputStream

object OutboxUtil {
    @Throws(Exception::class)
    fun <T : SpecificRecord> avroBytes(record: T): ByteArray {
        val out = ByteArrayOutputStream()
        val writer = SpecificDatumWriter<T>(record.schema)
        val enc = EncoderFactory.get().binaryEncoder(out, null)
        writer.write(record, enc)
        enc.flush()
        return out.toByteArray()
    }
}
