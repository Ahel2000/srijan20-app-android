package in.srijanju.androidapp.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import in.srijanju.androidapp.R;
import in.srijanju.androidapp.controller.EventAdapter;
import in.srijanju.androidapp.model.SrijanEvent;

public class Events extends Fragment {

  private final ArrayList<SrijanEvent> events = new ArrayList<>();

  private EventAdapter adapter;

  private ChildEventListener eventListener = new ChildEventListener() {
	@Override
	public void onChildAdded(
			@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
	  SrijanEvent e = dataSnapshot.getValue(SrijanEvent.class);
	  events.add(e);
	  adapter.notifyDataSetChanged();
	}

	@Override
	public void onChildChanged(
			@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

	}

	@Override
	public void onChildRemoved(
			@NonNull DataSnapshot dataSnapshot) {

	}

	@Override
	public void onChildMoved(
			@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

	}

	@Override
	public void onCancelled(
			@NonNull DatabaseError databaseError) {

	}
  };
  private DatabaseReference ref;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
	return inflater.inflate(R.layout.activity_events, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
	super.onActivityCreated(savedInstanceState);

	final FragmentActivity activity = getActivity();
	View view = getView();
	if (activity == null || view == null) {
	  Toast.makeText(getContext(), "Some error occurred", Toast.LENGTH_SHORT).show();
	  return;
	}

	FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
	if (user == null) {
	  Toast.makeText(getActivity(), "Not logged in", Toast.LENGTH_SHORT).show();
	  FirebaseAuth.getInstance().signOut();
	  AuthUI.getInstance().signOut(activity.getApplicationContext());
	  Intent intent = new Intent(getActivity(), MainActivity.class);
	  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
	  startActivity(intent);
	  return;
	}

	adapter = new EventAdapter(activity, events);
	GridView gridView = getView().findViewById(R.id.gv_events);

	// Open the event description page to show the details of the event
	gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	  @Override
	  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(getActivity(), EventDescription.class);
		// Put the event object of the event that was clicked
		Bundle bundle = new Bundle();
		bundle.putSerializable("event", events.get(position));
		intent.putExtras(bundle);
		startActivity(intent);
	  }
	});
	gridView.setAdapter(adapter);

	getView().findViewById(R.id.tv_schedule).setOnClickListener(new View.OnClickListener() {
	  @Override
	  public void onClick(View v) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/open?id=1rfMozwquwSvcP7b7OT-jeczepu-zura5"));
		startActivity(browserIntent);
	  }
	});

	getView().findViewById(R.id.tv_mementos).setOnClickListener(new View.OnClickListener() {
	  @Override
	  public void onClick(View v) {
		Intent myIntent = new Intent(activity, webview.class);
		myIntent.putExtra("url", "https://docs.google.com/forms/d/e/1FAIpQLSdb1ZhrcbsFBIoh3zOxTPax-U6_lJPpjPfkBo4j3Z3ybHfQsQ/viewform");
		startActivity(myIntent);
	  }
	});

	getView().findViewById(R.id.tv_workshop).setOnClickListener(new View.OnClickListener() {
	  @Override
	  public void onClick(View v) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.srijanju.in/app/workshops"));
		startActivity(browserIntent);
	  }
	});

	FirebaseDatabase db = FirebaseDatabase.getInstance();
	ref = db.getReference("srijan/events");
  }

  @Override
  public void onResume() {
	super.onResume();

	// Get the list of events
	ref.orderByChild("type").addChildEventListener(eventListener);
  }

  @Override
  public void onPause() {
	super.onPause();
	ref.removeEventListener(eventListener);
	events.clear();
  }
}