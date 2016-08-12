package cm.aptoide.pt.v8engine.receivers;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import cm.aptoide.pt.database.Database;
import cm.aptoide.pt.dataprovider.model.MinimalAd;
import cm.aptoide.pt.logger.Logger;
import cm.aptoide.pt.model.v2.GetAdsResponse;
import cm.aptoide.pt.utils.AptoideUtils;
import cm.aptoide.pt.v8engine.MainActivityFragment;
import cm.aptoide.pt.v8engine.R;
import cm.aptoide.pt.v8engine.analytics.Analytics;
import cm.aptoide.pt.v8engine.xml.XmlAppHandler;
import io.realm.Realm;
import lombok.Cleanup;

/**
 * Created by neuro on 10-08-2016.
 */
public class DeepLinkIntentReceiver extends AppCompatActivity {

	private ArrayList<String> server;
	private HashMap<String,String> app;
	private String TMP_MYAPP_FILE;
	private Class startClass = MainActivityFragment.class;
	private AsyncTask<String,Void,Void> asyncTask;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		TMP_MYAPP_FILE = getCacheDir() + "/myapp.myapp";
		String uri = getIntent().getDataString();
		Analytics.ApplicationLaunch.website(uri);

		if (uri.startsWith("aptoiderepo")) {

			ArrayList<String> repo = new ArrayList<>();
			repo.add(uri.substring(14));
			startActivityWithRepo(repo);
		} else if (uri.startsWith("aptoidexml")) {

			String repo = uri.substring(13);
			parseXmlString(repo);
			Intent i = new Intent(DeepLinkIntentReceiver.this, startClass);
			i.putExtra("newrepo", repo);
			//			i.addFlags(DeepLinksSources.NEW_REPO_FLAG);
			startActivity(i);
			finish();
		} else if (uri.startsWith("aptoidesearch://")) {
			startIntentFromPackageName(uri.split("aptoidesearch://")[1]);
		} else if (uri.startsWith("aptoidevoicesearch://")) {
			aptoidevoiceSearch(uri.split("aptoidevoicesearch://")[1]);
		} else if (uri.startsWith("market")) {
			String params = uri.split("&")[0];
			String param = params.split("=")[1];
			if (param.contains("pname:")) {
				param = param.substring(6);
			} else if (param.contains("pub:")) {
				param = param.substring(4);
			}
			startIntentFromPackageName(param);
		} else if (uri.startsWith("http://market.android.com/details?id=")) {
			String param = uri.split("=")[1];
			startIntentFromPackageName(param);
		} else if (uri.startsWith("https://market.android.com/details?id=")) {
			String param = uri.split("=")[1];
			startIntentFromPackageName(param);
		} else if (uri.startsWith("https://play.google.com/store/apps/details?id=")) {
			String params = uri.split("&")[0];
			String param = params.split("=")[1];
			if (param.contains("pname:")) {
				param = param.substring(6);
			} else if (param.contains("pub:")) {
				param = param.substring(4);
			}
			startIntentFromPackageName(param);
		} else if (uri.contains("aptword://")) {

			String param = uri.substring("aptword://".length());

			if (!TextUtils.isEmpty(param)) {

				param = param.replaceAll("\\*", "_").replaceAll("\\+", "/");

				String json = new String(Base64.decode(param.getBytes(), 0));

				Log.d("AptoideAptWord", json);

				GetAdsResponse.Ad ad = null;
				try {
					ad = new ObjectMapper().readValue(json, GetAdsResponse.Ad.class);
				} catch (IOException e) {
					Logger.printException(e);
				}

				if (ad != null) {
					Intent i = new Intent(this, startClass);
					i.putExtra(DeepLinksSources.FROM_AD, MinimalAd.from(ad));
					startActivity(i);
				}

				finish();
			}
		} else if (uri.contains("imgs.aptoide.com")) {

			String[] strings = uri.split("-");
			long id = Long.parseLong(strings[strings.length - 1].split("\\.myapp")[0]);

			startFromMyApp(id);
			finish();
		} else if (uri.startsWith("http://webservices.aptoide.com")) {
			/** refactored to remove org.apache libs */
			Map<String,String> params = null;

			try {
				params = AptoideUtils.StringU.splitQuery(URI.create(uri));
			} catch (UnsupportedEncodingException e) {
				Logger.printException(e);
			}

			if (params != null) {
				String uid = null;
				for (Map.Entry<String,String> entry : params.entrySet()) {
					if (entry.getKey().equals("uid")) {
						uid = entry.getValue();
					}
				}

				if (uid != null) {
					try {
						long id = Long.parseLong(uid);
						startFromMyApp(id);
					} catch (NumberFormatException e) {
						Logger.printException(e);
						Toast.makeText(getApplicationContext(), R.string.simple_error_occured + uid, Toast.LENGTH_LONG).show();
					}
				}
			}

			finish();
		} else if (uri.startsWith("file://")) {

			downloadMyApp();
		} else if (uri.startsWith("aptoideinstall://")) {

			try {
				long id = Long.parseLong(uri.substring("aptoideinstall://".length()));
				startFromMyApp(id);
			} catch (NumberFormatException e) {
				Logger.printException(e);
			}

			finish();
		} else {
			finish();
		}
	}

	public void startFromMyApp(long id) {
		//		Intent i = new Intent(this, appViewClass);
		Intent i = new Intent(this, startClass);
		i.putExtra(MainActivityFragment.TargetFragment.APP_VIEW_FRAGMENT, true);
		i.putExtra(DeepLinksSources.FROM_MYAPP, true);
		i.putExtra(DeepLinksKeys.APP_ID_KEY, id);

		startActivity(i);
	}

	public void aptoidevoiceSearch(String param) {
		// TODO: voiceSearch was used by a foreign app, dunno if still used.
		//        Cursor c = new AptoideDatabase(Aptoide.getDb()).getSearchResults(param, StoreActivity.Sort.DOWNLOADS);
		//
		//        ArrayList<String> namelist = new ArrayList<String>();
		//        ArrayList<Long> idlist = new ArrayList<Long>();
		//
		//        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
		//            namelist.add(c.getString(c.getColumnIndex("name")));
		//            idlist.add(c.getLong(c.getColumnIndex("_id")));
		//        }
		//
		//        Intent i = new Intent();
		//        i.putStringArrayListExtra("namelist", namelist);
		//        i.putExtra("idlist", AptoideUtils.longListToLongArray(idlist));
		//
		//        setResult(UNKONWN_FLAG, i);
		finish();
	}

	private void parseXmlString(String file) {

		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			XmlAppHandler handler = new XmlAppHandler();
			xr.setContentHandler(handler);

			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(file));
			xr.parse(is);
			server = handler.getServers();
			app = handler.getApp();
		} catch (IOException | SAXException | ParserConfigurationException e) {
			Logger.printException(e);
		}
	}

	public void startActivityWithRepo(ArrayList<String> repo) {
		Intent i = new Intent(DeepLinkIntentReceiver.this, startClass);
		i.putExtra(DeepLinksSources.NEW_REPO, repo);
		//		i.addFlags(DeepLinksSources.NEW_REPO_FLAG);
		startActivity(i);
		Analytics.ApplicationLaunch.newRepo();

		finish();
	}

	public void startIntentFromPackageName(String packageName) {
		@Cleanup
		Realm realm = Database.get();

		Intent i;
		if (Database.InstalledQ.isInstalled(packageName, realm)) {
			//			i = new Intent(this, appViewClass);
			i = new Intent(this, startClass);
			i.putExtra(DeepLinksSources.MARKET_INTENT, true);
			i.putExtra(DeepLinksKeys.PACKAGENAME_KEY, packageName);
		} else {
			//			i = new Intent(this, SearchActivity.class);
			i = new Intent(this, startClass);
			i.putExtra(MainActivityFragment.TargetFragment.SEARCH_FRAGMENT, packageName);
			i.putExtra(SearchManager.QUERY, packageName);
		}

		startActivity(i);
		finish();
	}

	private void downloadMyApp() {
		asyncTask = new MyAppDownloader().execute(getIntent().getDataString());
	}

	private void downloadMyappFile(String myappUri) throws Exception {
		try {
			URL url = new URL(myappUri);
			URLConnection connection;
			if (!myappUri.startsWith("file://")) {
				connection = url.openConnection();
				connection.setReadTimeout(5000);
				connection.setConnectTimeout(5000);
			} else {
				connection = url.openConnection();
			}

			BufferedInputStream getit = new BufferedInputStream(connection.getInputStream(), 1024);

			File file_teste = new File(TMP_MYAPP_FILE);
			if (file_teste.exists()) {
				file_teste.delete();
			}

			FileOutputStream saveit = new FileOutputStream(TMP_MYAPP_FILE);
			BufferedOutputStream bout = new BufferedOutputStream(saveit, 1024);
			byte data[] = new byte[1024];

			int readed = getit.read(data, 0, 1024);
			while (readed != -1) {
				bout.write(data, 0, readed);
				readed = getit.read(data, 0, 1024);
			}

			bout.close();
			getit.close();
			saveit.close();
		} catch (Exception e) {
			Logger.printException(e);
		}
	}

	private void parseXmlMyapp(String file) throws Exception {

		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XmlAppHandler handler = new XmlAppHandler();
			sp.parse(new File(file), handler);
			server = handler.getServers();
			app = handler.getApp();
		} catch (IOException | SAXException | ParserConfigurationException e) {
			Logger.printException(e);
		}
	}

	private void proceed() {
		if (server != null) {
			startActivityWithRepo(server);
		} else {
			Toast.makeText(this, getString(R.string.error_occured), Toast.LENGTH_LONG).show();
			finish();
		}
	}

	public static class DeepLinksSources {

		public final static String NEW_REPO = "newrepo";
		//		public final static int NEW_REPO_FLAG = 12345;
		public static final String FROM_DOWNLOAD_NOTIFICATION = "fromDownloadNotification";
		public static final String FROM_TIMELINE = "fromTimeline";
		public static final String NEW_UPDATES = "new_updates";
		public static final String FROM_AD = "fromAd";
		public static final String FROM_MYAPP = "fromMyapp";
		public static final String MARKET_INTENT = "market_intent";
	}

	public static class DeepLinksKeys {

		public static final String APP_ID_KEY = "appId";
		public static final String PACKAGENAME_KEY = "packageName";
	}

	class MyAppDownloader extends AsyncTask<String,Void,Void> {

		ProgressDialog pd;

		@Override
		protected Void doInBackground(String... params) {

			try {
				downloadMyappFile(params[0]);
				parseXmlMyapp(TMP_MYAPP_FILE);
			} catch (Exception e) {
				Logger.printException(e);
			}

			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pd = new ProgressDialog(DeepLinkIntentReceiver.this);
			pd.show();
			pd.setCancelable(false);
			pd.setMessage(getString(R.string.please_wait));
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			if (pd.isShowing() && !isFinishing()) {
				pd.dismiss();
			}

			if (app != null && !app.isEmpty()) {

				/** never worked... */
				//                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(IntentReceiver.this);
				//                final AlertDialog installAppDialog = dialogBuilder.create();
				////                installAppDialog.setTitle(ApplicationAptoide.MARKETNAME);
				//                installAppDialog.setIcon(android.R.drawable.ic_menu_more);
				//                installAppDialog.setCancelable(false);
				//
				//
				//                installAppDialog.setMessage(getString(R.string.installapp_alrt) + app.get("name") + "?");
				//
				//                installAppDialog.setButton(Dialog.BUTTON_POSITIVE, getString(android.R.string.yes), new Dialog.OnClickListener() {
				//                    @Override
				//                    public void onClick(DialogInterface arg0, int arg1) {
				////                        Download download = new Download();
				////                        Log.d("Aptoide-IntentReceiver", "getapk id: " + id);
				////                        download.setId(id);
				////                        ((Start)getApplicationContext()).installApp(0);
				//
				//                        Toast toast = Toast.makeText(IntentReceiver.this, getString(R.string.starting_download), Toast.LENGTH_SHORT);
				//                        toast.show();
				//                    }
				//                });
				//
				//                installAppDialog.setButton(Dialog.BUTTON_NEGATIVE, getString(android.R.string.no), neutralListener);
				//                installAppDialog.setOnDismissListener(IntentReceiver.this);
				//                installAppDialog.show();

			} else {
				proceed();
			}
		}
	}
}