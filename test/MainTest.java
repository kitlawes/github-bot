import static org.junit.Assert.assertEquals;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class MainTest {

    @Test
    public void testQueryGitHubAPI() {
        try {
            JSONObject jsonObject = Main.queryGitHubAPI(new URL("https://api.github.com/repos/kitlawes/github-bot/issues"));
            JSONArray jsonArray = jsonObject.getJSONArray("issues");
            assertEquals(2, jsonArray.length());
            assert (jsonArray.getJSONObject(0).has("pull_request"));
            assert (!jsonArray.getJSONObject(1).has("pull_request"));
            assertEquals(2, jsonArray.getJSONObject(0).getInt("comments"));
            assertEquals(2, jsonArray.getJSONObject(1).getInt("comments"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

}