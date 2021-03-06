/*
 * Copyright 2013 Tim Roes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.luciocossio.android.enhancedlistdemo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.luciocossio.android.enhancedlist.EnhancedList;
import com.luciocossio.android.enhancedlist.EnhancedListView;
import com.luciocossio.android.enhancedlist.EnhancedRecyclerListView;
import com.luciocossio.android.enhancedlist.SwipeDirection;
import com.luciocossio.android.enhancedlist.UndoStyle;
import com.luciocossio.android.enhancedlist.Undoable;

import java.util.ArrayList;
import java.util.List;

public class MainActivityRecycler extends ActionBarActivity {

    private enum ControlGroup {
        SWIPE_TO_DISMISS
    }

    private static final String PREF_UNDO_STYLE = "UNDO_STYLE";
    private static final String PREF_SWIPE_TO_DISMISS = "SWIPE_TO_DISMISS";
    private static final String PREF_SWIPE_DIRECTION = "SWIPE_DIRECTION";
    private static final String PREF_SWIPE_LAYOUT = "SWIPE_LAYOUT";

    private EnhancedRecyclerAdapter adapter;

    private EnhancedRecyclerListView listView;
    private DrawerLayout drawerLayout;

    private Bundle undoStylePref;
    private Bundle swipeDirectionPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_recycler);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View view, float v) {
            }

            @Override
            public void onDrawerOpened(View view) {
                listView.discardUndo();
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View view) {
                supportInvalidateOptionsMenu();
                applySettings();
            }

            @Override
            public void onDrawerStateChanged(int i) {
            }

        });

        listView = (EnhancedRecyclerListView) findViewById(R.id.list);

        adapter = new EnhancedRecyclerAdapter();
        adapter.resetItems();

        listView.setAdapter(adapter);

        CheckBox swipeToDismiss = (CheckBox) findViewById(R.id.pref_swipetodismiss);
        swipeToDismiss.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getPreferences(MODE_PRIVATE).edit().putBoolean(PREF_SWIPE_TO_DISMISS, isChecked).commit();
                enableControlGroup(ControlGroup.SWIPE_TO_DISMISS, isChecked);
            }
        });
        swipeToDismiss.setChecked(getPreferences(MODE_PRIVATE).getBoolean(PREF_SWIPE_TO_DISMISS, false));

        CheckBox swipeLayout = (CheckBox) findViewById(R.id.pref_swipelayout);
        swipeLayout.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getPreferences(MODE_PRIVATE).edit().putBoolean(PREF_SWIPE_LAYOUT, isChecked).commit();
            }
        });
        swipeLayout.setChecked(getPreferences(MODE_PRIVATE).getBoolean(PREF_SWIPE_LAYOUT, false));

        undoStylePref = new Bundle();
        undoStylePref.putInt(DialogPicker.DIALOG_TITLE, R.string.pref_undo_style_title);
        undoStylePref.putInt(DialogPicker.DIALOG_ITEMS_ID, R.array.undo_style);
        undoStylePref.putString(DialogPicker.DIALOG_PREF_KEY, PREF_UNDO_STYLE);

        swipeDirectionPref = new Bundle();
        swipeDirectionPref.putInt(DialogPicker.DIALOG_TITLE, R.string.pref_swipe_direction_title);
        swipeDirectionPref.putInt(DialogPicker.DIALOG_ITEMS_ID, R.array.swipe_direction);
        swipeDirectionPref.putString(DialogPicker.DIALOG_PREF_KEY, PREF_SWIPE_DIRECTION);

        enableControlGroup(ControlGroup.SWIPE_TO_DISMISS, getPreferences(MODE_PRIVATE).getBoolean(PREF_SWIPE_TO_DISMISS, false));

        // Set the callback that handles dismisses.
        listView.setDismissCallback(new com.luciocossio.android.enhancedlist.OnDismissCallback() {
            /**
             * This method will be called when the user swiped a way or deleted it via
             * {@link com.luciocossio.android.enhancedlist.EnhancedListView#delete(int)}.
             *
             * @param listView The {@link EnhancedListView} the item has been deleted from.
             * @param position The position of the item to delete from your adapter.
             * @return An {@link com.luciocossio.android.enhancedlist.Undoable}, if you want
             *      to give the user the possibility to undo the deletion.
             */
            @Override
            public Undoable onDismiss(EnhancedList listView, final int position) {

                final String item = (String) adapter.getItem(position);
                adapter.remove(position);
                return new Undoable() {
                    @Override
                    public void undo() {
                        adapter.insert(position, item);
                    }

                    @Override
                    public void discard() {
                        //delete item permanently
                    }
                };
            }
        });

        listView.setSwipingLayout(R.id.swiping_layout);

        applySettings();

    }

    /**
     * Enables or disables a group of widgets in the settings drawer.
     *
     * @param group   The Group that should be disabled/enabled.
     * @param enabled Whether the group should be enabled or not.
     */
    private void enableControlGroup(ControlGroup group, boolean enabled) {
        switch (group) {
            case SWIPE_TO_DISMISS:
                findViewById(R.id.pref_swipedirection).setEnabled(enabled);
                findViewById(R.id.pref_swipelayout).setEnabled(enabled);
                break;
        }
    }

    /**
     * Applies the settings the user has made to the list view.
     */
    private void applySettings() {

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);

        // Set the UndoStyle, the user selected.
        UndoStyle style;
        switch (prefs.getInt(PREF_UNDO_STYLE, 0)) {
            default:
                style = UndoStyle.SINGLE_POPUP;
                break;
            case 1:
                style = UndoStyle.MULTILEVEL_POPUP;
                break;
            case 2:
                style = UndoStyle.COLLAPSED_POPUP;
                break;
        }
        listView.setUndoStyle(style);

        // Enable or disable Swipe to Dismiss
        if (prefs.getBoolean(PREF_SWIPE_TO_DISMISS, false)) {
            listView.enableSwipeToDismiss();

            // Set the swipe direction
            SwipeDirection direction;
            switch (prefs.getInt(PREF_SWIPE_DIRECTION, 0)) {
                default:
                    direction = SwipeDirection.BOTH;
                    break;
                case 1:
                    direction = SwipeDirection.START;
                    break;
                case 2:
                    direction = SwipeDirection.END;
                    break;
            }
            listView.setSwipeDirection(direction);

            // Enable or disable swiping layout feature
            listView.setSwipingLayout(prefs.getBoolean(PREF_SWIPE_LAYOUT, false)
                    ? R.id.swiping_layout : 0);

        } else {
            listView.disableSwipeToDismiss();
        }

    }

    @Override
    protected void onStop() {
        if (listView != null) {
            listView.discardUndo();
        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        boolean drawer = drawerLayout.isDrawerVisible(Gravity.RIGHT);

        menu.findItem(R.id.action_settings).setVisible(!drawer);
        menu.findItem(R.id.action_done).setVisible(drawer);

        return super.onPrepareOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                drawerLayout.openDrawer(Gravity.RIGHT);
                return true;
            case R.id.action_done:
                drawerLayout.closeDrawers();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void resetItems(View view) {
        listView.discardUndo();
        adapter.resetItems();
        drawerLayout.closeDrawers();
    }

    public void selectUndoStyle(View view) {
        DialogPicker picker = new DialogPicker();
        picker.setArguments(undoStylePref);
        picker.show(getSupportFragmentManager(), "UNDO_STYLE_PICKER");
    }

    public void selectSwipeDirection(View view) {
        DialogPicker picker = new DialogPicker();
        picker.setArguments(swipeDirectionPref);
        picker.show(getSupportFragmentManager(), "SWIPE_DIR_PICKER");
    }

    private class EnhancedRecyclerAdapter extends RecyclerView.Adapter<EnhancedRecyclerAdapter.ViewHolder> {

        private List<String> mItems = new ArrayList<String>();

        void resetItems() {
            mItems.clear();
            for (int i = 1; i <= 40; i++) {
                mItems.add("Item " + i);
            }
            notifyDataSetChanged();
        }

        public void remove(int position) {
            mItems.remove(position);
            notifyItemRemoved(position);
        }

        public void insert(int position, String item) {
            mItems.add(position, item);
            notifyItemInserted(position);
        }

        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item, parent, false);

            EnhancedRecyclerAdapter.ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(final EnhancedRecyclerAdapter.ViewHolder holder, final int position) {
            holder.itemView.findViewById(R.id.action_delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listView.delete(holder.itemView);
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivityRecycler.this, "Clicked on item " + adapter.getItem(position), Toast.LENGTH_SHORT).show();
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(MainActivityRecycler.this, "Long clicked on item " + adapter.getItem(position), Toast.LENGTH_SHORT).show();
                    return true;
                }
            });

            holder.textView = (TextView) holder.itemView.findViewById(R.id.text);
            holder.position = position;
            holder.textView.setText(mItems.get(position));
        }

        /**
         * Get the row id associated with the specified position in the list.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView textView;
            public int position;

            public ViewHolder(View itemView) {
                super(itemView);
            }
        }

    }

    private class DialogPicker extends DialogFragment {

        final static String DIALOG_TITLE = "dialog_title";
        final static String DIALOG_ITEMS_ID = "items_id";
        final static String DIALOG_PREF_KEY = "pref_key";

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Bundle args = getArguments();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(args.getInt(DIALOG_TITLE));
            builder.setSingleChoiceItems(
                    args.getInt(DIALOG_ITEMS_ID),
                    getPreferences(MODE_PRIVATE).getInt(args.getString(DIALOG_PREF_KEY), 0),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences prefs = getPreferences(MODE_PRIVATE);
                            prefs.edit().putInt(args.getString(DIALOG_PREF_KEY), which).commit();
                            dialog.dismiss();
                        }
                    }
            );

            return builder.create();
        }

    }
}
