package com.android.i18n.phonenumbers.prefixmapper;

import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrefixFileReader {
    private static final Logger logger = Logger.getLogger(PrefixFileReader.class.getName());
    private Map<String, PhonePrefixMap> availablePhonePrefixMaps = new HashMap();
    private MappingFileProvider mappingFileProvider = new MappingFileProvider();
    private final String phonePrefixDataDirectory;

    public PrefixFileReader(String phonePrefixDataDirectory) {
        this.phonePrefixDataDirectory = phonePrefixDataDirectory;
        loadMappingFileProvider();
    }

    private void loadMappingFileProvider() {
        IOException e;
        Throwable th;
        InputStream inputStream = null;
        try {
            InputStream in = new ObjectInputStream(PrefixFileReader.class.getResourceAsStream(this.phonePrefixDataDirectory + "config"));
            try {
                this.mappingFileProvider.readExternal(in);
                close(in);
                inputStream = in;
            } catch (IOException e2) {
                e = e2;
                inputStream = in;
                try {
                    logger.log(Level.WARNING, e.toString());
                    close(inputStream);
                } catch (Throwable th2) {
                    th = th2;
                    close(inputStream);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                inputStream = in;
                close(inputStream);
                throw th;
            }
        } catch (IOException e3) {
            e = e3;
            logger.log(Level.WARNING, e.toString());
            close(inputStream);
        }
    }

    private PhonePrefixMap getPhonePrefixDescriptions(int prefixMapKey, String language, String script, String region) {
        String fileName = this.mappingFileProvider.getFileName(prefixMapKey, language, script, region);
        if (fileName.length() == 0) {
            return null;
        }
        if (!this.availablePhonePrefixMaps.containsKey(fileName)) {
            loadPhonePrefixMapFromFile(fileName);
        }
        return (PhonePrefixMap) this.availablePhonePrefixMaps.get(fileName);
    }

    private void loadPhonePrefixMapFromFile(String fileName) {
        IOException t;
        NullPointerException e;
        Throwable th;
        InputStream inputStream = null;
        try {
            InputStream in = new ObjectInputStream(PrefixFileReader.class.getResourceAsStream(this.phonePrefixDataDirectory + fileName));
            try {
                PhonePrefixMap map = new PhonePrefixMap();
                if (in != null) {
                    map.readExternal(in);
                }
                this.availablePhonePrefixMaps.put(fileName, map);
                close(in);
                inputStream = in;
            } catch (IOException e2) {
                t = e2;
                inputStream = in;
                logger.log(Level.WARNING, t.toString());
                close(inputStream);
            } catch (NullPointerException e3) {
                e = e3;
                inputStream = in;
                try {
                    logger.log(Level.WARNING, e.toString());
                    close(inputStream);
                } catch (Throwable th2) {
                    th = th2;
                    close(inputStream);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                inputStream = in;
                close(inputStream);
                throw th;
            }
        } catch (IOException e4) {
            t = e4;
            logger.log(Level.WARNING, t.toString());
            close(inputStream);
        } catch (NullPointerException e5) {
            e = e5;
            logger.log(Level.WARNING, e.toString());
            close(inputStream);
        }
    }

    private static void close(InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, e.toString());
            }
        }
    }

    public String getDescriptionForNumber(PhoneNumber number, String lang, String script, String region) {
        int countryCallingCode = number.getCountryCode();
        int phonePrefix = countryCallingCode != 1 ? countryCallingCode : ((int) (number.getNationalNumber() / 10000000)) + 1000;
        PhonePrefixMap phonePrefixDescriptions = getPhonePrefixDescriptions(phonePrefix, lang, script, region);
        String lookup = phonePrefixDescriptions != null ? phonePrefixDescriptions.lookup(number) : null;
        if ((lookup == null || lookup.length() == 0) && mayFallBackToEnglish(lang)) {
            PhonePrefixMap defaultMap = getPhonePrefixDescriptions(phonePrefix, "en", "", "");
            if (defaultMap == null) {
                return "";
            }
            lookup = defaultMap.lookup(number);
        }
        if (lookup == null) {
            lookup = "";
        }
        return lookup;
    }

    private boolean mayFallBackToEnglish(String lang) {
        return (lang.equals("zh") || (lang.equals("ja") ^ 1) == 0) ? false : lang.equals("ko") ^ 1;
    }
}
