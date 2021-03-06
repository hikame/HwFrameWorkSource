package com.android.server.display;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.provider.Settings.System;
import android.util.Log;
import android.util.Slog;
import java.util.Calendar;

public class HwEyeProtectionDividedTimeControl {
    private static final String[] ACTION_DIVIDED_TIME_CONTROL = new String[]{"com.android.server.action.DIVIDED_TIME_CONTROL_START", "com.android.server.action.DIVIDED_TIME_CONTROL_END"};
    public static final int ALARM_TYPE_DIVIDED_TIME_CONTROL_END = 1;
    public static final int ALARM_TYPE_DIVIDED_TIME_CONTROL_MAX = 2;
    public static final int ALARM_TYPE_DIVIDED_TIME_CONTROL_START = 0;
    public static final int DAY_IN_MINUTE = 1440;
    public static final long DAY_IN_MIllIS = 86400000;
    private static final int DEFAULT_TIME = -1;
    private static final int DEFAULT_USER = 0;
    private static final int HOUR_IN_MINUTE = 60;
    private static final String TAG = "EyeProtectionDividedTimeControl";
    private static final String USER_ID = "user_id";
    private AlarmManager mAlarmManager = null;
    private Context mContext;
    private HwEyeProtectionControllerImpl mEyeProtectionControlImpl = null;
    private boolean mInDividedTimeFlag = false;
    private boolean mInNextDay = false;
    private boolean mNowInNextDay = false;
    private int mScheduledBeginHour = -1;
    private int mScheduledBeginMinute = -1;
    private long mScheduledBeginTime = -1;
    private int mScheduledEndHour = -1;
    private int mScheduledEndMinute = -1;
    private long mScheduledEndTime = -1;
    private TimeControlAlarmReceiver mTimeControlAlarmReceiver = null;

    private class TimeControlAlarmReceiver extends BroadcastReceiver {
        public TimeControlAlarmReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(HwEyeProtectionDividedTimeControl.ACTION_DIVIDED_TIME_CONTROL[0]);
            filter.addAction(HwEyeProtectionDividedTimeControl.ACTION_DIVIDED_TIME_CONTROL[1]);
            HwEyeProtectionDividedTimeControl.this.mContext.registerReceiver(this, filter);
            Slog.d(HwEyeProtectionDividedTimeControl.TAG, "TimeControlAlarmReceiver registerReceiver");
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                PowerManager pm = (PowerManager) context.getSystemService("power");
                boolean isScreenOn = pm.isScreenOn();
                String str = HwEyeProtectionDividedTimeControl.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("onReceive intent action = ");
                stringBuilder.append(intent.getAction());
                stringBuilder.append(",isScreenOn ");
                stringBuilder.append(isScreenOn);
                Slog.i(str, stringBuilder.toString());
                if (HwEyeProtectionDividedTimeControl.ACTION_DIVIDED_TIME_CONTROL[0].equals(intent.getAction())) {
                    HwEyeProtectionDividedTimeControl.this.setTimeControlAlarm(86400000, 0);
                    HwEyeProtectionDividedTimeControl.this.setValidTime(false);
                    if (!isScreenOn) {
                        str = HwEyeProtectionDividedTimeControl.TAG;
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("isScreenon is ");
                        stringBuilder.append(pm.isScreenOn());
                        Slog.i(str, stringBuilder.toString());
                        HwEyeProtectionDividedTimeControl.this.mEyeProtectionControlImpl.setEyeProtectionScreenTurnOffMode(2);
                    } else if (HwEyeProtectionDividedTimeControl.this.mInDividedTimeFlag) {
                        str = HwEyeProtectionDividedTimeControl.TAG;
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("onReceive intent action = ");
                        stringBuilder.append(intent.getAction());
                        stringBuilder.append(" has in divided time status");
                        Slog.i(str, stringBuilder.toString());
                    } else {
                        HwEyeProtectionDividedTimeControl.this.mInDividedTimeFlag = true;
                        HwEyeProtectionDividedTimeControl.this.mEyeProtectionControlImpl.setEyeScheduleSwitchToUserMode(3);
                    }
                } else if (HwEyeProtectionDividedTimeControl.ACTION_DIVIDED_TIME_CONTROL[1].equals(intent.getAction())) {
                    HwEyeProtectionDividedTimeControl.this.mInDividedTimeFlag = false;
                    if (HwEyeProtectionDividedTimeControl.this.mInNextDay) {
                        HwEyeProtectionDividedTimeControl.this.setTimeControlAlarm(0, 1);
                    } else {
                        HwEyeProtectionDividedTimeControl.this.setTimeControlAlarm(86400000, 1);
                    }
                    if (isScreenOn) {
                        HwEyeProtectionDividedTimeControl.this.mEyeProtectionControlImpl.setEyeScheduleSwitchToUserMode(0);
                    } else {
                        str = HwEyeProtectionDividedTimeControl.TAG;
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("isScreenon is ");
                        stringBuilder.append(pm.isScreenOn());
                        Slog.i(str, stringBuilder.toString());
                        HwEyeProtectionDividedTimeControl.this.mEyeProtectionControlImpl.setEyeProtectionScreenTurnOffMode(1);
                    }
                }
            }
        }
    }

    public HwEyeProtectionDividedTimeControl(Context context, HwEyeProtectionControllerImpl eyeProtectionControlImpl) {
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        if (this.mAlarmManager == null) {
            Slog.d(TAG, "can not get alarmManager");
        }
        this.mContext = context;
        this.mEyeProtectionControlImpl = eyeProtectionControlImpl;
    }

    protected void init() {
        this.mTimeControlAlarmReceiver = new TimeControlAlarmReceiver();
    }

    public void setTimeControlAlarm(long delayTime, int type) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("set setTimeControlAlarm type is ");
        stringBuilder.append(type);
        stringBuilder.append(", delayTime is ");
        stringBuilder.append(delayTime);
        Slog.d(str, stringBuilder.toString());
        if (type < 2 && this.mScheduledBeginHour != -1 && this.mScheduledEndHour != -1) {
            String str2;
            StringBuilder stringBuilder2;
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            int day = cal.get(5);
            cal.set(13, 0);
            cal.set(14, 0);
            if (type == 0) {
                if (delayTime != 0) {
                    day++;
                }
                cal.set(11, this.mScheduledBeginHour);
                cal.set(12, this.mScheduledBeginMinute);
                cal.set(5, day);
                str2 = TAG;
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append("set setTimeControlAlarm mScheduledBeginHour ");
                stringBuilder2.append(this.mScheduledBeginHour);
                stringBuilder2.append(", mScheduledBeginMinute is ");
                stringBuilder2.append(this.mScheduledBeginMinute);
                stringBuilder2.append(", day is ");
                stringBuilder2.append(day);
                Slog.i(str2, stringBuilder2.toString());
            } else {
                cal.set(11, this.mScheduledEndHour);
                cal.set(12, this.mScheduledEndMinute);
                if (this.mInNextDay && delayTime != 0) {
                    day += 2;
                }
                if (this.mInNextDay && delayTime == 0) {
                    if (this.mInDividedTimeFlag && this.mNowInNextDay) {
                        Slog.i(TAG, "end time has not coming set it in today");
                    } else {
                        day++;
                    }
                }
                if (!(this.mInNextDay || delayTime == 0)) {
                    day++;
                }
                cal.set(5, day);
                str2 = TAG;
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append("set setTimeControlAlarm mScheduledEndHour ");
                stringBuilder2.append(this.mScheduledEndHour);
                stringBuilder2.append(", mScheduledEndMinute is ");
                stringBuilder2.append(this.mScheduledEndMinute);
                stringBuilder2.append(", day is ");
                stringBuilder2.append(day);
                Slog.i(str2, stringBuilder2.toString());
            }
            str2 = TAG;
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append("set setTimeControlAlarm day is ");
            stringBuilder2.append(day);
            Slog.i(str2, stringBuilder2.toString());
            long time = cal.getTimeInMillis();
            Intent intent = new Intent(ACTION_DIVIDED_TIME_CONTROL[type]);
            PendingIntent pending = PendingIntent.getBroadcast(this.mContext, 0, intent, 0);
            if (pending != null) {
                this.mAlarmManager.cancel(pending);
                Slog.d(TAG, "setTimeControlAlarm(): cancel prev alarm");
            }
            this.mAlarmManager.setExact(0, time, PendingIntent.getBroadcast(this.mContext, 0, intent, 0));
        }
    }

    protected boolean isNeedDelay() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(11);
        int minute = cal.get(12);
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("hour is ");
        stringBuilder.append(hour);
        stringBuilder.append(",minute is ");
        stringBuilder.append(minute);
        stringBuilder.append(",mScheduledBeginHour is ");
        stringBuilder.append(this.mScheduledBeginHour);
        stringBuilder.append(", mScheduledBeginMinute is ");
        stringBuilder.append(this.mScheduledBeginMinute);
        Slog.d(str, stringBuilder.toString());
        if (hour > this.mScheduledBeginHour || (hour == this.mScheduledBeginHour && minute >= this.mScheduledBeginMinute)) {
            return true;
        }
        return false;
    }

    protected void setTime(int time, int type) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("set time type is ");
        stringBuilder.append(type);
        Slog.d(str, stringBuilder.toString());
        if (type == 0) {
            this.mScheduledBeginTime = (long) time;
        } else if (type == 1) {
            this.mScheduledEndTime = (long) time;
        } else {
            Slog.d(TAG, "no type to settime");
            return;
        }
        SetHourAndMininte(time, type);
    }

    private void SetHourAndMininte(int time, int type) {
        int hour = time / 60;
        int minute = time % 60;
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SetHourAndMininte hour is ");
        stringBuilder.append(hour);
        stringBuilder.append(",minute is ");
        stringBuilder.append(minute);
        Slog.d(str, stringBuilder.toString());
        if (type == 0) {
            this.mScheduledBeginHour = hour;
            this.mScheduledBeginMinute = minute;
        } else if (type == 1) {
            this.mScheduledEndHour = hour;
            this.mScheduledEndMinute = minute;
        } else {
            Log.d(TAG, "no type to set hour and minute");
        }
    }

    private void SetTimeToDefault(int defaultValue) {
        this.mScheduledBeginTime = (long) defaultValue;
        this.mScheduledEndTime = (long) defaultValue;
        this.mScheduledBeginHour = defaultValue;
        this.mScheduledBeginMinute = defaultValue;
        this.mScheduledEndHour = defaultValue;
        this.mScheduledEndMinute = defaultValue;
    }

    public void cancelTimeControlAlarm(int type) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("cancelTimeControlAlarm type is ");
        stringBuilder.append(type);
        Slog.d(str, stringBuilder.toString());
        if (type < 2) {
            PendingIntent pending = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_DIVIDED_TIME_CONTROL[type]), 0);
            if (pending != null) {
                this.mAlarmManager.cancel(pending);
            }
        }
    }

    public void delayTimeControlAlarm(long time, int type) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("delayTimeControlAlarm type is ");
        stringBuilder.append(type);
        Slog.d(str, stringBuilder.toString());
        if (type < 2) {
            Intent intent = new Intent(ACTION_DIVIDED_TIME_CONTROL[type]);
            PendingIntent pending = PendingIntent.getBroadcast(this.mContext, 0, intent, 0);
            if (pending != null) {
                this.mAlarmManager.cancel(pending);
            }
            this.mAlarmManager.setExact(0, time, PendingIntent.getBroadcast(this.mContext, 0, intent, 0));
        }
    }

    public boolean getInDividedTimeFlag() {
        return this.mInDividedTimeFlag;
    }

    public void setInDividedTimeFlag(boolean flag) {
        this.mInDividedTimeFlag = flag;
    }

    public void updateDiviedTimeFlag() {
        if (this.mScheduledBeginTime != -1 && this.mScheduledEndTime != -1) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            int curTime = (cal.get(11) * 60) + cal.get(12);
            int beginTime = (this.mScheduledBeginHour * 60) + this.mScheduledBeginMinute;
            int endTime = (this.mScheduledEndHour * 60) + this.mScheduledEndMinute;
            this.mInNextDay = false;
            this.mNowInNextDay = false;
            if (endTime < beginTime) {
                this.mInNextDay = true;
            }
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("updateDiviedTimeFlag mScheduledBeginHour=");
            stringBuilder.append(this.mScheduledBeginHour);
            stringBuilder.append(",mScheduledBeginMinute=");
            stringBuilder.append(this.mScheduledBeginMinute);
            Slog.d(str, stringBuilder.toString());
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("updateDiviedTimeFlag mScheduledEndHour=");
            stringBuilder.append(this.mScheduledEndHour);
            stringBuilder.append(",mScheduledEndMinute=");
            stringBuilder.append(this.mScheduledEndMinute);
            stringBuilder.append(",inNextDay is ");
            stringBuilder.append(this.mInNextDay);
            Slog.d(str, stringBuilder.toString());
            if (this.mInNextDay) {
                if ((curTime < beginTime || curTime >= DAY_IN_MINUTE) && (curTime < 0 || curTime > endTime)) {
                    this.mInDividedTimeFlag = false;
                } else {
                    this.mInDividedTimeFlag = true;
                    if (curTime >= 0 && curTime <= endTime) {
                        this.mNowInNextDay = true;
                    }
                }
            } else if (curTime < beginTime || curTime >= endTime) {
                this.mInDividedTimeFlag = false;
            } else {
                this.mInDividedTimeFlag = true;
            }
            String str2 = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("updateDiviedTimeFlag mInDividedTimeFlag=");
            stringBuilder2.append(this.mInDividedTimeFlag);
            Slog.d(str2, stringBuilder2.toString());
        }
    }

    public void reSetTimeControlAlarm() {
        if (this.mScheduledBeginTime != -1 && this.mScheduledEndTime != -1) {
            Slog.d(TAG, "reSetTimeControlAlarm");
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            int curTime = (cal.get(11) * 60) + cal.get(12);
            int beginTime = (this.mScheduledBeginHour * 60) + this.mScheduledBeginMinute;
            int endTime = (this.mScheduledEndHour * 60) + this.mScheduledEndMinute;
            this.mInNextDay = false;
            this.mNowInNextDay = false;
            if (endTime < beginTime) {
                this.mInNextDay = true;
            }
            if (this.mInNextDay) {
                resetTimeControlAlarmNextDay(curTime, beginTime, endTime);
            } else {
                resetTimeControlAlarmToday(curTime, beginTime, endTime);
            }
        }
    }

    private void resetTimeControlAlarmToday(int curTime, int beginTime, int endTime) {
        if (curTime >= beginTime && curTime < endTime) {
            this.mInDividedTimeFlag = true;
            setTimeControlAlarm(86400000, 0);
            setTimeControlAlarm(0, 1);
        } else if (curTime >= endTime) {
            this.mInDividedTimeFlag = false;
            setTimeControlAlarm(86400000, 0);
            setTimeControlAlarm(86400000, 1);
        } else if (curTime < beginTime) {
            this.mInDividedTimeFlag = false;
            setTimeControlAlarm(0, 0);
            setTimeControlAlarm(0, 1);
        }
    }

    private void resetTimeControlAlarmNextDay(int curTime, int beginTime, int endTime) {
        if ((curTime < beginTime || curTime >= DAY_IN_MINUTE) && (curTime < 0 || curTime > endTime)) {
            this.mInDividedTimeFlag = false;
            setTimeControlAlarm(0, 0);
            setTimeControlAlarm(0, 1);
            return;
        }
        this.mInDividedTimeFlag = true;
        if (curTime >= 0 && curTime <= endTime) {
            this.mNowInNextDay = true;
            setTimeControlAlarm(0, 0);
            setTimeControlAlarm(0, 1);
        }
        if (curTime >= beginTime && curTime < DAY_IN_MINUTE) {
            setTimeControlAlarm(86400000, 0);
            setTimeControlAlarm(0, 1);
        }
    }

    public boolean testTimeIsValid(long time) {
        if (time != 0 && System.currentTimeMillis() < time) {
            return true;
        }
        return false;
    }

    public void setValidTime(boolean bValidFlg) {
        long validtime = 0;
        if (bValidFlg) {
            validtime = getValidTime();
        }
        System.putLongForUser(this.mContext.getContentResolver(), Utils.KEY_EYE_COMFORT_VALID, validtime, -2);
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("setValidTime validtime is ");
        stringBuilder.append(validtime);
        stringBuilder.append(", user =");
        stringBuilder.append(-2);
        Slog.i(str, stringBuilder.toString());
    }

    private long getValidTime() {
        Calendar c = Calendar.getInstance();
        c.set(5, c.get(5) + 1);
        return c.getTimeInMillis();
    }
}
