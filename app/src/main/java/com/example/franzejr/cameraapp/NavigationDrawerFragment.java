package com.example.franzejr.cameraapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    protected Uri mMediaUri;


    private int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;
    private int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 1;
    private int PICK_PHOTO_REQUEST = 2;
    private int PICK_VIDEO_REQUEST = 3;

    //Media Types
    public static final int MEDIA_TYPE_IMAGE = 33;
    public static final int MEDIA_TYPE_VIDEO = 44;



    private int RESULT_CANCELED = 11;
    private int RESULT_OK = 22;


    protected DialogInterface.OnClickListener mDialogListener = new DialogInterface.OnClickListener(){

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            switch(i){
                case 0: //Take Picture
                    // create Intent to take a picture and return control to the calling application
                    Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                    if(mMediaUri == null){
                        //error
                        Toast.makeText(getActivity(), R.string.problem_external_device, Toast.LENGTH_LONG).show();
                    }else{
                        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                        startActivityForResult(takePhotoIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                    }

                    break;
                case 1: //Take Video
                    Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
                    if(mMediaUri == null){
                        //error
                        Toast.makeText(getActivity(), R.string.problem_external_device, Toast.LENGTH_LONG).show();
                    }else{
                        videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                        videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
                        videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0); //0 == lowest quality
                        startActivityForResult(videoIntent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
                    }
                    break;
                case 2: //Choose Picture
                    Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    choosePhotoIntent.setType("images/*");
                    startActivityForResult(choosePhotoIntent,PICK_PHOTO_REQUEST);
                    break;
                case 3: //Chose Video
                    Intent chooseVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    chooseVideoIntent.setType("video/*");
                    startActivityForResult(chooseVideoIntent,PICK_VIDEO_REQUEST);
                    break;

            }

        }
    };

    private Uri getOutputMediaFileUri(int mediaType) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        if(isExternalStorageAvailable()){
            //getTheUri


            // 1. Get the External Storage Directory
            String appName = getActivity().getString(R.string.app_name);
            File mediaStorageDir = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),appName);
            // 2. Create our subdirectory
            if(!mediaStorageDir.exists()){
                if(!mediaStorageDir.mkdirs()){
                    Toast.makeText(getActivity(), "Failed to create directory",Toast.LENGTH_SHORT);

                    Log.e("DIRECTORY", "Failed to create directory");
                    return null;
                }
            }
            // 3. Create a filename
            // 4. Create the file
            File mediaFile;
            Date now = new Date();
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now);

            String path = mediaStorageDir.getPath() + File.separator;
            if(mediaType == MEDIA_TYPE_IMAGE){
                mediaFile = new File(path + "IMG_" + timestamp + ".jpg");
            }
            else if(mediaType == MEDIA_TYPE_VIDEO){
                mediaFile = new File(path + "VID_" + timestamp + ".mp4");
            }else{
                return null;
            }
            Toast.makeText(getActivity(), "Saving Media in: "+ Uri.fromFile(mediaFile).toString(),Toast.LENGTH_SHORT);
            Log.e("CREATING", Uri.fromFile(mediaFile).toString());
            // 5. Return the file's Uri
            return Uri.fromFile(mediaFile);
        }else{
            return null;
        }
    }

    private boolean isExternalStorageAvailable(){
        String state = Environment.getExternalStorageState();
        if(state.equals(Environment.MEDIA_MOUNTED))
            return true;
        else
            return false;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){

            if (requestCode == PICK_PHOTO_REQUEST || requestCode == PICK_VIDEO_REQUEST) {
                if (data == null) {
                    Toast.makeText(getActivity(), "Error", Toast.LENGTH_LONG).show();
                } else {
                    mMediaUri = data.getData();
                }
            }else{
                Intent mediaScanIntent =  new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(mMediaUri);
                getActivity().sendBroadcast(mediaScanIntent);
                Toast.makeText(getActivity(), "Media saved",Toast.LENGTH_SHORT);

            }
        }
        else if(resultCode == RESULT_CANCELED){

        }
    }

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        // Select either the default item (0) or the last selected item.
        selectItem(mCurrentSelectedPosition);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mDrawerListView = (ListView) inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
        mDrawerListView.setAdapter(new ArrayAdapter<String>(
                getActionBar().getThemedContext(),
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1,
                new String[]{
                        getString(R.string.title_section1),
                        getString(R.string.title_section2),
                        getString(R.string.title_section3),
                        getString(R.string.title_section4),
                        getString(R.string.title_section5),
                }));
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
        return mDrawerListView;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.drawable.ic_drawer,             /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }

            switch(position){
                case 1: //Take Picture
                    // create Intent to take a picture and return control to the calling application
                    Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                    if(mMediaUri == null){
                        //error
                        Toast.makeText(getActivity(), R.string.problem_external_device, Toast.LENGTH_LONG).show();
                    }else{
                        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                        startActivityForResult(takePhotoIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                    }

                    break;
                case 2: //Take Video
                    Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
                    if(mMediaUri == null){
                        //error
                        Toast.makeText(getActivity(), R.string.problem_external_device, Toast.LENGTH_LONG).show();
                    }else{
                        videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                        videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
                        videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0); //0 == lowest quality
                        startActivityForResult(videoIntent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
                    }
                    break;
                case 3: //Choose Picture
                    Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    choosePhotoIntent.setType("images/*");
                    startActivityForResult(choosePhotoIntent,PICK_PHOTO_REQUEST);
                    break;
                case 4: //Chose Video
                    Intent chooseVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    chooseVideoIntent.setType("video/*");
                    startActivityForResult(chooseVideoIntent,PICK_VIDEO_REQUEST);
                    break;
                default:
                    break;

            }

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_take_picture:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setItems(R.array.camera_choices, mDialogListener);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
    }
}
