package cm.aptoide.pt.social.data.share;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import cm.aptoide.accountmanager.Account;
import cm.aptoide.pt.R;
import cm.aptoide.pt.networking.image.ImageLoader;
import cm.aptoide.pt.social.data.AppUpdate;
import cm.aptoide.pt.view.recycler.displayable.SpannableFactory;
import cm.aptoide.pt.view.rx.RxAlertDialog;

class AppUpdatePostShareDialog extends BaseShareDialog<AppUpdate> {

  @LayoutRes static final int LAYOUT_ID = R.layout.timeline_recommendation_preview;

  AppUpdatePostShareDialog(RxAlertDialog dialog) {
    super(dialog);
  }

  void setupView(View view, AppUpdate post) {
    final Context context = view.getContext();
    ImageView appIcon =
        (ImageView) view.findViewById(R.id.displayable_social_timeline_recommendation_icon);
    TextView appName =
        (TextView) view.findViewById(R.id.displayable_social_timeline_recommendation_similar_apps);
    TextView getApp = (TextView) view.findViewById(
        R.id.displayable_social_timeline_recommendation_get_app_button);
    RatingBar ratingBar = (RatingBar) view.findViewById(R.id.rating_bar);

    ImageLoader.with(context)
        .load(post.getAppUpdateIcon(), appIcon);
    appName.setText(post.getAppUpdateName());
    ratingBar.setRating(post.getAppUpdateAverageRating());

    SpannableFactory spannableFactory = new SpannableFactory();

    getApp.setText(spannableFactory.createColorSpan(
        context.getString(R.string.displayable_social_timeline_article_get_app_button, ""),
        ContextCompat.getColor(context, R.color.appstimeline_grey), ""));
  }

  public static class Builder extends BaseShareDialog.Builder {

    public Builder(Context context, SharePostViewSetup sharePostViewSetup, Account account) {
      super(context, sharePostViewSetup, account, LAYOUT_ID);
    }

    public AppUpdatePostShareDialog build() {
      return new AppUpdatePostShareDialog(buildRxAlertDialog());
    }
  }
}
