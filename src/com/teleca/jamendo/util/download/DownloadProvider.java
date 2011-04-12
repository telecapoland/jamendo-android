/*
 * Copyright (C) 2011 Teleca Poland Sp. z o.o. <android@teleca.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.teleca.jamendo.util.download;

import java.util.ArrayList;

import com.teleca.jamendo.api.Track;

/**
 * Download jobs provider. Handles storing DownloadJobs.
 * 
 * @author Bartosz Cichosz
 * 
 */
public interface DownloadProvider {

	public abstract ArrayList<DownloadJob> getAllDownloads();

	public abstract ArrayList<DownloadJob> getCompletedDownloads();

	public abstract ArrayList<DownloadJob> getQueuedDownloads();

	public abstract void downloadCompleted(DownloadJob job);

	public abstract boolean queueDownload(DownloadJob downloadJob);

	public abstract void removeDownload(DownloadJob job);

	public abstract boolean trackAvailable(Track track);

}