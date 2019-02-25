package com.kabryxis.tmp.media;

import com.kabryxis.kabutils.Images;
import com.kabryxis.kabutils.data.file.Files;
import com.kabryxis.kabutils.data.file.yaml.ConfigSection;
import com.kabryxis.tmp.swing.BasicMouseListener;
import com.kabryxis.tmp.swing.ComponentBuilder;
import com.kabryxis.tmp.swing.JImage;
import com.kabryxis.tmp.swing.TextAreaBuilder;
import com.kabryxis.tmp.user.SeasonTracker;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Season {
	
	private final Show show;
	private final int number;
	private final File directory;
	private final Episode[] mainEpisodes;
	private final Episode[] extraEpisodes;
	private final JPanel pagePanel;
	private final Set<long[]> timestampSkipSegments;
	private final int[] chapterSkipSegments;
	
	private ConfigSection mainSection, extraSection;
	
	public Season(Show show, int number, ConfigSection section) {
		this.show = show;
		this.number = number;
		directory = new File(show.getDirectory(), String.valueOf(number));
		mainSection = section.get("main");
		extraSection = section.get("extra");
		Set<long[]> timestampSkipSegments = null;
		int[] chapterSkipSegments = null;
		if(mainSection != null) {
			List<String> list = mainSection.getList("skip", String.class);
			if(list != null && !list.isEmpty()) {
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
		}
		this.timestampSkipSegments = timestampSkipSegments;
		this.chapterSkipSegments = chapterSkipSegments;
		mainEpisodes = populateEpisodes(directory, mainSection);
		File extrasDirectory = new File(directory, "extras");
		extraEpisodes = extrasDirectory.exists() ? populateEpisodes(extrasDirectory, extraSection) : null;
		pagePanel = new ComponentBuilder<>(new JPanel(null)).bounds(0, 0, 1560 - 40, 900).backgroundColor(Color.DARK_GRAY).build();
	}
	
	public Season(Show show, int number, File directory) {
		this.show = show;
		this.number = number;
		this.directory = directory;
		mainEpisodes = populateEpisodes(directory, null);
		File extrasDirectory = new File(directory, "extras");
		extraEpisodes = extrasDirectory.exists() ? populateEpisodes(extrasDirectory, null) : null;
		pagePanel = new ComponentBuilder<>(new JPanel(null)).bounds(0, 0, 1560 - 40, 900).backgroundColor(Color.DARK_GRAY).build();
		timestampSkipSegments = null;
		chapterSkipSegments = null;
		mainSection = null;
		extraSection = null;
	}
	
	public Show getShow() {
		return show;
	}
	
	public int getNumber() {
		return number;
	}
	
	public File getDirectory() {
		return directory;
	}
	
	public Episode[] getMainEpisodes() {
		return mainEpisodes;
	}
	
	public int getMainEpisodesAmount() {
		return mainEpisodes.length;
	}
	
	public Episode getEpisode(int number) {
		return mainEpisodes[number - 1];
	}
	
	public int getExtraEpisodes() {
		return extraEpisodes == null ? 0 : extraEpisodes.length;
	}
	
	public Episode getExtraEpisode(int number) {
		return extraEpisodes[number - 1];
	}
	
	public JPanel getPagePanel() {
		return pagePanel;
	}
	
	public Set<long[]> getTimestampSkipSegments() {
		return timestampSkipSegments;
	}
	
	public int[] getChapterSkipSegments() {
		return chapterSkipSegments;
	}
	
	private Episode[] populateEpisodes(File directory, ConfigSection section) {
		File[] files = Files.getFilesWithEndings(directory, Episode.VALID_EXTENSIONS);
		Episode[] episodes = new Episode[files.length];
		for(int i = 0; i < files.length; i++) {
			int number = i + 1;
			Episode episode;
			Object obj = null;
			if(section != null) obj = section.get(String.valueOf(number));
			if(obj == null) episode = new Episode(this, number, files[i]);
			//if(obj instanceof String) episode = new Episode(this, number, (String)obj, section.getPath() + "." + number);
			else if(obj instanceof ConfigSection) {
				ConfigSection episodeSection = (ConfigSection)obj;
				String path = episodeSection.get("path");
				if(path != null) episode = new Episode(this, number, new File(show.getDirectory(), path), episodeSection);
				else episode = new Episode(this, number, files[i], episodeSection);
			}
			else throw new IllegalArgumentException(); // TODO
			episodes[i] = episode;
		}
		return episodes;
	}
	
	public void loadUI() {
		Image image = null;
		if(mainSection != null) {
			String path = mainSection.get("image");
			if(path != null) {
				try {
					image = ImageIO.read(new File(show.getDirectory(), path));
				} catch(IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		if(image == null){
			try {
				image = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource("default.png")));
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
		pagePanel.add(new JImage(Images.reduce(image, 450, 900)));
		JPanel pageTitlePanel = new ComponentBuilder<>(new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0))).bounds(470, 0, 1560 - 470, 125).backgroundColor(Color.DARK_GRAY).build();
		JTextArea titleArea = new TextAreaBuilder().text(show.getFriendlyName() + " Season " + number).font(new Font("Segoe Print", Font.BOLD, 50))
				.wrap(false).backgroundColor(Color.DARK_GRAY).foregroundColor(Color.WHITE).build();
		if(mainSection != null) pagePanel.add(new TextAreaBuilder().text(mainSection.get("description", String.class)).font(new Font("Times New Roman", Font.PLAIN, 24))
				.backgroundColor(Color.DARK_GRAY).foregroundColor(Color.WHITE).bounds(470, 145, 1560 - 470, 150).build());
		JPanel episodesPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 15, 5));
		episodesPanel.setBackground(Color.DARK_GRAY);
		episodesPanel.setBounds(470, 305, 1560 - 470, 900 - 305);
		JTextArea seasonText = new JTextArea();
		TextAreaBuilder seasonTextBuilder = new TextAreaBuilder(seasonText);
		seasonTextBuilder.backgroundColor(Color.DARK_GRAY).size(100, 30); // preferredSize might be needed
		for(Episode episode : mainEpisodes) {
			episodesPanel.add(seasonTextBuilder.text("Episode " + episode.getNumber()).foregroundColor(episode.getTracker().hasSeen() ? Color.LIGHT_GRAY : Color.WHITE)
					.mouseListener((BasicMouseListener)event -> {
						seasonText.setForeground(Color.LIGHT_GRAY);
						episode.getTracker().clicked();
						show.getMediaManager().getTMP().playEpisode(episode);
					}).build());
		}
		pagePanel.add(episodesPanel);
		pageTitlePanel.add(titleArea);
		pagePanel.add(pageTitlePanel);
		pagePanel.setVisible(false);
		show.getMediaManager().getTMP().getMainPanel().add(pagePanel);
	}
	
	public void setIntroLength(long introLength) {
		transformDataSection();
		mainSection.put("intro-length", introLength);
		mainSection.requestSave();
	}
	
	public void setSubtitleTrackName(String trackName) {
		transformDataSection();
		mainSection.put("sub-track-name", trackName);
		mainSection.requestSave();
	}
	
	public void setAudioTrackName(String trackName) {
		transformDataSection();
		mainSection.put("audio-track-name", trackName);
		mainSection.requestSave();
	}
	
	public long getIntroLength() {
		return mainSection == null ? 0L : mainSection.getLong("intro-length", 0L);
	}
	
	public String getSubtitleTrackName() {
		return mainSection == null ? null : mainSection.get("sub-track-name");
	}
	
	public String getAudioTrackName() {
		return mainSection == null ? null : mainSection.get("audio-track-name");
	}
	
	public SeasonTracker getTracker() {
		return show.getTracker().getSeasonTracker(number);
	}
	
	private void transformDataSection() {
		if(mainSection == null) mainSection = show.getData().computeSectionIfAbsent(String.format("seasons.%s.main", number));
	}
	
}
