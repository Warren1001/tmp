package com.kabryxis.tmp.user;

import com.kabryxis.kabutils.data.file.yaml.ConfigSection;
import com.kabryxis.tmp.media.Episode;
import com.kabryxis.tmp.media.Season;

public class SeasonTracker {
	
	private final User user;
	private final ShowTracker showTracker;
	private final Season season;
	private final ConfigSection section;
	private final EpisodeTracker[] episodeTrackers;
	
	public SeasonTracker(User user, ShowTracker showTracker, Season season) {
		this.user = user;
		this.showTracker = showTracker;
		this.season = season;
		section = user.getData().computeSectionIfAbsent(season.getShow().getName() + ".seasons." + season.getNumber());
		Episode[] episodes = season.getMainEpisodes();
		episodeTrackers = new EpisodeTracker[episodes.length];
		for(int i = 0; i < episodes.length; i++) {
			episodeTrackers[i] = new EpisodeTracker(user, this, episodes[i]);
		}
	}
	
	public ShowTracker getShowTracker() {
		return showTracker;
	}
	
	public EpisodeTracker getEpisodeTracker(int number) {
		return episodeTrackers[number - 1];
	}
	
	public void setLastEpisode(int number) {
		section.put("last-episode", number);
	}
	
	public Episode getLastEpisode() {
		return season.getEpisode(section.getInt("last-episode", 1));
	}
	
}
