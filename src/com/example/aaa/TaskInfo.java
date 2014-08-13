package com.example.aaa;

import java.io.Serializable;

public class TaskInfo implements Serializable {
	private static final long serialVersionUID = -2810508248527772902L;

	public static final int WAITING = 0;
	public static final int RUNNING = 1;
	public static final int CANCELED = 2;

	private int taskId;
	private String taskName;
	private int progress;
	private int status;

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}
