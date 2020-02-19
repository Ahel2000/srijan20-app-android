package in.srijanju.androidapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class Ambassador extends Fragment {

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
	return inflater.inflate(R.layout.activity_ca, container, false);
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

	TextView caText = getView().findViewById(R.id.CA_text);
	String textVal = "For the past 11 years, you’ve stormed into theatres celebrating the " +
			"phenomenon" +
			" that was Avengers: looked upto them dreamy-eyed as they wielded their superpower, wished " +
			"almost half-heartedly,\n\n" +
			"”What if I was the one?”\n\n" +
			"As Kolkata’s biggest techno-management carnival slowly picks up steam, Team Srijan presents" +
			" " +
			"before you, an idea called the Campus Avengers Initiative.\nThe idea is to bring " +
			"together a " +
			"group of remarkable people: honest, responsible and steadfast; and see if they can become " +
			"something more.\n\n" +
			"This is your chance to be THE ONE. Why be the pedestrian when you can own the " +
			"spotlight?\n\n" +
			"We are accepting applications for Campus Avengers.";
	caText.setText(textVal);
	Button apply = getView().findViewById(R.id.CA_button);
	apply.setOnClickListener(
			new View.OnClickListener() {
			  @Override
			  public void onClick(View v) {
				Intent myIntent = new Intent(activity, webview.class);
				myIntent.putExtra("url",
						"https://docs.google" +
								".com/forms/d/e/1FAIpQLSewELaYYdVa33eY2F1z_487plvltoFAu3VF2KiiWHg41sT5Zg/viewform");
				startActivity(myIntent);
			  }
			}
	);
  }
}