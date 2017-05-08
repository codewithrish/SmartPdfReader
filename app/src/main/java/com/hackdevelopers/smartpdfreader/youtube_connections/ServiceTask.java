package com.hackdevelopers.smartpdfreader.youtube_connections;

import android.os.AsyncTask;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.hackdevelopers.smartpdfreader.AppConstants;

import java.io.IOException;
import java.util.List;

public class ServiceTask extends AsyncTask<Object, Void, Object[]> implements
        ServiceTaskInterface {
    private static final String TAG = ServiceTask.class.getSimpleName();
    private ServerResponseListener mServerResponseListener = null;
    private int mRequestCode = 0;

    public void setmServerResponseListener(
            ServerResponseListener mServerResponseListener) {
        this.mServerResponseListener = mServerResponseListener;
    }

    public ServiceTask(int iReqCode) {
        mRequestCode = iReqCode;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mServerResponseListener.prepareRequest(mRequestCode);
    }

    @Override
    protected Object[] doInBackground(Object... params) {
        if (params == null)
            throw new NullPointerException("Parameters to the async task can never be null");

        mServerResponseListener.goBackground();

        Object[] resultDetails = new Object[2];
        resultDetails[0] = mRequestCode;

        switch (mRequestCode) {
            case AppConstants.SEARCH_VIDEO:
                resultDetails[1] = loadVideos((String) params[0]);
                break;
        }

        return resultDetails;
    }

    @Override
    protected void onPostExecute(Object[] result) {
        super.onPostExecute(result);
        mServerResponseListener.completedRequest(result);
    }

    private List<SearchResult> loadVideos(String queryTerm) {
        try {

            YouTube youtube = new YouTube.Builder(transport, jsonFactory, new HttpRequestInitializer() {
                public void initialize(HttpRequest request) throws IOException {
                }
            }).setApplicationName(AppConstants.APP_NAME).build();

            YouTube.Search.List search = youtube.search().list("id,snippet");

            search.setKey(AppConstants.KEY);
            search.setQ(queryTerm);

            search.setType("video");

            search.setMaxResults(AppConstants.NUMBER_OF_VIDEOS_RETURNED);
            
            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResultList = searchResponse.getItems();
            if (searchResultList != null) {
                return searchResultList;
            }
        } catch (GoogleJsonResponseException e) {
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (IOException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return null;
    }

}
