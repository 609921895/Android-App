package com.csci150.newsapp.entirenews;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.NestedScrollView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.csci150.newsapp.entirenews.utils.ElasticDragDismissFrameLayout;
import com.csci150.newsapp.entirenews.utils.FourThreeImageView;
import com.csci150.newsapp.entirenews.utils.NotifyingScrollView;
import com.csci150.newsapp.entirenews.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class ScrollingActivity extends Activity implements
        NotifyingScrollView.OnScrollChangedListener,
        ViewTreeObserver.OnGlobalLayoutListener {
    private static final String TAG = "ScrollingActivity";

    public final static String RESULT_EXTRA_NEWS_ID = "RESULT_EXTRA_NEWS_ID";
    public static final String EXTRA_NEWS_ITEM = "EXTRA_NEWS_ITEM";
    private static final float mVerticalParallaxSpeed = 0.3f;
    int fabOffset;
    private int mCoverImageHeight;
    private FourThreeImageView ivCover;
    private NewsItem newsItem;
    private ConstraintLayout mLayout;
    private ImageButton ibBack;
    //private FabToggle fab2;
    private ElasticDragDismissFrameLayout draggableFrame;
    private ElasticDragDismissFrameLayout.SystemChromeFader chromeFader;
    private TextView tvTitle, tvArticle, tvViews, tvDate, tvSource;
    private Map<String, String> mSources = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.print(TAG, "onCreate");
        setContentView(R.layout.my_demo2);
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        //FloatingActionButton fab = findViewById(R.id.fab);
        //fab.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View view) {
        //        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
        //                .setAction("Action", null).show();
        //    }
        //});
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //fab2 = findViewById(R.id.fab2);
        ivCover = findViewById(R.id.iv_cover);
        tvTitle = findViewById(R.id.tv_title);
        tvArticle = findViewById(R.id.tv_article);
        tvViews = findViewById(R.id.tv_views);
        tvDate = findViewById(R.id.tv_date);
        tvSource = findViewById(R.id.tv_source);
        draggableFrame = findViewById(R.id.draggable_frame);
        draggableFrame = findViewById(R.id.draggable_frame);
        mLayout = findViewById(R.id.constraint_layout);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResultAndFinish();
            }
        });
        chromeFader = new ElasticDragDismissFrameLayout.SystemChromeFader(this) {
            @Override
            public void onDragDismissed() {
                setResultAndFinish();
            }
        };

        ((NotifyingScrollView) findViewById(R.id.scroll_view)).setOnScrollChangedListener(this);
        ivCover.getViewTreeObserver().addOnGlobalLayoutListener(this);

        final Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_NEWS_ITEM)) {
            newsItem = intent.getParcelableExtra(EXTRA_NEWS_ITEM);
            bindNews(true);
        }
    }

    void bindNews(final boolean postponeEnterTransition) {
        //final Resources res = getResources();
        createMap();

        Glide.with(this)
                .load(newsItem.getCover())
                .apply(new RequestOptions().centerCrop().error(R.drawable.sample))
                .into(ivCover);

        if (postponeEnterTransition) postponeEnterTransition();
        ivCover.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        ivCover.getViewTreeObserver().removeOnPreDrawListener(this);
                        calculateFabPosition();
                        if (postponeEnterTransition) startPostponedEnterTransition();
                        return true;
                    }
                });

        tvTitle.setText(newsItem.getTitle());
        tvArticle.setText(newsItem.getArticle());
        if (newsItem.getViews() > 0) {
            tvViews.setVisibility(View.VISIBLE);
            String views = getResources()
                    .getQuantityString(R.plurals.views, newsItem.getViews(), newsItem.getViews());
            tvViews.setText(views);
        } else {
            tvViews.setVisibility(View.GONE);
        }
        String date = Utils.getDateAgo(getApplicationContext(), newsItem.getCreatedAt());
        tvDate.setText(date);
        String source = mSources.get(newsItem.getSource());
        if (source != null)
            tvSource.setText(source);
        else
            tvSource.setText(R.string.news);
    }

    void calculateFabPosition() {
        // calculate 'natural' position i.e. with full height image. Store it for use when scrolling
        //fabOffset = ivCover.getHeight() - (fab2.getHeight() / 2);
        //fab2.setOffset(fabOffset);
        // calculate min position i.e. pinned to the collapsed image when scrolled
        //fab2.setMinOffset(ivCover.getMinimumHeight() - (fab2.getHeight() / 2));
    }

    void setResultAndFinish() {
        //content.setBackgroundColor(getResources().getColor(R.color.transparent));
        final Intent resultData = new Intent();
        resultData.putExtra(RESULT_EXTRA_NEWS_ID, "gfhjfgjftghjdtuhrtedy"); //newsItem.id);
        setResult(RESULT_OK, resultData);
        finishAfterTransition();
    }

    private void createMap() {
        String[] mTabsChoices = getResources().getStringArray(R.array.list_sources_names);
        for (String str : mTabsChoices) {
            mSources.put(Utils.createSlug(str), "- by " + str);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.print(TAG, "onResume");
        draggableFrame.addListener(chromeFader);
    }

    @Override
    protected void onPause() {
        draggableFrame.removeListener(chromeFader);
        super.onPause();
        Utils.print(TAG, "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.print(TAG, "onDestroy");
    }

    @Override
    public void onBackPressed() {
        setResultAndFinish();
        Utils.print(TAG, "onBackPressed");
    }

    @Override
    public boolean onNavigateUp() {
        setResultAndFinish();
        Utils.print(TAG, "onNavigateUp");
        return true;
    }

    @Override
    public void onScrollChanged(NestedScrollView who, int l, int t, int oldL, int oldT) {
        /*
        if (l < mCoverImageHeight) {
            final float ratio = (float) Math.min(Math.max(t, 0), mCoverImageHeight) / mCoverImageHeight;
            ivCover.setTranslationY(ratio * mCoverImageHeight * mVerticalParallaxSpeed);
        }
        */
        //final int scrollY = mLayout.getTop();
        //fab2.setOffset(fabOffset + scrollY);
    }

    @Override
    public void onGlobalLayout() {
        /*
        mCoverImageHeight = ivCover.getHeight();
        ViewTreeObserver obs = ivCover.getViewTreeObserver();
        obs.removeOnGlobalLayoutListener(this);
        */
    }
}