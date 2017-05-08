package com.hackdevelopers.smartpdfreader.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.hackdevelopers.smartpdfreader.AppConstants;
import com.hackdevelopers.smartpdfreader.R;
import com.hackdevelopers.smartpdfreader.events.SendUriData;
import com.hackdevelopers.smartpdfreader.events.TextChangedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.hackdevelopers.smartpdfreader.AppConstants.PICKFILE_RESULT_CODE;

/**
 * Created by risha on 5/4/2017.
 */

public class PdfFragment extends Fragment {

    public static String VIDEO_ID = "PLlyCyjh2pUe9wv-hU4my-Nen_SvXIzxGB";
    private YouTubePlayer YPlayer;
    private YouTubePlayerSupportFragment mYoutubePlayerFragment;
    private RelativeLayout youtube;
    private View youtube_player;
    private static final int RECOVERY_DIALOG_REQUEST = 1;
    private View view = null;

    private PDFView pdfView;

    EventBus bus = EventBus.getDefault();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        if(!bus.isRegistered(this)) {
            bus.register(this);
        }

        if(view == null) {
            view = inflater.inflate(R.layout.fragment_pdf, container, false);

            youtube = (RelativeLayout) view.findViewById(R.id.youtube);
            youtube_player = view.findViewById(R.id.youtube_player);

            mYoutubePlayerFragment = new YouTubePlayerSupportFragment();

            mYoutubePlayerFragment.initialize(AppConstants.KEY, new YouTubePlayer.OnInitializedListener() {
                @Override
                public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestore) {
                    YPlayer = youTubePlayer;

                    YPlayer.setPlayerStateChangeListener(playerStateChangeListener);
                    YPlayer.setPlaybackEventListener(playbackEventListener);

                    if(!wasRestore) {
                        YPlayer.cuePlaylist(VIDEO_ID);
                    }
                }

                @Override
                public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult errorReason) {
                    if (errorReason.isUserRecoverableError()) {
                        errorReason.getErrorDialog(getActivity(), RECOVERY_DIALOG_REQUEST).show();
                    } else {
                        String errorMessage = String.format(
                                "There was an error initializing the YouTubePlayer",
                                errorReason.toString());
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });

            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.youtube, mYoutubePlayerFragment);
            fragmentTransaction.commit();


            pdfView = (PDFView)view.findViewById(R.id.pdfView);
            pdfView.fromAsset("sample.pdf")
                    .load();
        }
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    private YouTubePlayer.PlaybackEventListener playbackEventListener = new YouTubePlayer.PlaybackEventListener() {
        @Override
        public void onPlaying() {

        }

        @Override
        public void onPaused() {

        }

        @Override
        public void onStopped() {

        }

        @Override
        public void onBuffering(boolean b) {

        }

        @Override
        public void onSeekTo(int i) {

        }
    };

    private YouTubePlayer.PlayerStateChangeListener playerStateChangeListener = new YouTubePlayer.PlayerStateChangeListener() {
        @Override
        public void onLoading() {

        }

        @Override
        public void onLoaded(String s) {
            YPlayer.play();
        }

        @Override
        public void onAdStarted() {

        }

        @Override
        public void onVideoStarted() {

        }

        @Override
        public void onVideoEnded() {

        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {

        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_two, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_browse:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                startActivityForResult(intent,PICKFILE_RESULT_CODE);
                break;

            case R.id.action_expand_collapse:
                if(youtube.getVisibility() == View.VISIBLE) {
                    youtube.setVisibility(View.GONE);
                    youtube_player.setVisibility(View.GONE);
                    item.setIcon(R.drawable.ic_expand_more_white_24px);
                } else {
                    youtube.setVisibility(View.VISIBLE);
                    youtube_player.setVisibility(View.VISIBLE);
                    item.setIcon(R.drawable.ic_expand_less_white_24px);
                }
                break;
            case R.id.two:
                Toast.makeText(getContext(), "TWO", Toast.LENGTH_SHORT).show();
                break;


        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null) {
            Uri pdfPath = data.getData();
            pdfView.fromUri(pdfPath).load();
            EventBus bus = EventBus.getDefault();
            bus.post(new SendUriData(pdfPath));

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(TextChangedEvent event) {
        final String vidId = event.newText.toString();
        if(event.newText.equals(vidId)) {
            VIDEO_ID = vidId;
            youtube.setVisibility(View.VISIBLE);
            youtube_player.setVisibility(View.VISIBLE);
            YPlayer.cueVideo(VIDEO_ID);
        }

    }

}
