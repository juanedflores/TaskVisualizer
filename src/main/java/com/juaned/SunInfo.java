package com.juaned;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

public class SunInfo {

  public static JSONObject sunInfoObject;

  public SunInfo() throws IOException, JSONException {
    LocalDate today = LocalDate.now();
    String yesterdayString = (today.minusDays(1)).format(DateTimeFormatter.ISO_DATE);

    String urlString =
        String.format(
            "https://api.sunrise-sunset.org/json?lat=29.42412&lng=-98.49363&date=%s",
            yesterdayString);

    sunInfoObject = readJsonFromUrl(urlString);
    String sunriseString = sunInfoObject.getString("sunrise");
    String sunsetString = sunInfoObject.getString("sunset");

    // changed yyyy-MM-dd h:mm:ss a to yyyy-MM-dd H:mm:ss
    String DATE_FORMAT = "yyyy-MM-dd h:mm:ss a";
    ZoneId central = ZoneId.of("America/Chicago");

    String sunriseDateString = yesterdayString + " " + sunriseString;
    System.out.println("sunrisedatestring: " + sunriseDateString);
    LocalDateTime sunrise_ldt =
        LocalDateTime.parse(sunriseDateString, DateTimeFormatter.ofPattern(DATE_FORMAT));
    System.out.println("sunrise_ldt " + sunrise_ldt);
    ZonedDateTime sunriseTZ = sunrise_ldt.atZone(ZoneOffset.UTC).withZoneSameInstant(central);

    String sunsetDateString = yesterdayString + " " + sunsetString;
    System.out.println("sunset: " + sunsetDateString);

    LocalDateTime sunset_ldt =
        LocalDateTime.parse(sunsetDateString, DateTimeFormatter.ofPattern(DATE_FORMAT));
    ZonedDateTime sunsetTZ = sunset_ldt.atZone(ZoneOffset.UTC).withZoneSameInstant(central);

    Float sunsethrfract = (float) sunsetTZ.getHour() + ((float) sunsetTZ.getMinute() / 60);
    Float sunrisehrfract = (float) sunriseTZ.getHour() + ((float) sunriseTZ.getMinute() / 60);

    sunInfoObject.put("sunset", sunsethrfract);
    sunInfoObject.put("sunrise", sunrisehrfract);
  }

  public static JSONObject readJsonFromUrl(String urlQueryString)
      throws IOException, JSONException {
    InputStream input = new URL(urlQueryString).openStream();
    try {
      BufferedReader re =
          new BufferedReader(new InputStreamReader(input, Charset.forName("UTF-8")));
      String text = Read(re);
      JSONObject json = new JSONObject(text).getJSONObject("results"); // input stream to string
      return json;
    } catch (IOException ex) {
      return null;
    } finally {
      input.close();
    }
  }

  public static String Read(Reader re) throws IOException { // class Declaration
    StringBuilder str = new StringBuilder(); // To Store Url Data In String.
    int temp;
    do {
      temp = re.read(); // reading Charcter By Chracter.
      str.append((char) temp);
    } while (temp != -1);
    return str.toString();
  }
}
