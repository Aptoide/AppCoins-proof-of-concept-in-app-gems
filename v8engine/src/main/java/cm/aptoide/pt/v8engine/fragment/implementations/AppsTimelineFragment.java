/*
 * Copyright (c) 2016.
 * Modified by Marcelo Benites on 29/06/2016.
 */

package cm.aptoide.pt.v8engine.fragment.implementations;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.trello.rxlifecycle.FragmentEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;

import cm.aptoide.pt.dataprovider.PackageRepository;
import cm.aptoide.pt.dataprovider.util.ErrorUtils;
import cm.aptoide.pt.dataprovider.ws.v7.GetUserTimelineRequest;
import cm.aptoide.pt.model.v7.Datalist;
import cm.aptoide.pt.model.v7.listapp.App;
import cm.aptoide.pt.model.v7.timeline.Article;
import cm.aptoide.pt.model.v7.timeline.Feature;
import cm.aptoide.pt.model.v7.timeline.GetUserTimeline;
import cm.aptoide.pt.model.v7.timeline.StoreLatestApps;
import cm.aptoide.pt.model.v7.timeline.TimelineItem;
import cm.aptoide.pt.v8engine.R;
import cm.aptoide.pt.v8engine.fragment.GridRecyclerSwipeFragment;
import cm.aptoide.pt.v8engine.view.recycler.displayable.Displayable;
import cm.aptoide.pt.v8engine.view.recycler.displayable.SpannableFactory;
import cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.ProgressBarDisplayable;
import cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.grid.AppUpdateDisplayable;
import cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.grid.ArticleDisplayable;
import cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.grid.DateCalculator;
import cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.grid.FeatureDisplayable;
import cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.grid.StoreLatestAppsDisplayable;
import cm.aptoide.pt.v8engine.view.recycler.listeners.RxEndlessRecyclerView;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by marcelobenites on 6/17/16.
 */
public class AppsTimelineFragment extends GridRecyclerSwipeFragment {

	public static final int SEARCH_LIMIT = 7;
	private SpannableFactory spannableFactory;
	private DateCalculator dateCalculator;
	private boolean loading;
	private int offset;
	private Subscription subscription;
	private PackageRepository packageRespository;
	private List<String> packages;
	private Observable<String> latestInstalledAppsObservable;

	public static AppsTimelineFragment newInstance() {
		AppsTimelineFragment fragment = new AppsTimelineFragment();
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dateCalculator = new DateCalculator();
		spannableFactory = new SpannableFactory();
		packageRespository = new PackageRepository(getContext().getPackageManager());
		latestInstalledAppsObservable = packageRespository.getLatestInstalledPackages(5).cache();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (getAdapter().getItemCount() > 0 && (subscription == null || subscription.isUnsubscribed()) && packages != null) {
			subscription = getUpdateTimelineSubscription(getLoadMoreObservable(packages));
		}
	}

	@Override
	public void load(boolean refresh) {
		if (subscription != null) {
			subscription.unsubscribe();
		}
		subscription = getUpdateTimelineSubscription(getPackagesObservable(latestInstalledAppsObservable)
				.flatMap(packages -> Observable.concat(getFreshLoadObservable(refresh, packages), getLoadMoreObservable(packages))));
	}

	@NonNull
	private Observable<List<String>> getPackagesObservable(Observable<String> latestInstalledAppsObservable) {
		return Observable.concat(latestInstalledAppsObservable, packageRespository.getRandomInstalledPackages(5))
				.toList()
				.doOnNext(packages -> setPackages(packages));
	}

	@NonNull
	private Observable<GetUserTimeline> getFreshLoadObservable(boolean refresh, List<String> packages) {
		return GetUserTimelineRequest.of(SEARCH_LIMIT, 0, packages)
				.observe(refresh)
				.doOnNext(item -> getAdapter().clearDisplayables());
	}

	private Subscription getUpdateTimelineSubscription(Observable<GetUserTimeline> timelineUpdateSource) {
		return timelineUpdateSource
				.<GetUserTimeline> compose(bindUntilEvent(FragmentEvent.PAUSE))
				.filter(item -> item.getDatalist() != null)
				.doOnNext(item -> setOffset(item.getDatalist()))
				.flatMapIterable(getUserTimeline -> getUserTimeline.getDatalist().getList())
				.filter(timelineItem -> timelineItem != null)
				.map(timelineItem -> timelineItem.getData())
				.filter(item -> (item instanceof Article || item instanceof Feature || item instanceof StoreLatestApps || item instanceof App))
				.map(item -> itemToDisplayable(item, dateCalculator, spannableFactory))
				.buffer(1, TimeUnit.SECONDS, SEARCH_LIMIT)
				.observeOn(AndroidSchedulers.mainThread())
				.doOnNext(item -> removeLoading())
				.subscribe(displayables -> addDisplayables(displayables), throwable -> finishLoading((Throwable) throwable));
	}

	private Observable<GetUserTimeline> getLoadMoreObservable(List<String> packages) {
		return RxEndlessRecyclerView.loadMore(recyclerView, getAdapter())
				.filter(item -> !isLoading())
				.doOnNext(item -> addLoading())
				.concatMap(item -> GetUserTimelineRequest.of(SEARCH_LIMIT, offset, packages).observe())
				.delay(1, TimeUnit.SECONDS)
				.retryWhen(errors -> errors
						.delay(1, TimeUnit.SECONDS)
						.observeOn(AndroidSchedulers.mainThread())
						.doOnNext(error -> showErrorSnackbar(error)))
				.subscribeOn(AndroidSchedulers.mainThread());
	}

	private void showErrorSnackbar(Throwable error) {
		removeLoading();
		@StringRes int errorString;
		if (ErrorUtils.isNoNetworkConnection(error)) {
			errorString = R.string.fragment_social_timeline_no_connection;
		} else {
			errorString = R.string.fragment_social_timeline_general_error;
		}
		Snackbar.make(getView(), errorString, Snackbar.LENGTH_SHORT).show();
	}

	private void setOffset(Datalist<TimelineItem> datalist) {
		offset = datalist.getNext();
	}

	private void addLoading() {
		this.loading = true;
		adapter.addDisplayable(new ProgressBarDisplayable());
	}

	private void removeLoading() {
		if (loading) {
			this.loading = false;
			adapter.popDisplayable();
		}
	}

	private boolean isLoading() {
		return loading;
	}

	@NonNull
	private Displayable itemToDisplayable(Object item, DateCalculator dateCalculator, SpannableFactory
			spannableFactory) {

		if (item instanceof Article) {
			return ArticleDisplayable.from((Article) item, dateCalculator, spannableFactory);
		} else if (item instanceof Feature) {
			return FeatureDisplayable.from((Feature) item, dateCalculator, spannableFactory);
		} else if (item instanceof StoreLatestApps) {
			return StoreLatestAppsDisplayable.from((StoreLatestApps) item, dateCalculator);
		} else if (item instanceof App) {
			return AppUpdateDisplayable.fromApp((App) item, spannableFactory);
		}
		throw new IllegalArgumentException("Only articles, features, store latest apps and app updates supported.");
	}

	public void setPackages(List<String> packages) {
		this.packages = packages;
	}
}