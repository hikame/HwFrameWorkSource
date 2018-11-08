package vendor.huawei.hardware.eid.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class SEC_IMAGE_L {
    public final byte[] deSkey = new byte[2048];
    public int deSkeyLen;
    public final byte[] image = new byte[BUFF_LEN_E.SEC_IMAGE_MAX_LEN];
    public int len;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != SEC_IMAGE_L.class) {
            return false;
        }
        SEC_IMAGE_L other = (SEC_IMAGE_L) otherObject;
        return this.len == other.len && this.deSkeyLen == other.deSkeyLen && HidlSupport.deepEquals(this.image, other.image) && HidlSupport.deepEquals(this.deSkey, other.deSkey);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.len))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.deSkeyLen))), Integer.valueOf(HidlSupport.deepHashCode(this.image)), Integer.valueOf(HidlSupport.deepHashCode(this.deSkey))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".len = ");
        builder.append(this.len);
        builder.append(", .deSkeyLen = ");
        builder.append(this.deSkeyLen);
        builder.append(", .image = ");
        builder.append(Arrays.toString(this.image));
        builder.append(", .deSkey = ");
        builder.append(Arrays.toString(this.deSkey));
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(493576), 0);
    }

    public static final ArrayList<SEC_IMAGE_L> readVectorFromParcel(HwParcel parcel) {
        ArrayList<SEC_IMAGE_L> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 493576), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            SEC_IMAGE_L _hidl_vec_element = new SEC_IMAGE_L();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 493576));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_index_0_0;
        this.len = _hidl_blob.getInt32(0 + _hidl_offset);
        this.deSkeyLen = _hidl_blob.getInt32(4 + _hidl_offset);
        long _hidl_array_offset_0 = _hidl_offset + 8;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < BUFF_LEN_E.SEC_IMAGE_MAX_LEN; _hidl_index_0_0++) {
            this.image[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
            _hidl_array_offset_0++;
        }
        _hidl_array_offset_0 = _hidl_offset + 491528;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 2048; _hidl_index_0_0++) {
            this.deSkey[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
            _hidl_array_offset_0++;
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(493576);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<SEC_IMAGE_L> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 493576);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((SEC_IMAGE_L) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 493576));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_index_0_0;
        _hidl_blob.putInt32(0 + _hidl_offset, this.len);
        _hidl_blob.putInt32(4 + _hidl_offset, this.deSkeyLen);
        long _hidl_array_offset_0 = _hidl_offset + 8;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < BUFF_LEN_E.SEC_IMAGE_MAX_LEN; _hidl_index_0_0++) {
            _hidl_blob.putInt8(_hidl_array_offset_0, this.image[_hidl_index_0_0]);
            _hidl_array_offset_0++;
        }
        _hidl_array_offset_0 = _hidl_offset + 491528;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 2048; _hidl_index_0_0++) {
            _hidl_blob.putInt8(_hidl_array_offset_0, this.deSkey[_hidl_index_0_0]);
            _hidl_array_offset_0++;
        }
    }
}
