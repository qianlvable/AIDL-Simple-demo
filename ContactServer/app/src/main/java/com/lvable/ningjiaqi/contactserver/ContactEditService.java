package com.lvable.ningjiaqi.contactserver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.concurrent.RunnableFuture;

/**
 * Created by ningjiaqi on 16/4/18.
 */
public class ContactEditService extends Service {
    private final IContactEditor.Stub mBinder = new IContactEditor.Stub() {
        @Override
        public void addContact(String name, String phone) throws RemoteException {
            Utils.addContact(getApplicationContext(),name,phone);
        }

        @Override
        public void updateContact(String name, String number, String contactId) throws RemoteException {
            Utils.updateContact(getApplicationContext(),name,number,contactId);
        }

        @Override
        public void delteContact(String contactId) throws RemoteException {
            Utils.deleteContact(getApplicationContext(),contactId);
        }

        @Override
        public List<String> getAllConactIds() throws RemoteException {
            return Utils.getAllConactIds(getApplicationContext());
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
