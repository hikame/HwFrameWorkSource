package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawUserInfoStatistic extends AManagedObject {
    public static final Creator<RawUserInfoStatistic> CREATOR = new Creator<RawUserInfoStatistic>() {
        public RawUserInfoStatistic createFromParcel(Parcel in) {
            return new RawUserInfoStatistic(in);
        }

        public RawUserInfoStatistic[] newArray(int size) {
            return new RawUserInfoStatistic[size];
        }
    };
    private Integer mCallDialNum;
    private Integer mCallDurationTime;
    private Integer mCallRecvNum;
    private Integer mContactNum;
    private Date mFirstAlarmClock;
    private String mHWID;
    private Date mHWIDBirthday;
    private Integer mHWIDGender;
    private String mHWIDName;
    private Integer mId;
    private Double mMobileDataSurplus;
    private Double mMobileDataTotal;
    private Integer mMusicNum;
    private Integer mPhotoNum;
    private Integer mReservedInt;
    private String mReservedText;
    private Date mTimeStamp;
    private Integer mVideoNum;
    private Double mWifiDataTotal;

    public RawUserInfoStatistic(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mHWID = cursor.getString(3);
        this.mContactNum = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.mMusicNum = cursor.isNull(5) ? null : Integer.valueOf(cursor.getInt(5));
        this.mVideoNum = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.mPhotoNum = cursor.isNull(7) ? null : Integer.valueOf(cursor.getInt(7));
        this.mFirstAlarmClock = cursor.isNull(8) ? null : new Date(cursor.getLong(8));
        this.mCallDialNum = cursor.isNull(9) ? null : Integer.valueOf(cursor.getInt(9));
        this.mCallRecvNum = cursor.isNull(10) ? null : Integer.valueOf(cursor.getInt(10));
        this.mCallDurationTime = cursor.isNull(11) ? null : Integer.valueOf(cursor.getInt(11));
        this.mWifiDataTotal = cursor.isNull(12) ? null : Double.valueOf(cursor.getDouble(12));
        this.mMobileDataTotal = cursor.isNull(13) ? null : Double.valueOf(cursor.getDouble(13));
        this.mMobileDataSurplus = cursor.isNull(14) ? null : Double.valueOf(cursor.getDouble(14));
        this.mHWIDName = cursor.getString(15);
        this.mHWIDBirthday = cursor.isNull(16) ? null : new Date(cursor.getLong(16));
        this.mHWIDGender = cursor.isNull(17) ? null : Integer.valueOf(cursor.getInt(17));
        if (!cursor.isNull(18)) {
            num = Integer.valueOf(cursor.getInt(18));
        }
        this.mReservedInt = num;
        this.mReservedText = cursor.getString(19);
    }

    public RawUserInfoStatistic(Parcel in) {
        String str = null;
        super(in);
        if (in.readByte() == (byte) 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mTimeStamp = in.readByte() == (byte) 0 ? null : new Date(in.readLong());
        this.mHWID = in.readByte() == (byte) 0 ? null : in.readString();
        this.mContactNum = in.readByte() == (byte) 0 ? null : Integer.valueOf(in.readInt());
        this.mMusicNum = in.readByte() == (byte) 0 ? null : Integer.valueOf(in.readInt());
        this.mVideoNum = in.readByte() == (byte) 0 ? null : Integer.valueOf(in.readInt());
        this.mPhotoNum = in.readByte() == (byte) 0 ? null : Integer.valueOf(in.readInt());
        this.mFirstAlarmClock = in.readByte() == (byte) 0 ? null : new Date(in.readLong());
        this.mCallDialNum = in.readByte() == (byte) 0 ? null : Integer.valueOf(in.readInt());
        this.mCallRecvNum = in.readByte() == (byte) 0 ? null : Integer.valueOf(in.readInt());
        this.mCallDurationTime = in.readByte() == (byte) 0 ? null : Integer.valueOf(in.readInt());
        this.mWifiDataTotal = in.readByte() == (byte) 0 ? null : Double.valueOf(in.readDouble());
        this.mMobileDataTotal = in.readByte() == (byte) 0 ? null : Double.valueOf(in.readDouble());
        this.mMobileDataSurplus = in.readByte() == (byte) 0 ? null : Double.valueOf(in.readDouble());
        this.mHWIDName = in.readByte() == (byte) 0 ? null : in.readString();
        this.mHWIDBirthday = in.readByte() == (byte) 0 ? null : new Date(in.readLong());
        this.mHWIDGender = in.readByte() == (byte) 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedInt = in.readByte() == (byte) 0 ? null : Integer.valueOf(in.readInt());
        if (in.readByte() != (byte) 0) {
            str = in.readString();
        }
        this.mReservedText = str;
    }

    private RawUserInfoStatistic(Integer mId, Date mTimeStamp, String mHWID, Integer mContactNum, Integer mMusicNum, Integer mVideoNum, Integer mPhotoNum, Date mFirstAlarmClock, Integer mCallDialNum, Integer mCallRecvNum, Integer mCallDurationTime, Double mWifiDataTotal, Double mMobileDataTotal, Double mMobileDataSurplus, String mHWIDName, Date mHWIDBirthday, Integer mHWIDGender, Integer mReservedInt, String mReservedText) {
        this.mId = mId;
        this.mTimeStamp = mTimeStamp;
        this.mHWID = mHWID;
        this.mContactNum = mContactNum;
        this.mMusicNum = mMusicNum;
        this.mVideoNum = mVideoNum;
        this.mPhotoNum = mPhotoNum;
        this.mFirstAlarmClock = mFirstAlarmClock;
        this.mCallDialNum = mCallDialNum;
        this.mCallRecvNum = mCallRecvNum;
        this.mCallDurationTime = mCallDurationTime;
        this.mWifiDataTotal = mWifiDataTotal;
        this.mMobileDataTotal = mMobileDataTotal;
        this.mMobileDataSurplus = mMobileDataSurplus;
        this.mHWIDName = mHWIDName;
        this.mHWIDBirthday = mHWIDBirthday;
        this.mHWIDGender = mHWIDGender;
        this.mReservedInt = mReservedInt;
        this.mReservedText = mReservedText;
    }

    public int describeContents() {
        return 0;
    }

    public Integer getMId() {
        return this.mId;
    }

    public void setMId(Integer mId) {
        this.mId = mId;
        setValue();
    }

    public Date getMTimeStamp() {
        return this.mTimeStamp;
    }

    public void setMTimeStamp(Date mTimeStamp) {
        this.mTimeStamp = mTimeStamp;
        setValue();
    }

    public String getMHWID() {
        return this.mHWID;
    }

    public void setMHWID(String mHWID) {
        this.mHWID = mHWID;
        setValue();
    }

    public Integer getMContactNum() {
        return this.mContactNum;
    }

    public void setMContactNum(Integer mContactNum) {
        this.mContactNum = mContactNum;
        setValue();
    }

    public Integer getMMusicNum() {
        return this.mMusicNum;
    }

    public void setMMusicNum(Integer mMusicNum) {
        this.mMusicNum = mMusicNum;
        setValue();
    }

    public Integer getMVideoNum() {
        return this.mVideoNum;
    }

    public void setMVideoNum(Integer mVideoNum) {
        this.mVideoNum = mVideoNum;
        setValue();
    }

    public Integer getMPhotoNum() {
        return this.mPhotoNum;
    }

    public void setMPhotoNum(Integer mPhotoNum) {
        this.mPhotoNum = mPhotoNum;
        setValue();
    }

    public Date getMFirstAlarmClock() {
        return this.mFirstAlarmClock;
    }

    public void setMFirstAlarmClock(Date mFirstAlarmClock) {
        this.mFirstAlarmClock = mFirstAlarmClock;
        setValue();
    }

    public Integer getMCallDialNum() {
        return this.mCallDialNum;
    }

    public void setMCallDialNum(Integer mCallDialNum) {
        this.mCallDialNum = mCallDialNum;
        setValue();
    }

    public Integer getMCallRecvNum() {
        return this.mCallRecvNum;
    }

    public void setMCallRecvNum(Integer mCallRecvNum) {
        this.mCallRecvNum = mCallRecvNum;
        setValue();
    }

    public Integer getMCallDurationTime() {
        return this.mCallDurationTime;
    }

    public void setMCallDurationTime(Integer mCallDurationTime) {
        this.mCallDurationTime = mCallDurationTime;
        setValue();
    }

    public Double getMWifiDataTotal() {
        return this.mWifiDataTotal;
    }

    public void setMWifiDataTotal(Double mWifiDataTotal) {
        this.mWifiDataTotal = mWifiDataTotal;
        setValue();
    }

    public Double getMMobileDataTotal() {
        return this.mMobileDataTotal;
    }

    public void setMMobileDataTotal(Double mMobileDataTotal) {
        this.mMobileDataTotal = mMobileDataTotal;
        setValue();
    }

    public Double getMMobileDataSurplus() {
        return this.mMobileDataSurplus;
    }

    public void setMMobileDataSurplus(Double mMobileDataSurplus) {
        this.mMobileDataSurplus = mMobileDataSurplus;
        setValue();
    }

    public String getMHWIDName() {
        return this.mHWIDName;
    }

    public void setMHWIDName(String mHWIDName) {
        this.mHWIDName = mHWIDName;
        setValue();
    }

    public Date getMHWIDBirthday() {
        return this.mHWIDBirthday;
    }

    public void setMHWIDBirthday(Date mHWIDBirthday) {
        this.mHWIDBirthday = mHWIDBirthday;
        setValue();
    }

    public Integer getMHWIDGender() {
        return this.mHWIDGender;
    }

    public void setMHWIDGender(Integer mHWIDGender) {
        this.mHWIDGender = mHWIDGender;
        setValue();
    }

    public Integer getMReservedInt() {
        return this.mReservedInt;
    }

    public void setMReservedInt(Integer mReservedInt) {
        this.mReservedInt = mReservedInt;
        setValue();
    }

    public String getMReservedText() {
        return this.mReservedText;
    }

    public void setMReservedText(String mReservedText) {
        this.mReservedText = mReservedText;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.mId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mId.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.mTimeStamp != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mTimeStamp.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mHWID != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mHWID);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mContactNum != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mContactNum.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mMusicNum != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mMusicNum.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mVideoNum != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mVideoNum.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mPhotoNum != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mPhotoNum.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mFirstAlarmClock != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mFirstAlarmClock.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCallDialNum != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCallDialNum.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCallRecvNum != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCallRecvNum.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCallDurationTime != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mCallDurationTime.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mWifiDataTotal != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mWifiDataTotal.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mMobileDataTotal != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mMobileDataTotal.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mMobileDataSurplus != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.mMobileDataSurplus.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mHWIDName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mHWIDName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mHWIDBirthday != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mHWIDBirthday.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mHWIDGender != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mHWIDGender.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mReservedInt != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mReservedInt.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mReservedText != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mReservedText);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<RawUserInfoStatistic> getHelper() {
        return RawUserInfoStatisticHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.RawUserInfoStatistic";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RawUserInfoStatistic { mId: ").append(this.mId);
        sb.append(", mTimeStamp: ").append(this.mTimeStamp);
        sb.append(", mHWID: ").append(this.mHWID);
        sb.append(", mContactNum: ").append(this.mContactNum);
        sb.append(", mMusicNum: ").append(this.mMusicNum);
        sb.append(", mVideoNum: ").append(this.mVideoNum);
        sb.append(", mPhotoNum: ").append(this.mPhotoNum);
        sb.append(", mFirstAlarmClock: ").append(this.mFirstAlarmClock);
        sb.append(", mCallDialNum: ").append(this.mCallDialNum);
        sb.append(", mCallRecvNum: ").append(this.mCallRecvNum);
        sb.append(", mCallDurationTime: ").append(this.mCallDurationTime);
        sb.append(", mWifiDataTotal: ").append(this.mWifiDataTotal);
        sb.append(", mMobileDataTotal: ").append(this.mMobileDataTotal);
        sb.append(", mMobileDataSurplus: ").append(this.mMobileDataSurplus);
        sb.append(", mHWIDName: ").append(this.mHWIDName);
        sb.append(", mHWIDBirthday: ").append(this.mHWIDBirthday);
        sb.append(", mHWIDGender: ").append(this.mHWIDGender);
        sb.append(", mReservedInt: ").append(this.mReservedInt);
        sb.append(", mReservedText: ").append(this.mReservedText);
        sb.append(" }");
        return sb.toString();
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String getDatabaseVersion() {
        return "0.0.10";
    }

    public int getDatabaseVersionCode() {
        return 10;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
