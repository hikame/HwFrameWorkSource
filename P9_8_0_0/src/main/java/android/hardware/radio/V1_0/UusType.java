package android.hardware.radio.V1_0;

public final class UusType {
    public static final int TYPE1_IMPLICIT = 0;
    public static final int TYPE1_NOT_REQUIRED = 2;
    public static final int TYPE1_REQUIRED = 1;
    public static final int TYPE2_NOT_REQUIRED = 4;
    public static final int TYPE2_REQUIRED = 3;
    public static final int TYPE3_NOT_REQUIRED = 6;
    public static final int TYPE3_REQUIRED = 5;

    public static final java.lang.String dumpBitfield(int r1) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.radio.V1_0.UusType.dumpBitfield(int):java.lang.String
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:31)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:56)
	at jadx.core.ProcessClass.process(ProcessClass.java:39)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:296)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.radio.V1_0.UusType.dumpBitfield(int):java.lang.String");
    }

    public static final String toString(int o) {
        if (o == 0) {
            return "TYPE1_IMPLICIT";
        }
        if (o == 1) {
            return "TYPE1_REQUIRED";
        }
        if (o == 2) {
            return "TYPE1_NOT_REQUIRED";
        }
        if (o == 3) {
            return "TYPE2_REQUIRED";
        }
        if (o == 4) {
            return "TYPE2_NOT_REQUIRED";
        }
        if (o == 5) {
            return "TYPE3_REQUIRED";
        }
        if (o == 6) {
            return "TYPE3_NOT_REQUIRED";
        }
        return "0x" + Integer.toHexString(o);
    }
}
