package cm.aptoide.pt.v8engine.view.recycler.displayable.implementations.grid;

import java.util.Date;

import cm.aptoide.pt.model.v7.Type;
import cm.aptoide.pt.model.v7.timeline.Article;
import cm.aptoide.pt.model.v7.timeline.ArticleTimelineItem;
import cm.aptoide.pt.model.v7.timeline.TimelineItem;
import cm.aptoide.pt.v8engine.R;
import cm.aptoide.pt.v8engine.view.recycler.displayable.Displayable;
import cm.aptoide.pt.v8engine.view.recycler.displayable.DisplayablePojo;

/**
 * Created by marcelobenites on 6/17/16.
 */
public class ArticleDisplayable extends Displayable {

	private Article article;

	public ArticleDisplayable() {
	}

	public ArticleDisplayable(Article article) {
		this.article = article;
	}

	public String getTitle() {
		return article.getTitle();
	}

	public String getUrl() {
		return article.getUrl();
	}

	public String getPublisher() {
		return article.getPublisher();
	}

	public String getThumbnailUrl() {
		return article.getThumbnailUrl();
	}

	public int getHoursSinceLastUpdate(Date currentDate) {
		long interval = currentDate.getTime() - article.getDate().getTime();
		return (int)(interval/(1000 * 60 * 60));
	}

	@Override
	public Type getType() {
		return Type.SOCIAL_TIMELINE;
	}

	@Override
	public int getViewLayout() {
		return R.layout.displayable_social_timeline_article;
	}
}