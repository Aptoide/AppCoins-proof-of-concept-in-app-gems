/*
 * Copyright (c) 2016.
 * Modified by SithEngineer on 12/05/2016.
 */

package cm.aptoide.pt.v8engine.fragment;

import android.support.annotation.NonNull;

import cm.aptoide.pt.v8engine.R;
import cm.aptoide.pt.v8engine.interfaces.ReloadInterface;
import cm.aptoide.pt.v8engine.layouthandler.LoaderLayoutHandler;
import cm.aptoide.pt.v8engine.layouthandler.SwipeLoaderLayoutHandler;

/**
 * Created by neuro on 05-05-2016.
 */
public abstract class GridRecyclerSwipeFragment extends GridRecyclerFragment implements
		ReloadInterface {

	@NonNull
	@Override
	protected LoaderLayoutHandler createLoaderLayoutHandler() {
		return new SwipeLoaderLayoutHandler(getViewToShowAfterLoadingId(), this);
	}

	@Override
	public void reload() {
		load(true);
	}

	@Override
	public int getContentViewId() {
		return R.layout.recycler_swipe_fragment;
	}
}