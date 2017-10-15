/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.example.android.datafrominternet;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.datafrominternet.utilities.NetworkUtils;


import java.io.IOException;
import java.net.URL;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>{


    /* A constant to save and restore the URL that is being displayed */
    private static final String SEARCH_QUERY_URL_EXTRA = "query";

    /* A constant to save and restore the JSON that is being displayed */
    private static final String SEARCH_RESULTS_RAW_JSON = "results";


    private static final int GITHUB_SEARCH_LOADER=20;
    private EditText mSearchBoxEditText;

    private TextView mUrlDisplayTextView;
    private TextView mSearchResultsTextView;

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearchBoxEditText = (EditText) findViewById(R.id.et_search_box);

        mUrlDisplayTextView = (TextView) findViewById(R.id.tv_url_display);
        mSearchResultsTextView = (TextView) findViewById(R.id.tv_github_search_results_json);

        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        if (savedInstanceState != null) {
            String queryUrl = savedInstanceState.getString(SEARCH_QUERY_URL_EXTRA);

        }

        getSupportLoaderManager().initLoader(GITHUB_SEARCH_LOADER, null, this);
        // Initialize the loader with GITHUB_SEARCH_LOADER as the ID, null for the bundle, and this for the context
    }

    /**
     * This method retrieves the search text from the EditText, constructs the
     * URL (using {@link NetworkUtils}) for the github repository you'd like to find, displays
     * that URL in a TextView, and finally request that an AsyncTaskLoader performs the GET request.
     */
    private void makeGithubSearchQuery() {
        String githubQuery = mSearchBoxEditText.getText().toString();
        //  If no search was entered, indicate that there isn't anything to search for and return
        if(githubQuery==null||TextUtils.isEmpty(githubQuery)){
            return;
        }


        URL githubSearchUrl = NetworkUtils.buildUrl(githubQuery);
        mUrlDisplayTextView.setText(githubSearchUrl.toString());
        //saving url use putString with SEARCH_QUERY_URL_EXTRA as the key and the String value of the URL as the value
        Bundle queryBundle = new Bundle();
        queryBundle.putString(SEARCH_QUERY_URL_EXTRA, githubSearchUrl.toString());
        //create loader manager
        LoaderManager loaderManager= getSupportLoaderManager();
        //Get our Loader by calling getLoader and passing the ID we specified
        Loader<String> githubSeacheLoader = loaderManager.getLoader(GITHUB_SEARCH_LOADER);
        if(loaderManager==null){
            loaderManager.initLoader(GITHUB_SEARCH_LOADER, queryBundle,this);
        }
        else{
            loaderManager.restartLoader(GITHUB_SEARCH_LOADER,queryBundle,this);
        }

    }

    private void showJsonDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the JSON data is visible */
        mSearchResultsTextView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mSearchResultsTextView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }


    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(this) {
            // Create a String member variable called mGithubJson that will store the raw JSON
            String mGithubJson;
            @Override
            public String loadInBackground() {
                 /* Extract the search query from the args using our constant */
                String searcheQueryUrlString = args.getString(SEARCH_QUERY_URL_EXTRA);
                 /* If no arguments were passed, we don't have a query to perform. Simply return. */
                if(searcheQueryUrlString==null|| TextUtils.isEmpty(searcheQueryUrlString)){
                    return null;
                }
                /* Parse the URL from the passed in String and perform the search */
                try {
                    URL githubUrl = new URL(searcheQueryUrlString);
                    return NetworkUtils.getResponseFromHttpUrl(githubUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if(args==null){
                    return;
                }
                mLoadingIndicator.setVisibility(View.VISIBLE);
                if(mGithubJson==null){
                    forceLoad();
                }
                else {
                    deliverResult(mGithubJson);
                }
                // If mGithubJson is not null, deliver that result. Otherwise, force a load
            }
            @Override
            public void deliverResult(String data){
                mGithubJson=data;
                super.deliverResult(data);
            }
        };
    }
    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (data != null && !data.equals("")) {
            showJsonDataView();
            mSearchResultsTextView.setText(data);
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();
        if (itemThatWasClickedId == R.id.action_search) {
            makeGithubSearchQuery();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String queryUrl = mUrlDisplayTextView.getText().toString();
        outState.putString(SEARCH_QUERY_URL_EXTRA, queryUrl);

    }
}