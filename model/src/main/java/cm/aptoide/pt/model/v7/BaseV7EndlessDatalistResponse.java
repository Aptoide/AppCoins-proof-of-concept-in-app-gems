/*
 * Copyright (c) 2016.
 * Modified by SithEngineer on 18/08/2016.
 */

package cm.aptoide.pt.model.v7;

import lombok.Data;

/**
 * Created by sithengineer on 18/08/16.
 */
@Data
public class BaseV7EndlessDatalistResponse<T> extends BaseV7EndlessResponse {

	private Datalist<T> datalist;

	@Override
	public int getCurrentSize() {
		return hasData() ? datalist.getList().size() : 0;
	}

	@Override
	public int getNextSize() {
		return hasData() ? NEXT_STEP : 0;
	}

	@Override
	public boolean hasData() {
		return datalist != null && datalist.getList() != null;
	}
}