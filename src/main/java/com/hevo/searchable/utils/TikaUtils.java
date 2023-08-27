package com.hevo.searchable.utils;

import lombok.extern.log4j.Log4j2;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Log4j2
public class TikaUtils {
    public static String parseDocument(File file) throws IOException, TikaException, SAXException {
        try(InputStream stream = new FileInputStream(file)){
            Parser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler();
            ParseContext context = new ParseContext();
            Metadata metadata = new Metadata();
            parser.parse(stream, handler, metadata, context);
            return handler.toString();
        }
    }
}
