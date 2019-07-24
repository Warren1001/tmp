package com.kabryxis.tmp.swing;

import com.kabryxis.kabutils.Images;
import com.kabryxis.tmp.TMP;
import com.kabryxis.tmp.media.Show;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Set;

public class BlockTile extends JPanel {
	
	private static final int WIDTH = 380;
	
	private final Set<Component> hoverComponents = new HashSet<>();
	
	private final MouseListener clickListener, hoverListener;
	
	public BlockTile(Show show, Image image) {
		super(new FlowLayout(FlowLayout.CENTER, WIDTH, 8));
		hoverComponents.add(this);
		setPreferredSize(new Dimension(WIDTH, 500));
		setBackground(TMP.DEFAULT_BG_COLOR);
		clickListener = (BasicMouseListener)e -> show.getMediaManager().getTMP().getUserManager().getSelectedUser().getShowTracker(show).getLastSeasonTracker().getLastEpisode().play();
		hoverListener = (BasicMouseHoverListener)(e, hover) -> {
			Color color = hover ? TMP.DEFAULT_BG_COLOR.brighter() : TMP.DEFAULT_BG_COLOR;
			hoverComponents.forEach(component -> component.setBackground(color));
		};
		addMouseListener(clickListener);
		addMouseListener(hoverListener);
		addWithClick(new ComponentBuilder<>(new JImage(Images.resize(image, WIDTH, 300))).preferredSize(WIDTH, 300).build());
		String titleString = show.getFriendlyName();
		String englishName = show.getData().get("english", String.class);
		JTextArea title = new TextAreaBuilder().wrap(false).font(new Font("Segoe Print", Font.BOLD, 21)).fgColor(Color.WHITE).build();
		if(englishName != null) {
			boolean onlyEnglish = show.getMediaManager().getTMP().getUserManager().getSelectedUser().getData().getBoolean("only-english", false);
			if(onlyEnglish) titleString = englishName;
			else titleString += " - (" + englishName + ")";
			show.getMediaManager().getTMP().getUserManager().registerSelectedUserListener(user ->
					title.setText(user.getData().getBoolean("only-english", false) ? englishName : show.getFriendlyName() + " - (" + englishName + ")"));
		}
		title.setText(titleString);
		addWithClick(title);
		java.util.List<String> genres = show.getGenres();
		genres.sort(String.CASE_INSENSITIVE_ORDER);
		addWithClick(new TextAreaBuilder(String.join(", ", genres)).wrap(false).fgColor(Color.WHITE).build());
		double rating = show.getAverageRating();
		JImage starsImage = new JImage(TMP.getRatingStars(rating));
		starsImage.setToolTipText(String.valueOf(rating));
		addWithClick(starsImage);
		JTextArea browse = new TextAreaBuilder("Browse").wrap(false).font(new Font("Arial", Font.BOLD, 15)).fgColor(Color.BLUE).build();
		browse.addMouseListener((BasicMouseListener)e -> show.getMediaManager().getTMP().setCurrentlyVisibleMainPanel(show.getPagePanel()));
		browse.setToolTipText("Warning! Potential spoilers if you click this.");
		add(browse);
	}
	
	@Override
	public Component add(Component component) {
		hoverComponents.add(component);
		component.addMouseListener(hoverListener);
		return super.add(component);
	}
	
	public void addWithClick(Component component) {
		component.addMouseListener(clickListener);
		add(component);
	}
	
}
