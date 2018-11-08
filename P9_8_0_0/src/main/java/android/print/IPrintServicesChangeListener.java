package android.print;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPrintServicesChangeListener extends IInterface {

    public static abstract class Stub extends Binder implements IPrintServicesChangeListener {
        private static final String DESCRIPTOR = "android.print.IPrintServicesChangeListener";
        static final int TRANSACTION_onPrintServicesChanged = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPrintServicesChangeListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPrintServicesChangeListener)) {
                return new Proxy(obj);
            }
            return (IPrintServicesChangeListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onPrintServicesChanged();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onPrintServicesChanged() throws RemoteException;
}
