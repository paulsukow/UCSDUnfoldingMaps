package earthquakemap;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.AbstractMapProvider;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import processing.core.PApplet;
import processing.core.PImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * This is app was created to practice general OOP in Java and is the course project from the
 * Coursera UCSD Object Oriented Programing in Java Course: https://www.coursera.org/learn/object-oriented-java
 * Author: UC San Diego Intermediate Software Development MOOC team
 * @author Paul Sukow
 * Date: July 17, 2015
 * */
public class EarthquakeCityMap extends PApplet {

	private static final long serialVersionUID = 1L;

	private static final boolean OFFLINE = false;

	private static final String CITY_DATA = "city-data.json";
	private static final String COUNTRIES_DATA = "countries.geo.json";
	private static final String OFFLINE_MAP_TILES = "blankLight-1-3.mbtiles";

	private String earthquakesFeedURL = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";

	private UnfoldingMap map;

	private List<Marker> cityMarkers;
	private List<Marker> quakeMarkers;
	private List<Marker> countryMarkers;

	private CommonMarker lastSelected;
	private CommonMarker lastClicked;

    static PImage oceanMarker;
    static PImage landMarker;
    static PImage cityMarker;

	public void setup() {
		loadMarkerImages();

		initializeCanvas();
		map = createUnfoldingMap();

		MapUtils.createDefaultEventDispatcher(this, map);

        loadCountryMarkers();
		loadCityMarkers();
		loadQuakeMarkers();

	    printQuakesToConsoleForDebugging();
        sortAndPrintMarkersToConsoleForDebugging(20);

        addMarkersToMap();
	}

	private void loadMarkerImages() {
		oceanMarker = loadImage("OceanMarker.png");
		landMarker = loadImage("LandMarker.png");
		cityMarker = loadImage("CityMarker.png");
	}

	private void initializeCanvas() {
		size(900, 700, OPENGL);
	}

	private UnfoldingMap createUnfoldingMap() {
		AbstractMapProvider mapProvider = OFFLINE ? new MBTilesMapProvider(OFFLINE_MAP_TILES) : new Google.GoogleMapProvider();
		return new UnfoldingMap(this, 200, 50, 650, 600, mapProvider);
	}

	private void loadCountryMarkers() {
		List<Feature> countries = GeoJSONReader.loadData(this, COUNTRIES_DATA);
		countryMarkers = MapUtils.createSimpleMarkers(countries);
	}

	private void loadCityMarkers() {
		List<Feature> cities = GeoJSONReader.loadData(this, CITY_DATA);
		cityMarkers = cities.stream()
				.map(CityMarker :: new)
				.collect(toList());
	}

	private void loadQuakeMarkers() {
		if (OFFLINE) {
			earthquakesFeedURL = "2.5_week.atom";  // The same feed, but saved August 7, 2015
		}
		else {
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
			earthquakesFeedURL = "2.5_week.atom";
		}

		List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesFeedURL);
		quakeMarkers = new ArrayList<>();

		for(PointFeature feature : earthquakes) {
			if(isLand(feature)) {
				quakeMarkers.add(new LandQuakeMarker(feature));
			}
			else {
				quakeMarkers.add(new OceanQuakeMarker(feature));
			}
		}
	}

	private void sortAndPrintMarkersToConsoleForDebugging(int numToPrint) {
		Marker[] quakes = quakeMarkers.toArray(new Marker[quakeMarkers.size()]);
		Arrays.sort(quakes);

		if(numToPrint > quakes.length) {
			numToPrint = quakes.length;
		}

		for(int i = 0; i < numToPrint; i++) {
			println(quakes[i]);
		}
	}

	private void addMarkersToMap() {
		map.addMarkers(quakeMarkers);
		map.addMarkers(cityMarkers);
	}

	public void draw() {
		background(0);
		map.draw();
		addKey();
	}

	@Override
	public void mouseMoved() {
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;
		}

		selectMarkerIfHover(quakeMarkers);
		selectMarkerIfHover(cityMarkers);
	}

	private void selectMarkerIfHover(List<Marker> markers) {
		if (lastSelected != null) {
			return;
		}

		for (Marker m : markers) {
			CommonMarker marker = (CommonMarker)m;
			if (marker.isInside(map,  mouseX, mouseY)) {
				lastSelected = marker;
				marker.setSelected(true);
				return;
			}
		}
	}

	@Override
	public void mouseClicked() {
		if (lastClicked != null) {
			unhideMarkers();
			lastClicked = null;
		}
		else if (lastClicked == null) {
			checkEarthquakesForClick();
			if (lastClicked == null) {
				checkCitiesForClick();
			}
		}
	}

	private void checkCitiesForClick() {
		if (lastClicked != null) {
			return;
		}

		for (Marker marker : cityMarkers) {
			if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {
				lastClicked = (CommonMarker)marker;

				for (Marker mhide : cityMarkers) {
					if (mhide != lastClicked) {
						mhide.setHidden(true);
					}
				}
				for (Marker mhide : quakeMarkers) {
					EarthquakeMarker quakeMarker = (EarthquakeMarker)mhide;
					if (quakeMarker.getDistanceTo(marker.getLocation())
							> quakeMarker.threatCircle()) {
						quakeMarker.setHidden(true);
					}
				}
				return;
			}
		}
	}

	private void checkEarthquakesForClick() {
		if (lastClicked != null) {
			return;
		}

		for (Marker m : quakeMarkers) {
			EarthquakeMarker marker = (EarthquakeMarker)m;
			if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {
				lastClicked = marker;

				for (Marker mhide : quakeMarkers) {
					if (mhide != lastClicked) {
						mhide.setHidden(true);
					}
				}
				for (Marker mhide : cityMarkers) {
					if (mhide.getDistanceTo(marker.getLocation())
							> marker.threatCircle()) {
						mhide.setHidden(true);
					}
				}
				return;
			}
		}
	}

	private void unhideMarkers() {
		quakeMarkers.forEach(marker -> marker.setHidden(false));
		cityMarkers.forEach(marker -> marker.setHidden(false));
	}

	private void addKey() {
		fill(255, 250, 240);

		int xbase = 25;
		int ybase = 50;

		rect(xbase, ybase, 150, 250);

		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Earthquake Key", xbase+25, ybase+25);

        int city_xbase = xbase + 35;
        int city_ybase = ybase + 50;
        tint(150, 30, 30);
		image(EarthquakeCityMap.cityMarker, city_xbase - 5, city_ybase - 6, 15, 15);
        noTint();

		fill(0, 0, 0);
		textAlign(LEFT, CENTER);
        text("City Marker", city_xbase + 15, city_ybase);

        int land_xbase = xbase + 35;
        int land_ybase = ybase + 70;
        tint(0, 0, 255);
        image(EarthquakeCityMap.landMarker, land_xbase - 5, land_ybase - 6, 15, 15);
        noTint();

        fill(0, 0, 0);
        textAlign(LEFT, CENTER);
        text("Land Quake", xbase+50, ybase+70);

        int ocean_xbase = xbase + 35;
        int ocean_ybase = ybase + 90;
        tint(0, 0, 255);
        image(EarthquakeCityMap.oceanMarker, ocean_xbase - 5, ocean_ybase - 6, 15, 15);
        noTint();

        fill(0, 0, 0);
        textAlign(LEFT, CENTER);
		text("Ocean Quake", xbase+50, ybase+90);

        text("Size ~ Magnitude", xbase+25, ybase+110);

		fill(255, 255, 255);

		fill(color(255, 255, 0));
		ellipse(xbase+35, ybase+140, 12, 12);
		fill(color(0, 0, 255));
		ellipse(xbase+35, ybase+160, 12, 12);
		fill(color(255, 0, 0));
		ellipse(xbase+35, ybase+180, 12, 12);

		textAlign(LEFT, CENTER);
		fill(0, 0, 0);
		text("Shallow", xbase+50, ybase+140);
		text("Intermediate", xbase+50, ybase+160);
		text("Deep", xbase+50, ybase+180);

		text("Past hour", xbase+50, ybase+200);

		fill(255, 255, 255);
		int centerx = xbase+35;
		int centery = ybase+200;
		ellipse(centerx, centery, 12, 12);

		strokeWeight(2);
		line(centerx-8, centery-8, centerx+8, centery+8);
		line(centerx-8, centery+8, centerx+8, centery-8);
	}

	private boolean isLand(PointFeature earthquake) {
		for (Marker country : countryMarkers) {
			if (isInCountry(earthquake, country)) {
				return true;
			}
		}

		return false;
	}

	private void printQuakesToConsoleForDebugging() {
		int totalWaterQuakes = quakeMarkers.size();
		for (Marker country : countryMarkers) {
			String countryName = country.getStringProperty("name");
			int numQuakes = 0;
			for (Marker marker : quakeMarkers)
			{
				EarthquakeMarker eqMarker = (EarthquakeMarker)marker;
				if (eqMarker.isOnLand()) {
					if (countryName.equals(eqMarker.getStringProperty("country"))) {
						numQuakes++;
					}
				}
			}
			if (numQuakes > 0) {
				totalWaterQuakes -= numQuakes;
				System.out.println(countryName + ": " + numQuakes);
			}
		}
		System.out.println("OCEAN QUAKES: " + totalWaterQuakes);
	}

	private boolean isInCountry(PointFeature earthquake, Marker country) {
		Location earthquakeLocation = earthquake.getLocation();

		if(country.getClass() == MultiMarker.class) {

			for(Marker marker : ((MultiMarker)country).getMarkers()) {
				if(((AbstractShapeMarker)marker).isInsideByLocation(earthquakeLocation)) {
					earthquake.addProperty("country", country.getProperty("name"));
					return true;
				}
			}
		}

		else if(((AbstractShapeMarker)country).isInsideByLocation(earthquakeLocation)) {
			earthquake.addProperty("country", country.getProperty("name"));
			return true;
		}
		return false;
	}
}
