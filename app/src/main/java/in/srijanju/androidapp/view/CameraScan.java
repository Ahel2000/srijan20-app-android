package in.srijanju.androidapp.view;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.Collections;

import in.srijanju.androidapp.R;
import in.srijanju.androidapp.model.SrijanEvent;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class CameraScan extends AppCompatActivity implements ZXingScannerView.ResultHandler {

  private static final int RC_PERMISSION = 1002;
  private boolean mPermissionGranted;

  private FirebaseUser user = null;

  private ZXingScannerView mScannerView;
  private ProgressBar progressBar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_camera_scan);

	mScannerView = findViewById(R.id.camera);
	progressBar = findViewById(R.id.progress);

	user = FirebaseAuth.getInstance().getCurrentUser();
	if (user == null) {
	  Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
	  FirebaseAuth.getInstance().signOut();
	  AuthUI.getInstance().signOut(getApplicationContext());
	  Intent intent = new Intent(CameraScan.this, MainActivity.class);
	  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
	  startActivity(intent);
	  finish();
	  return;
	}

	mScannerView.setAutoFocus(true);
	mScannerView.setFormats(Collections.singletonList(BarcodeFormat.QR_CODE));
	mScannerView.setAspectTolerance(0.5f);
	mScannerView.setLaserEnabled(true);
	mScannerView.setIsBorderCornerRounded(true);
	mScannerView.setBorderCornerRadius(32);
	mScannerView.setBorderColor(getResources().getColor(R.color.colorAccent));
  }

  @Override
  protected void onResume() {
	super.onResume();
	if (mPermissionGranted) {
	  mScannerView.setResultHandler(this);
	  mScannerView.startCamera();
	} else {
	  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
		if (checkSelfPermission(
				Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
		  mPermissionGranted = false;
		  requestPermissions(new String[]{Manifest.permission.CAMERA}, RC_PERMISSION);
		} else {
		  mPermissionGranted = true;
		  mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
		  mScannerView.startCamera();
		}
	  } else {
		mPermissionGranted = true;
		mScannerView.setResultHandler(this);
		mScannerView.startCamera();
	  }
	}
  }

  @Override
  protected void onPause() {
	super.onPause();
	mScannerView.stopCameraPreview();
	mScannerView.stopCamera();
  }

  @Override
  protected void onStop() {
	super.onStop();
	mScannerView.stopCameraPreview();
	mScannerView.stopCamera();
  }

  @Override
  public void onRequestPermissionsResult(
		  int requestCode, @NonNull String[] permissions,
		  @NonNull int[] grantResults) {
	if (requestCode == RC_PERMISSION) {
	  if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
		mPermissionGranted = true;
		mScannerView.setResultHandler(this);
		mScannerView.startCamera();
	  } else {
		mPermissionGranted = false;
		Toast.makeText(CameraScan.this, "You need to allow camera permission",
				Toast.LENGTH_SHORT).show();
		finish();
	  }
	}
  }

  @Override
  public void handleResult(Result rawResult) {
	processCameraResult(rawResult.getText());
  }

  private void resumeCameraPreview() {
	mScannerView.resumeCameraPreview(this);
  }

  private void processCameraResult(String eventCode) {
	DatabaseReference eventRef = null;
	try {
	  eventRef = FirebaseDatabase.getInstance().getReference("srijan/events/" + eventCode);
	} catch (DatabaseException ignored) {
	}
	if (eventRef == null) {
	  Toast.makeText(CameraScan.this, "Wrong code scanned", Toast.LENGTH_SHORT).show();
	  resumeCameraPreview();
	  return;
	}
	final DatabaseReference finalEventRef = eventRef;
	finalEventRef.addListenerForSingleValueEvent(new ValueEventListener() {
	  @Override
	  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
		if (!dataSnapshot.exists()) {
		  Toast.makeText(CameraScan.this, "Wrong code scanned", Toast.LENGTH_SHORT).show();
		  resumeCameraPreview();
		  return;
		}

		final SrijanEvent event = dataSnapshot.getValue(SrijanEvent.class);
		if (event == null) {
		  Toast.makeText(CameraScan.this, "Didn't work", Toast.LENGTH_SHORT).show();
		  resumeCameraPreview();
		  return;
		}

		// If event doesn't require additional info such as F5
		if (event.reg_type != null && event.reg_type.equals(SrijanEvent.RegType.NO_INFO)) {
		  progressBar.setVisibility(View.VISIBLE);
		  finalEventRef.child("regs/" + user.getUid()).setValue(user.getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
			@Override
			public void onComplete(@NonNull Task<Void> task) {
			  progressBar.setVisibility(View.GONE);
			  if (task.isSuccessful()) {
				AlertDialog.Builder builder = new AlertDialog.Builder(CameraScan.this);

				builder.setMessage("Registered!")
						.setTitle(event.name)
						.setCancelable(false)
						.setIcon(R.drawable.ic_launcher)
						.setPositiveButton("Thanks", new DialogInterface.OnClickListener() {
						  @Override
						  public void onClick(DialogInterface dialog, int which) {
							finish();
						  }
						})
						.create()
						.show();
				return;
			  }

			  Toast.makeText(CameraScan.this, "Some error occurred! Try again.",
					  Toast.LENGTH_LONG).show();
			  finish();
			}
		  });
		  return;
		}

		// If in-app registration is not used
		if (event.reg_link != null && !event.reg_link.equals("") && !event.reg_link.equals("none") && Patterns.WEB_URL.matcher(event.reg_link).matches()) {
		  Intent myIntent = new Intent(CameraScan.this, webview.class);
		  myIntent.putExtra("url", event.reg_link);
		  startActivity(myIntent);
		  finish();
		  return;
		}
		if (event.maxts == 0) {
		  Toast.makeText(CameraScan.this, "Registration not yet started",
				  Toast.LENGTH_SHORT).show();
		  finish();
		  return;
		}

		Intent eventIntent = new Intent(CameraScan.this, EventRegister.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("event", event);
		eventIntent.putExtras(bundle);
		startActivity(eventIntent);
		finish();
	  }

	  @Override
	  public void onCancelled(@NonNull DatabaseError databaseError) {
		Toast.makeText(CameraScan.this, "Didn't work", Toast.LENGTH_SHORT).show();
		resumeCameraPreview();
	  }
	});
  }
}
