package models;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import us.monoid.web.JSONResource;
import us.monoid.web.Resty;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

public class EchoNest {

	private final static int RESULT_LIMIT = 100;
	private final static String echoBase = "https://developer.echonest.com/api/v4/";
	private final static String echoApikey = "?api_key=" + Tokens.ECHONEST_KEY;

	public String[] getGenres(int numGenres) throws IOException {
		if (numGenres < 1) {
			return new String[] {};
		}
		
		String uri = echoBase + "artist/list_genres" + echoApikey;

		try {
			JSONArray genres = new Resty().json(uri).object()
					.getJSONObject("response").getJSONArray("genres");
	
			if (numGenres > genres.length()) {
				numGenres = genres.length();
			}
	
			String[] result = new String[numGenres];
	
			for (int i = 0; i < numGenres; i++) {
				int genreIndex = (int) (Math.random() * genres.length());
	
				result[i] = genres.getJSONObject(genreIndex).getString("name");
			}
	
			return result;
			
		// There are genres, so something went wrong.
		} catch(JSONException e) {
			throw new IOException(e);
		}
	}

	public Track[] getTracks(final int numTracks, final String[] genres) throws IOException {
		if(numTracks < 1 || genres.length < 1){
			return new Track[] {};
		}
		
		Track[] tracks = new Track[numTracks];

		for (int i = 0; i < 2; i++) {
			String genre;

			if (numTracks == genres.length) {
				genre = genres[i];
			} else {
				genre = genres[(int) (Math.random() * genres.length)];
			}

			String uri;
			
			uri = echoBase
					+ "song/search"
					+ echoApikey
					+ "&format=json&results="
					+ RESULT_LIMIT
					+ "&sort=song_hotttnesss-desc&bucket=id:rdio-CA&bucket=audio_summary&bucket=tracks"
					+ "&style=" + URLEncoder.encode(genre, "UTF-8");
			
			JSONResource response = new Resty().json(uri);
			
			try {
				JSONArray songs = response.object().getJSONObject("response").getJSONArray("songs");
	
				ArrayList indexes = new ArrayList<Integer>();
				
				for (int j = 0; j < songs.length(); j++) {
					if (songs.getJSONObject(j).getJSONArray("tracks").length() > 0) {
						indexes.add(j);
					}
				}

				// if num is 0 or no tracks returned.
				if(indexes.size() < 1) {
					//FIXME
					return new Track[] {};
				}
	
				tracks[i] = new Track();
				tracks[i].id = songs
						.getJSONObject(
								(int) indexes.get((int) (Math.random() * indexes
										.size()))).getJSONArray("tracks")
						.getJSONObject(0).getString("foreign_id")
						.substring(("rdio-CA:track:").length());
				tracks[i].genre = genre;
				
			// No songs returned
			} catch(JSONException e) {
				throw new IOException(e);
			}
		}

		return tracks;
	}

}
