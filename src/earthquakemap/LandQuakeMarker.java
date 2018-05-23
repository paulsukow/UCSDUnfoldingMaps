package earthquakemap;

import de.fhpotsdam.unfolding.data.PointFeature;
import processing.core.PGraphics;

public class LandQuakeMarker extends EarthquakeMarker {

	public LandQuakeMarker(PointFeature quake) {
		super(quake);
		isOnLand = true;
	}

	@Override
	public void drawEarthquake(PGraphics pg, float x, float y) {
        pg.image(EarthquakeCityMap.landMarker, x - 5, y - 5, 2 * radius, 2 * radius);
	}

	public String getCountry() {
		return (String) getProperty("country");
	}
}