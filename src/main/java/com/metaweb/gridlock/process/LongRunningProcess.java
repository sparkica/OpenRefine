package com.metaweb.gridlock.process;

import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;

abstract public class LongRunningProcess extends Process {
	final protected String _description;
	protected ProcessManager 	_manager;
	protected Thread 			_thread;
	protected int				_progress; // out of 100
	
	protected LongRunningProcess(String description) {
		_description = description;
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}

	@Override
	JSONObject getJSON(Properties options) throws JSONException {
		JSONObject o = new JSONObject();
		
		o.put("description", _description);
		o.put("immediate", false);
		o.put("status", _thread == null ? "pending" : (_thread.isAlive() ? "running" : "done"));
		o.put("progress", _progress);
		
		return o;
	}

	@Override
	public boolean isImmediate() {
		return false;
	}

	@Override
	public void performImmediate() {
		throw new RuntimeException("Not an immediate process");
	}

	@Override
	public void startPerforming(ProcessManager manager) {
		if (_thread == null) {
			_manager = manager;
			
			_thread = new Thread(getRunnable());
			_thread.start();
		}
	}
	
	abstract protected Runnable getRunnable();
}