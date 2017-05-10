import org.json.JSONArray;
import org.json.JSONObject;
import sun.misc.BASE64Encoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GitHubBot {

    String repositoryOwnerUsername;
    String repositoryName;
    String gitHubBotUsername;
    String gitHubBotPassword;

    GitHubBot(String repositoryOwnerUsername,
              String repositoryName,
              String gitHubBotUsername,
              String gitHubBotPassword) {
        this.repositoryOwnerUsername = repositoryOwnerUsername;
        this.repositoryName = repositoryName;
        this.gitHubBotUsername = gitHubBotUsername;
        this.gitHubBotPassword = gitHubBotPassword;
    }

    public static void main(String[] args) {

        try {

            String repositoryOwnerUsername = "kitlawes";
            String repositoryName = "github-bot";
            String gitHubBotUsername = "kitlawes";
            String gitHubBotPassword = "";
            GitHubBot gitHubBot = new GitHubBot(
                    repositoryOwnerUsername,
                    repositoryName,
                    gitHubBotUsername,
                    gitHubBotPassword);

            JSONArray issuesJSONArray = gitHubBot.getIssuesForRepository();
            // The issues of a repository include the pull requests of the repository.
            JSONArray pullRequestsJSONArray = gitHubBot.getPullRequestsFromIssues(issuesJSONArray);
            for (int i = 0; i < pullRequestsJSONArray.length(); i++) {
                JSONObject pullRequestJSONObject = pullRequestsJSONArray.getJSONObject(i);
                int countForCommentsRemainingToRespondTo = gitHubBot.getCountForCommentsRemainingToRespondTo(pullRequestJSONObject);
                for (int j = 0; j < countForCommentsRemainingToRespondTo; j++) {
                    gitHubBot.respondWithCommentOnPullRequest(pullRequestJSONObject);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public JSONArray getIssuesForRepository() throws IOException {
        return readWithGitHubAPI((new URL("https://api.github.com/repos/" + repositoryOwnerUsername + "/" + repositoryName + "/issues")));
    }

    public JSONArray getPullRequestsFromIssues(JSONArray issuesJSONArray) throws MalformedURLException {
        JSONArray pullRequestsJSONArray = new JSONArray();
        for (int i = 0; i < issuesJSONArray.length(); i++) {
            JSONObject issueJSONObject = issuesJSONArray.getJSONObject(i);
            if (issueJSONObject.has("pull_request")) {
                pullRequestsJSONArray.put(issueJSONObject);
            }
        }
        return pullRequestsJSONArray;
    }

    public int getCountForCommentsRemainingToRespondTo(JSONObject pullRequestJSONObject) throws IOException {
        int countForCommentsRemainingToRespondTo = 0;
        // Initial comment
        String issueBody = pullRequestJSONObject.getString("body");
        if (issueBody.contains("@bot say-hello")) {
            countForCommentsRemainingToRespondTo++;
        }
        String commentsURL = pullRequestJSONObject.getString("comments_url");
        JSONArray commentsJSONArray = readWithGitHubAPI(new URL(commentsURL));
        // Subsequent comments
        for (int j = 0; j < commentsJSONArray.length(); j++) {
            String commentBody = commentsJSONArray.getJSONObject(j).getString("body");
            if (commentBody.contains("@bot say-hello")) {
                countForCommentsRemainingToRespondTo++;
            }
            String commentUserLogin = commentsJSONArray.getJSONObject(j).getJSONObject("user").getString("login");
            if (commentUserLogin.equals(gitHubBotUsername) && commentBody.equals("hello world")) {
                // Comment is a response from the bot, which means one less response is necessary
                countForCommentsRemainingToRespondTo--;
            }
        }
        return countForCommentsRemainingToRespondTo;
    }

    public JSONObject respondWithCommentOnPullRequest(JSONObject pullRequestJSONObject) throws IOException {
        int issueNumber = pullRequestJSONObject.getInt("number");
        return writeWithGitHubAPI(
                new URL("https://api.github.com/repos/" + repositoryOwnerUsername + "/" + repositoryName + "/issues/" + issueNumber + "/comments"),
                "{\"body\":\"hello world\"}"
        );
    }

    public JSONArray readWithGitHubAPI(URL url) throws IOException {
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
        return jsonObject.getJSONArray("array");
    }

    public JSONObject writeWithGitHubAPI(URL url, String body) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        String encoding = new BASE64Encoder().encode((gitHubBotUsername + ":" + gitHubBotPassword).getBytes());
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
        return new JSONObject(stringBuilder.toString());
    }

}
