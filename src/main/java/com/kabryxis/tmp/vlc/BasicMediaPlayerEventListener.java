package com.kabryxis.tmp.vlc;

import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;

public class BasicMediaPlayerEventListener implements MediaPlayerEventListener {
	
	@Override
	public void mediaChanged(MediaPlayer player, libvlc_media_t t, String s) {}
	
	@Override
	public void opening(MediaPlayer player) {}
	
	@Override
	public void buffering(MediaPlayer player, float v) {}
	
	@Override
	public void playing(MediaPlayer player) {}
	
	@Override
	public void paused(MediaPlayer player) {}
	
	@Override
	public void stopped(MediaPlayer player) {}
	
	@Override
	public void forward(MediaPlayer player) {}
	
	@Override
	public void backward(MediaPlayer player) {}
	
	@Override
	public void finished(MediaPlayer player) {}
	
	@Override
	public void timeChanged(MediaPlayer player, long l) {}
	
	@Override
	public void positionChanged(MediaPlayer player, float v) {}
	
	@Override
	public void seekableChanged(MediaPlayer player, int i) {}
	
	@Override
	public void pausableChanged(MediaPlayer player, int i) {}
	
	@Override
	public void titleChanged(MediaPlayer player, int i) {}
	
	@Override
	public void snapshotTaken(MediaPlayer player, String s) {}
	
	@Override
	public void lengthChanged(MediaPlayer player, long l) {}
	
	@Override
	public void videoOutput(MediaPlayer player, int i) {}
	
	@Override
	public void scrambledChanged(MediaPlayer player, int i) {}
	
	@Override
	public void elementaryStreamAdded(MediaPlayer player, int i, int i1) {}
	
	@Override
	public void elementaryStreamDeleted(MediaPlayer player, int i, int i1) {}
	
	@Override
	public void elementaryStreamSelected(MediaPlayer player, int i, int i1) {}
	
	@Override
	public void corked(MediaPlayer player, boolean b) {}
	
	@Override
	public void muted(MediaPlayer player, boolean b) {}
	
	@Override
	public void volumeChanged(MediaPlayer player, float v) {}
	
	@Override
	public void audioDeviceChanged(MediaPlayer player, String s) {}
	
	@Override
	public void chapterChanged(MediaPlayer player, int i) {
		System.out.println("chapterChanged: " + i);
	}
	
	@Override
	public void error(MediaPlayer player) {}
	
	@Override
	public void mediaPlayerReady(MediaPlayer player) {
		System.out.println("mediaPlayerReady");
	}
	
	@Override
	public void mediaMetaChanged(MediaPlayer player, int i) {}
	
	@Override
	public void mediaSubItemAdded(MediaPlayer player, libvlc_media_t t) {}
	
	@Override
	public void mediaDurationChanged(MediaPlayer player, long l) {}
	
	@Override
	public void mediaParsedChanged(MediaPlayer player, int i) {}
	
	@Override
	public void mediaParsedStatus(MediaPlayer player, int i) {
		System.out.println("mediaParsedStatus:" + i);
	}
	
	@Override
	public void mediaFreed(MediaPlayer player) {}
	
	@Override
	public void mediaStateChanged(MediaPlayer player, int i) {}
	
	@Override
	public void mediaSubItemTreeAdded(MediaPlayer player, libvlc_media_t t) {}
	
	@Override
	public void newMedia(MediaPlayer player) {}
	
	@Override
	public void subItemPlayed(MediaPlayer player, int i) {}
	
	@Override
	public void subItemFinished(MediaPlayer player, int i) {}
	
	@Override
	public void endOfSubItems(MediaPlayer player) {}
	
}
