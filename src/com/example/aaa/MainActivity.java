package com.example.aaa;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	public final static String TAG = "MainActivity";
	private DownloadService mService = null;
	private static int task_count = 0;
	private final static String ACTION_UPDATE = "com.chris.download.service.UPDATE";
	private final static String ACTION_FINISHED = "com.chris.download.service.FINISHED";
	Intent it;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_UPDATE);
		filter.addAction(ACTION_FINISHED);
		registerReceiver(myReceiver, filter);

		it =  new Intent(this, DownloadService.class);


		Button add_task = (Button) findViewById(R.id.add_task);
		add_task.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				TaskInfo ti = new TaskInfo();
				ti.setTaskId(task_count++);
				ti.setTaskName(TAG + ti.getTaskId());
				ti.setProgress(0);
				ti.setStatus(TaskInfo.WAITING);
//				mService.addTaskInQueue(ti);
				it.putExtra("ti",ti);
				startService(it);
			}
		});

		Button cancel_task = (Button) findViewById(R.id.cancel_task);
		cancel_task.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				int index = (int) (Math.random() * task_count);
				mService.cancelTaskById(index);
			}
		});
	}

	private BroadcastReceiver myReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ACTION_UPDATE)) {
				TaskInfo progress = (TaskInfo) intent
						.getSerializableExtra("progress");
				Log.e(TAG, "myReceiver - progress = " + progress.getProgress());
			} else if (intent.getAction().equals(ACTION_FINISHED)) {
				boolean isSuccess = intent.getBooleanExtra("success", false);
				Log.e(TAG, "myReceiver - success = " + isSuccess);
			}
		}
	};

//	@Override
//	protected void onResume() {
//		super.onResume();
//		Log.e(TAG, "Activity onResume");
//
//		Intent it = new Intent(this, DownloadService.class);
//		bindService(it, mServiceConn, Service.START_STICKY);
//	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(myReceiver);
	}

	public ServiceConnection mServiceConn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = ((DownloadService.ServiceBinder) service).getService();
			Log.e(TAG, "onServiceConnected: mService = " + mService);

			if (mService != null) {
				mService.notifyToActivity(true, true);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
		}
	};

}
