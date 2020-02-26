package in.srijanju.androidapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import in.srijanju.androidapp.R;

public class Team extends Fragment {

  private final String[] type = {"core", "events"};

  private final int NUM_PAGES = 2;

  private ViewPager2 viewPager;

  private FragmentStateAdapter pagerAdapter;

  public Team() {
	// Required empty public constructor
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
						   Bundle savedInstanceState) {
	// Inflate the layout for this fragment
	return inflater.inflate(R.layout.activity_team, container, false);
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
	  Toast.makeText(activity, "Not logged in", Toast.LENGTH_SHORT).show();
	  FirebaseAuth.getInstance().signOut();
	  AuthUI.getInstance().signOut(activity.getApplicationContext());
	  Intent intent = new Intent(activity, MainActivity.class);
	  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
	  startActivity(intent);
	  return;
	}

	viewPager = view.findViewById(R.id.pagerTeam);
	pagerAdapter = new TeamAdapter(this);
	viewPager.setAdapter(pagerAdapter);

	TabLayout tabsTeam = view.findViewById(R.id.tabTeam);
	new TabLayoutMediator(tabsTeam, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
	  @Override
	  public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
		tab.setText(type[position]);
	  }
	}).attach();
  }

  private class TeamAdapter extends FragmentStateAdapter {

	public TeamAdapter(@NonNull Fragment fragment) {
	  super(fragment);
	}

	@NonNull
	@Override
	public Fragment createFragment(int position) {
	  return new TeamContent(type[position]);
	}

	@Override
	public int getItemCount() {
	  return NUM_PAGES;
	}
  }
}
