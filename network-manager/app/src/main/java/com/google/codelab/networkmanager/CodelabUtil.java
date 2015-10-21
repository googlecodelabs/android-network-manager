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

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Utility class to provide networking and file operations.
 */
public class CodelabUtil {

    public static final String TASK_ID = "taskId";
    public static final String TASK_STATUS = "status";
    public static final String TASK_UPDATE_FILTER = "task-update";
    private static final String TAG = "CodelabUtil";
    private static final String FILE_NAME = "taskfile.dat";
    private static final String ONLINE_LOCATION = "https://google.com";

    /**
     * Make a network request form ONLINE_LOCATION.
     */
    public static boolean makeNetworkCall() {
        try {
            URL url = new URL(ONLINE_LOCATION);
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.getInputStream();
            Log.d(TAG, "Network call completed.");
            return true;
        } catch (IOException e) {
            Log.e(TAG, "IOException " + e.getMessage());
            return false;
        }
    }

    public static List<TaskItem> taskItemsFromString(String taskStr) {
        Gson gson = new Gson();
        Type taskItemType = new TypeToken<ArrayList<TaskItem>>(){}.getType();
        List<TaskItem> taskItems = gson.fromJson(taskStr, taskItemType);
        return taskItems;
    }

    private static String taskItemsToString(List<TaskItem> taskItems) {
        return new Gson().toJson(taskItems);
    }

    public static List<TaskItem> getTaskItemsFromFile(Context context) {
        List<TaskItem> taskItems = new ArrayList<>();
        File taskFile = new File(context.getFilesDir(), FILE_NAME);
        if (!taskFile.exists()) {
            return taskItems;
        }
        try {
            String taskStr = IOUtils.toString(new FileInputStream(taskFile));
            taskItems.addAll(taskItemsFromString(taskStr));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return taskItems;
    }

    public static TaskItem getTaskItemFromFile(Context context, String id) {
        List<TaskItem> taskItems = getTaskItemsFromFile(context);
        for (int i = 0; i < taskItems.size(); i++) {
            TaskItem taskItem = taskItems.get(i);
            if (taskItem.getId().equals(id)) {
                return taskItem;
            }
        }
        return null;
    }

    /**
     * Overwrite Tasks in file with those given here.
     */
    private static void saveTaskItemsToFile(Context context, List<TaskItem> taskItems) {
        String taskStr = taskItemsToString(taskItems);
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            IOUtils.write(taskStr, fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveTaskItemToFile(Context context, TaskItem taskItem) {
        List<TaskItem> taskItems = getTaskItemsFromFile(context);
        for (int i = 0; i < taskItems.size(); i++) {
            TaskItem ti = taskItems.get(i);
            if (ti.getId().equals(taskItem.getId())) {
                taskItems.set(i, taskItem);
                break;
            }
        }
        saveTaskItemsToFile(context, taskItems);
    }

    /**
     * Add a Task to the front of the task list.
     */
    public static void addTaskItemToFile(Context context, TaskItem taskItem) {
        List<TaskItem> taskItems = getTaskItemsFromFile(context);
        taskItems.add(0, taskItem);
        saveTaskItemsToFile(context, taskItems);
    }

    public static void deleteTaskItemFromFile(Context context, TaskItem taskItem) {
        List<TaskItem> taskItems = getTaskItemsFromFile(context);
        for (int i = 0; i < taskItems.size(); i++) {
            TaskItem ti = taskItems.get(i);
            if (ti.getId().equals(taskItem.getId())) {
                taskItems.remove(i);
                break;
            }
        }
        saveTaskItemsToFile(context, taskItems);
    }
}
