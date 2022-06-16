package com.cankocak.rpi_tracker;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.cankocak.rpi_tracker.databinding.FragmentSettingsBinding;

import java.util.HashSet;
import java.util.Set;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;

    private FeedViewModel feedViewModel;
    private boolean textChangedDueToSaveEvent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        feedViewModel = new ViewModelProvider(requireActivity()).get(FeedViewModel.class);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textChangedDueToSaveEvent = false;
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!textChangedDueToSaveEvent) {
                    binding.buttonSave.setEnabled(true);
                }
            }
        };
        binding.editTextUrl.addTextChangedListener(textWatcher);
        binding.editTextKeywords.addTextChangedListener(textWatcher);
        binding.editTextUpdateInterval.addTextChangedListener(textWatcher);
        updateTexts();

        binding.buttonSave.setEnabled(false);
        binding.buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Set<String> keywords = new HashSet<String>();
                for (String keyword : binding.editTextKeywords.getText().toString().split(":")) {
                    keywords.add(keyword);
                }
                feedViewModel.setPreferences(binding.editTextUrl.getText().toString(),
                                             keywords,
                                             Integer.parseInt(binding.editTextUpdateInterval.getText().toString()));
                textChangedDueToSaveEvent = true;
                updateTexts();
                textChangedDueToSaveEvent = false;
                binding.buttonSave.setEnabled(false);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setFABVisibility(View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity) getActivity()).setFABVisibility(View.VISIBLE);
    }

    private void updateTexts() {
        binding.editTextUrl.setText(feedViewModel.getUrlStr());

        StringBuilder keywordsStr = new StringBuilder();
        Set<String> filterKeywords = feedViewModel.getFilterKeywords();
        if (filterKeywords.size() > 0) {
            for (String keyword : filterKeywords) {
                keywordsStr.append(keyword + ":");

            }
            keywordsStr.deleteCharAt(keywordsStr.length() - 1);
        }
        binding.editTextKeywords.setText(new String(keywordsStr));

        binding.editTextUpdateInterval.setText(Integer.toString(feedViewModel.getUpdateInterval()));
    }
}
