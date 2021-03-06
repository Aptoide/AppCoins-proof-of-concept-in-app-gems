package cm.aptoide.pt.view.store;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import cm.aptoide.pt.R;
import cm.aptoide.pt.dataprovider.model.v7.store.Store;
import cm.aptoide.pt.view.recycler.displayable.Displayable;
import cm.aptoide.pt.view.recycler.widget.Widget;
import java.util.List;

/**
 * Created by trinkes on 07/02/2017.
 */

public abstract class MetaStoresBaseWidget<T extends Displayable> extends Widget<T> {

  protected MetaStoresBaseWidget(@NonNull View itemView) {
    super(itemView);
  }

  protected void setupSocialLinks(List<Store.SocialChannel> socialChannels,
      LinearLayout socialChannelsLayout) {
    socialChannelsLayout.removeAllViews();
    LayoutInflater layoutInflater = getContext().getLayoutInflater();
    ImageButton imageButton;
    for (int i = 0; i < 1 && i < socialChannels.size(); i++) {
      Store.SocialChannel socialChannel = socialChannels.get(i);
      if (socialChannel.getType() != null) {
        layoutInflater.inflate(R.layout.social_button_layout, socialChannelsLayout);
        imageButton = ((ImageButton) socialChannelsLayout.getChildAt(i));
        switch (socialChannel.getType()) {
          case FACEBOOK:
            imageButton.setImageDrawable(getDrawable(R.drawable.facebook_logo));
            break;
          case TWITTER:
            imageButton.setImageDrawable(getDrawable(R.drawable.twitter_logo));
            break;
          case YOUTUBE:
            imageButton.setImageDrawable(getDrawable(R.drawable.youtube_logo));
            break;
          case TWITCH:
            imageButton.setImageDrawable(getDrawable(R.drawable.twitch_logo));
            break;
        }
        imageButton.setOnClickListener(view -> sendEvent(socialChannel.getUrl()));
      }
    }
  }

  private Drawable getDrawable(@DrawableRes int drawable) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      return getContext().getDrawable(drawable);
    } else {
      return getContext().getResources()
          .getDrawable(drawable);
    }
  }

  public void sendEvent(String url) {
    if (!TextUtils.isEmpty(url)) {
      Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.setData(Uri.parse(url));
      getContext().startActivity(intent);
    }
  }
}
