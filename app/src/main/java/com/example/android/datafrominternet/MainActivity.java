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
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.datafrominternet.utilities.NetworkUtils;

import java.io.IOException;
import java.net.URL;

import static android.R.attr.id;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;

public class MainActivity extends AppCompatActivity {

    EditText mSearchBoxEditText;
    TextView mUrlDisplayTextView;
    TextView mSearchResultsTextView;
    TextView mErrorMessageTextView;
    ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearchBoxEditText=(EditText) findViewById(R.id.et_search_box);
        mUrlDisplayTextView= (TextView)findViewById(R.id.tv_url_display);
        mSearchResultsTextView=(TextView)findViewById(R.id.tv_github_search_results_json);
        mErrorMessageTextView=(TextView)findViewById(R.id.tv_error_message);
        mLoadingIndicator=(ProgressBar)findViewById(R.id.pb_loading_indicator);


    }
    private void makeGithubSearchQuery (){
        String gitHubQuery=mSearchBoxEditText.getText().toString();
        //build url
        URL gitHubSearchUrl= NetworkUtils.buildUrl(gitHubQuery);
        //display url
        mUrlDisplayTextView.setText(gitHubSearchUrl.toString());
        //create string to get connection
        String gitHubSearchResult=null;
        //instantiate GithubQueryTask
       new GithubQueryTask().execute(gitHubSearchUrl);

    }
    private void showJsonDataView(){
        mErrorMessageTextView.setVisibility(View.INVISIBLE);
        mSearchResultsTextView.setVisibility(View.VISIBLE);
    }
    private void showErrorMessage(){
        mErrorMessageTextView.setVisibility(View.VISIBLE);
        mSearchResultsTextView.setVisibility(View.INVISIBLE);
    }
    public class GithubQueryTask extends AsyncTask<URL,Void,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(URL... params) {
            URL searchURL = params[0];
            String githubSearchResults = null;

            try {
                githubSearchResults = NetworkUtils.getResponseFromHttpUrl(searchURL);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return githubSearchResults;
        }

        @Override
        protected void onPostExecute(String s) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if(s!=null&&!s.equals("")){
                showJsonDataView();
                //set string with connection to text view to see the result
                mSearchResultsTextView.setText(s);
            }
            else{
                showErrorMessage();
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuItemSelected = item.getItemId();
        if(menuItemSelected==R.id.action_search){
           makeGithubSearchQuery();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
