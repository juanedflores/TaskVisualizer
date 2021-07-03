package com.juaned;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import org.json.JSONObject;

public class SunInfo {

	public static JSONObject sunInfoObject;

	public SunInfo() {

		LocalDate today = LocalDate.now();
		// LocalDate yesterday = (today.minusDays(1));
		String yesterdayString = (today.minusDays(1)).format(DateTimeFormatter.ISO_DATE);

		String urlString = String.format("https://api.sunrise-sunset.org/json?lat=29.42412&lng=-98.49363&date=%s",
				yesterdayString);

		// String jsonString = jsonGetRequest(urlString);
		// sunInfoObject = new JSONObject(jsonString).getJSONObject("results");
		sunInfoObject = jsonGetRequest(urlString);
		String sunriseString = sunInfoObject.getString("sunrise");
		String sunsetString = sunInfoObject.getString("sunset");

		String DATE_FORMAT = "yyyy-MM-dd H:mm:ss a";
		ZoneId central = ZoneId.of("America/Chicago");

		String sunriseDateString = yesterdayString + " " + sunriseString;
		LocalDateTime sunrise_ldt = LocalDateTime.parse(sunriseDateString, DateTimeFormatter.ofPattern(DATE_FORMAT));
		ZonedDateTime sunriseTZ = sunrise_ldt.atZone(ZoneOffset.UTC).withZoneSameInstant(central);

		String sunsetDateString = yesterdayString + " " + sunsetString;
		LocalDateTime sunset_ldt = LocalDateTime.parse(sunsetDateString, DateTimeFormatter.ofPattern(DATE_FORMAT));
		ZonedDateTime sunsetTZ = sunset_ldt.atZone(ZoneOffset.UTC).withZoneSameInstant(central);

		Float sunsethrfract = (float) sunsetTZ.getHour() + ((float) sunsetTZ.getMinute() / 60);
		Float sunrisehrfract = (float) sunriseTZ.getHour() + ((float) sunriseTZ.getMinute() / 60);

		sunInfoObject.put("sunset", sunsethrfract);
		sunInfoObject.put("sunrise", sunrisehrfract);
	}

	private static String streamToString(InputStream inputStream) {
		String text = new Scanner(inputStream, "UTF-8").useDelimiter("\\Z").next();
		return text;
	}

	public static JSONObject jsonGetRequest(String urlQueryString) {
		String json = null;
		try {
			URL url = new URL(urlQueryString);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setInstanceFollowRedirects(false);
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("charset", "utf-8");
			connection.connect();
			InputStream inStream = connection.getInputStream();
			json = streamToString(inStream); // input stream to string
			return new JSONObject(json).getJSONObject("results");
		} catch (IOException ex) {
			// possibly no internet
			System.out.println("Couldn't fetch sun data. No internet connection?");
			JSONObject jsonObject = new JSONObject("{'sunrise': '11:33:49 AM', 'sunset': '01:35:08 AM'}");
			GUI.showNoConnectionWarning();
			// ex.printStackTrace();
			return jsonObject;
		}
	}
}
