package in.srijanju.androidapp.controller;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.util.Pair;

import java.util.ArrayList;

import in.srijanju.androidapp.R;

public class EventRegisteredAdapter extends BaseAdapter {

  private Activity context;
  private ArrayList<Pair<String, String>> events;

  public EventRegisteredAdapter(Activity context, ArrayList<Pair<String, String>> list) {
	this.context = context;
	events = list;
  }

  @Override
  public int getCount() {
	return events.size();
  }

  @Override
  public Object getItem(int position) {
	return events.get(position);
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
	  v = inflater.inflate(R.layout.item_event_reg, parent, false);

	  AnimationSet set = new AnimationSet(true);

	  Animation animT = new TranslateAnimation(position % 2 == 0 ? -100 : 100, 0f, 0f, 0f);

	  set.addAnimation(animT);
	  set.setDuration(250);

	  v.startAnimation(set);
	} else {
	  v = convertView;
	}
	TextView eventName = v.findViewById(R.id.tv_event_name);
	TextView teamName = v.findViewById(R.id.tv_team);

	eventName.setText(events.get(position).first);
	teamName.setText(String.format("Team: %s", events.get(position).second));

	return v;
  }
}
