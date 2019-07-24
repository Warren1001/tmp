package com.kabryxis.tmp.user;

import com.kabryxis.kabutils.data.file.yaml.Config;
import com.kabryxis.tmp.DisplayOption;
import com.kabryxis.tmp.SortOption;
import com.kabryxis.tmp.TMP;
import com.kabryxis.tmp.media.Show;
import com.kabryxis.tmp.swing.FadingImage;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class User {
	
	private final TMP tmp;
	private final String name;
	private final Config data;
	private final FadingImage image;
	private final Map<Show, ShowTracker> showTrackers = new HashMap<>();
	private final List<SortOption> sortOptionOrder;
	
	public User(TMP tmp, Config data, FadingImage image) {
		this.tmp = tmp;
		name = data.getName();
		this.image = image;
		this.data = data;
		data.save();
		for(Show show : tmp.getMediaManager().getShows()) {
			showTrackers.put(show, new ShowTracker(this, show));
		}
		List<String> list = data.getList("sort_option_order", String.class);
		sortOptionOrder = list == null || list.isEmpty() ? Collections.singletonList(SortOption.ALPHABETICAL) : list.stream().map(SortOption::getByName)
				.filter(sortOption -> sortOption != SortOption.USER_ORDER).collect(Collectors.toList());
	}
	
	public User(TMP tmp, String name, Color color, FadingImage image) {
		this.tmp = tmp;
		this.name = name;
		data = new Config(new File("users" + File.separator + name + ".yml"));
		data.put("color", color);
		this.image = image;
		data.save();
		for(Show show : tmp.getMediaManager().getShows()) {
			showTrackers.put(show, new ShowTracker(this, show));
		}
		List<String> list = data.getList("sort_option_order", String.class);
		sortOptionOrder = list == null || list.isEmpty() ? Collections.singletonList(SortOption.ALPHABETICAL) : list.stream().map(SortOption::getByName).collect(Collectors.toList());
	}
	
	public String getName() {
		return name;
	}
	
	public Config getData() {
		return data;
	}
	
	public void setAsSelected() {
		image.setManual(true);
		image.setFaded(true);
		if(data.get("color", Color.class) == null) {
			data.put("color", JColorChooser.showDialog(null, "Choose your profile color", Color.WHITE));
			data.save();
		}
	}
	
	public void setAsUnselected() {
		image.setFaded(false);
		image.setManual(false);
	}
	
	public ShowTracker getShowTracker(Show show) {
		return showTrackers.get(show);
	}
	
	public void setDisplayOption(DisplayOption displayOption) {
		data.put("display", displayOption.name());
		data.save();
	}
	
	public DisplayOption getDisplayOption() {
		return data.getEnum("display", DisplayOption.class, DisplayOption.BLOCK);
	}
	
	public void setSortOption(SortOption sortOption) {
		data.put("sort", sortOption.getName());
		data.save();
	}
	
	public SortOption getSortOption() {
		return SortOption.getByName(data.get("sort", "ALPHABETICAL"));
	}
	
	public List<SortOption> getSortOptionOrder() {
		return sortOptionOrder;
	}
	
}
