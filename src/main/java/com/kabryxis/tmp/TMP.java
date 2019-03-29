package com.kabryxis.tmp;

import com.kabryxis.kabutils.Images;
import com.kabryxis.kabutils.concurrent.Threads;
import com.kabryxis.kabutils.data.file.yaml.Config;
import com.kabryxis.tmp.media.Episode;
import com.kabryxis.tmp.media.MediaManager;
import com.kabryxis.tmp.swing.*;
import com.kabryxis.tmp.user.EpisodeTracker;
import com.kabryxis.tmp.user.UserManager;
import com.kabryxis.tmp.vlc.BasicMediaPlayerEventListener;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.TrackDescription;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.windows.Win32FullScreenStrategy;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;
import uk.co.caprica.vlcj.runtime.x.LibXUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.*;
import java.util.List;

public class TMP {
	
	public static void main(String[] args) {
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), System.getenv("VLC_PATH"));
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
		LibXUtil.initialise();
		Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
			exception.printStackTrace();
			Threads.sleep(15000);
		});
		new TMP();
	}
	
	private final Config data;
	private final MediaManager mediaManager;
	private final UserManager userManager;
	private final Canvas mediaPlayerCanvas;
	private final EmbeddedMediaPlayer mediaPlayer;
	private final JFrame mainFrame;
	private final JPanel mainPanel;
	private final BarMenu barMenu;
	
	private Node<Component> currentlyVisiblePanelNode;
	
	private Episode currentEpisode;
	private EpisodeTracker currentEpisodeTracker;
	//private int currentAudioTrack;
	//private int currentSubtitleTrack;
	private boolean firstPlay = false;
	
	public TMP() {
		mainFrame = new FrameBuilder("Tarrant's Media Player").layout(null).icon(Images.loadFromResource(getClass().getClassLoader(), "icon.png"))
				.bounds(0, 0, 1920, 1080).backgroundColor(Color.DARK_GRAY).build();
		data = new Config(new File("data.yml"), true);
		List<String> commandLineArgs = new ArrayList<>();
		Collections.addAll(commandLineArgs, "--no-plugins-cache", "--no-video-title-show", "--no-snapshot-preview"/*, "--longhelp", "--advanced"*/);
		commandLineArgs.addAll(data.getList("command-line", String.class, Collections.emptyList()));
		MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory(commandLineArgs);
		//mediaPlayerFactory.setUserAgent("vlcj test player");
		mediaPlayerCanvas = new Canvas();
		mediaPlayerCanvas.setBackground(Color.DARK_GRAY);
		mediaPlayerCanvas.setBounds(0, 0, 1920, 1080);
		mediaPlayerCanvas.setVisible(false);
		mediaPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer(new Win32FullScreenStrategy(mainFrame));
		mediaPlayer.setEnableMouseInputHandling(false);
		mediaPlayer.setEnableKeyInputHandling(false);
		mediaPlayer.setVideoSurface(mediaPlayerFactory.newVideoSurface(mediaPlayerCanvas));
		mediaPlayer.setPlaySubItems(true);
		mediaPlayer.addMediaPlayerEventListener(new BasicMediaPlayerEventListener() {
			
			@Override
			public void playing(MediaPlayer player) {
				if(firstPlay) {
					List<TrackDescription> spuDescriptions = player.getSpuDescriptions();
					String manualSubtitleTrack = currentEpisode.getSubtitleTrackName();
					if(manualSubtitleTrack == null) manualSubtitleTrack = currentEpisode.getSeason().getSubtitleTrackName();
					boolean foundSub = false;
					if(manualSubtitleTrack != null) {
						for(TrackDescription trackDescription : spuDescriptions) {
							if(trackDescription.description().equals(manualSubtitleTrack)) {
								foundSub = true;
								player.setSpu(trackDescription.id());
								break;
							}
						}
						if(!foundSub) throw new IllegalStateException(String.format("Could not find subtitle track named '%s' from:\n\t%s", manualSubtitleTrack, spuDescriptions));
					}
					if(!foundSub) {
						for(TrackDescription spuTrack : spuDescriptions) {
							if(spuTrack.description().toLowerCase().contains("english")) {
								player.setSpu(spuTrack.id());
								break;
							}
						}
					}
					List<TrackDescription> audioDescriptions = player.getAudioDescriptions();
					String manualAudioTrack = currentEpisode.getSeason().getAudioTrackName();
					boolean foundAudio = false;
					if(manualAudioTrack != null) {
						for(TrackDescription trackDescription : audioDescriptions) {
							if(trackDescription.description().equals(manualAudioTrack)) {
								foundAudio = true;
								player.setAudioTrack(trackDescription.id());
								break;
							}
						}
						if(!foundAudio) throw new IllegalStateException(String.format("Could not find audio track named '%s' from:\n\t%s", manualAudioTrack, audioDescriptions));
					}
					if(!foundAudio) {
						for(TrackDescription audioDescription : audioDescriptions) {
							if(audioDescription.description().toLowerCase().contains("japanese")) {
								player.setAudioTrack(audioDescription.id());
								break;
							}
						}
					}
					long lastSeenTime = currentEpisodeTracker.getLastSeenTime();
					if(lastSeenTime != 0L) player.setTime(lastSeenTime);
					currentEpisode.onPlay(player);
					firstPlay = false;
				}
			}
			
			@Override
			public void timeChanged(MediaPlayer player, long time) {
				int[] chapterSegments = currentEpisode.getChapterSkipSegments();
				if(chapterSegments != null && com.kabryxis.kabutils.data.Arrays.containsInt(chapterSegments, player.getChapter())) {
					if(player.getChapterCount() - 1 == player.getChapter()) playNextEpisode();
					else player.nextChapter();
					return;
				}
				Set<long[]> timestampSkipSegments = currentEpisode.getTimestampSkipSegments();
				if(timestampSkipSegments != null) {
					for(long[] timestampSkipSegment : timestampSkipSegments) {
						long begin = timestampSkipSegment[0];
						long end = timestampSkipSegment[1];
						if(end == 0 && time >= begin) {
							playNextEpisode();
							return;
						}
						if(time >= begin && time <= end) {
							player.setTime(end + 1);
							return;
						}
					}
				}
				currentEpisodeTracker.setLastSeenTime(time);
			}
			
			@Override
			public void endOfSubItems(MediaPlayer player) {
				playNextEpisode();
			}
			
		});
		KeyListener keyListener = (BasicKeyListener)e -> {
			switch(e.getKeyCode()) {
				case KeyCode.ESCAPE:
					exitEpisode();
					break;
				case KeyCode.A:
					if(e.isControlDown()) currentEpisode.getSeason().setAudioTrackName(mediaPlayer.getAudioDescriptions().get(getAudioTrackIndex()).description());
					else setNextAudioTrack();
					break;
				case KeyCode.S:
					if(e.isControlDown()) currentEpisode.getSeason().setSubtitleTrackName(mediaPlayer.getSpuDescriptions().get(getSubtitleTrackIndex()).description());
					else if(e.isShiftDown()) currentEpisode.setSubtitleTrackName(mediaPlayer.getSpuDescriptions().get(getSubtitleTrackIndex()).description());
					else setNextSubtitleTrack();
					break;
				case KeyCode.LEFT_ARROW:
					mediaPlayer.setTime(mediaPlayer.getTime() - (e.isControlDown() ? 50 : 50000));
					break;
				case KeyCode.RIGHT_ARROW:
					if(e.isControlDown()) mediaPlayer.nextFrame();
					else {
						long newTime = mediaPlayer.getTime() + 50000;
						if(newTime >= mediaPlayer.getLength()) playNextEpisode();
						else mediaPlayer.setTime(newTime);
					}
					break;
				case KeyCode.SPACE:
					mediaPlayer.setPause(mediaPlayer.isPlaying());
					break;
				case KeyCode.I:
					if(e.isControlDown()) {
						if(currentEpisodeTracker.isIntroStartTimeSet()) currentEpisodeTracker.setIntroLength(mediaPlayer.getTime());
						else currentEpisodeTracker.setIntroStartTime(mediaPlayer.getTime());
					}
					break;
				default:
					System.out.println("keyCode: " + e.getKeyCode());
					break;
			}
		};
		mainFrame.addKeyListener(keyListener);
		mediaPlayerCanvas.addKeyListener(keyListener);
		mainPanel = new ComponentBuilder<>(new JPanel(null)).bounds(0, 0, 1920, 1080).backgroundColor(Color.DARK_GRAY.darker()).build();
		Image dividerImage = Images.loadFromResource(getClass().getClassLoader(), "divider.png");
		barMenu = new BarMenu(1920, 30, 0.5F, 2, 2, Color.DARK_GRAY.darker().darker());
		mediaManager = new MediaManager(this);
		userManager = new UserManager(this, barMenu);
		barMenu.addRight(new ComponentBuilder<>(new FadingImage(Images.loadFromResource(getClass().getClassLoader(), "x.png")))
				.mouseListener((BasicMouseListener)e -> System.exit(0)).build());
		barMenu.addRight(new ComponentBuilder<>(new FadingImage(Images.loadFromResource(getClass().getClassLoader(), "userplus.png")))
				.mouseListener((BasicMouseListener)e -> userManager.createAndSelectUser()).build());
		barMenu.addRight(new JImage(dividerImage));
		FadingImage detailsFadingImage = new FadingImage(Images.loadFromResource(getClass().getClassLoader(), "details.png"));
		FadingImage blockFadingImage = new FadingImage(Images.loadFromResource(getClass().getClassLoader(), "block.png"));
		barMenu.addLeft(new ComponentBuilder<>(detailsFadingImage).mouseListener((BasicMouseListener)e -> setDisplay(DisplayOption.DETAILS)).build());
		barMenu.addLeft(new ComponentBuilder<>(blockFadingImage).mouseListener((BasicMouseListener)e -> setDisplay(DisplayOption.BLOCK)).build());
		barMenu.addLeft(new JImage(dividerImage));
		barMenu.addLeft(new ComponentBuilder<>(new FadingImage(Images.loadFromResource(getClass().getClassLoader(), "alphabetical.png")))
				.mouseListener((BasicMouseListener)e -> setOrder(SortOption.ALPHABETICAL)).build());
		barMenu.addLeft(new ComponentBuilder<>(new FadingImage(Images.loadFromResource(getClass().getClassLoader(), "lastseen.png")))
				.mouseListener((BasicMouseListener)e -> setOrder(SortOption.LAST_SEEN)).build());
		barMenu.addLeft(new JImage(dividerImage));
		userManager.loadUsers();
		barMenu.addTo(mainPanel);
		DisplayOption displayOption = userManager.getSelectedUser().getDisplayOption();
		switch(displayOption) {
			case BLOCK:
				blockFadingImage.setFaded(true);
				break;
			case DETAILS:
				detailsFadingImage.setFaded(true);
				break;
		}
		mediaManager.initialize(displayOption, userManager.getSelectedUser().getSortOption());
		mainFrame.add(mediaPlayerCanvas);
		mediaManager.getPanels().forEach(mainPanel::add);
		mainFrame.add(mainPanel);
		mainFrame.setExtendedState(mainFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		currentlyVisiblePanelNode = new Node<>(mediaManager.getMainPanel());
		mainFrame.setVisible(true);
		//mediaPlayer.toggleFullScreen();
	}
	
	public Config getData() {
		return data;
	}
	
	public UserManager getUserManager() {
		return userManager;
	}
	
	public MediaManager getMediaManager() {
		return mediaManager;
	}
	
	public JPanel getMainPanel() {
		return mainPanel;
	}
	
	public void playEpisode(Episode episode) {
		mainPanel.setVisible(false);
		mediaPlayerCanvas.setVisible(true);
		currentEpisode = episode;
		userManager.getSelectedUser().getShowTracker(currentEpisode.getSeason().getShow()).setLastWatched();
		currentEpisodeTracker = userManager.getSelectedUser().getShowTracker(episode.getSeason().getShow()).getSeasonTracker(episode.getSeason().getNumber()).getEpisodeTracker(episode.getNumber());
		currentEpisodeTracker.getSeasonTracker().setLastEpisode(currentEpisode.getNumber());
		firstPlay = true;
		mediaPlayer.playMedia(episode.getEpisodeFile().getPath());
		mediaPlayer.setFullScreen(true);
		userManager.saveAll();
	}
	
	public void playNextEpisode() {
		currentEpisodeTracker.setLastSeenTime(0L);
		playEpisode(currentEpisode.getNextEpisode());
	}
	
	public void exitEpisode() {
		DisplayOption displayOption = userManager.getSelectedUser().getDisplayOption();
		//mediaManager.loadUI(displayOption, displayOption);
		mediaPlayer.setFullScreen(false);
		mediaPlayer.stop();
		mediaPlayerCanvas.setVisible(false);
		mainPanel.setVisible(true);
		userManager.saveAll();
	}
	
	/*public void setSubtitleTrack(int subtitleTrackIndex, boolean global) {
		currentSubtitleTrack = subtitleTrackIndex;
		TrackDescription desc = mediaPlayer.getSpuDescriptions().get(subtitleTrackIndex);
		System.out.println(desc);
		mediaPlayer.setSpu(desc.id());
		if(global) currentEpisode.getSeason().setSubtitleTrackName(desc.description());
		else currentEpisode.setSubtitleTrackName(desc.description());
	}*/
	
	public void setCurrentlyVisibleMainPanel(Component component) {
		currentlyVisiblePanelNode.get().setVisible(false);
		currentlyVisiblePanelNode = currentlyVisiblePanelNode.after(component);
		component.setVisible(true);
	}
	
	public void setPreviousCurrentlyVisibleMainPanel() {
		if(currentlyVisiblePanelNode.hasPrevious()) {
			DisplayOption displayOption = userManager.getSelectedUser().getDisplayOption();
			//mediaManager.loadUI(displayOption, displayOption);
			currentlyVisiblePanelNode.get().setVisible(false);
			currentlyVisiblePanelNode = currentlyVisiblePanelNode.getPrevious();
			currentlyVisiblePanelNode.get().setVisible(true);
		}
	}
	
	public void setNextAudioTrack() {
		int audio = mediaPlayer.getAudioTrack();
		List<TrackDescription> audioDescriptions = mediaPlayer.getAudioDescriptions();
		for(int i = 0; i < audioDescriptions.size(); i++) {
			TrackDescription audioTrack = audioDescriptions.get(i);
			if(audioTrack.id() == audio) {
				mediaPlayer.setAudioTrack(audioDescriptions.get(i == audioDescriptions.size() - 1 ? 0 : i + 1).id());
				break;
			}
		}
	}
	
	public void setNextSubtitleTrack() {
		int spu = mediaPlayer.getSpu();
		List<TrackDescription> spuDescriptions = mediaPlayer.getSpuDescriptions();
		System.out.println(spuDescriptions);
		for(int i = 0; i < spuDescriptions.size(); i++) {
			TrackDescription spuTrack = spuDescriptions.get(i);
			if(spuTrack.id() == spu) {
				mediaPlayer.setSpu(spuDescriptions.get(i == spuDescriptions.size() - 1 ? 0 : i + 1).id());
				break;
			}
		}
	}
	
	public int getAudioTrackIndex() {
		int audio = mediaPlayer.getAudioTrack();
		List<TrackDescription> audioDescriptions = mediaPlayer.getAudioDescriptions();
		for(int i = 0; i < audioDescriptions.size(); i++) {
			TrackDescription audioTrack = audioDescriptions.get(i);
			if(audioTrack.id() == audio) return i;
		}
		throw new IllegalStateException(String.format("Could not find audio track with id '%s' from:\n\t%s", audio, audioDescriptions));
	}
	
	public int getSubtitleTrackIndex() {
		int spu = mediaPlayer.getSpu();
		List<TrackDescription> spuDescriptions = mediaPlayer.getSpuDescriptions();
		for(int i = 0; i < spuDescriptions.size(); i++) {
			TrackDescription spuTrack = spuDescriptions.get(i);
			if(spuTrack.id() == spu) return i;
		}
		throw new IllegalStateException(String.format("Could not find subtitle track with id '%s' from:\n\t%s", spu, spuDescriptions));
	}
	
	public void setDisplay(DisplayOption displayOption) {
		DisplayOption currentDisplayOption = userManager.getSelectedUser().getDisplayOption();
		if(currentDisplayOption != displayOption) {
			userManager.getSelectedUser().setDisplayOption(displayOption);
			mediaManager.setDisplay(displayOption);
		}
	}
	
	public void setOrder(SortOption sortOption) {
		SortOption currentSortOption = userManager.getSelectedUser().getSortOption();
		if(currentSortOption != sortOption) {
			userManager.getSelectedUser().setSortOption(sortOption);
			mediaManager.setOrder(sortOption);
		}
	}
	
}
