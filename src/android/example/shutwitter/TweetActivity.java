package android.example.shutwitter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.bumptech.glide.Glide;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;


public class TweetActivity extends Activity {

    private EditText mInputText;
    private Twitter mTwitter;
    private Button mGazou;
    private ImageView imageView,imageView2,imageView3,imageView4;
    private Boolean gazou=false;

    int k =0;
    Uri[] multi_uri=null;
    Uri multiuri = null;
    Uri uri =null;

    InputStream in;
    long[]mediaIds = null;
    long media_id =0;

    private static final int RESULT_PICK_IMAGEFILE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_tweet);

        mTwitter = TwitterUtils.getTwitterInstance(this);
        //Twitter文字入力
        mInputText = (EditText) findViewById(R.id.input_text);

        //Twitter画像選択（ギャラリーから）
        mGazou = (Button)findViewById(R.id.button1);
        mGazou.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/*");
                gazou=true;
                startActivityForResult(intent, RESULT_PICK_IMAGEFILE);
            }
        });

        findViewById(R.id.action_tweet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tweet();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,final Intent resultData) {

        if (requestCode != RESULT_PICK_IMAGEFILE || resultCode != RESULT_OK) {
            return;
        }
        imageView=(ImageView) findViewById(R.id.imageView1);
        imageView2=(ImageView) findViewById(R.id.imageView2);
        imageView3=(ImageView) findViewById(R.id.imageView3);
        imageView4=(ImageView) findViewById(R.id.imageView4);



        ClipData clipData = resultData.getClipData();

        if(clipData!=null){
        	//画像を2枚以上投稿する場合
            List list = new ArrayList();
            float size =0.1f;
            gazou = true;
            mediaIds = new long[clipData.getItemCount()];
            for (int i = 0; i < clipData.getItemCount();  i++) {
            	ClipData.Item item = clipData.getItemAt(i);
                multiuri=item.getUri();
                list.add(multiuri);
                //Log.i("flg", uri.toString());
             }
             multi_uri =(Uri[])list.toArray(new Uri[0]);
             switch(clipData.getItemCount()){

             	case 2:
                  Glide.with(TweetActivity.this).load(multi_uri[0]).thumbnail(size).into(imageView);
                  Glide.with(TweetActivity.this).load(multi_uri[1]).thumbnail(size).into(imageView2);
                  imageView3.setVisibility(View.GONE);
                  imageView4.setVisibility(View.GONE);
                  k=2;
                  break;
             	case 3:
             	  Glide.with(TweetActivity.this).load(multi_uri[0]).thumbnail(size).into(imageView);
                  Glide.with(TweetActivity.this).load(multi_uri[1]).thumbnail(size).into(imageView2);
                  Glide.with(TweetActivity.this).load(multi_uri[2]).thumbnail(size).into(imageView3);
                  imageView4.setVisibility(View.GONE);
                  k=3;
                  break;
                case 4:
                  Glide.with(TweetActivity.this).load(multi_uri[0]).thumbnail(size).into(imageView);
                  Glide.with(TweetActivity.this).load(multi_uri[1]).thumbnail(size).into(imageView2);
                  Glide.with(TweetActivity.this).load(multi_uri[2]).thumbnail(size).into(imageView3);
                  Glide.with(TweetActivity.this).load(multi_uri[3]).thumbnail(size).into(imageView4);
                  k=4;
                  break;
                default:
                  showToast("Twitterへの画像投稿は4枚までです");
                  imageView.setVisibility(View.GONE);
                  imageView2.setVisibility(View.GONE);
                  imageView3.setVisibility(View.GONE);
                  imageView4.setVisibility(View.GONE);
                  gazou=false;
                  k=0;
                  break;

              }
                        //Log.i("Flg", multi_uri.toString());

          }else{
        	  //画像を1枚のみ投稿する場合

              uri = resultData.getData();
              Glide.with(TweetActivity.this).load(uri).thumbnail(0.1f).into(imageView);
              imageView2.setVisibility(View.GONE);
              imageView3.setVisibility(View.GONE);
              imageView4.setVisibility(View.GONE);

          }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_tweet_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.menu_not:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



   private void tweet() {
     //画像アップロード
	   if(gazou==true){
		   new AsyncTask<Void, Void, Boolean>() {
		        @Override
		        protected Boolean doInBackground(Void... params) {

		            try {
		                //mTwitterはOAuth認可済みであるとする
		                String message =mInputText.getText().toString();

		                for(int i =0;i<k;i++){
		                	Bitmap bitmap=getDownSize(multi_uri[i]);
		                	ByteArrayOutputStream stream = new ByteArrayOutputStream();
		                    bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
		                    byte[] imageInByte = stream.toByteArray();

		                    ByteArrayInputStream bis = new ByteArrayInputStream(imageInByte);
		                	mediaIds[i] = (mTwitter.uploadMedia(String.format("[filename_%d]", i + 1), bis).getMediaId());

		                }
		                if(k==0){
		                	Bitmap bitmap=getDownSize(uri);
		                	ByteArrayOutputStream stream = new ByteArrayOutputStream();
		                    bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
		                    byte[] imageInByte = stream.toByteArray();
		                    ByteArrayInputStream bis = new ByteArrayInputStream(imageInByte);
		                	mediaIds = new long[1];
		                	mediaIds[0] = (mTwitter.uploadMedia(String.format("[filename_%d]", 1),bis).getMediaId());

		                }
	                    StatusUpdate update = new StatusUpdate(message);
		                update.setMediaIds(mediaIds);
		                mTwitter.updateStatus(update);

		            } catch (TwitterException e) {
		                e.printStackTrace();
		                return false;
		            }
		            return true;
		        }
		        @Override
		        protected void onPostExecute(Boolean result) {
		            if(result) {
		                showToast("投稿に成功しました");

		            } else {
		                showToast("投稿に失敗しました");
		            }
		        }
		    }.execute();
	   }else{
		   new AsyncTask<String, Void, Boolean>(){
			   @Override
		        protected Boolean doInBackground(String... params){
				   try {
			           // 省略
			           mTwitter.updateStatus(params[0]);
			           return true;

			        } catch (TwitterException e) {
			            return false;
			        }

			   }
			   @Override
			    protected void onPostExecute(Boolean result) {
			        if(result) {
			            showToast("投稿に成功しました");
			        } else {
			            showToast("投稿に失敗しました");
			        }
			    }
		   }.execute(mInputText.getText().toString());
	   }
   }

   private Bitmap getDownSize(Uri uri){
	   InputStream inputStream;
	try {
		inputStream = getContentResolver().openInputStream(uri);
		Log.i("!",uri.toString() );
		// 画像サイズ情報を取得する
	   	BitmapFactory.Options imageOptions = new BitmapFactory.Options();
	   	imageOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
	   	imageOptions.inJustDecodeBounds = true;
	   	BitmapFactory.decodeStream(inputStream, null, imageOptions);
	   	Log.v("image", "Original Image Size: " + imageOptions.outWidth + " x " + imageOptions.outHeight);
	   	// もし、画像が大きかったら縮小して読み込む
	   	//  今回はimageSizeMaxの大きさに合わせる
	   	Bitmap bitmap=null;
	   	int imageSizeMax = 500;
	   	inputStream = getContentResolver().openInputStream(uri);
	   	float imageScaleWidth = (float)imageOptions.outWidth / imageSizeMax;
	   	float imageScaleHeight = (float)imageOptions.outHeight / imageSizeMax;

	   	// もしも、縮小できるサイズならば、縮小して読み込む
	   	if (imageScaleWidth > 2 && imageScaleHeight > 2) {
	   	    BitmapFactory.Options imageOptions2 = new BitmapFactory.Options();
	   	    imageOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;

	   	    // 縦横、小さい方に縮小するスケールを合わせる
	   	    int imageScale = (int)Math.floor((imageScaleWidth > imageScaleHeight ? imageScaleHeight : imageScaleWidth));

	   	    // inSampleSizeには2のべき上が入るべきなので、imageScaleに最も近く、かつそれ以下の2のべき上の数を探す
	   	    for (int i = 2; i <= imageScale; i *= 2) {
	   	        imageOptions2.inSampleSize = i;
	   	    }

	   	    bitmap = BitmapFactory.decodeStream(inputStream, null, imageOptions2);
	   	    Log.v("image", "Sample Size: 1/" + imageOptions2.inSampleSize);
	   	} else {
	   	    bitmap = BitmapFactory.decodeStream(inputStream);
	   	}
	   	return bitmap;

	} catch (FileNotFoundException e) {
		// TODO 自動生成された catch ブロック
		e.printStackTrace();
		return null;
	}




   }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

}