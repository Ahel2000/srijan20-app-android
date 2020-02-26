package in.srijanju.androidapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import in.srijanju.androidapp.R;
import in.srijanju.androidapp.controller.TeamAdapter;
import in.srijanju.androidapp.model.TeamMember;

public class TeamContent extends Fragment {

  private String type;
  private TeamAdapter adapter;
  private ArrayList<TeamMember> members = new ArrayList<>();
  private ChildEventListener eventListener = new ChildEventListener() {
	@Override
	public void onChildAdded(
			@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
	  TeamMember member = dataSnapshot.getValue(TeamMember.class);
	  members.add(member);
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
  private ValueEventListener showListener = new ValueEventListener() {
	@Override
	public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
	  Boolean _show;
	  _show = dataSnapshot.getValue(Boolean.class);
	  boolean show = false;
	  if (_show != null) show = _show;

	  if (show) {
		ref.removeEventListener(eventListener);
		members.clear();
		adapter.notifyDataSetChanged();
		ref.orderByChild((type != null && type.equals("core")) ? "order" : "event").addChildEventListener(eventListener);
	  } else {
		ref.removeEventListener(eventListener);
		members.clear();
		adapter.notifyDataSetChanged();
	  }
	}

	@Override
	public void onCancelled(@NonNull DatabaseError databaseError) {
	}
  };

  public TeamContent() {
	// Required empty public constructor
  }

  TeamContent(String type) {
	this.type = type;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
						   Bundle savedInstanceState) {
	// Inflate the layout for this fragment
	return inflater.inflate(R.layout.fragment_team_content, container, false);
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

	adapter = new TeamAdapter(activity, members);
	GridView gridView = getView().findViewById(R.id.gv_team);
	gridView.setAdapter(adapter);
	gridView.setEmptyView(view.findViewById(R.id.tv_no_team));

	FirebaseDatabase db = FirebaseDatabase.getInstance();
	ref = db.getReference("srijan/team/" + type);

	DatabaseReference showRef = db.getReference("srijan/team/show");
	showRef.addListenerForSingleValueEvent(showListener);
  }
}
