import static org.junit.Assert.assertEquals;

import org.json.JSONArray;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class GitHubBotTest {

    @Test
    public void testQueryGitHubAPI() {
        try {
            JSONArray jsonArray = GitHubBot.readWithGitHubAPI(new URL("https://api.github.com/repos/kitlawes/github-bot/issues"));
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