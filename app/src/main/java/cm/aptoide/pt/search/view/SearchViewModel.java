package cm.aptoide.pt.search.view;

import org.parceler.Parcel;

@Parcel class SearchViewModel implements SearchView.Model {
  String currentQuery;
  String storeName;
  boolean onlyTrustedApps;
  boolean allStoresSelected;
  int allStoresOffset = 0;
  int followedStoresOffset = 0;
  boolean reachedBottomAllStores = false;
  boolean reachedBottomFollowedStores = false;
  boolean loadedAds = false;

  SearchViewModel() {
  }

  SearchViewModel(String currentQuery, String storeName, boolean onlyTrustedApps,
      boolean allStoresSelected) {
    this.currentQuery = currentQuery;
    this.storeName = storeName;
    this.onlyTrustedApps = onlyTrustedApps;
    this.allStoresSelected = allStoresSelected;
  }

  SearchViewModel(String currentQuery, String storeName, boolean onlyTrustedApps) {
    this(currentQuery, storeName, onlyTrustedApps, true);
  }

  SearchViewModel(String currentQuery, boolean onlyTrustedApps) {
    this(currentQuery, null, onlyTrustedApps, true);
  }

  SearchViewModel(String currentQuery, String storeName) {
    this(currentQuery, storeName, true, true);
  }

  @Override public String getCurrentQuery() {
    return currentQuery;
  }

  @Override public String getStoreName() {
    return storeName;
  }

  @Override public boolean isOnlyTrustedApps() {
    return onlyTrustedApps;
  }

  @Override public boolean isAllStoresSelected() {
    return allStoresSelected;
  }

  public void setAllStoresSelected(boolean allStoresSelected) {
    this.allStoresSelected = allStoresSelected;
  }

  public int getAllStoresOffset() {
    return allStoresOffset;
  }

  public int getFollowedStoresOffset() {
    return followedStoresOffset;
  }

  public boolean hasReachedBottomOfAllStores() {
    return reachedBottomAllStores;
  }

  public boolean hasReachedBottomOfFollowedStores() {
    return reachedBottomFollowedStores;
  }

  public void incrementOffsetAndCheckIfReachedBottomOfFollowedStores(int offset) {
    this.followedStoresOffset += offset;
    if (offset == 0) {
      reachedBottomFollowedStores = true;
    }
  }

  public void incrementOffsetAndCheckIfReachedBottomOfAllStores(int offset) {
    this.allStoresOffset += offset;
    if (offset == 0) {
      reachedBottomAllStores = true;
    }
  }

  @Override public boolean hasLoadedAds() {
    return loadedAds;
  }

  @Override public void setHasLoadedAds() {
    loadedAds = true;
  }
}
