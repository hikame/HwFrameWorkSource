package tmsdk.common.module.intelli_sms;

public final class SmsCheckInput {
    public static final int EM_CHECK_TYPE_COMMON = 0;
    public static final int EM_CHECK_TYPE_PAY = 1;
    public static final int EST_MMS = 1;
    public static final int EST_NORMAL = 0;
    public static final int EST_WAP_PUSH = 2;
    public static final int SMS_IN = 0;
    public static final int SMS_OUT = 1;
    public String sender;
    public String sms;
    public int uiCheckFlag;
    public int uiCheckType;
    public int uiSmsInOut;
    public int uiSmsType;

    public SmsCheckInput(String str, String str2, int i, int i2, int i3, int i4) {
        this.sender = str;
        this.sms = str2;
        this.uiSmsType = i;
        this.uiCheckType = i2;
        this.uiSmsInOut = i3;
        this.uiCheckFlag = i4;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SmsCheckInput -l_2_R = (SmsCheckInput) obj;
        if (this.sender != null) {
            if (!this.sender.equals(-l_2_R.sender)) {
                return false;
            }
        } else if (-l_2_R.sender != null) {
            return false;
        }
        if (this.sms != null) {
            if (!this.sms.equals(-l_2_R.sms)) {
                return false;
            }
        } else if (-l_2_R.sms != null) {
            return false;
        }
        return this.uiCheckFlag == -l_2_R.uiCheckFlag && this.uiCheckType == -l_2_R.uiCheckType && this.uiSmsInOut == -l_2_R.uiSmsInOut && this.uiSmsType == -l_2_R.uiSmsType;
    }

    public int hashCode() {
        int i = 0;
        int hashCode = ((this.sender != null ? this.sender.hashCode() : 0) + 31) * 31;
        if (this.sms != null) {
            i = this.sms.hashCode();
        }
        return ((((((((hashCode + i) * 31) + this.uiCheckFlag) * 31) + this.uiCheckType) * 31) + this.uiSmsInOut) * 31) + this.uiSmsType;
    }

    public String toString() {
        return "SmsCheckInput [sender=" + this.sender + ", sms=" + this.sms + ", uiSmsType=" + this.uiSmsType + ", uiCheckType=" + this.uiCheckType + ", uiSmsInOut=" + this.uiSmsInOut + ", uiCheckFlag=" + this.uiCheckFlag + "]";
    }
}
