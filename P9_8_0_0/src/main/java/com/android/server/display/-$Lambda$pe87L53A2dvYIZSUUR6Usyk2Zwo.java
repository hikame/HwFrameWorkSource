package com.android.server.display;

import android.os.IBinder;
import android.view.SurfaceControl;
import com.android.server.display.VirtualDisplayAdapter.SurfaceControlDisplayFactory;

final /* synthetic */ class -$Lambda$pe87L53A2dvYIZSUUR6Usyk2Zwo implements SurfaceControlDisplayFactory {
    private final /* synthetic */ IBinder $m$0(String arg0, boolean arg1) {
        return SurfaceControl.createDisplay(arg0, arg1);
    }

    public final IBinder createDisplay(String str, boolean z) {
        return $m$0(str, z);
    }
}
