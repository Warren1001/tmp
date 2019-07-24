package com.kabryxis.tmp.media;

import com.kabryxis.kabutils.Images;
import com.kabryxis.kabutils.data.Maths;
import com.kabryxis.kabutils.data.file.Files;
import com.kabryxis.kabutils.data.file.yaml.ConfigSection;
import com.kabryxis.tmp.swing.BasicMouseListener;
import com.kabryxis.tmp.swing.ComponentBuilder;
import com.kabryxis.tmp.swing.JImage;
import com.kabryxis.tmp.swing.TextAreaBuilder;
import com.kabryxis.tmp.user.SeasonTracker;
import uk.co.caprica.vlcj.player.MediaPlayer;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Season {
	
	private final long[] definedSkipDurations = new long[10];
	
	private final Show show;
	private final int number;
	private final File directory;
	private final Episode[] episodes;
	private final JPanel pagePanel;
	
	private ConfigSection section;
	
	public Season(Show show, int number, ConfigSection section) {
		this.show = show;
		this.number = number;
		directory = new File(show.getDirectory(), String.valueOf(number));
		this.section = section;
		if(section != null) {
			ConfigSection skipSection = section.get("skip");
			if(skipSection != null) skipSection.forEach((key, value) -> definedSkipDurations[Integer.parseInt(key)] = Maths.toLong(value));
		}
		episodes = populateEpisodes(directory, section);
		pagePanel = new ComponentBuilder<>(new JPanel(null)).bounds(0, 30, 1920, 1080 - 30).visible(false).build();
	}
	
	public Season(Show show, int number, File directory) {
		this.show = show;
		this.number = number;
		this.directory = directory;
		episodes = populateEpisodes(directory, null);
		pagePanel = new ComponentBuilder<>(new JPanel(null)).bounds(0, 30, 1920, 1080 - 30).visible(false).build();
		section = null;
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
	
	public Episode[] getEpisodes() {
		return episodes;
	}
	
	public Episode getEpisode(int number) {
		return episodes[number - 1];
	}
	
	public JPanel getPagePanel() {
		return pagePanel;
	}
	
	public long getSkipDuration(int id) {
		return definedSkipDurations[id];
	}
	
	private Episode[] populateEpisodes(File directory, ConfigSection section) {
		File[] files = Files.getFilesWithEndings(directory, Episode.VALID_EXTENSIONS);
		Episode[] episodes = new Episode[files.length];
		//com.kabryxis.kabutils.data.Arrays.forEach(files, file -> System.out.println("BEFORE: " + file.getName()));
		//Arrays.sort(files, (f1, f2) -> String.CASE_INSENSITIVE_ORDER.compare(f1.getName(), f2.getName()));
		//com.kabryxis.kabutils.data.Arrays.forEach(files, file -> System.out.println("AFTER: " + file.getName()));
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
		if(section != null) {
			File file = new File(directory, section.get("image", "image.png"));
			if(file.exists()) image = Images.read(file);
		}
		if(image == null) image = show.getImage();
		JPanel leftPanel = new ComponentBuilder<>(new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 15))).bounds(0, 0, 450, 1080 - 30).build();
		JPanel rightPanel = new ComponentBuilder<>(new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 15))).bounds(450, 0, 1920 - 450, 1080 - 30).build();
		leftPanel.add(new JImage(Images.resize(image, 450, 900)));
		JPanel pageTitlePanel = new ComponentBuilder<>(new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0))).preferredSize(1920 - 450, 125).build();
		pageTitlePanel.add(new TextAreaBuilder(String.format("%s Season %s", show.getFriendlyName(), number)).font(new Font("Segoe Print", Font.BOLD, 50)).wrap(false).build());
		rightPanel.add(pageTitlePanel);
		if(section != null) rightPanel.add(new TextAreaBuilder(section.get("description", String.class)).font(new Font("Times New Roman", Font.PLAIN, 24))
				.preferredSize(1920 - 450, 150).build());
		JPanel episodesPanel = new ComponentBuilder<>(new JPanel(new FlowLayout(FlowLayout.LEADING, 15, 5))).preferredSize(1920 - 450, 600).build();
		for(Episode episode : episodes) {
			JTextArea episodeText = new JTextArea(String.format("Episode %s", episode.getNumber()));
			episodesPanel.add(new TextAreaBuilder(episodeText).preferredSize(100, 30).fgColor(episode.getTracker().hasSeen() ? Color.LIGHT_GRAY : Color.WHITE)
					.mouseListener((BasicMouseListener)e -> {
						episodeText.setForeground(Color.LIGHT_GRAY);
						episode.getTracker().clicked();
						show.getMediaManager().getTMP().playEpisode(episode, e.isControlDown());
					}).build());
		}
		rightPanel.add(episodesPanel);
		pagePanel.add(leftPanel);
		pagePanel.add(rightPanel);
		show.getMediaManager().getTMP().getMainPanel().add(pagePanel);
	}
	
	public void setSpu(int spu) {
		transformDataSection();
		section.put("spu-id", spu);
		section.requestSave();
	}
	
	public void setAudioTrackName(String trackName) {
		transformDataSection();
		section.put("audio-track-name", trackName);
		section.requestSave();
	}
	
	public void setDefinedSkipDuration(int id, long duration) {
		definedSkipDurations[id] = duration;
		transformDataSection();
		section.put("skip." + id, duration);
		section.requestSave();
	}
	
	public int getSpuId() {
		return section == null ? -2 : section.getInt("spu-id", -2);
	}
	
	public String getAudioTrackName() {
		return section == null ? null : section.get("audio-track-name");
	}
	
	public SeasonTracker getTracker() {
		return show.getTracker().getSeasonTracker(number);
	}
	
	private void transformDataSection() {
		if(section == null) section = show.getData().computeSectionIfAbsent(String.format("seasons.%s", number));
	}
	
	public void onPlay(MediaPlayer player) {
		int spu = getSpuId();
		if(spu != -2) player.setSpu(spu);
	}
	
}
