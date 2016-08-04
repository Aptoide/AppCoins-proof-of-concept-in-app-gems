/*
 * Copyright (c) 2016.
 * Modified by SithEngineer on 29/07/2016.
 */

package cm.aptoide.pt.v8engine.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trello.rxlifecycle.components.support.RxFragment;

import cm.aptoide.pt.actions.PermissionRequest;
import cm.aptoide.pt.database.Database;
import cm.aptoide.pt.v8engine.interfaces.Lifecycle;
import io.realm.Realm;
import rx.functions.Action0;

/**
 * Created by neuro on 14-04-2016.
 */
public abstract class SupportV4BaseFragment extends RxFragment implements Lifecycle, PermissionRequest {

	private final String TAG = getClass().getSimpleName();
	protected Realm realm;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			loadExtras(getArguments());
		}
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		bindViews(view);
		setupViews();
	}

	@Override
	public void onDestroyView() {

		realm.close();
		realm = null;

		super.onDestroyView();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		realm = Database.get(getActivity());

		return inflater.inflate(getContentViewId(), container, false);
	}

	/**
	 * Called after onCreate. This is where arguments should be loaded.
	 *
	 * @param args {@link #getArguments()}
	 */
	@Override
	public void loadExtras(Bundle args) {
		// optional method
	}

	/**
	 * Setup previously binded views.
	 */
	public abstract void setupViews();

	@Override
	public void setupToolbar() {
		// optional method
	}

	@TargetApi(Build.VERSION_CODES.M)
	public void requestAccessToExternalFileSystem(@Nullable Action0 toRunWhenAccessIsGranted, @Nullable Action0 toRunWhenAccessIsDenied) {
		try {
			((PermissionRequest) this.getActivity()).requestAccessToExternalFileSystem(toRunWhenAccessIsGranted, toRunWhenAccessIsDenied);
		} catch (ClassCastException e) {
			throw new IllegalStateException("Containing activity of this fragment must implement " + PermissionRequest.class.getName());
		}
	}

	@TargetApi(Build.VERSION_CODES.M)
	public void requestAccessToExternalFileSystem(boolean forceShowRationale, @Nullable Action0 toRunWhenAccessIsGranted, @Nullable Action0
			toRunWhenAccessIsDenied) {
		try {
			((PermissionRequest) this.getActivity()).requestAccessToExternalFileSystem(forceShowRationale, toRunWhenAccessIsGranted, toRunWhenAccessIsDenied);
		} catch (ClassCastException e) {
			throw new IllegalStateException("Containing activity of this fragment must implement " + PermissionRequest.class.getName());
		}
	}

	@TargetApi(Build.VERSION_CODES.M)
	public void requestAccessToAccounts(@Nullable Action0 toRunWhenAccessIsGranted, @Nullable Action0 toRunWhenAccessIsDenied) {
		try {
			((PermissionRequest) this.getActivity()).requestAccessToAccounts(toRunWhenAccessIsGranted, toRunWhenAccessIsDenied);
		} catch (ClassCastException e) {
			throw new IllegalStateException("Containing activity of this fragment must implement " + PermissionRequest.class.getName());
		}
	}

	@TargetApi(Build.VERSION_CODES.M)
	public void requestAccessToAccounts(boolean forceShowRationale, @Nullable Action0 toRunWhenAccessIsGranted, @Nullable Action0 toRunWhenAccessIsDenied) {
		try {
			((PermissionRequest) this.getActivity()).requestAccessToAccounts(forceShowRationale, toRunWhenAccessIsGranted, toRunWhenAccessIsDenied);
		} catch (ClassCastException e) {
			throw new IllegalStateException("Containing activity of this fragment must implement " + PermissionRequest.class.getName());
		}
	}
}