import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

public class GitHubBotTest {

    GitHubBot gitHubBot = null;

    @Before
    public void setup() {
        String repositoryOwnerUsername = "owner";
        String repositoryName = "repository";
        String gitHubBotUsername = "githubbot";
        String gitHubBotPassword = "password";
        gitHubBot = new GitHubBot(
                repositoryOwnerUsername,
                repositoryName,
                gitHubBotUsername,
                gitHubBotPassword);
    }

    @Test
    public void testRespondToCommentsOnPullRequests() {
        try {

            JSONArray expectedIssuesJSONArray = new JSONArray();
            JSONArray expectedPullRequestJSONArray = new JSONArray();
            JSONObject pullRequestJSONObject = new JSONObject();
            expectedPullRequestJSONArray.put(pullRequestJSONObject);
            pullRequestJSONObject = new JSONObject();
            expectedPullRequestJSONArray.put(pullRequestJSONObject);
            int expectedCountForCommentsRemainingToRespondTo = 2;

            GitHubBot gitHubBot = Mockito.spy(this.gitHubBot);
            Mockito.when(gitHubBot.getWithGitHubAPI(any(URL.class))).thenAnswer(new Answer<JSONArray>() {
                @Override
                public JSONArray answer(InvocationOnMock invocation) throws Throwable {
                    return null;
                }
            });
            Mockito.when(gitHubBot.postWithGitHubAPI(any(URL.class), anyString())).thenAnswer(new Answer<JSONObject>() {
                @Override
                public JSONObject answer(InvocationOnMock invocation) throws Throwable {
                    return null;
                }
            });
            Mockito.when(gitHubBot.getIssuesForRepository()).thenAnswer(new Answer<JSONArray>() {
                @Override
                public JSONArray answer(InvocationOnMock invocation) throws Throwable {
                    return expectedIssuesJSONArray;
                }
            });
            Mockito.when(gitHubBot.getPullRequestsFromIssues(any(JSONArray.class))).thenAnswer(new Answer<JSONArray>() {
                @Override
                public JSONArray answer(InvocationOnMock invocation) throws Throwable {
                    return expectedPullRequestJSONArray;
                }
            });
            Mockito.when(gitHubBot.getCountForCommentsRemainingToRespondTo(any(JSONObject.class))).thenAnswer(new Answer<Integer>() {
                @Override
                public Integer answer(InvocationOnMock invocation) throws Throwable {
                    return expectedCountForCommentsRemainingToRespondTo;
                }
            });
            Mockito.when(gitHubBot.respondWithCommentOnPullRequest(any(JSONObject.class))).thenAnswer(new Answer<JSONObject>() {
                @Override
                public JSONObject answer(InvocationOnMock invocation) throws Throwable {
                    return null;
                }
            });

            gitHubBot.respondToCommentsOnPullRequests();

            // Test that getIssuesForRepository is called once (plus another time when mocked)
            Mockito.verify(gitHubBot, Mockito.times(2)).getIssuesForRepository();

            ArgumentCaptor<JSONArray> jsonArrayAgument = ArgumentCaptor.forClass(JSONArray.class);
            Mockito.verify(gitHubBot).getPullRequestsFromIssues(jsonArrayAgument.capture());
            // Test that getPullRequestsFromIssues is called once with the correct argument
            assertEquals(expectedIssuesJSONArray, jsonArrayAgument.getValue());

            ArgumentCaptor<JSONObject> jsonObjectAgument = ArgumentCaptor.forClass(JSONObject.class);
            Mockito.verify(gitHubBot, Mockito.times(expectedPullRequestJSONArray.length())).getCountForCommentsRemainingToRespondTo(jsonObjectAgument.capture());
            // Test that getCountForCommentsRemainingToRespondTo is called twice with the correct arguments
            assertEquals(expectedPullRequestJSONArray.getJSONObject(0), jsonObjectAgument.getAllValues().get(0));
            assertEquals(expectedPullRequestJSONArray.getJSONObject(1), jsonObjectAgument.getAllValues().get(1));

            jsonObjectAgument = ArgumentCaptor.forClass(JSONObject.class);
            int mockitoTimes = expectedPullRequestJSONArray.length() * expectedCountForCommentsRemainingToRespondTo;
            Mockito.verify(gitHubBot, Mockito.times(mockitoTimes)).respondWithCommentOnPullRequest(jsonObjectAgument.capture());
            // Test that respondWithCommentOnPullRequest is called four times with the correct arguments
            assertEquals(expectedPullRequestJSONArray.getJSONObject(0), jsonObjectAgument.getAllValues().get(0));
            assertEquals(expectedPullRequestJSONArray.getJSONObject(0), jsonObjectAgument.getAllValues().get(1));
            assertEquals(expectedPullRequestJSONArray.getJSONObject(1), jsonObjectAgument.getAllValues().get(2));
            assertEquals(expectedPullRequestJSONArray.getJSONObject(1), jsonObjectAgument.getAllValues().get(3));

        } catch (IOException ioException) {
            fail();
            ioException.printStackTrace();
        }
    }

    @Test
    public void testGetIssuesForRepository() {
        try {

            JSONArray expectedIssuesJSONArray = new JSONArray();

            GitHubBot gitHubBot = Mockito.spy(this.gitHubBot);
            Mockito.when(gitHubBot.getWithGitHubAPI(any(URL.class))).thenAnswer(new Answer<JSONArray>() {
                @Override
                public JSONArray answer(InvocationOnMock invocation) throws Throwable {
                    return expectedIssuesJSONArray;
                }
            });

            String testRepositoryOwnerUsername = "testrepositoryownerusername";
            gitHubBot.repositoryOwnerUsername = testRepositoryOwnerUsername;
            String testRepositoryName = "testrepositoryname";
            gitHubBot.repositoryName = testRepositoryName;
            JSONArray actualIssuesJSONArray = gitHubBot.getIssuesForRepository();
            // Test that getIssuesForRepository returns the issues JSONArray it receives from getWithGitHubAPI
            assertEquals(expectedIssuesJSONArray, actualIssuesJSONArray);

            ArgumentCaptor<URL> urlAgument = ArgumentCaptor.forClass(URL.class);
            Mockito.verify(gitHubBot).getWithGitHubAPI(urlAgument.capture());
            // Test that getIssuesForRepository calls getWithGitHubAPI with the correct argument
            assertEquals(new URL("https://api.github.com/repos/" + testRepositoryOwnerUsername + "/" + testRepositoryName + "/issues"), urlAgument.getValue());

        } catch (IOException ioException) {
            fail();
            ioException.printStackTrace();
        }
    }

    @Test
    public void testGetPullRequestsFromIssues() {
        try {

            JSONArray issuesJSONArray = new JSONArray();
            JSONArray expectedPullRequestsJSONArray = new JSONArray();
            JSONObject pullRequestJSONObject = new JSONObject();
            pullRequestJSONObject.put("pull_request", new JSONObject());
            issuesJSONArray.put(pullRequestJSONObject);
            expectedPullRequestsJSONArray.put(pullRequestJSONObject);
            JSONObject issueJSONObject = new JSONObject();
            issuesJSONArray.put(issueJSONObject);
            pullRequestJSONObject = new JSONObject();
            pullRequestJSONObject.put("pull_request", new JSONObject());
            issuesJSONArray.put(pullRequestJSONObject);
            expectedPullRequestsJSONArray.put(pullRequestJSONObject);

            // Test that getPullRequestsFromIssues returns a JSONArray containing only the pull requests
            JSONArray actualPullRequestsJSONArray = gitHubBot.getPullRequestsFromIssues(issuesJSONArray);
            assertEquals(expectedPullRequestsJSONArray.length(), actualPullRequestsJSONArray.length());
            for (int i = 0; i < expectedPullRequestsJSONArray.length(); i++) {
                assertEquals(expectedPullRequestsJSONArray.getJSONObject(i), actualPullRequestsJSONArray.getJSONObject(i));
            }

        } catch (MalformedURLException malformedURLException) {
            fail();
            malformedURLException.printStackTrace();
        }
    }

    @Test
    public void testGetCountForCommentsRemainingToRespondTo() {
        try {

            String testCommentsURL = "https://api.github.com/repos/username/repository/issues/50/comments";
            String testGitHubBotUsername = "testgithubbotusername";

            GitHubBot gitHubBot = Mockito.spy(this.gitHubBot);
            Mockito.when(gitHubBot.getWithGitHubAPI(any(URL.class))).thenAnswer(new Answer<JSONArray>() {
                @Override
                public JSONArray answer(InvocationOnMock invocation) throws Throwable {
                    if (invocation.getArguments()[0].equals(new URL(testCommentsURL))) {
                        JSONArray jsonArray = new JSONArray();

                        // Test that the "@bot say-hello" command is detected in a subsequent comment of the pull
                        // request
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("body", "test body @bot say-hello test body");
                        JSONObject userJSONObject = new JSONObject();
                        userJSONObject.put("login", "testlogin");
                        jsonObject.put("user", userJSONObject);
                        jsonArray.put(jsonObject);

                        // Test that the "@bot say-hello" command is detected in multiple comments of the pull request
                        jsonObject = new JSONObject();
                        jsonObject.put("body", "test body @bot say-hello test body");
                        userJSONObject = new JSONObject();
                        userJSONObject.put("login", "testlogin");
                        jsonObject.put("user", userJSONObject);
                        jsonArray.put(jsonObject);

                        // Test that a comment without the "@bot say-hello" command is not detected
                        jsonObject = new JSONObject();
                        jsonObject.put("body", "test body");
                        userJSONObject = new JSONObject();
                        userJSONObject.put("login", "testlogin");
                        jsonObject.put("user", userJSONObject);
                        jsonArray.put(jsonObject);

                        // Test that a "hello world" comment from the bot is detected
                        jsonObject = new JSONObject();
                        jsonObject.put("body", "hello world");
                        userJSONObject = new JSONObject();
                        userJSONObject.put("login", testGitHubBotUsername);
                        jsonObject.put("user", userJSONObject);
                        jsonArray.put(jsonObject);

                        // Test that a "hello world" comment not from the bot is not detected
                        jsonObject = new JSONObject();
                        jsonObject.put("body", "hello world");
                        userJSONObject = new JSONObject();
                        userJSONObject.put("login", "testlogin");
                        jsonObject.put("user", userJSONObject);
                        jsonArray.put(jsonObject);

                        return jsonArray;
                    }
                    return null;
                }
            });

            gitHubBot.gitHubBotUsername = testGitHubBotUsername;
            JSONObject pullRequestJSONObject = new JSONObject();
            // Test that the "@bot say-hello" command is detected in the first comment of the pull request
            String testBody = "test body @bot say-hello test body";
            pullRequestJSONObject.put("body", testBody);
            pullRequestJSONObject.put("comments_url", testCommentsURL);
            int actualCountForCommentsRemainingToRespondTo = gitHubBot.getCountForCommentsRemainingToRespondTo(pullRequestJSONObject);
            assertEquals(2, actualCountForCommentsRemainingToRespondTo);

            ArgumentCaptor<URL> urlAgument = ArgumentCaptor.forClass(URL.class);
            Mockito.verify(gitHubBot).getWithGitHubAPI(urlAgument.capture());
            // Test that getCountForCommentsRemainingToRespondTo calls getWithGitHubAPI with the correct argument
            assertEquals(new URL(testCommentsURL), urlAgument.getValue());

        } catch (IOException ioException) {
            fail();
            ioException.printStackTrace();
        }
    }

    @Test
    public void testRespondWithCommentOnPullRequest() {
        try {

            GitHubBot gitHubBot = Mockito.spy(this.gitHubBot);
            Mockito.when(gitHubBot.postWithGitHubAPI(any(URL.class), anyString())).thenAnswer(new Answer<JSONObject>() {
                @Override
                public JSONObject answer(InvocationOnMock invocation) throws Throwable {
                    return null;
                }
            });

            String testRepositoryOwnerUsername = "testrepositoryownerusername";
            gitHubBot.repositoryOwnerUsername = testRepositoryOwnerUsername;
            String testRepositoryName = "testrepositoryname";
            gitHubBot.repositoryName = testRepositoryName;
            JSONObject pullRequestJSONObject = new JSONObject();
            int testNumber = 50;
            pullRequestJSONObject.put("number", testNumber);
            gitHubBot.respondWithCommentOnPullRequest(pullRequestJSONObject);

            ArgumentCaptor<URL> urlAgument = ArgumentCaptor.forClass(URL.class);
            ArgumentCaptor<String> stringArgument = ArgumentCaptor.forClass(String.class);
            Mockito.verify(gitHubBot).postWithGitHubAPI(urlAgument.capture(), stringArgument.capture());
            // Test that respondWithCommentOnPullRequest calls postWithGitHubAPI with the correct arguments
            assertEquals(new URL("https://api.github.com/repos/" + testRepositoryOwnerUsername + "/" + testRepositoryName + "/issues/" + testNumber + "/comments"), urlAgument.getValue());
            assertEquals("{\"body\":\"hello world\"}", stringArgument.getValue());

        } catch (IOException ioException) {
            fail();
            ioException.printStackTrace();
        }
    }

}