package com.metaweb.gridlock.history;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;

import com.metaweb.gridlock.ProjectManager;
import com.metaweb.gridlock.model.Project;

public class History implements Serializable {
	private static final long serialVersionUID = -1529783362243627391L;
	
	protected long				 _projectID;
	protected List<HistoryEntry> _pastEntries;
	protected List<HistoryEntry> _futureEntries;
	
	public History(Project project) {
		_projectID = project.id;
		_pastEntries = new ArrayList<HistoryEntry>();
		_futureEntries = new ArrayList<HistoryEntry>();
	}
	
	public void addEntry(HistoryEntry entry) {
		_futureEntries.clear();
		_pastEntries.add(entry);
		entry.apply(ProjectManager.singleton.getProject(_projectID));
	}
	
	public List<HistoryEntry> getLastPastEntries(int count) {
		return _pastEntries.subList(Math.max(_pastEntries.size() - count, 0), _pastEntries.size());
	}
	
	public void undoRedo(long lastDoneEntryID) {
		if (lastDoneEntryID == 0) {
			undo(_pastEntries.size());
		} else {
			for (int i = 0; i < _pastEntries.size(); i++) {
				if (_pastEntries.get(i).id == lastDoneEntryID) {
					undo(_pastEntries.size() - i - 1);
					return;
				}
			}
			
			for (int i = 0; i < _futureEntries.size(); i++) {
				if (_futureEntries.get(i).id == lastDoneEntryID) {
					redo(i + 1);
					return;
				}
			}
		}
	}
	
	protected void undo(int times) {
		Project project = ProjectManager.singleton.getProject(_projectID);
		
		while (times > 0 && _pastEntries.size() > 0) {
			HistoryEntry entry = _pastEntries.remove(_pastEntries.size() - 1);
			times--;
			
			entry.revert(project);
			
			_futureEntries.add(0, entry);
		}
	}
	
	protected void redo(int times) {
		Project project = ProjectManager.singleton.getProject(_projectID);
		
		while (times > 0 && _futureEntries.size() > 0) {
			HistoryEntry entry = _futureEntries.remove(0);
			times--;
			
			entry.apply(project);
			
			_pastEntries.add(entry);
		}
	}
	
	public JSONObject getJSON(Properties options) throws JSONException {
		JSONObject o = new JSONObject();
		
		List<JSONObject> a = new ArrayList<JSONObject>(_pastEntries.size());
		for (HistoryEntry entry : _pastEntries) {
			a.add(entry.getJSON(options));
		}
		o.put("past", a);
		
		List<JSONObject> b = new ArrayList<JSONObject>(_futureEntries.size());
		for (HistoryEntry entry : _futureEntries) {
			b.add(entry.getJSON(options));
		}
		o.put("future", b);
		
		return o;
	}
}