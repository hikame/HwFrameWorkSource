package com.huawei.android.pushagent.model.b;

import android.content.Context;
import android.content.Intent;
import com.huawei.android.pushagent.datatype.tcp.PushDataReqMessage;
import com.huawei.android.pushagent.model.a.e;
import com.huawei.android.pushagent.model.a.g;
import com.huawei.android.pushagent.utils.b;
import com.huawei.android.pushagent.utils.d.c;
import java.io.UnsupportedEncodingException;

public class f extends c {
    public f(Context context, PushDataReqMessage pushDataReqMessage) {
        super(context, pushDataReqMessage);
    }

    protected void ii() {
        if (-1 == this.cb) {
            this.cb = com.huawei.android.pushagent.utils.f.vb();
        }
    }

    protected boolean ic(Context context) {
        if (1 != g.aq(context).ba() || (im(context) ^ 1) == 0 || (com.huawei.android.pushagent.model.d.f.kv(context, this.pkgName, String.valueOf(this.cb)) ^ 1) == 0) {
            return true;
        }
        c.sh("PushLog2951", this.pkgName + "'s token is not exist in current user");
        return false;
    }

    protected boolean ib(Context context) {
        if (1 != g.aq(context).ba() || (im(context) ^ 1) == 0 || (com.huawei.android.pushagent.model.d.f.kw(context, this.pkgName, String.valueOf(this.cb), this.tokenBytes) ^ 1) == 0) {
            return true;
        }
        c.sh("PushLog2951", this.pkgName + "'s token is not equal in current user");
        return false;
    }

    protected boolean hz(Context context) {
        if (!e.ae(context).af(b.ue(this.pkgName, String.valueOf(this.cb)))) {
            return true;
        }
        c.sg("PushLog2951", "closePush_Notify, pkgName is " + this.pkgName);
        return false;
    }

    protected boolean ia(Context context) {
        if (b.tv(context, this.pkgName, this.cb)) {
            return true;
        }
        return false;
    }

    protected boolean hy(Context context) {
        if (b.tv(context, "com.huawei.android.pushagent", this.cb)) {
            return true;
        }
        return false;
    }

    protected void id(Context context) {
        c.sg("PushLog2951", "try to send selfshow msg to push client ,package " + this.pkgName + " to depose selfshow msg");
        Intent intent = new Intent("com.huawei.intent.action.PUSH");
        intent.putExtra("selfshow_info", this.cc);
        intent.putExtra("selfshow_token", this.tokenBytes);
        intent.setFlags(32);
        if (hy(context)) {
            intent.setPackage("com.huawei.android.pushagent");
            try {
                intent.putExtra("extra_encrypt_data", com.huawei.android.pushagent.utils.a.e.nw("com.huawei.android.pushagent", b.ug().getBytes("UTF-8")));
            } catch (UnsupportedEncodingException e) {
                c.sf("PushLog2951", e.toString());
            }
        } else {
            intent.setPackage(this.pkgName);
        }
        b.tr(context, intent, this.cb);
    }
}
