package com.hackdevelopers.smartpdfreader.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hackdevelopers.smartpdfreader.R;
import com.hackdevelopers.smartpdfreader.RecyclerItemClickListener;
import com.hackdevelopers.smartpdfreader.events.SearchKeyWordEvent;
import com.hackdevelopers.smartpdfreader.events.SendUriData;
import com.hackdevelopers.smartpdfreader.nlp.AccessTokenLoader;
import com.hackdevelopers.smartpdfreader.nlp.ApiFragment;
import com.hackdevelopers.smartpdfreader.nlp.EntityInfo;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;

import static com.hackdevelopers.smartpdfreader.AppConstants.FRAGMENT_API;
import static com.hackdevelopers.smartpdfreader.AppConstants.LOADER_ACCESS_TOKEN;
import static com.hackdevelopers.smartpdfreader.AppConstants.PICKFILE_RESULT_CODE;
import static com.hackdevelopers.smartpdfreader.AppConstants.pdfText;
import static com.hackdevelopers.smartpdfreader.R.id.viewpager;

/**
 * Created by risha on 5/4/2017.
 */

public class SuggestionsFragment extends Fragment implements ApiFragment.Callback{

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.analyze:
                    startAnalyze();
                    break;
            }
        }
    };

    private View view;

    private EventBus bus = EventBus.getDefault();

    private static EntityInfo[] mEntities;

    private View mResults;
    private ProgressBar mProgress;
    private TextView mIntroduction;
    private TextView status;
    private RecyclerView list;

    private boolean mHidingResult;
    private Uri pdfUri = null;

    private SuggestionsFragment.EntitiesAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        if(!bus.isRegistered(this)) {
            bus.register(this);
        }

        if(view  == null) {
            view = inflater.inflate(R.layout.fragment_suggestions, container, false);
            view.findViewById(R.id.analyze).setOnClickListener(mOnClickListener);
            mIntroduction = (TextView) view.findViewById(R.id.introduction);
            mResults = view.findViewById(R.id.results);
            mProgress = (ProgressBar) view.findViewById(R.id.progress);
            status = (TextView) view.findViewById(R.id.status);

            list = (RecyclerView)view.findViewById(R.id.list);
            list.setLayoutManager(new LinearLayoutManager(getActivity()));

            mAdapter = new SuggestionsFragment.EntitiesAdapter(getActivity(), mEntities);
            list.setAdapter(mAdapter);

            list.addOnItemTouchListener(
                    new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
                        @Override public void onItemClick(View view, int position) {
                            String s = mEntities[position].name;
                            EventBus bus = EventBus.getDefault();
                            bus.post(new SearchKeyWordEvent(s));
                            ViewPager vp=(ViewPager) getActivity().findViewById(viewpager);
                            vp.setCurrentItem(2);
                        }
                    })
            );


            FragmentManager fm = getChildFragmentManager();

            if (getApiFragment() == null) {
                fm.beginTransaction().add(new ApiFragment(), FRAGMENT_API).commit();
            }
            prepareApi();
        }

        return view;
    }

    @Override
    public void onEntitiesReady(EntityInfo[] entities) {
        showResults();
        mAdapter.setEntities(entities);
    }

    private void showResults() {

        mIntroduction.setVisibility(View.GONE);
        if (mProgress.getVisibility() == View.VISIBLE) {
            ViewCompat.animate(mProgress)
                    .alpha(0.f)
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(View view) {
                            view.setVisibility(View.INVISIBLE);
                        }
                    });
        }
        if (mHidingResult) {
            ViewCompat.animate(mResults).cancel();
        }
        if (mResults.getVisibility() == View.INVISIBLE) {
            mResults.setVisibility(View.VISIBLE);
            ViewCompat.setAlpha(mResults, 0.01f);
            ViewCompat.animate(mResults)
                    .alpha(1.f)
                    .setListener(null)
                    .start();
        }
    }

    private ApiFragment getApiFragment() {
        return (ApiFragment) getChildFragmentManager().findFragmentByTag(FRAGMENT_API);
    }

    private void prepareApi() {

        getActivity().getSupportLoaderManager().initLoader(LOADER_ACCESS_TOKEN, null,
                new LoaderManager.LoaderCallbacks<String>() {
                    @Override
                    public Loader<String> onCreateLoader(int id, Bundle args) {
                        return new AccessTokenLoader(getActivity());
                    }

                    @Override
                    public void onLoadFinished(Loader<String> loader, String token) {
                        getApiFragment().setAccessToken(token);
                    }

                    @Override
                    public void onLoaderReset(Loader<String> loader) {
                    }
                });
    }

    private void startAnalyze() {

        showProgress();
        new ExtractTextFromPdf().execute();
    }

    private void showProgress() {
        mIntroduction.setVisibility(View.GONE);
        if (mResults.getVisibility() == View.VISIBLE) {
            mHidingResult = true;
            ViewCompat.animate(mResults)
                    .alpha(0.f)
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(View view) {
                            mHidingResult = false;
                            view.setVisibility(View.INVISIBLE);
                        }
                    });
        }
        if (mProgress.getVisibility() == View.INVISIBLE) {
            mProgress.setVisibility(View.VISIBLE);
            ViewCompat.setAlpha(mProgress, 0.f);
            ViewCompat.animate(mProgress)
                    .alpha(1.f)
                    .setListener(null)
                    .start();
        }
    }

    public String stripText() {
        String parsedText = null;
        PDDocument document = null;

        try {
            if(pdfUri == null) {
                document = PDDocument.load(getActivity().getAssets().open("sample.pdf"));
            } else  {

                document = PDDocument.load(new File(pdfUri.getPath()));
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

        try {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setStartPage(0);
            pdfStripper.setEndPage(document.getNumberOfPages());
            parsedText = pdfStripper.getText(document);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parsedText;
    }

    private class ExtractTextFromPdf extends AsyncTask<String, Integer, String> {

        protected String doInBackground(String... urls) {
            return stripText();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            status.setText("Extracting It will take While ...");
        }

        protected void onPostExecute(String result) {
            getApiFragment().analyzeEntities(result);
            status.setText("Click To Get Suggestions");
            Toast.makeText(getContext(), "Result Calculated", Toast.LENGTH_SHORT).show();

        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public TextView type;
        public TextView salience;
        public TextView wikipediaUrl;

        public ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_entity, parent, false));
            name = (TextView) itemView.findViewById(R.id.name);
            type = (TextView) itemView.findViewById(R.id.type);
            salience = (TextView) itemView.findViewById(R.id.salience);
            wikipediaUrl = (TextView) itemView.findViewById(R.id.wikipedia_url);
        }

    }

    private static class EntitiesAdapter extends RecyclerView.Adapter<SuggestionsFragment.ViewHolder> {

        private final Context mContext;


        public EntitiesAdapter(Context context, EntityInfo[] entities) {
            mContext = context;
            mEntities = entities;
        }

        @Override
        public SuggestionsFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new SuggestionsFragment.ViewHolder(LayoutInflater.from(mContext), parent);
        }

        @Override
        public void onBindViewHolder(SuggestionsFragment.ViewHolder holder, int position) {
            EntityInfo entity = mEntities[position];
            holder.name.setText(entity.name);
            holder.type.setText(entity.type);
            holder.salience.setText(mContext.getString(R.string.salience_format, entity.salience));
            holder.wikipediaUrl.setText(entity.wikipediaUrl);
            Linkify.addLinks(holder.wikipediaUrl, Linkify.WEB_URLS);
        }

        @Override
        public int getItemCount() {
            return mEntities == null ? 0 : mEntities.length;
        }

        public void setEntities(EntityInfo[] entities) {
            mEntities = entities;
            notifyDataSetChanged();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SendUriData event) {
        Uri pdfPath = event.uri;
        pdfUri = pdfPath;
        Toast.makeText(getContext(), new File(pdfUri.getPath()).toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_one, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.one:
                Toast.makeText(getContext(), "ONE", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

}
