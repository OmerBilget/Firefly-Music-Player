package com.example.firefly;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MusicHelper {

    private final String SHARED_PREFS="shared_prefs";
    private final String LIST_SAVE_DATA="list";
    private final String SHUFFLE_DATA="shuffle";
    private final String REPEAT_DATA="repeat";
    private final String LIST_ACTIVE_DATA="list_Active";
    private final String ACTIVE_TRACK_DATA="active_track";
    private final String ACTIVE_TRACK_CURRENT_POSITION_DATA="active_pos";
    private TrackList mainList;
    private TrackList favorites;
    private List<AlbumList> albums;
    private List<TrackList> customList;


    private boolean shuffle;
    private boolean repeat;
    private boolean isPlaying;
    private Music activeTrack;
    private TrackList activeTracklist;
    private int activeTracklistIndex;
    private final Random random;
    private boolean isLoadCurrenPosition;

    //singleton object

    private MusicHelper() {

        mainList=new TrackList("mainList");
        albums=new ArrayList<>();
        favorites=new TrackList("Favorites");
        customList=new ArrayList<>();
        customList.add(favorites);
        isPlaying=false;
        shuffle=false;
        repeat=false;
        activeTracklistIndex=0;
        activeTracklist=mainList;
        random=new Random();
        isLoadCurrenPosition=false;

    }

    private static MusicHelper musicHelper;
    public static MusicHelper getInstance(){
        if(musicHelper==null){
            musicHelper=new MusicHelper();
        }
        return musicHelper;
    }

    //getter and setters


    public boolean isLoadCurrenPosition() {
        return isLoadCurrenPosition;
    }

    public void setLoadCurrenPosition(boolean loadCurrenPosition) {
        isLoadCurrenPosition = loadCurrenPosition;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public boolean isShuffle() {
        return shuffle;
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public Music getActiveTrack() {
        return activeTrack;
    }

    public void setActiveTrack(Music activeTrack) {
        this.activeTrack = activeTrack;
    }

    public TrackList getActiveTracklist() {
        return activeTracklist;
    }

    public void setActiveTracklist(TrackList activeTracklist) {
        this.activeTracklist = activeTracklist;
    }

    public int getActiveTracklistIndex() {
        return activeTracklistIndex;
    }

    public void setActiveTracklistIndex(int activeTracklistIndex) {
        this.activeTracklistIndex = activeTracklistIndex;
    }

    public void setAlbums(List<AlbumList> albums) {
        this.albums = albums;
    }


    public TrackList getMainList() {
        return mainList;
    }

    public void setMainList(TrackList mainList) {
        this.mainList = mainList;
    }

    public TrackList getFavorites() {
        return favorites;
    }

    public void setFavorites(TrackList favorites) {
        this.favorites = favorites;
    }


    public List<TrackList> getCustomList() {
        return customList;
    }

    public void setCustomList(List<TrackList> customList) {
        this.customList = customList;
    }

    public List<AlbumList> getAlbums() {
        return albums;
    }








    //methods
    public void sortMusicFiles(){
        int size=mainList.getTracks().size();
        for(int i=0;i<size-1;i++){
            for(int j=0;j<size-i-1;j++){
                if(mainList.getTracks().get(j).getTrackName().compareToIgnoreCase(mainList.getTracks().get(j+1).getTrackName())>0){
                    Music tmp=mainList.getTracks().get(j);
                    mainList.getTracks().set(j,mainList.getTracks().get(j+1));
                    mainList.getTracks().set(j+1,tmp);
                }
            }
        }
    }

    public void sortTrackList(TrackList trackList){
        if(trackList==null){
            return;
        }
        int size=trackList.getTracks().size();
        for(int i=0;i<size-1;i++){
            for(int j=0;j<size-i-1;j++){
                if(trackList.getTracks().get(j).getTrackName().compareToIgnoreCase(trackList.getTracks().get(j+1).getTrackName())>0){
                    Music tmp=trackList.getTracks().get(j);
                    trackList.getTracks().set(j,trackList.getTracks().get(j+1));
                    trackList.getTracks().set(j+1,tmp);
                }
            }
        }
    }



    public void loadMusicFiles(Context context){

        ContentResolver contentResolver=context.getContentResolver();
        Uri uri= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor=contentResolver.query(uri,null,MediaStore.Audio.Media.IS_MUSIC + " != 0",null,null);
        if(cursor==null){
            Toast.makeText(context,"music getting error",Toast.LENGTH_SHORT).show();
        }
        else if(!cursor.moveToFirst()){
            Toast.makeText(context,"no music",Toast.LENGTH_SHORT).show();
        }else{
            while (cursor.moveToNext()){
                int index=cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
                String songName = cursor.getString(index);

                index=cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                long song_id = cursor.getLong(index);
                Uri songUri= ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,song_id);

                index=cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                String fullpath = cursor.getString(index);

                index=cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                String albumName = cursor.getString(index);

                index=cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                String title= cursor.getString(index);

                index=cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                String artist_name = cursor.getString(index);

                index=cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                long album_id = cursor.getLong(index);

                index=cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                String duration = cursor.getString(index);


                Music music=new Music(song_id,title,artist_name,albumName,getTimeFormatted(Integer.valueOf(duration.trim())),duration,fullpath,songName,album_id,songUri);
                this.mainList.getTracks().add(music);
            }
        }

        cursor.close();
        int count=mainList.getTracks().size();
        sortMusicFiles();
        fillLists(context);
        //Toast.makeText(context,String.valueOf(count),Toast.LENGTH_SHORT).show();
        if(mainList.getTracks().size()>0){
            activeTracklist=mainList;
            activeTrack=activeTracklist.getTracks().get(0);
            activeTracklistIndex=0;
        }
        loadShuffle(context);
        loadRepeat(context);
    }

    private void fillLists(Context context) {
        fillAlbums(context);
        sortAlbumsByName();
    }

    private void fillAlbums(Context context) {
        int size=mainList.getTracks().size();
        if(size==0){
            return ;
        }
        Music music;
        music=mainList.getTracks().get(0);
        albums.add(new AlbumList(music.getArtistName(),music.getAlbum_id(),new TrackList(music.getAlbumName())));
        albums.get(0).getAlbumTracks().getTracks().add(music);
        for(int i=1;i<size;i++){
            music=mainList.getTracks().get(i);
            int index=isContainAlbum(music);
            if(index==-1){
                //new album
                albums.add(new AlbumList(music.getArtistName(),music.getAlbum_id(),new TrackList(music.getAlbumName())));
                albums.get(albums.size()-1).getAlbumTracks().getTracks().add(music);
            }else{
                albums.get(index).getAlbumTracks().getTracks().add(music);
            }

        }
    }
    private int isContainAlbum(Music music){
        int size=albums.size();
        for(int i=0;i<size;i++){
            if(albums.get(i).getAlbum_ID()==music.getAlbum_id()){
                return i;
            }
        }
        return -1;
    }


    public void sortAlbumsByName(){
        int size=albums.size();
        for(int i=0;i<size-1;i++){
            for(int j=0;j<size-i-1;j++){
                if(albums.get(j).getAlbumTracks().getName().compareToIgnoreCase(albums.get(j+1).getAlbumTracks().getName())>0){
                    AlbumList tmp=albums.get(j);
                    albums.set(j,albums.get(j+1));
                    albums.set(j+1,tmp);
                }
            }
        }
    }


    public void sortAlbumsByArtist(){
        int size=albums.size();
        for(int i=0;i<size-1;i++){
            for(int j=0;j<size-i-1;j++){
                if(albums.get(j).getAlbumArtist().compareToIgnoreCase(albums.get(j+1).getAlbumArtist())>0){
                    AlbumList tmp=albums.get(j);
                    albums.set(j,albums.get(j+1));
                    albums.set(j+1,tmp);
                }
            }
        }
    }


    public boolean listControl(String name){
        int size=customList.size();
        for(int i=0;i<size;i++){
            if(customList.get(i).getName().equals(name)){
                return false;
            }
        }
        return true;
    }


    public boolean isContainMusic(TrackList trackList,Music music){
        int size=trackList.getTracks().size();
        for(int i=0;i<size;i++){
            if(trackList.getTracks().get(i).equals(music)){
                return true;
            }
        }
        return false;
    }
    public void add_to_list(TrackList newList){
        customList.add(newList);
    }


    public void save(Context context){
        List<TrackListSave> customListSave=new ArrayList<>();
        int size=customList.size();
        for(int i=0;i<size;i++){
            customListSave.add(new TrackListSave(customList.get(i).getName()));
            int sizeInnerLoop=customList.get(i).getTracks().size();
            for(int j=0;j<sizeInnerLoop;j++){
                Music music=customList.get(i).getTracks().get(j);
                customListSave.get(i).getList().add(new MusicSave(music.getFileName(),music.getId()));
            }
        }
        Gson gson=new Gson();

        String json=gson.toJson(customListSave,new TypeToken<List<TrackListSave>>(){}.getType());
        if(json==null){
            //Toast.makeText(context, "save error", Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREFS,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString(LIST_SAVE_DATA, json);
        editor.apply();

    }

    public void saveTrackList(Context context){
        TrackListSave activeTrackListSave=new TrackListSave("activeList");
        int size=activeTracklist.getTracks().size();
        if(activeTracklist==mainList){
            SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREFS,Context.MODE_PRIVATE);
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putString(LIST_ACTIVE_DATA,null);
            editor.apply();
        }
        for(int i=0;i<size;i++){
            Music music=activeTracklist.getTracks().get(i);
            activeTrackListSave.getList().add(new MusicSave(music.getTrackName(),music.getId()));
        }
        Gson gson=new Gson();
        String json=gson.toJson(activeTrackListSave,TrackListSave.class);
        if(json==null){
            return;
        }
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREFS,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString(LIST_ACTIVE_DATA, json);
        editor.commit();
    }

    public void saveActiveTrack(Context context,Music music,long currentPosition){
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREFS,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putLong(ACTIVE_TRACK_DATA,music.getId());
        editor.putLong(ACTIVE_TRACK_CURRENT_POSITION_DATA,currentPosition);
        editor.commit();

    }
    public Music loadActiveTrack(Context context){
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREFS,Context.MODE_PRIVATE);
        long music_id=sharedPreferences.getLong(ACTIVE_TRACK_DATA,-100);
        if(music_id!=-100 && mainList!=null){
            Music music=findMusicByID(music_id);
            return music;
        }
        return null;
    }
    public long loadActivePosition(Context context){
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREFS,Context.MODE_PRIVATE);
        long currentPosition=sharedPreferences.getLong(ACTIVE_TRACK_CURRENT_POSITION_DATA,0);
        return currentPosition;
    }

    public TrackList loadActiveTrackList(Context context){
        TrackListSave activeTrackListSave;
        TrackList activeTrackList=new TrackList("activeTrackList");
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREFS,Context.MODE_PRIVATE);
        String json=sharedPreferences.getString(LIST_ACTIVE_DATA,null);
        Gson gson=new Gson();
        if(json==null){
            return mainList;
        }
        activeTrackListSave=gson.fromJson(json,TrackListSave.class);
        if(activeTrackListSave!=null){
            int size=activeTrackListSave.getList().size();
            for(int i=0;i<size;i++){
                Music music=findMusicByID(activeTrackListSave.getList().get(i).getID());
                if(music!=null){
                    activeTrackList.getTracks().add(music);
                }
            }
        }
        return activeTrackList;
    }

    public void saveTrackAll(Context context,Music music,long currentPosition){
        if(music==null){
            return;
        }
        saveActiveTrack(context,music,currentPosition);
        saveTrackList(context);
    }
    public void loadTrackAll(Context context){
        if(mainList==null){
            return;
        }
        Music music=loadActiveTrack(context);
        if(music!=null){
            activeTrack=music;
        }
        TrackList trackList=loadActiveTrackList(context);
        if(trackList!=null){
            activeTracklist=trackList;
        }
    }


    public void saveShuffle(Context context){
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREFS,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean(SHUFFLE_DATA,shuffle);
        editor.apply();
    }
    public void loadShuffle(Context context){
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREFS,Context.MODE_PRIVATE);
        shuffle=sharedPreferences.getBoolean(SHUFFLE_DATA,false);
    }

    public void saveRepeat(Context context){
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREFS,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean(REPEAT_DATA,repeat);
        editor.apply();
    }
    public void loadRepeat(Context context){
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREFS,Context.MODE_PRIVATE);
        repeat=sharedPreferences.getBoolean(REPEAT_DATA,false);
    }
    public void load(Context context){
        List<TrackListSave> customListSave;
        List<TrackList> customList=new ArrayList<>();
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREFS,Context.MODE_PRIVATE);
        String json=sharedPreferences.getString(LIST_SAVE_DATA,null);
        Gson gson=new Gson();
        if(json!=null){
            customListSave=gson.fromJson(json,new TypeToken<List<TrackListSave>>(){}.getType());
        }else{
            return;
        }
        if(customListSave!=null){
            int size=customListSave.size();
            for(int i=0;i<size;i++){
                customList.add(new TrackList(customListSave.get(i).getListName()));
                int innerLoopSize=customListSave.get(i).getList().size();
                for(int j=0;j<innerLoopSize;j++){
                    Music music=findMusicByID(customListSave.get(i).getList().get(j).getID());
                    if(music!=null){
                        customList.get(i).getTracks().add(music);
                    }
                }
            }
        }else{
            return;
        }
        this.customList=customList;
        this.favorites=this.customList.get(0);

    }

    public Music findMusicByID(long id){
        int size=mainList.getTracks().size();
        for(int i=0;i<size;i++){
            Music music=mainList.getTracks().get(i);
            if(music.getId()==id){
                return music;
            }
        }
        return null;
    }

    public boolean removeFromList(int listIndex,Music music){
        if(listIndex<0 || listIndex>customList.size() || music==null){
            return false;
        }
        boolean isRemoved=customList.get(listIndex).getTracks().remove(music);
        return isRemoved;
    }


    public boolean removeFromList(int listIndex,int index){
        if(listIndex<0 || listIndex>customList.size() ){
            return false;
        }
        try{
            customList.get(listIndex).getTracks().remove(index);
        }catch (IndexOutOfBoundsException e){
            e.printStackTrace();
        }
        return true;
    }

    public boolean deleteMusic(Music music){

        //first custom list delete
        deleteFromCustomList(music);
        //second albums
        deleteFromAlbums(music);
        //third main list
        return deleteFromMainList(music);
    }
    public void deleteFromCustomList(Music music){
        int size=customList.size();
        for(int i=0;i<size;i++){
            customList.get(i).getTracks().remove(music);
        }
    }
    public void deleteFromAlbums(Music music){
        int size=albums.size();
        for(int i=0;i<size;i++){
            albums.get(i).getAlbumTracks().getTracks().remove(music);
        }
    }

    public void deleteCustomList(int position){
        try{
            customList.remove(position);
        }catch (IndexOutOfBoundsException e){
            e.printStackTrace();
        }
    }
    public boolean deleteFromMainList(Music music){
        return mainList.getTracks().remove(music);
    }

    public String getTimeFormatted(int time){
        int seconds=(time/1000)%60;
        int minute=time/60000;
        String secondString;
        if(seconds<10){
            secondString="0"+ seconds;
        }else{
            secondString=Integer.toString(seconds);
        }
        return minute +":"+secondString;
    }

    public boolean isInFavorites(Music music){
        if(music==null){
            return false;
        }
        return favorites.getTracks().contains(music);
    }

    public boolean addToTheFavorites(Music music){
        if(music==null){
            return false;
        }
        if(customList!=null && customList.get(0)!=null){
            if(customList.get(0).getTracks().contains(music)==false){
                customList.get(0).getTracks().add(music);
                return true;
            }
        }
        return false;
    }
    public boolean removeFromTheFavorites(Music music){
        if(music==null){
            return false;
        }
        if(customList!=null && customList.get(0)!=null){
            return customList.get(0).getTracks().remove(music);
        }
        return false;
    }

    public int findActiveTrackListIndex(){
      return activeTracklist.getTracks().indexOf(activeTrack);
    }

    public Music getNextSong(){
        if(repeat){
            if(activeTrack!=null){
                return activeTrack;
            }else{
                return null;
            }
        }
        int index=findActiveTrackListIndex();
        if(index==-1){
            //if song not found
            if(activeTracklist.getTracks().size()>0){
                return activeTracklist.getTracks().get(0);
            }else if(activeTracklist.getTracks().size()==0){
                //list is empty
                return activeTrack;
            }
        }else{
            if(shuffle){
                int size=activeTracklist.getTracks().size();

                int randomNumber=random.nextInt(size);
                return activeTracklist.getTracks().get(randomNumber);
            }else{
                if(activeTracklist.getTracks().size()>0 ){
                    //if last index
                    if(index==activeTracklist.getTracks().size()-1){
                        return activeTracklist.getTracks().get(0);
                    }else{
                        return activeTracklist.getTracks().get(index+1);
                    }

                }
            }

        }
        return null;
    }

    public Music getPrevSong(){
        if(repeat){
            if(activeTrack!=null){
                return activeTrack;
            }else{
                return null;
            }
        }
        int index=findActiveTrackListIndex();
        if(index==-1){
            //if song not found
            if(activeTracklist.getTracks().size()>0){
                return activeTracklist.getTracks().get(0);
            }else if(activeTracklist.getTracks().size()==0){
                //list is empty
                return activeTrack;
            }
        }else{
            if(shuffle){
                int size=activeTracklist.getTracks().size();

                int randomNumber=random.nextInt(size);
                return activeTracklist.getTracks().get(randomNumber);
            }else{
                if(activeTracklist.getTracks().size()>0 ){
                    //if last index
                    if(index==0){
                        return activeTracklist.getTracks().get(activeTracklist.getTracks().size()-1);
                    }else{
                        return activeTracklist.getTracks().get(index-1);
                    }

                }
            }
        }
        return null;
    }

}
