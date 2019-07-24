package com.kabryxis.tmp.media;

import com.kabryxis.kabutils.data.Maths;
import com.kabryxis.kabutils.data.file.yaml.ConfigSection;
import com.kabryxis.tmp.user.EpisodeTracker;
import uk.co.caprica.vlcj.player.MediaPlayer;

import java.io.File;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class Episode {
	
	public static final String[] VALID_EXTENSIONS = { ".mp4", ".mkv" };
	
	private final Season season;
	private final int number;
	private final File episodeFile;
	private final String description;
	private final String dataPath;
	private final File extraSubtitles;
	
	private ConfigSection section;
	private Set<long[]> skipSegments;
	
	public Episode(Season season, int number, File episodeFile, ConfigSection section) {
		this.season = season;
		this.number = number;
		this.section = section;
		dataPath = null;
		this.episodeFile = episodeFile;
		description = section.get("description");
		ConfigSection skipSection = section.get("skip");
		if(skipSection != null) {
			skipSegments = new HashSet<>();
			skipSection.forEach((skipKey, skipValue) -> {
				if(skipKey.equalsIgnoreCase("custom")) {
					((ConfigSection)skipValue).forEach((customSkipKey, customSkipValue) -> {
						long[] skipSegment = {Long.parseLong(customSkipKey), (Long)customSkipValue};
						skipSegments.add(skipSegment);
					});
				}
				else {
					long duration = season.getSkipDuration(Integer.parseInt(skipKey));
					if(duration != 0L) {
						long offset = Maths.toLong(skipValue);
						long[] skipSegment = {offset, offset + duration};
						skipSegments.add(skipSegment);
					}
				}
			});
		}
		else skipSegments = null;
		String loadExtraSubs = section.get("load-extra-subs");
		if(loadExtraSubs != null) extraSubtitles = new File(season.getDirectory(), loadExtraSubs);
		else extraSubtitles = null;
	}
	
	/*public Episode(Season season, int number, String path, String dataPath) {
		this.season = season;
		this.number = number;
		this.dataPath = dataPath;
		episodeFile = new File(season.getShow().getDirectory(), path);
		description = null;
		timestampSkipSegments = season.getTimestampSkipSegments();
		chapterSkipSegments = season.getChapterSkipSegments();
		extraSubtitles = null;
	}*/
	
	public Episode(Season season, int number, File episodeFile) {
		this.season = season;
		this.number = number;
		dataPath = null;
		this.episodeFile = episodeFile;
		description = null;
		skipSegments = null;
		extraSubtitles = null;
	}
	
	public Season getSeason() {
		return season;
	}
	
	public int getNumber() {
		return number;
	}
	
	public File getEpisodeFile() {
		return episodeFile;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void addCustomSkipSegment(long start, long end) {
		if(skipSegments == null) skipSegments = new HashSet<>();
		long[] segment = {start, end};
		skipSegments.add(segment);
		transformDataSection();
		section.put("skip.custom." + start, end);
		section.requestSave();
	}
	
	public void addDefinedSkipSegment(int id, long start) {
		if(skipSegments == null) skipSegments = new HashSet<>();
		long[] segment = {start, start + season.getSkipDuration(id)};
		skipSegments.add(segment);
		transformDataSection();
		section.put("skip." + id, start);
		section.requestSave();
		System.out.println(String.format("episode %sx%s of %s added skip segment %s", season.getNumber(), number, season.getShow().getFriendlyName(), id));
	}
	
	public boolean checkForSkip(MediaPlayer mediaPlayer, long time) {
		if(skipSegments != null) {
			Optional<long[]> optionalSegment = skipSegments.stream().filter(segment -> time >= segment[0] && time < segment[1]).findFirst();
			if(optionalSegment.isPresent()) {
				long end = optionalSegment.get()[1];
				if(end + 3000 >= mediaPlayer.getLength()) season.getShow().getMediaManager().getTMP().playNextEpisode();
				else if(!checkForSkip(mediaPlayer, end + 3000)) mediaPlayer.setTime(end);
				return true;
			}
		}
		return false;
	}
	
	public Episode getNextEpisode() {
		return season.getEpisode(number + 1);
	}
	
	public void play() {
		System.out.println(String.format("episode %sx%s of %s has %s skip segments", season.getNumber(), number, season.getShow().getFriendlyName(), skipSegments == null ? 0 : skipSegments.size()));
		season.getShow().getMediaManager().getTMP().playEpisode(this);
	}
	
	private void transformDataSection() {
		if(section == null) {
			section = new ConfigSection();
			String path = season.getShow().getData().get(String.format("seasons.%s.%s", season.getNumber(), number));
			if(path != null) section.put("path", path);
			season.getShow().getData().put(String.format("seasons.%s.%s", season.getNumber(), number), section);
		}
	}
	
	public void onPlay(MediaPlayer player) {
		System.out.println("CALLED");
		season.onPlay(player);
		if(extraSubtitles != null && extraSubtitles.exists()) player.setSubTitleFile(extraSubtitles);
		int spu = section.getInt("spu", -2);
		if(spu != -2) player.setSpu(spu);
	}
	
	public EpisodeTracker getTracker() {
		return season.getTracker().getEpisodeTracker(number);
	}
	
	public void setSpu(int spu) {
		transformDataSection();
		section.put("spu-id", spu);
		section.requestSave();
	}
	
	public int getSpu() {
		return section == null ? -2 : section.getInt("sub-track-name", -2);
	}
	
}
