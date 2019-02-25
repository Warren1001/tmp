package com.kabryxis.tmp.user;

import com.kabryxis.kabutils.Images;
import com.kabryxis.kabutils.data.file.Files;
import com.kabryxis.kabutils.data.file.yaml.Config;
import com.kabryxis.kabutils.data.file.yaml.serialization.SerializationType;
import com.kabryxis.kabutils.data.file.yaml.serialization.Serializer;
import com.kabryxis.tmp.TMP;
import com.kabryxis.tmp.swing.BarMenu;
import com.kabryxis.tmp.swing.BasicMouseListener;
import com.kabryxis.tmp.swing.ComponentBuilder;
import com.kabryxis.tmp.swing.FadingImage;
import org.apache.commons.lang3.Validate;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class UserManager {
	
	private final List<Consumer<User>> selectedUserActions = new ArrayList<>();
	private final Map<String, User> users = new HashMap<>();
	
	private final TMP tmp;
	private final BarMenu barMenu;
	private final File userDirectory;
	private final Config data;
	private final Image baseUserImage;
	
	private User selectedUser;
	
	public UserManager(TMP tmp, BarMenu barMenu) {
		this.tmp = tmp;
		this.barMenu = barMenu;
		userDirectory = new File("users");
		userDirectory.mkdir();
		data = new Config(new File("data.yml"), true);
		baseUserImage = Images.loadFromResource(getClass().getClassLoader(), "user.png");
		Config.registerSerializer(new Serializer() {
			
			private final Class<?>[] classes = { Color.class };
			
			@Override
			public String getPrefix() {
				return "clr";
			}
			
			@Override
			public SerializationType getType() {
				return SerializationType.STRING;
			}
			
			@Override
			public Class<?>[] getClasses() {
				return classes;
			}
			
			@Override
			public Object deserialize(Object o) {
				String string = (String)o;
				String[] args = string.split(",");
				if(args.length >= 3) {
					int red = Integer.parseInt(args[0]);
					int green = Integer.parseInt(args[1]);
					int blue = Integer.parseInt(args[2]);
					int alpha = args.length == 4 ? Integer.parseInt(args[3]) : 255;
					return new Color(red, green, blue, alpha);
				}
				throw new IllegalArgumentException(o + " is not a Color object.");
			}
			
			@Override
			public Object serialize(Object obj) {
				Color color = (Color)obj;
				return color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "," + color.getAlpha();
			}
			
		});
		Runtime.getRuntime().addShutdownHook(new Thread(this::saveAll));
	}
	
	public void loadUsers() {
		Stream.of(Files.getFilesWithEnding(new File("users"), ".yml")).map(file -> new Config(file, true))
				.sorted(Comparator.comparingInt(o -> o.getInt("pos"))).forEachOrdered(config -> {
			String name = config.getName();
			Color color = config.get("color", Color.class, Color.WHITE);
			FadingImage fadingImage = new ComponentBuilder<>(new FadingImage(Images.setColor(Images.copy(baseUserImage), color))).build();
			User user = new User(tmp, config, fadingImage);
			fadingImage.addMouseListener(((BasicMouseListener)e -> setSelectedUser(user)));
			users.put(name, user);
			barMenu.addRight(fadingImage);
		});
		setSelectedUser(getUser(data.get("selected-user")));
	}
	
	public User createAndSelectUser() {
		String providedName = (String)JOptionPane.showInputDialog(null, null, "WHAT IS THOU NAME", JOptionPane.QUESTION_MESSAGE, new ImageIcon(), null, null);
		return users.computeIfAbsent(providedName.toLowerCase(), name -> {
			Color color = JColorChooser.showDialog(null, "Choose a profile color", Color.WHITE);
			FadingImage fadingImage = new ComponentBuilder<>(new FadingImage(Images.setColor(Images.copy(baseUserImage), color))).build();
			User user = new User(tmp, name, color, fadingImage);
			fadingImage.addMouseListener(((BasicMouseListener)e -> setSelectedUser(user)));
			user.getData().put("pos", users.size());
			users.put(name, user);
			barMenu.addRight(fadingImage);
			return user;
		});
	}
	
	public User getUser(String name) {
		return users.get(Validate.notNull(name));
	}
	
	public void setSelectedUser(User user) {
		Validate.notNull(user, "user cannot be null");
		if(selectedUser == user) return;
		if(selectedUser != null) selectedUser.setAsUnselected();
		selectedUser = user;
		user.setAsSelected();
		selectedUserActions.forEach(action -> action.accept(user));
		data.put("selected-user", user.getName());
		data.save();
	}
	
	public User getSelectedUser() {
		return selectedUser;
	}
	
	public void registerSelectedUserListener(Consumer<User> action) {
		selectedUserActions.add(action);
	}
	
	public void saveAll() {
		users.values().forEach(user -> user.getData().save());
	}
	
}
