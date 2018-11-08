package huawei.android.security.facerecognition.task;

import huawei.android.security.facerecognition.FaceRecognizeEvent;
import huawei.android.security.facerecognition.FaceRecognizeManagerImpl.CallbackHolder;
import huawei.android.security.facerecognition.FaceRecognizeManagerImpl.ServiceHolder;
import huawei.android.security.facerecognition.base.HwSecurityMsgCenter;
import huawei.android.security.facerecognition.base.HwSecurityTaskBase;
import huawei.android.security.facerecognition.base.HwSecurityTaskBase.EventListener;
import huawei.android.security.facerecognition.base.HwSecurityTaskBase.RetCallback;
import huawei.android.security.facerecognition.base.HwSecurityTaskBase.TimerOutProc;
import huawei.android.security.facerecognition.base.HwSecurityTimerTask;
import huawei.android.security.facerecognition.request.FaceRecognizeRequest;
import huawei.android.security.facerecognition.utils.LogUtil;

public class DoCancelEnrollTask extends FaceRecognizeTask implements EventListener {
    private static final long TIMEOUT = 3000;
    private int mRetErrorCode;
    private int mRetFaceId;
    private int mRetUserId;
    TimerOutProc mTimeoutProc = new TimerOutProc() {
        public void onTimerOut() {
            DoCancelEnrollTask.this.endWithResult(0);
        }
    };
    private HwSecurityTimerTask mTimer = new HwSecurityTimerTask();

    public DoCancelEnrollTask(HwSecurityTaskBase parent, RetCallback callback, FaceRecognizeRequest request) {
        super(parent, callback, request);
        HwSecurityMsgCenter.staticRegisterEvent(7, this, this);
        HwSecurityMsgCenter.staticRegisterEvent(8, this, this);
        HwSecurityMsgCenter.staticRegisterEvent(9, this, this);
    }

    public int doAction() {
        LogUtil.i("", "do cancel enroll task");
        if (ServiceHolder.getInstance().cancelEnrollment() != 0) {
            return 0;
        }
        this.mTimer.setTimeout(TIMEOUT, this.mTimeoutProc);
        return -1;
    }

    public void onStop() {
        this.mTimer.cancel();
        HwSecurityMsgCenter.staticUnregisterEvent(7, this);
        HwSecurityMsgCenter.staticUnregisterEvent(8, this);
        HwSecurityMsgCenter.staticUnregisterEvent(9, this);
    }

    public boolean onEvent(FaceRecognizeEvent ev) {
        switch (ev.getType()) {
            case 7:
                this.mRetErrorCode = ev.getArgs()[0];
                if (this.mRetErrorCode == 0) {
                    this.mRetFaceId = ev.getArgs()[1];
                    this.mRetUserId = ev.getArgs()[2];
                }
                CallbackHolder.getInstance().onCallbackEvent(this.mTaskRequest.getReqId(), 1, 1, this.mRetErrorCode);
                break;
            case 8:
                CallbackHolder.getInstance().onCallbackEvent(this.mTaskRequest.getReqId(), 1, 3, ev.getArgs()[0]);
                break;
            case 9:
                endWithResult(0);
                break;
            default:
                return false;
        }
        return true;
    }

    public int getErrorCode() {
        return this.mRetErrorCode;
    }

    public int getUserId() {
        return this.mRetUserId;
    }

    public int getFaceId() {
        return this.mRetFaceId;
    }
}
