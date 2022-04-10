/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sopan.exoviewpager.gallery;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;
import com.sopan.exoviewpager.R;
import com.sopan.exoviewpager.model.GalleryModel;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ItemViewerFragment extends Fragment {

    private static final String ARGS_ITEM = "args_item";

    private SimpleExoPlayer simpleExoPlayer;

    private DefaultTrackSelector trackSelector;

    private GalleryModel currentGalleryModel;

    private SimpleExoPlayerView simpleExoPlayerView;

    public static ItemViewerFragment newInstance(GalleryModel item) {
        ItemViewerFragment fragment = new ItemViewerFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARGS_ITEM, item);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_preview_item, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView imageView = view.findViewById(R.id.imgPreview);
        simpleExoPlayerView = view.findViewById(R.id.video_player_view);

        currentGalleryModel = getArguments().getParcelable(ARGS_ITEM);
        if (currentGalleryModel == null) {
            return;
        }

        if (currentGalleryModel.isVideo() || currentGalleryModel.isAudio()) {

            simpleExoPlayerView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);

            initializePlayer();

        } else if (currentGalleryModel.isImage() || currentGalleryModel.isGif()) {
            simpleExoPlayerView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);

            Glide.with(getActivity())
                    .load(currentGalleryModel.getFilePath())
                    .placeholder(android.R.color.black)
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(imageView);

        }

    }

    private void initializePlayer() {
        Log.e("ExoPlayer::", "initializePlayer Called");
        MediaSource mediaSource;

        if (currentGalleryModel.isAudio()) {
            trackSelector = new DefaultTrackSelector();
            simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector);

            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            DataSource.Factory mediaDataSourceFactory = new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), getString(R.string.app_name)), (TransferListener<? super DataSource>) bandwidthMeter);
            DefaultExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

            if (currentGalleryModel.getFilePath() != null) {

                mediaSource = new ExtractorMediaSource(Uri.parse(currentGalleryModel.getFilePath()),
                        mediaDataSourceFactory, extractorsFactory, null, null);
                simpleExoPlayer.setPlayWhenReady(false);
                simpleExoPlayer.prepare(mediaSource);

            }

        } else {

            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

            TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);

            trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

            simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector);

            DefaultExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

            DataSource.Factory mediaDataSourceFactory = new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), getString(R.string.app_name)), (TransferListener<? super DataSource>) bandwidthMeter);

            if (currentGalleryModel.getFilePath() != null) {
                mediaSource = new ExtractorMediaSource(Uri.parse(currentGalleryModel.getFilePath()), mediaDataSourceFactory, extractorsFactory, null, null);
                simpleExoPlayer.setPlayWhenReady(false);
                simpleExoPlayer.prepare(mediaSource);
            }
        }

        if (simpleExoPlayerView != null) simpleExoPlayerView.setPlayer(simpleExoPlayer);

    }

    private void releasePlayer() {
        if (simpleExoPlayer != null) {
            Log.e("ExoPlayer::", "releasePlayer");
            simpleExoPlayer.release();
            simpleExoPlayer = null;
            trackSelector = null;
            simpleExoPlayerView.setPlayer(null);
        } else {
            Log.d("ExoPlayer::", " no need to release");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e("ExoPlayer::", "onStart Called");
        if (simpleExoPlayer == null && currentGalleryModel != null) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("ExoPlayer::", "onResume Called");
        if (simpleExoPlayer == null && currentGalleryModel != null) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("ExoPlayer::", "onPause Called");
        releasePlayer();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e("ExoPlayer::", "onStop Called");
        releasePlayer();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    public void imHiddenNow() {
        releasePlayer();
    }

    public void imVisibleNow() {
        initializePlayer();
    }

    @Override
    public void onDestroyView() {
        Log.e("ExoPlayer::", "onDestroyView Called");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.e("ExoPlayer::", "onDestroy Called");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Log.e("ExoPlayer::", "onDetach Called");
        super.onDetach();
    }


}
