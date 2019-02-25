package com.kabryxis.tmp.user;

import com.kabryxis.kabutils.data.file.yaml.ConfigSection;
import com.kabryxis.tmp.media.Episode;
import com.kabryxis.tmp.media.Season;

public class EpisodeTracker {
	
	private final User user;
	private final SeasonTracker seasonTracker;
	private final Episode episode;
	private final ConfigSection section;
	
	private long introStartTime = -1L;
	
	public EpisodeTracker(User user, SeasonTracker seasonTracker, Episode episode) {
		this.user = user;
		this.seasonTracker = seasonTracker;
		this.episode = episode;
		Season season = episode.getSeason();
		section = user.getData().computeSectionIfAbsent(season.getShow().getName() + ".seasons." + season.getNumber() + ".main." + episode.getNumber());
	}
	
	public SeasonTracker getSeasonTracker() {
		return seasonTracker;
	}
	
	public boolean hasSeen() {
		return section.getBoolean("seen", false);
	}
	
	public void clicked() {
		section.put("seen", true);
		//user.getData().save();
	}
	
	public void setLastSeenTime(long time) {
		if(time == 0) section.remove("last-seen-time");
		else section.put("last-seen-time", time);
	}
	
	public long getLastSeenTime() {
		return section.getLong("last-seen-time", 0L);
	}
	
	public void setIntroStartTime(long introStartTime) {
		this.introStartTime = introStartTime;
		episode.setIntroStartTime(introStartTime);
	}
	
	public boolean isIntroStartTimeSet() {
		return introStartTime != -1L;
	}
	
	public void setIntroLength(long introEndTime) {
		episode.getSeason().setIntroLength(introEndTime - introStartTime);
		introStartTime = -1L;
	}
	
}
