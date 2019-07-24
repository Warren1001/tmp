package com.kabryxis.tmp;

import com.kabryxis.tmp.media.Show;
import com.kabryxis.tmp.user.User;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SortOption implements Comparator<Show> {
	
	private static final Map<String, SortOption> ORDER_BY_NAME = new HashMap<>();
	
	public static final SortOption USER_ORDER = new SortOption("USER_ORDER") {
		
		@Override
		public int compare(Show show1, Show show2) {
			User selectedUser = show1.getMediaManager().getTMP().getUserManager().getSelectedUser();
			List<SortOption> sortOptionList = selectedUser.getSortOptionOrder();
			for(SortOption option : sortOptionList) {
				int compare = option.compare(show1, show2);
				if(compare != 0) return compare;
			}
			return ALPHABETICAL.compare(show1, show2);
		}
		
	};
	public static final SortOption LAST_SEEN = new SortOption("LAST_SEEN") {
		
		@Override
		public int compare(Show show1, Show show2) {
			User selectedUser = show1.getMediaManager().getTMP().getUserManager().getSelectedUser();
			int compare = Long.compare(selectedUser.getShowTracker(show2).getLastWatched(), selectedUser.getShowTracker(show1).getLastWatched());
			return compare == 0 ? ALPHABETICAL.compare(show1, show2) : compare;
		}
		
	};
	public static final SortOption ALPHABETICAL = new SortOption("ALPHABETICAL") {
		
		@Override
		public int compare(Show show1, Show show2) {
			return String.CASE_INSENSITIVE_ORDER.compare(show1.getFriendlyName(), show2.getFriendlyName());
		}
		
	};
	public static final SortOption RECENTLY_ADDED = new SortOption("RECENTLY_ADDED") {
		
		@Override
		public int compare(Show show1, Show show2) {
			long diff = show1.getAdded() - show2.getAdded();
			if(diff >= 1000) return -1; // backwards
			else if(diff <= -1000) return 1;
			else return ALPHABETICAL.compare(show1, show2);
		}
		
	};
	public static final SortOption RATING = new SortOption("RATING") {
		
		@Override
		public int compare(Show show1, Show show2) {
			int compare = Double.compare(show2.getAverageRating(), show1.getAverageRating());
			return compare == 0 ? ALPHABETICAL.compare(show1, show2) : compare;
		}
		
	};
	
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
