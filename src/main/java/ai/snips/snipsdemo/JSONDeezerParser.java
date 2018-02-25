package ai.snips.snipsdemo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
/**
 * Created by Taxicolor on 25/02/2018.
 */

public class JSONDeezerParser {

    public static String parseDeezerAPIResponse(String s) {

        String id = "";
        try {
            JSONObject obj = new JSONObject(s);

            id = obj.getJSONObject("data").getString("artist");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return id;
    }
}
