package com.kabryxis.tmp.user;

import com.kabryxis.kabutils.data.file.yaml.ConfigSection;
import com.kabryxis.tmp.media.Season;
import com.kabryxis.tmp.media.Show;

public class ShowTracker {
	
	private final User user;
	private final Show show;
	private final ConfigSection section;
	private final SeasonTracker[] seasonTrackers;
	
	public ShowTracker(User user, Show show) {
		this.user = user;
		this.show = show;
		section = user.getData().computeSectionIfAbsent(show.getName());
		Season[] seasons = show.getSeasons();
		seasonTrackers = new SeasonTracker[seasons.length];
		for(int i = 0; i < seasons.length; i++) {
			seasonTrackers[i] = new SeasonTracker(user, this, seasons[i]);
		}
	}
	
	public SeasonTracker getSeasonTracker(int number) {
		return seasonTrackers[number - 1];
	}
	
	public SeasonTracker getLastSeasonTracker() {
		return getSeasonTracker(section.getInt("last-season", 1));
	}
	
	public void setLastSeason(int number) {
		section.put("last-season", number);
	}
	
	public long getLastWatched() {
		return section.getLong("last-seen", 0L);
	}
	
	public void setLastWatched(long time) {
		section.put("last-seen", time);
	}
	
	public void setLastWatched() {
		setLastWatched(System.currentTimeMillis());
	}
	
}
