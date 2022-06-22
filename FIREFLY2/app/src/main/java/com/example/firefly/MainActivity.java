package com.example.firefly;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;


import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.appbar.AppBarLayout;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener,
        ListFragment.ListFragmentAddListener,
        RecyclerAdapterMainTrackList.MainTrackListOnClickListener,
        RecyclerAdapterMainTrackList.MainTrackListOptionOnClickListener,
        RecyclerAdapterMainList.MainListClickListener,
        RecyclerAdapterMainAlbum.MainAlbumOnClickListener, ServiceConnection,
        MusicService.OnTrackCompletionListener,
        MusicService.OnNotificationUpdateListener
{


    ViewPager2 viewPager;
    ViewPagerCustomAdapter viewPagerCustomAdapter;
    MainTrackListFragment mainTrackListFragment;
    MainAlbumFragment mainAlbumFragment;
    ListFragment listFragment;
    ActivityResultLauncher<Intent> activityResultLauncher;

    ColorStateList def;
    TextView item1;
    TextView item2;
    TextView item3;
    TextView select;


    Dialog addDialog;
    TextView addDialogCancelButton;
    TextView addDialogOkButton;
    EditText addDialogEditText;

    Dialog deleteDialog;
    TextView deleteDialogCancelButton;
    TextView deleteDialogOKButton;

    Dialog deleteListDialog;
    TextView deleteListDialogCancelButton;
    TextView deleteListDialogOKButton;

    MusicHelper musicHelper;
    AppBarLayout appBarLayout;
    LoadingDialog loadingDialog;

    ImageView searchButton;


    ConstraintLayout bottomBar;
    ImageView bottomBarImage;
    ImageButton prev;
    ImageButton play;
    ImageButton next;
    TextView bottomBarTrackName;
    TextView bottomBarArtist;

    int deletePosition;
    int deleteListPosition;

    boolean mBound;
    MusicService musicService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mBound=false;
        deletePosition=-1;
        deleteListPosition=-1;
        musicHelper=MusicHelper.getInstance();


        if(musicHelper.getMainList().getTracks().size()==0){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                musicHelper.loadMusicFiles(this);
                if(isMyServiceRunning(MusicService.class)==false){


                    Intent intentService=new Intent(MainActivity.this,MusicService.class);
                    if(Build.VERSION.SDK_INT>=26){
                        ContextCompat.startForegroundService(this,intentService);
                    }else{
                        startService(intentService);
                    }
                }
            }else{
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},11);
            }
        }
        musicHelper.load(this);
        musicHelper.loadTrackAll(getApplicationContext());


        //if service is not running start service




        //UI components

        if(listFragment==null){
            listFragment=new ListFragment();
        }else{
            if(listFragment.recyclerAdapterMainList!=null){
                listFragment.recyclerAdapterMainList.notifyDataSetChanged();
            }
        }


        if(mainTrackListFragment==null){
            mainTrackListFragment=new MainTrackListFragment();
        }else{
            if(mainTrackListFragment.recyclerAdapterMainTrackList!=null){
                mainTrackListFragment.recyclerAdapterMainTrackList.notifyDataSetChanged();
            }

        }


        if(mainAlbumFragment==null){
            mainAlbumFragment=new MainAlbumFragment();
        }else{
            if(mainAlbumFragment.recyclerAdapterMainAlbum!=null){
                mainAlbumFragment.recyclerAdapterMainAlbum.notifyDataSetChanged();
            }

        }

        if(viewPagerCustomAdapter==null){
            viewPagerCustomAdapter=new ViewPagerCustomAdapter(this);
        }
        appBarLayout=findViewById(R.id.tabLayout);

        viewPager=findViewById(R.id.viewpager2);
        viewPagerCustomAdapter.addFragment(listFragment,"lists");
        viewPagerCustomAdapter.addFragment(mainTrackListFragment,"mainTracks");
        viewPagerCustomAdapter.addFragment(mainAlbumFragment,"mainAlbums");
        viewPager.setAdapter(viewPagerCustomAdapter);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                onPageChange(position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
        //load files

        //tab layout
        item1 = findViewById(R.id.item1);
        item2 = findViewById(R.id.item2);
        item3 = findViewById(R.id.item3);
        item1.setOnClickListener(this);
        item2.setOnClickListener(this);
        item3.setOnClickListener(this);
        select = findViewById(R.id.select);
        def = item2.getTextColors();




        bottomBar=findViewById(R.id.mainActivityBottomBar);
        prev=findViewById(R.id.mainPrevButton);
        play=findViewById(R.id.mainPlayButton);
        next=findViewById(R.id.mainNextButton);
        bottomBarImage=findViewById(R.id.mainBottomImage);
        bottomBarTrackName=findViewById(R.id.mainBottomTrackName);
        bottomBarArtist=findViewById(R.id.mainBottomArtistName);





         bottomBar.setOnClickListener(view -> {
             Intent intent=new Intent(MainActivity.this,PlayScreenActivity.class);
             activityResultLauncher.launch(intent);
         });


         play.setOnClickListener(view -> {
             onPlayClicked();
         });
         prev.setOnClickListener(view -> {
             onPrevClicked();
         });
         next.setOnClickListener(view -> {
             onNextClicked();
         });








        //loading dialog
        loadingDialog=new LoadingDialog(this);
        //dialog
        addDialog=new Dialog(MainActivity.this);
        addDialog.setContentView(R.layout.add_dialog_layout);
        addDialog.getWindow().setBackgroundDrawableResource(R.drawable.add_dialog_background);
        addDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        addDialog.setCancelable(false);
        addDialog.getWindow().getAttributes().windowAnimations=R.style.dialogAnimation;
        addDialogCancelButton=addDialog.findViewById(R.id.AddDialogCancelButton);
        addDialogOkButton=addDialog.findViewById(R.id.addDialogOKButton);
        addDialogEditText=addDialog.findViewById(R.id.AddDialogEditText);
        addDialogCancelButton.setOnClickListener(view -> {
            addDialog.dismiss();
            addDialogEditText.setText("");
        });
        addDialogOkButton.setOnClickListener(view -> {
            addDialog.dismiss();
            String name=addDialogEditText.getText().toString();
            if(musicHelper.listControl(name) && name.isEmpty()==false){
                musicHelper.add_to_list(new TrackList(name));
                listFragment.recyclerAdapterMainList.notifyDataSetChanged();
                musicHelper.save(this);
            }else{
                Toast.makeText(this, "list name is not valid", Toast.LENGTH_SHORT).show();
            }
            addDialogEditText.setText("");
        });



        deleteDialog=new Dialog(MainActivity.this);
        deleteDialog.setContentView(R.layout.delete_dialog_layout);
        deleteDialog.getWindow().setBackgroundDrawableResource(R.drawable.add_dialog_background);
        deleteDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        deleteDialog.setCancelable(true);
        deleteDialog.getWindow().getAttributes().windowAnimations=R.style.dialogAnimation;
        deleteDialogCancelButton=deleteDialog.findViewById(R.id.deleteDialogCancel);
        deleteDialogOKButton=deleteDialog.findViewById(R.id.deleteDialogOkButton);
        deleteDialogCancelButton.setOnClickListener(view -> {
            deleteDialog.dismiss();
            deletePosition=-1;
        });
        deleteDialogOKButton.setOnClickListener(view -> {
            deleteDialog.dismiss();
            Music music=null;
            if(deletePosition!=-1 && deletePosition<musicHelper.getMainList().getTracks().size()){
               music=musicHelper.getMainList().getTracks().get(deletePosition);
                DeleteThread thread=new DeleteThread(musicHelper,music,deletePosition);
                thread.start();
            }

            deletePosition=-1;
        });

        deleteListDialog=new Dialog(MainActivity.this);
        deleteListDialog.setContentView(R.layout.delete_list_dialog_layout);
        deleteListDialog.getWindow().setBackgroundDrawableResource(R.drawable.add_dialog_background);
        deleteListDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        deleteListDialog.setCancelable(true);
        deleteListDialog.getWindow().getAttributes().windowAnimations=R.style.dialogAnimation;
        deleteListDialogCancelButton=deleteListDialog.findViewById(R.id.deleteListDialogCancel);
        deleteListDialogOKButton=deleteListDialog.findViewById(R.id.deleteListDialogOkButton);
        deleteListDialogCancelButton.setOnClickListener(view -> {
            deleteListDialog.dismiss();
            deleteListPosition=-1;
        });
        deleteListDialogOKButton.setOnClickListener(view -> {
            deleteListDialog.dismiss();
            if(deleteListPosition!=-1 && deleteListPosition<musicHelper.getCustomList().size()){
                musicHelper.deleteCustomList(deleteListPosition);
                if(listFragment!=null && listFragment.recyclerAdapterMainList!=null){
                    listFragment.recyclerAdapterMainList.notifyItemRemoved(deleteListPosition);
                    musicHelper.save(this);
                }
            }
            deleteListPosition=-1;
        });


        //search
        searchButton=findViewById(R.id.mainSearch);
        searchButton.setOnClickListener(view -> {
            Intent intent=new Intent(MainActivity.this,SearchActivity.class);
            activityResultLauncher.launch(intent);
        });


        //activity for result
        activityResultLauncher=registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result==null || result.getData()==null){
                            return;
                        }

                       //list add screen
                       if(result.getResultCode()==12){
                           boolean b=result.getData().getBooleanExtra("is_updated",false);
                           if(b){

                               if(listFragment!=null && listFragment.recyclerAdapterMainList!=null){
                                   listFragment.recyclerAdapterMainList.notifyDataSetChanged();
                               }
                               musicHelper.save(MainActivity.this);
                           }

                       //list activity
                       }else if(result.getResultCode()==17){
                           boolean b=result.getData().getBooleanExtra("is_updated",false);
                           if(b){

                           }
                           if(listFragment!=null && listFragment.recyclerAdapterMainList!=null){
                               listFragment.recyclerAdapterMainList.notifyDataSetChanged();
                           }
                       }

                       else if(result.getResultCode()==22){
                           boolean b=result.getData().getBooleanExtra("is_updated",false);
                           if(b){

                           }
                       }
                       //album activity
                       else if(result.getResultCode()==32){
                           boolean b=result.getData().getBooleanExtra("is_updated_album",false);
                           if(b){

                           }
                       }


                    }
                }
        );


    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            musicHelper.loadMusicFiles(this);
            if(isMyServiceRunning(MusicService.class)==false){


                Intent intentService=new Intent(MainActivity.this,MusicService.class);
                if(Build.VERSION.SDK_INT>=26){
                    ContextCompat.startForegroundService(this,intentService);
                }else{
                    startService(intentService);
                }
            }
        } else {
            System.exit(0);
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
        }
    }

    class LoadMusicThread extends Thread{
        MusicHelper musicHelper;
        Context context;
        LoadMusicThread(MusicHelper musicHelper,Context context){
          this.musicHelper=musicHelper;
        }
        @Override
        public void run() {
            super.run();

            runOnUiThread(() -> loadingDialog.showDialog("loading"));
            runOnUiThread(() -> loadingDialog.dismissDialog());
        }
    }

    @Override
    public void onClick(View view) {
        int selectedColor=ContextCompat.getColor(this,R.color.primary_text_color);
        int defaultColor=ContextCompat.getColor(this,R.color.foreground_1);
        if (view.getId() == R.id.item1){
            select.animate().x(0).setDuration(100);
            item1.setTextColor(selectedColor);
            item2.setTextColor(defaultColor);
            item3.setTextColor(defaultColor);
            viewPager.setCurrentItem(0,true);
        } else if (view.getId() == R.id.item2){
            item1.setTextColor(defaultColor);
            item2.setTextColor(selectedColor);
            item3.setTextColor(defaultColor);
            int size = item2.getWidth();
            viewPager.setCurrentItem(1,true);
            select.animate().x(size).setDuration(100);
        } else if (view.getId() == R.id.item3){
            item1.setTextColor(defaultColor);
            item3.setTextColor(selectedColor);
            item2.setTextColor(defaultColor);
            int size = item2.getWidth() * 2;
            viewPager.setCurrentItem(2,true);
            select.animate().x(size).setDuration(100);
        }
    }

    public void onPageChange(int position){
        int selectedColor=ContextCompat.getColor(this,R.color.primary_text_color);
        int defaultColor=ContextCompat.getColor(this,R.color.foreground_1);
        if (position==0){
            select.animate().x(0).setDuration(200);
            item1.setTextColor(selectedColor);
            item2.setTextColor(defaultColor);
            item3.setTextColor(defaultColor);
            viewPager.setCurrentItem(0,true);
        } else if (position==1){
            item1.setTextColor(defaultColor);
            item2.setTextColor(selectedColor);
            item3.setTextColor(defaultColor);
            int size = item2.getWidth();
            viewPager.setCurrentItem(1,true);
            select.animate().x(size).setDuration(200);
        } else if (position==2){
            item1.setTextColor(defaultColor);
            item3.setTextColor(selectedColor);
            item2.setTextColor(defaultColor);
            int size = item2.getWidth() * 2;
            viewPager.setCurrentItem(2,true);
            select.animate().x(size).setDuration(200);
        }
    }

    @Override
    public void addNewList() {
        openDialog();
    }

    public void openDialog(){
        addDialog.show();
    }

    public void openDialogDelete(){
        deleteDialog.show();
    }
    public void openDialogDeleteList(){
        deleteListDialog.show();
    }


    //main list clicked
    @Override
    public void onMainTrackListClicked(int position) {

        if(mBound && musicService!=null){
            if(musicHelper.getActiveTracklist().equals(musicHelper.getMainList())==false){
                musicHelper.setActiveTracklist(musicHelper.getMainList());
            }
            Music music=musicHelper.getMainList().getTracks().get(position);
            if(music!=null){
                updateMainTrackList(musicHelper.findActiveTrackListIndex());
                musicService.switchTrack(music);
                setBottomBar(music);
                if(musicHelper!=null && play!=null){
                    if(musicHelper.isPlaying()){
                        play.setImageResource(R.drawable.pause);
                    }else{
                        play.setImageResource(R.drawable.play);
                    }
                }
                updateMainTrackList(musicHelper.findActiveTrackListIndex());
            }
        }
    }




    //main track menu options
    @Override
    public void onAddClicked(int position) {
        Intent intent=new Intent(MainActivity.this,AddToListActivity.class);
        intent.putExtra("music_position",position);
        activityResultLauncher.launch(intent);
        //startActivity(intent);


    }

    @Override
    public void onDeleteClicked(int position) {
        this.deletePosition=position;
        openDialogDelete();
    }

    @Override
    public void onShareClicked(int position) {
        Music music;
        try{
            music=musicHelper.getMainList().getTracks().get(position);
        }catch (Exception e){
            return;
        }
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("audio/*");
        share.putExtra(Intent.EXTRA_STREAM, music.getUri());
        startActivity(Intent.createChooser(share, "Share Sound File"));
    }

    @Override
    public void onDetailClicked(int position) {

    }



    //list events
    @Override
    public void onClickMainList(int position) {

        Intent listIntent=new Intent(MainActivity.this,ListActivity.class);
        listIntent.putExtra("listIndex",position);
        activityResultLauncher.launch(listIntent);
    }

    @Override
    public void onLongClickMainList(int position) {

        this.deleteListPosition=position;
        openDialogDeleteList();
    }

    @Override
    public void onMainAlbumClicked(int position) {
        Intent albumIntent=new Intent(MainActivity.this,AlbumActivity.class);
        albumIntent.putExtra("albumIndex",position);
        activityResultLauncher.launch(albumIntent);
    }



    class DeleteThread extends Thread{
        MusicHelper musicHelper;
        Music deleteMusic;
        int position;
        boolean isDeleted=false;
        DeleteThread(MusicHelper musicHelper,Music music,int position){
            this.deleteMusic=music;
            this.musicHelper=musicHelper;
            this.position=position;
        }
        @Override
        public void run() {
            super.run();
            runOnUiThread(() -> loadingDialog.showDialog("loading"));
            if(this.deleteMusic!=null){
                isDeleted=musicHelper.deleteMusic(this.deleteMusic);
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(isDeleted){
                runOnUiThread(() -> {
                    updateRecyclerViewsDeleteTrack(position);
                });
                musicHelper.save(MainActivity.this);
            }
            runOnUiThread(() -> loadingDialog.dismissDialog());
        }
    }




    public void updateRecyclerViews(){
        if(listFragment!=null && listFragment.recyclerAdapterMainList!=null){
            listFragment.recyclerAdapterMainList.notifyDataSetChanged();
        }


        if(mainTrackListFragment!=null && mainTrackListFragment.recyclerAdapterMainTrackList!=null){
            mainTrackListFragment.recyclerAdapterMainTrackList.notifyDataSetChanged();
        }


        if(mainAlbumFragment!=null && mainAlbumFragment.recyclerAdapterMainAlbum!=null){
            mainAlbumFragment.recyclerAdapterMainAlbum.notifyDataSetChanged();
        }
    }


    public void updateRecyclerViewsDeleteTrack(int position){
        if(listFragment!=null && listFragment.recyclerAdapterMainList!=null){
            listFragment.recyclerAdapterMainList.notifyDataSetChanged();
        }


        if(mainTrackListFragment!=null && mainTrackListFragment.recyclerAdapterMainTrackList!=null){
            mainTrackListFragment.recyclerAdapterMainTrackList.notifyItemRemoved(position);
        }


        if(mainAlbumFragment!=null && mainAlbumFragment.recyclerAdapterMainAlbum!=null){
            mainAlbumFragment.recyclerAdapterMainAlbum.notifyDataSetChanged();
        }
    }


    public void setBottomBar(Music music){
        if(music!=null){
            bottomBarTrackName.setText(music.getTrackName());
            bottomBarArtist.setText(music.getArtistName());
            Long albumID=music.getAlbum_id();
            Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
            Uri imageUri = Uri.withAppendedPath(sArtworkUri, String.valueOf(albumID));
            Glide.with(this).load(imageUri).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.music_icon).into(bottomBarImage);
            bottomBarImage.setClipToOutline(true);
        }
    }

    //service methods
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MyBinder myBinder= (MusicService.MyBinder) iBinder;
        musicService=myBinder.getService();
        musicService.setOnTrackCompletionListener(this);
        musicService.setOnNotificationUpdateListener(this);
        setBottomBar(musicHelper.getActiveTrack());
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService=null;

    }

    private void updateMainTrackList(){
        if(mainTrackListFragment!=null && mainTrackListFragment.recyclerAdapterMainTrackList!=null){
            mainTrackListFragment.recyclerAdapterMainTrackList.notifyDataSetChanged();
        }
    }
    private void updateMainTrackList(int position){
        if(position==-1){
            return;
        }
        if(mainTrackListFragment!=null && mainTrackListFragment.recyclerAdapterMainTrackList!=null){
            mainTrackListFragment.recyclerAdapterMainTrackList.notifyItemChanged(position);
        }
    }
    public void onPlayClicked(){
        if(mBound && musicService!=null){
            musicService.playButtonClicked();
            if(musicHelper.isPlaying()){
                play.setImageResource(R.drawable.pause);
            }else{
                play.setImageResource(R.drawable.play);
            }
        }
    }
    public void onPrevClicked(){
        if(mBound && musicService!=null){
            updateMainTrackList(musicHelper.findActiveTrackListIndex());
            musicService.prevButtonClicked();
            setBottomBar(musicHelper.getActiveTrack());
            play.setImageResource(R.drawable.pause);
            updateMainTrackList(musicHelper.findActiveTrackListIndex());
        }
    }
    public void onNextClicked(){
        if(mBound && musicService!=null){
            updateMainTrackList(musicHelper.findActiveTrackListIndex());
            musicService.nextButtonClicked();
            setBottomBar(musicHelper.getActiveTrack());
            play.setImageResource(R.drawable.pause);
            updateMainTrackList(musicHelper.findActiveTrackListIndex());
        }
    }

    @Override
    public void onTrackCompleted() {
        //onNextClicked();
        setBottomBar(musicHelper.getActiveTrack());
        updateMainTrackList();
    }


    @Override
    protected void onPause() {
        unbindService(this);
        mBound=false;
        super.onPause();

    }

    @Override
    protected void onResume() {
        Intent intent=new Intent(this,MusicService.class);
        mBound=true;
        bindService(intent,this,BIND_AUTO_CREATE);
        updateRecyclerViews();
        super.onResume();
        if(musicHelper!=null && play!=null){
            if(musicHelper.isPlaying()){
                play.setImageResource(R.drawable.pause);
            }else{
                play.setImageResource(R.drawable.play);
            }
        }
    }


    @Override
    public boolean onNotificationPrevClicked() {
        if(mBound && musicService!=null){
            onPrevClicked();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNotificationPlayClicked() {
        if(mBound && musicService!=null){
            onPlayClicked();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNotificationNextClicked() {
        if(mBound && musicService!=null){
            onNextClicked();
            return true;
        }
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(musicService!=null){
            if(musicService.onNotificationUpdateListener==this){
                musicService.onNotificationUpdateListener=null;
            }
            if(musicService.onTrackCompletionListener==this){
                musicService.onTrackCompletionListener=null;
            }
        }
    }


}