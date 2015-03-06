A fork of https://github.com/timroes/EnhancedListView, since the original author no longer supports it.

EnhancedList
=============================

An Android ListView with enhanced functionality (e.g. Swipe To Dismiss and Undo)

Include It
------------------

  ```compile 'com.luciocossio.android:EnhancedList:0.0.1'```
  
Layout
------------------

Put the recycler list in your layout
```
<com.luciocossio.android.enhancedlist.EnhancedRecyclerListView
        android:id="@+id/list"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
```

Item Layout
------------------

Create a layout for the list items. It can have internal views that will be clickable. 
In the below case we have a ImageButton that will be used to delete the item by onClick events.

```
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent">
    <TextView
        android:gravity="center_vertical"
        android:textColor="@android:color/white"
        android:background="@color/list_bg"
        android:text="@string/list_item_background"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
    <FrameLayout android:id="@+id/swiping_layout"
        android:background="@android:color/white"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        <RelativeLayout
            android:background="?selectableItemBackground"
            android:layout_width="match_parent"
            android:layout_height="match_parent">
            <TextView android:id="@+id/text"
                android:layout_alignParentLeft="true"
                android:layout_centerVertical="true"
                android:textAppearance="?android:textAppearanceLarge"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"/>
            <ImageButton android:id="@+id/action_delete"
                android:background="?selectableItemBackground"
                android:layout_alignParentRight="true"
                android:src="@drawable/ic_action_delete"
                android:layout_height="match_parent" />
        </RelativeLayout>
    </FrameLayout>
</FrameLayout>
```

Adapter
-------------------

You should create an adapter that extends the RecyclerView.Adapter, and define a ViewHolder class.
The click events of the items should be defined when the view holders are binded.

```
private class EnhancedRecyclerAdapter extends RecyclerView.Adapter<EnhancedRecyclerAdapter.ViewHolder> {

        private List<String> items = new ArrayList<String>();

        public void remove(int position) {
            items.remove(position);
            notifyItemRemoved(position);
        }

        public void insert(int position, String item) {
            items.add(position, item);
            notifyItemInserted(position);
        }

        public Object getItem(int position) {
            return items.get(position);
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
                    //when action_delete ImageButton is clicked
                    listView.delete(holder.itemView);
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Do something when the item is clicked
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //Do something when item is long clicked
                    return true;
                }
            });

            holder.textView = (TextView) holder.itemView.findViewById(R.id.text);
            holder.position = position;
            holder.textView.setText(items.get(position));
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView textView;
            public int position;

            public ViewHolder(View itemView) {
                super(itemView);
            }
        }

    }
```

Set A Dismiss Callback
------------------

You should defined what happens when an item is dismiss, and return an Undoable action.
In this case, we simply remove the item from the adapter, and reinsert it back when the undo happens.

```
 listView.setDismissCallback(new com.luciocossio.android.enhancedlist.OnDismissCallback() {
            @Override
            public Undoable onDismiss(EnhancedList listView, final int position) {
                final String item = (String) adapter.getItem(position);
                adapter.remove(position);
                return new Undoable() {
                    @Override
                    public void undo() {
                        adapter.insert(position, item);
                    }
                    @Override public void discard() {
                        //delete item permanently
                    }
                };
            }
        });
```

Development Updates
------------------

- A complete refactoring was made from the old timroes project to split classes into different files, and extract some common behaviors and interfaces
- So far, the old ListView version still exist and should work the same
- A first EnhancedRecyclerListView draft is under review

Development TODO list
------------------

- Review RecyclerView use. Is it being used correctly? Using it's full potential?
- Add automated tests

## License

This project is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html). If you commit to this project (e.g. by sending Pull Requests) you agree to publish your code under the same license.

```text
Copyright 2015 Lucio Cossio

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
