package com.kabryxis.tmp.swing;

import com.kabryxis.tmp.media.MediaManager;
import com.kabryxis.tmp.media.Show;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;

public class TilePanel extends JPanel {
	
	private final MediaManager mediaManager;
	private final Function<Show, JPanel> showTileFunction;
	
	private boolean hasLoaded = false;
	
	public TilePanel(MediaManager mediaManager, Function<Show, JPanel> showTileFunction) {
		super(new FlowLayout(FlowLayout.CENTER, 3, 3));
		this.mediaManager = mediaManager;
		this.showTileFunction = showTileFunction;
		setBounds(0, 30, 1920, 1080 - 30);
		setBackground(Color.DARK_GRAY.darker());
		setVisible(false);
	}
	
	public void sort(Comparator<Show> order) {
		Collection<Show> shows = mediaManager.getShows();
		if(!hasLoaded) {
			shows.stream().sorted(order).forEachOrdered(show -> add(showTileFunction.apply(show)));
			hasLoaded = true;
		}
		else {
			List<Show> orderShows = new ArrayList<>(shows);
			orderShows.sort(order);
			for(int index = 0; index < orderShows.size(); index++) {
				add(showTileFunction.apply(orderShows.get(index)), index);
			}
			revalidate();
		}
	}
	
}
