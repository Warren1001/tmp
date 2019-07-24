package com.kabryxis.tmp.swing;

import com.kabryxis.tmp.FilterOptionBuilder;
import com.kabryxis.tmp.TMP;
import com.kabryxis.tmp.media.MediaManager;
import com.kabryxis.tmp.media.Show;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TilePanel extends JPanel {
	
	private final MediaManager mediaManager;
	private final Function<Show, JPanel> showTileFunction;
	private final FilterOptionBuilder filterOptionBuilder = new FilterOptionBuilder();
	
	private boolean hasLoaded = false;
	private Comparator<Show> order;
	
	public TilePanel(MediaManager mediaManager, Function<Show, JPanel> showTileFunction) {
		super(new FlowLayout(FlowLayout.CENTER, 3, 3));
		this.mediaManager = mediaManager;
		this.showTileFunction = showTileFunction;
		int ySize = (1080 - 30) * 4;
		int maxYScroll = ySize - (1080 - 30);
		setBounds(0, 30, 1920, ySize);
		setBackground(TMP.DEFAULT_BG_COLOR.darker());
		setVisible(false);
		addMouseWheelListener(e -> {
			int newLoc = (int)(getLocation().getY() + (e.getScrollAmount() * 20 * e.getWheelRotation() * -1));
			if(newLoc < -maxYScroll) newLoc = -maxYScroll;
			else if(newLoc > 30) newLoc = 30;
			setLocation(0, newLoc);
			System.out.println("TilePanel-mouseWheelMoved: " + e);
		});
	}
	
	public void sort(Comparator<Show> order) {
		this.order = order;
		relist();
	}
	
	public void relist() {
		Collection<Show> shows = mediaManager.getShows();
		Predicate<Show> predicate = filterOptionBuilder.build();
		if(!hasLoaded) {
			Stream<Show> stream = shows.stream().filter(predicate);
			if(order != null) stream = stream.sorted(order);
			stream.forEachOrdered(show -> add(showTileFunction.apply(show)));
			hasLoaded = true;
		}
		else {
			List<Show> orderShows = new ArrayList<>(shows);
			orderShows.sort(order);
			int index = 0;
			for(Show show : orderShows) {
				JPanel panel = showTileFunction.apply(show);
				if(predicate.test(show)) add(panel, index++);
				else remove(panel);
			}
			revalidate();
		}
	}
	
}
