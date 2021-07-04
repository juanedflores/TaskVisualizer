package com.juaned;

import processing.core.PApplet;
import processing.core.PFont;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.juaned.GUI.SavedTask;

import org.json.JSONArray;
import org.json.JSONObject;

public class ProcessingSketch extends PApplet {

	private static final long serialVersionUID = 1L;

	// Font
	PFont myFont;
	PFont boldFont;

	// JSON array
	JSONArray values;
	JSONObject sunvalues;

	// change these values manually in the size() function.
	int canvasWidth = 800; // 800
	int canvasHeight = 465; // 465

	// font size
	int fontSize = 18;

	// number constants
	int secondsinday = 86400;

	// Key appearance constants.
	int chartcenterX = canvasWidth / 2;
	int chartcenterY = canvasHeight / 2;
	double keyposXright = canvasWidth * 0.8125; // 650
	double keyposYright = canvasHeight * 0.09462; // 44
	double keyposXleft = canvasWidth * 0.1875; // 150
	double keyposYleft = canvasHeight * 0.09462; // 44
	double keyrectXoffset = canvasWidth * 0.03; // 24
	double keyrectYoffset = canvasHeight * 0.0086; // 4
	double keylineXoffset = canvasWidth * 0.00875; // 7
	double keylineYoffset = canvasHeight * 0.027956; // 13
	double keyspacing = canvasHeight * 0.10752; // 50, y distance between key labels.
	double keydataoffset = canvasHeight * 0.04301; // 20, y distance. font line height. might depend on font size.
	double keypercentoffset = canvasWidth * 0.1125; // 90, x distance between time and percentage text.
	int keylinelength = 32; // 32, depends on font size

	// colors
	int keyStrokeColor = color(0);
	int keyDurationColor = color(80);
	int completetaskcol = color(16, 71, 20); // percent col when user meets task goal.
	int incompletetaskcol = color(163, 54, 3); // percent col when user fails task goal.

	// CUSTOM USER VALUES
	// Daily task goal percentages.
	double sleepmin = 27.0; // min goal of sleeping at least 6hrs 30min.
	double sleepmax = 33.3; // max goal of not sleeping more than 8 hrs.
	double exhibitionmin = 8.3;
	double languagemin = 2.0;
	double miscmin = 4.16;

	// Array to keep track of special categories.
	String[] categories;
	int[] randomcolors;

	// Arrays to keep track of pie chart angles of all tasks.
	float[][] angles;
	int[] miscangles;
	int misctotalseconds = 0;

	// Arrays to keep track of starting position of all tasks.
	float[] startx;
	float[] startxmisc;

	public void setup() {
		size(800, 465);
		background(210);

		myFont = createFont("Piboto-Light", fontSize);
		boldFont = createFont("Piboto-BoldLtItalic", fontSize);

		// Get Input Data
		values = TaskInfo.taskInfoArray;
		sunvalues = SunInfo.sunInfoObject;

		// Instantiate total angle arrays to keep count.
		int anglecount = 0;
		int miscanglecount = 0;
		// Go through every task in json file.
		for (int i = 0; i < values.length(); i++) {
			JSONObject task = values.getJSONObject(i);
			JSONArray tags = task.getJSONArray("tags");
			// If there is more than one tag, then it is a special category.
			if (task.has("displaytag")) {
				anglecount++;
			} else {
				miscanglecount++;
				misctotalseconds += task.getInt("seconds");
			}
		}

		// Instantiate the arrays.
		categories = new String[anglecount];
		angles = new float[3][anglecount]; // [seconds][degrees][value determining whether on left or right side of
		// chart]
		startx = new float[anglecount]; // contains the degree of where a task starts.
		// misc
		miscangles = new int[miscanglecount];
		startxmisc = new float[miscanglecount];
		// random colors
		randomcolors = new int[anglecount];

		// Go through each task again to transfer info to arrays.
		int index = 0;
		int miscindex = 0;
		for (int i = 0; i < values.length(); i++) {
			JSONObject task = values.getJSONObject(i);
			JSONArray tags = task.getJSONArray("tags");
			// If special category, add category name and angles, else add angles to misc
			// category.
			if (task.has("displaytag")) {
				// Store the special category for the pie chart key.
				String displaytag = task.getString("displaytag");
				categories[index] = displaytag;

				angles[0][index] = task.getInt("seconds");
				angles[1][index] = map(task.getInt("seconds"), 0, secondsinday, 0, 360);
				startx[index] = PApplet.map(task.getFloat("startx"), (float) 0, (float) 24.0, (float) 0.0, (float) 360.0) + 90; // +90
																																																												// to
				index++;
			} else {
				miscangles[miscindex] = ceil(map(task.getInt("seconds"), 0, secondsinday, 0, 360));
				startxmisc[miscindex] = map(task.getFloat("startx"), (float) 0, (float) 24.0, (float) 0.0, (float) 360.0) + 90;
				miscindex++;
			}
		}

		// We must calculate the total seconds of every special category that gets
		// repeated.
		// We will store the total seconds in the index where the special tag first
		// appears.
		for (int i = 0; i < categories.length; i++) {
			for (int j = 0; j < i; j++) {
				if (categories[i].equals(categories[j])) {
					angles[0][j] += angles[0][i];
					angles[0][i] = 0;
				}
			}
		}

		/* DRAW */
		// Draw pie chart, save image, then exit program.
		angles = sortCategories(300, angles); // (diameter, angles array)
		pieTime(325); // draws the sundata
		pieChart(300, angles, miscangles);
	}

	void savePNG(String filePath) {
		saveFrame(filePath);
	}

	void pieTime(float diameter) {

		/* Get sun info */
		Float sunrisehrfract = sunvalues.getFloat("sunrise");
		Float sunsethrfract = sunvalues.getFloat("sunset");

		/* Draw sun info */
		stroke(0);
		float sunriseX = chartcenterX + cos(radians(map(sunrisehrfract, 0, 24, 0, 360) + 90)) * (diameter / 2);
		float sunriseY = chartcenterY + sin(radians(map(sunrisehrfract, 0, 24, 0, 360) + 90)) * (diameter / 2);
		line(chartcenterX, chartcenterY, sunriseX, sunriseY);
		float sunsetX = chartcenterX + cos(radians(map(sunsethrfract, 0, 24, 0, 360) + 90)) * (diameter / 2);
		float sunsetY = chartcenterY + sin(radians(map(sunsethrfract, 0, 24, 0, 360) + 90)) * (diameter / 2);
		line(chartcenterX, chartcenterY, sunsetX, sunsetY);
		fill(255, 140, 20);
		noStroke();
		arc(chartcenterX, chartcenterY, diameter + 25, diameter + 25, radians(map(sunrisehrfract, 0, 24, 0, 360) + 90),
				radians(map(sunsethrfract, 0, 24, 0, 360) + 90));
		fill(40, 140, 255);
		arc(chartcenterX, chartcenterY, diameter, diameter, radians(map(sunsethrfract, 0, 24, 0, 360) + 90),
				radians(map(sunrisehrfract, 0, 24, 0, 360) + 450));

		/* Draw hours */
		float angle = 0;
		float px = 0;
		float py = 0;
		float p2x = 0;
		float p2y = 0;
		float p3x = 0;
		float p3y = 0;
		int hour = 18;
		int sunoffset = 0;

		for (int i = 0; i < 24; i++) {

			if ((i + 18) % 24 >= sunrisehrfract && (i + 18) % 24 <= sunsethrfract) {
				sunoffset = 25;
			} else {
				sunoffset = 0;
			}

			// Draw a point and line.
			px = chartcenterX + cos(radians(angle)) * ((diameter + sunoffset) / 2);
			py = chartcenterY + sin(radians(angle)) * ((diameter + sunoffset) / 2);
			stroke(210);
			strokeWeight(5);
			point(px, py);

			p2x = chartcenterX + cos(radians(angle)) * ((diameter + 16 + sunoffset) / 2);
			p2y = chartcenterY + sin(radians(angle)) * ((diameter + 16 + sunoffset) / 2);
			p3x = chartcenterX + cos(radians(angle)) * ((diameter + 44 + sunoffset) / 2);
			p3y = chartcenterY - 2 + sin(radians(angle)) * ((diameter + 44 + sunoffset) / 2);
			stroke(0);
			strokeWeight((float) 1.5);
			line(px, py, p2x, p2y);

			// Draw the hour.
			textAlign(CENTER, CENTER);
			fill(125);
			if (hour == 24) {
				text("00", p3x, p3y);
			} else if (hour < 10) {
				String hourtext = "0" + hour;
				text(hourtext, p3x, p3y);
			} else {
				text(hour, p3x, p3y);
			}

			hour++;

			if (hour == 25) {
				hour = 1;
			}
			angle += 15;
		}
	}

	void pieChart(float diameter, float[][] data, int[] miscdata) {

		// Draw background of pie chart.
		fill(30, 20, 10);
		noStroke();
		ellipse(chartcenterX, chartcenterY, diameter, diameter);

		// Draw miscellaneous task slices first.
		for (int i = 0; i < miscdata.length; i++) {
			strokeWeight((float) 0.5);
			stroke(255, 255, 0);
			float gray = map(i, 0, miscdata.length, 200, 70);
			fill(gray);
			arc(chartcenterX, chartcenterY, diameter, diameter, radians(startxmisc[i]),
					radians(startxmisc[i]) + radians(miscdata[i]), PIE);
		}

		// Draw special category task slices.
		int percentcol = color(255, 0, 0);
		for (int i = 0; i < data[0].length; i++) {

			for (int j = 0; j < GUI.savedTasksArray.size(); j++) {
				SavedTask savedTask = GUI.savedTasksArray.get(j);
				int rancol = color(random(255), random(255), random(255));
				if (categories[i].equals(savedTask.name.toUpperCase())) {
					int r = savedTask.color.getRed();
					int g = savedTask.color.getGreen();
					int b = savedTask.color.getBlue();
					rancol = color(r,g,b);
					fill(rancol);
					break;
				} else {
					stroke(keyStrokeColor);
					fill(rancol);
				}
			}

			// The drawkey boolean is to prevent repeats of keys.
			boolean drawkey = true;
			if (data[0][i] == 0) {
				drawkey = false;
			}

			// Draw special category slice.
			strokeWeight((float) 0.5);
			arc(chartcenterX, chartcenterY, diameter, diameter, radians(startx[i]), radians(startx[i]) + radians(data[1][i]),
					PIE);
			// Draw the special category point.
			float arcmidpoint = startx[i] + data[1][i] / 2;
			float midX = chartcenterX + cos(radians(arcmidpoint)) * diameter / 2;
			float midY = chartcenterY + sin(radians(arcmidpoint)) * diameter / 2;
			strokeWeight(6);
			point(midX, midY);

			boolean right = true;
			if (data[2][i] == 0.0) {
				right = false;
			}

			if (drawkey) {
				if (right) {
					// Drawing the key color.
					strokeWeight(1);
					rectMode(CENTER);
					rect((float) keyposXright - (float) keyrectXoffset, (float) keyposYright - (float) keyrectYoffset, (float) 16,
							(float) 16);

					// Draw a line.
					stroke(175);
					line((float) keyposXright - (float) keylineXoffset, (float) keyposYright - (float) keylineYoffset,
							(float) keyposXright - (float) keylineXoffset,
							(float) keyposYright - (float) keylineYoffset + (float) keylinelength);

					// Drawing the key text.
					fill(keyStrokeColor);
					textSize(18);
					textFont(myFont);
					textAlign(LEFT);
					text(categories[i], (float) keyposXright, (float) keyposYright);

					// Draw the duration of task.
					fill(keyDurationColor);
					textSize(12);
					int minutes = (int) (data[0][i] / 60);
					int hours = 0;
					if (minutes >= 60) {
						hours = minutes / 60;
						minutes = minutes % 60;
					}
					text(hours + " hrs " + minutes + " mins ", (float) keyposXright,
							(float) keyposYright - 2 + (float) keydataoffset);

					// // Draw the percentage.
					fill(percentcol);
					textSize(12);
					float percentage = data[0][i] / (float) 86400.0 * 100;
					String strpercent = nf(percentage, 2, 2) + "%";
					text(strpercent, (float) (keyposXright + keypercentoffset), (float) (keyposYright - 2 + keydataoffset));

					keyposYright = keyposYright + keyspacing;
				} else {
					// Drawing the key color.
					strokeWeight(1);
					rectMode(CENTER);
					rect((float) (keyposXleft + keyrectXoffset), (float) (keyposYleft - keyrectYoffset), 15, 15);

					// Draw a line.
					stroke(175);
					line((float) (keyposXleft + keylineXoffset), (float) (keyposYleft - keylineYoffset),
							(float) (keyposXleft + keylineXoffset), (float) (keyposYleft - keylineYoffset + keylinelength));

					// Drawing the key text.
					fill(keyStrokeColor);
					textSize(18);
					textFont(myFont);
					textAlign(RIGHT);
					text(categories[i], (float) keyposXleft, (float) keyposYleft);

					// Draw the duration of task.
					fill(keyDurationColor);
					textSize(12);
					int minutes = (int) (data[0][i] / 60);
					int hours = 0;
					if (minutes >= 60) {
						hours = minutes / 60;
						minutes = minutes % 60;
					}
					text(hours + " hrs " + minutes + " mins ", (float) keyposXleft, (float) (keyposYleft - 2 + keydataoffset));

					// // Draw the percentage.
					fill(percentcol);
					textSize(12);
					float percentage = (float) (data[0][i] / 86400.0 * 100);
					String strpercent = nf(percentage, 2, 2) + "%";
					text(strpercent, (float) (keyposXleft - keypercentoffset), (float) (keyposYleft - 2 + keydataoffset));

					keyposYleft = keyposYleft + keyspacing;
				}
			}
		}

		// Add a misc category to key.
		strokeWeight(1);
		stroke(255, 255, 0);
		fill(125);
		rectMode(CENTER);
		rect((float) (keyposXright - keyrectXoffset), (float) (keyposYright - keyrectYoffset), 15, 15);
		// Draw a line.
		stroke(175);
		line((float) (keyposXright - keylineXoffset), (float) (keyposYright - keylineYoffset),
				(float) (keyposXright - keylineXoffset), (float) (keyposYright - keylineYoffset + keylinelength));
		fill(125);
		textSize(18);
		textAlign(LEFT);
		text("misc.", (float) (keyposXright), (float) (keyposYright));
		// Draw the total duration of misctask(s).
		fill(125);
		textSize(12);
		int miscminutes = misctotalseconds / 60;
		int mischours = 0;
		if (miscminutes >= 60) {
			mischours = miscminutes / 60;
			miscminutes = miscminutes % 60;
		}
		text(mischours + " hrs " + miscminutes + " mins ", (float) keyposXright,
				(float) (keyposYright - 2 + keydataoffset));

		/* GOAL */
		// Minimum of 1 hour of miscellaneous work.
		// Draw the percentage.
		textSize(12);
		float percentage = (float) (misctotalseconds / 86400.0 * 100);
		if (percentage >= miscmin) {
			percentcol = completetaskcol;
		} else {
			percentcol = incompletetaskcol;
		}
		String strpercent = nf(percentage, 2, 2) + "%";
		fill(percentcol);
		text(strpercent, (float) (keyposXright + keypercentoffset), (float) (keyposYright - 2 + keydataoffset));

		// Draw border of pie chart.
		stroke(255, 100, 30);
		noStroke();
		noFill();
		ellipse(chartcenterX, chartcenterY, diameter, diameter);

		// Draw the date.
		fill(30, 20, 10);
		textAlign(CENTER);
		textSize(17);
		textFont(boldFont);

		DateTimeFormatter date_format = DateTimeFormatter.ofPattern("EEEE - MMMM dd, yyyy");
		text((LocalDate.parse(TaskInfo.taskDay, DateTimeFormatter.ofPattern("yyyy-MM-dd"))).format(date_format), width / 2,
				height - 18);
	}

	float[][] sortCategories(float diameter, float[][] data) {

		// Instantiate new array that will store sorted values.
		float sorted[][] = new float[3][data[0].length];
		String newcategories[] = new String[data[0].length];
		float newstartx[] = new float[data[0].length];

		// Determine if slice is on the left side or right side.
		for (int i = 0; i < data[0].length; i++) {
			float arcmidpoint = startx[i] + data[1][i] / 2; // data[1] contains duration in degrees.
			float midX = chartcenterX + cos(radians(arcmidpoint)) * diameter / 2;
			if (midX >= width / 2) { // on right side
				data[2][i] = 1;
			} else if (midX < width / 2) { // on left side
				data[2][i] = 0;
			}
		}

		// Get all Y values of the center edge of each pie slice.
		float[] sortedMidY = new float[data[0].length];
		for (int i = 0; i < data[0].length; i++) {
			float arcmidpoint = startx[i] + data[1][i] / 2;
			float midY = chartcenterY + sin(radians(arcmidpoint)) * diameter / 2;
			sortedMidY[i] = midY;
		}

		// Sort the list of Y values.
		sortedMidY = sort(sortedMidY); // sorts from least to greatest.

		// Look for the matching values to make the appropriate switch of index.
		for (int i = 0; i < data[0].length; i++) {
			float arcmidpoint = startx[i] + data[1][i] / 2;
			float midY = chartcenterY + sin(radians(arcmidpoint)) * diameter / 2;
			for (int j = 0; j < data[0].length; j++) {
				if (midY == sortedMidY[j]) {
					sortedMidY[j] = -1;
					sorted[0][j] = data[0][i];
					sorted[1][j] = data[1][i];
					sorted[2][j] = data[2][i];
					newstartx[j] = startx[i];
					newcategories[j] = categories[i];
					break;
				}
			}
		}
		categories = newcategories;
		startx = newstartx;

		return sorted;
	}

}
