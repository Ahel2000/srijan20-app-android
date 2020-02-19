package in.srijanju.androidapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import in.srijanju.androidapp.R;

public class About extends Fragment {

  private static final String srijanAbt = "SRIJAN has always dedicated itself to the idea of " +
		  "promoting, showcasing, and encouraging concepts and research beyond the known periphery. " +
		  "SRIJAN is not just a fest for us. It is a Religion.";


  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
	return inflater.inflate(R.layout.activity_about, container, false);
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


	TextView srijanText = getView().findViewById(R.id.srijanText);
	srijanText.setText(srijanAbt);

	//OnClickListeners
	ImageView img1 = getView().findViewById(R.id.JU_official);
	img1.setOnClickListener(new View.OnClickListener() {
	  public void onClick(View v) {

		Intent myIntent = new Intent(activity, webview.class);
		myIntent.putExtra("url", "http://www.jaduniv.edu.in");
		startActivity(myIntent);
	  }
	});
	ImageView img2 = getView().findViewById(R.id.GS_official);
	img2.setOnClickListener(new View.OnClickListener() {
	  public void onClick(View v) {
		Intent myIntent = new Intent(activity, webview.class);
		myIntent.putExtra("url", "https://www.facebook.com/GamesSocietyJU/");
		startActivity(myIntent);
	  }
	});
	ImageView img3 = getView().findViewById(R.id.CC_official);
	img3.setOnClickListener(new View.OnClickListener() {
	  public void onClick(View v) {
		Intent myIntent = new Intent(activity, webview.class);
		myIntent.putExtra("url", "https://www.facebook.com/JUCodeClub/");
		startActivity(myIntent);
	  }
	});
	ImageView img4 = getView().findViewById(R.id.SC_official);
	img4.setOnClickListener(new View.OnClickListener() {
	  public void onClick(View v) {
		Intent myIntent = new Intent(activity, webview.class);
		myIntent.putExtra("url", "https://www.facebook.com/juscofficial/");
		startActivity(myIntent);
	  }
	});
  }
}
