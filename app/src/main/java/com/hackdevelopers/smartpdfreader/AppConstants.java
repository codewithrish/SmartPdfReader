package com.hackdevelopers.smartpdfreader;


import com.google.android.gms.common.Scopes;
import com.google.api.services.youtube.YouTubeScopes;

public interface AppConstants {

    public static final String pdfText = "";

    public static final int SEARCH_VIDEO = 1;

    public static final int PICKFILE_RESULT_CODE = 1;

    public static final String SEARCH_VIDEO_MSG = "Searching Videos";

    public static final int REQ_CODE_SPEECH_INPUT = 100;

    public static final String DIALOG_TITLE = "Loading";

    public static final long NUMBER_OF_VIDEOS_RETURNED = 25;
    public static final String APP_NAME = Singleton.appName();

    //YouTube API Key
    public static final String KEY = "AIzaSyC81bC6TCYo_y7TvPo4Mi441EXfm58OrB8";
    public static final String[] SCOPES = {Scopes.PROFILE, YouTubeScopes.YOUTUBE};

    //First Fragment
    public static final String FRAGMENT_API = "api";
    public static final int LOADER_ACCESS_TOKEN = 1;

    //Second Fragment
    public static final int EXTERNAL_STORAGE_PERMISSION_CONSTANT = 1;
    public static final int REQUEST_PERMISSION_SETTING = 2;

    public static final String rateUs = "ca-app-pub-9339324316850793~1803202665";
    public static final String ADMOB_APP_ID = "";


}
