package com.sportsbook.bettingservice.outbox;

import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;

import java.io.ByteArrayOutputStream;

public class OutboxUtil {
    public static <T extends SpecificRecord> byte[] avroBytes(T record) throws Exception {
        var out = new ByteArrayOutputStream();
        var w = new SpecificDatumWriter<T>(record.getSchema());
        var enc = EncoderFactory.get().binaryEncoder(out, null);
        w.write(record, enc);
        enc.flush();
        return out.toByteArray();
    }
}