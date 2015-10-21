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

public class TaskItem {

    public static final String ONEOFF_TASK = "Oneoff Task";
    public static final String NOW_TASK = "Now Task";
    public static final String PENDING_STATUS = "Pending";
    public static final String EXECUTED_STATUS = "Executed";
    public static final String FAILED_STATUS = "Failed";


    private String mId;
    private String mType;
    private String mStatus;

    public TaskItem(String id, String label, String status) {
        mId = id;
        mType = label;
        mStatus = status;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public String getType() {
        return mType;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String stat) {
        this.mStatus = stat;
    }
}
