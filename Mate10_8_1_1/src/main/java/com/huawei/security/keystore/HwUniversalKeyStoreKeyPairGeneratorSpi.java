package com.huawei.security.keystore;

import android.support.annotation.Nullable;
import android.util.Log;
import com.android.internal.util.ArrayUtils;
import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1InputStream;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.DERBitString;
import com.android.org.bouncycastle.asn1.DERInteger;
import com.android.org.bouncycastle.asn1.DERNull;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.asn1.x509.Certificate;
import com.android.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import com.android.org.bouncycastle.asn1.x509.TBSCertificate;
import com.android.org.bouncycastle.asn1.x509.Time;
import com.android.org.bouncycastle.asn1.x509.V3TBSCertificateGenerator;
import com.android.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import com.android.org.bouncycastle.jce.X509Principal;
import com.android.org.bouncycastle.jce.provider.X509CertificateObject;
import com.android.org.bouncycastle.x509.X509V3CertificateGenerator;
import com.huawei.security.HwCredentials;
import com.huawei.security.HwKeystoreManager;
import com.huawei.security.HwKeystoreManager.State;
import com.huawei.security.keymaster.HwKeyCharacteristics;
import com.huawei.security.keymaster.HwKeymasterArguments;
import com.huawei.security.keymaster.HwKeymasterBlob;
import com.huawei.security.keymaster.HwKeymasterCertificateChain;
import com.huawei.security.keymaster.HwKeymasterDefs;
import com.huawei.security.keymaster.HwKeymasterUtils;
import com.huawei.security.keystore.HwKeyProperties.Digest;
import com.huawei.security.keystore.HwKeyProperties.SignaturePadding;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGeneratorSpi;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class HwUniversalKeyStoreKeyPairGeneratorSpi extends KeyPairGeneratorSpi {
    private static final int RSA_DEFAULT_KEY_SIZE = 2048;
    private static final int RSA_MAX_KEY_SIZE = 4096;
    private static final int RSA_MIN_KEY_SIZE = 512;
    public static final String TAG = "HwKeyPairGenerator";
    private boolean mEncryptionAtRestRequired;
    private String mEntryAlias;
    private int mEntryUid;
    private int mKeySizeBits;
    private HwKeystoreManager mKeyStore;
    private int mKeymasterAlgorithm = -1;
    private int[] mKeymasterBlockModes;
    private int[] mKeymasterDigests;
    private int[] mKeymasterEncryptionPaddings;
    private int[] mKeymasterPurposes;
    private int[] mKeymasterSignaturePaddings;
    private int mOriginalKeymasterAlgorithm;
    private BigInteger mRSAPublicExponent;
    private SecureRandom mRng;
    private HwKeyGenParameterSpec mSpec;

    public static class RSA extends HwUniversalKeyStoreKeyPairGeneratorSpi {
        public RSA() {
            super(1);
        }
    }

    protected HwUniversalKeyStoreKeyPairGeneratorSpi(int keymasterAlgorithm) {
        this.mOriginalKeymasterAlgorithm = keymasterAlgorithm;
    }

    public void initialize(int keysize, SecureRandom random) {
        throw new IllegalArgumentException(HwKeyGenParameterSpec.class.getName() + " required to initialize this HwKeyPairGenerator");
    }

    public void initialize(java.security.spec.AlgorithmParameterSpec r9, java.security.SecureRandom r10) throws java.security.InvalidAlgorithmParameterException {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.huawei.security.keystore.HwUniversalKeyStoreKeyPairGeneratorSpi.initialize(java.security.spec.AlgorithmParameterSpec, java.security.SecureRandom):void. bs: [B:2:0x000f, B:25:0x00ba]
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:86)
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:296)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:199)
*/
        /*
        r8 = this;
        r8.resetAll();
        r5 = "HwKeyPairGenerator";
        r6 = "HwUniversalKeyStoreKeyPairGeneratorSpi initialize";
        android.util.Log.e(r5, r6);
        r4 = 0;
        if (r9 != 0) goto L_0x0036;
    L_0x000f:
        r5 = new java.security.InvalidAlgorithmParameterException;	 Catch:{ all -> 0x002f }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x002f }
        r6.<init>();	 Catch:{ all -> 0x002f }
        r7 = "Must supply params of type ";	 Catch:{ all -> 0x002f }
        r6 = r6.append(r7);	 Catch:{ all -> 0x002f }
        r7 = com.huawei.security.keystore.HwKeyGenParameterSpec.class;	 Catch:{ all -> 0x002f }
        r7 = r7.getName();	 Catch:{ all -> 0x002f }
        r6 = r6.append(r7);	 Catch:{ all -> 0x002f }
        r6 = r6.toString();	 Catch:{ all -> 0x002f }
        r5.<init>(r6);	 Catch:{ all -> 0x002f }
        throw r5;	 Catch:{ all -> 0x002f }
    L_0x002f:
        r5 = move-exception;
        if (r4 != 0) goto L_0x0035;
    L_0x0032:
        r8.resetAll();
    L_0x0035:
        throw r5;
    L_0x0036:
        r2 = r8.mOriginalKeymasterAlgorithm;	 Catch:{ all -> 0x002f }
        r5 = r9 instanceof com.huawei.security.keystore.HwKeyGenParameterSpec;	 Catch:{ all -> 0x002f }
        if (r5 == 0) goto L_0x007c;	 Catch:{ all -> 0x002f }
    L_0x003c:
        r0 = r9;	 Catch:{ all -> 0x002f }
        r0 = (com.huawei.security.keystore.HwKeyGenParameterSpec) r0;	 Catch:{ all -> 0x002f }
        r3 = r0;	 Catch:{ all -> 0x002f }
    L_0x0040:
        r5 = r3.getKeystoreAlias();	 Catch:{ all -> 0x002f }
        r5 = r8.getEntryAlias(r5);	 Catch:{ all -> 0x002f }
        r8.mEntryAlias = r5;	 Catch:{ all -> 0x002f }
        r5 = r3.getUid();	 Catch:{ all -> 0x002f }
        r8.mEntryUid = r5;	 Catch:{ all -> 0x002f }
        r8.mSpec = r3;	 Catch:{ all -> 0x002f }
        r8.mKeymasterAlgorithm = r2;	 Catch:{ all -> 0x002f }
        r5 = r3.getKeySize();	 Catch:{ all -> 0x002f }
        r8.mKeySizeBits = r5;	 Catch:{ all -> 0x002f }
        r8.initAlgorithmSpecificParameters();	 Catch:{ all -> 0x002f }
        r5 = r8.mKeySizeBits;	 Catch:{ all -> 0x002f }
        r6 = -1;	 Catch:{ all -> 0x002f }
        if (r5 != r6) goto L_0x0068;	 Catch:{ all -> 0x002f }
    L_0x0062:
        r5 = getDefaultKeySize(r2);	 Catch:{ all -> 0x002f }
        r8.mKeySizeBits = r5;	 Catch:{ all -> 0x002f }
    L_0x0068:
        r5 = r8.mKeySizeBits;	 Catch:{ all -> 0x002f }
        checkValidKeySize(r2, r5);	 Catch:{ all -> 0x002f }
        r5 = r3.getKeystoreAlias();	 Catch:{ all -> 0x002f }
        if (r5 != 0) goto L_0x00ba;	 Catch:{ all -> 0x002f }
    L_0x0073:
        r5 = new java.security.InvalidAlgorithmParameterException;	 Catch:{ all -> 0x002f }
        r6 = "KeyStore entry alias not provided";	 Catch:{ all -> 0x002f }
        r5.<init>(r6);	 Catch:{ all -> 0x002f }
        throw r5;	 Catch:{ all -> 0x002f }
    L_0x007c:
        r5 = r9 instanceof android.security.keystore.KeyGenParameterSpec;	 Catch:{ all -> 0x002f }
        if (r5 == 0) goto L_0x0087;	 Catch:{ all -> 0x002f }
    L_0x0080:
        r9 = (android.security.keystore.KeyGenParameterSpec) r9;	 Catch:{ all -> 0x002f }
        r3 = com.huawei.security.keystore.HwKeyGenParameterSpec.getInstance(r9);	 Catch:{ all -> 0x002f }
        goto L_0x0040;	 Catch:{ all -> 0x002f }
    L_0x0087:
        r5 = new java.security.InvalidAlgorithmParameterException;	 Catch:{ all -> 0x002f }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x002f }
        r6.<init>();	 Catch:{ all -> 0x002f }
        r7 = "Unsupported params class: ";	 Catch:{ all -> 0x002f }
        r6 = r6.append(r7);	 Catch:{ all -> 0x002f }
        r7 = r9.getClass();	 Catch:{ all -> 0x002f }
        r7 = r7.getName();	 Catch:{ all -> 0x002f }
        r6 = r6.append(r7);	 Catch:{ all -> 0x002f }
        r7 = ". Supported: ";	 Catch:{ all -> 0x002f }
        r6 = r6.append(r7);	 Catch:{ all -> 0x002f }
        r7 = com.huawei.security.keystore.HwKeyGenParameterSpec.class;	 Catch:{ all -> 0x002f }
        r7 = r7.getName();	 Catch:{ all -> 0x002f }
        r6 = r6.append(r7);	 Catch:{ all -> 0x002f }
        r6 = r6.toString();	 Catch:{ all -> 0x002f }
        r5.<init>(r6);	 Catch:{ all -> 0x002f }
        throw r5;	 Catch:{ all -> 0x002f }
    L_0x00ba:
        r5 = r3.getPurposes();	 Catch:{ IllegalArgumentException -> 0x0101, IllegalArgumentException -> 0x0101 }
        r5 = com.huawei.security.keystore.HwKeyProperties.Purpose.allToKeymaster(r5);	 Catch:{ IllegalArgumentException -> 0x0101, IllegalArgumentException -> 0x0101 }
        r8.mKeymasterPurposes = r5;	 Catch:{ IllegalArgumentException -> 0x0101, IllegalArgumentException -> 0x0101 }
        r5 = r3.getBlockModes();	 Catch:{ IllegalArgumentException -> 0x0101, IllegalArgumentException -> 0x0101 }
        r5 = com.huawei.security.keystore.HwKeyProperties.BlockMode.allToKeymaster(r5);	 Catch:{ IllegalArgumentException -> 0x0101, IllegalArgumentException -> 0x0101 }
        r8.mKeymasterBlockModes = r5;	 Catch:{ IllegalArgumentException -> 0x0101, IllegalArgumentException -> 0x0101 }
        r5 = r3.getEncryptionPaddings();	 Catch:{ IllegalArgumentException -> 0x0101, IllegalArgumentException -> 0x0101 }
        r5 = com.huawei.security.keystore.HwKeyProperties.EncryptionPadding.allToKeymaster(r5);	 Catch:{ IllegalArgumentException -> 0x0101, IllegalArgumentException -> 0x0101 }
        r8.mKeymasterEncryptionPaddings = r5;	 Catch:{ IllegalArgumentException -> 0x0101, IllegalArgumentException -> 0x0101 }
        r5 = r3.getSignaturePaddings();	 Catch:{ IllegalArgumentException -> 0x0101, IllegalArgumentException -> 0x0101 }
        r5 = com.huawei.security.keystore.HwKeyProperties.SignaturePadding.allToKeymaster(r5);	 Catch:{ IllegalArgumentException -> 0x0101, IllegalArgumentException -> 0x0101 }
        r8.mKeymasterSignaturePaddings = r5;	 Catch:{ IllegalArgumentException -> 0x0101, IllegalArgumentException -> 0x0101 }
        r5 = r3.isDigestsSpecified();	 Catch:{ IllegalArgumentException -> 0x0101, IllegalArgumentException -> 0x0101 }
        if (r5 == 0) goto L_0x00f2;	 Catch:{ IllegalArgumentException -> 0x0101, IllegalArgumentException -> 0x0101 }
    L_0x00e8:
        r5 = r3.getDigests();	 Catch:{ IllegalArgumentException -> 0x0101, IllegalArgumentException -> 0x0101 }
        r5 = com.huawei.security.keystore.HwKeyProperties.Digest.allToKeymaster(r5);	 Catch:{ IllegalArgumentException -> 0x0101, IllegalArgumentException -> 0x0101 }
        r8.mKeymasterDigests = r5;	 Catch:{ IllegalArgumentException -> 0x0101, IllegalArgumentException -> 0x0101 }
    L_0x00f2:
        r8.mRng = r10;	 Catch:{ all -> 0x002f }
        r5 = com.huawei.security.HwKeystoreManager.getInstance();	 Catch:{ all -> 0x002f }
        r8.mKeyStore = r5;	 Catch:{ all -> 0x002f }
        r4 = 1;
        if (r4 != 0) goto L_0x0100;
    L_0x00fd:
        r8.resetAll();
    L_0x0100:
        return;
    L_0x0101:
        r1 = move-exception;
        r5 = new java.security.InvalidAlgorithmParameterException;	 Catch:{ all -> 0x002f }
        r5.<init>(r1);	 Catch:{ all -> 0x002f }
        throw r5;	 Catch:{ all -> 0x002f }
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.security.keystore.HwUniversalKeyStoreKeyPairGeneratorSpi.initialize(java.security.spec.AlgorithmParameterSpec, java.security.SecureRandom):void");
    }

    private void initAlgorithmSpecificParameters() throws InvalidAlgorithmParameterException {
        AlgorithmParameterSpec algSpecificSpec = this.mSpec.getAlgorithmParameterSpec();
        switch (this.mKeymasterAlgorithm) {
            case 1:
                BigInteger publicExponent = null;
                if (algSpecificSpec instanceof RSAKeyGenParameterSpec) {
                    RSAKeyGenParameterSpec rsaSpec = (RSAKeyGenParameterSpec) algSpecificSpec;
                    if (this.mKeySizeBits == -1) {
                        this.mKeySizeBits = rsaSpec.getKeysize();
                    } else if (this.mKeySizeBits != rsaSpec.getKeysize()) {
                        throw new InvalidAlgorithmParameterException("RSA key size must match  between " + this.mSpec + " and " + algSpecificSpec + ": " + this.mKeySizeBits + " vs " + rsaSpec.getKeysize());
                    }
                    publicExponent = rsaSpec.getPublicExponent();
                } else if (algSpecificSpec != null) {
                    throw new InvalidAlgorithmParameterException("RSA may only use RSAKeyGenParameterSpec");
                }
                if (publicExponent == null) {
                    publicExponent = RSAKeyGenParameterSpec.F4;
                }
                if (publicExponent.compareTo(BigInteger.ZERO) < 1) {
                    throw new InvalidAlgorithmParameterException("RSA public exponent must be positive: " + publicExponent);
                } else if (publicExponent.compareTo(HwKeymasterArguments.UINT64_MAX_VALUE) > 0) {
                    throw new InvalidAlgorithmParameterException("Unsupported RSA public exponent: " + publicExponent + ". Maximum supported value: " + HwKeymasterArguments.UINT64_MAX_VALUE);
                } else {
                    this.mRSAPublicExponent = publicExponent;
                    return;
                }
            default:
                throw new ProviderException("Unsupported algorithm: " + this.mKeymasterAlgorithm);
        }
    }

    private static void checkValidKeySize(int keymasterAlgorithm, int keySize) throws InvalidAlgorithmParameterException {
        switch (keymasterAlgorithm) {
            case 1:
                if (keySize < RSA_MIN_KEY_SIZE || keySize > RSA_MAX_KEY_SIZE) {
                    throw new InvalidAlgorithmParameterException("RSA key size must be >= 512 and <= 4096");
                }
                return;
            default:
                throw new ProviderException("Unsupported algorithm: " + keymasterAlgorithm);
        }
    }

    private static int getDefaultKeySize(int keymasterAlgorithm) {
        switch (keymasterAlgorithm) {
            case 1:
                return RSA_DEFAULT_KEY_SIZE;
            default:
                throw new ProviderException("Unsupported algorithm: " + keymasterAlgorithm);
        }
    }

    public KeyPair generateKeyPair() {
        KeyPair keyPair = null;
        if (this.mKeyStore == null || this.mSpec == null) {
            throw new IllegalStateException("Not initialized");
        }
        int flags = this.mEncryptionAtRestRequired ? 1 : 0;
        if ((flags & 1) == 0 || this.mKeyStore.state() == State.UNLOCKED) {
            byte[] additionalEntropy = getRandomBytesToMixIntoKeystoreRng(this.mRng, (this.mKeySizeBits + 7) / 8);
            HwCredentials.deleteAllTypesForAlias(this.mKeyStore, this.mEntryAlias, this.mEntryUid);
            String privateKeyAlias = HwCredentials.USER_PRIVATE_KEY + this.mEntryAlias;
            boolean success = false;
            try {
                generateKeystoreKeyPair(privateKeyAlias, constructKeyGenerationArguments(), additionalEntropy, flags);
                KeyPair keyPair2 = loadKeystoreKeyPair(privateKeyAlias);
                byte[] certChainBytes = createCertificateChainBytes(privateKeyAlias, keyPair2);
                if (certChainBytes == null) {
                    Log.e(TAG, "generateKeyPair failed, CertificateChain is null!");
                    success = false;
                    return keyPair;
                }
                storeCertificateChainBytes(flags, certChainBytes);
                Log.i(TAG, "generateKeyPair successed");
                if (!true) {
                    HwCredentials.deleteAllTypesForAlias(this.mKeyStore, this.mEntryAlias, this.mEntryUid);
                }
                return keyPair2;
            } finally {
                if (!success) {
                    HwKeystoreManager hwKeystoreManager = this.mKeyStore;
                    String str = this.mEntryAlias;
                    keyPair = this.mEntryUid;
                    HwCredentials.deleteAllTypesForAlias(hwKeystoreManager, str, keyPair);
                }
            }
        } else {
            throw new IllegalStateException("Encryption at rest using secure lock screen credential requested for key pair, but the user has not yet entered the credential");
        }
    }

    private void storeCertificateChainBytes(int flags, byte[] bytes) throws ProviderException {
        if (bytes == null) {
            throw new ProviderException("Input param is invalid.");
        }
        int insertErrorCode = this.mKeyStore.set(HwCredentials.CERTIFICATE_CHAIN + this.mEntryAlias, new HwKeymasterBlob(bytes), this.mEntryUid);
        if (insertErrorCode != 1) {
            throw new ProviderException("Failed to store attestation certificate chain", HwKeystoreManager.getKeyStoreException(insertErrorCode));
        }
    }

    private void storeCertificateChain(int flags, Iterable<byte[]> iterable) throws ProviderException {
        if (iterable == null) {
            throw new ProviderException("Input param is invalid.");
        }
        Iterator<byte[]> iter = iterable.iterator();
        storeCertificate(HwCredentials.USER_CERTIFICATE, (byte[]) iter.next(), flags, "Failed to store certificate");
        if (iter.hasNext()) {
            ByteArrayOutputStream certificateConcatenationStream = new ByteArrayOutputStream();
            while (iter.hasNext()) {
                byte[] data = (byte[]) iter.next();
                certificateConcatenationStream.write(data, 0, data.length);
            }
            storeCertificate(HwCredentials.CA_CERTIFICATE, certificateConcatenationStream.toByteArray(), flags, "Failed to store attestation CA certificate");
        }
    }

    private void storeCertificate(String prefix, byte[] certificateBytes, int flags, String failureMessage) throws ProviderException {
        if (certificateBytes == null) {
            Log.e(TAG, "storeCertificate certificateBytes is null");
            return;
        }
        int insertErrorCode = this.mKeyStore.set(prefix + this.mEntryAlias, new HwKeymasterBlob(certificateBytes), this.mEntryUid);
        if (insertErrorCode != 1) {
            throw new ProviderException(failureMessage, HwKeystoreManager.getKeyStoreException(insertErrorCode));
        }
    }

    private Iterable<byte[]> createCertificateChain(String privateKeyAlias, KeyPair keyPair) throws ProviderException {
        byte[] challenge = this.mSpec.getAttestationChallenge();
        if (challenge == null) {
            return Collections.singleton(generateSelfSignedCertificateBytes(keyPair));
        }
        HwKeymasterArguments args = new HwKeymasterArguments();
        args.addBytes(HwKeymasterDefs.KM_TAG_ATTESTATION_CHALLENGE, challenge);
        return getAttestationChain(privateKeyAlias, keyPair, args);
    }

    private byte[] createCertificateChainBytes(String privateKeyAlias, KeyPair keyPair) throws ProviderException {
        byte[] challenge = getChallenge(this.mSpec);
        if (challenge == null) {
            return generateSelfSignedCertificateBytes(keyPair);
        }
        HwKeymasterArguments args = new HwKeymasterArguments();
        args.addBytes(HwKeymasterDefs.KM_TAG_ATTESTATION_CHALLENGE, challenge);
        return getAttestationChainBytes(privateKeyAlias, keyPair, args);
    }

    private byte[] generateSelfSignedCertificateBytes(KeyPair keyPair) throws ProviderException {
        try {
            return generateSelfSignedCertificate(keyPair.getPrivate(), keyPair.getPublic()).getEncoded();
        } catch (Exception e) {
            throw new ProviderException("Failed to generate self-signed certificate", e);
        } catch (CertificateEncodingException e2) {
            throw new ProviderException("Failed to obtain encoded form of self-signed certificate", e2);
        }
    }

    private X509Certificate generateSelfSignedCertificate(PrivateKey privateKey, PublicKey publicKey) throws CertificateParsingException, IOException {
        String signatureAlgorithm = getCertificateSignatureAlgorithm(this.mKeymasterAlgorithm, this.mKeySizeBits, this.mSpec);
        if (signatureAlgorithm == null) {
            return generateSelfSignedCertificateWithFakeSignature(publicKey);
        }
        try {
            return generateSelfSignedCertificateWithValidSignature(privateKey, publicKey, signatureAlgorithm);
        } catch (Exception e) {
            return generateSelfSignedCertificateWithFakeSignature(publicKey);
        }
    }

    private X509Certificate generateSelfSignedCertificateWithValidSignature(PrivateKey privateKey, PublicKey publicKey, String signatureAlgorithm) throws Exception {
        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
        certGen.setPublicKey(publicKey);
        certGen.setSerialNumber(this.mSpec.getCertificateSerialNumber());
        certGen.setSubjectDN(this.mSpec.getCertificateSubject());
        certGen.setIssuerDN(this.mSpec.getCertificateSubject());
        certGen.setNotBefore(this.mSpec.getCertificateNotBefore());
        certGen.setNotAfter(this.mSpec.getCertificateNotAfter());
        certGen.setSignatureAlgorithm(signatureAlgorithm);
        return certGen.generate(privateKey);
    }

    private X509Certificate generateSelfSignedCertificateWithFakeSignature(PublicKey publicKey) throws IOException, CertificateParsingException {
        AlgorithmIdentifier sigAlgId;
        byte[] signature;
        Throwable th;
        V3TBSCertificateGenerator tbsGenerator = new V3TBSCertificateGenerator();
        switch (this.mKeymasterAlgorithm) {
            case 1:
                sigAlgId = new AlgorithmIdentifier(PKCSObjectIdentifiers.sha256WithRSAEncryption, DERNull.INSTANCE);
                signature = new byte[1];
                break;
            case 3:
                sigAlgId = new AlgorithmIdentifier(X9ObjectIdentifiers.ecdsa_with_SHA256);
                ASN1EncodableVector v = new ASN1EncodableVector();
                v.add(new DERInteger(0));
                v.add(new DERInteger(0));
                signature = new DERSequence().getEncoded();
                break;
            default:
                throw new ProviderException("Unsupported key algorithm: " + this.mKeymasterAlgorithm);
        }
        Throwable th2 = null;
        ASN1InputStream aSN1InputStream = null;
        try {
            ASN1InputStream publicKeyInfoIn = new ASN1InputStream(publicKey.getEncoded());
            try {
                tbsGenerator.setSubjectPublicKeyInfo(SubjectPublicKeyInfo.getInstance(publicKeyInfoIn.readObject()));
                if (publicKeyInfoIn != null) {
                    try {
                        publicKeyInfoIn.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    throw th2;
                }
                tbsGenerator.setSerialNumber(new ASN1Integer(this.mSpec.getCertificateSerialNumber()));
                X509Principal subject = new X509Principal(this.mSpec.getCertificateSubject().getEncoded());
                tbsGenerator.setSubject(subject);
                tbsGenerator.setIssuer(subject);
                tbsGenerator.setStartDate(new Time(this.mSpec.getCertificateNotBefore()));
                tbsGenerator.setEndDate(new Time(this.mSpec.getCertificateNotAfter()));
                tbsGenerator.setSignature(sigAlgId);
                TBSCertificate tbsCertificate = tbsGenerator.generateTBSCertificate();
                ASN1EncodableVector result = new ASN1EncodableVector();
                result.add(tbsCertificate);
                result.add(sigAlgId);
                result.add(new DERBitString(signature));
                return new X509CertificateObject(Certificate.getInstance(new DERSequence(result)));
            } catch (Throwable th4) {
                th = th4;
                aSN1InputStream = publicKeyInfoIn;
                if (aSN1InputStream != null) {
                    try {
                        aSN1InputStream.close();
                    } catch (Throwable th5) {
                        if (th2 == null) {
                            th2 = th5;
                        } else if (th2 != th5) {
                            th2.addSuppressed(th5);
                        }
                    }
                }
                if (th2 == null) {
                    throw th2;
                }
                throw th;
            }
        } catch (Throwable th6) {
            th = th6;
            if (aSN1InputStream != null) {
                aSN1InputStream.close();
            }
            if (th2 == null) {
                throw th;
            }
            throw th2;
        }
    }

    @Nullable
    private static String getCertificateSignatureAlgorithm(int keymasterAlgorithm, int keySizeBits, HwKeyGenParameterSpec spec) {
        if ((spec.getPurposes() & 4) == 0 || spec.isUserAuthenticationRequired() || !spec.isDigestsSpecified()) {
            return null;
        }
        switch (keymasterAlgorithm) {
            case 1:
                if (!ArrayUtils.contains(SignaturePadding.allToKeymaster(spec.getSignaturePaddings()), 5)) {
                    return null;
                }
                int maxDigestOutputSizeBits = keySizeBits - 240;
                int bestKeymasterDigest = -1;
                int bestDigestOutputSizeBits = -1;
                for (Integer intValue : getAvailableKeymasterSignatureDigests(spec.getDigests(), HwUniversalKeyStoreProvider.getSupportedEcdsaSignatureDigests())) {
                    int keymasterDigest = intValue.intValue();
                    int outputSizeBits = HwKeymasterUtils.getDigestOutputSizeBits(keymasterDigest);
                    if (outputSizeBits <= maxDigestOutputSizeBits) {
                        if (bestKeymasterDigest == -1) {
                            bestKeymasterDigest = keymasterDigest;
                            bestDigestOutputSizeBits = outputSizeBits;
                        } else if (outputSizeBits > bestDigestOutputSizeBits) {
                            bestKeymasterDigest = keymasterDigest;
                            bestDigestOutputSizeBits = outputSizeBits;
                        }
                    }
                }
                if (bestKeymasterDigest == -1) {
                    return null;
                }
                return Digest.fromKeymasterToSignatureAlgorithmDigest(bestKeymasterDigest) + "WithRSA";
            default:
                throw new ProviderException("Unsupported algorithm: " + keymasterAlgorithm);
        }
    }

    private static Set<Integer> getAvailableKeymasterSignatureDigests(String[] authorizedKeyDigests, String[] supportedSignatureDigests) {
        int i = 0;
        Set<Integer> authorizedKeymasterKeyDigests = new HashSet();
        for (int keymasterDigest : Digest.allToKeymaster(authorizedKeyDigests)) {
            authorizedKeymasterKeyDigests.add(Integer.valueOf(keymasterDigest));
        }
        Set<Integer> supportedKeymasterSignatureDigests = new HashSet();
        int[] allToKeymaster = Digest.allToKeymaster(supportedSignatureDigests);
        int length = allToKeymaster.length;
        while (i < length) {
            supportedKeymasterSignatureDigests.add(Integer.valueOf(allToKeymaster[i]));
            i++;
        }
        Set<Integer> result = new HashSet(supportedKeymasterSignatureDigests);
        result.retainAll(authorizedKeymasterKeyDigests);
        return result;
    }

    private Iterable<byte[]> getAttestationChain(String privateKeyAlias, KeyPair keyPair, HwKeymasterArguments args) throws ProviderException {
        HwKeymasterCertificateChain outChain = new HwKeymasterCertificateChain();
        int errorCode = this.mKeyStore.attestKey(privateKeyAlias, this.mEntryUid, args, outChain);
        if (errorCode != 1) {
            throw new ProviderException("Failed to generate attestation certificate chain", HwKeystoreManager.getKeyStoreException(errorCode));
        }
        Collection<byte[]> chain = outChain.getCertificates();
        if (chain.size() >= 2) {
            return chain;
        }
        throw new ProviderException("Attestation certificate chain contained " + chain.size() + " entries. At least two are required.");
    }

    private byte[] getAttestationChainBytes(String privateKeyAlias, KeyPair keyPair, HwKeymasterArguments args) throws ProviderException {
        Iterator<byte[]> iter = getAttestationChain(privateKeyAlias, keyPair, args).iterator();
        ByteArrayOutputStream certificateConcatenationStream = new ByteArrayOutputStream();
        do {
            byte[] data = (byte[]) iter.next();
            certificateConcatenationStream.write(data, 0, data.length);
        } while (iter.hasNext());
        return certificateConcatenationStream.toByteArray();
    }

    private void generateKeystoreKeyPair(String privateKeyAlias, HwKeymasterArguments args, byte[] additionalEntropy, int flags) throws ProviderException {
        String str = privateKeyAlias;
        HwKeymasterArguments hwKeymasterArguments = args;
        byte[] bArr = additionalEntropy;
        int errorCode = this.mKeyStore.generateKey(str, hwKeymasterArguments, bArr, this.mEntryUid, flags, new HwKeyCharacteristics());
        if (errorCode != 1) {
            throw new ProviderException("Failed to generate key pair", HwKeystoreManager.getKeyStoreException(errorCode));
        }
    }

    protected KeyPair loadKeystoreKeyPair(String privateKeyAlias) throws ProviderException {
        try {
            return HwUniversalKeyStoreProvider.loadAndroidKeyStoreKeyPairFromKeystore(this.mKeyStore, privateKeyAlias, this.mEntryUid);
        } catch (UnrecoverableKeyException e) {
            throw new ProviderException("Failed to load generated key pair from keystore", e);
        }
    }

    private HwKeymasterArguments constructKeyGenerationArguments() {
        HwKeymasterArguments args = new HwKeymasterArguments();
        args.addUnsignedInt(HwKeymasterDefs.KM_TAG_KEY_SIZE, (long) this.mKeySizeBits);
        args.addEnum(HwKeymasterDefs.KM_TAG_ALGORITHM, this.mKeymasterAlgorithm);
        args.addEnums(HwKeymasterDefs.KM_TAG_PURPOSE, this.mKeymasterPurposes);
        args.addEnums(HwKeymasterDefs.KM_TAG_BLOCK_MODE, this.mKeymasterBlockModes);
        args.addEnums(HwKeymasterDefs.KM_TAG_PADDING, this.mKeymasterEncryptionPaddings);
        args.addEnums(HwKeymasterDefs.KM_TAG_PADDING, this.mKeymasterSignaturePaddings);
        args.addEnums(HwKeymasterDefs.KM_TAG_DIGEST, this.mKeymasterDigests);
        HwKeymasterUtils.addUserAuthArgs(args, this.mSpec.isUserAuthenticationRequired(), this.mSpec.getUserAuthenticationValidityDurationSeconds(), this.mSpec.isUserAuthenticationValidWhileOnBody(), this.mSpec.isInvalidatedByBiometricEnrollment(), 0);
        args.addDateIfNotNull(HwKeymasterDefs.KM_TAG_ACTIVE_DATETIME, this.mSpec.getKeyValidityStart());
        args.addDateIfNotNull(HwKeymasterDefs.KM_TAG_ORIGINATION_EXPIRE_DATETIME, this.mSpec.getKeyValidityForOriginationEnd());
        args.addDateIfNotNull(HwKeymasterDefs.KM_TAG_USAGE_EXPIRE_DATETIME, this.mSpec.getKeyValidityForConsumptionEnd());
        addAlgorithmSpecificParameters(args);
        if (this.mSpec.isUniqueIdIncluded()) {
            args.addBoolean(HwKeymasterDefs.KM_TAG_INCLUDE_UNIQUE_ID);
        }
        return args;
    }

    private void addAlgorithmSpecificParameters(HwKeymasterArguments keymasterArgs) {
        switch (this.mKeymasterAlgorithm) {
            case 1:
                keymasterArgs.addUnsignedLong(HwKeymasterDefs.KM_TAG_RSA_PUBLIC_EXPONENT, this.mRSAPublicExponent);
                addExtraParameters(keymasterArgs);
                return;
            default:
                throw new ProviderException("Unsupported algorithm: " + this.mKeymasterAlgorithm);
        }
    }

    private byte[] getRandomBytesToMixIntoKeystoreRng(SecureRandom rng, int sizeBytes) {
        if (sizeBytes <= 0) {
            return new byte[0];
        }
        if (rng == null) {
            rng = new SecureRandom();
        }
        byte[] result = new byte[sizeBytes];
        rng.nextBytes(result);
        return result;
    }

    protected String getEntryAlias(String keystoreAlias) {
        return keystoreAlias;
    }

    protected byte[] getChallenge(HwKeyGenParameterSpec mSpec) {
        return mSpec.getAttestationChallenge();
    }

    protected void addExtraParameters(HwKeymasterArguments keymasterArgs) {
    }

    protected HwKeystoreManager getKeyStoreManager() {
        return this.mKeyStore;
    }

    protected int getEntryUid() {
        return this.mEntryUid;
    }

    protected void resetAll() {
        this.mEntryAlias = null;
        this.mEntryUid = -1;
        this.mKeymasterAlgorithm = -1;
        this.mKeymasterPurposes = null;
        this.mKeymasterBlockModes = null;
        this.mKeymasterEncryptionPaddings = null;
        this.mKeymasterSignaturePaddings = null;
        this.mKeymasterDigests = null;
        this.mKeySizeBits = 0;
        this.mSpec = null;
        this.mRSAPublicExponent = null;
        this.mEncryptionAtRestRequired = false;
        this.mRng = null;
        this.mKeyStore = null;
    }
}
