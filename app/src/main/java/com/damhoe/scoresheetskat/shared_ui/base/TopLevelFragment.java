package com.damhoe.scoresheetskat.shared_ui.base;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.MenuProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.damhoe.scoresheetskat.R;
import com.damhoe.scoresheetskat.databinding.FragmentTopLevelBinding;
import com.damhoe.scoresheetskat.shared_ui.behaviors.ScrollViewBehaviorHandler;
import com.damhoe.scoresheetskat.shared_ui.utils.InsetsManager;

public abstract class TopLevelFragment extends Fragment {
   protected FragmentTopLevelBinding baseBinding;
   protected View contentView;

   @Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater,
                            @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
      baseBinding = DataBindingUtil.inflate(inflater,
              R.layout.fragment_top_level, container, false);

      // Create the content view and add it to the scroll view
      contentView = onCreateContentView(inflater, container);
      baseBinding.nestedScrollView.addView(contentView);

      // Update add button
      //baseBinding.addButton.setText(addButtonText());

      baseBinding.addButton.setVisibility(View.GONE);

      return baseBinding.getRoot();
   }

   /*
    * Override this method instead of onCreateView in derived fragments
    */
   protected abstract View onCreateContentView(
           @NonNull LayoutInflater inflater, @Nullable ViewGroup container);

   protected Drawable addButtonDrawable() {
      return ResourcesCompat.getDrawable(getResources(), R.drawable.ic_add_black_24dp, null);
   }

   protected abstract String title();

   @Override
   public void onViewCreated(@NonNull View view,
                             @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);

      // Set insets to account for status bar
      InsetsManager.applyStatusBarInsets(baseBinding.appbarLayout);

      baseBinding.toolbar.setTitle(title());
      baseBinding.addButton.setImageDrawable(addButtonDrawable());

      // Setup add button scrolling behavior
      // Extend / shrink on scroll up / down
      ScrollViewBehaviorHandler.setupWithFAB(
              baseBinding.nestedScrollView, baseBinding.addButton);

      // Setup navigation
      NavController navController = findNavController();
      NavigationUI.setupWithNavController(baseBinding.bottomNavView, navController);

      // Add menu
      addMenu();
   }

   private NavController findNavController() {
      return Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
   }

   protected abstract boolean onMenuItemSelected(@NonNull MenuItem menuItem);

   private void addMenu() {
      baseBinding.toolbar.addMenuProvider(new MenuProvider() {
         @Override
         public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
            menuInflater.inflate(R.menu.options_menu, menu);
         }

         @Override
         public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
            return TopLevelFragment.this.onMenuItemSelected(menuItem);
         }
      }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
   }

   @Override
   public void onResume() {
      super.onResume();
      baseBinding.addButton.show();
   }

   @Override
   public void onPause() {
      super.onPause();
      baseBinding.addButton.hide();
   }
}
