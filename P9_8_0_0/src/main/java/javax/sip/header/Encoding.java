package javax.sip.header;

import java.text.ParseException;

public interface Encoding {
    String getEncoding();

    void setEncoding(String str) throws ParseException;
}
