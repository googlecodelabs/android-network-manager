/**
 * Copyright Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.codelab.networkmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Controller of main/only UI for this sample.
 */
public class MainActivity extends AppCompatActivity {

    public static final String TASK_ID_PREFIX = "task-id";

    private class LoadTask extends AsyncTask<Void, Void, List<TaskItem>> {

        private Context mContext;

        public LoadTask(Context context) {
            mContext = context;
        }

        @Override
        protected List<TaskItem> doInBackground(Void... voids) {
            return CodelabUtil.getTaskItemsFromFile(mContext);
        }

        @Override
        protected void onPostExecute(List<TaskItem> taskItems) {
            mTaskAdapter.setTaskItems(taskItems);
            mTaskAdapter.notifyDataSetChanged();
        }
    }

    // AsyncTask used to add tasks to file off the UI thread.
    private class AddTask extends AsyncTask<TaskItem, Void, TaskItem> {

        private Context mContext;

        public AddTask(Context context) {
            mContext = context;
        }

        @Override
        protected TaskItem doInBackground(TaskItem... taskItems) {
            // Items are being added to the list before being scheduled/executed to allow their state to be
            // visible in the UI.
            TaskItem taskItem = taskItems[0];
            CodelabUtil.addTaskItemToFile(mContext, taskItem);
            return taskItem;
        }

        @Override
        protected void onPostExecute(TaskItem taskItem) {
            mTaskAdapter.addTaskItem(taskItem);
            mRecyclerView.scrollToPosition(0);

            if (taskItem.getType().equals(TaskItem.ONEOFF_TASK)) {
                Bundle bundle = new Bundle();
                bundle.putString(CodelabUtil.TASK_ID, taskItem.getId());

                // Schedule oneoff task.
                OneoffTask oneoffTask = new OneoffTask.Builder()
                        .setService(BestTimeService.class)
                        .setTag(taskItem.getId())
                        .setRequiredNetwork(OneoffTask.NETWORK_STATE_CONNECTED)
                        // Use an execution window of 30 seconds or more. Less than 30 seconds would not allow
                        // GcmNetworkManager enough time to optimize the next best time to execute your task.
                        .setExecutionWindow(0, 30)
                        .setExtras(bundle)
                        .build();
                mGcmNetworkManager.schedule(oneoffTask);
            } else {
                // Immediately make network call.
                Intent nowIntent = new Intent(mContext, NowIntentService.class);
                nowIntent.putExtra(CodelabUtil.TASK_ID, taskItem.getId());
                mContext.startService(nowIntent);
            }
        }
    }

    private static final String TAG = "MainActivity";
    private GcmNetworkManager mGcmNetworkManager;
    private LocalBroadcastManager mLocalBroadcastManager;
    private BroadcastReceiver mBroadcastReceiver;

    private TaskAdapter mTaskAdapter;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTaskAdapter = new TaskAdapter(new ArrayList<TaskItem>());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mTaskAdapter);
        mGcmNetworkManager = GcmNetworkManager.getInstance(this);
        Button bestTimeButton = (Button) findViewById(R.id.bestTimeButton);
        bestTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String taskId = TASK_ID_PREFIX + Calendar.getInstance().getTimeInMillis();
                Log.d(TAG, "Scheduling oneoff task. " + taskId);
                TaskItem taskItem = new TaskItem(taskId, TaskItem.ONEOFF_TASK, TaskItem.PENDING_STATUS);
                new AddTask(view.getContext()).execute(taskItem);
            }
        });

        Button nowButton = (Button) findViewById(R.id.nowButton);
        nowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String taskId = TASK_ID_PREFIX + Calendar.getInstance().getTimeInMillis();
                Log.d(TAG, "Creating a Now Task. " + taskId);
                TaskItem taskItem = new TaskItem(taskId, TaskItem.NOW_TASK, TaskItem.PENDING_STATUS);
                new AddTask(view.getContext()).execute(taskItem);
            }
        });

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String taskId = intent.getStringExtra(CodelabUtil.TASK_ID);
                String status = intent.getStringExtra(CodelabUtil.TASK_STATUS);

                mTaskAdapter.updateTaskItemStatus(taskId, status);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, new IntentFilter(CodelabUtil.TASK_UPDATE_FILTER));
        new LoadTask(this).execute();
    }

    @Override
    public void onPause() {
        mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }
}
