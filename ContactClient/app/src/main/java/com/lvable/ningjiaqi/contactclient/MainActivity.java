package com.lvable.ningjiaqi.contactclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.lvable.ningjiaqi.contactserver.IContactEditor;

import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private boolean mBound;
    private IContactEditor mService;
    private BaseAdapter mAdapter;
    private Handler mHandler;

    private List<String> mData = null;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = IContactEditor.Stub.asInterface(iBinder);
            mBound = true;
            try {
                mData = mService.getAllConactIds();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if (mData != null) {
                mAdapter = new MyAdapter(mData);
                mLitView.setAdapter(mAdapter);
                mLitView.setOnItemClickListener(MainActivity.this);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
            mBound = false;
        }
    };

    private ListView mLitView;
    private EditText mEtName;
    private EditText mEtPhone;
    private EditText mEtId;

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.lvable.ningjiaqi.contactserver"
                , "com.lvable.ningjiaqi.contactserver.ContactEditService"));

        boolean success = bindService(intent
                , mConnection, Context.BIND_AUTO_CREATE);
        Log.d("wtf","success ? "+success);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        mBound = false;
        mService = null;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLitView = (ListView) findViewById(R.id.list);
        mEtId = (EditText) findViewById(R.id.et_id);
        mEtName = (EditText) findViewById(R.id.et_name);
        mEtPhone = (EditText) findViewById(R.id.et_phone);

        mHandler = new Handler();

    }

    public void addClick(View view) {
        String name = mEtName.getText().toString();
        String phone = mEtPhone.getText().toString();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this,"信息不全",Toast.LENGTH_SHORT).show();
            return;
        }

        if (mBound) {
            try {
                mService.addContact(name,phone);
                // hack:add操作需要一段时间，延迟抓取
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            updateResultList();
                            Toast.makeText(getApplicationContext(),"add success",Toast.LENGTH_SHORT).show();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                },500);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void editClick(View view) {
        String id = mEtId.getText().toString();
        if (TextUtils.isEmpty(id)) {
            Toast.makeText(this,"请在下面列表选取要修改的item",Toast.LENGTH_SHORT).show();
            return;
        }

        String name = mEtName.getText().toString();
        String phone = mEtPhone.getText().toString();
        if (mBound) {
            try {
                mService.updateContact(name,phone,id);
                updateResultList();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }

    private void updateResultList() throws RemoteException {
        List<String> data = mService.getAllConactIds();
        mData.clear();
        mData.addAll(data);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String[] data = adapterView.getAdapter().getItem(i).toString().split(",");
        String name = data[0].trim();
        String phone = data[2].trim();
        String id = data[1].trim();

        mEtId.setText(id);
        mEtPhone.setText(phone);
        mEtName.setText(name);
    }

    public class MyAdapter extends BaseAdapter {
        public List<String> mData;

        public MyAdapter(List<String> mData) {
            this.mData = mData;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int i) {
            return mData.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            View convertView = view;
            if (convertView == null) {
                convertView = LayoutInflater
                        .from(viewGroup.getContext()).inflate(R.layout.item_layout, null);
            }
            TextView infoTv = (TextView) convertView.findViewById(R.id.info);
            infoTv.setText(mData.get(i));
            View btnDel = convertView.findViewById(R.id.btn_delete);
            btnDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String[] item = mData.get(i).split(",");
                    if (item.length > 1) {
                        String id = item[1].trim();
                        mEtId.setText(id);
                        if (mBound) {
                            try {
                                mService.delteContact(id);
                                updateResultList();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
            return convertView;
        }
    }
}
