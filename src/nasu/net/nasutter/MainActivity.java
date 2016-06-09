package nasu.net.nasutter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import twitter4j.MediaEntity;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UserMentionEntity;


public class MainActivity extends ListActivity {

	private TweetAdapter mAdapter;
	private Twitter mTwitter;
	private long rt_id=0;
	private static final int RATE_LIMITED_STATUS_CODE = 400;
	private static final int COUNT_MAX =20;
	private static ProgressDialog waitDialog;
	private View mFooter;

	AbsListView.OnScrollListener mOnScrollListner = new AbsListView.OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (getListView().getLastVisiblePosition() == (mAdapter.getCount() - 1)){
				moreLoadTimeLine();
				Log.v("lv","II:通過しました。");
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
							 int visibleItemCount, int totalItemCount) {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		if (!TwitterUtils.hasAccessToken(this)) {
			Intent intent = new Intent(this, TwitterOAuthActivity.class);
			startActivity(intent);
			finish();
		} else {

			mTwitter = TwitterUtils.getTwitterInstance(this);
			mAdapter = new TweetAdapter(this);
			setListAdapter(mAdapter);

			ListView lv = getListView();
			// ListViewが最下層に来たら動的にタイムラインを20件取得
			lv.addFooterView(getFooter());
			Log.v("getFooter","通過しました。");

			lv.setOnScrollListener(mOnScrollListner);

			Log.v("lv","I:通過しました。");
			lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, final View view,
										final int position, final long id) {
					Log.v("lv","通過しました。");
					//Tweetクリックメニュー
					final Status item = (Status) parent.getItemAtPosition(position);
					//username
					final String user ="@"+ item.getUser().getScreenName() ;
					//公式RT
					rt_id =item.getId();

					//showToast(String.valueOf(rt_id));
					//popup-menu
					final CharSequence[] items = {"返信","全員へ返信", "公式RT","このtweetをお気に入り登録する"};

					final AlertDialog.Builder listDlg = new AlertDialog.Builder(view.getContext());
					listDlg.setTitle(user+"さんへどうする？");
					listDlg.setNegativeButton("閉じる",null);
					listDlg.setItems(
							items,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(final DialogInterface dialog, int which) {

									LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
									View view = inflater.inflate(R.layout.new_tweet, null);
									Button btn =(Button)view.findViewById(R.id.action_tweet);

									final EditText editText = (EditText)view.findViewById(R.id.input_text);
									dialog.dismiss();
									//switch-case
									switch(which){
										case 0:
											//showToast((String) items[0]);

											editText.setText(user +" ");

											editText.setSelection(user.length()+1);



											btn.setOnClickListener(new View.OnClickListener() {
												public void onClick(View v) {
													if(dialog != null){
														dialog.dismiss();
													}

													AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>() {
														@Override
														protected Boolean doInBackground(String... params) {

															try {
																// �ԐM
																mTwitter.updateStatus(params[0]);

																return true;
															} catch (TwitterException e) {
																e.printStackTrace();
																return false;
															}
														}

														@Override
														protected void onPostExecute(Boolean result) {
															dialog.dismiss();
															if (result) {
																showToast("返信しました");
																editText.setText("");
															} else {

																showToast("返信できませんでした");

															}
														}
													};
													task.execute(editText.getText().toString());
												}


											});

											new AlertDialog.Builder(MainActivity.this)
													.setTitle(user+"さんへ"+(String) items[0])
													.setView(view)
													.setPositiveButton( "閉じる", new DialogInterface.OnClickListener() {
														@Override
														public void onClick(DialogInterface dialog, int which) {
															dialog.dismiss();
														} }) .show();

											break;
										case 1:
											//�S���֕ԐM

											UserMentionEntity[] ume = item.getUserMentionEntities();
											if(ume != null){

												List<String> reply = new ArrayList<String>();
												StringBuilder sb = new StringBuilder();
												for(int i=0;i<ume.length;i++){
													UserMentionEntity replyall=ume[i];
													reply.add("@"+replyall.getScreenName());
													sb.append("@"+replyall.getScreenName()+" ");

												}


												String[] replies = ( String[] )reply.toArray( new String[0] );
												String userall=user+" "+ new String(sb);
												editText.setText(user+" "+ new String(sb));
												editText.setSelection(userall.length());
												//showToast(String.valueOf(userall.length())+"����");

											}

											btn.setOnClickListener(new View.OnClickListener() {
												public void onClick(View v) {
													if(dialog != null){
														dialog.dismiss();
													}



													AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>() {
														@Override
														protected Boolean doInBackground(String... params) {

															try {
																// �ԐM
																mTwitter.updateStatus(params[0]);


																return true;
															} catch (TwitterException e) {
																e.printStackTrace();
																return false;
															}
														}

														@Override
														protected void onPostExecute(Boolean result) {
															dialog.dismiss();
															if (result) {
																showToast("全員へ返信しました");
																editText.setText("");
															} else {

																showToast("返信できませんでした。");

															}
														}
													};
													task.execute(editText.getText().toString());
												}


											});

											final AlertDialog.Builder listDlg = new AlertDialog.Builder(view.getContext());
											new AlertDialog.Builder(MainActivity.this)
													.setTitle((String) items[1])
													.setView(view)
													.setPositiveButton( "閉じる", new DialogInterface.OnClickListener() {
														@Override
														public void onClick(DialogInterface dialog, int which) {
															dialog.dismiss();
														} }) .show();


											break;
										case 2:
											//RT
											AsyncTask<Long,Void,Boolean> rt = new AsyncTask<Long,Void,Boolean>(){
												@Override
												protected Boolean doInBackground(Long... params) {
													try{
														mTwitter.retweetStatus(params[0]).getId();

														return true;
													} catch (TwitterException e) {
														// TODO Auto-generated catch block
														e.printStackTrace();
														return false;
													}
												}
												@Override
												protected void onPostExecute(Boolean result) {
													dialog.dismiss();
													if (result) {
														showToast("公式RTしました");
													} else {
														showToast("公式RTできませんでした");
													}
												}
											};
											rt.execute(rt_id);

											break;

										case 3:
											//  ���C�ɓ���
											AsyncTask<Long,Void,Boolean> fav = new AsyncTask<Long,Void,Boolean>(){

												@Override
												protected Boolean doInBackground(Long... params) {
													try {
														mTwitter.createFavorite((params[0])).getId();
														return true;
													} catch (TwitterException e) {
														// TODO �����������ꂽ catch �u���b�N
														e.printStackTrace();
														return false;
													}


												}
												@Override
												protected void onPostExecute(Boolean result) {
													dialog.dismiss();
													if (result) {
														showToast("tweetをお気に入り登録できませんでした");


													} else {

														showToast("何らかの事情でtweetをお気に入り登録できませんでした。");

													}
												}


											};
											fav.execute(item.getId());
											break;
									}


								}


							});

					// �\��

					listDlg.create().show();
				}

			});



			reloadTimeLine();

		}

	}




	private View getFooter() {
		if (mFooter == null) {
			mFooter = getLayoutInflater().inflate(R.layout.listview_footer,
					null);
		}
		return mFooter;
	}




	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
			case R.id.menu_refresh:
				reloadTimeLine();
				showToast("更新しました");
				return true;
			case R.id.menu_tweet:
				Intent intent = new Intent(this, TweetActivity.class);
				startActivity(intent);
				finish();
				return true;
			case R.id.menu_reply:
				showToast("リプライ一覧を取得しています。");
				replyTimeLine();
				showToast("リプライ一覧を取得しました。");
				return true;
		}
		return super.onOptionsItemSelected(item);
	}



	//Twitter
	private class TweetAdapter extends ArrayAdapter<twitter4j.Status> {

		private LayoutInflater mInflater;

		public TweetAdapter(Context context) {
			super(context, android.R.layout.simple_list_item_1);
			mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.list_item_tweet, null);
			}

			Status item = getItem(position);
			//UserName
			TextView name = (TextView)convertView.findViewById(R.id.name);
			name.setText(item.getUser().getName());
			//@xxx(userID)
			TextView screenName = (TextView) convertView.findViewById(R.id.screen_name);
			screenName.setText("@" + item.getUser().getScreenName());
			//本文
			TextView text=(TextView) convertView.findViewById(R.id.text);


			//TWのキャラの仕掛け

			String str = item.getText();


        /*if(str.indexOf("@")!=-1){
            String x_user ="@"+item.getInReplyToScreenName();
            UserMentionEntity[] ume = item.getUserMentionEntities();
             if(ume != null){
                List reply = new ArrayList();
                for(int i=0;i<ume.length;i++){
                    UserMentionEntity replyall=ume[i];

                    reply.add(x_user);
                    if(str.indexOf(x_user)!= -1){
                        int start = str.indexOf(x_user);
                        int end = x_user.length();
                        SpannableString spannable = new SpannableString(str);
                        BackgroundColorSpan span = new BackgroundColorSpan(Color.argb(50, 184, 101, 204));
                        spannable.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        text.setText(spannable);
                    }else{
                        text.setText(item.getText());
                    }


                }

                String[] replies = ( String[] )reply.toArray( new String[0] );


            }


        }
        else{ */
			text.setText(str);
			//   }


			//UserIcon
			String iconUrl =item.getUser().getProfileImageURL();
			ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
			Glide.with(MainActivity.this).load(iconUrl).into(icon);
			//RT
			ImageView rt_icon = (ImageView) convertView.findViewById(R.id.rt_icon);
			if(item.isRetweet()){
				String rtUrl =item.getRetweetedStatus().getUser().getProfileImageURL();
				Glide.with(MainActivity.this).load(rtUrl).into(rt_icon);
				rt_icon.setVisibility(View.VISIBLE);
			}else{
				rt_icon.setVisibility(View.GONE);
			}

			//Userタイムラインの画像の表示

			MediaEntity[] mediaEntitys = item.getExtendedMediaEntities();
			ImageView media1=(ImageView)convertView.findViewById(R.id.imageView1);
			ImageView media2=(ImageView)convertView.findViewById(R.id.imageView2);
			ImageView media3=(ImageView)convertView.findViewById(R.id.imageView3);
			ImageView media4=(ImageView)convertView.findViewById(R.id.imageView4);



			if(mediaEntitys !=null){
				List list = new ArrayList();

				for( int i = 0; i < mediaEntitys.length; i ++ ){
					MediaEntity mediaEntity = mediaEntitys[i];
					String mediaURL = mediaEntity.getMediaURL()+":small";
					list.add(mediaURL);
				}
				String[] medias = ( String[] )list.toArray( new String[0] );

				switch(mediaEntitys.length){
					case 1:
						media1.setVisibility(View.VISIBLE);
						media2.setVisibility(View.GONE);
						media3.setVisibility(View.GONE);
						media4.setVisibility(View.GONE);
						Glide.with(MainActivity.this).load(medias[0]).thumbnail(0.1f).into(media1);
						break;
					case 2:
						media1.setVisibility(View.VISIBLE);
						media2.setVisibility(View.VISIBLE);
						media3.setVisibility(View.GONE);
						media4.setVisibility(View.GONE);
						Glide.with(MainActivity.this).load(medias[0]).thumbnail(0.1f).into(media1);
						Glide.with(MainActivity.this).load(medias[1]).thumbnail(0.1f).into(media2);
						break;
					case 3:
						media1.setVisibility(View.VISIBLE);
						media2.setVisibility(View.VISIBLE);
						media3.setVisibility(View.VISIBLE);
						media4.setVisibility(View.GONE);
						Glide.with(MainActivity.this).load(medias[0]).thumbnail(0.1f).into(media1);
						Glide.with(MainActivity.this).load(medias[1]).thumbnail(0.1f).into(media2);
						Glide.with(MainActivity.this).load(medias[2]).thumbnail(0.1f).into(media3);
						break;
					case 4:
						media1.setVisibility(View.VISIBLE);
						media2.setVisibility(View.VISIBLE);
						media3.setVisibility(View.VISIBLE);
						media4.setVisibility(View.VISIBLE);
						Glide.with(MainActivity.this).load(medias[0]).thumbnail(0.1f).into(media1);
						Glide.with(MainActivity.this).load(medias[1]).thumbnail(0.1f).into(media2);
						Glide.with(MainActivity.this).load(medias[2]).thumbnail(0.1f).into(media3);
						Glide.with(MainActivity.this).load(medias[3]).thumbnail(0.1f).into(media4);
						break;
					default:
						media1.setVisibility(View.GONE);
						media2.setVisibility(View.GONE);
						media3.setVisibility(View.GONE);
						media4.setVisibility(View.GONE);
						break;

				}


			}else{
				media1.setVisibility(View.GONE);
				media2.setVisibility(View.GONE);
				media3.setVisibility(View.GONE);
				media4.setVisibility(View.GONE);

			}

			return convertView;
		}
	}

	private void replyTimeLine(){
		AsyncTask<Void,Void,ResponseList<Status>> task = new AsyncTask<Void,Void,ResponseList<Status>>(){

			@Override
			protected ResponseList<twitter4j.Status> doInBackground(Void... arg0) {
				// TODO Auto-generated method stub
				try{
					Paging paging = new Paging();
					paging.setCount(10);
					return mTwitter.getMentionsTimeline(paging);
				}catch(TwitterException e){
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(ResponseList<twitter4j.Status> result) {
				if (result != null) {
					mAdapter.clear();
					for (twitter4j.Status status : result) {
						mAdapter.add(status);
					}
					getListView().setSelection(0);

				} else {
					showToast("リプライを取得できませんでした");

				}
			}
		};
		task.execute();

	}


		//timeline
		//timeline
		private void reloadTimeLine() {
			AsyncTask<Void, Void, List<twitter4j.Status>> task = new AsyncTask<Void, Void, List<twitter4j.Status>>() {
				@Override
				protected void onPreExecute() {
					//NOWLOADING画面表示
					Log.d("TAG_ASYNCTASK", "onPreExecute");
					// Dialog表示
					waitDialog = new ProgressDialog(MainActivity.this);
					waitDialog.setMessage("NOW LOADING...");
					waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					waitDialog.setIndeterminate(true);
					waitDialog.show();


				}

				@Override
				protected List<twitter4j.Status> doInBackground(Void... params) {

					int page = 1;
					ResponseList<twitter4j.Status> tl = null;
					int total = 0;


					while (true) {
						Paging paging = new Paging(page++,COUNT_MAX);
						try {
							Thread.sleep(50);
							if (tl == null) {
								tl = mTwitter.getHomeTimeline(paging);
							} else {

								total = tl.size();
								tl.addAll(mTwitter.getHomeTimeline(paging));
							}

							if (tl.size() == total) {
								break;
							}

						} catch (TwitterException e) {
							if (RATE_LIMITED_STATUS_CODE != e.getStatusCode()) {
								continue;
							}
							e.printStackTrace();
							break;
						} catch (InterruptedException e) {
							// TODO
							e.printStackTrace();
						}

					}
					return tl;



				}

				@Override
				protected void onPostExecute(List<twitter4j.Status> result) {

					if (result != null) {
						if (waitDialog.isShowing()){waitDialog.dismiss();}
						mAdapter.clear();
						for (twitter4j.Status status : result) {
							mAdapter.add(status);
						}
						getListView().setSelection(0);


					} else {
						showToast("タイムラインの取得に失敗しました。。。");

					}
				}
			};
			task.execute();
		}

	private void moreLoadTimeLine(){
		if(getFooter().isShown()){
			getFooter().setVisibility(View.GONE);
		}else {
			getFooter().setVisibility(View.VISIBLE);
		}

		AsyncTask<Void, Void, List<twitter4j.Status>> task = new AsyncTask<Void, Void, List<twitter4j.Status>>() {
			@Override
			protected List<twitter4j.Status> doInBackground(Void... params) {
				try {

					Paging paging = new Paging();
					twitter4j.Status status = mAdapter.getItem(mAdapter.getCount()-1);
					//paging.setMaxId(status.getId());
					paging.setSinceId(status.getId());

					return mTwitter.getHomeTimeline(paging);
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(List<twitter4j.Status> result) {
				if (result != null) {
					for (twitter4j.Status status : result) {
						mAdapter.add(status);
					}
					getListView().invalidate();

				} else {
					showToast("タイムラインの取得に失敗しました。。。");

				}
			}
		};
		task.execute();
	}

	private void showToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

}