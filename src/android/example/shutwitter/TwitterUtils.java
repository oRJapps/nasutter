package android.example.shutwitter;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;



public class TwitterUtils {

    private static final String TOKEN = "token";
    private static final String TOKEN_SECRET = "token_secret";
    private static final String PREF_NAME = "twitter_access_token";

    /**
     * Twitter繧､繝ｳ繧ｹ繧ｿ繝ｳ繧ｹ繧貞叙蠕励＠縺ｾ縺吶�ゅい繧ｯ繧ｻ繧ｹ繝医�ｼ繧ｯ繝ｳ縺御ｿ晏ｭ倥＆繧後※縺�繧後�ｰ閾ｪ蜍慕噪縺ｫ繧ｻ繝�繝医＠縺ｾ縺吶��
     * 
     * @param context
     * @return
     */
    public static Twitter getTwitterInstance(Context context) {
        String consumerKey = context.getString(R.string.twitter_consumer_key);
        String consumerSecret = context.getString(R.string.twitter_consumer_secret);

        TwitterFactory factory = new TwitterFactory();
        Twitter twitter = factory.getInstance();
        twitter.setOAuthConsumer(consumerKey, consumerSecret);

        if (hasAccessToken(context)) {
            twitter.setOAuthAccessToken(loadAccessToken(context));
        }
        return twitter;
    }

    /**
     * 繧｢繧ｯ繧ｻ繧ｹ繝医�ｼ繧ｯ繝ｳ繧偵�励Μ繝輔ぃ繝ｬ繝ｳ繧ｹ縺ｫ菫晏ｭ倥＠縺ｾ縺吶��
     * 
     * @param context
     * @param accessToken
     */
    public static void storeAccessToken(Context context, AccessToken accessToken) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString(TOKEN, accessToken.getToken());
        editor.putString(TOKEN_SECRET, accessToken.getTokenSecret());
        editor.commit();
    }

    /**
     * 繧｢繧ｯ繧ｻ繧ｹ繝医�ｼ繧ｯ繝ｳ繧偵�励Μ繝輔ぃ繝ｬ繝ｳ繧ｹ縺九ｉ隱ｭ縺ｿ霎ｼ縺ｿ縺ｾ縺吶��
     * 
     * @param context
     * @return
     */
    public static AccessToken loadAccessToken(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
        String token = preferences.getString(TOKEN, null);
        String tokenSecret = preferences.getString(TOKEN_SECRET, null);
        if (token != null && tokenSecret != null) {
            return new AccessToken(token, tokenSecret);
        } else {
            return null;
        }
    }

    /**
     * 繧｢繧ｯ繧ｻ繧ｹ繝医�ｼ繧ｯ繝ｳ縺悟ｭ伜惠縺吶ｋ蝣ｴ蜷医�ｯtrue繧定ｿ斐＠縺ｾ縺吶��
     * 
     * @return
     */
    public static boolean hasAccessToken(Context context) {
        return loadAccessToken(context) != null;
    }
}
