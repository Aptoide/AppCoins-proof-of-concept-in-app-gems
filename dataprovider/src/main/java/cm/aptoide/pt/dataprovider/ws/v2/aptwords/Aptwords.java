package cm.aptoide.pt.dataprovider.ws.v2.aptwords;

import java.util.HashMap;

import cm.aptoide.pt.dataprovider.core.WebService;
import cm.aptoide.pt.model.v2.GetAdsResponse;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by neuro on 22-03-2016.
 */
abstract class Aptwords<U> extends WebService<Aptwords.Interfaces, U> {

	protected Aptwords() {
		super(Interfaces.class);
	}

	@Override
	protected String getBaseHost() {
		return "http://webservices.aptwords.net";
	}

	interface Interfaces {

		@POST("/api/2/getAds")
		@FormUrlEncoded
		Observable<GetAdsResponse> getAds(@FieldMap HashMap<String, String> arg);
	}
}
