package controllers;

import com.rdio.simple.Parameters;
import com.rdio.simple.RdioClient;
import com.rdio.simple.RdioClient.RdioException;
import com.rdio.simple.RdioCoreClient;
import com.sun.servicetag.UnauthorizedAccessException;

import org.json.JSONArray;
import org.json.JSONException;

import play.mvc.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

import org.json.JSONObject;

import models.*;

public class Application extends Controller {
	private final static int NUM_GENRES = 2;

	private static RdioClient getRdio() {
		String token = session.get("accessToken");
		String tokenSecret = session.get("accessTokenSecret");

		RdioClient.Consumer consumer = new RdioClient.Consumer(
				Tokens.RDIO_CONSUMER_KEY, Tokens.RDIO_CONSUMER_SECRET);

		if (token != null && tokenSecret != null) {
			return new RdioCoreClient(consumer, new RdioClient.Token(token,
					tokenSecret));
		} else {
			return new RdioCoreClient(consumer);
		}
	}

	public static void index() {
		try {
			// Get two random tracks
			String[] genres = new EchoNest().getGenres(NUM_GENRES);
			
			renderIndex(genres);			
		} catch (IOException e) {
			renderText("Error" + e.toString());
		}
	}

	public static void login() throws IOException {
		RdioClient rdio = getRdio();
		RdioClient.AuthState authState;
		try {
			authState = rdio.beginAuthentication(Router
					.getFullUrl("Application.callback"));
		} catch (RdioClient.RdioException e) {
			redirect(Router.getFullUrl("Application.logout"));
			return;
		}
		session.put("requestToken", authState.requestToken.token);
		session.put("requestTokenSecret", authState.requestToken.secret);
		session.remove("accessToken", "accessTokenSecret");
		redirect(authState.url);
	}

	public static void callback(String oauth_verifier) throws IOException {
		if (oauth_verifier != null) {
			RdioClient rdio = getRdio();
			RdioClient.Token requestToken = new RdioClient.Token(
					session.get("requestToken"),
					session.get("requestTokenSecret"));
			RdioClient.Token accessToken;
			try {
				accessToken = rdio.completeAuthentication(oauth_verifier,
						requestToken);
			} catch (RdioClient.RdioException e) {
				session.remove("requestToken", "requestTokenSecret");
				redirect(Router.getFullUrl("Application.logout"));
				return;
			}
			session.put("accessToken", accessToken.token);
			session.put("accessTokenSecret", accessToken.secret);
		}
		session.remove("requestToken", "requestTokenSecret");
		redirect(Router.getFullUrl("Application.index"));
	}

	public static void logout() {
		session.remove("accessToken", "accessTokenSecret");
		redirect(Router.getFullUrl("Application.index"));
	}

	public static void pick(String genre) {
		try {
			String[] lessGenres = new EchoNest().getGenres(NUM_GENRES - 1);
			String[] genres = new String[NUM_GENRES];
			
			genres[0] = genre;
			for(int i = 1; i < NUM_GENRES; i++) {
				genres[i] = lessGenres[i - 1];
			}

			renderIndex(genres);
		} catch (IOException e) {
			renderText("Error" + e.toString());
		}
	}

	private static void renderIndex(String[] genres) throws IOException {
		Track[] tracks = new EchoNest().getTracks(NUM_GENRES, genres);

		Parameters params = new Parameters();
		params.and("keys", tracks[0].id + "," + tracks[1].id);
		
		try {
			String response = getRdio().call("get", params);
			JSONObject result = new JSONObject(response).getJSONObject("result");
	
			for (int i = 0; i < NUM_GENRES; i++) {
				JSONObject jsonTrack = result.getJSONObject(tracks[i].id);
				tracks[i].name = jsonTrack.getString("name");
				tracks[i].artist = jsonTrack.getString("artist");
				tracks[i].album = jsonTrack.getString("album");
				tracks[i].albumImageUrl = jsonTrack.getString("icon");
				tracks[i].duration = jsonTrack.getInt("duration");
			}
		} catch(RdioClient.AuthorizationException e) {
			redirect(Router.getFullUrl("Application.login"));
		
		// The apis would have changed if this happens
		} catch(RdioClient.RdioException | JSONException e) {
			throw new IOException(e);
		}

		render("Application/index.html", tracks);
	}
}