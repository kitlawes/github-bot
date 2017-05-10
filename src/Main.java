import org.json.JSONArray;
import org.json.JSONObject;
import sun.misc.BASE64Encoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class Main {

    static String REPOSITORY = "kitlawes/github-bot";
    static String GITHUB_BOT_USERNAME = "kitlawes";
    static String GITHUB_BOT_PASSWORD = "";

    public static void main(String[] args) {

        try {
            JSONArray jsonArray = readWithGitHubAPI((new URL("https://api.github.com/repos/" + REPOSITORY + "/issues")));
            for (int i = 0; i < jsonArray.length(); i++) {
                if (jsonArray.getJSONObject(i).has("pull_request")) {
                    int commandCount = 0;
                    String issueBody = jsonArray.getJSONObject(i).getString("body");
                    if (issueBody.contains("@bot say-hello")) {
                        commandCount++;
                    }
                    String commentsURL = jsonArray.getJSONObject(i).getString("comments_url");
                    JSONArray commentsJSONArray = readWithGitHubAPI(new URL(commentsURL));
                    for (int j = 0; j < commentsJSONArray.length(); j++) {
                        String commentUserLogin = commentsJSONArray.getJSONObject(j).getJSONObject("user").getString("login");
                        String commentBody = commentsJSONArray.getJSONObject(j).getString("body");
                        if (commentBody.contains("@bot say-hello")) {
                            commandCount++;
                        }
                        if (commentUserLogin.equals(GITHUB_BOT_USERNAME) && commentBody.equals("hello world")) {
                            commandCount--;
                        }
                    }
                    String issueNumber = jsonArray.getJSONObject(i).getString("number");
                    for (int j = 0; j < commandCount; j++) {
                        writeWithGitHubAPI(new URL("https://api.github.com/repos/" + REPOSITORY + "/issues/" + issueNumber + "/comments"), "{\"body\":\"hello world\"}");
                    }
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    public static JSONArray readWithGitHubAPI(URL url) {
        JSONArray jsonArray = null;
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
            JSONObject jsonObject = new JSONObject("{\"array\":" + stringBuilder.toString() + "}");
            jsonArray = jsonObject.getJSONArray("array");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    public static JSONArray writeWithGitHubAPI(URL url, String body) {
        JSONArray jsonArray = null;
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            String encoding = new BASE64Encoder().encode((GITHUB_BOT_USERNAME + ":" + GITHUB_BOT_PASSWORD).getBytes());
            httpURLConnection.setRequestProperty("Authorization", "Basic " + encoding);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.getOutputStream().write(body.getBytes("UTF8"));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            StringBuffer stringBuilder = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();
            JSONObject jsonObject = new JSONObject("{\"array\":" + stringBuilder.toString() + "}");
            jsonArray = jsonObject.getJSONArray("array");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

}
