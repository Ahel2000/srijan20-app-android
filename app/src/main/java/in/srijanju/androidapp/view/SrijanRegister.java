package in.srijanju.androidapp.view;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.Map;

import in.srijanju.androidapp.R;
import in.srijanju.androidapp.model.UserSend;

public class SrijanRegister extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_srijan_register);

	final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
	if (user == null) {
	  Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
	  FirebaseAuth.getInstance().signOut();
	  AuthUI.getInstance().signOut(getApplicationContext());
	  Intent intent = new Intent(SrijanRegister.this, MainActivity.class);
	  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	  startActivity(intent);
	  finish();
	  return;
	}

	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
	  Window window = getWindow();
	  window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
	  window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
	}

	final TextInputEditText etName = findViewById(R.id.et_name);
	// Set user's name
	etName.setText(user.getDisplayName());

	TextInputEditText etEmail = findViewById(R.id.et_email);
	// Set user's email and disallow edit
	etEmail.setText(user.getEmail());
	etEmail.setEnabled(false);
	etEmail.setInputType(InputType.TYPE_NULL);

	final TextInputEditText etClg = findViewById(R.id.et_clg);

	final TextInputEditText etDegree = findViewById(R.id.et_degree);

	final TextInputEditText etCourse = findViewById(R.id.et_course);

	// Set the possible values of year
	final MaterialSpinner spinner = findViewById(R.id.spinner_year);
	spinner.setItems("1st year", "2nd year", "3rd year", "4th year", "5th year");
	spinner.setBackgroundResource(R.drawable.outline_general);
	spinner.getListView()
			.setDivider(new ColorDrawable(Color.parseColor("#33000000")));
	spinner.getListView().setDividerHeight(1);

	// Submit the details
	Button btnRegister = findViewById(R.id.btn_register);
	btnRegister.setOnClickListener(new View.OnClickListener() {
	  @Override
	  public void onClick(View v) {
		String name = String.valueOf(etName.getText());
		String email = user.getEmail();
		String college = String.valueOf(etClg.getText());
		String degree = String.valueOf(etDegree.getText());
		String course = String.valueOf(etCourse.getText());

		String[] suffix = {"st", "nd", "rd", "th", "th"};
		int pos = spinner.getSelectedIndex();
		String year = (pos + 1) + suffix[pos];
		String uid = user.getUid();
		Map<String, String> updatetime = ServerValue.TIMESTAMP;

		UserSend newUserSend = new UserSend(name, email, college, degree, course, year, 1,
				updatetime);
		// Add user to the database
		FirebaseDatabase.getInstance()
				.getReference("srijan/profile/" + uid + "/parentprofile")
				.setValue(newUserSend)
				.addOnSuccessListener(new OnSuccessListener<Void>() {
				  @Override
				  public void onSuccess(Void aVoid) {
					Toast toast = Toast.makeText(getApplicationContext(), "Registered!",
							Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();

					// If added successfully, open app content
					startActivity(new Intent(SrijanRegister.this, MainPage.class));
					finish();
				  }
				})
				.addOnFailureListener(new OnFailureListener() {
				  @Override
				  public void onFailure(@NonNull Exception e) {
					Toast toast = Toast.makeText(getApplicationContext(), "Please enter correct details",
							Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				  }
				});

	  }
	});

	// Logout the user and display the sign in page
	Button btnLogout = findViewById(R.id.btn_logout);
	btnLogout.setOnClickListener(new View.OnClickListener() {
	  @Override
	  public void onClick(View v) {
		FirebaseAuth.getInstance().signOut();
		AuthUI.getInstance().signOut(getApplicationContext());
		startActivity(new Intent(SrijanRegister.this, MainActivity.class));
		finish();
	  }
	});
  }
}