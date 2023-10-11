package com.damhoe.scoresheetskat.shared_ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.damhoe.scoresheetskat.R;
import com.damhoe.scoresheetskat.databinding.FragmentScrollableBaseBinding;

public abstract class ScrollableFragment extends BaseFragment {

   protected View contentLayout;

   @Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

      FragmentScrollableBaseBinding baseBinding = DataBindingUtil.inflate(inflater,
              R.layout.fragment_scrollable_base, container, false);

      baseBinding.container.addView(contentLayout);
      return baseBinding.getRoot();
   }
}
