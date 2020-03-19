package ml.docilealligator.infinityforreddit.Activity;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Adapter.CustomizeThemeRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomTheme;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeSettingsItem;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeViewModel;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class CustomizeThemeActivity extends BaseActivity {

    public static final String EXTRA_THEME_TYPE = "ETT";
    public static final int EXTRA_LIGHT_THEME = 0;
    public static final int EXTRA_DARK_THEME = 1;
    public static final int EXTRA_AMOLED_THEME = 2;

    @BindView(R.id.appbar_layout_customize_theme_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_customize_theme_activity)
    Toolbar toolbar;
    @BindView(R.id.recycler_view_customize_theme_activity)
    RecyclerView recyclerView;
    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;
    @Inject
    RedditDataRoomDatabase redditDataRoomDatabase;
    @Inject
    CustomThemeWrapper customThemeWrapper;

    public CustomThemeViewModel customThemeViewModel;
    private CustomizeThemeRecyclerViewAdapter adapter;
    private int themeType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_theme);

        ButterKnife.bind(this);

        applyCustomTheme();

        themeType = getIntent().getIntExtra(EXTRA_THEME_TYPE, EXTRA_LIGHT_THEME);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new CustomizeThemeRecyclerViewAdapter();

        customThemeViewModel = new ViewModelProvider(this, new CustomThemeViewModel.Factory(redditDataRoomDatabase))
                .get(CustomThemeViewModel.class);

        LiveData<CustomTheme> customThemeLiveData;
        switch (themeType) {
            case EXTRA_DARK_THEME:
                toolbar.setTitle(getString(R.string.customize_dark_theme_fragment_title));
                customThemeLiveData = customThemeViewModel.getDarkCustomTheme();
                break;
            case EXTRA_AMOLED_THEME:
                toolbar.setTitle(getString(R.string.customize_amoled_theme_fragment_title));
                customThemeLiveData = customThemeViewModel.getAmoledCustomTheme();
                break;
            default:
                toolbar.setTitle(getString(R.string.customize_light_theme_fragment_title));
                customThemeLiveData = customThemeViewModel.getLightCustomTheme();
                break;
        }

        int androidVersion = Build.VERSION.SDK_INT;
        customThemeLiveData.observe(this, new Observer<CustomTheme>() {
            @Override
            public void onChanged(CustomTheme customTheme) {
                ArrayList<CustomThemeSettingsItem> customThemeSettingsItems =
                        CustomThemeSettingsItem.convertCustomTheme(CustomizeThemeActivity.this, customTheme);
                if (androidVersion < Build.VERSION_CODES.O) {
                    customThemeSettingsItems.get(customThemeSettingsItems.size() - 2).itemDetails = getString(R.string.theme_item_available_on_android_8);
                }
                if (androidVersion < Build.VERSION_CODES.M) {
                    customThemeSettingsItems.get(customThemeSettingsItems.size() - 3).itemDetails = getString(R.string.theme_item_available_on_android_6);
                }

                adapter.setCustomThemeSettingsItem(customThemeSettingsItems);
            }
        });
    }

    @Override
    protected SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return customThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        applyAppBarLayoutAndToolbarTheme(appBarLayout, toolbar);
    }
}
