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
import uk.co.caprica.vlcj.player.ChapterDescription;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.TrackDescription;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.windows.Win32FullScreenStrategy;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;
import uk.co.caprica.vlcj.runtime.x.LibXUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TMP {
	
	private static final Color DEF_C = Color.DARK_GRAY.darker().darker();
	public static final Color DEFAULT_BG_COLOR = DEF_C;
	public static final Color ERROR_COLOR_1 = Color.BLUE;
	
	public static void main(String[] args) {
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), System.getenv("VLC_PATH"));
		Native.load(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
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
	private long skipOffset = -1L;
	
	public TMP() {
		mainFrame = new FrameBuilder("Tarrant's Media Player").layout(null).icon(Images.loadFromResource(getClass().getClassLoader(), "icon.png"))
				.bounds(0, 0, 1920, 1080).bgColor(ERROR_COLOR_1).build();
		//mainFrame.addMouseWheelListener(e -> System.out.println("mainFrame-mouseWheelMoved: " + e));
		data = new Config(new File("data.yml"), true);
		List<String> commandLineArgs = new ArrayList<>();
		Collections.addAll(commandLineArgs, "--no-plugins-cache", "--no-video-title-show", "--no-snapshot-preview"/*, "--longhelp", "--advanced"*/);
		commandLineArgs.addAll(data.getList("command-line", String.class, Collections.emptyList()));
		MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory(commandLineArgs);
		//mediaPlayerFactory.setUserAgent("vlcj test player");
		mediaPlayerCanvas = new ComponentBuilder<>(new Canvas()).bounds(0, 0, 1920, 1080).bgColor(ERROR_COLOR_1).visible(false).build();
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
					for(TrackDescription spuTrack : spuDescriptions) {
						if(spuTrack.description().toLowerCase().contains("eng")) {
							player.setSpu(spuTrack.id());
							break;
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
				if(!currentEpisode.checkForSkip(player, time)) currentEpisodeTracker.setLastSeenTime(time);
			}
			
			@Override
			public void endOfSubItems(MediaPlayer player) {
				playNextEpisode();
			}
			
		});
		KeyListener keyListener = new BasicKeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) { // keyTyped is bugged and has a blank event except for #getKeyChar, it also does not call some keys (ie arrow keys)
				//System.out.println("keyTyped:" + e);
				switch(e.getKeyChar()) {
					case KeyEvent.VK_ESCAPE:
						exitEpisode();
						break;
					case '0':
					case '1':
					case '2':
					case '3':
					case '4':
					case '5':
					case '6':
					case '7':
					case '8':
					case '9':
						int id = Integer.parseInt(String.valueOf(e.getKeyChar()));
						long time = mediaPlayer.getTime();
						if(skipOffset != -1L) {
							currentEpisode.getSeason().setDefinedSkipDuration(id, time - skipOffset);
							currentEpisode.addDefinedSkipSegment(id, skipOffset);
							skipOffset = -1L;
							break;
						}
						long duration = currentEpisode.getSeason().getSkipDuration(id);
						if(duration == 0L || e.isControlDown()) skipOffset = time;
						else {
							currentEpisode.addDefinedSkipSegment(id, time);
							long end = time + duration;
							if(!currentEpisode.checkForSkip(mediaPlayer, end - 1)) mediaPlayer.setTime(end);
						}
						break;
					case KeyEvent.VK_DEAD_GRAVE:
						if(skipOffset == -1L || e.isControlDown()) skipOffset = mediaPlayer.getTime();
						else {
							currentEpisode.addCustomSkipSegment(skipOffset, mediaPlayer.getTime());
							skipOffset = -1L;
						}
						break;
					case 'a':
						if(e.isControlDown()) currentEpisode.getSeason().setAudioTrackName(mediaPlayer.getAudioDescriptions().get(getAudioTrackIndex()).description());
						else setNextAudioTrack();
						break;
					case 's':
						if(e.isControlDown()) currentEpisode.getSeason().setSpu(mediaPlayer.getSpu());
						else if(e.isShiftDown()) currentEpisode.setSpu(mediaPlayer.getSpu());
						else setNextSubtitleTrack();
						break;
					case KeyEvent.VK_SPACE:
						mediaPlayer.setPause(mediaPlayer.isPlaying());
						break;
					case 'i':
						/*if(e.isControlDown()) {
							if(currentEpisodeTracker.isIntroStartTimeSet()) currentEpisodeTracker.setIntroLength(mediaPlayer.getTime());
							else currentEpisodeTracker.setIntroStartTime(mediaPlayer.getTime());
						}*/
						break;
					default:
						System.out.println("keyChar: " + String.valueOf(e.getKeyChar()));
						break;
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) { // keyPressed is bugged with certain interactions, it causes ghost artifacts with rendering swing components
				//System.out.println("keyPressed:" + e);
				switch(e.getExtendedKeyCode()) {
					case KeyEvent.VK_LEFT:
						mediaPlayer.setTime(mediaPlayer.getTime() - (e.isControlDown() ? 50 : 50000));
						break;
					case KeyEvent.VK_RIGHT:
						long newTime = mediaPlayer.getTime() + (e.isControlDown() ? 50 : 50000);
						if(newTime >= mediaPlayer.getLength()) playNextEpisode();
						else mediaPlayer.setTime(newTime);
						break;
					default:
						break;
				}
			}
			
		};
		mainFrame.addKeyListener(keyListener);
		mediaPlayerCanvas.addKeyListener(keyListener);
		mainPanel = new ComponentBuilder<>(new JPanel(null)).bounds(0, 0, 1920, 1080).bgColor(Color.GREEN).build();
		//mainPanel.add(new JImage(Images.shade(Images.reduce(Images.loadFromResource(getClass().getClassLoader(), "bg.png"), 1920, 1080), DEFAULT_BG_COLOR)));
		Image dividerImage = Images.loadFromResource(getClass().getClassLoader(), "divider.png");
		barMenu = new BarMenu(1920, 30, 0.5F, 2, 2, DEFAULT_BG_COLOR.darker());
		mediaManager = new MediaManager(this);
		userManager = new UserManager(this, barMenu);
		barMenu.addRight(new ComponentBuilder<>(new FadingImage(Images.loadFromResource(getClass().getClassLoader(), "x.png")))
				.mouseListener((BasicMouseListener)e -> System.exit(0)).build());
		barMenu.addRight(new ComponentBuilder<>(new FadingImage(Images.loadFromResource(getClass().getClassLoader(), "userplus.png")))
				.mouseListener((BasicMouseListener)e -> userManager.createAndSelectUser()).build());
		barMenu.addRight(new JImage(dividerImage));
		FadingImage detailsFadingImage = new FadingImage(Images.loadFromResource(getClass().getClassLoader(), "details.png"));
		FadingImage blockFadingImage = new FadingImage(Images.loadFromResource(getClass().getClassLoader(), "block.png"));
		barMenu.addLeft(new ComponentBuilder<>(new FadingImage(Images.loadFromResource(getClass().getClassLoader(), "back.png")))
				.mouseListener((BasicMouseListener)e -> setPreviousCurrentlyVisibleMainPanel()).build());
		barMenu.addLeft(new JImage(dividerImage));
		//barMenu.addLeft(new ComponentBuilder<>(detailsFadingImage).mouseListener((BasicMouseListener)e -> setDisplay(DisplayOption.DETAILS)).build());
		barMenu.addLeft(new ComponentBuilder<>(blockFadingImage).mouseListener((BasicMouseListener)e -> setDisplay(DisplayOption.BLOCK)).build());
		barMenu.addLeft(new JImage(dividerImage));
		barMenu.addLeft(new ComponentBuilder<>(new FadingImage(Images.loadFromResource(getClass().getClassLoader(), "alphabetical.png")))
				.mouseListener((BasicMouseListener)e -> setOrder(SortOption.ALPHABETICAL)).build());
		barMenu.addLeft(new ComponentBuilder<>(new FadingImage(Images.loadFromResource(getClass().getClassLoader(), "lastseen.png")))
				.mouseListener((BasicMouseListener)e -> setOrder(SortOption.LAST_SEEN)).build());
		barMenu.addLeft(new ComponentBuilder<>(new FadingImage(Images.loadFromResource(getClass().getClassLoader(), "lastseen.png")))
				.mouseListener((BasicMouseListener)e -> setOrder(SortOption.RATING)).build());
		barMenu.addLeft(new ComponentBuilder<>(new FadingImage(Images.loadFromResource(getClass().getClassLoader(), "lastseen.png")))
				.mouseListener((BasicMouseListener)e -> setOrder(SortOption.RECENTLY_ADDED)).build());
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
	
	public void playEpisode(Episode episode, boolean fromBeginning) {
		mainPanel.setVisible(false);
		mediaPlayerCanvas.setVisible(true);
		currentEpisode = episode;
		userManager.getSelectedUser().getShowTracker(currentEpisode.getSeason().getShow()).setLastWatched();
		currentEpisodeTracker = userManager.getSelectedUser().getShowTracker(episode.getSeason().getShow()).getSeasonTracker(episode.getSeason().getNumber()).getEpisodeTracker(episode.getNumber());
		currentEpisodeTracker.getSeasonTracker().setLastEpisode(currentEpisode.getNumber());
		firstPlay = true;
		if(fromBeginning) currentEpisodeTracker.setLastSeenTime(0L);
		mediaPlayer.playMedia(episode.getEpisodeFile().getPath());
		mediaPlayer.setFullScreen(true);
		userManager.saveAll();
	}
	
	public void playEpisode(Episode episode) {
		playEpisode(episode, false);
	}
	
	public void playNextEpisode() {
		currentEpisodeTracker.setLastSeenTime(0L);
		playEpisode(currentEpisode.getNextEpisode(), true);
	}
	
	public void exitEpisode() {
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
				System.out.println("setting subtitle to " + spuTrack);
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
	
	public static Image getRatingStars(double rating) {
		Image emptyStarImage = Images.loadFromResource(TMP.class.getClassLoader(), "hstar.png");
		Image fullStarImage = Images.loadFromResource(TMP.class.getClassLoader(), "fstar.png");
		int fullStars = (int)rating;
		double partialStar = rating - fullStars;
		BufferedImage baseImage = new BufferedImage(30 * 10, 30, 2);
		Graphics2D graphics = baseImage.createGraphics();
		for(int i = 0; i < fullStars; i++) {
			graphics.drawImage(fullStarImage, i * 30, 0, null);
		}
		BufferedImage partialStarImage = Images.copy(emptyStarImage);
		Graphics2D partialStarGraphics = partialStarImage.createGraphics();
		int partialPixelsX = (int)((30 - 12) * partialStar);
		partialStarGraphics.drawImage(fullStarImage, 6, 0, 6 + partialPixelsX, 30, 6, 0, 6 + partialPixelsX, 30, null);
		partialStarGraphics.dispose();
		graphics.drawImage(partialStarImage, fullStars * 30, 0, null);
		for(int i = fullStars + 1; i < 10; i++) {
			graphics.drawImage(emptyStarImage, i * 30, 0, null);
		}
		graphics.dispose();
		return baseImage;
	}
	
	public void skipChapter(int chapterIndex) {
		ChapterDescription chapterDescription = mediaPlayer.getExtendedChapterDescriptions().get(chapterIndex);
		mediaPlayer.setTime(chapterDescription.getOffset() + chapterDescription.getDuration());
	}
	
}
