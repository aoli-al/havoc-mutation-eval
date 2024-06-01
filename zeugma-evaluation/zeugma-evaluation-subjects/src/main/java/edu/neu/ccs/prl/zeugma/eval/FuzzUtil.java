package edu.neu.ccs.prl.zeugma.eval;

import org.w3c.dom.Document;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

public final class FuzzUtil {
    private static final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    private FuzzUtil() {
        throw new AssertionError();
    }

    public static InputStream toInputStream(Document document) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            transformerFactory.newTransformer().transform(new DOMSource(document), new StreamResult(out));
            return new ByteArrayInputStream(out.toByteArray());
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    public static PrintStream suppressStandardErr() {
        PrintStream result = System.err;
        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
            }
        }));
        return result;
    }
}
