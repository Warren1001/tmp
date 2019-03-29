package com.kabryxis.tmp;

import com.kabryxis.tmp.media.Show;
import com.kabryxis.tmp.user.ShowTracker;
import com.kabryxis.tmp.user.User;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class SortOption implements Comparator<Show> {
	
	private static final Map<String, SortOption> ORDER_BY_NAME = new HashMap<>();
	
	public static final SortOption LAST_SEEN = new SortOption("LAST_SEEN") {
		
		@Override
		public int compare(Show show1, Show show2) {
			User selectedUser = show1.getMediaManager().getTMP().getUserManager().getSelectedUser();
			ShowTracker showTracker1 = selectedUser.getShowTracker(show1);
			ShowTracker showTracker2 = selectedUser.getShowTracker(show2);
			long lastSeen1 = showTracker1.getLastWatched();
			long lastSeen2 = showTracker2.getLastWatched();
			return lastSeen1 >= lastSeen2 ? (lastSeen1 == lastSeen2 ? ALPHABETICAL.compare(show1, show2) : -1) : 1;
		}
		
	};
	public static final SortOption ALPHABETICAL = new SortOption("ALPHABETICAL") {
		
		@Override
		public int compare(Show show1, Show show2) {
			return String.CASE_INSENSITIVE_ORDER.compare(show1.getFriendlyName(), show2.getFriendlyName());
		}
		
	};
	public static final SortOption RECENTLY_ADDED = new SortOption("RECENTLY_ADDED");
	public static final SortOption RATING = new SortOption("RATING");
	
	public static SortOption getByName(String name) {
		return ORDER_BY_NAME.get(name.toUpperCase());
	}
	
	private final String name;
	
	private SortOption(String name) {
		this.name = name;
		ORDER_BY_NAME.put(name, this);
	}
	
	@Override
	public int compare(Show o1, Show o2) {
		return 0;
	}
	
	public String getName() {
		return name;
	}
	
}
