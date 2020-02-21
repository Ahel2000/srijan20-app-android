package in.srijanju.androidapp.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

import in.srijanju.androidapp.R;

public class MainActivity extends AppCompatActivity {

  // Google sign in request code
  final int RC_SIGN_IN = 1001;

  final String REDIRECT_TO_URL = "redirect_to_url";
  final String REDIRECT_TO_URL_DEF = "none";

  final String DATA_APP_UPDATE = "app_update";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);

	Bundle extras = getIntent().getExtras();
	if (extras != null) {
	  // If a url redirection data is sent
	  String redirectUrl = extras.getString(REDIRECT_TO_URL, REDIRECT_TO_URL_DEF);
	  if (!redirectUrl.equals(REDIRECT_TO_URL_DEF)) {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl)));
		finish();
		return;
	  }

	  // If app update notification is sent
	  String update = extras.getString(DATA_APP_UPDATE, "0");
	  if (update.equals("1")) {
		final String appPackageName = getPackageName();
		try {
		  startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
		} catch (android.content.ActivityNotFoundException anfe) {
		  startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
		}
		finish();
		return;
	  }
	}

	new Thread(new Runnable() {
	  @Override
	  public void run() {
		// Sleep for 100ms and check if user is signed in
		try {
		  Thread.sleep(1000);
		} catch (InterruptedException e) {
		  e.printStackTrace();
		}

		// If user already signed in, start the app
		if (FirebaseAuth.getInstance().getCurrentUser() != null) {
		  startApp();
		  return;
		}

		// Otherwise show the sign in button
		runOnUiThread(new Runnable() {
		  @Override
		  public void run() {
			final Button btnSign = findViewById(R.id.btn_mp_signin);
			btnSign.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
			btnSign.setClickable(true);
			btnSign.setFocusable(true);
			btnSign.setOnClickListener(new View.OnClickListener() {
			  @Override
			  public void onClick(View v) {
				// Choose authentication providers
				List<AuthUI.IdpConfig> providers =
						Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build(),
								new AuthUI.IdpConfig.FacebookBuilder().build(),
								new AuthUI.IdpConfig.EmailBuilder().build());

				// Create and launch sign-in intent
				startActivityForResult(
						AuthUI.getInstance()
								.createSignInIntentBuilder()
								.setAvailableProviders(providers)
								.setIsSmartLockEnabled(false)
								.setLogo(R.drawable.ic_launcher)
								.setTheme(R.style.AppTheme)
								.build(),
						RC_SIGN_IN);
			  }
			});
			btnSign.setVisibility(View.VISIBLE);
		  }
		});
	  }
	}).start();
  }

  private void startApp() {
	FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
	if (user == null) {
	  Toast.makeText(this, "Login error", Toast.LENGTH_SHORT).show();
	  return;
	}

	// Get uuid and check if registration complete
	// If registered, start main page else register
	String uid = user.getUid();
	FirebaseDatabase.getInstance()
			.getReference("srijan/profile/" + uid + "/parentprofile/complete")
			.addListenerForSingleValueEvent(
					new ValueEventListener() {

					  @Override
					  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						// If completed value doesn't exist, register
						if (dataSnapshot.getValue() == null) {
						  startActivity(new Intent(MainActivity.this, SrijanRegister.class));
						  finish();
						  return;
						}

						// Get the value
						long val = 0;
						try {
						  Long ll = dataSnapshot.getValue(Long.class);
						  if (ll != null)
							val = ll;
						} catch (NullPointerException ignored) {
						}

						// If registered, start main page else register
						if (val == 1) {
						  startActivity(new Intent(MainActivity.this, MainPage.class));
						} else {
						  startActivity(new Intent(MainActivity.this, SrijanRegister.class));
						}
						finish();
					  }

					  @Override
					  public void onCancelled(@NonNull DatabaseError databaseError) {
					  }
					}
			);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);

	if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
	  // Successfully signed in
	  startApp();
	}
  }
}