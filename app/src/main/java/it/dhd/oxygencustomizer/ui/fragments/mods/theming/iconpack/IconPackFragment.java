package it.dhd.oxygencustomizer.ui.fragments.mods.theming.iconpack;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayoutMediator;

import it.dhd.oxygencustomizer.OxygenCustomizer;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentIconPackBinding;
import it.dhd.oxygencustomizer.ui.base.BaseFragment;
import it.dhd.oxygencustomizer.xposed.utils.IconPackUtil;


public class IconPackFragment extends BaseFragment implements IconPackUtil.IconPackQueryListener {

    public static final String PX_ICON_PACK_REPO = "https://github.com/DHD2280/OCIconPackTemplate";
    private IconPackUtil mIconPackUtil;
    private FragmentIconPackBinding binding;

    // Search
    public String mSearchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentIconPackBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MenuHost menuHost = requireActivity();
        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                // Add menu items here
                menu.add(0, 1, 0, "Search")
                        .setIcon(R.drawable.ic_search)
                        .setIconTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.textColorPrimary)))
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                menu.add(0, 2, 0, "Info")
                        .setIcon(R.drawable.settingslib_ic_info_outline_24)
                        .setIconTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.textColorPrimary)))
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                // Handle the menu selection
                if (menuItem.getItemId() == 2) {
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle(requireContext().getString(R.string.icon_pack_disclaimer_title))
                            .setMessage(getClickableText(requireActivity(), requireContext().getString(R.string.icon_pack_disclaimer_desc, PX_ICON_PACK_REPO), PX_ICON_PACK_REPO))
                            .setPositiveButton(R.string.depth_effect_ok_btn, (dialog, which) -> dialog.dismiss())
                            .show();
                    return true;
                } else if (menuItem.getItemId() == 1) {
                    if (binding.search.getVisibility() == View.VISIBLE) {
                        binding.searchpref.search.setText("");
                        binding.search.setVisibility(View.GONE);
                    } else {
                        binding.search.setVisibility(View.VISIBLE);
                        binding.searchpref.search.requestFocus();
                    }
                }
                return true;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        IconPackCollectionAdapter fragmentCollectionAdapter = new IconPackCollectionAdapter(this);
        binding.pager.setAdapter(fragmentCollectionAdapter);
        String[] mTabTitles = {getString(R.string.icon_pack_selection_title), getString(R.string.icon_pack_customization_title)};
        new TabLayoutMediator(binding.tabLayout, binding.pager,
                (tab, position) -> tab.setText(mTabTitles[position])
        ).attach();
        binding.pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.resetButton.setVisibility(isFabVisible() ? View.VISIBLE : View.GONE);
                submitQuery();
            }
        });
        binding.resetButton.setOnClickListener(v -> resetIconPacks());

        ViewGroup tabs = (ViewGroup) binding.tabLayout.getChildAt(0);
        int tabCount = tabs.getChildCount();
        Log.i("IconPackFragment", "tabCount: " + tabCount);
        for (int i = 0; i < tabCount; i++) {
            View tab = tabs.getChildAt(i);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) tab.getLayoutParams();
            if (i != 0) layoutParams.setMarginStart(dpToPx(6));
            if (i != tabCount - 1) layoutParams.setMarginEnd(dpToPx(6));
            tab.setLayoutParams(layoutParams);
            tab.requestLayout();
        }
        binding.tabLayout.requestLayout();

        binding.searchpref.search.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mSearchQuery = charSequence.toString();
                submitQuery();
            }
        });

        mIconPackUtil = IconPackUtil.getInstance(requireContext());
        mIconPackUtil.addListener(this);
    }

    /**
     * @noinspection SameParameterValue
     */
    @NonNull
    public static SpannableString getClickableText(Activity activity, String message, String link) {
        SpannableString spannableMessage = new SpannableString(message);

        int start = message.indexOf(link);
        int end = start + link.length();

        spannableMessage.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                    activity.startActivity(intent);
                } catch (Exception exception) {
                    Log.e("IconPackRepo", "Browser not found");
                }
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        Linkify.addLinks(spannableMessage, Linkify.WEB_URLS);

        return spannableMessage;
    }

    public static int dpToPx(int dp) {
        return dpToPx((float) dp);
    }

    public static int dpToPx(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, OxygenCustomizer.get().getResources().getDisplayMetrics());
    }

    @Override
    public void onIconPacksLoaded(IconPackUtil.ResourceMapping mapping, IconPackUtil.IconPackMapping packMapping) {
        Log.d("IconPackFragment", "onIconPacksLoaded: mapping=" + mapping + ", packMapping=" + packMapping);
        new Handler(Looper.getMainLooper()).post(() -> {
            binding.loadingIndicator.setVisibility(View.GONE);

            boolean iconPackAvailable = packMapping != null && !packMapping.isEmpty();
            binding.tabLayout.setVisibility(iconPackAvailable ? View.VISIBLE : View.GONE);
            binding.pager.setVisibility(iconPackAvailable ? View.VISIBLE : View.GONE);
            binding.noPacksLayout.setVisibility(iconPackAvailable ? View.GONE : View.VISIBLE);
        });
    }

    public void submitQuery() {
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof IconPackListFragment iconPackListFragment) {
            iconPackListFragment.query(mSearchQuery);
        } else if (currentFragment instanceof IconPackCustomizationFragment iconPackCustomizationFragment) {
            iconPackCustomizationFragment.query(mSearchQuery);
        }
    }

    private boolean isFabVisible() {
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof IconPackListFragment iconPackListFragment) {
            return iconPackListFragment.isFabVisible();
        } else if (currentFragment instanceof IconPackCustomizationFragment iconPackCustomizationFragment) {
            return iconPackCustomizationFragment.isFabVisible();
        }
        return false;
    }

    public void requestFabVisibility() {
        binding.resetButton.setVisibility(isFabVisible() ? View.VISIBLE : View.GONE);
    }

    private Fragment getCurrentFragment() {
        int currentItem = binding.pager.getCurrentItem();
        String fragmentTag = "f" + currentItem;
        return getChildFragmentManager().findFragmentByTag(fragmentTag);
    }

    private void resetIconPacks() {
        IconPackUtil iconPackUtil = IconPackUtil.getInstance(requireContext());
        for (IconPackUtil.IconPack iconPack : iconPackUtil.mIconPackMapping.getIconPacks()) {
            iconPackUtil.disable(iconPack);
        }
        iconPackUtil.queryIconPacks(false);
    }

    @Override
    public String getTitle() {
        return getString(R.string.icon_packs_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    private static class IconPackCollectionAdapter extends FragmentStateAdapter {

        public IconPackCollectionAdapter(Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment fragment;
            if (position == 0) {
                fragment = new IconPackListFragment();
            } else {
                fragment = new IconPackCustomizationFragment();
            }
            return fragment;
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
