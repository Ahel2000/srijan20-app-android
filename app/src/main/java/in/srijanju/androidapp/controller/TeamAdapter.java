package in.srijanju.androidapp.controller;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
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
import in.srijanju.androidapp.model.TeamMember;

public class TeamAdapter extends BaseAdapter {

  private Activity context;
  private ArrayList<TeamMember> mems;

  public TeamAdapter(Activity context, ArrayList<TeamMember> list) {
	this.context = context;
	mems = list;
  }

  @Override
  public int getCount() {
	return mems.size();
  }

  @Override
  public Object getItem(int position) {
	return mems.get(position);
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
	  v = inflater.inflate(R.layout.item_team, parent, false);

	  AnimationSet set = new AnimationSet(true);

	  Animation animT = new TranslateAnimation(150, 0f, 0f, 0f);

	  set.addAnimation(animT);
	  set.setDuration(130);

	  v.startAnimation(set);
	} else {
	  v = convertView;
	}

	TextView name = v.findViewById(R.id.tv_team_mem_name);
	TextView dy = v.findViewById(R.id.tv_team_mem_dept);
	TextView post = v.findViewById(R.id.tv_team_mem_post);
	final ImageView ivDp = v.findViewById(R.id.iv_team_mem_pic);

	name.setText(mems.get(position).name);
	dy.setText(mems.get(position).dy);
	if (mems.get(position).post == null) {
	  post.setText(mems.get(position).event);
	  post.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
	  post.setTypeface(post.getTypeface(), Typeface.ITALIC);
	  dy.setVisibility(View.GONE);
	} else {
	  post.setText(mems.get(position).post);
	  post.setTextColor(v.getResources().getColor(android.R.color.black));
	}

	Glide.with(context).asBitmap().load(mems.get(position).dp).into(
			new CustomTarget<Bitmap>() {
			  @Override
			  public void onResourceReady(
					  @NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
				ivDp.setImageBitmap(resource);
			  }

			  @Override
			  public void onLoadCleared(
					  @Nullable Drawable placeholder) {
			  }
			});

	return v;
  }
}
