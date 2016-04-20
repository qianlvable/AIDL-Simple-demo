package com.lvable.ningjiaqi.contactserver;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by ningjiaqi on 16/4/18.
 */
public class Utils {
    public static void addContact(Context context, String name, String phone) {
        new AddContactTask(context).execute(name,phone);
    }

    public static void deleteContact(Context context, String contactId) {
        if (TextUtils.isEmpty(contactId))
            return;

        String where = ContactsContract.Data.CONTACT_ID + " = ? ";
        String[] param = {contactId};
        ArrayList<android.content.ContentProviderOperation> ops = new ArrayList<>();
        ops.add(ContentProviderOperation
                .newDelete(android.provider.ContactsContract.Data.CONTENT_URI)
                .withSelection(where,param).build());
        ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                .withSelection(ContactsContract.RawContacts.CONTACT_ID + "=" + contactId, null)
                .build());
        ContentResolver contentResolver = context.getContentResolver();
        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public static boolean updateContact(Context context, String name
            , String number, String contactId) {
        boolean success = true;

        try {

            if (TextUtils.isEmpty(name) && TextUtils.isEmpty(number) && TextUtils.isEmpty(contactId)) {
                success = false;
            } else {
                ContentResolver contentResolver = context.getContentResolver();

                String where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";

                String[] nameParams = new String[]{contactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE};
                String[] numberParams = new String[]{contactId, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE};

                ArrayList<android.content.ContentProviderOperation> ops = new ArrayList<>();
                ops.add(android.content.ContentProviderOperation.newUpdate(android.provider.ContactsContract.Data.CONTENT_URI)
                        .withSelection(where, nameParams)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                        .build());

                ops.add(android.content.ContentProviderOperation.newUpdate(android.provider.ContactsContract.Data.CONTENT_URI)
                        .withSelection(where, numberParams)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                        .build());

                contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
            }
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }


    public static ArrayList<String> getAllConactIds(Context context) {
        ArrayList<String> contactList = new ArrayList<String>();

        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projects = {ContactsContract.Data.DISPLAY_NAME
                , ContactsContract.Data.CONTACT_ID
                , ContactsContract.CommonDataKinds.Phone.NUMBER };
        Cursor cursor = contentResolver.query(uri, projects,null,null,null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String _id = cursor
                            .getString(cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                    String name = cursor.getString(cursor
                            .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String phone = cursor.getString(
                            cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    contactList.add(name+" , "+_id+" , "+phone);
                }
                while (cursor.moveToNext());
            }
        }

        return contactList;
    }

    public static class AddContactTask extends AsyncTask<String,Void,Void> {
        private WeakReference<Context> mContext;
        public AddContactTask(Context context) {
            mContext = new WeakReference<Context>(context);
        }
        @Override
        protected Void doInBackground(String... params) {
            String name = params[0];
            String phone = params[1];
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                    .build());
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                    .build());

            try {
                if (mContext != null) {
                    mContext.get().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                }
            } catch (Exception e) {
                // Log exception
                Log.e("wtf", "Exceptoin encoutered while inserting contact: " + e);
            }
            return null;
        }
    }
}
