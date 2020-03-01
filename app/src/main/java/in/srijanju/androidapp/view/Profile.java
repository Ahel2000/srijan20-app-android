package in.srijanju.androidapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.util.Pair;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import in.srijanju.androidapp.R;
import in.srijanju.androidapp.controller.EventRegisteredAdapter;
import in.srijanju.androidapp.model.User;

public class Profile extends Fragment {

  private final ArrayList<Pair<String, Pair<String, String>>> events = new ArrayList<>();
  private DatabaseReference ref;
  private EventRegisteredAdapter adapter;
  private ChildEventListener regEventListener = new ChildEventListener() {
	@Override
	public void onChildAdded(
			@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
	  events.add(Pair.create(dataSnapshot.getKey(), Pair.create(String.valueOf(dataSnapshot.child(
			  "event").getValue()), String.valueOf(dataSnapshot.child("team").getValue()))));
	  adapter.notifyDataSetChanged();
	}

	@Override
	public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
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

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
	return inflater.inflate(R.layout.activity_profile, container, false);
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

	adapter = new EventRegisteredAdapter(activity, events);
	FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
	if (user == null) {
	  Toast.makeText(activity, "Not logged in", Toast.LENGTH_SHORT).show();
	  FirebaseAuth.getInstance().signOut();
	  AuthUI.getInstance().signOut(activity);
	  Intent intent = new Intent(activity, MainActivity.class);
	  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
	  startActivity(intent);
	  return;
	}

	final TextView tvName = view.findViewById(R.id.tv_profile_name);
	final TextView tvEmail = view.findViewById(R.id.tv_profile_email);
	final TextView tvClg = view.findViewById(R.id.tv_clg);
	final TextView tvDegree = view.findViewById(R.id.tv_degree);
	final TextView tvCourse = view.findViewById(R.id.tv_course);
	final TextView tvYear = view.findViewById(R.id.tv_year);

	CardView cvQr = view.findViewById(R.id.cv_qr);
	cvQr.setOnClickListener(new View.OnClickListener() {
	  @Override
	  public void onClick(View v) {
		startActivity(new Intent(activity, CameraScan.class));
	  }
	});

	FirebaseDatabase.getInstance().getReference("srijan/profile/" + user.getUid() +
			"/parentprofile").addListenerForSingleValueEvent(new ValueEventListener() {
	  @Override
	  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
		User user = dataSnapshot.getValue(User.class);
		if (user == null) {
		  Toast.makeText(activity, "Something weird happened! :(", Toast.LENGTH_SHORT).show();
		  return;
		}
		tvName.setText(user.name);
		tvEmail.setText(user.email);
		tvClg.setText(user.college);
		tvDegree.setText(user.degree);
		tvCourse.setText(String.format("Department of %s", user.course));
		tvYear.setText(String.format("%s year", user.year));
	  }

	  @Override
	  public void onCancelled(@NonNull DatabaseError databaseError) {
	  }
	});

	final FirebaseDatabase db = FirebaseDatabase.getInstance();
	ref = db.getReference("srijan/profile/" + user.getUid() + "/events");
	GridView gridView = getView().findViewById(R.id.gv_events);
	gridView.setEmptyView(view.findViewById(R.id.tv_no_reg_event));
	gridView.setAdapter(adapter);
	getView().findViewById(R.id.btn_profile_logout).setOnClickListener(new View.OnClickListener() {
	  @Override
	  public void onClick(View v) {
		FirebaseAuth.getInstance().signOut();
		AuthUI.getInstance().signOut(activity.getApplicationContext());
		Intent intent = new Intent(activity, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	  }
	});
	gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	  @Override
	  public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
		final Pair<String, String> eventDet = events.get(position).second;
		if (eventDet != null) {
		  db.getReference("srijan/events/" + events.get(position).first + "/teams/" + eventDet.second).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
			  if (!dataSnapshot.exists()) return;

			  StringBuilder stringBuilder = new StringBuilder();
			  stringBuilder.append("Team: ").append(dataSnapshot.child("name").getValue(String.class))
					  .append("\n\nLead: ").append(dataSnapshot.child("lead").getValue(String.class))
					  .append("\n\nLead contact: ")
					  .append(dataSnapshot.child("lead-contact").exists() ?
							  dataSnapshot.child("lead-contact").getValue(String.class)
							  : dataSnapshot.child("leadContact").getValue(String.class));

			  if (dataSnapshot.child("mems/1").exists()) {
				stringBuilder.append("\n\nMembers:");
				stringBuilder.append("\n\t- ").append(dataSnapshot.child("mems/1/email").getValue(String.class));
			  }
			  if (dataSnapshot.child("mems/2").exists())
				stringBuilder.append("\n\t- ").append(dataSnapshot.child("mems/2/email").getValue(String.class));
			  if (dataSnapshot.child("mems/3").exists())
				stringBuilder.append("\n\t- ").append(dataSnapshot.child("mems/3/email").getValue(String.class));
			  if (dataSnapshot.child("mems/4").exists())
				stringBuilder.append("\n\t- ").append(dataSnapshot.child("mems/4/email").getValue(String.class));

			  new AlertDialog.Builder(view.getContext())
					  .setIcon(R.drawable.ic_launcher)
					  .setTitle(eventDet.first)
					  .setMessage(stringBuilder.toString())
					  .setPositiveButton("Awesome!", null)
					  .create()
					  .show();
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			}
		  });
		}
	  }
	});
  }

  @Override
  public void onResume() {
	super.onResume();
	ref.addChildEventListener(regEventListener);
  }

  @Override
  public void onPause() {
	super.onPause();
	ref.removeEventListener(regEventListener);
	events.clear();
  }
}
