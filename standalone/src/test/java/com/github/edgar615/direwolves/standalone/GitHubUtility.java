package com.github.edgar615.direwolves.standalone;

import com.github.edgar615.util.base.EncryptUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GitHubUtility {
 
  private static final Logger LOG = LoggerFactory.getLogger(GitHubUtility.class.getName());
  private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
  private static final char[] HEX = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
  };

  private static final String payload = "{\"ref\":\"refs/heads/master\","
                                        +
                                        "\"before\":\"9f55424eb55fc3ad98853d119b712dfbf1fd7aef\","
                                        +
                                        "\"after\":\"887e342ebdd2b890bed8d64a68f8dbaf2cc9f607\","
                                        + "\"created\":false,\"deleted\":false,\"forced\":false,"
                                        + "\"base_ref\":null,\"compare\":\"https://github"
                                        + ".com/edgar615/api-router/compare/9f55424eb55f...887e342ebdd2\",\"commits\":[{\"id\":\"887e342ebdd2b890bed8d64a68f8dbaf2cc9f607\",\"tree_id\":\"fe755e0859223ab3f69097ddc8fd455bdcef198a\",\"distinct\":true,\"message\":\"test\",\"timestamp\":\"2018-02-01T17:02:43+08:00\",\"url\":\"https://github.com/edgar615/api-router/commit/887e342ebdd2b890bed8d64a68f8dbaf2cc9f607\",\"author\":{\"name\":\"edgar615\",\"email\":\"edgar615@gmail.com\",\"username\":\"edgar615\"},\"committer\":{\"name\":\"edgar615\",\"email\":\"edgar615@gmail.com\",\"username\":\"edgar615\"},\"added\":[\"conflict1.json\"],\"removed\":[],\"modified\":[]}],\"head_commit\":{\"id\":\"887e342ebdd2b890bed8d64a68f8dbaf2cc9f607\",\"tree_id\":\"fe755e0859223ab3f69097ddc8fd455bdcef198a\",\"distinct\":true,\"message\":\"test\",\"timestamp\":\"2018-02-01T17:02:43+08:00\",\"url\":\"https://github.com/edgar615/api-router/commit/887e342ebdd2b890bed8d64a68f8dbaf2cc9f607\",\"author\":{\"name\":\"edgar615\",\"email\":\"edgar615@gmail.com\",\"username\":\"edgar615\"},\"committer\":{\"name\":\"edgar615\",\"email\":\"edgar615@gmail.com\",\"username\":\"edgar615\"},\"added\":[\"conflict1.json\"],\"removed\":[],\"modified\":[]},\"repository\":{\"id\":119802578,\"name\":\"api-router\",\"full_name\":\"edgar615/api-router\",\"owner\":{\"name\":\"edgar615\",\"email\":\"67672283@qq.com\",\"login\":\"edgar615\",\"id\":1721765,\"avatar_url\":\"https://avatars1.githubusercontent.com/u/1721765?v=4\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/edgar615\",\"html_url\":\"https://github.com/edgar615\",\"followers_url\":\"https://api.github.com/users/edgar615/followers\",\"following_url\":\"https://api.github.com/users/edgar615/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/edgar615/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/edgar615/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/edgar615/subscriptions\",\"organizations_url\":\"https://api.github.com/users/edgar615/orgs\",\"repos_url\":\"https://api.github.com/users/edgar615/repos\",\"events_url\":\"https://api.github.com/users/edgar615/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/edgar615/received_events\",\"type\":\"User\",\"site_admin\":false},\"private\":false,\"html_url\":\"https://github.com/edgar615/api-router\",\"description\":null,\"fork\":false,\"url\":\"https://github.com/edgar615/api-router\",\"forks_url\":\"https://api.github.com/repos/edgar615/api-router/forks\",\"keys_url\":\"https://api.github.com/repos/edgar615/api-router/keys{/key_id}\",\"collaborators_url\":\"https://api.github.com/repos/edgar615/api-router/collaborators{/collaborator}\",\"teams_url\":\"https://api.github.com/repos/edgar615/api-router/teams\",\"hooks_url\":\"https://api.github.com/repos/edgar615/api-router/hooks\",\"issue_events_url\":\"https://api.github.com/repos/edgar615/api-router/issues/events{/number}\",\"events_url\":\"https://api.github.com/repos/edgar615/api-router/events\",\"assignees_url\":\"https://api.github.com/repos/edgar615/api-router/assignees{/user}\",\"branches_url\":\"https://api.github.com/repos/edgar615/api-router/branches{/branch}\",\"tags_url\":\"https://api.github.com/repos/edgar615/api-router/tags\",\"blobs_url\":\"https://api.github.com/repos/edgar615/api-router/git/blobs{/sha}\",\"git_tags_url\":\"https://api.github.com/repos/edgar615/api-router/git/tags{/sha}\",\"git_refs_url\":\"https://api.github.com/repos/edgar615/api-router/git/refs{/sha}\",\"trees_url\":\"https://api.github.com/repos/edgar615/api-router/git/trees{/sha}\",\"statuses_url\":\"https://api.github.com/repos/edgar615/api-router/statuses/{sha}\",\"languages_url\":\"https://api.github.com/repos/edgar615/api-router/languages\",\"stargazers_url\":\"https://api.github.com/repos/edgar615/api-router/stargazers\",\"contributors_url\":\"https://api.github.com/repos/edgar615/api-router/contributors\",\"subscribers_url\":\"https://api.github.com/repos/edgar615/api-router/subscribers\",\"subscription_url\":\"https://api.github.com/repos/edgar615/api-router/subscription\",\"commits_url\":\"https://api.github.com/repos/edgar615/api-router/commits{/sha}\",\"git_commits_url\":\"https://api.github.com/repos/edgar615/api-router/git/commits{/sha}\",\"comments_url\":\"https://api.github.com/repos/edgar615/api-router/comments{/number}\",\"issue_comment_url\":\"https://api.github.com/repos/edgar615/api-router/issues/comments{/number}\",\"contents_url\":\"https://api.github.com/repos/edgar615/api-router/contents/{+path}\",\"compare_url\":\"https://api.github.com/repos/edgar615/api-router/compare/{base}...{head}\",\"merges_url\":\"https://api.github.com/repos/edgar615/api-router/merges\",\"archive_url\":\"https://api.github.com/repos/edgar615/api-router/{archive_format}{/ref}\",\"downloads_url\":\"https://api.github.com/repos/edgar615/api-router/downloads\",\"issues_url\":\"https://api.github.com/repos/edgar615/api-router/issues{/number}\",\"pulls_url\":\"https://api.github.com/repos/edgar615/api-router/pulls{/number}\",\"milestones_url\":\"https://api.github.com/repos/edgar615/api-router/milestones{/number}\",\"notifications_url\":\"https://api.github.com/repos/edgar615/api-router/notifications{?since,all,participating}\",\"labels_url\":\"https://api.github.com/repos/edgar615/api-router/labels{/name}\",\"releases_url\":\"https://api.github.com/repos/edgar615/api-router/releases{/id}\",\"deployments_url\":\"https://api.github.com/repos/edgar615/api-router/deployments\",\"created_at\":1517471728,\"updated_at\":\"2018-02-01T07:55:28Z\",\"pushed_at\":1517475779,\"git_url\":\"git://github.com/edgar615/api-router.git\",\"ssh_url\":\"git@github.com:edgar615/api-router.git\",\"clone_url\":\"https://github.com/edgar615/api-router.git\",\"svn_url\":\"https://github.com/edgar615/api-router\",\"homepage\":null,\"size\":1,\"stargazers_count\":0,\"watchers_count\":0,\"language\":null,\"has_issues\":true,\"has_projects\":true,\"has_downloads\":true,\"has_wiki\":true,\"has_pages\":false,\"forks_count\":0,\"mirror_url\":null,\"archived\":false,\"open_issues_count\":0,\"license\":null,\"forks\":0,\"open_issues\":0,\"watchers\":0,\"default_branch\":\"master\",\"stargazers\":0,\"master_branch\":\"master\"},\"pusher\":{\"name\":\"edgar615\",\"email\":\"67672283@qq.com\"},\"sender\":{\"login\":\"edgar615\",\"id\":1721765,\"avatar_url\":\"https://avatars1.githubusercontent.com/u/1721765?v=4\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/edgar615\",\"html_url\":\"https://github.com/edgar615\",\"followers_url\":\"https://api.github.com/users/edgar615/followers\",\"following_url\":\"https://api.github.com/users/edgar615/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/edgar615/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/edgar615/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/edgar615/subscriptions\",\"organizations_url\":\"https://api.github.com/users/edgar615/orgs\",\"repos_url\":\"https://api.github.com/users/edgar615/repos\",\"events_url\":\"https://api.github.com/users/edgar615/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/edgar615/received_events\",\"type\":\"User\",\"site_admin\":false}}";
  public static void main(String[] args) {
    System.out.println(verifySignature(payload, "sha1=a15a8a1c0d6df2332d8fe4a8cf777743b3e4ea0f", "123456"));
  }
 
  public static boolean verifySignature(String payload, String signature, String secret) {
    boolean isValid = false;
 
    try {
      String actual = EncryptUtils.encryptHmacSha1(payload, secret);
      String excepted = signature.substring(5);
      System.out.println(actual);
      System.out.println(excepted);
      isValid = excepted.equalsIgnoreCase(actual);
 
    } catch (IOException ex) {
 
      ex.printStackTrace();
 
    }

    return isValid;
  }
 
  private static char[] encode(byte[] bytes) {
    final int amount = bytes.length;
    char[] result = new char[2 * amount];
 
    int j = 0;
    for (int i = 0; i < amount; i++) {
      result[j++] = HEX[(0xF0 & bytes[i]) >>> 4];
      result[j++] = HEX[(0x0F & bytes[i])];
    }
 
    return result;
  }
 
}