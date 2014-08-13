package com.example.aaa;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

public class DownloadService extends Service {
	private static final String TAG = "DownloadService";
	ServiceBinder mBinder;
	ArrayList<TaskInfo> mDownloadQueue;
	NotificationManager mNotificationManager;
	Notification mNotification;
	RemoteViews mRemoteView;
	boolean bNotifyWhenUpdate;
	boolean bNotifyWhenFinished;
	boolean isRunning;
	static final int DOWNLOAD_STATUS_UPDATE = 1;
	static final int DOWNLOAD_STATUS_SUCCESS = 2;
	private final static String ACTION_UPDATE = "com.chris.download.service.UPDATE";
	private final static String ACTION_FINISHED = "com.chris.download.service.FINISHED";

	@Override
	public IBinder onBind(Intent intent) {
		Log.e(TAG, "onBind");
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		TaskInfo ti = (TaskInfo) intent.getSerializableExtra("ti");
		addTaskInQueue(ti);
		Log.e(TAG, "onStartCommand");
		return START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.e(TAG, "onCreate");
		mBinder = new ServiceBinder();
		mDownloadQueue = new ArrayList<TaskInfo>();
		mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		mNotification = new Notification();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mBinder = null;
		mDownloadQueue = null;
		mNotificationManager = null;
		mNotification = null;
		mRemoteView = null;
		Log.e(TAG, "onDestroy");
	}

	public class ServiceBinder extends Binder {
		public DownloadService getService() {
			return DownloadService.this;
		}
	}

	public void notifyToActivity(boolean update, boolean finished) {
		bNotifyWhenUpdate = update;
		bNotifyWhenFinished = finished;
	}

	public void addTaskInQueue(TaskInfo ti) {
		if (mDownloadQueue != null) {
			mDownloadQueue.add(ti);
			Log.e(TAG, "addTaskInQueue id = " + ti.getTaskId());
		}

		if (isRunning == false && mDownloadQueue.size() > 0) {
			startDownload();
		}
	}

	public void cancelTaskById(int id) {
		Log.e(TAG, "cancelTaskById id = " + id);
		for (int i = 0; i < mDownloadQueue.size(); i++) {
			TaskInfo ti = mDownloadQueue.get(i);
			if (ti.getTaskId() == id) {
				if (ti.getStatus() == TaskInfo.RUNNING) {
					ti.setStatus(TaskInfo.CANCELED);
				} else {
					mDownloadQueue.remove(i);
				}
				break;
			}
		}
	}

	private void startDownload() {
		if (isRunning) {
			return;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (mDownloadQueue != null && mDownloadQueue.size() > 0) {
					isRunning = true;

					TaskInfo ti = mDownloadQueue.get(0);
					while (ti.getProgress() < 100
							&& ti.getStatus() != TaskInfo.CANCELED) {
						Message msg = mHandler.obtainMessage(
								DOWNLOAD_STATUS_UPDATE, ti);
						mHandler.sendMessage(msg);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						ti.setProgress(ti.getProgress() + 10);
					}

					if (ti.getProgress() == 100 && mDownloadQueue.size() == 1) {
						Log.e(TAG, ti.getTaskName() + " is finished!");
						Message msg = mHandler.obtainMessage(
								DOWNLOAD_STATUS_SUCCESS, ti);
						mHandler.sendMessage(msg);
					} else if (ti.getStatus() == TaskInfo.CANCELED) {
						Log.e(TAG, ti.getTaskName() + " is canceled!");
					}
					if (mDownloadQueue != null) {
						mDownloadQueue.remove(ti);
					}
				}
				isRunning = false;
			}
		}).start();
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWNLOAD_STATUS_UPDATE: {
				mNotification.icon = R.drawable.ic_launcher;
				mNotification.when = System.currentTimeMillis();
				mNotification.tickerText = "开始下载...";
				// 放置在"正在运行"栏目中
				mNotification.flags = Notification.FLAG_ONGOING_EVENT;

				TaskInfo ti = (TaskInfo) msg.obj;
				Log.e(TAG, "update : progress = " + ti.getProgress());

				notifyUpdate(ti);
				break;
			}

			case DOWNLOAD_STATUS_SUCCESS: {
				mNotification.flags = Notification.FLAG_AUTO_CANCEL;
				mNotification.contentView = null;
				Intent it = new Intent(DownloadService.this,
						DownloadService.class);
				PendingIntent pi = PendingIntent.getActivity(
						DownloadService.this, 0, it,
						PendingIntent.FLAG_UPDATE_CURRENT);
				mNotification.setLatestEventInfo(DownloadService.this, "下载完成",
						"文件已下载完毕", pi);
				mNotificationManager.notify(1, mNotification);

				notifyFinished(true);
				break;
			}

			default:
				break;
			}
		}
	};

	private void notifyUpdate(TaskInfo ti) {
		if (bNotifyWhenUpdate) {
			Intent it = new Intent(ACTION_UPDATE);
			it.putExtra("progress", ti.getProgress());
			DownloadService.this.sendBroadcast(it);
		}
	}

	private void notifyFinished(boolean isSuccess) {
		if (bNotifyWhenFinished) {
			Intent it = new Intent(ACTION_FINISHED);
			it.putExtra("success", isSuccess);
			DownloadService.this.sendBroadcast(it);
		}
	}
}
