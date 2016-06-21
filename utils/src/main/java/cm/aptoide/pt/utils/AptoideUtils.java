/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 15/06/2016.
 */

package cm.aptoide.pt.utils;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UnknownFormatConversionException;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import cm.aptoide.pt.logger.Logger;
import lombok.Getter;
import lombok.Setter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.net.ConnectivityManager.TYPE_ETHERNET;
import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_WIFI;

/**
 * Created by neuro on 26-05-2016.
 */
public class AptoideUtils {

	@Getter @Setter private static Context context;

	public static class Core {

		public static int getVerCode() {
			PackageManager manager = context.getPackageManager();
			try {
				PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
				return info.versionCode;
			} catch (PackageManager.NameNotFoundException e) {
				return -1;
			}
		}

		public static String filters(boolean hwSpecsFilter) {
			if (!hwSpecsFilter) {
				return null;
			}

			int minSdk = SystemU.getSdkVer();
			String minScreen = ScreenU.getScreenSize();
			String minGlEs = SystemU.getGlEsVer();

			final int density = ScreenU.getDensityDpi();

			String cpuAbi = SystemU.getAbis();

			int myversionCode = 0;
			PackageManager manager = context.getPackageManager();
			try {
				myversionCode = manager.getPackageInfo(context.getPackageName(), 0).versionCode;
			} catch (PackageManager.NameNotFoundException ignore) {
			}

			String filters = (Build.DEVICE.equals("alien_jolla_bionic") ? "apkdwn=myapp&" : "") + "maxSdk=" + minSdk +
					"&maxScreen=" + minScreen + "&maxGles=" + minGlEs + "&myCPU=" + cpuAbi + "&myDensity=" + density +
					"&myApt=" + myversionCode;

			return Base64.encodeToString(filters.getBytes(), 0)
					.replace("=", "")
					.replace("/", "*")
					.replace("+", "_")
					.replace("\n", "");
		}
	}

	public static class AlgorithmU {

		public static byte[] computeSha1(byte[] bytes) {
			MessageDigest md;
			try {
				md = MessageDigest.getInstance("SHA-1");
				md.update(bytes, 0, bytes.length);
				return md.digest();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}

			return new byte[0];
		}

		public static String computeSha1(String text) {
			try {
				return convToHex(computeSha1(text.getBytes("iso-8859-1")));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return "";
		}

		public static String computeHmacSha1(String value, @NonNull String keyString) {
			try {
				SecretKeySpec key = new SecretKeySpec((keyString).getBytes("UTF-8"), "HmacSHA1");
				Mac mac = Mac.getInstance("HmacSHA1");
				mac.init(key);

				byte[] bytes = mac.doFinal(value.getBytes("UTF-8"));
				return convToHex(bytes);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			}
			return "";
		}

		public static String computeSha1WithColon(byte[] bytes) {
			return convToHexWithColon(computeSha1(bytes));
		}

		private static String convToHexWithColon(byte[] data) {
			StringBuilder buf = new StringBuilder();
			for (int i = 0; i < data.length; i++) {
				int halfbyte = (data[i] >>> 4) & 0x0F;
				int two_halfs = 0;
				do {
					if ((0 <= halfbyte) && (halfbyte <= 9)) {
						buf.append((char) ('0' + halfbyte));
					} else {
						buf.append((char) ('a' + (halfbyte - 10)));
					}
					halfbyte = data[i] & 0x0F;
				} while (two_halfs++ < 1);

				if (i < data.length - 1) {
					buf.append(":");
				}
			}
			return buf.toString();
		}

		private static String convToHex(byte[] data) {
			final StringBuilder buffer = new StringBuilder();
			for (byte b : data) {
				buffer.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
			}
			return buffer.toString();
		}

		public static String computeMd5(File f) {
			byte[] buffer = new byte[1024];
			int read, i;
			String md5hash;
			try {
				MessageDigest digest = MessageDigest.getInstance("MD5");
				InputStream is = new FileInputStream(f);
				while ((read = is.read(buffer)) > 0) {
					digest.update(buffer, 0, read);
				}
				byte[] md5sum = digest.digest();
				BigInteger bigInt = new BigInteger(1, md5sum);
				md5hash = bigInt.toString(16);
				is.close();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

			if (md5hash.length() != 33) {
				String tmp = "";
				for (i = 1; i < (33 - md5hash.length()); i++) {
					tmp = tmp.concat("0");
				}
				md5hash = tmp.concat(md5hash);
			}

			return md5hash;
		}

		public static String computeMd5(@NonNull PackageInfo packageInfo) {
			String sourceDir = packageInfo.applicationInfo.sourceDir;
			File apkFile = new File(sourceDir);
			return computeMd5(apkFile);
		}
	}

	public static class ImageSizeU {

		static final private int baseLine = 96;
		static final private int baseLineAvatar = 150;
		static final private int baseLineXNotification = 320;
		static final private int baseLineYNotification = 180;
		private static int baseLineScreenshotLand = 256;
		private static int baseLineScreenshotPort = 96;

		private static String generateSizeString(int baseLine) {

			float densityMultiplier = getDensityMultiplier();

			int size = (int) (baseLine * densityMultiplier);

			//Log.d("Aptoide-IconSize", "Size is " + size);

			return size + "x" + size;
		}

		private static String generateSizeStringAvatar() {
			return generateSizeString(baseLineAvatar);
		}

		private static String generateSizeStringNotification() {

			float densityMultiplier = getDensityMultiplier();

			int sizeX = (int) (baseLineXNotification * densityMultiplier);
			int sizeY = (int) (baseLineYNotification * densityMultiplier);

			//Log.d("Aptoide-IconSize", "Size is " + size);

			return sizeX + "x" + sizeY;
		}

		private static String generateSizeStringScreenshots(String orient) {
			float densityMultiplier = getDensityMultiplier();

			int size;
			if (orient != null && orient.equals("portrait")) {
				size = baseLineScreenshotPort * ((int) densityMultiplier);
			} else {
				size = baseLineScreenshotLand * ((int) densityMultiplier);
			}

			return size + "x" + ScreenU.getDensityDpi();
		}

		private static String[] splitUrlExtension(String url) {
			return url.split(RegexU.SPLIT_URL_EXTENSION);
		}

		private static float getDensityMultiplier() {
			float densityMultiplier = context.getResources().getDisplayMetrics().density;

			//Log.d("Aptoide-IconSize", "Original mult is" + densityMultiplier);

			if (densityMultiplier <= 0.75f) {
				densityMultiplier = 0.75f;
			} else if (densityMultiplier <= 1) {
				densityMultiplier = 1f;
			} else if (densityMultiplier <= 1.333f) {
				densityMultiplier = 1.3312500f;
			} else if (densityMultiplier <= 1.5f) {
				densityMultiplier = 1.5f;
			} else if (densityMultiplier <= 2f) {
				densityMultiplier = 2f;
			} else if (densityMultiplier <= 3f) {
				densityMultiplier = 3f;
			} else {
				densityMultiplier = 4f;
			}

			return densityMultiplier;
		}

		public static String parseAvatarUrl(String avatarUrl) {
			String[] splittedUrl = splitUrlExtension(avatarUrl);
			return splittedUrl[0] + "_" + generateSizeStringAvatar() + "." + splittedUrl[1];
		}

		public static String parseScreenshotUrl(String screenshotUrl, String orientation) {
			String sizeString = ImageSizeU.generateSizeStringScreenshots(orientation);

			String[] splitUrl = splitUrlExtension(screenshotUrl);
			return splitUrl[0] + "_" + sizeString + "." + splitUrl[1];
		}

		public static String screenshotToThumb(String imageUrl, String orientation) {

			String screen = null;

			try {

				if (imageUrl.contains("_screen")) {
					screen = parseScreenshotUrl(imageUrl, orientation);
				} else {

					String[] splitString = imageUrl.split("/");
					StringBuilder db = new StringBuilder();
					for (int i = 0; i != splitString.length - 1; i++) {
						db.append(splitString[i]);
						db.append("/");
					}

					db.append("thumbs/mobile/");
					db.append(splitString[splitString.length - 1]);
					screen = db.toString();
				}
			} catch (Exception e) {
				Logger.printException(e);
				// FIXME uncomment the following lines
				//Crashlytics.setString("imageUrl", imageUrl);
				//Crashlytics.logException(e);
			}

			return screen;
		}
	}

	public static final class MathU {

		public static int greatestCommonDivisor(int a, int b) {
			while (b > 0) {
				int temp = b;
				b = a % b; // % is remainder
				a = temp;
			}
			return a;
		}

		public static int leastCommonMultiple(int a, int b) {
			return a * (b / greatestCommonDivisor(a, b));
		}

		public static int leastCommonMultiple(int[] input) {
			int result = input[0];
			for (int i = 1; i < input.length; i++) result = leastCommonMultiple(result, input[i]);
			return result;
		}
	}

	public static class RegexU {

		private static final String STORE_ID_FROM_GET_URL = "store_id\\/(\\d+)\\/";
		private static final String STORE_NAME_FROM_GET_URL = "store_name\\/(.*?)\\/";
		private static final String SPLIT_URL_EXTENSION = "\\.(?=[^\\.]+$)";

		private static Pattern STORE_ID_FROM_GET_URL_PATTERN;
		private static Pattern STORE_NAME_FROM_GET_URL_PATTERN;

		public static Pattern getStoreIdFromGetUrlPattern() {
			if (STORE_ID_FROM_GET_URL_PATTERN == null) {
				STORE_ID_FROM_GET_URL_PATTERN = Pattern.compile(STORE_ID_FROM_GET_URL);
			}

			return STORE_ID_FROM_GET_URL_PATTERN;
		}

		public static Pattern getStoreNameFromGetUrlPattern() {
			if (STORE_NAME_FROM_GET_URL_PATTERN == null) {
				STORE_NAME_FROM_GET_URL_PATTERN = Pattern.compile(STORE_NAME_FROM_GET_URL);
			}

			return STORE_NAME_FROM_GET_URL_PATTERN;
		}
	}

	public static final class ScreenU {

		public static final float REFERENCE_WIDTH_DPI = 360;

		private static ScreenUtilsCache screenWidthInDipCache = new ScreenUtilsCache();

		public static int getCurrentOrientation() {
			return context.getResources().getConfiguration().orientation;
		}

		public static float getScreenWidthInDip() {
			if (getCurrentOrientation() != screenWidthInDipCache.orientation) {
				WindowManager wm = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
				DisplayMetrics dm = new DisplayMetrics();
				wm.getDefaultDisplay().getMetrics(dm);
				screenWidthInDipCache.set(getCurrentOrientation(), dm.widthPixels / dm.density);
			}

			return screenWidthInDipCache.value;
		}

		public static int getPixels(int dipValue) {
			Resources r = context.getResources();
			int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, r.getDisplayMetrics());
			Logger.d("getPixels", "" + px);
			return px;
		}

		private static int getScreenSizeInt() {
			return context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
		}

		public static int getNumericScreenSize() {
			int size = context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
			return (size + 1) * 100;
		}

		public static String getScreenSize() {
			return Size.values()[getScreenSizeInt()].name().toLowerCase(Locale.ENGLISH);
		}

		public static int getDensityDpi() {

			DisplayMetrics metrics = new DisplayMetrics();
			((WindowManager) context.getSystemService(Service.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);

			int dpi = metrics.densityDpi;

			if (dpi <= 120) {
				dpi = 120;
			} else if (dpi <= 160) {
				dpi = 160;
			} else if (dpi <= 213) {
				dpi = 213;
			} else if (dpi <= 240) {
				dpi = 240;
			} else if (dpi <= 320) {
				dpi = 320;
			} else if (dpi <= 480) {
				dpi = 480;
			} else {
				dpi = 640;
			}

			return dpi;
		}

		public enum Size {
			notfound, small, normal, large, xlarge;

			public static Size lookup(String screen) {
				try {
					return valueOf(screen);
				} catch (Exception e) {
					return notfound;
				}
			}
		}

		private static class ScreenUtilsCache {

			private int orientation = -1;
			private float value;

			public void set(int currentOrientation, float value) {
				this.orientation = currentOrientation;
				this.value = value;
			}
		}
	}

	public static final class StringU {

		public static String withSuffix(long count) {
			if (count < 1000) {
				return String.valueOf(count);
			}
			int exp = (int) (Math.log(count) / Math.log(1000));
			return String.format(Locale.ENGLISH, "%d %c", (int) (count / Math.pow(1000, exp)), "kMGTPE".charAt(exp -
					1));
		}

		public static String withBinarySuffix(long bytes) {
			int unit = 1024;
			if (bytes < unit) {
				return bytes + " B";
			}
			int exp = (int) (Math.log(bytes) / Math.log(unit));
			String pre = ("KMGTPE").charAt(exp - 1) + "";
			return String.format(Locale.ENGLISH, "%.1f %sb", bytes / Math.pow(unit, exp), pre);
		}

		public static String getResString(@StringRes int stringResId) {
			return context.getResources().getString(stringResId);
		}

		public static String getFormattedString(@StringRes int resId, Object... formatArgs) {
			String result;
			final Resources resources = context.getResources();
			try {
				result = resources.getString(resId, formatArgs);
			} catch (UnknownFormatConversionException ex) {
				final String resourceEntryName = resources.getResourceEntryName(resId);
				final String displayLanguage = Locale.getDefault().getDisplayLanguage();
				Logger.e("UnknownFormatConversion", "String: " + resourceEntryName + " Locale: " + displayLanguage);
				//// TODO: 18-05-2016 neuro uncomment
				//			Crashlytics.log(3, "UnknownFormatConversion", "String: " + resourceEntryName + " Locale:
				// " +
				// displayLanguage);
				result = resources.getString(resId);
			}
			return result;
		}

		public static String commaSeparatedValues(List<?> list) {
			String s = new String();

			if (list.size() > 0) {
				s = list.get(0).toString();

				for (int i = 1; i < list.size(); i++) {
					s += "," + list.get(i).toString();
				}
			}

			return s;
		}
	}

	public static class SystemU {

		public static String JOLLA_ALIEN_DEVICE = "alien_jolla_bionic";

		public static String getAndroidId() {
			// TODO: 15-06-2016 neuro lazzy may not be a bad idea.
			return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
		}

		public static int getSdkVer() {
			return Build.VERSION.SDK_INT;
		}

		public static String getGlEsVer() {
			return ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getDeviceConfigurationInfo()
					.getGlEsVersion();
		}

		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		@SuppressWarnings("deprecation")
		public static String getAbis() {
			final String[] abis = getSdkVer() >= Build.VERSION_CODES.LOLLIPOP ? Build.SUPPORTED_ABIS : new
					String[]{Build.CPU_ABI, Build.CPU_ABI2};
			final StringBuilder builder = new StringBuilder();
			for (int i = 0; i < abis.length; i++) {
				builder.append(abis[i]);
				if (i < abis.length - 1) {
					builder.append(",");
				}
			}
			return builder.toString();
		}

		public static String getCountryCode() {
			return context.getResources().getConfiguration().locale.getLanguage() + "_" + context.getResources()
					.getConfiguration().locale.getCountry();
		}

		public static PackageInfo getPackageInfo(String packageName) {
			try {
				return context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
			return null;
		}

		public static List<PackageInfo> getAllInstalledApps() {
			return context.getPackageManager().getInstalledPackages(PackageManager.GET_SIGNATURES);
		}

		public static List<PackageInfo> getUserInstalledApps() {
			List<PackageInfo> tmp = new LinkedList<>();

			for (PackageInfo packageInfo : getAllInstalledApps()) {
				if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
					tmp.add(packageInfo);
				}
			}

			return tmp;
		}

		public static String getApkLabel(PackageInfo packageInfo) {
			return packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
		}

		public static String getApkIconPath(PackageInfo packageInfo) {
			return "android.resource://" + packageInfo.packageName + "/" + packageInfo.applicationInfo.icon;
		}

		public static void openApp(String packageName) {
			Intent launchIntentForPackage = context.getPackageManager().getLaunchIntentForPackage(packageName);

			if (launchIntentForPackage != null) {
				context.startActivity(launchIntentForPackage);
			}
		}

		public static void uninstallApp(Context context, String packageName) {
			Uri uri = Uri.fromParts("package", packageName, null);
			Intent intent = new Intent(Intent.ACTION_DELETE, uri);
			context.startActivity(intent);
		}

		public static String getConnectionType() {
			final ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context
					.CONNECTIVITY_SERVICE);
			final NetworkInfo info = manager.getActiveNetworkInfo();

			if (info.getTypeName() != null) {
				switch (info.getType()) {
					case TYPE_ETHERNET:
						return "ethernet";
					case TYPE_WIFI:
						return "mobile";
					case TYPE_MOBILE:
						return "mobile";
				}
			}
			return "unknown";
		}
	}

	public static final class ThreadU {

		public static void runOnIoThread(Runnable runnable) {
			Observable.just(null).observeOn(Schedulers.io()).subscribe(o -> runnable.run(), Logger::printException);
		}

		public static void runOnUiThread(Runnable runnable) {
			Observable.just(null)
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(o -> runnable.run(), Logger::printException);
		}

		public static void sleep(long l) {
			try {
				Thread.sleep(l);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public static boolean isUiThread() {
			return Looper.getMainLooper().getThread() == Thread.currentThread();
		}
	}

	public static class HtmlU {

		public static CharSequence parse(String text) {
			return Html.fromHtml(text.replace("\n", "<br/>").replace("&", "&amp;"));
		}
	}

	public static class ResourseU {

		public static int getInt(@IntegerRes int resId) {
			return context.getResources().getInteger(resId);
		}

		public static Drawable getDrawable(@DrawableRes int drawableId) {
			return context.getResources().getDrawable(drawableId);
		}

		public static String getString(@StringRes int stringRes) {
			return StringU.getResString(stringRes);
		}
	}

	public static class DateTimeU extends DateUtils {

		private static final long millisInADay = 1000 * 60 * 60 * 24;
		private static String mTimestampLabelYesterday;
		private static String mTimestampLabelToday;
		private static String mTimestampLabelJustNow;
		private static String mTimestampLabelMinutesAgo;
		private static String mTimestampLabelHoursAgo;
		private static String mTimestampLabelHourAgo;
		private static String mTimestampLabelDaysAgo;
		private static String mTimestampLabelWeekAgo;
		private static String mTimestampLabelWeeksAgo;
		private static String mTimestampLabelMonthAgo;
		private static String mTimestampLabelMonthsAgo;
		private static String mTimestampLabelYearAgo;
		private static String mTimestampLabelYearsAgo;
		private static DateTimeU instance;
		private static String[] weekdays = new DateFormatSymbols().getWeekdays(); // get day names

		/**
		 * Singleton constructor, needed to get access to the application context & strings for i18n
		 *
		 * @param context Context
		 *
		 * @return DateTimeUtils singleton instance
		 *
		 * @throws Exception
		 */
		public static DateTimeU getInstance(Context context) {
			if (instance == null) {
				instance = new DateTimeU();
				mTimestampLabelYesterday = context.getResources()
						.getString(R.string.WidgetProvider_timestamp_yesterday);
				mTimestampLabelToday = context.getResources().getString(R.string.WidgetProvider_timestamp_today);
				mTimestampLabelJustNow = context.getResources().getString(R.string.WidgetProvider_timestamp_just_now);
				mTimestampLabelMinutesAgo = context.getResources()
						.getString(R.string.WidgetProvider_timestamp_minutes_ago);
				mTimestampLabelHoursAgo = context.getResources().getString(R.string
						.WidgetProvider_timestamp_hours_ago);
				mTimestampLabelHourAgo = context.getResources().getString(R.string.WidgetProvider_timestamp_hour_ago);
				mTimestampLabelDaysAgo = context.getResources().getString(R.string.WidgetProvider_timestamp_days_ago);
				mTimestampLabelWeekAgo = context.getResources().getString(R.string.WidgetProvider_timestamp_week_ago2);
				mTimestampLabelWeeksAgo = context.getResources().getString(R.string
						.WidgetProvider_timestamp_weeks_ago);
				mTimestampLabelMonthAgo = context.getResources().getString(R.string
						.WidgetProvider_timestamp_month_ago);
				mTimestampLabelMonthsAgo = context.getResources()
						.getString(R.string.WidgetProvider_timestamp_months_ago);
				mTimestampLabelYearAgo = context.getResources().getString(R.string.WidgetProvider_timestamp_year_ago);
				mTimestampLabelYearsAgo = context.getResources().getString(R.string
						.WidgetProvider_timestamp_years_ago);
			}
			return instance;
		}

		/**
		 * Checks if the given date is yesterday.
		 *
		 * @param date - Date to check.
		 *
		 * @return TRUE if the date is yesterday, FALSE otherwise.
		 */
		private static boolean isYesterday(long date) {

			final Calendar currentDate = Calendar.getInstance();
			currentDate.setTimeInMillis(date);

			final Calendar yesterdayDate = Calendar.getInstance();
			yesterdayDate.add(Calendar.DATE, -1);

			return yesterdayDate.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR) && yesterdayDate.get(Calendar
					.DAY_OF_YEAR) == currentDate
					.get(Calendar.DAY_OF_YEAR);
		}

		/**
		 * Displays a user-friendly date difference string
		 *
		 * @param timedate Timestamp to format as date difference from now
		 *
		 * @return Friendly-formatted date diff string
		 */
		public String getTimeDiffString(Context context, long timedate) {
			Calendar startDateTime = Calendar.getInstance();
			Calendar endDateTime = Calendar.getInstance();
			endDateTime.setTimeInMillis(timedate);
			long milliseconds1 = startDateTime.getTimeInMillis();
			long milliseconds2 = endDateTime.getTimeInMillis();
			long diff = milliseconds1 - milliseconds2;

			long hours = diff / (60 * 60 * 1000);
			long minutes = diff / (60 * 1000);
			minutes = minutes - 60 * hours;
			long seconds = diff / (1000);

			boolean isToday = DateTimeU.isToday(timedate);
			boolean isYesterday = DateTimeU.isYesterday(timedate);

			if (hours > 0 && hours < 12) {
				return hours == 1 ? AptoideUtils.StringU.getFormattedString(R.string
						.WidgetProvider_timestamp_hour_ago, hours) : AptoideUtils.StringU
						.getFormattedString(R.string.WidgetProvider_timestamp_hours_ago, hours);
			} else if (hours <= 0) {
				if (minutes > 0) {
					return AptoideUtils.StringU.getFormattedString(R.string.WidgetProvider_timestamp_minutes_ago,
							minutes);
				} else {
					return mTimestampLabelJustNow;
				}
			} else if (isToday) {
				return mTimestampLabelToday;
			} else if (isYesterday) {
				return mTimestampLabelYesterday;
			} else if (startDateTime.getTimeInMillis() - timedate < millisInADay * 6) {
				return weekdays[endDateTime.get(Calendar.DAY_OF_WEEK)];
			} else {
				return formatDateTime(context, timedate, DateUtils.FORMAT_NUMERIC_DATE);
			}
		}

		public String getTimeDiffAll(Context context, long time) {

			long diffTime = new Date().getTime() - time;

			if (isYesterday(time) || isToday(time)) {
				getTimeDiffString(context, time);
			} else {
				if (diffTime < DateUtils.WEEK_IN_MILLIS) {
					int diffDays = Double.valueOf(Math.ceil(diffTime / millisInADay)).intValue();
					return diffDays == 1 ? mTimestampLabelYesterday : AptoideUtils.StringU.getFormattedString(R.string
							.WidgetProvider_timestamp_days_ago, diffDays);
				} else if (diffTime < DateUtils.WEEK_IN_MILLIS * 4) {
					int diffDays = Double.valueOf(Math.ceil(diffTime / WEEK_IN_MILLIS)).intValue();
					return diffDays == 1 ? mTimestampLabelMonthAgo : AptoideUtils.StringU.getFormattedString(R.string
							.WidgetProvider_timestamp_months_ago, diffDays);
				} else if (diffTime < DateUtils.WEEK_IN_MILLIS * 4 * 12) {
					int diffDays = Double.valueOf(Math.ceil(diffTime / (WEEK_IN_MILLIS * 4))).intValue();
					return diffDays == 1 ? mTimestampLabelMonthAgo : AptoideUtils.StringU.getFormattedString(R.string
							.WidgetProvider_timestamp_months_ago, diffDays);
				} else {
					int diffDays = Double.valueOf(Math.ceil(diffTime / (WEEK_IN_MILLIS * 4 * 12))).intValue();
					return diffDays == 1 ? mTimestampLabelYearAgo : AptoideUtils.StringU.getFormattedString(R.string
							.WidgetProvider_timestamp_years_ago, diffDays);
				}
			}

			return getTimeDiffString(context, time);
		}
	}

	/**
	 * Created with IntelliJ IDEA. User: rmateus Date: 03-12-2013 Time: 12:58 To change this template use File |
	 * Settings | File Templates.
	 */
	public static class IconSizeU {

		public static final int DEFAULT_SCREEN_DENSITY = -1;
		public static final HashMap<Integer, String> mStoreIconSizes;
		public static final int ICONS_SIZE_TYPE = 0;
		public static final HashMap<Integer, String> mIconSizes;
		public static final int STORE_ICONS_SIZE_TYPE = 1;
		static final private int baseLine = 96;
		static final private int baseLineAvatar = 150;
		static final private int baseLineXNotification = 320;
		static final private int baseLineYNotification = 180;
		private static int baseLineScreenshotLand = 256;
		private static int baseLineScreenshotPort = 96;

		static {
			mStoreIconSizes = new HashMap<>();
			mStoreIconSizes.put(DisplayMetrics.DENSITY_XXXHIGH, "");
			mStoreIconSizes.put(DisplayMetrics.DENSITY_XXHIGH, "450x450");
			mStoreIconSizes.put(DisplayMetrics.DENSITY_XHIGH, "300x300");
			mStoreIconSizes.put(DisplayMetrics.DENSITY_HIGH, "225x225");
			mStoreIconSizes.put(DisplayMetrics.DENSITY_MEDIUM, "150x150");
			mStoreIconSizes.put(DisplayMetrics.DENSITY_LOW, "113x113");
		}

		static {
			mIconSizes = new HashMap<>();
			mIconSizes.put(DisplayMetrics.DENSITY_XXXHIGH, "");
			mIconSizes.put(DisplayMetrics.DENSITY_XXHIGH, "288x288");
			mIconSizes.put(DisplayMetrics.DENSITY_XHIGH, "192x192");
			mIconSizes.put(DisplayMetrics.DENSITY_HIGH, "144x144");
			mIconSizes.put(DisplayMetrics.DENSITY_MEDIUM, "127x127");
			mIconSizes.put(DisplayMetrics.DENSITY_LOW, "96x96");
		}

		public static String generateSizeStringNotification() {
			if (context == null) {
				return "";
			}
			float densityMultiplier = densityMultiplier();

			int sizeX = (int) (baseLineXNotification * densityMultiplier);
			int sizeY = (int) (baseLineYNotification * densityMultiplier);

			//Log.d("Aptoide-IconSize", "Size is " + size);

			return sizeX + "x" + sizeY;
		}

		public static String generateSizeStoreString() {
			String iconRes = mStoreIconSizes.get(context.getResources().getDisplayMetrics().densityDpi);
			return iconRes != null ? iconRes : getDefaultSize(STORE_ICONS_SIZE_TYPE);
		}

		public static String generateSizeString() {
			String iconRes = mIconSizes.get(context.getResources().getDisplayMetrics().densityDpi);
			return iconRes != null ? iconRes : getDefaultSize(ICONS_SIZE_TYPE);
		}

		public static String generateSizeStringAvatar() {
			if (context == null) {
				return "";
			}
			float densityMultiplier = densityMultiplier();

			int size = Math.round(baseLineAvatar * densityMultiplier);

			//Log.d("Aptoide-IconSize", "Size is " + size);

			return size + "x" + size;
		}

		public static String generateSizeStringScreenshots(String orient) {
			if (context == null) {
				return "";
			}
			boolean isPortrait = orient != null && orient.equals("portrait");
			int dpi = ScreenU.getDensityDpi();
			return getThumbnailSize(dpi, isPortrait);
		}

		private static String getThumbnailSize(int density, boolean isPortrait) {
			if (!isPortrait) {
				if (density >= 640) {
					return "1024x640";
				} else if (density >= 480) {
					return "768x480";
				} else if (density >= 320) {
					return "512x320";
				} else if (density >= 240) {
					return "384x240";
				} else if (density >= 213) {
					return "340x213";
				} else if (density >= 160) {
					return "256x160";
				} else {
					return "192x120";
				}
			} else {
				if (density >= 640) {
					return "384x640";
				} else if (density >= 480) {
					return "288x480";
				} else if (density >= 320) {
					return "192x320";
				} else if (density >= 240) {
					return "144x240";
				} else if (density >= 213) {
					return "127x213";
				} else if (density >= 160) {
					return "96x160";
				} else {
					return "72x120";
				}
			}
		}

		private static Float densityMultiplier() {
			if (context == null) {
				return 0f;
			}

			float densityMultiplier = context.getResources().getDisplayMetrics().density;

			if (densityMultiplier <= 0.75f) {
				densityMultiplier = 0.75f;
			} else if (densityMultiplier <= 1) {
				densityMultiplier = 1f;
			} else if (densityMultiplier <= 1.333f) {
				densityMultiplier = 1.3312500f;
			} else if (densityMultiplier <= 1.5f) {
				densityMultiplier = 1.5f;
			} else if (densityMultiplier <= 2f) {
				densityMultiplier = 2f;
			} else if (densityMultiplier <= 3f) {
				densityMultiplier = 3f;
			} else {
				densityMultiplier = 4f;
			}
			return densityMultiplier;
		}

		public static String getDefaultSize(int varType) {

			switch (varType) {
				case STORE_ICONS_SIZE_TYPE:
					if (ScreenU.getDensityDpi() < DisplayMetrics.DENSITY_HIGH) {
						return mStoreIconSizes.get(DisplayMetrics.DENSITY_LOW);
					} else {
						return mStoreIconSizes.get(DisplayMetrics.DENSITY_XXXHIGH);
					}
				case ICONS_SIZE_TYPE:
					if (ScreenU.getDensityDpi() < DisplayMetrics.DENSITY_HIGH) {
						return mIconSizes.get(DisplayMetrics.DENSITY_LOW);
					} else {
						return mIconSizes.get(DisplayMetrics.DENSITY_XXXHIGH);
					}
			}
			return null;
		}

		/**
		 * On v7 webservices there is no attribute of HD icon. <br />Instead,
		 * the logic is that if the filename ends with <b>_icon</b> it is an HD icon.
		 *
		 * @param iconUrl The String with the URL of the icon
		 * @return A String with
		 */
		public static String parseIcon(String iconUrl) {
			try {
				if (iconUrl.contains("_icon")) {
					String sizeString = IconSizeU.generateSizeString();
					if (sizeString != null && !sizeString.isEmpty()) {
						String[] splittedUrl = iconUrl.split("\\.(?=[^\\.]+$)");
						iconUrl = splittedUrl[0] + "_" + sizeString + "." + splittedUrl[1];
					}
				}
			} catch (Exception e) {
				Logger.printException(e);
			}
			return iconUrl;
		}
	}

}