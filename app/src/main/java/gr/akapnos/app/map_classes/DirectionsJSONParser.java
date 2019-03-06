package gr.akapnos.app.map_classes;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class DirectionsJSONParser {
	
	/* Receives a JSONObject and returns a list of lists containing latitude and longitude */
	public ArrayList<ArrayList<HashMap<String,String>>> parse(JSONObject jObject){

		ArrayList<ArrayList<HashMap<String, String>>> routes = new ArrayList<>() ;
		JSONArray jRoutes;
		JSONArray jLegs;
		JSONArray jSteps;
		JSONObject jDistance;
		JSONObject jDuration;
		
		try {			
			
			jRoutes = jObject.getJSONArray("routes");
			
			/* Traversing all routes */
			for(int i=0;i<jRoutes.length();i++){			
				jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");

				ArrayList<HashMap<String, String>> path = new ArrayList<>();

				/* Traversing all legs */
				for(int j=0;j<jLegs.length();j++){
					
					/* Getting distance from the json data */
					jDistance = ((JSONObject) jLegs.get(j)).getJSONObject("distance");
					HashMap<String, String> hmDistance = new HashMap<>();
					hmDistance.put("distance", jDistance.getString("text"));
					
					/* Getting duration from the json data */
					jDuration = ((JSONObject) jLegs.get(j)).getJSONObject("duration");
					HashMap<String, String> hmDuration = new HashMap<>();
					hmDuration.put("duration", jDuration.getString("text"));
					
					/* Adding distance object to the path */
					path.add(hmDistance);
					
					/* Adding duration object to the path */
					path.add(hmDuration);					
					
					
					jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");
					
					/* Traversing all steps */
					for(int k=0;k<jSteps.length();k++){
						String polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
						ArrayList<LatLng> list = decodePoly(polyline);
						
						/* Traversing all points */
						for(int l=0;l<list.size();l++){
							HashMap<String, String> hm = new HashMap<>();
							hm.put("lat", Double.toString(list.get(l).latitude) );
							hm.put("lng", Double.toString(list.get(l).longitude) );
							path.add(hm);						
						}								
					}					
				}
				routes.add(path);
			}
			
		} catch (JSONException e) {			
			e.printStackTrace();
		} catch (Exception ignored){
		}
		
		return routes;
	}

	public String parseTotalTime(JSONObject jObject){

		JSONArray jRoutes;
		JSONArray jLegs;
		JSONObject jDuration;
		String total_time = null;
		try {

			jRoutes = jObject.getJSONArray("routes");

			/* Traversing all routes */
			for(int i=0;i<jRoutes.length();i++){
				jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");

				/* Traversing all legs */
				for(int j=0;j<jLegs.length();j++){
					jDuration = ((JSONObject) jLegs.get(j)).getJSONObject("duration");
					total_time = jDuration.getString("text");
					if(total_time != null) {
						break;
					}
				}
				if(total_time != null) {
					break;
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}catch (Exception ignored){
		}

		return total_time;
	}

	/*
	 * Method to decode polyline points 
	 * Courtesy : jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java 
	 * */
    private ArrayList<LatLng> decodePoly(String encoded) {

		ArrayList<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}