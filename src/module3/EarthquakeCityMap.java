package module3;

//Java utilities libraries
import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
import java.util.List;

//Processing library
import de.fhpotsdam.unfolding.geo.Location;
import processing.core.PApplet;

//Unfolding libraries
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;

//Parsing library
import parsing.ParseFeed;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 * @author Your name here
 * Date: July 17, 2015
 * */
public class EarthquakeCityMap extends PApplet {

	// You can ignore this.  It's to keep eclipse from generating a warning.
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFLINE, change the value of this variable to true
	private static final boolean offline = false;
	
	// Less than this threshold is a light earthquake
	public static final float THRESHOLD_MODERATE = 5;
	// Less than this threshold is a minor earthquake
	public static final float THRESHOLD_LIGHT = 4;

	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	
	// The map
	private UnfoldingMap map;
	
	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";


	public void setup() {
		size(950, 600, OPENGL);

		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 700, 500, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom"; 	// Same feed, saved Aug 7, 2015, for working offline
		}
		else {
			map = new UnfoldingMap(this, 200, 50, 700, 500, new Google.GoogleMapProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
			//earthquakesURL = "2.5_week.atom";
		}
		
	    map.zoomToLevel(2);
	    MapUtils.createDefaultEventDispatcher(this, map);	
			
	    // The List you will populate with new SimplePointMarkers
	    List<Marker> markers = new ArrayList<Marker>();

	    //Use provided parser to collect properties for each earthquake
	    //PointFeatures have a getLocation method
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    
	    // These print statements show you (1) all of the relevant properties 
	    // in the features, and (2) how to get one property and use it
	    if (earthquakes.size() > 0) {
	    	PointFeature f = earthquakes.get(0);
	    	System.out.println(f.getProperties());
	    	Object magObj = f.getProperty("magnitude");
	    	float mag = Float.parseFloat(magObj.toString());
	    	// PointFeatures also have a getLocation method
	    }
	    
	    //TODO: Add code here as appropriate
		for (PointFeature eq : earthquakes) {
			Location location = eq.getLocation();
			SimplePointMarker eqMarker = new SimplePointMarker(location);

            Object magObj = eq.getProperty("magnitude");
            float mag = Float.parseFloat(magObj.toString());

            if (mag < 4.0) {
				setAsSmall(eqMarker);
			} else if (mag >= 4.0 && mag < 5.0) {
				setAsMedium(eqMarker);
			} else if (mag >= 5.0) {
				setAsLarge(eqMarker);
			}
			markers.add(eqMarker);
		}

		map.addMarkers(markers);

	}

	private void setAsLarge(SimplePointMarker eqMarker) {
		int red = color(255, 0, 0);
		eqMarker.setColor(red);
		eqMarker.setRadius(12);
	}

	private void setAsMedium(SimplePointMarker eqMarker) {
		int yellow = color(255, 255, 0);
		eqMarker.setColor(yellow);
		eqMarker.setRadius(8);
	}

	private void setAsSmall(SimplePointMarker eqMarker) {
		int blue = color(0, 0, 255);
		eqMarker.setColor(blue);
		eqMarker.setRadius(4);
	}

	// A suggested helper method that takes in an earthquake feature and 
	// returns a SimplePointMarker for that earthquake
	// TODO: Implement this method and call it from setUp, if it helps
	private SimplePointMarker createMarker(PointFeature feature)
	{
		// finish implementing and use this method, if it helps.
		return new SimplePointMarker(feature.getLocation());
	}
	
	public void draw() {
	    background(10);
	    map.draw();
	    addKey();
	}


	// helper method to draw key in GUI
	// TODO: Implement this method to draw the key
	private void addKey() {
		// Remember you can use Processing's graphics methods here
        addKeyBorder();
        addKeyTitle();
        addSmallEqKeyItem();
        addMediumKeyItem();
        addLargeKeyItem();
	}

    private void addLargeKeyItem() {
        int red = color(254, 0, 0);
        fill(red);
        ellipse(60, 100, 12, 12);

        fill(0);
        textSize(10);
        textAlign(LEFT, CENTER);
        text("5.0+ Magnitude", 75, 100);
    }

    private void addMediumKeyItem() {
        int yellow = color(255, 255, 0);
        fill(yellow);
        ellipse(60, 125, 8, 8);

        fill(0);
        textSize(10);
        textAlign(LEFT, CENTER);
        text("4.0+ Magnitude", 75, 125);
    }

    private void addSmallEqKeyItem() {
        int blue = color(0, 0, 255);
        fill(blue);
        ellipse(60, 152, 4, 4);

        fill(0);
        textSize(10);
        textAlign(LEFT, CENTER);
        text("Below 4.0", 75, 150);
    }

    private void addKeyTitle() {
        fill(0);
        textSize(14);
        textAlign(CENTER, CENTER);
        text("Earthquake Key", 100, 75);
    }


    private void addKeyBorder() {
        fill(255);
        float xLocation = 25;
        float yLocation = 50;
        float width = 150;
        float height = 200;
        rect(xLocation, yLocation, width, height);
    }
}
