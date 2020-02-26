package in.srijanju.androidapp.controller;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.webkit.URLUtil;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;

import in.srijanju.androidapp.R;
import in.srijanju.androidapp.model.Sponsor;

public class SponsorAdapter extends BaseAdapter {
  private Activity context;
  private ArrayList<Sponsor> sponsors;

  public SponsorAdapter(Activity context, ArrayList<Sponsor> list) {
	this.context = context;
	sponsors = list;
  }

  @Override
  public int getCount() {
	return sponsors.size();
  }

  @Override
  public Object getItem(int position) {
	return sponsors.get(position);
  }

  @Override
  public long getItemId(int position) {
	return 0;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
	LayoutInflater inflater = context.getLayoutInflater();
	View v;
	if (convertView == null) {
	  v = inflater.inflate(R.layout.item_sponsor, parent, false);

	  AnimationSet set = new AnimationSet(true);

	  Animation animT = new TranslateAnimation(150, 0f, 0f, 0f);

	  set.addAnimation(animT);
	  set.setDuration(130);

	  v.startAnimation(set);
	} else {
	  v = convertView;
	}
	TextView name = v.findViewById(R.id.tv_sponsor_name);
	TextView type = v.findViewById(R.id.tv_sponsor_type);
	final ImageView ivPoster = v.findViewById(R.id.iv_sponsor_poster);
	TextView link = v.findViewById(R.id.tv_sponsor_link);

	String poster = sponsors.get(position).poster;
	if (poster == null || poster.equals("") || !URLUtil.isHttpsUrl(poster))
	  poster = "https://firebasestorage.googleapis.com/v0/b/srijanju20.appspot.com/o/srijan_poster.jpg?alt=media&token=03a68c2f-8beb-40b0-9353-da58fe7cc932";

	Glide.with(context).asBitmap().load(poster).into(
			new CustomTarget<Bitmap>() {
			  @Override
			  public void onResourceReady(
					  @NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
				ivPoster.setImageBitmap(resource);
			  }

			  @Override
			  public void onLoadCleared(
					  @Nullable Drawable placeholder) {

			  }
			});

	name.setText(sponsors.get(position).name);
	type.setText(sponsors.get(position).type);
	link.setText(sponsors.get(position).url);

	return v;
  }
}
