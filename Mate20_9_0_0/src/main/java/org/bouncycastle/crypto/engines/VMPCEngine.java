package org.bouncycastle.crypto.engines;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

public class VMPCEngine implements StreamCipher {
    protected byte[] P = null;
    protected byte n = (byte) 0;
    protected byte s = (byte) 0;
    protected byte[] workingIV;
    protected byte[] workingKey;

    public String getAlgorithmName() {
        return "VMPC";
    }

    public void init(boolean z, CipherParameters cipherParameters) {
        if (cipherParameters instanceof ParametersWithIV) {
            ParametersWithIV parametersWithIV = (ParametersWithIV) cipherParameters;
            if (parametersWithIV.getParameters() instanceof KeyParameter) {
                KeyParameter keyParameter = (KeyParameter) parametersWithIV.getParameters();
                this.workingIV = parametersWithIV.getIV();
                if (this.workingIV == null || this.workingIV.length < 1 || this.workingIV.length > 768) {
                    throw new IllegalArgumentException("VMPC requires 1 to 768 bytes of IV");
                }
                this.workingKey = keyParameter.getKey();
                initKey(this.workingKey, this.workingIV);
                return;
            }
            throw new IllegalArgumentException("VMPC init parameters must include a key");
        }
        throw new IllegalArgumentException("VMPC init parameters must include an IV");
    }

    protected void initKey(byte[] bArr, byte[] bArr2) {
        int i;
        this.s = (byte) 0;
        this.P = new byte[256];
        for (int i2 = 0; i2 < 256; i2++) {
            this.P[i2] = (byte) i2;
        }
        for (int i3 = 0; i3 < 768; i3++) {
            i = i3 & 255;
            this.s = this.P[((this.s + this.P[i]) + bArr[i3 % bArr.length]) & 255];
            byte b = this.P[i];
            this.P[i] = this.P[this.s & 255];
            this.P[this.s & 255] = b;
        }
        for (int i4 = 0; i4 < 768; i4++) {
            i = i4 & 255;
            this.s = this.P[((this.s + this.P[i]) + bArr2[i4 % bArr2.length]) & 255];
            byte b2 = this.P[i];
            this.P[i] = this.P[this.s & 255];
            this.P[this.s & 255] = b2;
        }
        this.n = (byte) 0;
    }

    public int processBytes(byte[] bArr, int i, int i2, byte[] bArr2, int i3) {
        if (i + i2 > bArr.length) {
            throw new DataLengthException("input buffer too short");
        } else if (i3 + i2 <= bArr2.length) {
            for (int i4 = 0; i4 < i2; i4++) {
                this.s = this.P[(this.s + this.P[this.n & 255]) & 255];
                byte b = this.P[(this.P[this.P[this.s & 255] & 255] + 1) & 255];
                byte b2 = this.P[this.n & 255];
                this.P[this.n & 255] = this.P[this.s & 255];
                this.P[this.s & 255] = b2;
                this.n = (byte) ((this.n + 1) & 255);
                bArr2[i4 + i3] = (byte) (b ^ bArr[i4 + i]);
            }
            return i2;
        } else {
            throw new OutputLengthException("output buffer too short");
        }
    }

    public void reset() {
        initKey(this.workingKey, this.workingIV);
    }

    public byte returnByte(byte b) {
        this.s = this.P[(this.s + this.P[this.n & 255]) & 255];
        byte b2 = this.P[(this.P[this.P[this.s & 255] & 255] + 1) & 255];
        byte b3 = this.P[this.n & 255];
        this.P[this.n & 255] = this.P[this.s & 255];
        this.P[this.s & 255] = b3;
        this.n = (byte) ((this.n + 1) & 255);
        return (byte) (b ^ b2);
    }
}
