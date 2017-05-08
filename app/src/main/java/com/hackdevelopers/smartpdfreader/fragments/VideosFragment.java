package com.hackdevelopers.smartpdfreader.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.api.services.youtube.model.SearchResult;
import com.hackdevelopers.smartpdfreader.R;
import com.hackdevelopers.smartpdfreader.events.SearchKeyWordEvent;
import com.hackdevelopers.smartpdfreader.events.TextChangedEvent;
import com.hackdevelopers.smartpdfreader.youtube.YtAdapter;
import com.hackdevelopers.smartpdfreader.youtube_connections.ServerResponseListener;
import com.hackdevelopers.smartpdfreader.youtube_connections.ServiceTask;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static com.hackdevelopers.smartpdfreader.R.id.viewpager;


public class VideosFragment extends Fragment implements AdapterView.OnItemClickListener, ServerResponseListener {



    private YtAdapter mYtAdapter = null;
    private ServiceTask mYtServiceTask = null;

    View view = null;

    private ListView mYtVideoLsv = null;

    private MaterialSearchView searchView;

    EventBus bus = EventBus.getDefault();

    public VideosFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        if(!bus.isRegistered(this)) {
            bus.register(this);
        }

        if(view == null) {
            view = inflater.inflate(R.layout.fragment_videos, container, false);

            mYtVideoLsv = (ListView)view.findViewById(R.id.yt_video_lsv);
            mYtVideoLsv.setOnItemClickListener(this);
            searchView = (MaterialSearchView)view.findViewById(R.id.search_view);

            search("google");
            initializeSearch();

            return view;
        }
        else {
            return view;
        }
    }

    public void initializeSearch() {

        searchView.setVoiceSearch(false);
        searchView.setCursorDrawable(R.drawable.custom_cursor);
        searchView.setEllipsize(true);


        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Snackbar.make(getView().findViewById(R.id.threeFragment), "Query: " + query, Snackbar.LENGTH_LONG)
                        .show();
                search(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                searchView.setVisibility(View.VISIBLE);
                EventBus bus = EventBus.getDefault();
                bus.post(new TextChangedEvent("hide"));
            }

            @Override
            public void onSearchViewClosed() {
                EventBus bus = EventBus.getDefault();
                bus.post(new TextChangedEvent("show"));
            }
        });
    }

    private void search(String query) {
        if (query.length() > 0) {
            mYtServiceTask = new ServiceTask(SEARCH_VIDEO);
            mYtServiceTask.setmServerResponseListener(this);
            mYtServiceTask.execute(query);
        } else {
            Toast.makeText(getContext(), "Empty field", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_three, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_search_mic) {
            promptSpeechInput();
        }
        return super.onOptionsItemSelected(item);
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Snackbar.make(getView().findViewById(R.id.threeFragment), "Query: " + result.get(0), Snackbar.LENGTH_LONG)
                            .show();

                    search(result.get(0));
                }
                break;
            }

        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        SearchResult obj = (SearchResult) mYtAdapter.getItem(position);
        String vidId = obj.getId().getVideoId();

        EventBus bus = EventBus.getDefault();
        bus.post(new TextChangedEvent(vidId));

        ViewPager vp=(ViewPager) getActivity().findViewById(viewpager);
        vp.setCurrentItem(1);

    }


    @Override
    public void prepareRequest(Object... objects) {

        Integer reqCode = (Integer) objects[0];

        if(reqCode==null || reqCode == 0)
            throw new NullPointerException("Request Code's value is Invalid.");
        String dialogMsg = null;
        switch (reqCode)
        {
            case SEARCH_VIDEO:
                dialogMsg = SEARCH_VIDEO_MSG;
                break;
        }

    }

    @Override
    public void goBackground(Object... objects) {
    }

    @Override
    public void completedRequest(Object... objects) {

        Integer reqCode = (Integer) objects[0];

        if(reqCode==null || reqCode == 0)
            throw new NullPointerException("Request Code's value is Invalid.");

        switch (reqCode) {
            case SEARCH_VIDEO:

                if (mYtAdapter == null) {
                    mYtAdapter = new YtAdapter(getActivity());
                    mYtAdapter.setmVideoList((List<SearchResult>) objects[1]);
                    mYtVideoLsv.setAdapter(mYtAdapter);
                } else {
                    mYtAdapter.setmVideoList((List<SearchResult>) objects[1]);
                    mYtAdapter.notifyDataSetChanged();
                }

                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SearchKeyWordEvent event) {
        final String searchKeyword = event.newText.toString();
        if(event.newText.equals(searchKeyword)) {
            search(searchKeyword);
        }

    }

}
