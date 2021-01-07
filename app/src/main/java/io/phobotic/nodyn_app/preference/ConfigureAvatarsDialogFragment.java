/*
 * Copyright (c) 2019 Jonathan Nelson <ciasaboark@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.phobotic.nodyn_app.preference;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MotionEventCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.avatar.AdorableAvatarProvider;
import io.phobotic.nodyn_app.avatar.AvatarProvider;
import io.phobotic.nodyn_app.avatar.BackendAvatarProvider;
import io.phobotic.nodyn_app.avatar.GravitarProvider;
import io.phobotic.nodyn_app.avatar.TinyGraphAvatarProvider;
import io.phobotic.nodyn_app.view.AvatarProviderView;

/**
 * Created by Jonathan Nelson on 3/21/18.
 */

public class ConfigureAvatarsDialogFragment extends DialogFragment {
    private static final String TAG = ConfigureAvatarsDialogFragment.class.getSimpleName();
    private static final String ARG_COLUMN_COUNT = "column_count";
    private View rootView;
    private int columnCount = 1;
    private RecyclerView list;
    private List<SelectableProvider> selectableProviders;
    private TouchCallback callback;
    private ItemTouchHelper touchHelper;

    public static ConfigureAvatarsDialogFragment newInstance(int columnCount) {
        ConfigureAvatarsDialogFragment fragment = new ConfigureAvatarsDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    public ConfigureAvatarsDialogFragment() {
        //required empty constructor
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        rootView = View.inflate(getContext(), R.layout.fragment_configure_avatars, null);

        if (getArguments() != null) {
            columnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        init();

        AlertDialog.Builder b = new MaterialAlertDialogBuilder(getContext(), R.style.Widgets_Dialog)
                .setIcon(R.drawable.ic_face_white_48dp)
                .setTitle(getString(R.string.user_configure_avatars_title))
                .setView(rootView)
                .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return b.create();
    }

    private void init() {
        findViews();
        buildProvierList();
        initList();
    }

    private void findViews() {
        list = rootView.findViewById(R.id.list);
    }

    private void buildProvierList() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        String enabledProviersString = prefs.getString(getString(R.string.user_avatars_enabled_providers),
                "");
        List<String> enabledProviders = Arrays.asList(enabledProviersString.split(","));

        List<AvatarProvider> avatarProviders = new ArrayList<>();

        for (String className : enabledProviders) {
            try {
                Class<?> clazz = Class.forName(className);
                Constructor<?> ctor = clazz.getConstructor();
                Object object = ctor.newInstance();
                avatarProviders.add((AvatarProvider) object);
            } catch (Exception e) {
                Crashlytics.logException(e);
                e.printStackTrace();
            }
        }

        //after we add all the enabled providers we need to add any missing providers from the default list
        List<AvatarProvider> defaultProviders = getDefaultProviders();
        for (AvatarProvider provider : defaultProviders) {
            if (!avatarProviders.contains(provider)) {
                avatarProviders.add(provider);
            }
        }

        //convert the list to a selectable list
        selectableProviders = new ArrayList<>();
        for (AvatarProvider provider : avatarProviders) {
            SelectableProvider selectableProvider = new SelectableProvider(provider);
            selectableProviders.add(selectableProvider);
            String providerClass = provider.getClass().getCanonicalName();
            if (enabledProviders.contains(providerClass)) {
                selectableProvider.setChecked(true);
            }
        }
    }

    private void initList() {
        Context context = rootView.getContext();
        if (columnCount <= 1) {
            LinearLayoutManager lm = new LinearLayoutManager(context);
            lm.setOrientation(RecyclerView.VERTICAL);
            list.setLayoutManager(lm);

        } else {
            list.setLayoutManager(new GridLayoutManager(context, columnCount));
        }

        AvatarProviderRecyclerViewAdapter adapter = new AvatarProviderRecyclerViewAdapter(selectableProviders);
        list.setAdapter(adapter);

        callback = new TouchCallback(adapter);
        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(list);
    }

    private List<AvatarProvider> getDefaultProviders() {
        List<AvatarProvider> defaultProviders = new ArrayList<>();
        defaultProviders.add(new BackendAvatarProvider());
        defaultProviders.add(new GravitarProvider());
        defaultProviders.add(new TinyGraphAvatarProvider());
        defaultProviders.add(new AdorableAvatarProvider());

        return defaultProviders;
    }

    private void onSelectionChanged() {
        for (SelectableProvider selectableProvider : selectableProviders) {
            Log.d(TAG, selectableProvider.getProvider().getName() + " is " +
                    (selectableProvider.isChecked() ? "enabled" : "disabled"));
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        StringBuilder enabledProviders = new StringBuilder();
        String prefix = "";
        for (SelectableProvider selectableProvider : selectableProviders) {
            String providerClass = selectableProvider.getProvider().getClass().getCanonicalName();
            if (selectableProvider.isChecked()) {
                enabledProviders.append(prefix).append(providerClass);
                prefix = ",";
            }
        }

        prefs.edit().putString(getString(R.string.user_avatars_enabled_providers),
                enabledProviders.toString()).commit();
    }

    public interface ItemTouchHelperAdapter {

        void onItemMove(int fromPosition, int toPosition);

        void onItemDismiss(int position);
    }


    public interface OnStartDragListener {

        /**
         * Called when a view is requesting a start of a drag.
         *
         * @param viewHolder The holder of the view to drag.
         */
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    private class AvatarProviderRecyclerViewAdapter extends
            RecyclerView.Adapter<AvatarProviderRecyclerViewAdapter.ViewHolder>
            implements ItemTouchHelperAdapter, OnStartDragListener {
        private final List<SelectableProvider> items;

        public AvatarProviderRecyclerViewAdapter(List<SelectableProvider> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            AvatarProviderView view = new AvatarProviderView(parent.getContext(), null);
            // manually set the CustomView's size
            view.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            SelectableProvider selectableProvider = items.get(position);
            holder.provider = selectableProvider;
            holder.view.setAvatarProvider(selectableProvider.getProvider());
            holder.checkbox.setChecked(selectableProvider.isChecked());

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.provider.setChecked(!holder.provider.isChecked());
                    holder.checkbox.setChecked(holder.provider.isChecked());
                    onSelectionChanged();
                }
            });

            holder.handle.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) ==
                            MotionEvent.ACTION_DOWN) {
                        onStartDrag(holder);
                    }
                    return false;
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
            touchHelper.startDrag(viewHolder);
        }

        @Override
        public void onItemMove(int fromPosition, int toPosition) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(items, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(items, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
            onSelectionChanged();
        }

        @Override
        public void onItemDismiss(int position) {

        }

        public class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {
            private CheckBox checkbox;
            private ImageView handle;
            private AvatarProviderView view;
            private SelectableProvider provider;


            public ViewHolder(AvatarProviderView view) {
                super(view);
                this.view = view;
                this.checkbox = view.findViewById(R.id.checkbox);
                this.handle = view.findViewById(R.id.drag_handle);
            }

            public void bind(SelectableProvider provider) {
                this.provider = provider;
                this.checkbox.setChecked(provider.isChecked());
                this.checkbox.setOnCheckedChangeListener(this);
            }

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                this.provider.setChecked(isChecked);
                onSelectionChanged();
            }
        }


    }

    public class SelectableProvider {
        private AvatarProvider provider;
        private boolean checked;
        private boolean enabled = true;

        public SelectableProvider(AvatarProvider provider) {
            this.provider = provider;
        }

        public boolean isChecked() {
            return checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public AvatarProvider getProvider() {
            return provider;
        }
    }

    private class TouchCallback extends ItemTouchHelper.Callback {
        private final ItemTouchHelperAdapter adapter;

        public TouchCallback(ItemTouchHelperAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) {
            adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        }
    }
}
