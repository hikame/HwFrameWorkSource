package com.android.server.fingerprint;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.app.SynchronousUserSwitchObserver;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint;
import android.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprintClientCallback;
import android.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprintClientCallback.Stub;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.IFingerprintClientActiveCallback;
import android.hardware.fingerprint.IFingerprintService;
import android.hardware.fingerprint.IFingerprintServiceLockoutResetCallback;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.hdm.HwDeviceManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.IHwBinder.DeathRecipient;
import android.os.IRemoteCallback;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.SELinux;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.security.KeyStore;
import android.util.Flog;
import android.util.Log;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.DumpUtils;
import com.android.server.FingerprintUnlockDataCollector;
import com.android.server.ServiceThread;
import com.android.server.SystemServerInitThreadPool;
import com.huawei.android.aod.HwAodManagerEx;
import com.huawei.cust.HwCustUtils;
import com.huawei.pgmng.log.LogPower;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FingerprintService extends AbsFingerprintService implements DeathRecipient {
    private static final String ACTION_AUTH_FINGER_UP = "com.huawei.finger.action_up";
    private static final String ACTION_LOCKOUT_RESET = "com.android.server.fingerprint.ACTION_LOCKOUT_RESET";
    private static final int BASE_BRIGHTNESS = 3000;
    private static final long CANCEL_TIMEOUT_LIMIT = 3000;
    private static final boolean CLEANUP_UNUSED_FP = false;
    private static final int CODE_DISABLE_FINGERPRINT_VIEW = 1114;
    private static final int CODE_ENABLE_FINGERPRINT_VIEW = 1115;
    private static final int CODE_GET_FINGERPRINT_LIST_ENROLLED = 1118;
    private static final int CODE_GET_HARDWARE_POSITION = 1110;
    private static final int CODE_GET_HARDWARE_TYPE = 1109;
    private static final int CODE_GET_HOVER_SUPPORT = 1113;
    private static final int CODE_GET_TOKEN_LEN_RULE = 1103;
    private static final int CODE_IS_FINGERPRINT_HARDWARE_DETECTED = 1119;
    private static final int CODE_IS_FP_NEED_CALIBRATE_RULE = 1101;
    private static final int CODE_IS_SUPPORT_DUAL_FINGERPRINT = 1120;
    private static final int CODE_KEEP_MASK_SHOW_AFTER_AUTHENTICATION = 1116;
    private static final int CODE_NOTIFY_OPTICAL_CAPTURE = 1111;
    private static final int CODE_REMOVE_FINGERPRINT = 1107;
    private static final int CODE_REMOVE_MASK_AND_SHOW_BUTTON = 1117;
    private static final int CODE_SET_CALIBRATE_MODE_RULE = 1102;
    private static final int CODE_SET_FINGERPRINT_MASK_VIEW = 1104;
    private static final int CODE_SET_HOVER_SWITCH = 1112;
    private static final int CODE_SHOW_FINGERPRINT_BUTTON = 1106;
    private static final int CODE_SHOW_FINGERPRINT_VIEW = 1105;
    private static final int CODE_SUSPEND_AUTHENTICATE = 1108;
    static final boolean DEBUG = true;
    private static boolean DEBUG_FPLOG = false;
    protected static final int ENROLL_UD = 4096;
    private static final int ERROR_CODE_COMMEN_ERROR = 8;
    private static final long FAIL_LOCKOUT_TIMEOUT_MS = 30000;
    private static final int FINGERPRINT_ACQUIRED_FINGER_DOWN = 2002;
    protected static final int FINGER_DOWN_TYPE_AUTHENTICATING = 1;
    protected static final int FINGER_DOWN_TYPE_AUTHENTICATING_SETTINGS = 2;
    protected static final int FINGER_DOWN_TYPE_ENROLLING = 0;
    private static final int FP_CLOSE = 0;
    private static final String FP_DATA_DIR = "fpdata";
    private static final int HIDDEN_SPACE_ID = -100;
    private static final int HUAWEI_FINGERPRINT_CAPTURE_COMPLETE = 0;
    private static final int HUAWEI_FINGERPRINT_DOWN_UD = 2102;
    private static final int HUAWEI_FINGERPRINT_UP = 2003;
    protected static final int HW_FP_AUTH_BOTH_SPACE = 33554432;
    protected static final int HW_FP_AUTH_UD = 134217728;
    protected static final int HW_FP_AUTH_UG = 67108864;
    private static final int HW_FP_NO_COUNT_FAILED_ATTEMPS = 16777216;
    private static final int MAX_BRIGHTNESS = 255;
    private static final int MAX_FAILED_ATTEMPTS_LOCKOUT_PERMANENT = 20;
    private static final int MAX_FAILED_ATTEMPTS_LOCKOUT_TIMED = 5;
    private static final int MSG_USER_SWITCHING = 10;
    private static final String PERM_AUTH_FINGER_UP = "com.huawei.authentication.HW_ACCESS_AUTH_SERVICE";
    private static final int PRIMARY_USER_ID = 0;
    protected static final String SETTINGS_PACKAGE_NAME = "com.android.settings";
    private static final int SET_FINGER_DOWN_ERROR_END = 4;
    private static final int SET_FINGER_DOWN_NORMAL_END = 3;
    private static final int SET_FINGER_DOWN_START = 2;
    private static final int SPECIAL_USER_ID = -101;
    private static final String SYSTEMUI_PACKAGE_NAME = "com.android.systemui";
    static final String TAG = "FingerprintService";
    private static final int TYPE_FINGERPRINT_AUTHENTICATION_RESULT_FAIL = 1;
    private static final int TYPE_FINGERPRINT_AUTHENTICATION_RESULT_SUCCESS = 0;
    private static final int TYPE_FINGERPRINT_AUTHENTICATION_UNCHECKED = 2;
    private long auTime;
    private long downTime;
    private FingerprintUnlockDataCollector fpDataCollector;
    private AlarmManager mAlarmManager;
    private final AppOpsManager mAppOps;
    private final Map<Integer, Long> mAuthenticatorIds = Collections.synchronizedMap(new HashMap());
    private final CopyOnWriteArrayList<IFingerprintClientActiveCallback> mClientActiveCallbacks = new CopyOnWriteArrayList();
    private Context mContext;
    private HashMap<Integer, PerformanceStats> mCryptoPerformanceMap = new HashMap();
    private int mCurrentAuthFpDev;
    private long mCurrentAuthenticatorId;
    private ClientMonitor mCurrentClient;
    private int mCurrentUserId = -10000;
    @GuardedBy("this")
    private IBiometricsFingerprint mDaemon;
    private IBiometricsFingerprintClientCallback mDaemonCallback = new Stub() {
        public void onEnrollResult(long deviceId, int fingerId, int groupId, int remaining) {
            Slog.w(FingerprintService.TAG, "onEnrollResult 1");
            final long j = deviceId;
            final int i = fingerId;
            final int i2 = groupId;
            final int i3 = remaining;
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    Slog.w(FingerprintService.TAG, "onEnrollResult 2");
                    FingerprintService.this.handleEnrollResult(j, i, i2, i3);
                }
            });
        }

        public void onAcquired(long deviceId, int acquiredInfo, int vendorCode) {
            Slog.w(FingerprintService.TAG, "onAcquired 1");
            final long j = deviceId;
            final int i = acquiredInfo;
            final int i2 = vendorCode;
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    Slog.w(FingerprintService.TAG, "onAcquired 2");
                    FingerprintService.this.handleAcquired(j, i, i2);
                }
            });
            int clientAcquireInfo = acquiredInfo == 6 ? vendorCode + 1000 : acquiredInfo;
            if (FingerprintService.DEBUG_FPLOG) {
                if (clientAcquireInfo == FingerprintService.FINGERPRINT_ACQUIRED_FINGER_DOWN && FingerprintService.this.fpDataCollector != null) {
                    FingerprintService.this.fpDataCollector.reportFingerDown();
                    FingerprintService.this.downTime = System.currentTimeMillis();
                } else if (clientAcquireInfo == 0 && FingerprintService.this.fpDataCollector != null) {
                    FingerprintService.this.fpDataCollector.reportCaptureCompleted();
                }
            }
            if (clientAcquireInfo == FingerprintService.FINGERPRINT_ACQUIRED_FINGER_DOWN && FingerprintService.this.currentClient(FingerprintService.this.mKeyguardPackage)) {
                LogPower.push(HdmiCecKeycode.UI_SOUND_PRESENTATION_BASS_STEP_MINUS);
            }
            if (FingerprintService.this.mCurrentClient == null) {
                Log.e(FingerprintService.TAG, "mCurrentClient is null notifyFinger failed");
                return;
            }
            if (clientAcquireInfo == FingerprintService.FINGERPRINT_ACQUIRED_FINGER_DOWN) {
                Log.d(FingerprintService.TAG, "onAcquired set mCurrentAuthFpDev DEVICE_BACK");
                FingerprintService.this.mCurrentAuthFpDev = 0;
            }
            String currentOpName = FingerprintService.this.mCurrentClient.getOwnerString();
            if (clientAcquireInfo == FingerprintService.HUAWEI_FINGERPRINT_DOWN_UD) {
                if (FingerprintService.this.mCurrentClient instanceof AuthenticationClient) {
                    Log.d(FingerprintService.TAG, "notify that AuthenticationClient finger down:" + currentOpName);
                    FingerprintService.this.mCurrentAuthFpDev = 1;
                    if (FingerprintService.SETTINGS_PACKAGE_NAME.equals(currentOpName)) {
                        FingerprintService.this.notifyFingerDown(2);
                    } else {
                        FingerprintService.this.notifyFingerDown(1);
                    }
                } else if (FingerprintService.this.mCurrentClient instanceof EnrollClient) {
                    Log.d(FingerprintService.TAG, "notify that EnrollClient finger down");
                    FingerprintService.this.notifyFingerDown(0);
                }
            } else if (clientAcquireInfo == 5 || clientAcquireInfo == 1 || clientAcquireInfo == FingerprintService.HUAWEI_FINGERPRINT_UP) {
                if (FingerprintService.this.mCurrentClient instanceof AuthenticationClient) {
                    Log.d(FingerprintService.TAG, "clientAcquireInfo = " + clientAcquireInfo);
                    FingerprintService.this.notifyAuthenticationFinished(currentOpName, 2, FingerprintService.this.mHwFailedAttempts);
                }
            } else if (clientAcquireInfo == 0 && (FingerprintService.this.mCurrentClient instanceof AuthenticationClient)) {
                if (FingerprintService.SETTINGS_PACKAGE_NAME.equals(currentOpName)) {
                    FingerprintService.this.notifyCaptureFinished(2);
                } else {
                    FingerprintService.this.notifyCaptureFinished(1);
                }
            }
        }

        public void onAuthenticated(long deviceId, int fingerId, int groupId, ArrayList<Byte> token) {
            Slog.w(FingerprintService.TAG, "onAuthenticated 1");
            if (FingerprintService.DEBUG_FPLOG && FingerprintService.this.fpDataCollector != null) {
                FingerprintService.this.fpDataCollector.reportFingerprintAuthenticated(fingerId != 0);
                FingerprintService.this.auTime = System.currentTimeMillis();
            }
            final long j = deviceId;
            final int i = fingerId;
            final int i2 = groupId;
            final ArrayList<Byte> arrayList = token;
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    Slog.w(FingerprintService.TAG, "onAuthenticated 2");
                    FingerprintService.this.handleAuthenticated(j, i, i2, arrayList);
                }
            });
        }

        public void onError(long deviceId, int error, int vendorCode) {
            Slog.w(FingerprintService.TAG, "onError 1");
            final long j = deviceId;
            final int i = error;
            final int i2 = vendorCode;
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    Slog.w(FingerprintService.TAG, "onError 2");
                    HwAodManagerEx.setPowerState(4);
                    FingerprintService.this.handleError(j, i, i2);
                }
            });
        }

        public void onRemoved(long deviceId, int fingerId, int groupId, int remaining) {
            Slog.w(FingerprintService.TAG, "onRemoved 1");
            final long j = deviceId;
            final int i = fingerId;
            final int i2 = groupId;
            final int i3 = remaining;
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    Slog.w(FingerprintService.TAG, "onRemoved 2");
                    FingerprintService.this.handleRemoved(j, i, i2, i3);
                }
            });
        }

        public void onEnumerate(long deviceId, int fingerId, int groupId, int remaining) {
            final long j = deviceId;
            final int i = fingerId;
            final int i2 = groupId;
            final int i3 = remaining;
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    FingerprintService.this.handleEnumerate(j, i, i2, i3);
                }
            });
        }
    };
    private LinkedList<Integer> mEnumeratingUserIds = new LinkedList();
    private int mFailedAttempts;
    private final FingerprintUtils mFingerprintUtils = FingerprintUtils.getInstance();
    private long mHalDeviceId;
    Handler mHandler = null;
    private HwCustFingerprintService mHwCust = ((HwCustFingerprintService) HwCustUtils.createObj(HwCustFingerprintService.class, new Object[0]));
    int mHwFailedAttempts = 0;
    private final String mKeyguardPackage;
    private final ArrayList<FingerprintServiceLockoutResetMonitor> mLockoutMonitors = new ArrayList();
    private final BroadcastReceiver mLockoutReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (FingerprintService.ACTION_LOCKOUT_RESET.equals(intent.getAction())) {
                FingerprintService.this.resetFailedAttempts(true);
            }
        }
    };
    long mLockoutTime = 0;
    private ClientMonitor mPendingClient;
    private HashMap<Integer, PerformanceStats> mPerformanceMap = new HashMap();
    private PerformanceStats mPerformanceStats;
    private final PowerManager mPowerManager;
    private final Runnable mResetClientState = new Runnable() {
        public void run() {
            Slog.w(FingerprintService.TAG, "Client " + (FingerprintService.this.mCurrentClient != null ? FingerprintService.this.mCurrentClient.getOwnerString() : "null") + " failed to respond to cancel, starting client " + (FingerprintService.this.mPendingClient != null ? FingerprintService.this.mPendingClient.getOwnerString() : "null"));
            FingerprintService.this.mCurrentClient = null;
            FingerprintService.this.startClient(FingerprintService.this.mPendingClient, false);
            FingerprintService.this.mPendingClient = null;
        }
    };
    private final Runnable mResetFailedAttemptsRunnable = new Runnable() {
        public void run() {
            FingerprintService.this.resetFailedAttempts(true);
        }
    };
    private boolean mSupportKids = SystemProperties.getBoolean("ro.config.kidsfinger_enable", false);
    private boolean mTimedLockoutCleared;
    private IBinder mToken = new Binder();
    private long mUDHalDeviceId;
    private ArrayList<UserFingerprint> mUnknownFingerprints = new ArrayList();
    private final UserManager mUserManager;
    private ClientMonitor mWaitupClient;
    private String opPackageName;

    private class FingerprintServiceLockoutResetMonitor {
        private static final long WAKELOCK_TIMEOUT_MS = 2000;
        private final IFingerprintServiceLockoutResetCallback mCallback;
        private final Runnable mRemoveCallbackRunnable = new Runnable() {
            public void run() {
                if (FingerprintServiceLockoutResetMonitor.this.mWakeLock.isHeld()) {
                    FingerprintServiceLockoutResetMonitor.this.mWakeLock.release();
                }
                FingerprintService.this.removeLockoutResetCallback(FingerprintServiceLockoutResetMonitor.this);
            }
        };
        private final WakeLock mWakeLock;

        public FingerprintServiceLockoutResetMonitor(IFingerprintServiceLockoutResetCallback callback) {
            this.mCallback = callback;
            this.mWakeLock = FingerprintService.this.mPowerManager.newWakeLock(1, "lockout reset callback");
        }

        public void sendLockoutReset() {
            if (this.mCallback != null) {
                try {
                    this.mWakeLock.acquire(WAKELOCK_TIMEOUT_MS);
                    this.mCallback.onLockoutReset(FingerprintService.this.mHalDeviceId, new IRemoteCallback.Stub() {
                        public void sendResult(Bundle data) throws RemoteException {
                            if (FingerprintServiceLockoutResetMonitor.this.mWakeLock.isHeld()) {
                                FingerprintServiceLockoutResetMonitor.this.mWakeLock.release();
                            }
                        }
                    });
                } catch (DeadObjectException e) {
                    Slog.w(FingerprintService.TAG, "Death object while invoking onLockoutReset: ", e);
                    FingerprintService.this.mHandler.post(this.mRemoveCallbackRunnable);
                } catch (RemoteException e2) {
                    Slog.w(FingerprintService.TAG, "Failed to invoke onLockoutReset: ", e2);
                }
            }
        }
    }

    private final class FingerprintServiceWrapper extends IFingerprintService.Stub {
        private FingerprintServiceWrapper() {
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (!FingerprintService.this.isHwTransactInterest(code)) {
                return super.onTransact(code, data, reply, flags);
            }
            FingerprintService.this.checkPermission("android.permission.USE_FINGERPRINT");
            return FingerprintService.this.onHwTransact(code, data, reply, flags);
        }

        public long preEnroll(IBinder token) {
            Flog.i(1303, "FingerprintService preEnroll");
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            return FingerprintService.this.startPreEnroll(token);
        }

        public int postEnroll(IBinder token) {
            Flog.i(1303, "FingerprintService postEnroll");
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            Flog.i(1303, "postEnroll client uid = " + Binder.getCallingUid() + ", postEnroll client pid = " + Binder.getCallingPid());
            return FingerprintService.this.startPostEnroll(token);
        }

        public void enroll(IBinder token, byte[] cryptoToken, int userId, IFingerprintServiceReceiver receiver, int flags, String opPackageName) {
            int enrolled;
            Flog.i(1303, "FingerprintService enroll");
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            int limit = FingerprintService.this.mContext.getResources().getInteger(17694789);
            if (FingerprintService.this.mFingerprintUtils.isDualFp()) {
                enrolled = FingerprintService.this.getEnrolledFingerprintsEx(opPackageName, flags == 4096 ? 1 : 0, userId).size();
            } else {
                enrolled = FingerprintService.this.getEnrolledFingerprints(userId).size();
            }
            if (enrolled >= limit) {
                Slog.w(FingerprintService.TAG, "Too many fingerprints registered");
                return;
            }
            boolean isPrivacyUser = FingerprintService.this.checkPrivacySpaceEnroll(userId, ActivityManager.getCurrentUser());
            if (FingerprintService.this.isCurrentUserOrProfile(userId) || (isPrivacyUser ^ 1) == 0) {
                final boolean restricted = isRestricted();
                final IBinder iBinder = token;
                final byte[] bArr = cryptoToken;
                final int i = userId;
                final IFingerprintServiceReceiver iFingerprintServiceReceiver = receiver;
                final int i2 = flags;
                final String str = opPackageName;
                FingerprintService.this.mHandler.post(new Runnable() {
                    public void run() {
                        FingerprintService.this.startEnrollment(iBinder, bArr, i, iFingerprintServiceReceiver, i2, restricted, str);
                    }
                });
                return;
            }
            Flog.w(1303, "user invalid enroll error");
        }

        private boolean isRestricted() {
            return FingerprintService.this.hasPermission("android.permission.MANAGE_FINGERPRINT") ^ 1;
        }

        public void cancelEnrollment(final IBinder token) {
            Flog.i(1303, "FingerprintService cancelEnrollment");
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            FingerprintService.this.notifyEnrollmentCanceled();
            Flog.i(1303, "cancelEnrollment client uid = " + Binder.getCallingUid() + ", cancelEnrollment client pid = " + Binder.getCallingPid());
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    ClientMonitor client = FingerprintService.this.mCurrentClient;
                    if ((client instanceof EnrollClient) && client.getToken() == token) {
                        client.stop(client.getToken() == token);
                    }
                }
            });
        }

        public void authenticate(IBinder token, long opId, int groupId, IFingerprintServiceReceiver receiver, int flags, String opPackageName) {
            Flog.i(1303, "FingerprintService authenticate");
            FingerprintService.this.notifyAuthenticationStarted(opPackageName, receiver, flags, groupId);
            if (HwDeviceManager.disallowOp(50)) {
                Slog.i(FingerprintService.TAG, "MDM forbid fingerprint authentication");
                FingerprintService.this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        Toast toast = Toast.makeText(FingerprintService.this.mContext, 33686051, 0);
                        toast.getWindowParams().type = 2010;
                        LayoutParams windowParams = toast.getWindowParams();
                        windowParams.privateFlags |= 16;
                        toast.show();
                    }
                }, 300);
                return;
            }
            int callingUid = Binder.getCallingUid();
            int callingPid = Binder.getCallingPid();
            int callingUserId = UserHandle.getCallingUserId();
            final boolean restricted = isRestricted();
            FingerprintService.this.setLivenessSwitch(opPackageName);
            if (FingerprintService.this.mSupportKids) {
                Slog.i(FingerprintService.TAG, "mSupportKids=" + FingerprintService.this.mSupportKids);
                FingerprintService.this.setKidsFingerprint(groupId, FingerprintService.this.isKeyguard(opPackageName));
            }
            if (FingerprintService.this.canUseFingerprint(opPackageName, true, callingUid, callingPid, callingUserId)) {
                final long j = opId;
                final IBinder iBinder = token;
                final int i = callingUserId;
                final int i2 = groupId;
                final IFingerprintServiceReceiver iFingerprintServiceReceiver = receiver;
                final int i3 = flags;
                final String str = opPackageName;
                FingerprintService.this.mHandler.post(new Runnable() {
                    public void run() {
                        Slog.i(FingerprintService.TAG, "authenticate run");
                        MetricsLogger.histogram(FingerprintService.this.mContext, "fingerprint_token", j != 0 ? 1 : 0);
                        HashMap<Integer, PerformanceStats> pmap = j == 0 ? FingerprintService.this.mPerformanceMap : FingerprintService.this.mCryptoPerformanceMap;
                        PerformanceStats stats = (PerformanceStats) pmap.get(Integer.valueOf(FingerprintService.this.mCurrentUserId));
                        if (stats == null) {
                            stats = new PerformanceStats();
                            pmap.put(Integer.valueOf(FingerprintService.this.mCurrentUserId), stats);
                        }
                        FingerprintService.this.mPerformanceStats = stats;
                        FingerprintService.this.startAuthentication(iBinder, j, i, i2, iFingerprintServiceReceiver, i3, restricted, str);
                    }
                });
                return;
            }
            Slog.v(FingerprintService.TAG, "authenticate(): reject " + opPackageName);
        }

        public void cancelAuthentication(final IBinder token, final String opPackageName) {
            int callingUid = Binder.getCallingUid();
            int callingPid = Binder.getCallingPid();
            int callingUserId = UserHandle.getCallingUserId();
            if (FingerprintService.this.canUseFingerprint(opPackageName, false, callingUid, callingPid, callingUserId)) {
                Flog.i(1303, "FingerprintService cancelAuthentication client uid = " + callingUid + ", cancelAuthentication client pid = " + callingPid + " callingUserId = " + callingUserId);
                FingerprintService.this.mHandler.post(new Runnable() {
                    public void run() {
                        FingerprintService.this.notifyAuthenticationCanceled(opPackageName);
                        ClientMonitor client = FingerprintService.this.mCurrentClient;
                        if (client instanceof AuthenticationClient) {
                            if (client.getToken() == token) {
                                Slog.v(FingerprintService.TAG, "stop client " + client.getOwnerString());
                                client.stop(client.getToken() == token);
                                return;
                            }
                            Slog.v(FingerprintService.TAG, "can't stop client " + client.getOwnerString() + " since tokens don't match");
                        } else if (client != null) {
                            Slog.v(FingerprintService.TAG, "can't cancel non-authenticating client " + client.getOwnerString());
                        }
                    }
                });
                return;
            }
            Slog.v(FingerprintService.TAG, "cancelAuthentication(): reject " + opPackageName);
        }

        public void setActiveUser(final int userId) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    FingerprintService.this.updateActiveGroup(userId, null);
                }
            });
        }

        public void remove(IBinder token, int fingerId, int groupId, int userId, IFingerprintServiceReceiver receiver) {
            Flog.i(1303, "FingerprintService remove");
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            final boolean restricted = isRestricted();
            final IBinder iBinder = token;
            final int i = fingerId;
            final int i2 = groupId;
            final int i3 = userId;
            final IFingerprintServiceReceiver iFingerprintServiceReceiver = receiver;
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    FingerprintService.this.startRemove(iBinder, i, i2, i3, iFingerprintServiceReceiver, restricted, false);
                }
            });
        }

        public void enumerate(IBinder token, int userId, IFingerprintServiceReceiver receiver) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            final boolean restricted = isRestricted();
            final IBinder iBinder = token;
            final int i = userId;
            final IFingerprintServiceReceiver iFingerprintServiceReceiver = receiver;
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    FingerprintService.this.startEnumerate(iBinder, i, iFingerprintServiceReceiver, restricted, false);
                }
            });
        }

        public boolean isHardwareDetected(long deviceId, String opPackageName) {
            boolean z = false;
            if (!FingerprintService.this.canUseFingerprint(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid(), UserHandle.getCallingUserId(), true)) {
                return false;
            }
            long token = Binder.clearCallingIdentity();
            try {
                if (!(FingerprintService.this.getFingerprintDaemon() == null || FingerprintService.this.mHalDeviceId == 0)) {
                    z = true;
                }
                Binder.restoreCallingIdentity(token);
                return z;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void rename(final int fingerId, final int groupId, final String name) {
            Flog.i(1303, "FingerprintService rename");
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            boolean isPrivacyUser = FingerprintService.this.checkPrivacySpaceEnroll(groupId, ActivityManager.getCurrentUser());
            if (FingerprintService.this.isCurrentUserOrProfile(groupId) || (isPrivacyUser ^ 1) == 0) {
                FingerprintService.this.mHandler.post(new Runnable() {
                    public void run() {
                        FingerprintService.this.mFingerprintUtils.renameFingerprintForUser(FingerprintService.this.mContext, fingerId, groupId, name);
                    }
                });
            } else {
                Flog.w(1303, "user invalid rename error");
            }
        }

        public List<Fingerprint> getEnrolledFingerprints(int userId, String opPackageName) {
            Flog.i(1303, "FingerprintService getEnrolledFingerprints");
            if (!FingerprintService.this.canUseFingerprint(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid(), UserHandle.getCallingUserId())) {
                return Collections.emptyList();
            }
            if (!FingerprintService.this.mFingerprintUtils.isDualFp()) {
                return FingerprintService.this.getEnrolledFingerprints(userId);
            }
            Slog.d(FingerprintService.TAG, "dualFingerprint getEnrolledFingerprints and userId is: " + userId);
            return FingerprintService.this.getEnrolledFingerprintsEx(opPackageName, -1, userId);
        }

        public boolean hasEnrolledFingerprints(int userId, String opPackageName) {
            Flog.i(1303, "FingerprintService hasEnrolledFingerprints");
            if (!FingerprintService.this.canUseFingerprint(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid(), UserHandle.getCallingUserId())) {
                return false;
            }
            if (!FingerprintService.this.mFingerprintUtils.isDualFp()) {
                return FingerprintService.this.hasEnrolledFingerprints(userId);
            }
            Slog.d(FingerprintService.TAG, "dualFingerprint hasEnrolledFingerprints and userId is: " + userId);
            boolean hasEnrollFingerprints = FingerprintService.this.getEnrolledFingerprintsEx(opPackageName, -1, userId).size() > 0;
            Slog.d(FingerprintService.TAG, "dualFingerprint hasEnrolledFingerprints: " + hasEnrollFingerprints);
            return hasEnrollFingerprints;
        }

        public long getAuthenticatorId(String opPackageName) {
            Flog.i(1303, "FingerprintService getAuthenticatorId");
            return FingerprintService.this.getAuthenticatorId(opPackageName);
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(FingerprintService.this.mContext, FingerprintService.TAG, pw)) {
                long ident = Binder.clearCallingIdentity();
                try {
                    if (args.length <= 0 || !"--proto".equals(args[0])) {
                        FingerprintService.this.dumpInternal(pw);
                    } else {
                        FingerprintService.this.dumpProto(fd);
                    }
                    Binder.restoreCallingIdentity(ident);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }

        public void resetTimeout(byte[] token) {
            FingerprintService.this.checkPermission("android.permission.RESET_FINGERPRINT_LOCKOUT");
            FingerprintService.this.mHandler.post(FingerprintService.this.mResetFailedAttemptsRunnable);
        }

        public void addLockoutResetCallback(final IFingerprintServiceLockoutResetCallback callback) throws RemoteException {
            if (callback == null) {
                Log.e(FingerprintService.TAG, " FingerprintServiceLockoutResetCallback is null, cannot addLockoutResetMonitor, return");
            } else {
                FingerprintService.this.mHandler.post(new Runnable() {
                    public void run() {
                        FingerprintService.this.addLockoutResetMonitor(new FingerprintServiceLockoutResetMonitor(callback));
                    }
                });
            }
        }

        public boolean isClientActive() {
            boolean z = true;
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            synchronized (FingerprintService.this) {
                if (FingerprintService.this.mCurrentClient == null && FingerprintService.this.mPendingClient == null) {
                    z = false;
                }
            }
            return z;
        }

        public void addClientActiveCallback(IFingerprintClientActiveCallback callback) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            FingerprintService.this.mClientActiveCallbacks.add(callback);
        }

        public void removeClientActiveCallback(IFingerprintClientActiveCallback callback) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            FingerprintService.this.mClientActiveCallbacks.remove(callback);
        }

        public int getRemainingNum() {
            FingerprintService.this.checkPermission("android.permission.USE_FINGERPRINT");
            if (FingerprintService.this.mHwCust != null && FingerprintService.this.mHwCust.isAtt()) {
                return FingerprintService.this.mHwCust.getRemainingNum(FingerprintService.this.mHwFailedAttempts, FingerprintService.this.mContext);
            }
            Slog.d(FingerprintService.TAG, " Remaining Num Attempts = " + (5 - FingerprintService.this.mHwFailedAttempts));
            return 5 - FingerprintService.this.mHwFailedAttempts;
        }

        public long getRemainingTime() {
            FingerprintService.this.checkPermission("android.permission.USE_FINGERPRINT");
            long now = SystemClock.elapsedRealtime();
            long nowToLockout = now - FingerprintService.this.mLockoutTime;
            Slog.d(FingerprintService.TAG, "Remaining Time mLockoutTime = " + FingerprintService.this.mLockoutTime + "  now = " + now);
            if (nowToLockout <= 0 || nowToLockout >= 30000) {
                return 0;
            }
            return 30000 - nowToLockout;
        }
    }

    private class PerformanceStats {
        int accept;
        int acquire;
        int lockout;
        int permanentLockout;
        int reject;

        private PerformanceStats() {
        }
    }

    private class UserFingerprint {
        Fingerprint f;
        int userId;

        public UserFingerprint(Fingerprint f, int userId) {
            this.f = f;
            this.userId = userId;
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG_FPLOG = isLoggable;
    }

    public FingerprintService(Context context) {
        super(context);
        this.mContext = context;
        this.mKeyguardPackage = ComponentName.unflattenFromString(context.getResources().getString(17039804)).getPackageName();
        this.mAppOps = (AppOpsManager) context.getSystemService(AppOpsManager.class);
        this.mPowerManager = (PowerManager) this.mContext.getSystemService(PowerManager.class);
        this.mContext.registerReceiver(this.mLockoutReceiver, new IntentFilter(ACTION_LOCKOUT_RESET), "android.permission.RESET_FINGERPRINT_LOCKOUT", null);
        this.mUserManager = UserManager.get(this.mContext);
        this.fpDataCollector = FingerprintUnlockDataCollector.getInstance();
        ServiceThread fingerprintThread = new ServiceThread("fingerprintServcie", -8, false);
        fingerprintThread.start();
        this.mHandler = new Handler(fingerprintThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 10:
                        Slog.i(FingerprintService.TAG, "MSG_USER_SWITCHING");
                        FingerprintService.this.handleUserSwitching(msg.arg1);
                        return;
                    default:
                        Slog.w(FingerprintService.TAG, "Unknown message:" + msg.what);
                        return;
                }
            }
        };
    }

    public void serviceDied(long cookie) {
        Slog.v(TAG, "fingerprint HAL died");
        MetricsLogger.count(this.mContext, "fingerprintd_died", 1);
        handleError(this.mHalDeviceId, 1, 0);
    }

    public synchronized IBiometricsFingerprint getFingerprintDaemon() {
        if (this.mDaemon == null) {
            Slog.v(TAG, "mDeamon was null, reconnect to fingerprint");
            try {
                this.mDaemon = IBiometricsFingerprint.getService();
            } catch (NoSuchElementException e) {
            } catch (RemoteException e2) {
                Slog.e(TAG, "Failed to get biometric interface", e2);
            }
            if (this.mDaemon == null) {
                Slog.w(TAG, "fingerprint HIDL not available");
                return null;
            }
            this.mDaemon.asBinder().linkToDeath(this, 0);
            try {
                this.mHalDeviceId = this.mDaemon.setNotify(this.mDaemonCallback);
            } catch (RemoteException e22) {
                Slog.e(TAG, "Failed to open fingerprint HAL", e22);
                this.mDaemon = null;
            }
            if (isSupportDualFingerprint() && this.mDaemon != null && sendCommandToHal(100) == 0) {
                try {
                    this.mUDHalDeviceId = this.mDaemon.setNotify(this.mDaemonCallback);
                    Slog.d(TAG, "dualFingerprint:mUDHalDeviceId is " + this.mUDHalDeviceId);
                } catch (RemoteException e222) {
                    Slog.e(TAG, "dualFingerprint Failed to setNotify callback for UD" + e222);
                }
            }
            Slog.v(TAG, "Fingerprint HAL id: " + this.mHalDeviceId);
            if (this.mHalDeviceId != 0 || (FingerprintUtils.getInstance().isDualFp() && this.mUDHalDeviceId != 0)) {
                loadAuthenticatorIds();
                updateActiveGroup(ActivityManager.getCurrentUser(), null);
                doFingerprintCleanup(ActivityManager.getCurrentUser());
            } else {
                Slog.w(TAG, "Failed to open Fingerprint HAL!");
                MetricsLogger.count(this.mContext, "fingerprintd_openhal_error", 1);
                this.mDaemon = null;
            }
        }
        return this.mDaemon;
    }

    protected long getHalDeviceId() {
        return this.mHalDeviceId;
    }

    protected long getUdHalDeviceId() {
        return this.mUDHalDeviceId;
    }

    private void loadAuthenticatorIds() {
        long t = System.currentTimeMillis();
        this.mAuthenticatorIds.clear();
        for (UserInfo user : UserManager.get(this.mContext).getUsers(true)) {
            int userId = getUserOrWorkProfileId(null, user.id);
            if (!this.mAuthenticatorIds.containsKey(Integer.valueOf(userId))) {
                updateActiveGroup(userId, null);
            }
        }
        t = System.currentTimeMillis() - t;
        if (t > 1000) {
            Slog.w(TAG, "loadAuthenticatorIds() taking too long: " + t + "ms");
        }
    }

    private void doFingerprintCleanup(int userId) {
    }

    private void resetEnumerateState() {
        Slog.v(TAG, "Enumerate cleaning up");
        this.mEnumeratingUserIds.clear();
        this.mUnknownFingerprints.clear();
    }

    private void enumerateNextUser() {
        int nextUser = ((Integer) this.mEnumeratingUserIds.getFirst()).intValue();
        updateActiveGroup(nextUser, null);
        boolean restricted = hasPermission("android.permission.MANAGE_FINGERPRINT") ^ 1;
        Slog.v(TAG, "Enumerating user id " + nextUser + " of " + this.mEnumeratingUserIds.size() + " remaining users");
        startEnumerate(this.mToken, nextUser, null, restricted, true);
    }

    private void cleanupUnknownFingerprints() {
        if (this.mUnknownFingerprints.isEmpty()) {
            resetEnumerateState();
            return;
        }
        Slog.w(TAG, "unknown fingerprint size: " + this.mUnknownFingerprints.size());
        UserFingerprint uf = (UserFingerprint) this.mUnknownFingerprints.get(0);
        this.mUnknownFingerprints.remove(uf);
        boolean restricted = hasPermission("android.permission.MANAGE_FINGERPRINT") ^ 1;
        updateActiveGroup(uf.userId, null);
        startRemove(this.mToken, uf.f.getFingerId(), uf.f.getGroupId(), uf.userId, null, restricted, true);
    }

    protected void handleEnumerate(long deviceId, int fingerId, int groupId, int remaining) {
        Slog.w(TAG, "Enumerate: fid=" + fingerId + ", gid=" + groupId + ", dev=" + deviceId + ", rem=" + remaining);
        ClientMonitor client = this.mCurrentClient;
        if ((client instanceof InternalRemovalClient) || ((client instanceof EnumerateClient) ^ 1) == 0) {
            client.onEnumerationResult(fingerId, groupId, remaining);
            if (remaining == 0) {
                this.mEnumeratingUserIds.poll();
                if (client instanceof InternalEnumerateClient) {
                    List<Fingerprint> enrolled = ((InternalEnumerateClient) client).getEnumeratedList();
                    Slog.w(TAG, "Added " + enrolled.size() + " enumerated fingerprints for deletion");
                    for (Fingerprint f : enrolled) {
                        this.mUnknownFingerprints.add(new UserFingerprint(f, client.getTargetUserId()));
                    }
                }
                removeClient(client);
                if (!this.mEnumeratingUserIds.isEmpty()) {
                    enumerateNextUser();
                } else if (client instanceof InternalEnumerateClient) {
                    Slog.v(TAG, "Finished enumerating all users");
                    cleanupUnknownFingerprints();
                }
            }
        }
    }

    protected void handleError(long deviceId, int error, int vendorCode) {
        ClientMonitor client = this.mCurrentClient;
        if (client instanceof EnrollClient) {
            notifyEnrollmentCanceled();
        }
        if (error == 8 && vendorCode > BASE_BRIGHTNESS) {
            vendorCode -= 3000;
            if (vendorCode >= 255) {
                vendorCode = 255;
            }
            Slog.w(TAG, "change brightness to " + vendorCode);
            notifyFingerCalibrarion(vendorCode);
        }
        if ((client instanceof InternalRemovalClient) || (client instanceof InternalEnumerateClient)) {
            resetEnumerateState();
        }
        if (client != null && client.onError(error, vendorCode)) {
            removeClient(client);
        }
        Slog.v(TAG, "handleError(client=" + (client != null ? client.getOwnerString() : "null") + ", error = " + error + ")");
        if (error == 5) {
            this.mHandler.removeCallbacks(this.mResetClientState);
            if (this.mPendingClient != null) {
                Slog.v(TAG, "start pending client " + this.mPendingClient.getOwnerString());
                startClient(this.mPendingClient, false);
                this.mPendingClient = null;
            }
        } else if (error == 1) {
            Slog.w(TAG, "Got ERROR_HW_UNAVAILABLE; try reconnecting next client.");
            synchronized (this) {
                this.mDaemon = null;
                this.mHalDeviceId = 0;
                this.mCurrentUserId = -10000;
            }
        }
    }

    protected void handleRemoved(long deviceId, int fingerId, int groupId, int remaining) {
        Slog.w(TAG, "Removed: fid=" + fingerId + ", gid=" + groupId + ", dev=" + deviceId + ", rem=" + remaining);
        ClientMonitor client = this.mCurrentClient;
        groupId = getRealUserIdForApp(groupId);
        if (client != null && client.onRemoved(fingerId, groupId, remaining)) {
            if (this.mFingerprintUtils.isDualFp() && (client instanceof RemovalClient)) {
                RemovalClient removeClient = (RemovalClient) client;
                boolean hasFingerprints = this.mFingerprintUtils.getFingerprintsForUser(this.mContext, groupId, -1).size() > 0;
                if (!(this.mFingerprintUtils.getFingerprintsForUser(this.mContext, groupId, 1).size() > 0)) {
                    sendCommandToHal(0);
                    Slog.d(TAG, "UDFingerprint all removed so TP CLOSE");
                }
                if (removeClient.getFingerId() == 0 && hasFingerprints) {
                    Slog.d(TAG, "dualFingerprint-> handleRemoved, but do not destory client.");
                    return;
                }
            }
            removeClient(client);
            if (!hasEnrolledFingerprints(groupId)) {
                updateActiveGroup(groupId, null);
            }
        }
        if ((client instanceof InternalRemovalClient) && (this.mUnknownFingerprints.isEmpty() ^ 1) != 0) {
            cleanupUnknownFingerprints();
        } else if (client instanceof InternalRemovalClient) {
            resetEnumerateState();
        }
    }

    protected void handleAuthenticated(long deviceId, int fingerId, int groupId, ArrayList<Byte> token) {
        ClientMonitor client = this.mCurrentClient;
        groupId = getRealUserIdForApp(groupId);
        if (fingerId != 0) {
            byte[] byteToken = new byte[token.size()];
            for (int i = 0; i < token.size(); i++) {
                byteToken[i] = ((Byte) token.get(i)).byteValue();
            }
            KeyStore.getInstance().addAuthToken(byteToken);
        }
        if (client != null && client.onAuthenticated(fingerId, groupId)) {
            if ((((AuthenticationClient) client).mFlags & 4096) != 0) {
                Slog.w(TAG, "AuthenticationClient remvoe with waitup");
                this.mWaitupClient = client;
            }
            removeClient(client);
        }
        if (this.mPerformanceStats == null) {
            Slog.w(TAG, "mPerformanceStats is null");
            return;
        }
        PerformanceStats performanceStats;
        if (fingerId != 0) {
            performanceStats = this.mPerformanceStats;
            performanceStats.accept++;
        } else {
            performanceStats = this.mPerformanceStats;
            performanceStats.reject++;
        }
    }

    protected void handleAcquired(long deviceId, int acquiredInfo, int vendorCode) {
        stopPickupTrunOff();
        if (this.mWaitupClient != null) {
            int clientCode = acquiredInfo == 6 ? vendorCode + 1000 : acquiredInfo;
            Slog.w(TAG, "handleAcquired for waitClient AC:" + acquiredInfo + " VC:" + vendorCode + " CC:" + clientCode);
            if (clientCode == HUAWEI_FINGERPRINT_UP) {
                this.mWaitupClient = null;
                Slog.w(TAG, "wait clint ActionUp.");
                this.mContext.sendBroadcastAsUser(new Intent(ACTION_AUTH_FINGER_UP), new UserHandle(0), PERM_AUTH_FINGER_UP);
            }
        }
        ClientMonitor client = this.mCurrentClient;
        int hwClientCode = acquiredInfo == 6 ? vendorCode + 1000 : acquiredInfo;
        if (hwClientCode == FINGERPRINT_ACQUIRED_FINGER_DOWN || hwClientCode == HUAWEI_FINGERPRINT_DOWN_UD) {
            String ownerString = client == null ? null : client.getOwnerString();
            Slog.i(TAG, "handleAcquired, with currentOpName : " + ownerString + " hwClientCode : " + hwClientCode);
            if (SYSTEMUI_PACKAGE_NAME.equals(ownerString)) {
                HwAodManagerEx.setPowerState(2);
            }
        }
        if (client != null && client.onAcquired(acquiredInfo, vendorCode)) {
            removeClient(client);
        }
        if (this.mPerformanceStats != null && getLockoutMode() == 0 && (client instanceof AuthenticationClient)) {
            PerformanceStats performanceStats = this.mPerformanceStats;
            performanceStats.acquire++;
        }
    }

    protected void handleEnrollResult(long deviceId, int fingerId, int groupId, int remaining) {
        ClientMonitor client = this.mCurrentClient;
        groupId = getRealUserIdForApp(groupId);
        if (client == null || !client.onEnrollResult(fingerId, groupId, remaining)) {
            notifyEnrollingFingerUp();
            Slog.w(TAG, "no eroll client, remove erolled fingerprint");
            if (remaining == 0) {
                IBiometricsFingerprint daemon = getFingerprintDaemon();
                if (daemon != null) {
                    try {
                        daemon.remove(fingerId, ActivityManager.getCurrentUser());
                    } catch (RemoteException e) {
                    }
                } else {
                    return;
                }
            }
        }
        removeClient(client);
        notifyEnrollmentCanceled();
        updateActiveGroup(groupId, null);
    }

    protected int getRealUserIdForApp(int groupId) {
        if (groupId != HIDDEN_SPACE_ID) {
            return groupId;
        }
        for (UserInfo user : this.mUserManager.getUsers(true)) {
            if (user != null && user.isHwHiddenSpace()) {
                return user.id;
            }
        }
        Slog.w(TAG, "getRealUserIdForApp error return 0");
        return 0;
    }

    private void userActivity() {
        this.mPowerManager.userActivity(SystemClock.uptimeMillis(), 2, 0);
    }

    void handleUserSwitching(int userId) {
        updateActiveGroup(userId, null);
        doFingerprintCleanup(userId);
    }

    private void removeClient(ClientMonitor client) {
        if (client != null) {
            client.destroy();
            if (!(client == this.mCurrentClient || this.mCurrentClient == null)) {
                Slog.w(TAG, new StringBuilder().append("Unexpected client: ").append(client.getOwnerString()).append("expected: ").append(this.mCurrentClient).toString() != null ? this.mCurrentClient.getOwnerString() : "null");
            }
        }
        if (this.mCurrentClient != null) {
            Slog.v(TAG, "Done with client: " + client.getOwnerString());
            this.mCurrentClient = null;
        }
        if (this.mPendingClient == null) {
            notifyClientActiveCallbacks(false);
        }
    }

    private boolean inLockoutMode() {
        if (this.mHwCust != null && this.mHwCust.isAtt()) {
            return this.mHwCust.inLockoutMode(this.mFailedAttempts, this.mContext);
        }
        return this.mFailedAttempts >= 5;
    }

    protected int getLockoutMode() {
        if (this.mFailedAttempts >= 20) {
            return 2;
        }
        if (this.mFailedAttempts > 0 && !this.mTimedLockoutCleared) {
            if (this.mHwCust == null || !this.mHwCust.isAtt()) {
                if (this.mFailedAttempts % 5 == 0) {
                    return 1;
                }
            } else if (this.mHwCust.isLockoutMode(this.mFailedAttempts, this.mContext)) {
                return 1;
            }
        }
        return 0;
    }

    private void scheduleLockoutReset() {
        if (this.mAlarmManager == null) {
            this.mAlarmManager = (AlarmManager) this.mContext.getSystemService(AlarmManager.class);
        }
        this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + 30000, getLockoutResetIntent());
    }

    private void cancelLockoutReset() {
        if (this.mAlarmManager == null) {
            this.mAlarmManager = (AlarmManager) this.mContext.getSystemService(AlarmManager.class);
        }
        this.mAlarmManager.cancel(getLockoutResetIntent());
    }

    private PendingIntent getLockoutResetIntent() {
        return PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_LOCKOUT_RESET), HW_FP_AUTH_UD);
    }

    protected void handleHwFailedAttempt(int flags, String packagesName) {
        if ((HW_FP_NO_COUNT_FAILED_ATTEMPS & flags) == 0 || !SETTINGS_PACKAGE_NAME.equals(packagesName)) {
            this.mHwFailedAttempts++;
        } else {
            Slog.i(TAG, "no need count hw failed attempts");
        }
    }

    public long startPreEnroll(IBinder token) {
        IBiometricsFingerprint daemon = getFingerprintDaemon();
        if (daemon == null) {
            Slog.w(TAG, "startPreEnroll: no fingerprint HAL!");
            return 0;
        }
        try {
            return daemon.preEnroll();
        } catch (RemoteException e) {
            Slog.e(TAG, "startPreEnroll failed", e);
            return 0;
        }
    }

    public int startPostEnroll(IBinder token) {
        IBiometricsFingerprint daemon = getFingerprintDaemon();
        if (daemon == null) {
            Slog.w(TAG, "startPostEnroll: no fingerprint HAL!");
            return 0;
        }
        try {
            return daemon.postEnroll();
        } catch (RemoteException e) {
            Slog.e(TAG, "startPostEnroll failed", e);
            return 0;
        }
    }

    protected void setLivenessSwitch(String opPackageName) {
        Slog.w(TAG, "father class call setLivenessSwitch");
    }

    private void startClient(ClientMonitor newClient, boolean initiatedByClient) {
        ClientMonitor currentClient = this.mCurrentClient;
        this.mHandler.removeCallbacks(this.mResetClientState);
        if (currentClient != null) {
            Slog.v(TAG, "request stop current client " + currentClient.getOwnerString());
            if (!((currentClient instanceof InternalEnumerateClient) || (currentClient instanceof InternalRemovalClient))) {
                currentClient.stop(initiatedByClient);
                if (this.mPendingClient != null) {
                    this.mPendingClient.destroy();
                }
            }
            this.mPendingClient = newClient;
            this.mHandler.removeCallbacks(this.mResetClientState);
            this.mHandler.postDelayed(this.mResetClientState, CANCEL_TIMEOUT_LIMIT);
        } else if (newClient != null) {
            this.mCurrentClient = newClient;
            Slog.v(TAG, "starting client " + newClient.getClass().getSuperclass().getSimpleName() + "(" + newClient.getOwnerString() + ")" + ", initiatedByClient = " + initiatedByClient + ")");
            notifyClientActiveCallbacks(true);
            newClient.start();
        }
    }

    void startRemove(IBinder token, int fingerId, int groupId, int userId, IFingerprintServiceReceiver receiver, boolean restricted, boolean internal) {
        if (getFingerprintDaemon() == null) {
            Slog.w(TAG, "startRemove: no fingerprint HAL!");
            return;
        }
        if (this.mFingerprintUtils.isDualFp() && fingerId != 0) {
            List<Fingerprint> finerprints = FingerprintUtils.getInstance().getFingerprintsForUser(getContext(), userId, 1);
            int fingerprintSize = finerprints.size();
            for (int i = 0; i < fingerprintSize; i++) {
                if (((Fingerprint) finerprints.get(i)).getFingerId() == fingerId) {
                    Slog.d(TAG, "dualFingerprint send MSG_REMOVE_UD");
                    sendCommandToHal(104);
                    break;
                }
            }
        }
        if (this.mFingerprintUtils.isDualFp() && fingerId == 0) {
            Slog.d(TAG, "dualFingerprint send MSG_REMOVE_ALL");
            sendCommandToHal(107);
        }
        if (internal) {
            Context context = getContext();
            startClient(new InternalRemovalClient(context, this.mHalDeviceId, token, receiver, fingerId, groupId, userId, restricted, context.getOpPackageName()) {
                public void notifyUserActivity() {
                }

                public IBiometricsFingerprint getFingerprintDaemon() {
                    return FingerprintService.this.getFingerprintDaemon();
                }
            }, true);
        } else if (token == null) {
            Slog.e(TAG, "startRemove error: token null!");
        } else {
            startClient(new RemovalClient(getContext(), this.mHalDeviceId, token, receiver, fingerId, groupId, userId, restricted, token.toString()) {
                public void notifyUserActivity() {
                    FingerprintService.this.userActivity();
                }

                public IBiometricsFingerprint getFingerprintDaemon() {
                    return FingerprintService.this.getFingerprintDaemon();
                }
            }, true);
        }
    }

    void startEnumerate(IBinder token, int userId, IFingerprintServiceReceiver receiver, boolean restricted, boolean internal) {
        if (getFingerprintDaemon() == null) {
            Slog.w(TAG, "startEnumerate: no fingerprint HAL!");
            return;
        }
        if (internal) {
            List<Fingerprint> enrolledList = getEnrolledFingerprints(userId);
            Context context = getContext();
            startClient(new InternalEnumerateClient(context, this.mHalDeviceId, token, receiver, userId, userId, restricted, context.getOpPackageName(), enrolledList) {
                public void notifyUserActivity() {
                }

                public IBiometricsFingerprint getFingerprintDaemon() {
                    return FingerprintService.this.getFingerprintDaemon();
                }
            }, true);
        } else if (token == null) {
            Slog.e(TAG, "startEnumerate error: token null!");
        } else {
            startClient(new EnumerateClient(getContext(), this.mHalDeviceId, token, receiver, userId, userId, restricted, token.toString()) {
                public void notifyUserActivity() {
                    FingerprintService.this.userActivity();
                }

                public IBiometricsFingerprint getFingerprintDaemon() {
                    return FingerprintService.this.getFingerprintDaemon();
                }
            }, true);
        }
    }

    public List<Fingerprint> getEnrolledFingerprints(int userId) {
        return this.mFingerprintUtils.getFingerprintsForUser(this.mContext, userId);
    }

    public boolean hasEnrolledFingerprints(int userId) {
        if (userId != UserHandle.getCallingUserId()) {
            checkPermission("android.permission.INTERACT_ACROSS_USERS");
        }
        if (this.mFingerprintUtils.getFingerprintsForUser(this.mContext, userId).size() > 0) {
            return true;
        }
        return false;
    }

    boolean hasPermission(String permission) {
        return getContext().checkCallingOrSelfPermission(permission) == 0;
    }

    void checkPermission(String permission) {
        getContext().enforceCallingOrSelfPermission(permission, "Must have " + permission + " permission.");
    }

    int getEffectiveUserId(int userId) {
        UserManager um = UserManager.get(this.mContext);
        if (um != null) {
            long callingIdentity = Binder.clearCallingIdentity();
            userId = um.getCredentialOwnerProfile(userId);
            Binder.restoreCallingIdentity(callingIdentity);
            return userId;
        }
        Slog.e(TAG, "Unable to acquire UserManager");
        return userId;
    }

    boolean isCurrentUserOrProfile(int userId) {
        UserManager um = UserManager.get(this.mContext);
        if (um == null) {
            Slog.e(TAG, "Unable to acquire UserManager");
            return false;
        }
        long token = Binder.clearCallingIdentity();
        try {
            for (int profileId : um.getEnabledProfileIds(ActivityManager.getCurrentUser())) {
                if (profileId == userId) {
                    return true;
                }
            }
            Binder.restoreCallingIdentity(token);
            return false;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private boolean isForegroundActivity(int uid, int pid) {
        try {
            List<RunningAppProcessInfo> procs = ActivityManager.getService().getRunningAppProcesses();
            int N = procs.size();
            for (int i = 0; i < N; i++) {
                RunningAppProcessInfo proc = (RunningAppProcessInfo) procs.get(i);
                if (proc.pid == pid && proc.uid == uid && proc.importance == 100) {
                    return true;
                }
            }
        } catch (RemoteException e) {
            Slog.w(TAG, "am.getRunningAppProcesses() failed");
        }
        return false;
    }

    private boolean canUseFingerprint(String opPackageName, boolean requireForeground, int uid, int pid, int userId) {
        return canUseFingerprint(opPackageName, requireForeground, uid, pid, userId, false);
    }

    private boolean canUseFingerprint(String opPackageName, boolean requireForeground, int uid, int pid, int userId, boolean isDetected) {
        if (opPackageName == null || opPackageName.equals("")) {
            Slog.i(TAG, "opPackageName is null or opPackageName is invalid");
            return false;
        }
        checkPermission("android.permission.USE_FINGERPRINT");
        this.opPackageName = opPackageName;
        if (opPackageName != null && (opPackageName.equals("com.huawei.hwasm") || opPackageName.equals("com.huawei.securitymgr") || opPackageName.equals("com.huawei.aod") || ((isDetected && opPackageName.equals("com.tencent.mm")) || isKeyguard(opPackageName)))) {
            return true;
        }
        if (isCurrentUserOrProfile(userId)) {
            try {
                if (this.mAppOps.noteOp(55, uid, opPackageName) != 0) {
                    Slog.w(TAG, "Rejecting " + opPackageName + " ; permission denied");
                    return false;
                }
                if (requireForeground) {
                    int i;
                    if (isForegroundActivity(uid, pid)) {
                        i = 1;
                    } else {
                        i = currentClient(opPackageName);
                    }
                    if ((i ^ 1) != 0) {
                        Slog.w(TAG, "Rejecting " + opPackageName + " ; not in foreground");
                        return false;
                    }
                }
                return true;
            } catch (SecurityException e) {
                Slog.w(TAG, "AppOpsManager noteOp error:" + e);
                return false;
            }
        }
        Slog.w(TAG, "Rejecting " + opPackageName + " ; not a current user or profile");
        return false;
    }

    private boolean currentClient(String opPackageName) {
        return this.mCurrentClient != null ? this.mCurrentClient.getOwnerString().equals(opPackageName) : false;
    }

    private boolean isKeyguard(String clientPackage) {
        return this.mKeyguardPackage.equals(clientPackage);
    }

    private void addLockoutResetMonitor(FingerprintServiceLockoutResetMonitor monitor) {
        if (!this.mLockoutMonitors.contains(monitor)) {
            this.mLockoutMonitors.add(monitor);
        }
    }

    private void removeLockoutResetCallback(FingerprintServiceLockoutResetMonitor monitor) {
        this.mLockoutMonitors.remove(monitor);
    }

    private void notifyLockoutResetMonitors() {
        for (int i = 0; i < this.mLockoutMonitors.size(); i++) {
            ((FingerprintServiceLockoutResetMonitor) this.mLockoutMonitors.get(i)).sendLockoutReset();
        }
    }

    private void notifyClientActiveCallbacks(boolean isActive) {
        List<IFingerprintClientActiveCallback> callbacks = this.mClientActiveCallbacks;
        for (int i = 0; i < callbacks.size(); i++) {
            try {
                ((IFingerprintClientActiveCallback) callbacks.get(i)).onClientActiveChanged(isActive);
            } catch (RemoteException e) {
                this.mClientActiveCallbacks.remove(callbacks.get(i));
            }
        }
    }

    private void startAuthentication(IBinder token, long opId, int callingUserId, int groupId, IFingerprintServiceReceiver receiver, int flags, boolean restricted, String opPackageName) {
        int newGroupId = groupId;
        updateActiveGroup(groupId, opPackageName);
        Slog.v(TAG, "startAuthentication(" + opPackageName + ")");
        if (shouldAuthBothSpaceFingerprints(opPackageName, flags)) {
            Slog.i(TAG, "should authenticate both space fingerprints");
            newGroupId = SPECIAL_USER_ID;
        }
        final String str = opPackageName;
        AuthenticationClient client = new AuthenticationClient(getContext(), this.mHalDeviceId, token, receiver, this.mCurrentUserId, newGroupId, opId, restricted, opPackageName, flags) {
            public boolean onAuthenticated(int fingerId, int groupId) {
                IFingerprintServiceReceiver receiver = getReceiver();
                boolean authenticated = fingerId != 0;
                if (receiver != null) {
                    if (authenticated) {
                        Log.e(FingerprintService.TAG, "onAuthenticated, pass");
                        FingerprintService.this.notifyAuthenticationFinished(str, 0, 0);
                        Flog.bdReport(FingerprintService.this.mContext, 8, "{pkg:" + str + ",ErrorCount:" + FingerprintService.this.mHwFailedAttempts + ",DeviceType:" + FingerprintService.this.mCurrentAuthFpDev + "}");
                        Log.i(FingerprintService.TAG, "onAuthenticated success:{pkg:" + str + ",ErrorCount:" + FingerprintService.this.mHwFailedAttempts + ",DeviceType:" + FingerprintService.this.mCurrentAuthFpDev + "}");
                    } else if (FingerprintService.this.auTime - FingerprintService.this.downTime > 0) {
                        Log.e(FingerprintService.TAG, "onAuthenticated,fail ,mHwFailedAttempts = " + FingerprintService.this.mHwFailedAttempts);
                        FingerprintService.this.notifyAuthenticationFinished(str, 1, FingerprintService.this.mHwFailedAttempts + 1);
                        Flog.bdReport(FingerprintService.this.mContext, 7, "{CostTime:" + (FingerprintService.this.auTime - FingerprintService.this.downTime) + ",pkg:" + str + ",DeviceType:" + FingerprintService.this.mCurrentAuthFpDev + "}");
                        Log.i(FingerprintService.TAG, "onAuthenticated fail:{CostTime:" + (FingerprintService.this.auTime - FingerprintService.this.downTime) + ",pkg:" + str + ",DeviceType:" + FingerprintService.this.mCurrentAuthFpDev + "}");
                    }
                }
                return super.onAuthenticated(fingerId, groupId);
            }

            public int handleFailedAttempt() {
                boolean noNeedAddFailedAttemps = false;
                if ((this.mFlags & FingerprintService.HW_FP_NO_COUNT_FAILED_ATTEMPS) != 0 && FingerprintService.SETTINGS_PACKAGE_NAME.equals(getOwnerString())) {
                    noNeedAddFailedAttemps = true;
                    Slog.i(FingerprintService.TAG, "no need count failed attempts");
                }
                if (!noNeedAddFailedAttemps) {
                    FingerprintService fingerprintService = FingerprintService.this;
                    fingerprintService.mFailedAttempts = fingerprintService.mFailedAttempts + 1;
                }
                FingerprintService.this.mTimedLockoutCleared = false;
                int lockoutMode = FingerprintService.this.getLockoutMode();
                PerformanceStats -get17;
                if (lockoutMode == 2) {
                    -get17 = FingerprintService.this.mPerformanceStats;
                    -get17.permanentLockout++;
                } else if (lockoutMode == 1) {
                    -get17 = FingerprintService.this.mPerformanceStats;
                    -get17.lockout++;
                }
                if (lockoutMode == 0) {
                    return 0;
                }
                FingerprintService.this.mLockoutTime = SystemClock.elapsedRealtime();
                FingerprintService.this.scheduleLockoutReset();
                if (FingerprintService.this.isKeyguard(str)) {
                    return 0;
                }
                return lockoutMode;
            }

            public void resetFailedAttempts() {
                if (inLockoutMode()) {
                    Slog.v(FingerprintService.TAG, "resetFailedAttempts should be called from APP");
                } else {
                    FingerprintService.this.resetFailedAttempts(true);
                }
            }

            public void notifyUserActivity() {
                FingerprintService.this.userActivity();
            }

            public IBiometricsFingerprint getFingerprintDaemon() {
                return FingerprintService.this.getFingerprintDaemon();
            }

            public void handleHwFailedAttempt(int flags, String packagesName) {
                FingerprintService.this.handleHwFailedAttempt(flags, packagesName);
            }

            public boolean inLockoutMode() {
                return FingerprintService.this.inLockoutMode();
            }
        };
        int lockoutMode = getLockoutMode();
        if (lockoutMode == 0 || (isKeyguard(opPackageName) ^ 1) == 0) {
            if (this.mFingerprintUtils.isDualFp()) {
                Slog.d(TAG, "dualFingerprint startAuthentication and flag is: " + flags);
                if (flags == 0) {
                    if (canUseUdFingerprint(opPackageName)) {
                        Slog.d(TAG, "dualFingerprint send MSG_AUTH_ALL");
                        sendCommandToHal(103);
                    }
                } else if ((HW_FP_AUTH_UD & flags) != 0) {
                    if ((HW_FP_AUTH_UG & flags) != 0) {
                        Slog.d(TAG, "dualFingerprint send MSG_AUTH_ALL");
                        sendCommandToHal(103);
                    } else {
                        Slog.d(TAG, "dualFingerprint send MSG_AUTH_UD");
                        sendCommandToHal(102);
                    }
                }
            }
            startClient(client, true);
            return;
        }
        int errorCode;
        Slog.v(TAG, "In lockout mode(" + lockoutMode + ") ; disallowing authentication");
        if (lockoutMode == 1) {
            errorCode = 7;
        } else {
            errorCode = 9;
        }
        if (!client.onError(errorCode, 0)) {
            Slog.w(TAG, "Cannot send permanent lockout message to client");
        }
    }

    private void startEnrollment(IBinder token, byte[] cryptoToken, int userId, IFingerprintServiceReceiver receiver, int flags, boolean restricted, String opPackageName) {
        updateActiveGroup(userId, opPackageName);
        int groupId = userId;
        EnrollClient client = new EnrollClient(getContext(), this.mHalDeviceId, token, receiver, userId, userId, cryptoToken, restricted, opPackageName) {
            public IBiometricsFingerprint getFingerprintDaemon() {
                return FingerprintService.this.getFingerprintDaemon();
            }

            public void notifyUserActivity() {
                FingerprintService.this.userActivity();
            }
        };
        if (this.mFingerprintUtils.isDualFp() && SETTINGS_PACKAGE_NAME.equals(opPackageName)) {
            int targetDevice = flags == 4096 ? 1 : 0;
            Slog.d(TAG, "dualFingerprint enroll targetDevice is: " + targetDevice);
            if (targetDevice == 1) {
                Slog.d(TAG, "dualFingerprint send MSG_ENROLL_UD");
                sendCommandToHal(101);
                client.setTargetDevice(1);
            }
        }
        notifyEnrollmentStarted(flags);
        startClient(client, true);
    }

    protected void resetFailedAttempts(boolean clearAttemptCounter) {
        if (getLockoutMode() != 0) {
            Slog.v(TAG, "Reset fingerprint lockout, clearAttemptCounter=" + clearAttemptCounter);
        }
        if (clearAttemptCounter) {
            this.mFailedAttempts = 0;
        }
        this.mTimedLockoutCleared = true;
        this.mHandler.removeCallbacks(this.mResetFailedAttemptsRunnable);
        cancelLockoutReset();
        notifyLockoutResetMonitors();
        this.mLockoutTime = 0;
        this.mHwFailedAttempts = 0;
    }

    private boolean isHwTransactInterest(int code) {
        if (code == CODE_IS_FP_NEED_CALIBRATE_RULE || code == CODE_SET_CALIBRATE_MODE_RULE || code == CODE_GET_TOKEN_LEN_RULE || code == CODE_SET_FINGERPRINT_MASK_VIEW || code == CODE_SHOW_FINGERPRINT_VIEW || code == CODE_SHOW_FINGERPRINT_BUTTON || code == 1107 || code == CODE_GET_HARDWARE_POSITION || code == CODE_GET_HARDWARE_TYPE || code == CODE_NOTIFY_OPTICAL_CAPTURE || code == CODE_SUSPEND_AUTHENTICATE || code == CODE_SET_HOVER_SWITCH || code == CODE_GET_HOVER_SUPPORT || code == CODE_DISABLE_FINGERPRINT_VIEW || code == CODE_ENABLE_FINGERPRINT_VIEW || code == CODE_KEEP_MASK_SHOW_AFTER_AUTHENTICATION || code == CODE_REMOVE_MASK_AND_SHOW_BUTTON || code == CODE_IS_SUPPORT_DUAL_FINGERPRINT || code == CODE_GET_FINGERPRINT_LIST_ENROLLED || code == CODE_IS_FINGERPRINT_HARDWARE_DETECTED) {
            return true;
        }
        return false;
    }

    private void dumpInternal(PrintWriter pw) {
        JSONObject dump = new JSONObject();
        try {
            dump.put("service", "Fingerprint Manager");
            JSONArray sets = new JSONArray();
            for (UserInfo user : UserManager.get(getContext()).getUsers()) {
                int i;
                int userId = user.getUserHandle().getIdentifier();
                int N = this.mFingerprintUtils.getFingerprintsForUser(this.mContext, userId).size();
                PerformanceStats stats = (PerformanceStats) this.mPerformanceMap.get(Integer.valueOf(userId));
                PerformanceStats cryptoStats = (PerformanceStats) this.mCryptoPerformanceMap.get(Integer.valueOf(userId));
                JSONObject set = new JSONObject();
                set.put("id", userId);
                set.put("count", N);
                String str = "accept";
                if (stats != null) {
                    i = stats.accept;
                } else {
                    i = 0;
                }
                set.put(str, i);
                str = "reject";
                if (stats != null) {
                    i = stats.reject;
                } else {
                    i = 0;
                }
                set.put(str, i);
                str = "acquire";
                if (stats != null) {
                    i = stats.acquire;
                } else {
                    i = 0;
                }
                set.put(str, i);
                str = "lockout";
                if (stats != null) {
                    i = stats.lockout;
                } else {
                    i = 0;
                }
                set.put(str, i);
                str = "permanentLockout";
                if (stats != null) {
                    i = stats.permanentLockout;
                } else {
                    i = 0;
                }
                set.put(str, i);
                str = "acceptCrypto";
                if (cryptoStats != null) {
                    i = cryptoStats.accept;
                } else {
                    i = 0;
                }
                set.put(str, i);
                str = "rejectCrypto";
                if (cryptoStats != null) {
                    i = cryptoStats.reject;
                } else {
                    i = 0;
                }
                set.put(str, i);
                str = "acquireCrypto";
                if (cryptoStats != null) {
                    i = cryptoStats.acquire;
                } else {
                    i = 0;
                }
                set.put(str, i);
                str = "lockoutCrypto";
                if (cryptoStats != null) {
                    i = cryptoStats.lockout;
                } else {
                    i = 0;
                }
                set.put(str, i);
                sets.put(set);
            }
            dump.put("prints", sets);
        } catch (JSONException e) {
            Slog.e(TAG, "dump formatting failure", e);
        }
        pw.println(dump);
    }

    private void dumpProto(FileDescriptor fd) {
        ProtoOutputStream proto = new ProtoOutputStream(fd);
        for (UserInfo user : UserManager.get(getContext()).getUsers()) {
            int userId = user.getUserHandle().getIdentifier();
            long userToken = proto.start(2272037699585L);
            proto.write(1112396529665L, userId);
            proto.write(1112396529666L, this.mFingerprintUtils.getFingerprintsForUser(this.mContext, userId).size());
            PerformanceStats normal = (PerformanceStats) this.mPerformanceMap.get(Integer.valueOf(userId));
            if (normal != null) {
                long countsToken = proto.start(1172526071811L);
                proto.write(1112396529665L, normal.accept);
                proto.write(1112396529666L, normal.reject);
                proto.write(1112396529667L, normal.acquire);
                proto.write(1112396529668L, normal.lockout);
                proto.write(1112396529669L, normal.lockout);
                proto.end(countsToken);
            }
            PerformanceStats crypto = (PerformanceStats) this.mCryptoPerformanceMap.get(Integer.valueOf(userId));
            if (crypto != null) {
                countsToken = proto.start(1172526071812L);
                proto.write(1112396529665L, crypto.accept);
                proto.write(1112396529666L, crypto.reject);
                proto.write(1112396529667L, crypto.acquire);
                proto.write(1112396529668L, crypto.lockout);
                proto.write(1112396529669L, crypto.lockout);
                proto.end(countsToken);
            }
            proto.end(userToken);
        }
        proto.flush();
    }

    public void onStart() {
        publishBinderService("fingerprint", new FingerprintServiceWrapper());
        SystemServerInitThreadPool.get().submit(new -$Lambda$NsdFXKe2P39OH-qCAY_zqOMIIsg(this), "FingerprintService.onStart");
        listenForUserSwitches();
    }

    /* synthetic */ void -com_android_server_fingerprint_FingerprintService-mthref-0() {
        getFingerprintDaemon();
    }

    public void onBootPhase(int phase) {
        Slog.d(TAG, "Fingerprint daemon is phase :" + phase);
        if (phase == 1000) {
            Slog.d(TAG, "Fingerprint mDaemon is " + this.mDaemon);
            if (getFingerprintDaemon() == null) {
                Slog.w(TAG, "Fingerprint daemon is null");
            }
        }
    }

    private void updateActiveGroup(int userId, String clientPackage) {
        IBiometricsFingerprint daemon = getFingerprintDaemon();
        if (daemon != null) {
            try {
                userId = getUserOrWorkProfileId(clientPackage, userId);
                if (userId != this.mCurrentUserId) {
                    File systemDir;
                    int userIdForHal = userId;
                    UserInfo info = this.mUserManager.getUserInfo(userId);
                    if (info != null && info.isHwHiddenSpace()) {
                        userIdForHal = HIDDEN_SPACE_ID;
                        Slog.i(TAG, "userIdForHal is " + HIDDEN_SPACE_ID);
                    }
                    if (userIdForHal == HIDDEN_SPACE_ID) {
                        Slog.i(TAG, "userIdForHal == HIDDEN_SPACE_ID");
                        systemDir = Environment.getUserSystemDirectory(0);
                    } else {
                        systemDir = Environment.getUserSystemDirectory(userId);
                    }
                    File fpDir = new File(systemDir, FP_DATA_DIR);
                    if (!fpDir.exists()) {
                        if (!fpDir.mkdir()) {
                            Slog.v(TAG, "Cannot make directory: " + fpDir.getAbsolutePath());
                            return;
                        } else if (!SELinux.restorecon(fpDir)) {
                            Slog.w(TAG, "Restorecons failed. Directory will have wrong label.");
                            return;
                        }
                    }
                    daemon.setActiveGroup(userIdForHal, fpDir.getAbsolutePath());
                    this.mCurrentUserId = userId;
                    updateFingerprints(userId);
                }
                if (this.mFingerprintUtils.isDualFp()) {
                    long authenticatorId = this.mFingerprintUtils.getFingerprintsForUser(this.mContext, userId, -1).size() > 0 ? daemon.getAuthenticatorId() : 0;
                    Slog.d(TAG, "daemon getAuthenticatorId = " + authenticatorId + " userId = " + userId);
                    this.mAuthenticatorIds.put(Integer.valueOf(userId), Long.valueOf(authenticatorId));
                } else {
                    this.mAuthenticatorIds.put(Integer.valueOf(userId), Long.valueOf(hasEnrolledFingerprints(userId) ? daemon.getAuthenticatorId() : 0));
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "Failed to setActiveGroup():", e);
            }
        }
    }

    private int getUserOrWorkProfileId(String clientPackage, int userId) {
        if (isKeyguard(clientPackage) || !isWorkProfile(userId)) {
            return getEffectiveUserId(userId);
        }
        return userId;
    }

    private boolean isWorkProfile(int userId) {
        UserInfo userInfo = null;
        long token = Binder.clearCallingIdentity();
        try {
            userInfo = this.mUserManager.getUserInfo(userId);
            return userInfo != null ? userInfo.isManagedProfile() : false;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void listenForUserSwitches() {
        try {
            ActivityManager.getService().registerUserSwitchObserver(new SynchronousUserSwitchObserver() {
                public void onUserSwitching(int newUserId) throws RemoteException {
                    Slog.w(FingerprintService.TAG, "onUserSwitching");
                    FingerprintService.this.mHandler.obtainMessage(10, newUserId, 0).sendToTarget();
                }
            }, TAG);
        } catch (RemoteException e) {
            Slog.w(TAG, "Failed to listen for user switching event", e);
        }
    }

    public long getAuthenticatorId(String opPackageName) {
        return ((Long) this.mAuthenticatorIds.getOrDefault(Integer.valueOf(getUserOrWorkProfileId(opPackageName, UserHandle.getCallingUserId())), Long.valueOf(0))).longValue();
    }
}
