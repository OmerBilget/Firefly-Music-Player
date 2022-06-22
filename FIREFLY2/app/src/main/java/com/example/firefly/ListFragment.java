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
import android.widget.ImageView;


public class ListFragment extends Fragment implements RecyclerAdapterMainList.MainListClickListener {


    RecyclerView recyclerView;
    RecyclerAdapterMainList recyclerAdapterMainList;
    LinearLayoutManager linearLayoutManager;
    ListFragmentAddListener listFragmentAddListener;
    RecyclerAdapterMainList.MainListClickListener mainListClickListener;
    ImageView addButton;
    View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView=view.findViewById(R.id.ListRecyclerView);
        recyclerAdapterMainList=new RecyclerAdapterMainList(getActivity(),this);
        recyclerView.setAdapter(recyclerAdapterMainList);
        linearLayoutManager=new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        addButton=view.findViewById(R.id.listFragmentAdd);
        addButton.setOnClickListener(v-> {
            if(listFragmentAddListener!=null){
                listFragmentAddListener.addNewList();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view= inflater.inflate(R.layout.fragment_list, container, false);
        return view;
    }

    @Override
    public void onClickMainList(int position) {
        if(mainListClickListener!=null){
            mainListClickListener.onClickMainList(position);
        }
    }

    @Override
    public void onLongClickMainList(int position) {
        if(mainListClickListener!=null){
            mainListClickListener.onLongClickMainList(position);
        }
    }

    public interface ListFragmentAddListener{
        void addNewList();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof MainActivity){
            listFragmentAddListener= (ListFragmentAddListener) context;
            mainListClickListener= (RecyclerAdapterMainList.MainListClickListener) context;
        }
    }
}