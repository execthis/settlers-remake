/*
 * Copyright (c) 2017
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package jsettlers.main.android.mainmenu.mappicker;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.Semaphore;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import jsettlers.common.menu.IMapDefinition;
import jsettlers.graphics.localization.Labels;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.main.android.R;
import jsettlers.main.android.core.resources.PreviewImageConverter;
import jsettlers.main.android.core.ui.FragmentUtil;
import jsettlers.main.android.core.ui.NoChangeItemAnimator;

/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_map_picker)
public abstract class MapPickerFragment extends Fragment {
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(Labels.getString("date.date-only"), Locale.getDefault());

	@ViewById(R.id.recycler_view)
	RecyclerView recyclerView;
	@ViewById(R.id.toolbar)
	Toolbar toolbar;

	private MapPickerViewModel viewModel;
	private MapAdapter adapter;

	protected abstract MapPickerViewModel createViewModel();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewModel = createViewModel();
	}

	@AfterViews
	void setupToolbar() {
		FragmentUtil.setActionBar(this, toolbar);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		viewModel.getMaps().observe(this, maps -> {
			if (adapter == null) {
				adapter = new MapAdapter(maps);
			}

			if (recyclerView.getAdapter() == null) {
				recyclerView.setHasFixedSize(true);
				recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
				recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity()).build());
				recyclerView.setItemAnimator(new NoChangeItemAnimator());
				recyclerView.setAdapter(adapter);
			}

			adapter.setItems(maps);
		});
	}

	/**
	 * Subclass related methods
	 */
	protected boolean showMapDates() {
		return false;
	}

	/**
	 * RecyclerView Adapter for displaying list of maps
	 */
	private class MapAdapter extends RecyclerView.Adapter<MapAdapter.MapHolder> {
		private MapLoader[] maps;
		private final Semaphore limitImageLoadingSemaphore = new Semaphore(3, true);

		private View.OnClickListener itemClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				RecyclerView.ViewHolder viewHolder = recyclerView.findContainingViewHolder(v);
				if (viewHolder != null) {
					int position = viewHolder.getAdapterPosition();
					MapLoader map = maps[position];
					viewModel.selectMap(map);
				}
			}
		};

		public MapAdapter(MapLoader[] maps) {
			this.maps = maps;
		}

		@Override
		public int getItemCount() {
			return maps.length;
		}

		@Override
		public MapHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			final View itemView = getActivity().getLayoutInflater().inflate(R.layout.item_map, parent, false);
			final MapHolder mapHolder = new MapHolder(itemView);
			itemView.setOnClickListener(itemClickListener);
			return mapHolder;
		}

		@Override
		public void onBindViewHolder(MapHolder holder, int position) {
			IMapDefinition map = maps[position];
			holder.bind(map);
		}

		void setItems(MapLoader[] maps) {
			this.maps = maps;
			notifyDataSetChanged();
		}

		class MapHolder extends RecyclerView.ViewHolder {
			final TextView nameTextView;
			final TextView dateTextView;
			final TextView playerCountTextView;
			final ImageView mapPreviewImageView;

			Disposable subscription;

			public MapHolder(View itemView) {
				super(itemView);
				nameTextView = itemView.findViewById(R.id.text_view_name);
				dateTextView = itemView.findViewById(R.id.text_view_date);
				playerCountTextView = itemView.findViewById(R.id.text_view_player_count);
				mapPreviewImageView = itemView.findViewById(R.id.image_view_map_preview);

				if (showMapDates()) {
					dateTextView.setVisibility(View.VISIBLE);
				}
			}

			public void bind(IMapDefinition mapDefinition) {
				mapPreviewImageView.setImageDrawable(null);
				nameTextView.setText(mapDefinition.getMapName());
				playerCountTextView.setText(mapDefinition.getMinPlayers() + "-" + mapDefinition.getMaxPlayers());

				if (showMapDates()) {
					dateTextView.setText(dateFormat.format(mapDefinition.getCreationDate()));
				}

				if (subscription != null) {
					subscription.dispose();
				}

				subscription = PreviewImageConverter.toBitmap(mapDefinition.getImage(), limitImageLoadingSemaphore)
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribeWith(new DisposableSingleObserver<Bitmap>() {
							@Override
							public void onSuccess(Bitmap bitmap) {
								mapPreviewImageView.setImageBitmap(bitmap);
							}

							@Override
							public void onError(Throwable e) {
								mapPreviewImageView.setImageDrawable(null);
							}
						});
			}
		}
	}
}
