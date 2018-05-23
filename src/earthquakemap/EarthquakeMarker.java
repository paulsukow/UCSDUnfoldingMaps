package earthquakemap;

import de.fhpotsdam.unfolding.data.PointFeature;
import processing.core.PConstants;
import processing.core.PGraphics;

public abstract class EarthquakeMarker extends CommonMarker implements Comparable<EarthquakeMarker> {

	protected boolean isOnLand;
	protected float radius;

	protected static final float KM_PER_MILE = 1.6f;

	public static final float THRESHOLD_MODERATE = 5;
	public static final float THRESHOLD_LIGHT = 4;
	public static final float THRESHOLD_INTERMEDIATE = 70;
	public static final float THRESHOLD_DEEP = 300;

	public abstract void drawEarthquake(PGraphics pg, float x, float y);

	public EarthquakeMarker (PointFeature feature) {
		super(feature.getLocation());

		java.util.HashMap<String, Object> properties = feature.getProperties();
		float magnitude = Float.parseFloat(properties.get("magnitude").toString());
		properties.put("radius", 2 * magnitude );
		setProperties(properties);
		this.radius = 1.75f * getMagnitude();
	}

	 public int compareTo(EarthquakeMarker marker) {
		 if (this.getMagnitude() > marker.getMagnitude()) {
			 return -1;
		 } else if (this.getMagnitude() < marker.getMagnitude()) {
			 return 1;
		 } else {
			 return 0;
		 }
	 }

	@Override
	public void drawMarker(PGraphics pg, float x, float y) {
		pg.pushStyle();

		colorDetermine(pg);
		drawEarthquake(pg, x, y);
		drawXOverMarkerIfQuakeWasInLastDay(pg, x, y);

		pg.popStyle();
	}

	private void drawXOverMarkerIfQuakeWasInLastDay(PGraphics pg, float x, float y) {
		String age = getStringProperty("age");
		if ("Past Hour".equals(age) || "Past Day".equals(age)) {
			pg.strokeWeight(2);
			int buffer = 2;
			pg.line(x-(radius+buffer),
					y-(radius+buffer),
					x+radius+buffer,
					y+radius+buffer);
			pg.line(x-(radius+buffer),
					y+(radius+buffer),
					x+radius+buffer,
					y-(radius+buffer));
		}
	}

	public void showTitle(PGraphics pg, float x, float y) {
		String title = getTitle();
		pg.pushStyle();

		pg.rectMode(PConstants.CORNER);

		pg.stroke(110);
		pg.fill(255,255,255);
		pg.rect(x, y + 15, pg.textWidth(title) +6, 18, 5);

		pg.textAlign(PConstants.LEFT, PConstants.TOP);
		pg.fill(0);
		pg.text(title, x + 3 , y +18);
		pg.popStyle();
	}

	public double threatCircle() {
		double miles = 20.0f * Math.pow(1.8, 2*getMagnitude()-5);
		double km = (miles * KM_PER_MILE);
		return km;
	}

	private void colorDetermine(PGraphics pg) {
		float depth = getDepth();

		if (depth < THRESHOLD_INTERMEDIATE) {
            pg.tint(255, 255, 0);
		}
		else if (depth < THRESHOLD_DEEP) {
            pg.tint(0, 0, 255);
		}
		else {
            pg.tint(255, 0, 0);
		}
	}

	public String toString() {
		return getTitle();
	}

	public float getMagnitude() {
		return Float.parseFloat(getProperty("magnitude").toString());
	}

	public float getDepth() {
		return Float.parseFloat(getProperty("depth").toString());
	}

	public String getTitle() {
		return (String) getProperty("title");
	}

	public float getRadius() {
		return Float.parseFloat(getProperty("radius").toString());
	}

	public boolean isOnLand() {
		return isOnLand;
	}
}
