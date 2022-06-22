package com.example.firefly;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AddToListTrackSelectActivity extends AppCompatActivity implements RecyclerAdapterAddToListTrackSelect.AddToListCommunicator {
    ImageView backButton;
    Button OkButton;
    TextView countText;
    EditText editText;


    RecyclerView recyclerView;
    RecyclerAdapterAddToListTrackSelect recyclerAdapterAddToListTrackSelect;
    LinearLayoutManager linearLayoutManager;
    CheckBox checkBox;

    MusicHelper musicHelper;
    int listIndex;
    int selectedCount;
    int totalCount;
    TrackList trackList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_list_track_select);
        listIndex=getIntent().getIntExtra("listIndexSelectTracks",-1);

        musicHelper=MusicHelper.getInstance();

        backButton=findViewById(R.id.addToListTrackSelectBackButton);
        OkButton=findViewById(R.id.addToListTrackSelectOkButton);
        editText=findViewById(R.id.addToListTrackSelectEditText);
        checkBox=findViewById(R.id.addToListTrackSelectCheckButtonALL);
        countText=findViewById(R.id.addToListTrackSelectSelectedCount);

        trackList=new TrackList("search");
        selectedCount=0;
        totalCount=musicHelper.getMainList().getTracks().size();


        recyclerView=findViewById(R.id.addToListTrackSelectRecyclerView);
        recyclerAdapterAddToListTrackSelect=new RecyclerAdapterAddToListTrackSelect(this,trackList,this);

        recyclerView.setAdapter(recyclerAdapterAddToListTrackSelect);
        linearLayoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        setCountText(selectedCount,totalCount);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(recyclerView!=null && recyclerAdapterAddToListTrackSelect!=null){
                    recyclerAdapterAddToListTrackSelect.getFilter().filter(editable.toString());

                }
            }
        });


        if(listIndex!=-1){

        }
        backButton.setOnClickListener(view -> {
            finishAct(false);
        });

        OkButton.setOnClickListener(view -> {
            boolean isTrackAdded=addToCustomList();
            finishAct(true);
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(recyclerView!=null && recyclerAdapterAddToListTrackSelect!=null){

                    recyclerAdapterAddToListTrackSelect.fillCheckList(b);
                    recyclerAdapterAddToListTrackSelect.notifyDataSetChanged();

                    int checkedCount=0;
                    if(b){
                        checkedCount=recyclerAdapterAddToListTrackSelect.getItemCount();
                    }
                    setCountText(checkedCount,totalCount);

                }
            }
        });
    }

    private void finishAct(boolean b){
        Intent resultIntent = new Intent();
        resultIntent.putExtra("isAddToList", b);
        setResult(27, resultIntent);
        finish();
    }

    private void setCountText(int selectedCount,int totalCount){
        if(countText!=null){
            countText.setText(String.valueOf(selectedCount)+"/"+String.valueOf(totalCount));
        }
    }

    @Override
    public void onCheckboxClicked(int size) {

        setCountText(size,totalCount);
    }

    @Override
    public void onSizeChanged(int size) {
        totalCount=size;
        selectedCount=0;
        setCountText(selectedCount,totalCount);
    }

    private boolean addToCustomList(){
        boolean flag=false;
        if(recyclerView!=null && recyclerAdapterAddToListTrackSelect!=null && musicHelper!=null && musicHelper.getCustomList()!=null && listIndex!=-1){
            TrackList trackList=recyclerAdapterAddToListTrackSelect.searchList;
            int size=trackList.getTracks().size();
            for(int i=0;i<size;i++){
                //if selected
                if(recyclerAdapterAddToListTrackSelect.checkedList.get(i)){
                    Music music=trackList.getTracks().get(i);
                    if(musicHelper.getCustomList().get(listIndex).getTracks().contains(music)==false){
                        musicHelper.getCustomList().get(listIndex).getTracks().add(music);
                        flag=true;
                    }
                }
            }

        }
        return flag;
    }
}