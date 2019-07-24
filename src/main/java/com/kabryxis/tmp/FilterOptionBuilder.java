package com.kabryxis.tmp;

import com.kabryxis.kabutils.data.Sets;
import com.kabryxis.kabutils.string.Strings;
import com.kabryxis.tmp.media.Show;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class FilterOptionBuilder {
	
	protected boolean type;
	
	public FilterOptionBuilder(boolean type) {
		this.type = type;
	}
	
	public FilterOptionBuilder() {
		this(true);
	}
	
	public FilterOptionBuilder type(boolean type) {
		this.type = type;
		return this;
	}
	
	protected boolean genreType = true;
	
	public FilterOptionBuilder genreType(boolean genreType) {
		this.genreType = genreType;
		return this;
	}
	
	protected Set<String> genreList;
	
	public FilterOptionBuilder addGenre(String genre) {
		if(genreList == null) genreList = new HashSet<>();
		genreList.add(genre);
		return this;
	}
	
	public FilterOptionBuilder addGenres(String... genres) {
		if(genreList == null) genreList = Sets.newHashSet(genres);
		else Collections.addAll(genreList, genres);
		return this;
	}
	
	public FilterOptionBuilder addGenres(Collection<String> genres) {
		if(genreList == null) genreList = new HashSet<>(genres);
		else genreList.addAll(genres);
		return this;
	}
	
	public FilterOptionBuilder removeGenre(String genre) {
		if(genreList != null) genreList.remove(genre);
		return this;
	}
	
	protected double rating = 0.0;
	
	public FilterOptionBuilder rating(double rating) {
		this.rating = rating;
		return this;
	}
	
	public Predicate<Show> build() {
		Predicate<Show> showPredicate = null;
		if(genreList != null) {
			for(String genre : genreList) {
				Predicate<Show> genrePredicate = show -> Strings.containsIgnoreCase(show.getGenres(), genre);
				if(showPredicate == null) showPredicate = genrePredicate;
				else if(genreType) showPredicate = showPredicate.and(genrePredicate);
				else showPredicate = showPredicate.or(genrePredicate);
			}
		}
		if(rating > 0.0) {
			Predicate<Show> ratingPredicate = show -> show.getAverageRating() >= rating;
			if(showPredicate == null) showPredicate = ratingPredicate;
			else if(type) showPredicate = showPredicate.and(ratingPredicate);
			else showPredicate = showPredicate.or(ratingPredicate);
		}
		return showPredicate == null ? show -> true : showPredicate;
	}
	
}
