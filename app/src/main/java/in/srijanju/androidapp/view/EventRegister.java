package in.srijanju.androidapp.view;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import in.srijanju.androidapp.R;
import in.srijanju.androidapp.SrijanActivity;
import in.srijanju.androidapp.model.SrijanEvent;

public class EventRegister extends SrijanActivity {

  final ArrayList<View> viewsAdded = new ArrayList<>();
  FirebaseUser user;
  EditText etLeadContact;
  Button btnRegister;
  DatabaseReference refReg;
  SrijanEvent event;
  Counter c;
  TextInputLayout lTeamName;
  EditText etTeamName;
  ScrollView svRegister;
  ProgressBar progressBar;

  ValueEventListener registeredListener = new ValueEventListener() {
	@Override
	public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
	  if (dataSnapshot.exists()) {
		svRegister.setVisibility(View.INVISIBLE);
		progressBar.setVisibility(View.VISIBLE);
		Toast toast =
				Toast.makeText(EventRegister.this, "Already registered", Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
		new Thread(new Runnable() {
		  @Override
		  public void run() {
			try {
			  Thread.sleep(1000);
			} catch (InterruptedException e) {
			  e.printStackTrace();
			}
			finish();
		  }
		}).start();
	  } else {
		svRegister.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.INVISIBLE);
	  }
	}

	@Override
	public void onCancelled(@NonNull DatabaseError databaseError) {
	}
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_event_register);

	user = FirebaseAuth.getInstance().getCurrentUser();
	if (user == null) {
	  Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
	  FirebaseAuth.getInstance().signOut();
	  AuthUI.getInstance().signOut(getApplicationContext());
	  Intent intent = new Intent(EventRegister.this, MainActivity.class);
	  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
	  startActivity(intent);
	  finish();
	  return;
	}

	// Get event details. If error, exit
	Bundle extras = getIntent().getExtras();
	if (extras == null) {
	  Toast.makeText(this, "Something went wrong! :(", Toast.LENGTH_SHORT).show();
	  finish();
	  return;
	}
	event = (SrijanEvent) extras.getSerializable("event");
	if (event == null || user == null) {
	  Toast.makeText(this, "Something went wrong! :(", Toast.LENGTH_SHORT).show();
	  finish();
	  return;
	}

	// If in-app registration is not used
	if (event.reg_link != null && !event.reg_link.equals("") && !event.reg_link.equals("none") && Patterns.WEB_URL.matcher(event.reg_link).matches()) {
	  Intent myIntent = new Intent(EventRegister.this, webview.class);
	  myIntent.putExtra("url", event.reg_link);
	  startActivity(myIntent);
	  finish();
	  return;
	}

	if (event.maxts == 0) {
	  Toast.makeText(EventRegister.this, "Registration not open",
			  Toast.LENGTH_SHORT).show();
	  finish();
	  return;
	}

	svRegister = findViewById(R.id.sv_register);
	progressBar = findViewById(R.id.progress);
	EditText etEventName = findViewById(R.id.et_event_name);
	etEventName.setText(event.name);
	etEventName.setEnabled(false);

	lTeamName = findViewById(R.id.in_team_name);
	etTeamName = findViewById(R.id.et_team_name);

	// If team name changes, remove the error
	etTeamName.addTextChangedListener(new TextWatcher() {
	  @Override
	  public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	  }

	  @Override
	  public void onTextChanged(CharSequence s, int start, int before, int count) {
		lTeamName.setError("");
	  }

	  @Override
	  public void afterTextChanged(Editable s) {
	  }
	});

	TextView tvTeamSize = findViewById(R.id.tv_size);
	tvTeamSize.setText(String.format(Locale.ENGLISH, "Team size allowed: %d - %d", event.mints, event.maxts));

	final EditText etLeadEmail = findViewById(R.id.et_team_lead);
	etLeadEmail.setText(user.getEmail());
	etLeadEmail.setEnabled(false);

	final EditText etMem1 = findViewById(R.id.et_team_mem1);
	etMem1.setText(user.getEmail());
	etMem1.setEnabled(false);

	etLeadContact = findViewById(R.id.et_team_lead_contact);

	LinearLayout linearLayout = findViewById(R.id.linearlayout);

	// Create "maxts" number of email fields
	for (int i = 2; i <= event.maxts; i++) {
	  @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.temp_layout, null);
	  TextInputLayout userNameIDTextInputLayout = view.findViewById(R.id.userIDTextInputLayout);
	  userNameIDTextInputLayout.setHint("Enter team member " + i + " email");
	  viewsAdded.add(view);
	  linearLayout.addView(view);
	}

	// When "register" is clicked, validate the data and push to the database
	btnRegister = findViewById(R.id.btn_register);
	btnRegister.setOnClickListener(new View.OnClickListener() {
	  @Override
	  public void onClick(View v) {
		// Ask for confirmation before registering

		AlertDialog.Builder builder = new AlertDialog.Builder(EventRegister.this);

		final AlertDialog alertDialog = builder.setMessage("Confirm Registration?")
				.setTitle(event.name)
				.setCancelable(false)
				.setIcon(R.drawable.ic_launcher)
				.setCancelable(true)
				.setPositiveButton("Yes, register!", new DialogInterface.OnClickListener() {
				  @Override
				  public void onClick(DialogInterface dialog, int which) {
					confirmRegistration();
				  }
				})
				.setNegativeButton("No", null)
				.create();
		alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
		  @Override
		  public void onShow(DialogInterface dialog) {
			alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
		  }
		});
		alertDialog.show();
	  }
	});
  }

  private void confirmRegistration() {
	btnRegister.setEnabled(false);

	/*
	 * Verify team name
	 */
	final String teamName = etTeamName.getText().toString();
	if (!teamName.matches("[a-zA-Z0-9]{3,16}")) {
	  lTeamName.setError("Team name should be alphanumeric. Length should be at least 3 and no more than 16.");
	  btnRegister.setEnabled(true);
	  return;
	}

// Stores emails entered
	final ArrayList<String> emails = new ArrayList<>();
	emails.add(user.getEmail());

// Stores unique emails entered, to check for duplicates
	final HashSet<String> uniqueMails = new HashSet<>();
	uniqueMails.add(user.getEmail());

// Check email validity
	final int[] no_of_members = {1};
	for (int i = 0; i < event.maxts - 1; i++) {
	  View x = viewsAdded.get(i);

	  TextInputEditText EMAIL = x.findViewById(R.id.userIDTextInputEditText);
	  String email = String.valueOf(EMAIL.getText());
	  if (!email.equals("") && !email.matches("[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\" +
			  ".[A-Za-z]{2,64}")) {
		Toast.makeText(EventRegister.this, "Enter valid email #" + (i + 2),
				Toast.LENGTH_SHORT).show();
		btnRegister.setEnabled(true);
		return;
	  } else {
		if (!email.equals("")) {
		  ++no_of_members[0];
		  emails.add(email);
		  uniqueMails.add(email);
		}
	  }
	}

	// Ensure no duplicates
	if (emails.size() != uniqueMails.size()) {
	  Toast.makeText(EventRegister.this, "Duplicate user", Toast.LENGTH_SHORT).show();
	  btnRegister.setEnabled(true);
	  return;
	}

	// Check number of emails given
	if (no_of_members[0] < event.mints) {
	  Toast.makeText(EventRegister.this, "Team size should be atleast " + event.mints,
			  Toast.LENGTH_SHORT).show();
	  btnRegister.setEnabled(true);
	  return;
	}

	refReg = FirebaseDatabase.getInstance().getReference("srijan/events/" + event.code +
			"/teams");

	// Check if team name is taken
	refReg.child(teamName).addListenerForSingleValueEvent(new ValueEventListener() {
	  @Override
	  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
		if (dataSnapshot.exists()) {
		  lTeamName.setError("Team name taken.");
		  btnRegister.setEnabled(true);
		  return;
		}

		final int number = no_of_members[0];
		final ArrayList<String> uids = new ArrayList<>();
		c = new Counter(number, uids, emails, teamName);

		// Verify if all the input emails are registered
		// If all are registered, get their uids, create and register the team
		uids.add(user.getUid());
		c.add();
		for (int i = 0; i < number - 1; ++i) {
		  View x = viewsAdded.get(i);

		  TextInputEditText EMAIL = x.findViewById(R.id.userIDTextInputEditText);
		  final String email = EMAIL.getText() != null ? EMAIL.getText().toString() : "";

		  final int ii = i;
		  FirebaseDatabase.getInstance().getReference("srijan/profile").orderByChild(
				  "parentprofile/email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
			  if (!dataSnapshot.exists()) {
				Toast.makeText(EventRegister.this, "#" + (ii + 2) + " is not " +
						"registered for Srijan", Toast.LENGTH_SHORT).show();
				btnRegister.setEnabled(true);
				return;
			  }
			  if (dataSnapshot.getChildren().iterator().next().child("events").child(event.code).exists()) {
				Toast.makeText(EventRegister.this, "#" + (ii + 2) + " is already registered" +
						" for this event", Toast.LENGTH_SHORT).show();
				btnRegister.setEnabled(true);
				return;
			  }

			  String uid = dataSnapshot.getChildren().iterator().next().getKey();
			  uids.add(uid);
			  c.add();
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			  Toast.makeText(EventRegister.this, "Some error occurred! Try again.",
					  Toast.LENGTH_SHORT).show();
			  btnRegister.setEnabled(true);
			}
		  });
		}
	  }

	  @Override
	  public void onCancelled(@NonNull DatabaseError databaseError) {
		btnRegister.setEnabled(true);
	  }
	});
  }

  // If everything is ok, create the team for the user
  private void createTeam(final String teamName, final ArrayList<String> uids, ArrayList<String> emails) {
	Map<String, Object> reg = new HashMap<>();
	reg.put("name", teamName);
	reg.put("lead", emails.get(0));
	reg.put("lead-contact", etLeadContact.getText().toString());
	Map<String, Object> mems = new HashMap<>();
	for (int i = 0; i < uids.size(); ++i) {
	  Map<String, String> temp = new HashMap<>();
	  temp.put("email", emails.get(i));
	  temp.put("uid", uids.get(i));
	  mems.put(String.valueOf(i), temp);
	}
	reg.put("mems", mems);

	// Create team and push to the events
	refReg.child(teamName).setValue(reg).addOnSuccessListener(new OnSuccessListener<Void>() {
	  @Override
	  public void onSuccess(Void aVoid) {
		String refbase = "srijan/profile/%s/events/%s";

		for (int i = 0; i < uids.size(); ++i) {
		  final Map<String, String> userEventReg = new HashMap<>();
		  userEventReg.put("team", teamName);
		  userEventReg.put("event", event.name);
		  FirebaseDatabase.getInstance().getReference(String.format(refbase, uids.get(i),
				  event.code)).setValue(userEventReg);
		}

		successfulRegDone();
	  }
	}).addOnFailureListener(new OnFailureListener() {
	  @Override
	  public void onFailure(@NonNull Exception e) {
		regError();
	  }
	});
  }

  // Show success message
  private void successfulRegDone() {
	Toast.makeText(EventRegister.this, "Registered! :)", Toast.LENGTH_SHORT).show();
	finish();
  }

  private void regError() {
	Toast.makeText(EventRegister.this, "Something went wrong! Report to srijanjdvu.ac@gmail" +
			".com for any queries", Toast.LENGTH_LONG).show();
	btnRegister.setEnabled(true);
  }

  @Override
  protected void onResume() {
	super.onResume();
	FirebaseDatabase.getInstance().getReference("srijan/profile/" + user.getUid() + "/events/" + event.code).addValueEventListener(registeredListener);
  }

  @Override
  protected void onPause() {
	super.onPause();
	FirebaseDatabase.getInstance().getReference("srijan/profile/" + user.getUid() + "/events/" + event.code).removeEventListener(registeredListener);
  }

  private class Counter {
	private int counter = 0;
	private int expCount;
	private ArrayList<String> uids;
	private ArrayList<String> emails;
	private String teamName;

	Counter(int exp, ArrayList<String> uids, ArrayList<String> emails, String tName) {
	  expCount = exp;
	  this.uids = uids;
	  this.emails = emails;
	  teamName = tName;
	}

	private void add() {
	  ++counter;
	  if (counter == expCount) {
		createTeam(teamName, uids, emails);
	  }
	}
  }
}
