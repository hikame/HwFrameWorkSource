package com.huawei.zxing;

import com.huawei.zxing.common.BitMatrix;
import java.util.Map;

public interface Writer {
    BitMatrix encode(String str, BarcodeFormat barcodeFormat, int i, int i2) throws WriterException;

    BitMatrix encode(String str, BarcodeFormat barcodeFormat, int i, int i2, Map<EncodeHintType, ?> map) throws WriterException;
}
