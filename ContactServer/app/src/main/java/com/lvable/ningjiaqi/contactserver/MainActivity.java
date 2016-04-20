package com.lvable.ningjiaqi.contactserver;

import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    private SimpleCursorAdapter mAdapter;
    public static final int CONTACT_LOADER_ID = 78;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] uiBindFrom = {   ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_URI,
                ContactsContract.CommonDataKinds.Phone.NUMBER};
        int[] uiBindTo = { R.id.tv_name, R.id.iv_icon ,R.id.tv_phone};
        mAdapter = new SimpleCursorAdapter(
                this, R.layout.item_layout,
                null, uiBindFrom, uiBindTo,
                0);
        ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(mAdapter);

        getSupportLoaderManager().initLoader(CONTACT_LOADER_ID,
                new Bundle(), this);

    }


    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        String[] projectionFields = new String[] {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.PHOTO_URI,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.Data.CONTACT_ID};

        CursorLoader cursorLoader = new CursorLoader(this,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, // URI
                projectionFields, // projection fields
                null, // the selection criteria
                null, // the selection args
                null // the sort order
        );

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }


}
