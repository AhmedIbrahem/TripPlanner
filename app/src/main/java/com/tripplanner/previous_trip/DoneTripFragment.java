package com.tripplanner.previous_trip;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.google.android.material.snackbar.Snackbar;
import com.tripplanner.Constants;
import com.tripplanner.R;
import com.tripplanner.data_layer.local_data.entity.Note;
import com.tripplanner.data_layer.local_data.entity.Trip;
import com.tripplanner.databinding.DoneTripFragmentBinding;

import java.util.ArrayList;
import java.util.List;

public class DoneTripFragment extends Fragment {

    private DoneTripViewModel mViewModel;
    private RecyclerView finishedTripRecView;
    private TripAdapter finishedTripAdapter;
    List<Trip> finshedtripList=new ArrayList<>();
    ConstraintLayout frameLayout;
    DoneTripFragmentBinding binding;
    List<Note> notes=new ArrayList<>();
    RelativeLayout emptystate;

    private static final String TAG = "DoneTripFragment";
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
         binding = DataBindingUtil.inflate(
                inflater, R.layout.done_trip_fragment, container, false);
        View view = binding.getRoot();
        emptystate=view.findViewById(R.id.emptyState);
        if(isOnline()) {
            emptystate.setVisibility(View.INVISIBLE);
        }
        else
        {
            emptystate.setVisibility(View.VISIBLE);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        finishedTripRecView = binding.finishedTripRecyclerView;
        frameLayout = binding.mainlayout;
        finishedTripAdapter = new TripAdapter(finshedtripList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        finishedTripRecView.setLayoutManager(mLayoutManager);
        finishedTripRecView.setAdapter(finishedTripAdapter);
        mViewModel = ViewModelProviders.of(this).get(DoneTripViewModel.class);
        // TODO: Use the ViewModel
        mViewModel.getDoneTrip().observe(getViewLifecycleOwner(), trips -> {
            finishedTripAdapter.setArray(trips);
            finshedtripList=trips;
            if(finshedtripList.size()==0)
            {
                binding.emptyStateId.setVisibility(View.VISIBLE);
            }
            else
            {
                binding.emptyStateId.setVisibility(View.INVISIBLE);
                finishedTripRecView.setVisibility(View.VISIBLE);

            }
        });
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback_finished = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, new RecyclerItemTouchHelper.RecyclerItemTouchHelperListener() {
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
                if (viewHolder instanceof TripAdapter.PreviousTripViewHandler) {
                    String name = finshedtripList.get(viewHolder.getAdapterPosition()).getName();

                    final Trip deletedItem = finshedtripList.get(viewHolder.getAdapterPosition());
                    final int deletedIndex = viewHolder.getAdapterPosition();
                    mViewModel.deleteTrip((int) deletedItem.getId());
                    notes=mViewModel.getTripNotes(deletedItem.getId());
                    finishedTripAdapter.removeItem(viewHolder.getAdapterPosition());
                    Snackbar snackbar = Snackbar
                            .make(frameLayout, name + " removed from trip!", Snackbar.LENGTH_LONG);
                    snackbar.setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finishedTripAdapter.restoreItem(deletedItem, deletedIndex);
                            mViewModel.insertTrip(deletedItem, (ArrayList<Note>) notes);

                        }
                    });
                    snackbar.setActionTextColor(Color.YELLOW);
                    snackbar.show();
                }
            }
        });
        new ItemTouchHelper(itemTouchHelperCallback_finished).attachToRecyclerView(finishedTripRecView);

    }
    public  boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }


}
