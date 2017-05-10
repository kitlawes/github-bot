import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Main {
    public static void main(String[] args) {

        JSONObject jsonObject = null;
        try {
            jsonObject = queryGitHubAPI(new URL("https://api.github.com/repos/kitlawes/github-bot/issues"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        JSONArray jsonArray = jsonObject.getJSONArray("issues");
        for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.getJSONObject(i).has("pull_request")) {
                System.out.println(jsonArray.getJSONObject(i).getString("url"));
            }
        }

    }

    public static JSONObject queryGitHubAPI(URL url) {
        JSONObject jsonObject = null;
        try {
            StringBuilder stringBuilder = new StringBuilder();
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();
            jsonObject = new JSONObject("{\"issues\":" + stringBuilder.toString() + "}");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
