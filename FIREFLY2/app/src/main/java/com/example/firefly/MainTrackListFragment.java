package com.example.firefly;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;



public class MainTrackListFragment extends Fragment implements RecyclerAdapterMainTrackList.MainTrackListOnClickListener , RecyclerAdapterMainTrackList.MainTrackListOptionOnClickListener {

    RecyclerView recyclerView;
    RecyclerAdapterMainTrackList recyclerAdapterMainTrackList;
    RecyclerAdapterMainTrackList.MainTrackListOptionOnClickListener optionOnClickListener;
    RecyclerAdapterMainTrackList.MainTrackListOnClickListener listOnClickListener;
    LinearLayoutManager linearLayoutManager;
    View view;
    public MainTrackListFragment() {
        // Required empty public constructor

    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView=view.findViewById(R.id.mainTrackListFragmentRecyclerView);
        recyclerAdapterMainTrackList=new RecyclerAdapterMainTrackList(view.getContext(),this,this);
        recyclerView.setAdapter(recyclerAdapterMainTrackList);
        linearLayoutManager=new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_main_track_list, container, false);
        this.view=view;
        return view;
    }




    @Override
    public void onMainTrackListClicked(int position) {
        if(listOnClickListener!=null){
            listOnClickListener.onMainTrackListClicked(position);
        }
    }

    @Override
    public void onAddClicked(int position) {

        if(optionOnClickListener!=null){
            optionOnClickListener.onAddClicked(position);
        }
    }

    @Override
    public void onDeleteClicked(int position) {

        if(optionOnClickListener!=null){
            optionOnClickListener.onDeleteClicked(position);
        }
    }

    @Override
    public void onShareClicked(int position) {
        if(optionOnClickListener!=null){
            optionOnClickListener.onShareClicked(position);
        }
    }

    @Override
    public void onDetailClicked(int position) {

        if(optionOnClickListener!=null){
            optionOnClickListener.onDeleteClicked(position);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof MainActivity){
            optionOnClickListener= (RecyclerAdapterMainTrackList.MainTrackListOptionOnClickListener) context;
            listOnClickListener= (RecyclerAdapterMainTrackList.MainTrackListOnClickListener) context;
        }
    }
}