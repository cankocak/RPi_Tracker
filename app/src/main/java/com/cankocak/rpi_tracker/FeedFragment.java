package com.cankocak.rpi_tracker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cankocak.rpi_tracker.databinding.FragmentFeedBinding;

public class FeedFragment extends Fragment implements FeedRecyclerViewAdapter.OnClickListener {
    private FragmentFeedBinding binding;

    private FeedViewModel feedViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // FeedViewModel initialization
        feedViewModel = new ViewModelProvider(requireActivity()).get(FeedViewModel.class);
        feedViewModel.init(requireActivity().getApplicationContext());
        feedViewModel.getLiveFeedUpdatedFlag().observe(requireActivity(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (integer > 0) {
                    refreshFeed();
                } else {
                    Toast.makeText(requireContext(),
                            "RPI_Tracker: " + "Error while updating RSS feed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void refreshFeed() {
        FeedRecyclerViewAdapter adapter =
                new FeedRecyclerViewAdapter(requireContext(),
                                            feedViewModel.getFeed(),
                                            this);
        binding.feedRecyclerView.setAdapter(adapter);
        binding.feedRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        binding.feedProgressBar.setVisibility(View.GONE);
        Log.i("RPI_Tracker", "Feed list GUI updated");
    }

    private void requestFeedUpdate() {
        binding.feedProgressBar.setVisibility(View.VISIBLE);
        feedViewModel.updateFeed();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFeedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.feedProgressBar.setVisibility(View.GONE);
        binding.feedSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        binding.feedSwipeRefreshLayout.setRefreshing(false);
                        requestFeedUpdate();
                    }
                }
        );

        requestFeedUpdate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // From interface FeedRecyclerViewAdapter.OnClickListener
    @Override
    public void onClick(int position) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(feedViewModel.getFeed().get(position).getLink()));
        startActivity(browserIntent);
    }
}