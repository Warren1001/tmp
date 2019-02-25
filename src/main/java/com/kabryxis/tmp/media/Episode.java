package com.kabryxis.tmp.media;

import com.kabryxis.kabutils.data.file.yaml.ConfigSection;
import com.kabryxis.tmp.user.EpisodeTracker;
import uk.co.caprica.vlcj.player.MediaPlayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Episode {
	
	public static final String[] VALID_EXTENSIONS = { ".mp4", ".mkv" };
	
	private final Season season;
	private final int number;
	private final File episodeFile;
	private final String description;
	private final String dataPath;
	private final File extraSubtitles;
	
	private Set<long[]> timestampSkipSegments;
	private int[] chapterSkipSegments;
	private ConfigSection section;
	
	public Episode(Season season, int number, File episodeFile, ConfigSection section) {
		this.season = season;
		this.number = number;
		this.section = section;
		dataPath = null;
		this.episodeFile = episodeFile;
		description = section.get("description");
		List<String> list = section.getList("skip", String.class);
		if(list == null || list.isEmpty()) {
			timestampSkipSegments = season.getTimestampSkipSegments();
			chapterSkipSegments = season.getChapterSkipSegments();
		}
		else {
			Set<long[]> timestampsList = new HashSet<>();
			List<Integer> chapterList = new ArrayList<>();
			for(String string : list) {
				String[] args0 = string.split(":", 2);
				String key = args0[0];
				String rest = args0[1];
				if(key.equalsIgnoreCase("c")) chapterList.add(Integer.parseInt(rest));
				else if(key.equalsIgnoreCase("t")) {
					if(rest.contains(",")) {
						String[] args1 = rest.split(",");
						long[] timestampArray = new long[2];
						for(String arg : args1) {
							String[] args2 = arg.split(":");
							String tKey = args2[0];
							long value = Long.parseLong(args2[1]);
							if(tKey.equalsIgnoreCase("s")) timestampArray[0] = value;
							else if(tKey.equalsIgnoreCase("e")) timestampArray[1] = value;
						}
						timestampsList.add(timestampArray);
					}
					else {
						long[] timestampArray = new long[2];
						String[] args2 = rest.split(":");
						String tKey = args2[0];
						long value = Long.parseLong(args2[1]);
						if(tKey.equalsIgnoreCase("s")) timestampArray[0] = value;
						else if(tKey.equalsIgnoreCase("e")) timestampArray[1] = value;
						timestampsList.add(timestampArray);
					}
				}
			}
			timestampSkipSegments = timestampsList;
			chapterSkipSegments = new int[chapterList.size()];
			for(int i = 0; i < chapterList.size(); i++) {
				chapterSkipSegments[i] = chapterList.get(i);
			}
		}
		long introLength = season.getIntroLength();
		if(introLength > 0L) {
			long introStart = section.getLong("intro-start");
			long[] timestamp = new long[2];
			timestamp[0] = introStart;
			timestamp[1] = introStart + introLength;
			if(timestampSkipSegments == null) timestampSkipSegments = new HashSet<>();
			timestampSkipSegments.add(timestamp);
		}
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
		timestampSkipSegments = season.getTimestampSkipSegments();
		chapterSkipSegments = season.getChapterSkipSegments();
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
	
	public Set<long[]> getTimestampSkipSegments() {
		return timestampSkipSegments;
	}
	
	public int[] getChapterSkipSegments() {
		return chapterSkipSegments;
	}
	
	public Episode getNextEpisode() {
		return season.getEpisode(number + 1);
	}
	
	protected File findEpisodeFile(String path) {
		File folder = season.getShow().getDirectory();
		File file = null;
		if(path != null) file = new File(folder, path);
		if(file == null || !file.exists()) {
			for(String ext : VALID_EXTENSIONS) {
				file = new File(folder, season.getNumber() + "x" + number + ext);
				if(file.exists()) break;
			}
		}
		if(!file.exists()) throw new RuntimeException(new FileNotFoundException("could not find season " + season.getNumber() + " episode " + number + " file for show " + season.getShow().getName()));
		return file;
	}
	
	public void setIntroStartTime(long introStartTime) {
		transformDataSection();
		section.put("intro-start", introStartTime);
		section.requestSave();
	}
	
	public void play() {
		season.getShow().getMediaManager().getTMP().playEpisode(this);
	}
	
	private void transformDataSection() {
		if(section == null) {
			section = new ConfigSection();
			String path = season.getShow().getData().get(String.format("seasons.%s.main.%s", season.getNumber(), number));
			if(path != null) section.put("path", path);
			season.getShow().getData().put(String.format("seasons.%s.main.%s", season.getNumber(), number), section);
		}
	}
	
	public void onPlay(MediaPlayer player) {
		if(extraSubtitles != null && extraSubtitles.exists()) player.setSubTitleFile(extraSubtitles);
	}
	
	public EpisodeTracker getTracker() {
		return season.getTracker().getEpisodeTracker(number);
	}
	
	public void setSubtitleTrackName(String trackName) {
		transformDataSection();
		section.put("sub-track-name", trackName);
		section.requestSave();
	}
	
	public String getSubtitleTrackName() {
		return section == null ? null : section.get("sub-track-name");
	}
	
}
