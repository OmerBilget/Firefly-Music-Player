package com.example.firefly;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


public class MainAlbumFragment extends Fragment implements RecyclerAdapterMainAlbum.MainAlbumOnClickListener {

    RecyclerView recyclerView;
    RecyclerAdapterMainAlbum recyclerAdapterMainAlbum;
    MusicHelper musicHelper;
    GridLayoutManager gridLayoutManager;
    RecyclerAdapterMainAlbum.MainAlbumOnClickListener mainAlbumOnClickListener;
    View view;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView=view.findViewById(R.id.AlbumRecyclerView);
        recyclerAdapterMainAlbum=new RecyclerAdapterMainAlbum(getActivity(),this);
        recyclerView.setAdapter(recyclerAdapterMainAlbum);
        gridLayoutManager=new GridLayoutManager(getActivity(),2,RecyclerView.VERTICAL,false);
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_main_album, container, false);
        this.view=view;
        return view;
    }

    @Override
    public void onMainAlbumClicked(int position) {
        if(mainAlbumOnClickListener!=null){
            mainAlbumOnClickListener.onMainAlbumClicked(position);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof MainActivity){
            mainAlbumOnClickListener= (RecyclerAdapterMainAlbum.MainAlbumOnClickListener) context;
        }
    }
}