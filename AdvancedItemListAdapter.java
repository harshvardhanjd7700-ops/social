package finix.social.finixapp.adapter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import androidx.media3.datasource.DataSpec;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.cache.SimpleCache;
import androidx.media3.datasource.cache.CacheDataSource;
import jp.wasabeef.glide.transformations.BlurTransformation;
import finix.social.finixapp.util.MentionHelper;
import finix.social.finixapp.util.UsernameLinkifier;


import android.view.ViewParent;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.SeekParameters;
import androidx.media3.common.C;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.DefaultLoadControl;
import android.graphics.Rect;import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;


import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;

import  finix.social.finixapp.libs.circularImageView.*;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import github.ankushsachdeva.emojicon.EmojiconEditText;
import github.ankushsachdeva.emojicon.EmojiconTextView;
import finix.social.finixapp.AppActivity;
import finix.social.finixapp.GroupActivity;
import finix.social.finixapp.HashtagsActivity;
import finix.social.finixapp.LoginActivity;
import finix.social.finixapp.MainActivity;
import finix.social.finixapp.MediaViewerActivity;
import finix.social.finixapp.ProfileActivity;
import finix.social.finixapp.R;
import finix.social.finixapp.ReactionsActivity;
import finix.social.finixapp.RegisterActivity;

import finix.social.finixapp.VideoViewActivity;
import finix.social.finixapp.ViewItemActivity;
import finix.social.finixapp.ViewYouTubeVideoActivity;
import finix.social.finixapp.app.App;
import finix.social.finixapp.constants.Constants;
import finix.social.finixapp.model.Comment;
import finix.social.finixapp.model.Item;
import finix.social.finixapp.model.MediaItem;
import finix.social.finixapp.util.Api;
import finix.social.finixapp.util.CustomRequest;
import finix.social.finixapp.util.TagClick;
import finix.social.finixapp.util.TagSelectingTextview;
import finix.social.finixapp.view.ResizableImageView;


import androidx.media3.datasource.DataSource;

import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import com.bumptech.glide.request.RequestListener;

import com.bumptech.glide.request.target.Target;

import com.bumptech.glide.load.engine.GlideException;

import com.bumptech.glide.Glide;

import com.bumptech.glide.request.RequestOptions;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.PlayerView;
import androidx.media3.common.Player;

@UnstableApi
public class AdvancedItemListAdapter extends RecyclerView.Adapter<AdvancedItemListAdapter.ViewHolder> implements Constants, TagClick {
    private static final String PAYLOAD_CAPTION_TOGGLE = "PAYLOAD_CAPTION_TOGGLE";

    // Helper: attach ExoPlayer to a StyledPlayerView only when attached to window
    private void attachPlayerSafely(final PlayerView view,
                                    final Player player) {
        if (view == null) return;
        if (view.isAttachedToWindow()) {
            try { view.setPlayer(player); } catch (Throwable ignored) {}
        } else {
            view.addOnAttachStateChangeListener(new android.view.View.OnAttachStateChangeListener() {
                @Override public void onViewAttachedToWindow(android.view.View v) {
                    try { view.removeOnAttachStateChangeListener(this); } catch (Throwable ignored) {}
                    try { view.setPlayer(player); } catch (Throwable ignored) {}
                }
                @Override public void onViewDetachedFromWindow(android.view.View v) { /* no-op */ }
            });
        }
    }

    // === ReadMore (from MediaItemFragment reference): state ===
    private static final int CAPTION_MAX_CHARS_COLLAPSED = 180; // collapsed length by chars
    private final android.util.SparseBooleanArray captionExpandedByPos = new android.util.SparseBooleanArray();
    // === end ReadMore state ===


    // === Prefetch: fixed policy (2 ahead, 512KB, skip if cached) ===
    private void prefetchNextItems(int position) {
        try {
            java.util.List<?> listRef = this.items; // expects field 'items'
            if (listRef == null || listRef.isEmpty()) return;
            int size = listRef.size();
            int start = Math.max(0, position + 1);
            int end = Math.min(size, start + 2); // prefetch next 2
            for (int i = start; i < end; i++) {
                Object obj = listRef.get(i);
                try {
                    java.lang.reflect.Method m = obj.getClass().getMethod("getVideoUrl");
                    Object urlObj = m.invoke(obj);
                    String url = urlObj != null ? urlObj.toString() : null;
                    prefetchVideo(this.simpleCache, url); // expects field 'simpleCache'
                } catch (Throwable ignored) { /* item has no video */ }
            }
        } catch (Throwable ignoredOuter) {}
    }

    // Checks if the first 'upToBytes' of the URL are already cached.
    private boolean isCachedUpTo(SimpleCache cache, String url, long upToBytes) {
        try {
            if (cache == null || url == null || url.trim().isEmpty()) return false;
            String key = Uri.parse(url).toString(); // default cache key
            long cached = cache.getCachedBytes(key, 0, upToBytes);
            return cached >= upToBytes;
        } catch (Throwable ignore) {
            return false;
        }
    }

    // Prefetch up to 512 KB via CacheDataSource (no CacheUtil)
    private void prefetchVideo(SimpleCache cache, String url) {
        final long prefetchTargetBytes = 512L * 1024L; // 512 KB
        if (cache == null || url == null) return;
        final String finalUrl = url.trim();
        if (finalUrl.isEmpty()) return;
        if (isCachedUpTo(cache, finalUrl, prefetchTargetBytes)) return;

        new Thread(() -> {
            DataSource.Factory http = new DefaultHttpDataSource.Factory();
            CacheDataSource cds = new CacheDataSource(cache, http.createDataSource());
            DataSpec spec = new DataSpec(Uri.parse(finalUrl));
            byte[] buffer = new byte[256 * 1024]; // 256 KB
            long total = 0L;
            try {
                cds.open(spec);
                int n;
                while (total < prefetchTargetBytes && (n = cds.read(buffer, 0, buffer.length)) > 0) {
                    total += n;
                    if (isCachedUpTo(cache, finalUrl, prefetchTargetBytes)) break;
                }
            } catch (Throwable ignore) {
            } finally {
                try { cds.close(); } catch (Throwable ignored) {}
            }
        }, "prefetch-video").start();
    }



    // === User-toggleable prefetch modes ===// 2 items, ~512KB each// 3 items, ~4MB each/** Call this from Activity/Fragment UI toggle to set user preference. *//** Optionally expose current mode */// === Prefetch without CacheUtil: manual CacheDataSource read loop ===

    // Checks if the first 'upToBytes' of the URL are already cached.






    /**
     * Prefetch next N items starting from (position+1). Requires a field 'items' and 'simpleCache'.
     */



    // Warm next few items' data (images via ImageLoader; videos via CacheUtil first-bytes)
    private void maybePrefetchAhead(int basePosition, int lookAhead) {
        if (!ENABLE_PREFETCH) return;
        if (items == null) return;
        int total = items.size();
        if (basePosition < 0 || basePosition >= total) return;

        for (int i = 1; i <= lookAhead; i++) {
            int idx = basePosition + i;
            if (idx >= total) break;
            try {
                Item it = items.get(idx);
                if (it == null) continue;

                // Image prefetch via existing Volley ImageLoader (no view binding)
                try {
                    if (imageLoader != null && it.getImgUrl() != null && it.getImgUrl().length() != 0) {
                        imageLoader.get(it.getImgUrl(), new ImageLoader.ImageListener() {
                            @Override public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) { /* warmed */ }
                            @Override public void onErrorResponse(com.android.volley.VolleyError error) { /* ignore */ }
                        });
                    }
                } catch (Throwable ignored) {}

                // Video prefetch: first ~1MB into SimpleCache (if available)
                try {
                    String v = it.getVideoUrl();
                    if (v != null && v.length() != 0) {
                        prefetchVideoFirstBytes(v, 1_000_000L);
                    }
                } catch (Throwable ignored) {}

            } catch (Throwable ignored) {}
        }
    }

    private void prefetchVideoFirstBytes(final String url, final long bytesToPrefetch) {
        if (!ENABLE_PREFETCH) return;
        if (url == null || url.length() == 0) return;

        // We need SimpleCache to benefit; if not present, skip quietly
        if (simpleCache == null) return;

        // Avoid duplicate jobs
        synchronized (prefetchVideoJobs) {
            if (!prefetchVideoJobs.add(url)) return;
        }
        try {
            if (prefetchIo == null) prefetchIo = Executors.newSingleThreadExecutor();
        } catch (Throwable ignored) {}

        prefetchIo.execute(new Runnable() {
            @Override public void run() {
                try {
                    // Use existing httpFactory if available; else create a local one
                    DefaultHttpDataSource.Factory http;
                    try {
                        // reflection check to see if field httpFactory exists and is non-null
                        java.lang.reflect.Field f = AdvancedItemListAdapter.this.getClass().getDeclaredField("httpFactory");
                        f.setAccessible(true);
                        Object val = f.get(AdvancedItemListAdapter.this);
                        if (val instanceof DefaultHttpDataSource.Factory && val != null) {
                            http = (DefaultHttpDataSource.Factory) val;
                        } else {
                            http = new DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true);
                        }
                    } catch (Throwable t) {
                        http = new DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true);
                    }

                    Uri uri = Uri.parse(url);
                    DataSpec spec = new DataSpec(uri, 0, bytesToPrefetch, null);
                    CacheDataSource cds = new CacheDataSource(simpleCache, http.createDataSource());
                    byte[] buffer = new byte[64 * 1024];
                    // CacheUtil call removed; using manual prefetch
                } catch (Throwable ignored) {
                } finally {
                    synchronized (prefetchVideoJobs) { prefetchVideoJobs.remove(url); }
                }
            }
        });
    }

    // === Prefetch (adapter-only, background; non-invasive) ===
    private final boolean ENABLE_PREFETCH = true;
    private ExecutorService prefetchIo;
    private final Set<String> prefetchVideoJobs = new HashSet<>();

    // default to true to keep previous behaviour (autoplay starts muted)
    private boolean autoplayMuted = true;

    // === Shared ExoPlayer + state (merged) ===
    private androidx.media3.exoplayer.ExoPlayer sharedPlayer = null;
    private DefaultTrackSelector sharedTrackSelector = null;
    private ViewHolder sharedHolder = null;
    private int sharedPosition = RecyclerView.NO_POSITION;

    // --- ExoPlayer Auto-Play/Stop Logic ---

    // These helpers allow your fragment to wire up auto-play/stop.
    private RecyclerView attachedRecyclerView = null;
    // start auto-play as soon as a small portion is visible (instant)
    private final int autoPlayVisiblePercent = 1; // percent visible required to auto-play
    // prepare early when X% visible (start prepare but don't play)
    // prepare early when tiny portion visible (prepare but don't start)
    private final int prepareVisiblePercent = 1;

    // Cache config: initialize lazily in constructor or an init method
    private androidx.media3.datasource.cache.SimpleCache simpleCache = null;
    private androidx.media3.datasource.cache.CacheDataSource.Factory cacheDataSourceFactory = null;
    private boolean autoPlayEnabled = true;

    // Attach the RecyclerView for auto-play tracking
    public void attachToRecyclerView(@NonNull RecyclerView recyclerView) {
        this.attachedRecyclerView = recyclerView;

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (!autoPlayEnabled) return;
                autoPlayVideoIfNeeded();
            }
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView rv, int newState) {
                if (!autoPlayEnabled) return;
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    autoPlayVideoIfNeeded();
                }
            }
        });

        recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override public void onChildViewAttachedToWindow(@NonNull View view) { }
            @Override public void onChildViewDetachedFromWindow(@NonNull View view) {
                RecyclerView.ViewHolder vh = recyclerView.getChildViewHolder(view);
                if (vh instanceof ViewHolder) {
                    ViewHolder holder = (ViewHolder) vh;
                    if (holder == currentPlayerViewHolder) {
                        holder.releasePlayer();
                        currentPlayer = null;
                        currentPlayerViewHolder = null;
                        currentPlayingPosition = -1;
                    }


                    // NEW: also handle the shared / autoplay player when its row detaches
                    if (holder == sharedHolder) {
                        try { if (sharedHolder.playerView != null) sharedHolder.playerView.setPlayer(null); } catch (Throwable ignore) {}
                        try { if (sharedPlayer != null) sharedPlayer.stop(); } catch (Throwable ignore) {}
                        try { if (sharedPlayer != null) sharedPlayer.release(); } catch (Throwable ignore) {}
                        sharedPlayer = null;
                        sharedHolder = null;
                        sharedPosition = RecyclerView.NO_POSITION;
                    }
                }
            }
        });

// run an initial autoplay check after layout so visible videos start immediately
        if (recyclerView != null) {
            recyclerView.post(() -> {
                try { autoPlayVideoIfNeeded(); } catch (Throwable ignored) {}
            });
        }

    }

    // Finds the most visible video and plays it
    public void autoPlayVideoIfNeeded() {
        if (attachedRecyclerView == null) return;
        RecyclerView.LayoutManager lm = attachedRecyclerView.getLayoutManager();
        if (!(lm instanceof GridLayoutManager)) return;

        GridLayoutManager glm = (GridLayoutManager) lm;
        int first = glm.findFirstVisibleItemPosition();
        int last = glm.findLastVisibleItemPosition();

        int bestPos = -1;
        int bestVisible = 0;

        for (int i = first; i <= last; i++) {
            if (i < 0 || i >= items.size()) continue;
            Item it = items.get(i);
            if (it.getVideoUrl() == null || it.getVideoUrl().isEmpty()) continue;

            RecyclerView.ViewHolder vh = attachedRecyclerView.findViewHolderForAdapterPosition(i);
            if (!(vh instanceof ViewHolder)) continue;
            ViewHolder holder = (ViewHolder) vh;

            View candidate = null;
            if (holder.playerView != null && holder.playerView.getVisibility() == View.VISIBLE) {
                candidate = holder.playerView;
            } else if (holder.mVideoLayout != null) {
                candidate = holder.mVideoLayout;
            }
            if (candidate == null) continue; // Defensive: skip if neither view exists

            int visible = getVisibleHeightPercent(candidate, attachedRecyclerView);

            if (visible > bestVisible) {
                bestVisible = visible;
                bestPos = i;
            }

// If a row is partially visible and we haven't prepared it yet, prepare it early to hide buffering
            try {
                if (visible >= prepareVisiblePercent && sharedPosition != i) {
                    RecyclerView.ViewHolder prepVh = attachedRecyclerView.findViewHolderForAdapterPosition(i);
                    if (prepVh instanceof ViewHolder) {
                        final ViewHolder prepHolder = (ViewHolder) prepVh;
                        final Item prepItem = items.get(i);
                        // Prepare the shared player with this item's media but don't start playback yet
                        if (sharedPlayer != null) {
                            androidx.media3.common.MediaItem mediaItem =
                                    androidx.media3.common.MediaItem.fromUri(android.net.Uri.parse(prepItem.getVideoUrl()));
                            sharedHolder = prepHolder;
                            sharedPosition = i;
                            // choose data source factory: prefer cacheDataSourceFactory if available, otherwise default network factory
                            androidx.media3.datasource.DataSource.Factory dataSourceFactory;
                            if (cacheDataSourceFactory != null) {
                                dataSourceFactory = cacheDataSourceFactory;
                            } else {
                                dataSourceFactory = new androidx.media3.datasource.DefaultDataSource.Factory(context);
                            }

// create a MediaSource (progressive mp4/http)
                            androidx.media3.exoplayer.source.MediaSource mediaSource =
                                    new androidx.media3.exoplayer.source.ProgressiveMediaSource.Factory(dataSourceFactory)
                                            .createMediaSource(mediaItem);

                            sharedPlayer.setMediaSource(mediaSource);
                            sharedPlayer.prepare();
                            sharedPlayer.setPlayWhenReady(false);
                        }
                    }
                }
            } catch (Throwable ignored) {}

        }

        if (bestPos != -1 && bestVisible >= autoPlayVisiblePercent) {
            if (bestPos != currentPlayingPosition) {
                RecyclerView.ViewHolder vh = attachedRecyclerView.findViewHolderForAdapterPosition(bestPos);
                if (vh instanceof ViewHolder) {
                    ViewHolder holder = (ViewHolder) vh;
                    playVideo(holder, bestPos, autoplayMuted); // auto-muted
                }
            }
        } else {
            // No sufficiently-visible video found - stop any active player(s)
            if (currentPlayerViewHolder != null) {
                currentPlayerViewHolder.releasePlayer();
                currentPlayer = null;
                currentPlayerViewHolder = null;
                currentPlayingPosition = -1;
            }
            if (sharedHolder != null) {
                try { if (sharedHolder.playerView != null) sharedHolder.playerView.setPlayer(null); } catch (Throwable ignore) {}
                try { if (sharedPlayer != null) { sharedPlayer.stop(); sharedPlayer.release(); sharedPlayer = null; } } catch (Throwable ignore) {}
                sharedHolder = null;
                sharedPosition = RecyclerView.NO_POSITION;
            }
        }
    }

    // Get the percent of the view visible in RecyclerView
    private int getVisibleHeightPercent(@NonNull View child, @NonNull RecyclerView parent) {
        if (child == null) return 0;
        Rect rect = new Rect();
        boolean isVisible = child.getGlobalVisibleRect(rect);
        if (!isVisible) return 0;
        int visibleHeight = rect.height();
        int totalHeight = child.getHeight();
        if (totalHeight <= 0) totalHeight = child.getMeasuredHeight();
        if (totalHeight <= 0) return 0;
        int percent = (int) ((visibleHeight * 100f) / totalHeight);
        if (percent < 0) percent = 0;
        if (percent > 100) percent = 100;
        return percent;
    }

    // --- ExoPlayer Setup for Playing a Video ---
    public void playVideo(ViewHolder holder, int position, boolean autoMuted) {
        // If this video is already playing, do NOT reset/restart it!
        if (sharedPosition == position && sharedPlayer != null && sharedPlayer.isPlaying()) {
            // Already playing, just ensure it's visible and controls are set.
            holder.playerView.setVisibility(android.view.View.VISIBLE);
            holder.btnMute.setVisibility(android.view.View.VISIBLE);
            holder.mVideoProgressBar.setVisibility(android.view.View.GONE);
            holder.mVideoImg.setVisibility(android.view.View.GONE);
            holder.mItemPlayVideo.setVisibility(android.view.View.GONE);
            return;
        }

        int ap = holder.getAdapterPosition();
        if (ap == RecyclerView.NO_POSITION) return;
        position = ap;

        Item p = items.get(position);
        if (p == null) return;
        String url = p.getVideoUrl();
        if (url == null || url.trim().isEmpty()) return;
        // Normalize UI state before starting playback to avoid black screens
        try {
            holder.mVideoProgressBar.setVisibility(android.view.View.VISIBLE);
            holder.playerView.setVisibility(android.view.View.INVISIBLE); // show when READY
            holder.mVideoImg.setVisibility(android.view.View.VISIBLE);    // thumbnail visible during buffering
            holder.mItemPlayVideo.setVisibility(android.view.View.GONE);
            holder.btnMute.setVisibility(android.view.View.GONE);
        } catch (Throwable ignore) {}

        if (sharedHolder != null && sharedHolder != holder) {
            try { if (sharedHolder.playerView != null) sharedHolder.playerView.setPlayer(null); } catch (Throwable ignore) {}
        }

        if (sharedPlayer == null) {
            try {
                sharedTrackSelector = new DefaultTrackSelector(context);
                TrackSelectionParameters params = new TrackSelectionParameters.Builder(context)
                        .setMaxVideoSizeSd()
                        .setMaxVideoBitrate(2_000_000)
                        .build();
                sharedTrackSelector.setParameters(params);

                DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(context).setEnableDecoderFallback(true);



                sharedPlayer = new androidx.media3.exoplayer.ExoPlayer.Builder(context, renderersFactory)
                        .setTrackSelector(sharedTrackSelector)
                        .setLoadControl(new DefaultLoadControl.Builder()
                                .setBufferDurationsMs(2_000, 15_000, 500, 500)
                                .setPrioritizeTimeOverSizeThresholds(false)
                                .build())
                        .build();

                try { sharedPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT); } catch (Throwable ignore) {}
                try { sharedPlayer.setSeekParameters(SeekParameters.CLOSEST_SYNC); } catch (Throwable ignore) {}

                sharedPlayer.addListener(new androidx.media3.common.Player.Listener() {
                    @Override
                    public void onPlaybackStateChanged(int state) {
                        if (sharedHolder != null && state == androidx.media3.common.Player.STATE_READY) {
                            try {
                                // Player is ready to play, but we KEEP the thumbnail visible here.
                                if (sharedHolder.mVideoProgressBar != null) {
                                    sharedHolder.mVideoProgressBar.setVisibility(android.view.View.GONE);
                                }
                                if (sharedHolder.playerView != null) {
                                    sharedHolder.playerView.setVisibility(android.view.View.VISIBLE);
                                    android.view.View surface = sharedHolder.playerView.getVideoSurfaceView();
                                    if (surface != null) surface.setVisibility(android.view.View.VISIBLE);
                                }
                                if (sharedHolder.mItemPlayVideo != null) {
                                    sharedHolder.mItemPlayVideo.setVisibility(android.view.View.GONE);
                                }
                                // Do NOT hide mVideoImg here. That is handled in onRenderedFirstFrame().
                            } catch (Throwable ignore) {}
                        }
                    }

                    @Override
                    public void onRenderedFirstFrame() {
                        if (sharedHolder != null) {
                            try {
                                if (sharedHolder.mVideoImg != null) {
                                    sharedHolder.mVideoImg.setVisibility(android.view.View.GONE);
                                }
                                if (sharedHolder.playerView != null) {
                                    android.view.View surface = sharedHolder.playerView.getVideoSurfaceView();
                                    if (surface != null) surface.setVisibility(android.view.View.VISIBLE);
                                }
                            } catch (Throwable ignored) {}
                        }
                    }

                    @Override
                    public void onPlayerError(androidx.media3.common.PlaybackException error) {
                        Log.e("ExoPlayer", "onPlayerError", error);
                        if (sharedHolder != null) {
                            try {
                                if (sharedHolder.mVideoProgressBar != null) {
                                    sharedHolder.mVideoProgressBar.setVisibility(android.view.View.GONE);
                                }
                                if (sharedHolder.playerView != null) {
                                    sharedHolder.playerView.setVisibility(android.view.View.GONE);
                                }
                                if (sharedHolder.mVideoImg != null) {
                                    sharedHolder.mVideoImg.setVisibility(android.view.View.VISIBLE);
                                }
                                if (sharedHolder.mItemPlayVideo != null) {
                                    sharedHolder.mItemPlayVideo.setVisibility(android.view.View.VISIBLE);
                                }
                                if (sharedHolder.btnMute != null) {
                                    sharedHolder.btnMute.setVisibility(android.view.View.GONE);
                                }
                            } catch (Throwable ignored) {}
                        }
                        try { sharedPlayer.stop(); } catch (Throwable ignored) {}
                    }
                });
            } catch (Throwable t) {
                Log.e("ExoPlayer", "Failed to build shared player", t);
                return;
            }
        }

        holder.playerView.setUseController(false);
        attachPlayerSafely(holder.playerView, sharedPlayer);

// --- Double-tap like on playing video ---
        final int adapterPosition = position; // ensure it's final for the lambda

        if (holder.playerView != null) {
            final GestureDetector playerGestureDetector = new GestureDetector(holder.playerView.getContext(),
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onDown(MotionEvent e) {
                            // Must return true so GestureDetector will track subsequent events (needed for double-tap)
                            return true;
                        }

                        @Override
                        public boolean onDoubleTap(MotionEvent e) {
                            // always animate video heart overlay
                            if (holder.mVideoHeartOverlay != null) {
                                showHeartAnimation(holder.mVideoHeartOverlay);
                            }
                            try { startHeartTrail(holder, e.getX(), e.getY(), true); } catch (Throwable ignored) {}

                            if (App.getInstance().getId() != 0 && !p.isMyLike()) {
                                p.setMyLike(true);
                                p.setLikesCount(p.getLikesCount() + 1);

                                like(p, adapterPosition, 0);

                                new Handler().postDelayed(() -> notifyItemChanged(adapterPosition, "reactions"), 350);
                            }return true;
                        }

                        @Override
                        public boolean onSingleTapConfirmed(MotionEvent e) {
                            // Only open fullscreen for app-hosted videos (not YouTube).
                            try {
                                if (p.getVideoUrl() != null && p.getVideoUrl().length() != 0) {
                                    // Stop/release per-row player if active for this adapter position
                                    if (currentPlayer != null && currentPlayingPosition == adapterPosition) {
                                        try { currentPlayer.setPlayWhenReady(false); } catch (Throwable ignored) {}
                                        try { currentPlayer.stop(); } catch (Throwable ignored) {}
                                        try { currentPlayer.release(); } catch (Throwable ignored) {}
                                        currentPlayer = null;
                                        if (currentPlayerViewHolder != null) {
                                            try { currentPlayerViewHolder.releasePlayer(); } catch (Throwable ignored) {}
                                            currentPlayerViewHolder = null;
                                        }
                                        currentPlayingPosition = -1;
                                    }
                                    // Stop/release shared autoplay player if present
                                    if (sharedPlayer != null) {
                                        try { sharedPlayer.setPlayWhenReady(false); } catch (Throwable ignored) {}
                                        try { sharedPlayer.stop(); } catch (Throwable ignored) {}
                                        try { sharedPlayer.release(); } catch (Throwable ignored) {}
                                        sharedPlayer = null;
                                    }
                                    // stop prefetch executor
                                    try { if (prefetchIo != null) { prefetchIo.shutdownNow(); prefetchIo = null; } } catch (Throwable ignored) {}

                                    if (sharedHolder != null) {
                                        try { sharedHolder.playerView.setPlayer(null); } catch (Throwable ignored) {}
                                        sharedHolder = null;
                                    }
                                    sharedPosition = RecyclerView.NO_POSITION;

                                    // Open fullscreen activity for app-hosted video
                                    watchVideo(p.getVideoUrl());
                                    return true;
                                }
                            } catch (Throwable ignored) {}
                            // For YouTube or if no app-hosted video, do nothing special here;
                            // return false so StyledPlayerView/controls can handle the tap (pause/play).
                            return false;
                        }
                    });

            // IMPORTANT: return the detector result so when the detector handles the touch it is consumed
            holder.playerView.setOnTouchListener((viewTouched, event) -> {
                // Let GestureDetector process the event, but always consume the touch so PlayerView doesn't handle it
                playerGestureDetector.onTouchEvent(event);
                return true; // CONSUME the event
            });}
// --- End double-tap block ---
        holder.playerView.setVisibility(android.view.View.VISIBLE);
        holder.btnMute.setVisibility(android.view.View.VISIBLE);
        holder.mVideoProgressBar.setVisibility(android.view.View.VISIBLE);
        holder.mVideoImg.setVisibility(android.view.View.GONE);
        holder.mItemPlayVideo.setVisibility(android.view.View.GONE);

        androidx.media3.common.MediaItem mediaItem =
                androidx.media3.common.MediaItem.fromUri(Uri.parse(url));
        try {
            try { prefetchVideoFirstBytes(url, 2_500_000L); } catch (Throwable ignored) {}
            sharedPlayer.setMediaItem(mediaItem);
            sharedPlayer.prepare();
            sharedPlayer.setPlayWhenReady(true);
        } catch (Throwable t) {
            Log.e("ExoPlayer", "prepare failed", t);
            try { sharedPlayer.stop(); } catch (Throwable ignored) {}
            holder.playerView.setVisibility(android.view.View.GONE);
            holder.mVideoImg.setVisibility(android.view.View.VISIBLE);
            holder.mItemPlayVideo.setVisibility(android.view.View.VISIBLE);
            holder.mVideoProgressBar.setVisibility(android.view.View.GONE);
            holder.btnMute.setVisibility(android.view.View.GONE);
            return;
        }

        sharedPlayer.setVolume(autoMuted ? 0f : 1f);
        holder.btnMute.setImageResource(autoMuted ? R.drawable.volume_off : R.drawable.volume_on);
        holder.btnMute.setOnClickListener(v -> {
            try {
                if (sharedPlayer.getVolume() == 0f) {
                    sharedPlayer.setVolume(1f);
                    holder.btnMute.setImageResource(R.drawable.volume_on);
                    autoplayMuted = false; // remember user unmuted
                } else {
                    sharedPlayer.setVolume(0f);
                    holder.btnMute.setImageResource(R.drawable.volume_off);
                    autoplayMuted = true; // user muted
                }
            } catch (Throwable ignore) {}
        });

        sharedHolder = holder;
        sharedPosition = position;
    }

    // Optional: helpers for fragment lifecycle
    // replace existing pauseCurrent()
    public void pauseCurrent() {
        try {
            if (currentPlayer != null) {
                currentPlayer.setPlayWhenReady(false);
            }
        } catch (Throwable ignored) {}

        try {
            if (sharedPlayer != null) {
                sharedPlayer.setPlayWhenReady(false);
            }
        } catch (Throwable ignored) {}
    }

    public void resumeCurrentIfVisible() {
        if (currentPlayer != null && currentPlayerViewHolder != null) {
            currentPlayer.setPlayWhenReady(true);
        }
    }
    public void releaseCurrent() {
        if (currentPlayer != null) {
            try {
                currentPlayer.release();
            } catch (Throwable ignored) {
                // ignore
            }
            currentPlayer = null;
        }

        if (currentPlayerViewHolder != null) {
            try {
                currentPlayerViewHolder.releasePlayer();
            } catch (Throwable ignored) {
                // ignore
            }
            currentPlayerViewHolder = null;
        }

        currentPlayingPosition = -1;
    }

    public void releaseSharedPlayer() {
        // detach player from holder
        if (sharedHolder != null) {
            try {
                if (sharedHolder.playerView != null) {
                    sharedHolder.playerView.setPlayer(null);
                }
            } catch (Throwable ignored) {
                // ignore
            }
            sharedHolder = null;
        }

        // stop & release shared player
        if (sharedPlayer != null) {
            try {
                sharedPlayer.stop();
            } catch (Throwable ignored) {
                // ignore
            }
            try {
                sharedPlayer.release();
            } catch (Throwable ignored) {
                // ignore
            }
            sharedPlayer = null;
        }

        // clear selector and position
        sharedTrackSelector = null;
        sharedPosition = RecyclerView.NO_POSITION;
    }



    private androidx.media3.exoplayer.ExoPlayer currentPlayer = null;
    private int currentPlayingPosition = -1;
    private ViewHolder currentPlayerViewHolder = null;

    private long replyToUserId = 0;

    private int pageId = 0;

    private List<Item> items = new ArrayList<>();

    private Context context;

    TagSelectingTextview mTagSelectingTextview;

    public static int hashTagHyperLinkDisabled = 0;

    public static final String HASHTAGS_COLOR = "#5BCFF2";

    ImageLoader imageLoader = App.getInstance().getImageLoader();

    private OnItemMenuButtonClickListener onItemMenuButtonClickListener;




    public interface OnItemMenuButtonClickListener {

        void onItemClick(View view, Item obj, int actionId, int position);
    }

    public void setOnMoreButtonClickListener(final OnItemMenuButtonClickListener onItemMenuButtonClickListener) {

        this.onItemMenuButtonClickListener = onItemMenuButtonClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView mHeartOverlay;

        public ImageView mVideoHeartOverlay;
        public CircularImageView mItemAuthorPhoto, mItemAuthorIcon, mItemFeelingIcon;
        public TextView mItemAuthor, mItemFeelingTitle;
        public ImageView mItemAuthorOnlineIcon, mItemPlayVideo;
        public ImageView mItemMenuButton;
        public ResizableImageView mItemImg;
        public ImageView mVideoImg;
        public RelativeLayout mVideoLayout, mImageLayout;
        public LinearLayout mImagesCounterLayout;
        public TextView mImagesCounterLabel;
        public ImageView mItemLikeImg, mItemCommentImg, mItemRepostImg;
        public TextView mItemRepostsCount;
        public EmojiconTextView mItemDescription;
        public TextView mItemTimeAgo;
        public ProgressBar mImageProgressBar, mVideoProgressBar;
        public MaterialRippleLayout mItemLikeButton, mItemCommentButton, mItemRepostButton;

        public MaterialRippleLayout mItemReactionButton0, mItemReactionButton1, mItemReactionButton2, mItemReactionButton3, mItemReactionButton4, mItemReactionButton5;
        public TextView mItemLikeButtonText;

        public LinearLayout mLocationLayout, mAccessModeLayout, mPinModeLayout;
        public TextView mLocationLabel, mAccessModeLabel;
        public ImageView mAccessModeImage;

        public LinearLayout mLinkContainer;
        public ImageView mLinkImage;
        public TextView mLinkTitle;
        public TextView mLinkDescription;

        public CardView mAdCard;
        public NativeAdView mAdView;
        public ProgressBar mAdProgressBar;
        public AdView mAdBannerView;

        public Button mSpotlightMoreBtn;
        public RecyclerView mSpotlightRecyclerView;


        public LinearLayout mCardRepostContainer;

        public ProgressBar mReImageProgressBar, mReVideoProgressBar;
        public RelativeLayout mReVideoLayout, mReImageLayout;
        public LinearLayout mReImagesCounterLayout;
        public TextView mReImagesCounterLabel;
        public ResizableImageView mReItemImg;
        public ImageView mReVideoImg;

        public CircularImageView mReAuthorPhoto, mReAuthorIcon;
        public TextView mReAuthor, mReAuthorUsername;
        public ImageView mRePlayVideo;
        public EmojiconTextView mReDescription;
        public TextView mReTimeAgo;

        public LinearLayout mReLinkContainer, mReMessageContainer, mReHeaderContainer, mReBodyContainer;
        public ImageView mReLinkImage;
        public TextView mReLinkTitle;
        public TextView mReLinkDescription;

        public LinearLayout mItemCountersContainer;
        public MaterialRippleLayout mItemCountersContainerButton;
        public ImageView mItemLikesCountImage, mItemCommentsCountImage;
        public TextView mItemLikesCountText, mItemCommentsCountText;

        public LinearLayout mFooterContainer, mReactionsContainer;

        public PlayerView playerView;
        public android.widget.ImageButton btnMute;
        public androidx.media3.exoplayer.ExoPlayer exoPlayer; // For this ViewHolder only

        // Switch mode

        private SwitchCompat mModeSwitch;
        private TextView mModePanelTitle;

        // New Item Card

        private CardView mNewItemCard;
        private TextView mNewItemTitle;
        private CircularImageView mNewItemImage;

        // OTP tooltip

        private Button mLinkNumberButton;
        private ImageButton mCloseTooltipButton;

        // Empty Card


        private ImageView mSplash;
        private TextView mTitle, mDesc;


        public ViewHolder(View v, int itemType) {
            super(v);

            if (itemType == VIEW_TYPE_DEFAULT) {
                playerView = v.findViewById(R.id.playerView);
                if (playerView != null) {
                    try {
                    } catch (Throwable ignore) {}
                }
                btnMute = v.findViewById(R.id.btnMute);

                mFooterContainer = (LinearLayout) v.findViewById(R.id.cardFooterContainer);
                mReactionsContainer = (LinearLayout) v.findViewById(R.id.cardReactionsContainer);

                mItemAuthorPhoto = (CircularImageView) v.findViewById(R.id.itemAuthorPhoto);
                mItemAuthorIcon = (CircularImageView) v.findViewById(R.id.itemAuthorIcon);

                mItemFeelingIcon = (CircularImageView) v.findViewById(R.id.itemFeelingIcon);

                mItemAuthor = (TextView) v.findViewById(R.id.itemAuthor);
                mItemAuthorOnlineIcon = (ImageView) v.findViewById(R.id.itemAuthorOnlineIcon);

                mAccessModeLayout = (LinearLayout) v.findViewById(R.id.access_mode_layout);
                mPinModeLayout = (LinearLayout) v.findViewById(R.id.pin_mode_layout);
                mLocationLayout = (LinearLayout) v.findViewById(R.id.location_layout);

                mLocationLabel = (TextView) v.findViewById(R.id.location_label);
                mAccessModeLabel = (TextView) v.findViewById(R.id.access_mode_label);
                mAccessModeImage = (ImageView) v.findViewById(R.id.access_mode_image);

                mItemFeelingTitle = (TextView) v.findViewById(R.id.itemFeelingTitle);

                mVideoLayout = (RelativeLayout) v.findViewById(R.id.video_layout);
                mImageLayout = (RelativeLayout) v.findViewById(R.id.image_layout);
                mImagesCounterLayout = (LinearLayout) v.findViewById(R.id.images_counter_layout);

                mImagesCounterLabel = (TextView) v.findViewById(R.id.images_counter_label);

                mItemImg = (ResizableImageView) v.findViewById(R.id.item_image);

                mVideoImg = (ImageView) v.findViewById(R.id.video_image);
                mItemPlayVideo = (ImageView) v.findViewById(R.id.video_play_image);
                mHeartOverlay = (ImageView) v.findViewById(R.id.heart_overlay);


                mVideoHeartOverlay = (ImageView) v.findViewById(R.id.video_heart_overlay); // NEW
                mImageProgressBar = (ProgressBar) v.findViewById(R.id.image_progress_bar);
                mVideoProgressBar = (ProgressBar) v.findViewById(R.id.video_progress_bar);

                mItemDescription = (EmojiconTextView) v.findViewById(R.id.itemDescription);

                mItemMenuButton = (ImageView) v.findViewById(R.id.itemMenuButton);
                mItemLikeImg = (ImageView) v.findViewById(R.id.itemLikeImg);
                mItemCommentImg = (ImageView) v.findViewById(R.id.itemCommentImg);
                mItemRepostImg = (ImageView) v.findViewById(R.id.itemRepostImg);
                mItemTimeAgo = (TextView) v.findViewById(R.id.itemTimeAgo);

                mItemRepostsCount = (TextView) v.findViewById(R.id.itemRepostsCount);

                mItemLikeButton = (MaterialRippleLayout) v.findViewById(R.id.itemLikeButton);
                mItemLikeButtonText = (TextView) v.findViewById(R.id.itemLikeText);
                mItemCommentButton = (MaterialRippleLayout) v.findViewById(R.id.itemCommentButton);
                mItemRepostButton = (MaterialRippleLayout) v.findViewById(R.id.itemRepostButton);

                mItemReactionButton0 = (MaterialRippleLayout) v.findViewById(R.id.itemReaction0);
                mItemReactionButton1 = (MaterialRippleLayout) v.findViewById(R.id.itemReaction1);
                mItemReactionButton2 = (MaterialRippleLayout) v.findViewById(R.id.itemReaction2);
                mItemReactionButton3 = (MaterialRippleLayout) v.findViewById(R.id.itemReaction3);
                mItemReactionButton4 = (MaterialRippleLayout) v.findViewById(R.id.itemReaction4);
                mItemReactionButton5 = (MaterialRippleLayout) v.findViewById(R.id.itemReaction5);

                mLinkContainer = (LinearLayout) v.findViewById(R.id.linkContainer);
                mLinkTitle = (TextView) v.findViewById(R.id.linkTitle);
                mLinkDescription = (TextView) v.findViewById(R.id.linkDescription);
                mLinkImage = (ImageView) v.findViewById(R.id.linkImage);

                // Repost

                mReHeaderContainer = (LinearLayout) v.findViewById(R.id.reHeaderContainer);
                mReMessageContainer = (LinearLayout) v.findViewById(R.id.reMessageContainer);
                mReBodyContainer = (LinearLayout) v.findViewById(R.id.reBodyContainer);
                mCardRepostContainer = (LinearLayout) v.findViewById(R.id.cardRepostContainer);

                mReAuthorPhoto = (CircularImageView) v.findViewById(R.id.reAuthorPhoto);
                mReAuthorIcon = (CircularImageView) v.findViewById(R.id.reAuthorIcon);

                mReAuthor = (TextView) v.findViewById(R.id.reAuthor);
                mReAuthorUsername = (TextView) v.findViewById(R.id.reAuthorUsername);

                mReImageProgressBar = (ProgressBar) v.findViewById(R.id.repost_image_progress_bar);
                mReItemImg = (ResizableImageView) v.findViewById(R.id.repost_item_image);
                mReImageLayout = (RelativeLayout) v.findViewById(R.id.repost_image_layout);

                mReImagesCounterLayout = (LinearLayout) v.findViewById(R.id.repost_images_counter_layout);
                mReImagesCounterLabel = (TextView) v.findViewById(R.id.repost_images_counter_label);

                mReVideoProgressBar = (ProgressBar) v.findViewById(R.id.repost_video_progress_bar);
                mReVideoLayout = (RelativeLayout) v.findViewById(R.id.repost_video_layout);
                mReVideoImg = (ImageView) v.findViewById(R.id.repost_video_image);
                mRePlayVideo = (ImageView) v.findViewById(R.id.repost_video_play_image);

                mReDescription = (EmojiconTextView) v.findViewById(R.id.reDescription);
                mReTimeAgo = (TextView) v.findViewById(R.id.reTimeAgo);

                mReLinkContainer = (LinearLayout) v.findViewById(R.id.reLinkContainer);
                mReLinkTitle = (TextView) v.findViewById(R.id.reLinkTitle);
                mReLinkDescription = (TextView) v.findViewById(R.id.reLinkDescription);
                mReLinkImage = (ImageView) v.findViewById(R.id.reLinkImage);

                // Counters

                mItemCountersContainer = (LinearLayout) v.findViewById(R.id.item_counters_container);

                mItemCountersContainerButton = (MaterialRippleLayout) v.findViewById(R.id.item_counters_container_button);

                mItemLikesCountImage = (ImageView) v.findViewById(R.id.item_likes_icon);
                mItemCommentsCountImage = (ImageView) v.findViewById(R.id.item_comments_icon);

                mItemLikesCountText = (TextView) v.findViewById(R.id.item_likes_count);
                mItemCommentsCountText = (TextView) v.findViewById(R.id.item_comments_count);

            } else if (itemType == VIEW_TYPE_AD) {

                mAdCard = (CardView) v.findViewById(R.id.adCard);
                mAdView = (NativeAdView) v.findViewById(R.id.ad_native_view);
                mAdBannerView = (AdView) v.findViewById(R.id.ad_banner_view);
                mAdProgressBar = (ProgressBar) v.findViewById(R.id.ad_progress_bar);

            } else if (itemType == VIEW_TYPE_SWITCH_MODE) {

                mModeSwitch = (SwitchCompat) v.findViewById(R.id.mode_switch);
                mModePanelTitle = (TextView) v.findViewById(R.id.mode_switch_panel_title);

            } else if (itemType == VIEW_TYPE_NEW_ITEM) {

                mNewItemCard = (CardView) v.findViewById(R.id.newItemCard);
                mNewItemTitle = (TextView) v.findViewById(R.id.newItemTitle);
                mNewItemImage = (CircularImageView) v.findViewById(R.id.newItemImage);

            } else if (itemType == VIEW_TYPE_OTP_TOOLTIP) {

                mLinkNumberButton = (Button) v.findViewById(R.id.link_number_button);
                mCloseTooltipButton = (ImageButton) v.findViewById(R.id.close_tooltip_button);

            } else if (itemType == VIEW_TYPE_EMPTY_LIST) {

                mSplash = (ImageView) v.findViewById(R.id.splash);
                mTitle = (TextView) v.findViewById(R.id.title);
                mDesc = (TextView) v.findViewById(R.id.desc);
            }
        }

        // --- Add this method below the constructor ---
        public void releasePlayer() {
            if (exoPlayer != null) {
                exoPlayer.release();
                exoPlayer = null;
            }
            if (playerView != null) {
                playerView.setPlayer(null);
                playerView.setVisibility(android.view.View.GONE);
            }
            if (btnMute != null) {
                btnMute.setVisibility(android.view.View.GONE);
            }
        }
    }


    public AdvancedItemListAdapter(Context ctx, List<Item> items) {
        // Prefetch executor setup
        try { if (prefetchIo == null) prefetchIo = Executors.newSingleThreadExecutor(); } catch (Throwable ignored) {}


        this.context = ctx;
        this.items = items;

        if (imageLoader == null) {

            imageLoader = App.getInstance().getImageLoader();
        }

        mTagSelectingTextview = new TagSelectingTextview();
// initialize simple cache + cache datasource factory (best-effort; kept small)
        try {
            java.io.File cacheFolder = new java.io.File(context.getCacheDir(), "video_cache");
            long maxCacheSize = 50L * 1024L * 1024L; // 50 MB, tune as needed
            androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor evictor =
                    new androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor(maxCacheSize);
            androidx.media3.database.ExoDatabaseProvider dbProvider =
                    new androidx.media3.database.ExoDatabaseProvider(context);
            simpleCache = new androidx.media3.datasource.cache.SimpleCache(cacheFolder, evictor, dbProvider);

            androidx.media3.datasource.DefaultHttpDataSource.Factory httpFactory =
                    new androidx.media3.datasource.DefaultHttpDataSource.Factory()
                            .setAllowCrossProtocolRedirects(true);

            cacheDataSourceFactory = new androidx.media3.datasource.cache.CacheDataSource.Factory()
                    .setCache(simpleCache)
                    .setUpstreamDataSourceFactory(httpFactory)
                    .setFlags(androidx.media3.datasource.cache.CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);

        } catch (Throwable ignore) {
            // if SimpleCache init fails, fall back to null and player will use network directly
            simpleCache = null;
            cacheDataSourceFactory = null;
        }
    }

    public AdvancedItemListAdapter(Context ctx, List<Item> items, int pageId) {

        this.context = ctx;
        this.items = items;
        this.pageId = pageId;

        if (imageLoader == null) {

            imageLoader = App.getInstance().getImageLoader();
        }

        mTagSelectingTextview = new TagSelectingTextview();
// initialize simple cache + cache datasource factory (best-effort; kept small)
        try {
            java.io.File cacheFolder = new java.io.File(context.getCacheDir(), "video_cache");
            long maxCacheSize = 50L * 1024L * 1024L; // 50 MB, tune as needed
            androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor evictor =
                    new androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor(maxCacheSize);
            androidx.media3.database.ExoDatabaseProvider dbProvider =
                    new androidx.media3.database.ExoDatabaseProvider(context);
            simpleCache = new androidx.media3.datasource.cache.SimpleCache(cacheFolder, evictor, dbProvider);

            androidx.media3.datasource.DefaultHttpDataSource.Factory httpFactory =
                    new androidx.media3.datasource.DefaultHttpDataSource.Factory()
                            .setAllowCrossProtocolRedirects(true);

            cacheDataSourceFactory = new androidx.media3.datasource.cache.CacheDataSource.Factory()
                    .setCache(simpleCache)
                    .setUpstreamDataSourceFactory(httpFactory)
                    .setFlags(androidx.media3.datasource.cache.CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);

        } catch (Throwable ignore) {
            // if SimpleCache init fails, fall back to null and player will use network directly
            simpleCache = null;
            cacheDataSourceFactory = null;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_DEFAULT) {


            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_row, parent, false);

            return new ViewHolder(v, viewType);

        } else if (viewType == VIEW_TYPE_AD) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ad_item, parent, false);

            return new ViewHolder(v, viewType);

        } else if (viewType == VIEW_TYPE_SWITCH_MODE) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mode_list_row, parent, false);

            return new ViewHolder(v, viewType);

        } else if (viewType == VIEW_TYPE_NEW_ITEM) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_list_row, parent, false);

            return new ViewHolder(v, viewType);

        } else if (viewType == VIEW_TYPE_OTP_TOOLTIP) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_otp_tooltip_list_row, parent, false);

            return new ViewHolder(v, viewType);

        } else if (viewType == VIEW_TYPE_EMPTY_LIST) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_empty_list_row, parent, false);

            return new ViewHolder(v, viewType);
        }

        return null;
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.releasePlayer();
        if (currentPlayerViewHolder == holder) {
            currentPlayer = null;
            currentPlayerViewHolder = null;
            currentPlayingPosition = -1;
        }

        // Also clear shared holder/player if recycled holder was the shared one
        if (sharedHolder == holder) {
            // Detach any video surface from the shared player before releasing it
            try {
                if (sharedPlayer != null) {
                    sharedPlayer.clearVideoSurface();
                    sharedPlayer.setVideoSurface(null);
                }
            } catch (Throwable ignore) {}

            try { if (sharedPlayer != null) sharedPlayer.stop(); } catch (Throwable ignore) {}
            try { if (sharedPlayer != null) sharedPlayer.release(); } catch (Throwable ignore) {}
            sharedPlayer = null;
            sharedHolder = null;
            sharedPosition = RecyclerView.NO_POSITION;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {


        final Item p = items.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_DEFAULT) {

            onBindItem(holder, position);

            // --- ExoPlayer Inline Video Logic ---
            holder.playerView.setVisibility(android.view.View.GONE);
            holder.btnMute.setVisibility(android.view.View.GONE);

            holder.releasePlayer();

            holder.mItemPlayVideo.setVisibility(android.view.View.VISIBLE);


            holder.mItemPlayVideo.setOnClickListener(v -> {
                int ap = holder.getAdapterPosition();
                if (ap == RecyclerView.NO_POSITION) return;
                playVideo(holder, ap, autoplayMuted);
            });

// --- End ExoPlayer Logic ---

        } else if (holder.getItemViewType() == VIEW_TYPE_EMPTY_LIST) {

            //

        } else if (holder.getItemViewType() == VIEW_TYPE_OTP_TOOLTIP) {

            holder.mLinkNumberButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    onItemMenuButtonClickListener.onItemClick(v, p,  ITEM_ACTIONS_LINK_NUMBER, position);
                }
            });

            holder.mCloseTooltipButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    onItemMenuButtonClickListener.onItemClick(v, p,  ITEM_ACTIONS_CLOSE_OTP_TOOLTIP, position);
                }
            });

        } else if (holder.getItemViewType() == VIEW_TYPE_NEW_ITEM) {

            if (App.getInstance().getPhotoUrl() != null && App.getInstance().getPhotoUrl().length() > 0) {

                App.getInstance().getImageLoader().get(App.getInstance().getPhotoUrl(), ImageLoader.getImageListener(holder.mNewItemImage, R.drawable.profile_default_photo, R.drawable.profile_default_photo));

            } else {

                holder.mNewItemImage.setImageResource(R.drawable.profile_default_photo);
            }

            //

            SpannableStringBuilder txt = new SpannableStringBuilder(String.format(context.getString(R.string.msg_new_item_promo), App.getInstance().getFullname()));
            txt.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, App.getInstance().getFullname().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            holder.mNewItemTitle.setText(txt);

            //

            holder.mNewItemCard.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    onItemMenuButtonClickListener.onItemClick(v, p,  ITEM_ACTIONS_NEW_ITEM, position);
                }
            });

        } else if (holder.getItemViewType() == VIEW_TYPE_SWITCH_MODE) {

            holder.mModeSwitch.setOnCheckedChangeListener(null);
            holder.mModeSwitch.setEnabled(true);

            if (App.getInstance().getFeedMode() == 1) {

                holder.mModeSwitch.setChecked(true);
                holder.mModePanelTitle.setText(R.string.label_feed_mode_1);

            } else {

                holder.mModeSwitch.setChecked(false);
                holder.mModePanelTitle.setText(R.string.label_feed_mode_0);
            }

            holder.mModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    buttonView.setEnabled(false);

                    if (isChecked) {

                        App.getInstance().setFeedMode(1);
                        holder.mModePanelTitle.setText(R.string.label_feed_mode_1);

                    } else {

                        App.getInstance().setFeedMode(0);
                        holder.mModePanelTitle.setText(R.string.label_feed_mode_0);
                    }

                    App.getInstance().saveData();

                    onItemMenuButtonClickListener.onItemClick(buttonView, p,  ITEM_ACTIONS_SWITCH_MODE, position);
                }
            });

        } else if (holder.getItemViewType() == VIEW_TYPE_AD) {

            holder.mAdProgressBar.setVisibility(android.view.View.GONE);

            holder.mAdBannerView.setVisibility(android.view.View.GONE);
            holder.mAdView.setVisibility(android.view.View.GONE);

            AdLoader.Builder builder = new AdLoader.Builder(context, App.getInstance().getAdmobSettings().getBannerNativeAdUnitId());

            // OnUnifiedNativeAdLoadedListener implementation.
            builder.forNativeAd(

                    (NativeAd.OnNativeAdLoadedListener) nativeAd -> {
                        // If this callback occurs after the activity is destroyed, you must call
                        // destroy and return or you may get a memory leak.

                        // You must call destroy on old ads when you are done with them,
                        // otherwise you will have a memory leak.

                        holder.mAdView.setMediaView((MediaView) holder.mAdView.findViewById(R.id.ad_media));

                        // Set other ad assets.
                        holder.mAdView.setHeadlineView(holder.mAdView.findViewById(R.id.ad_headline));
                        holder.mAdView.setBodyView(holder.mAdView.findViewById(R.id.ad_body));
                        holder.mAdView.setCallToActionView(holder.mAdView.findViewById(R.id.ad_call_to_action));
                        holder.mAdView.setIconView(holder.mAdView.findViewById(R.id.ad_app_icon));
                        holder.mAdView.setPriceView(holder.mAdView.findViewById(R.id.ad_price));
                        holder.mAdView.setStarRatingView(holder.mAdView.findViewById(R.id.ad_stars));
                        holder.mAdView.setStoreView(holder.mAdView.findViewById(R.id.ad_store));
                        holder.mAdView.setAdvertiserView(holder.mAdView.findViewById(R.id.ad_advertiser));

                        // The headline and mediaContent are guaranteed to be in every UnifiedNativeAd.
                        ((TextView) holder.mAdView.getHeadlineView()).setText(context.getString(R.string.label_admob_ad_headline) + " " + nativeAd.getHeadline());
                        holder.mAdView.getMediaView().setMediaContent(nativeAd.getMediaContent());

                        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
                        // check before trying to display them.
                        if (nativeAd.getBody() == null) {

                            holder.mAdView.getBodyView().setVisibility(View.INVISIBLE);

                        } else {

                            holder.mAdView.getBodyView().setVisibility(android.view.View.VISIBLE);
                            ((TextView) holder.mAdView.getBodyView()).setText(nativeAd.getBody());
                        }

                        if (nativeAd.getCallToAction() == null) {

                            holder.mAdView.getCallToActionView().setVisibility(View.INVISIBLE);

                        } else {

                            holder.mAdView.getCallToActionView().setVisibility(android.view.View.VISIBLE);
                            ((Button) holder.mAdView.getCallToActionView()).setText(nativeAd.getCallToAction());
                        }

                        if (nativeAd.getIcon() == null) {

                            holder.mAdView.getIconView().setVisibility(android.view.View.GONE);

                        } else {

                            ((ImageView) holder.mAdView.getIconView()).setImageDrawable(nativeAd.getIcon().getDrawable());
                            holder.mAdView.getIconView().setVisibility(android.view.View.VISIBLE);
                        }

                        if (nativeAd.getPrice() == null) {

                            holder.mAdView.getPriceView().setVisibility(View.INVISIBLE);

                        } else {

                            holder.mAdView.getPriceView().setVisibility(android.view.View.VISIBLE);
                            ((TextView) holder.mAdView.getPriceView()).setText(nativeAd.getPrice());
                        }

                        if (nativeAd.getStore() == null) {

                            holder.mAdView.getStoreView().setVisibility(View.INVISIBLE);

                        } else {

                            holder.mAdView.getStoreView().setVisibility(android.view.View.VISIBLE);
                            ((TextView) holder.mAdView.getStoreView()).setText(nativeAd.getStore());
                        }

                        if (nativeAd.getStarRating() == null) {

                            holder.mAdView.getStarRatingView().setVisibility(View.INVISIBLE);

                        } else {

                            ((RatingBar) holder.mAdView.getStarRatingView()).setRating(nativeAd.getStarRating().floatValue());
                            holder.mAdView.getStarRatingView().setVisibility(android.view.View.VISIBLE);
                        }

                        if (nativeAd.getAdvertiser() == null) {

                            holder.mAdView.getAdvertiserView().setVisibility(View.INVISIBLE);

                        } else {

                            ((TextView) holder.mAdView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
                            holder.mAdView.getAdvertiserView().setVisibility(android.view.View.VISIBLE);
                        }

                        // This method tells the Google Mobile Ads SDK that you have finished populating your
                        // native ad view with this native ad.
                        holder.mAdView.setNativeAd(nativeAd);

                        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
                        // have a video asset.
                        VideoController vc = nativeAd.getMediaContent().getVideoController();

                        // Updates the UI to say whether or not this ad has a video asset.
                        if (vc.hasVideoContent()) {

                            Log.e("admob", "Video status: Ad contains a %.2f:1 video asset.");

                            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
                            // VideoController will call methods on this object when events occur in the video
                            // lifecycle.

                            vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                                @Override
                                public void onVideoEnd() {
                                    // Publishers should allow native ads to complete video playback before
                                    // refreshing or replacing them with another ad in the same UI location.

                                    Log.e("admob", "Video status: Video playback has ended.");
                                    super.onVideoEnd();
                                }
                            });

                        } else {

                            Log.e("admob", "Video status: Ad does not contain a video asset.");
                        }
                    });

            VideoOptions videoOptions =
                    new VideoOptions.Builder().setStartMuted(true).build();

            NativeAdOptions adOptions =
                    new NativeAdOptions.Builder().setVideoOptions(videoOptions).build();

            builder.withNativeAdOptions(adOptions);

            AdLoader adLoader = builder.withAdListener(new AdListener() {

                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {

                    String error = String.format("domain: %s, code: %d, message: %s", loadAdError.getDomain(), loadAdError.getCode(), loadAdError.getMessage());
                    Log.e("admob","Failed to load native ad with error " + error);

                    holder.mAdBannerView.setVisibility(android.view.View.VISIBLE);
                    holder.mAdView.setVisibility(android.view.View.GONE);
                    holder.mAdProgressBar.setVisibility(android.view.View.GONE);

                    AdRequest adRequest = new AdRequest.Builder().build();
                    holder.mAdBannerView.loadAd(adRequest);

                }

                @Override
                public void onAdLoaded() {

                    Log.e("admob","Ad loaded");

                    holder.mAdView.setVisibility(android.view.View.VISIBLE);
                    holder.mAdProgressBar.setVisibility(android.view.View.GONE);
                }

            }).build();

            adLoader.loadAd(new AdRequest.Builder().build());

            holder.mAdCard.setVisibility(android.view.View.VISIBLE);
        }

// ensure newly-bound rows get considered for autoplay immediately
        if (attachedRecyclerView != null) {
            attachedRecyclerView.post(() -> {
                try { autoPlayVideoIfNeeded(); } catch (Throwable ignored) {}
            });
        }



        // Prefetch: warm next 2 items (images + first video bytes)
        try { maybePrefetchAhead(position, 2); } catch (Throwable ignored) {}

        try {  } catch (Throwable ignored) {}

        try { prefetchNextItems(position); } catch (Throwable ignored) {}

        // === ReadMore apply (from MediaItemFragment ref) ===
        try {
            android.widget.TextView __desc = (android.widget.TextView) holder.itemView.findViewById(finix.social.finixapp.R.id.itemDescription);
            if (__desc != null) {
                CharSequence __text = __desc.getText();
                setCollapsibleDescriptionForHolder(holder, __desc, __text, CAPTION_MAX_CHARS_COLLAPSED);
            }
        } catch (Throwable ignored) {}
    }


    public void onBindItem(ViewHolder holder, final int position) {



        final Item p = items.get(position);

        holder.mReactionsContainer.setVisibility(android.view.View.GONE);
        holder.mFooterContainer.setVisibility(android.view.View.VISIBLE);

        holder.mItemCountersContainer.setVisibility(android.view.View.GONE);

        holder.mLinkContainer.setVisibility(android.view.View.GONE);

        holder.mItemPlayVideo.setVisibility(android.view.View.GONE);
        holder.mImageProgressBar.setVisibility(android.view.View.GONE);
        holder.mVideoProgressBar.setVisibility(android.view.View.GONE);

        holder.mImageLayout.setVisibility(android.view.View.GONE);
        holder.mImagesCounterLayout.setVisibility(android.view.View.GONE);
        holder.mVideoLayout.setVisibility(android.view.View.GONE);

        holder.mAccessModeLayout.setVisibility(android.view.View.GONE);
        holder.mPinModeLayout.setVisibility(android.view.View.GONE);
        holder.mLocationLayout.setVisibility(android.view.View.GONE);

        holder.mItemAuthorPhoto.setVisibility(android.view.View.VISIBLE);

        holder.mItemAuthorPhoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (App.getInstance().getId() != 0) {

                    if (p.getGroupId() == 0) {

                        Intent intent = new Intent(context, ProfileActivity.class);
                        intent.putExtra("profileId", p.getFromUserId());
                        context.startActivity(intent);

                    } else {

                        Intent intent = new Intent(context, GroupActivity.class);
                        intent.putExtra("groupId", p.getGroupId());
                        context.startActivity(intent);
                    }

                } else {

                    showAuthorizeDlg(v, p,  ITEM_ACTIONS_MENU, position);
                }
            }
        });

        if (p.getFromUserPhotoUrl().length() != 0) {

            imageLoader.get(p.getFromUserPhotoUrl(), ImageLoader.getImageListener(holder.mItemAuthorPhoto, R.drawable.profile_default_photo, R.drawable.profile_default_photo));

        } else {

            holder.mItemAuthorPhoto.setVisibility(android.view.View.VISIBLE);
            holder.mItemAuthorPhoto.setImageResource(R.drawable.profile_default_photo);
        }

        if (p.getFromUserVerify() == 1) {

            holder.mItemAuthorIcon.setVisibility(android.view.View.VISIBLE);

        } else {

            holder.mItemAuthorIcon.setVisibility(android.view.View.GONE);
        }

        holder.mItemAuthor.setVisibility(android.view.View.VISIBLE);
        holder.mItemAuthor.setText(p.getFromUserFullname());

        holder.mItemAuthor.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (App.getInstance().getId() != 0) {

                    if (p.getGroupId() == 0) {

                        Intent intent = new Intent(context, ProfileActivity.class);
                        intent.putExtra("profileId", p.getFromUserId());
                        context.startActivity(intent);

                    } else {

                        Intent intent = new Intent(context, GroupActivity.class);
                        intent.putExtra("groupId", p.getGroupId());
                        context.startActivity(intent);
                    }

                } else {

                    showAuthorizeDlg(v, p,  ITEM_ACTIONS_MENU, position);
                }
            }
        });

        if (p.getFeeling() == 0) {

            holder.mItemFeelingIcon.setVisibility(android.view.View.GONE);
            holder.mItemFeelingTitle.setVisibility(android.view.View.GONE);

        } else {

            holder.mItemFeelingIcon.setVisibility(android.view.View.VISIBLE);
            holder.mItemFeelingTitle.setVisibility(android.view.View.VISIBLE);

            ImageLoader imageLoader = App.getInstance().getImageLoader();

            imageLoader.get(Constants.WEB_SITE + "feelings/" + Integer.toString(p.getFeeling()) + ".png", ImageLoader.getImageListener(holder.mItemFeelingIcon, R.drawable.mood, R.drawable.mood));
        }

        holder.mItemAuthorOnlineIcon.setVisibility(android.view.View.GONE);

//        if (p.getFromUserOnline() && p.getFromUserAllowShowOnline() == ENABLED) {
//
//            holder.mItemAuthorOnlineIcon.setVisibility(android.view.View.VISIBLE);
//
//        } else {
//
//            holder.mItemAuthorOnlineIcon.setVisibility(android.view.View.GONE);
//        }

        if (getLocation(p).length() > 0) {

            holder.mLocationLayout.setVisibility(android.view.View.VISIBLE);
            holder.mLocationLabel.setText(getLocation(p));
        }

        if (p.getGroupId() == 0) {

            holder.mAccessModeLayout.setVisibility(android.view.View.VISIBLE);

            if (p.getAccessMode() == 0) {

                holder.mAccessModeLabel.setText(context.getString(R.string.label_post_to_public));
                holder.mAccessModeImage.setImageResource(R.drawable.ic_unlock);

            } else {

                holder.mAccessModeLabel.setText(context.getString(R.string.label_post_to_friends));
                holder.mAccessModeImage.setImageResource(R.drawable.ic_lock);
            }
        }

        if (this.pageId == PAGE_PROFILE && p.getPinned() == 1) {

            holder.mPinModeLayout.setVisibility(android.view.View.VISIBLE);
        }

        if (p.getImgUrl().length() != 0){

            holder.mImageLayout.setVisibility(android.view.View.VISIBLE);
            holder.mItemImg.setVisibility(android.view.View.VISIBLE);
            holder.mImageProgressBar.setVisibility(android.view.View.VISIBLE);

            final ProgressBar progressView = holder.mImageProgressBar;
            final ImageView imageView = holder.mItemImg;

            Picasso.with(context)
                    .load(p.getImgUrl())
                    .into(holder.mItemImg, new Callback() {

                        @Override
                        public void onSuccess() {

                            progressView.setVisibility(android.view.View.GONE);
                        }

                        @Override
                        public void onError() {

                            progressView.setVisibility(android.view.View.GONE);
                            imageView.setImageResource(R.drawable.img_loading_error);
                        }
                    });

        }

        // Double-tap like for image

        if (p.getImagesCount() != 0) {

            holder.mImagesCounterLayout.setVisibility(android.view.View.VISIBLE);
            holder.mImagesCounterLabel.setText(" +" + Integer.toString(p.getImagesCount()));
        }

        if (p.getVideoUrl() != null && p.getVideoUrl().length() != 0) {

            holder.mVideoLayout.setVisibility(android.view.View.VISIBLE);
            holder.mVideoImg.setVisibility(android.view.View.VISIBLE);
            holder.mVideoProgressBar.setVisibility(android.view.View.VISIBLE);

            if (p.getPreviewVideoImgUrl().length() != 0) {

                final ImageView imageView = holder.mVideoImg;
                final ProgressBar progressView = holder.mVideoProgressBar;
                final ImageView playButtonView = holder.mItemPlayVideo;

                Picasso.with(context)
                        .load(p.getPreviewVideoImgUrl())
                        .into(holder.mVideoImg, new Callback() {

                            @Override
                            public void onSuccess() {

                                progressView.setVisibility(android.view.View.GONE);
                                playButtonView.setVisibility(android.view.View.VISIBLE);
                            }

                            @Override
                            public void onError() {

                                progressView.setVisibility(android.view.View.GONE);
                                playButtonView.setVisibility(android.view.View.GONE);
                                imageView.setImageResource(R.drawable.ic_video_preview);
                            }
                        });

                // Double-tap like for video thumbnail
                final GestureDetector videoGestureDetector = new GestureDetector(holder.mVideoImg.getContext(), new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        if (p.getVideoUrl().length() != 0) {
                            watchVideo(p.getVideoUrl());
                        } else {
                            watchYoutubeVideo(p.getYouTubeVideoCode());
                        }
                        return true;
                    }
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        // Always show video heart
                        if (holder.mVideoHeartOverlay != null) {
                            showHeartAnimation(holder.mVideoHeartOverlay);
                        }

                        // Update like if needed
                        if (App.getInstance().getId() != 0 && !p.isMyLike()) {
                            p.setMyLike(true);
                            p.setLikesCount(p.getLikesCount() + 1);

                            like(p, position, 0);

                            new Handler().postDelayed(() -> notifyItemChanged(position, "reactions"), 350);
                        }return true;
                    }
                });
                holder.mVideoImg.setOnTouchListener((v, event) -> {
                    videoGestureDetector.onTouchEvent(event);
                    return true;
                });

            } else {

                holder.mVideoProgressBar.setVisibility(android.view.View.GONE);
                holder.mVideoImg.setVisibility(android.view.View.VISIBLE);
                holder.mItemPlayVideo.setVisibility(android.view.View.GONE);
                holder.mVideoImg.setImageResource(R.drawable.ic_video_preview);
            }

        } else if (p.getYouTubeVideoUrl() != null && p.getYouTubeVideoUrl().length() != 0) {

            holder.mVideoLayout.setVisibility(android.view.View.VISIBLE);
            holder.mVideoImg.setVisibility(android.view.View.VISIBLE);
            holder.mVideoProgressBar.setVisibility(android.view.View.VISIBLE);

            final ProgressBar progressView = holder.mVideoProgressBar;
            final ImageView playButtonView = holder.mItemPlayVideo;

            Picasso.with(context)
                    .load(p.getYouTubeVideoImg())
                    .into(holder.mVideoImg, new Callback() {

                        @Override
                        public void onSuccess() {

                            progressView.setVisibility(android.view.View.GONE);
                            playButtonView.setVisibility(android.view.View.VISIBLE);
                        }

                        @Override
                        public void onError() {
                            // TODO Auto-generated method stub

                        }
                    });

        } else {

            holder.mVideoImg.setVisibility(android.view.View.GONE);
        }

        final GestureDetector gestureDetector = new GestureDetector(holder.mItemImg.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                // Open fullscreen image
                ArrayList<MediaItem> images = new ArrayList<>();
                images.add(new MediaItem("", "", p.getImgUrl(), "", 0));
                Intent i = new Intent(context, MediaViewerActivity.class);
                i.putExtra("position", 0);
                i.putExtra("itemId", p.getId());
                i.putExtra("count", p.getImagesCount());
                i.putParcelableArrayListExtra("images", images);
                context.startActivity(i);
                return true;
            }
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // Always show heart animation
                if (holder.mHeartOverlay != null) {
                    showHeartAnimation(holder.mHeartOverlay);
                }
                try { startHeartTrail(holder, e.getX(), e.getY(), false); } catch (Throwable ignored) {}

                // Update state & call like() only if not liked yet
                if (App.getInstance().getId() != 0 && !p.isMyLike()) {
                    p.setMyLike(true);
                    p.setLikesCount(p.getLikesCount() + 1);

                    // Fire network call
                    like(p, position, 0);

                    // Delay UI rebind so animation isn't interrupted
                    new Handler().postDelayed(() -> notifyItemChanged(position, "reactions"), 350);
                }return true;
            }
        });
        holder.mItemImg.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        holder.mVideoImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // play video

                if (p.getVideoUrl().length() != 0) {

                    watchVideo(p.getVideoUrl());

                } else {

                    watchYoutubeVideo(p.getYouTubeVideoCode());
                }
            }
        });


        holder.mItemPlayVideo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (p.getVideoUrl().length() != 0) {

                    watchVideo(p.getVideoUrl());

                } else {

                    watchYoutubeVideo(p.getYouTubeVideoCode());
                }
            }
        });

        if (p.getPostType() == POST_TYPE_DEFAULT) {

            if (p.getPost().length() != 0) {

                holder.mItemDescription.setVisibility(android.view.View.VISIBLE);
                holder.mItemDescription.setText(p.getPost().replaceAll("<br>", "\n"));

                holder.mItemDescription.setMovementMethod(LinkMovementMethod.getInstance());

                String textHtml = p.getPost();

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

                    holder.mItemDescription.setText(mTagSelectingTextview.addClickablePart(Html.fromHtml(textHtml, Html.FROM_HTML_MODE_LEGACY).toString(), this, hashTagHyperLinkDisabled, HASHTAGS_COLOR), TextView.BufferType.SPANNABLE);
                    // Make @username mentions clickable in post captions
                    CharSequence currentCaption = holder.mItemDescription.getText();
                    if (currentCaption != null && currentCaption.length() > 0) {
                        CharSequence linkedCaption = UsernameLinkifier.linkifyUsernames(
                                holder.mItemDescription.getContext(),
                                currentCaption
                        );
                        holder.mItemDescription.setText(linkedCaption);
                        UsernameLinkifier.enableClicks(holder.mItemDescription);
                    }


                } else {


                    holder.mItemDescription.setText(mTagSelectingTextview.addClickablePart(Html.fromHtml(textHtml).toString(), this, hashTagHyperLinkDisabled, HASHTAGS_COLOR), TextView.BufferType.SPANNABLE);
                }

                holder.mItemDescription.setOnLongClickListener(new View.OnLongClickListener() {



                    @Override
                    public boolean onLongClick(View v) {

                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("msg", p.getPost().replaceAll("<br>", "\n"));
                        clipboard.setPrimaryClip(clip);

                        Toast.makeText(context, context.getString(R.string.msg_copied_to_clipboard), Toast.LENGTH_SHORT).show();

                        return false;
                    }
                });

            } else {

                holder.mItemDescription.setVisibility(android.view.View.GONE);
            }

        } else if (p.getPostType() == POST_TYPE_PHOTO_UPDATE) {

            holder.mItemDescription.setVisibility(android.view.View.VISIBLE);
            holder.mItemDescription.setText(p.getFromUserFullname() + " " + context.getString(R.string.label_updated_profile_photo));

        } else if (p.getPostType() == POST_TYPE_COVER_UPDATE) {

            // POST_TYPE_COVER_UPDATE

            holder.mItemDescription.setVisibility(android.view.View.VISIBLE);
            holder.mItemDescription.setText(p.getFromUserFullname() + " " + context.getString(R.string.label_updated_cover_photo));
        }

        holder.mItemTimeAgo.setVisibility(android.view.View.VISIBLE);
        holder.mItemTimeAgo.setText(p.getTimeAgo());


        holder.mItemMenuButton.setVisibility(android.view.View.VISIBLE);

        holder.mItemMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                onItemMenuButtonClick(view, p, position);
            }
        });

        final ImageView mItemMenuButton = holder.mItemMenuButton;

        holder.mItemMenuButton.setOnTouchListener(new View.OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                    animateIcon(mItemMenuButton);
                }

                return false;
            }
        });

        if (p.getCommentsCount() > 0 || p.getLikesCount() > 0) {

            holder.mItemCommentsCountImage.setVisibility(android.view.View.GONE);
            holder.mItemCommentsCountText.setVisibility(android.view.View.GONE);

            holder.mItemLikesCountImage.setVisibility(android.view.View.GONE);
            holder.mItemLikesCountText.setVisibility(android.view.View.GONE);

            holder.mItemCountersContainer.setVisibility(android.view.View.VISIBLE);

            if (p.getCommentsCount() > 0) {

                holder.mItemCommentsCountImage.setVisibility(android.view.View.VISIBLE);
                holder.mItemCommentsCountText.setVisibility(android.view.View.VISIBLE);

                holder.mItemCommentsCountText.setText(Integer.toString(p.getCommentsCount()));
            }

            if (p.getLikesCount() > 0) {

                holder.mItemLikesCountImage.setVisibility(android.view.View.VISIBLE);
                holder.mItemLikesCountText.setVisibility(android.view.View.VISIBLE);

                holder.mItemLikesCountText.setText(Integer.toString(p.getLikesCount()));
            }

        } else {

            holder.mItemCountersContainer.setVisibility(android.view.View.GONE);
        }

        holder.mItemCountersContainerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (App.getInstance().getId() == 0) {

                    showAuthorizeDlg(view, p,  ITEM_ACTIONS_MENU, position);

                } else {

                    showCommentsDialog(p, position);
                }

                // viewItem(p);
            }
        });

        if (p.getRePostsCount() > 0) {

            holder.mItemRepostsCount.setVisibility(android.view.View.VISIBLE);
            holder.mItemRepostsCount.setText(Integer.toString(p.getRePostsCount()));

        } else {

            holder.mItemRepostsCount.setVisibility(android.view.View.GONE);
        }

        if (p.isMyLike()) {

            holder.mItemLikeButtonText.setTextColor(context.getResources().getColor(R.color.colorTextReactionAny));

            switch (p.getReaction()) {

                case 1: {

                    holder.mItemLikeImg.setImageResource(R.drawable.ic_reaction_1);
                    holder.mItemLikeButtonText.setText(R.string.label_reaction_1);

                    break;
                }

                case 2: {

                    holder.mItemLikeImg.setImageResource(R.drawable.ic_reaction_2);
                    holder.mItemLikeButtonText.setText(R.string.label_reaction_2);

                    break;
                }

                case 3: {

                    holder.mItemLikeImg.setImageResource(R.drawable.ic_reaction_3);
                    holder.mItemLikeButtonText.setText(R.string.label_reaction_3);

                    break;
                }

                case 4: {

                    holder.mItemLikeImg.setImageResource(R.drawable.ic_reaction_4);
                    holder.mItemLikeButtonText.setText(R.string.label_reaction_4);

                    break;
                }

                case 5: {

                    holder.mItemLikeImg.setImageResource(R.drawable.ic_reaction_5);
                    holder.mItemLikeButtonText.setText(R.string.label_reaction_5);

                    break;
                }

                default: {

                    holder.mItemLikeImg.setImageResource(R.drawable.ic_reaction_0);
                    holder.mItemLikeButtonText.setText(R.string.label_reaction_0);
                    holder.mItemLikeButtonText.setTextColor(context.getResources().getColor(R.color.colorTextReactionLike));

                    break;
                }
            }

        } else {

            holder.mItemLikeImg.setImageResource(R.drawable.ic_like);
            holder.mItemLikeButtonText.setText(R.string.label_reaction_0);
            holder.mItemLikeButtonText.setTextColor(context.getResources().getColor(R.color.item_action_icon_tint));
        }

        final ImageView imgLike = holder.mItemLikeImg;

        holder.mItemLikeButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                holder.mReactionsContainer.setVisibility(android.view.View.VISIBLE);
                holder.mFooterContainer.setVisibility(android.view.View.GONE);

                Handler handler = null;
                handler = new Handler();
                handler.postDelayed(new Runnable(){

                    public void run(){

                        holder.mReactionsContainer.setVisibility(android.view.View.GONE);
                        holder.mFooterContainer.setVisibility(android.view.View.VISIBLE);
                    }
                }, 2500);

                return true;
            }
        });

        holder.mItemLikeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (App.getInstance().getId() != 0) {

                    if (p.isMyLike()) {

                        p.setMyLike(false);
                        p.setLikesCount(p.getLikesCount() - 1);

                    } else {

                        p.setMyLike(true);
                        p.setLikesCount(p.getLikesCount() + 1);

                        //imgLike.setImageResource(R.drawable.ic_like_active);
                    }

                    notifyItemChanged(position, "reactions");

                    animateIcon(imgLike);
                }

                like(p, position, p.getReaction());
            }
        });

        holder.mItemReactionButton0.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (p.isMyLike()) {

                    if (p.getReaction() == 0) {

                        p.setMyLike(false);
                        p.setLikesCount(p.getLikesCount() - 1);

                    } else {

                        p.setMyLike(true);
                    }

                } else {

                    p.setMyLike(true);
                    p.setLikesCount(p.getLikesCount() + 1);
                }

//                holder.mReactionsContainer.setVisibility(android.view.View.VISIBLE);
//                holder.mFooterContainer.setVisibility(android.view.View.GONE);

                p.setReaction(0);

                notifyItemChanged(position, "reactions");

                animateIcon(imgLike);

                like(p, position, p.getReaction());
            }
        });

        holder.mItemReactionButton1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (p.isMyLike()) {

                    if (p.getReaction() == 1) {

                        p.setMyLike(false);
                        p.setLikesCount(p.getLikesCount() - 1);

                    } else {

                        p.setMyLike(true);
                    }

                } else {

                    p.setMyLike(true);
                    p.setLikesCount(p.getLikesCount() + 1);
                }

//                holder.mReactionsContainer.setVisibility(android.view.View.VISIBLE);
//                holder.mFooterContainer.setVisibility(android.view.View.GONE);

                p.setReaction(1);

                notifyItemChanged(position, "reactions");

                animateIcon(imgLike);

                like(p, position, p.getReaction());
            }
        });

        holder.mItemReactionButton2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (p.isMyLike()) {

                    if (p.getReaction() == 2) {

                        p.setMyLike(false);
                        p.setLikesCount(p.getLikesCount() - 1);

                    } else {

                        p.setMyLike(true);
                    }

                } else {

                    p.setMyLike(true);
                    p.setLikesCount(p.getLikesCount() + 1);
                }

//                holder.mReactionsContainer.setVisibility(android.view.View.VISIBLE);
//                holder.mFooterContainer.setVisibility(android.view.View.GONE);

                p.setReaction(2);

                notifyItemChanged(position, "reactions");

                animateIcon(imgLike);

                like(p, position, p.getReaction());
            }
        });

        holder.mItemReactionButton3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (p.isMyLike()) {

                    if (p.getReaction() == 3) {

                        p.setMyLike(false);
                        p.setLikesCount(p.getLikesCount() - 1);

                    } else {

                        p.setMyLike(true);
                    }

                } else {

                    p.setMyLike(true);
                    p.setLikesCount(p.getLikesCount() + 1);
                }

//                holder.mReactionsContainer.setVisibility(android.view.View.VISIBLE);
//                holder.mFooterContainer.setVisibility(android.view.View.GONE);

                p.setReaction(3);

                notifyItemChanged(position, "reactions");

                animateIcon(imgLike);

                like(p, position, p.getReaction());
            }
        });

        holder.mItemReactionButton4.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (p.isMyLike()) {

                    if (p.getReaction() == 4) {

                        p.setMyLike(false);
                        p.setLikesCount(p.getLikesCount() - 1);

                    } else {

                        p.setMyLike(true);
                    }

                } else {

                    p.setMyLike(true);
                    p.setLikesCount(p.getLikesCount() + 1);
                }

//                holder.mReactionsContainer.setVisibility(android.view.View.VISIBLE);
//                holder.mFooterContainer.setVisibility(android.view.View.GONE);

                p.setReaction(4);

                notifyItemChanged(position, "reactions");

                animateIcon(imgLike);

                like(p, position, p.getReaction());
            }
        });

        holder.mItemReactionButton5.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (p.isMyLike()) {

                    if (p.getReaction() == 5) {

                        p.setMyLike(false);
                        p.setLikesCount(p.getLikesCount() - 1);

                    } else {

                        p.setMyLike(true);
                    }

                } else {

                    p.setMyLike(true);
                    p.setLikesCount(p.getLikesCount() + 1);
                }

//                holder.mReactionsContainer.setVisibility(android.view.View.VISIBLE);
//                holder.mFooterContainer.setVisibility(android.view.View.GONE);

                p.setReaction(5);

                notifyItemChanged(position, "reactions");

                animateIcon(imgLike);

                like(p, position, p.getReaction());
            }
        });

        holder.mItemCommentButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (App.getInstance().getId() == 0) {

                    showAuthorizeDlg(view, p,  ITEM_ACTIONS_MENU, position);

                } else {

                    showCommentsDialog(p, position);
                }

                // viewItem(p);
            }
        });

        holder.mItemRepostButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (App.getInstance().getId() == 0) {

                    showAuthorizeDlg(view, p,  ITEM_ACTIONS_MENU, position);

                } else {

                    onItemMenuButtonClickListener.onItemClick(view, p,  ITEM_ACTION_REPOST, position);
                }
            }
        });

        if (p.getUrlPreviewLink() != null && p.getUrlPreviewLink().length() > 0) {

            holder.mLinkContainer.setVisibility(android.view.View.VISIBLE);

            holder.mLinkContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!p.getUrlPreviewLink().startsWith("https://") && !p.getUrlPreviewLink().startsWith("http://")){

                        p.setUrlPreviewLink("http://" + p.getUrlPreviewLink());
                    }

                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(p.getUrlPreviewLink()));
                    context.startActivity(i);
                }
            });

            if (p.getUrlPreviewImage() != null && p.getUrlPreviewImage().length() != 0) {

                imageLoader.get(p.getUrlPreviewImage(), ImageLoader.getImageListener(holder.mLinkImage, R.drawable.img_link, R.drawable.img_link));

            } else {

                holder.mLinkImage.setImageResource(R.drawable.img_link);
            }

            if (p.getUrlPreviewTitle() != null && p.getUrlPreviewTitle().length() != 0) {

                holder.mLinkTitle.setText(p.getUrlPreviewTitle());

            } else {

                holder.mLinkTitle.setText("Link");
            }

            if (p.getUrlPreviewDescription() != null && p.getUrlPreviewDescription().length() != 0) {

                holder.mLinkDescription.setText(p.getUrlPreviewDescription());

            } else {

                holder.mLinkDescription.setText("Link");
            }
        }



        // Repost

        if (p.getRePostId() != 0) {

            holder.mCardRepostContainer.setVisibility(android.view.View.VISIBLE);

            holder.mReImageLayout.setVisibility(android.view.View.GONE);
            holder.mReVideoLayout.setVisibility(android.view.View.GONE);

            if (p.getRePostRemoveAt() == 0) {

                // original post available

                holder.mReMessageContainer.setVisibility(android.view.View.GONE);
                holder.mReLinkContainer.setVisibility(android.view.View.GONE);

                holder.mReAuthorPhoto.setVisibility(android.view.View.VISIBLE);

                holder.mReAuthorPhoto.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        if (App.getInstance().getId() != 0) {

                            Intent intent = new Intent(context, ViewItemActivity.class);
                            intent.putExtra("itemId", p.getRePostId());
                            context.startActivity(intent);
                        }
                    }
                });

                if (p.getRePostFromUserPhotoUrl().length() != 0) {

                    imageLoader.get(p.getRePostFromUserPhotoUrl(), ImageLoader.getImageListener(holder.mReAuthorPhoto, R.drawable.profile_default_photo, R.drawable.profile_default_photo));

                } else {

                    holder.mReAuthorPhoto.setVisibility(android.view.View.VISIBLE);
                    holder.mReAuthorPhoto.setImageResource(R.drawable.profile_default_photo);
                }

                if (p.getRePostFromUserVerify() == 1) {

                    holder.mReAuthorIcon.setVisibility(android.view.View.VISIBLE);

                } else {

                    holder.mReAuthorIcon.setVisibility(android.view.View.GONE);
                }

                holder.mReAuthor.setVisibility(android.view.View.VISIBLE);
                holder.mReAuthor.setText(p.getRePostFromUserFullname());

                holder.mReAuthor.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        if (App.getInstance().getId() != 0) {

                            Intent intent = new Intent(context, ViewItemActivity.class);
                            intent.putExtra("itemId", p.getRePostId());
                            context.startActivity(intent);
                        }
                    }
                });

                holder.mReAuthorUsername.setVisibility(android.view.View.VISIBLE);
                holder.mReAuthorUsername.setText("@" + p.getRePostFromUserUsername());

                if (p.getRePostImgUrl().length() != 0) {

                    holder.mReImageLayout.setVisibility(android.view.View.VISIBLE);

                    if (p.getReImagesCount() != 0) {

                        holder.mReImagesCounterLayout.setVisibility(android.view.View.VISIBLE);
                        holder.mReImagesCounterLabel.setText(" +" + Integer.toString(p.getReImagesCount()));

                    } else {

                        holder.mReImagesCounterLayout.setVisibility(android.view.View.GONE);
                    }

                    holder.mReItemImg.setVisibility(android.view.View.VISIBLE);
                    holder.mReImageProgressBar.setVisibility(android.view.View.VISIBLE);

                    final android.widget.ProgressBar reProgressView = holder.mReImageProgressBar;
                    final android.widget.ImageView reImageView = holder.mReItemImg;

// Glide with optional blurred thumbnail and a simple cross-fade
                    com.bumptech.glide.request.RequestOptions blurOptions = new com.bumptech.glide.request.RequestOptions()
                            .transform(new jp.wasabeef.glide.transformations.BlurTransformation(20, 4));

                    Glide.with(context)
                            .load(p.getRePostImgUrl())
                            .thumbnail(Glide.with(context).load(p.getRePostImgUrl()).apply(blurOptions))
                            .transition(com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade(200))
                            .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                                @Override
                                public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, Object model,
                                                            com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                                    try { reProgressView.setVisibility(android.view.View.GONE); } catch (Throwable ignore) {}
                                    try { reImageView.setImageResource(R.drawable.img_loading_error); } catch (Throwable ignore) {}
                                    return false;
                                }
                                @Override
                                public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model,
                                                               com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                               com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                    try { reProgressView.setVisibility(android.view.View.GONE); } catch (Throwable ignore) {}
                                    return false;
                                }
                            })
                            .into(holder.mReItemImg);

                }

                if (p.getReVideoUrl() != null && p.getReVideoUrl().length() != 0) {

                    holder.mReVideoLayout.setVisibility(android.view.View.VISIBLE);

                    holder.mRePlayVideo.setVisibility(android.view.View.GONE);
                    holder.mReVideoImg.setVisibility(android.view.View.VISIBLE);
                    holder.mReVideoProgressBar.setVisibility(android.view.View.VISIBLE);

                    if (p.getRePreviewVideoImageUrl() != null && p.getRePreviewVideoImageUrl().length() != 0) {

                        final ImageView reImageView = holder.mReVideoImg;
                        final ProgressBar reProgressView = holder.mReVideoProgressBar;
                        final ImageView rePlayButtonView = holder.mRePlayVideo;

                        Picasso.with(context)
                                .load(p.getRePreviewVideoImageUrl())
                                .into(holder.mReVideoImg, new Callback() {

                                    @Override
                                    public void onSuccess() {

                                        reProgressView.setVisibility(android.view.View.GONE);
                                        rePlayButtonView.setVisibility(android.view.View.VISIBLE);
                                    }

                                    @Override
                                    public void onError() {

                                        reProgressView.setVisibility(android.view.View.GONE);
                                        rePlayButtonView.setVisibility(android.view.View.VISIBLE);
                                        reImageView.setImageResource(R.drawable.img_loading_error);
                                    }
                                });

                    } else {

                        holder.mReVideoProgressBar.setVisibility(android.view.View.GONE);
                        holder.mReVideoImg.setImageResource(R.drawable.ic_video_preview);
                    }

                } else if (p.getReYouTubeVideoUrl() != null && p.getReYouTubeVideoUrl().length() != 0) {

                    holder.mReVideoLayout.setVisibility(android.view.View.VISIBLE);

                    holder.mReVideoImg.setVisibility(android.view.View.VISIBLE);
                    holder.mReVideoProgressBar.setVisibility(android.view.View.VISIBLE);

                    final ProgressBar reProgressView = holder.mReVideoProgressBar;
                    final ImageView rePlayButtonView = holder.mRePlayVideo;

                    Picasso.with(context)
                            .load(p.getReYouTubeVideoImg())
                            .into(holder.mReVideoImg, new Callback() {

                                @Override
                                public void onSuccess() {

                                    reProgressView.setVisibility(android.view.View.GONE);
                                    rePlayButtonView.setVisibility(android.view.View.VISIBLE);
                                }

                                @Override
                                public void onError() {
                                    // TODO Auto-generated method stub

                                }
                            });

                }

                holder.mReItemImg.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        ArrayList<MediaItem> images = new ArrayList<>();
                        images.add(new MediaItem("", "", p.getRePostImgUrl(), "", 0));

                        Intent i = new Intent(context, MediaViewerActivity.class);
                        i.putExtra("position", 0);
                        i.putExtra("itemId", p.getRePostId());
                        i.putExtra("count", p.getReImagesCount());
                        i.putParcelableArrayListExtra("images", images);
                        context.startActivity(i);
                    }
                });

                holder.mReVideoImg.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        if (p.getReVideoUrl().length() != 0) {

                            watchVideo(p.getReVideoUrl());

                        } else {

                            watchYoutubeVideo(p.getReYouTubeVideoCode());
                        }
                    }
                });

                holder.mRePlayVideo.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        if (p.getReVideoUrl().length() != 0) {

                            watchVideo(p.getReVideoUrl());

                        } else {

                            watchYoutubeVideo(p.getReYouTubeVideoCode());
                        }
                    }
                });

                if (p.getRePostPost().length() != 0) {

                    holder.mReDescription.setVisibility(android.view.View.VISIBLE);
                    holder.mReDescription.setText(p.getRePostPost().replaceAll("<br>", "\n"));

                    holder.mReDescription.setMovementMethod(LinkMovementMethod.getInstance());

                    String textHtml = p.getRePostPost();

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

                        holder.mReDescription.setText(mTagSelectingTextview.addClickablePart(Html.fromHtml(textHtml, Html.FROM_HTML_MODE_LEGACY).toString(), this, hashTagHyperLinkDisabled, HASHTAGS_COLOR), TextView.BufferType.SPANNABLE);

                    } else {

                        holder.mReDescription.setText(mTagSelectingTextview.addClickablePart(Html.fromHtml(textHtml).toString(), this, hashTagHyperLinkDisabled, HASHTAGS_COLOR), TextView.BufferType.SPANNABLE);
                    }

                } else {

                    holder.mReDescription.setVisibility(android.view.View.GONE);
                }

                holder.mReTimeAgo.setVisibility(android.view.View.VISIBLE);
                holder.mReTimeAgo.setText(p.getRePostTimeAgo());


                if (p.getReUrlPreviewLink() != null && p.getReUrlPreviewLink().length() > 0) {

                    holder.mReLinkContainer.setVisibility(android.view.View.VISIBLE);

                    holder.mReLinkContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (!p.getReUrlPreviewLink().startsWith("https://") && !p.getReUrlPreviewLink().startsWith("http://")){

                                p.setReUrlPreviewLink("http://" + p.getReUrlPreviewLink());
                            }

                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(p.getReUrlPreviewLink()));
                            context.startActivity(i);
                        }
                    });

                    if (p.getReUrlPreviewImage() != null && p.getReUrlPreviewImage().length() != 0) {

                        imageLoader.get(p.getReUrlPreviewImage(), ImageLoader.getImageListener(holder.mReLinkImage, R.drawable.img_link, R.drawable.img_link));

                    } else {

                        holder.mReLinkImage.setImageResource(R.drawable.img_link);
                    }

                    if (p.getReUrlPreviewTitle() != null && p.getReUrlPreviewTitle().length() != 0) {

                        holder.mReLinkTitle.setText(p.getReUrlPreviewTitle());

                    } else {

                        holder.mReLinkTitle.setText("Link");
                    }

                    if (p.getReUrlPreviewDescription() != null && p.getReUrlPreviewDescription().length() != 0) {

                        holder.mReLinkDescription.setText(p.getReUrlPreviewDescription());

                    } else {

                        holder.mReLinkDescription.setText("Link");
                    }
                }


            } else {

                // original post has deleted
                // show message

                holder.mReMessageContainer.setVisibility(android.view.View.VISIBLE);

                holder.mReHeaderContainer.setVisibility(android.view.View.GONE);
                holder.mReBodyContainer.setVisibility(android.view.View.GONE);
            }

        } else {

            // not repost
            // hide repost container

            holder.mCardRepostContainer.setVisibility(android.view.View.GONE);
        }
    }

    private void onItemMenuButtonClick(final View view, final Item post, final int position){

        onItemMenuButtonClickListener.onItemClick(view, post, ITEM_ACTIONS_MENU, position);
    }

    private void animateIcon(ImageView icon) {

        ScaleAnimation scale = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(175);
        scale.setInterpolator(new LinearInterpolator());

        icon.startAnimation(scale);
    }

    public void watchYoutubeVideo(String id) {

        if (YOUTUBE_API_KEY.length() > 5) {

            Intent i = new Intent(context, ViewYouTubeVideoActivity.class);
            i.putExtra("videoCode", id);
            context.startActivity(i);

        } else {

            try {

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
                context.startActivity(intent);

            } catch (ActivityNotFoundException ex) {

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + id));
                context.startActivity(intent);
            }
        }
    }

    public void watchVideo(String videoUrl) {

        Intent i = new Intent(context, VideoViewActivity.class);
        i.putExtra("videoUrl", videoUrl);
        context.startActivity(i);
    }

    private String getLocation(Item item) {

        String location = "";

        if (item.getCountry().length() > 0 || item.getCity().length() > 0) {

            if (item.getCountry().length() > 0) {

                location = item.getCountry();
            }

            if (item.getCity().length() > 0) {

                if (item.getCountry().length() > 0) {

                    location = location + ", " + item.getCity();

                } else {

                    location = item.getCity();
                }
            }
        }

        return location;
    }

    private void showHeartAnimation(final ImageView heartOverlay) {
        if (heartOverlay == null) return;

        // Ensure we run on the UI thread and that the overlay is moved to front before animating.
        heartOverlay.post(() -> {
            try {
                // Bring the overlay itself to front (don't touch the parent; that previously caused ordering issues)
                try {
                    heartOverlay.bringToFront();
                } catch (Throwable ignore) {}

                // For Lollipop+ ensure elevation/z is higher so it sits top-most
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    try {
                        heartOverlay.setZ(100f);
                    } catch (Throwable ignoreZ) { }
                }

                heartOverlay.setVisibility(android.view.View.VISIBLE);
                heartOverlay.setScaleX(0.1f);
                heartOverlay.setScaleY(0.1f);
                heartOverlay.setAlpha(1f);
                heartOverlay.animate()
                        .scaleX(1.5f)
                        .scaleY(1.5f)
                        .setDuration(200)
                        .withEndAction(() -> heartOverlay.animate()
                                .alpha(0f)
                                .setDuration(300)
                                .withEndAction(() -> heartOverlay.setVisibility(android.view.View.GONE))
                                .start())
                        .start();
            } catch (Throwable t) {
                // Best-effort fallback: simply fade the heart and hide it
                try {
                    heartOverlay.setVisibility(android.view.View.VISIBLE);
                    heartOverlay.animate()
                            .alpha(0f)
                            .setDuration(300)
                            .withEndAction(() -> heartOverlay.setVisibility(android.view.View.GONE))
                            .start();
                } catch (Throwable ignored) {}
            }
        });
    }


    private void like(final Item p, final int position, final int reaction) {

        if (p.getRemoveAt() != 0) {

            return;
        }

        if (App.getInstance().getId() == 0) {

            showAuthorizeDlg(null, p, ITEM_ACTIONS_MENU, position);

            return;
        }

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_REACTIONS_MAKE, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {

                                p.setLikesCount(response.getInt("likesCount"));
                                p.setMyLike(response.getBoolean("myLike"));
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            Log.e("Item.Reaction", response.toString());

                            // Interstitial ad

                            if (App.getInstance().getInterstitialAdSettings().getInterstitialAdAfterNewLike() != 0 && App.getInstance().getAdmob() == ADMOB_DISABLED) {

                                App.getInstance().getInterstitialAdSettings().setCurrentInterstitialAdAfterNewLike(App.getInstance().getInterstitialAdSettings().getCurrentInterstitialAdAfterNewLike() + 1);

                                if (App.getInstance().getInterstitialAdSettings().getCurrentInterstitialAdAfterNewLike() >= App.getInstance().getInterstitialAdSettings().getInterstitialAdAfterNewLike()) {

                                    App.getInstance().getInterstitialAdSettings().setCurrentInterstitialAdAfterNewLike(0);

                                    App.getInstance().showInterstitialAd(null);
                                }

                                App.getInstance().saveData();
                            }

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e("Item.Reaction", error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("reaction", Integer.toString(p.getReaction()));
                params.put("itemId", Long.toString(p.getId()));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void showCommentsDialog(final Item item, final int item_position) {

        if (item.getRemoveAt() != 0) {

            return;
        }

        final ArrayList<Comment> itemsList;
        final CommentsListAdapter itemsAdapter;

        itemsList = new ArrayList<Comment>();
        itemsAdapter = new CommentsListAdapter(context, itemsList);

        final Dialog dialog = new Dialog(context, R.style.CommentsDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.setContentView(R.layout.dialog_comments);
        dialog.setCancelable(true);

        final LinearLayout mItemInfoContainer = (LinearLayout) dialog.findViewById(R.id.item_info_container);
        mItemInfoContainer.setVisibility(android.view.View.GONE);

        final MaterialRippleLayout mShowLikesButton = (MaterialRippleLayout) dialog.findViewById(R.id.show_likes_button);
        mShowLikesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (App.getInstance().getId() != 0) {

                    Intent intent = new Intent(context, ReactionsActivity.class);
                    intent.putExtra("itemId", item.getId());
                    context.startActivity(intent);
                }
            }
        });

        final MaterialRippleLayout mCloseDialogButton = (MaterialRippleLayout) dialog.findViewById(R.id.close_dialog_button);
        mCloseDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.cancel();
            }
        });

        final TextView mLikesCountLabel = (TextView) dialog.findViewById(R.id.likes_count_label);

        if (item.getLikesCount() != 0) {

            mItemInfoContainer.setVisibility(android.view.View.VISIBLE);

            mLikesCountLabel.setText(Integer.toString(item.getLikesCount()));
        }

        final EmojiconEditText mCommentEditor = (EmojiconEditText) dialog.findViewById(R.id.comment_editor);
        final LinearLayout mSendButton = (LinearLayout) dialog.findViewById(R.id.send_comment_button);

        // Enable @mention suggestions in the comment input
        new MentionHelper(mCommentEditor);


        final ProgressBar mProgressBar = (ProgressBar) dialog.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(android.view.View.GONE);

        final TextView mMessageLabel = (TextView) dialog.findViewById(R.id.message_label);
        mMessageLabel.setVisibility(android.view.View.GONE);

        final NestedScrollView mDlgNestedView = (NestedScrollView) dialog.findViewById(R.id.nested_view);
        final RecyclerView mDlgRecyclerView = (RecyclerView) dialog.findViewById(R.id.recycler_view);

        final GridLayoutManager mLayoutManager = new GridLayoutManager(context, 1);
        mDlgRecyclerView.setLayoutManager(mLayoutManager);

        itemsAdapter.setOnMoreButtonClickListener(new CommentsListAdapter.OnItemMenuButtonClickListener() {

            @Override
            public void onItemClick(View v, Comment obj, int actionId, final int position) {

                switch (actionId){

                    case R.id.action_remove: {

                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                        alertDialog.setTitle(context.getText(R.string.label_delete));

                        alertDialog.setMessage(context.getText(R.string.label_delete_comment));
                        alertDialog.setCancelable(true);

                        alertDialog.setNegativeButton(context.getText(R.string.action_cancel), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.cancel();
                            }
                        });

                        alertDialog.setPositiveButton(context.getText(R.string.action_yes), new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {

                                Api api = new Api(context);
                                api.commentDelete(itemsList.get(position).getId(), Constants.ITEM_TYPE_POST);

                                itemsList.remove(position);
                                itemsAdapter.notifyItemRemoved(position);

                                item.setCommentsCount(item.getCommentsCount() - 1);

                                notifyItemChanged(item_position);
                            }
                        });

                        alertDialog.show();

                        break;
                    }

                    case R.id.action_reply: {

                        if (App.getInstance().getId() != 0) {

                            replyToUserId = obj.getFromUserId();

                            mCommentEditor.setText("@" + obj.getOwner().getUsername() + ", ");
                            mCommentEditor.setSelection(mCommentEditor.getText().length());

                            mCommentEditor.requestFocus();

                        }

                        break;
                    }

                    case R.id.action_report: {

                        String[] profile_report_categories = new String[] {

                                context.getText(R.string.label_profile_report_0).toString(),
                                context.getText(R.string.label_profile_report_1).toString(),
                                context.getText(R.string.label_profile_report_2).toString(),
                                context.getText(R.string.label_profile_report_3).toString(),

                        };

                        androidx.appcompat.app.AlertDialog.Builder alertDialog = new androidx.appcompat.app.AlertDialog.Builder(context);
                        alertDialog.setTitle(context.getText(R.string.label_post_report_title));

                        alertDialog.setSingleChoiceItems(profile_report_categories, 0, null);
                        alertDialog.setCancelable(true);

                        alertDialog.setNegativeButton(context.getText(R.string.action_cancel), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.cancel();
                            }
                        });

                        alertDialog.setPositiveButton(context.getText(R.string.action_ok), new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {

                                androidx.appcompat.app.AlertDialog alert = (androidx.appcompat.app.AlertDialog) dialog;
                                int reason = alert.getListView().getCheckedItemPosition();

                                Toast.makeText(context, context.getString(R.string.label_item_reported), Toast.LENGTH_SHORT).show();
                            }
                        });

                        alertDialog.show();

                        break;
                    }
                }
            }
        });

        mDlgRecyclerView.setAdapter(itemsAdapter);

        mDlgRecyclerView.setNestedScrollingEnabled(true);

        itemsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onChanged() {

                super.onChanged();

                if (itemsList.size() != 0) {

                    mDlgRecyclerView.setVisibility(android.view.View.VISIBLE);
                    mProgressBar.setVisibility(android.view.View.GONE);
                    mMessageLabel.setVisibility(android.view.View.GONE);

                    mDlgNestedView.post(new Runnable() {

                        @Override
                        public void run() {
                            // Select the last row so it will scroll into view...
                            mDlgNestedView.fullScroll(View.FOCUS_DOWN);
                        }
                    });

                } else {

                    mProgressBar.setVisibility(android.view.View.GONE);
                    mMessageLabel.setVisibility(android.view.View.VISIBLE);
                }
            }
        });

        if (item.getCommentsCount() != 0) {

            if (itemsList.size() == 0) {

                mMessageLabel.setVisibility(android.view.View.GONE);
                mDlgRecyclerView.setVisibility(android.view.View.GONE);
                mProgressBar.setVisibility(android.view.View.VISIBLE);

                Api api = new Api(context);
                api.getItemComments(item.getId(), itemsList, itemsAdapter);
            }

        } else {

            mMessageLabel.setVisibility(android.view.View.VISIBLE);
        }

        mCommentEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                mSendButton.setEnabled(!s.toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String text = mCommentEditor.getText().toString().trim();

                if (text.length() != 0) {

                    item.setCommentsCount(item.getCommentsCount() + 1);

                    notifyItemChanged(item_position);

                    Api api = new Api(context);
                    api.sendComment(item.getId(), Constants.ITEM_TYPE_POST, replyToUserId, text, itemsList, itemsAdapter);

                    replyToUserId = 0;
                }

                mCommentEditor.setText("");
            }
        });

        dialog.show();

        WindowManager.LayoutParams lp = new  WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);
    }

    public void swapItem(int fromPosition,int toPosition){

        Collections.swap(items, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void showAuthorizeDlg(View view, Item obj, int actionId, int position) {

        onItemMenuButtonClickListener.onItemClick(view, obj, actionId, position);

//        androidx.appcompat.app.AlertDialog.Builder alertDialog = new androidx.appcompat.app.AlertDialog.Builder(context);
//        alertDialog.setTitle(context.getText(R.string.dlg_authorization_title));
//
//        alertDialog.setMessage(context.getText(R.string.dlg_authorization_msg));
//        alertDialog.setCancelable(true);
//
//        alertDialog.setNegativeButton(context.getText(R.string.action_login), new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//                Intent i = new Intent(context, LoginActivity.class);
//                context.startActivity(i);
//
//                dialog.cancel();
//            }
//        });
//
//        alertDialog.setPositiveButton(context.getText(R.string.action_signup), new DialogInterface.OnClickListener() {
//
//            public void onClick(DialogInterface dialog, int which) {
//
//                Intent i = new Intent(context, RegisterActivity.class);
//                context.startActivity(i);
//
//                dialog.cancel();
//            }
//        });
//
//        alertDialog.setNeutralButton(context.getText(R.string.action_cancel), new DialogInterface.OnClickListener() {
//
//            public void onClick(DialogInterface dialog, int which) {
//
//                dialog.cancel();
//            }
//        });
//
//        alertDialog.show();
    }

    @Override
    public void clickedTag(CharSequence tag) {

        if (App.getInstance().getId() != 0) {

            Intent i = new Intent(context, HashtagsActivity.class);
            i.putExtra("hashtag", tag);
            context.startActivity(i);
        }
    }

    @Override
    public int getItemCount() {

        return items.size();
    }

    public int getItemCount(int viewType) {

        int cnt = 0;

        for (int i = 0; i < items.size(); i++) {

            Item item = items.get(i);

            if (item.getViewType() == viewType) {

                cnt++;
            }

        }

        return cnt;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {

        final Item p = items.get(position);

        return p.getViewType();
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads != null && !payloads.isEmpty() && payloads.contains(PAYLOAD_CAPTION_TOGGLE)) { bindCaptionOnly(holder, position); return; }

        if (payloads != null && !payloads.isEmpty()) {
            for (Object payload : payloads) {
                if (payload instanceof String && ((String) payload).equals("reactions")) {
                    // Partial update: update reaction UI and counters only, do NOT release player or change playerView
                    Item p = items.get(position);

                    // Update like/reaction display
                    if (p.isMyLike()) {
                        holder.mItemLikeButtonText.setTextColor(context.getResources().getColor(R.color.colorTextReactionAny));
                        switch (p.getReaction()) {
                            case 1: {
                                holder.mItemLikeImg.setImageResource(R.drawable.ic_reaction_1);
                                holder.mItemLikeButtonText.setText(R.string.label_reaction_1);
                                break;
                            }
                            case 2: {
                                holder.mItemLikeImg.setImageResource(R.drawable.ic_reaction_2);
                                holder.mItemLikeButtonText.setText(R.string.label_reaction_2);
                                break;
                            }
                            case 3: {
                                holder.mItemLikeImg.setImageResource(R.drawable.ic_reaction_3);
                                holder.mItemLikeButtonText.setText(R.string.label_reaction_3);
                                break;
                            }
                            case 4: {
                                holder.mItemLikeImg.setImageResource(R.drawable.ic_reaction_4);
                                holder.mItemLikeButtonText.setText(R.string.label_reaction_4);
                                break;
                            }
                            case 5: {
                                holder.mItemLikeImg.setImageResource(R.drawable.ic_reaction_5);
                                holder.mItemLikeButtonText.setText(R.string.label_reaction_5);
                                break;
                            }
                            default: {
                                holder.mItemLikeImg.setImageResource(R.drawable.ic_reaction_0);
                                holder.mItemLikeButtonText.setText(R.string.label_reaction_0);
                                holder.mItemLikeButtonText.setTextColor(context.getResources().getColor(R.color.colorTextReactionLike));
                                break;
                            }
                        }
                    } else {
                        holder.mItemLikeImg.setImageResource(R.drawable.ic_like);
                        holder.mItemLikeButtonText.setText(R.string.label_reaction_0);
                        holder.mItemLikeButtonText.setTextColor(context.getResources().getColor(R.color.item_action_icon_tint));
                    }

                    // Update counters (likes/comments)
                    if (p.getCommentsCount() > 0 || p.getLikesCount() > 0) {
                        holder.mItemCommentsCountImage.setVisibility(android.view.View.GONE);
                        holder.mItemCommentsCountText.setVisibility(android.view.View.GONE);

                        holder.mItemLikesCountImage.setVisibility(android.view.View.GONE);
                        holder.mItemLikesCountText.setVisibility(android.view.View.GONE);

                        holder.mItemCountersContainer.setVisibility(android.view.View.VISIBLE);

                        if (p.getCommentsCount() > 0) {
                            holder.mItemCommentsCountImage.setVisibility(android.view.View.VISIBLE);
                            holder.mItemCommentsCountText.setVisibility(android.view.View.VISIBLE);
                            holder.mItemCommentsCountText.setText(Integer.toString(p.getCommentsCount()));
                        }

                        if (p.getLikesCount() > 0) {
                            holder.mItemLikesCountImage.setVisibility(android.view.View.VISIBLE);
                            holder.mItemLikesCountText.setVisibility(android.view.View.VISIBLE);
                            holder.mItemLikesCountText.setText(Integer.toString(p.getLikesCount()));
                        }
                    } else {
                        holder.mItemCountersContainer.setVisibility(android.view.View.GONE);
                    }

                    // Update reposts count if present
                    if (p.getRePostsCount() > 0) {
                        holder.mItemRepostsCount.setVisibility(android.view.View.VISIBLE);
                        holder.mItemRepostsCount.setText(Integer.toString(p.getRePostsCount()));
                    } else {
                        holder.mItemRepostsCount.setVisibility(android.view.View.GONE);
                    }

                    // Done with partial update
                    return;
                }
            }
        }

        // Fallback to full bind
        onBindViewHolder(holder, position);
    }

    // add this new helper
    public void pauseAll() {
        // pause + optionally release per-row player
        try {
            if (currentPlayer != null) {
                currentPlayer.setPlayWhenReady(false);
                try { currentPlayer.stop(); } catch (Throwable ignored) {}
                try { currentPlayer.release(); } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}

        if (currentPlayerViewHolder != null) {
            try { currentPlayerViewHolder.releasePlayer(); } catch (Throwable ignored) {}
            currentPlayerViewHolder = null;
        }
        currentPlayer = null;
        currentPlayingPosition = -1;

        // stop & release shared autoplay player
        try {
            if (sharedPlayer != null) {
                sharedPlayer.setPlayWhenReady(false);
                try { sharedPlayer.stop(); } catch (Throwable ignored) {}
                try { sharedPlayer.release(); } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}

        if (sharedHolder != null) {
            try { if (sharedHolder.playerView != null) sharedHolder.playerView.setPlayer(null); } catch (Throwable ignored) {}
            sharedHolder = null;
        }
        sharedPlayer = null;
        sharedTrackSelector = null;
        sharedPosition = RecyclerView.NO_POSITION;
    }


    // add this to expose autoplay check from the adapter
    public void resumeAutoplayIfVisible() {
        // autoPlayVideoIfNeeded() is private — expose it by calling it here
        try {
            autoPlayVideoIfNeeded();
        } catch (Throwable ignored) {}
    }


    // ===== Heart Burst / Emoji Trail (self-contained) =====
    private static final int HEART_TRAIL_MIN = 3;
    private static final int HEART_TRAIL_MAX = 5;

    private int dp(Context c, float v) {
        return (int)(v * c.getResources().getDisplayMetrics().density + 0.5f);
    }

    private void startHeartTrail(ViewHolder holder, float tapX, float tapY, boolean onVideo) {
        if (holder == null) return;
        final View overlay = onVideo ? holder.mVideoHeartOverlay : holder.mHeartOverlay;
        if (overlay == null) return;

        ViewParent parent = overlay.getParent();
        if (!(parent instanceof ViewGroup)) return;
        ViewGroup container = (ViewGroup) parent;

        java.util.Random rnd = new java.util.Random();
        int count = HEART_TRAIL_MIN + rnd.nextInt(Math.max(1, HEART_TRAIL_MAX - HEART_TRAIL_MIN + 1));

        for (int i = 0; i < count; i++) {
            final int delay = i * 40; // slight stagger
            final float fx = tapX + (rnd.nextFloat() - 0.5f) * dp(overlay.getContext(), 16);
            final float fy = tapY + (rnd.nextFloat() - 0.5f) * dp(overlay.getContext(), 16);
            overlay.postDelayed(() -> spawnHeartTrail(container, fx, fy), delay);
        }
    }

    private void spawnHeartTrail(ViewGroup container, float x, float y) {
        if (container == null) return;
        final Context ctx = container.getContext();
        final TextView tv = new TextView(ctx);
        tv.setText("❤");
        tv.setTextSize(24f);
        tv.setAlpha(0f);
        tv.setTextColor(Color.WHITE);
        int size = dp(ctx, 32);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size);
        lp.leftMargin = (int) x - size / 2;
        lp.topMargin = (int) y - size / 2;

        // Ensure parent can position children; if not FrameLayout, try to add anyway
        try {
            container.addView(tv, lp);
        } catch (Throwable t) {
            try { container.addView(tv); } catch (Throwable ignored) {}
        }

        java.util.Random rnd = new java.util.Random();
        float driftX = (rnd.nextFloat() - 0.5f) * dp(ctx, 48);
        float travelY = -dp(ctx, 80) - rnd.nextInt(dp(ctx, 60));
        long dur = 900 + rnd.nextInt(400);

        tv.setScaleX(0.6f);
        tv.setScaleY(0.6f);
        tv.animate()
                .alpha(1f)
                .translationXBy(driftX)
                .translationYBy(travelY)
                .scaleX(1.15f)
                .scaleY(1.15f)
                .setDuration(dur)
                .withEndAction(() -> {
                    try { container.removeView(tv); } catch (Throwable ignored) {}
                })
                .start();
    }
    // ===== End Heart Burst / Emoji Trail

    // === ReadMore helper adapted from MediaItemFragment.setCollapsibleDescription ===
    private void setCollapsibleDescriptionForHolder(final androidx.recyclerview.widget.RecyclerView.ViewHolder holder,
                                                    final android.widget.TextView tv,
                                                    final CharSequence spannedText,
                                                    final int maxChars) {
        if (tv == null || holder == null) return;
        try { tv.setMovementMethod(android.text.method.LinkMovementMethod.getInstance()); } catch (Throwable ignored) {}
        try { tv.setHighlightColor(0x00000000); } catch (Throwable ignored) {}

        if (spannedText == null) {
            tv.setText("");
            return;
        }

        final int pos = holder.getBindingAdapterPosition();
        if (pos < 0) { tv.setText(spannedText); return; }

        final boolean expanded = captionExpandedByPos.get(pos, false);

        if (spannedText.length() <= maxChars) {
            tv.setText(spannedText, android.widget.TextView.BufferType.SPANNABLE);
            return;
        }

        final CharSequence full = spannedText;
        final String READ_MORE = " Read more";
        final String READ_LESS = " Read less";

        if (expanded) {
            android.text.SpannableStringBuilder expandedSb = new android.text.SpannableStringBuilder(full);
            int start = expandedSb.length();
            expandedSb.append(READ_LESS);
            expandedSb.setSpan(new android.text.style.ClickableSpan() {
                @Override
                public void onClick(android.view.View widget) {
                    captionExpandedByPos.put(pos, false);
                    try { notifyItemChanged(pos, PAYLOAD_CAPTION_TOGGLE); } catch (Throwable ignored) {}
                }
            }, start, start + READ_LESS.length(), android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            expandedSb.setSpan(new android.text.style.ForegroundColorSpan(0xFF2B6BE6),
                    start, start + READ_LESS.length(), android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.setText(expandedSb, android.widget.TextView.BufferType.SPANNABLE);
        } else {
            int end = Math.min(maxChars, full.length());
            String prefix = full.subSequence(0, end).toString();
            if (end < full.length()) {
                int lastSpace = prefix.lastIndexOf(' ');
                if (lastSpace > maxChars - 40) {
                    prefix = prefix.substring(0, lastSpace);
                }
            }
            android.text.SpannableStringBuilder collapsed = new android.text.SpannableStringBuilder(prefix);
            if (end < full.length()) collapsed.append("…");
            int start = collapsed.length();
            collapsed.append(READ_MORE);
            collapsed.setSpan(new android.text.style.ClickableSpan() {
                @Override
                public void onClick(android.view.View widget) {
                    captionExpandedByPos.put(pos, true);
                    try { notifyItemChanged(pos, PAYLOAD_CAPTION_TOGGLE); } catch (Throwable ignored) {}
                }
            }, start, start + READ_MORE.length(), android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            collapsed.setSpan(new android.text.style.ForegroundColorSpan(0xFF2B6BE6),
                    start, start + READ_MORE.length(), android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            tv.setText(collapsed, android.widget.TextView.BufferType.SPANNABLE);
        }
    }
    // === end ReadMore helper ===


    private void bindCaptionOnly(@NonNull ViewHolder holder, int position) {
        try {
            final Item p = items.get(position);
            if (holder.mItemDescription != null) {
                CharSequence spanned = (p.getPost() != null) ? p.getPost().replaceAll("<br>", "\n") : "";
                setCollapsibleDescriptionForHolder(holder, holder.mItemDescription, spanned, 200);
            }
        } catch (Throwable ignored) {}
    }
}
