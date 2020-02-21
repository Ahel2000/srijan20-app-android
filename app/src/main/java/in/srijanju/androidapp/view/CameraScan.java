package in.srijanju.androidapp.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.budiyev.android.codescanner.ErrorCallback;
import com.budiyev.android.codescanner.ScanMode;
import com.firebase.ui.auth.AuthUI;
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

public class CameraScan extends AppCompatActivity {

  private static final int RC_PERMISSION = 1002;
  private CodeScanner mCodeScanner;
  private boolean mPermissionGranted;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_camera_scan);

	FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
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

	CodeScannerView scannerView = findViewById(R.id.scanner_view);
	mCodeScanner = new CodeScanner(this, scannerView);
	mCodeScanner.setFormats(Collections.singletonList(BarcodeFormat.QR_CODE));
	mCodeScanner.setScanMode(ScanMode.CONTINUOUS);
	mCodeScanner.setDecodeCallback(new DecodeCallback() {
	  @Override
	  public void onDecoded(@NonNull final Result result) {
		runOnUiThread(new Runnable() {
		  @Override
		  public void run() {
			String eventCode = result.getText();
			DatabaseReference eventRef = null;
			try {
			  eventRef = FirebaseDatabase.getInstance().getReference("srijan/events/" + eventCode);
			} catch (DatabaseException ignored) {
			}
			if (eventRef == null) {
			  Toast.makeText(CameraScan.this, "Wrong code scanned", Toast.LENGTH_SHORT).show();
			  return;
			}
			eventRef.addListenerForSingleValueEvent(new ValueEventListener() {
			  @Override
			  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (!dataSnapshot.exists()) {
				  Toast.makeText(CameraScan.this, "Wrong code scanned", Toast.LENGTH_SHORT).show();
				  return;
				}

				SrijanEvent event = dataSnapshot.getValue(SrijanEvent.class);
				if (event == null) {
				  Toast.makeText(CameraScan.this, "Didn't work", Toast.LENGTH_SHORT).show();
				  return;
				}
				if (event.maxts == 0) {
				  Toast.makeText(CameraScan.this, "Registration not yet started",
						  Toast.LENGTH_SHORT).show();
				  return;
				}
				Intent eventIntent = new Intent(CameraScan.this, EventRegister.class);
				Bundle bundle = new Bundle();
				bundle.putSerializable("event", event);
				eventIntent.putExtras(bundle);
				startActivity(eventIntent);
			  }

			  @Override
			  public void onCancelled(@NonNull DatabaseError databaseError) {
				Toast.makeText(CameraScan.this, "Didn't work", Toast.LENGTH_SHORT).show();
			  }
			});
		  }
		});
	  }
	});
	mCodeScanner.setErrorCallback(new ErrorCallback() {
	  @Override
	  public void onError(@NonNull Exception error) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
		  if (checkSelfPermission(
				  Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
			mPermissionGranted = false;
			requestPermissions(new String[]{Manifest.permission.CAMERA}, RC_PERMISSION);
		  } else {
			mPermissionGranted = true;
		  }
		} else {
		  mPermissionGranted = true;
		}
	  }
	});
	scannerView.setOnClickListener(new View.OnClickListener() {
	  @Override
	  public void onClick(View view) {
		mCodeScanner.startPreview();
	  }
	});
  }

  @Override
  protected void onResume() {
	super.onResume();
	if (mPermissionGranted) {
	  mCodeScanner.startPreview();
	} else {
	  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
		if (checkSelfPermission(
				Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
		  mPermissionGranted = false;
		  requestPermissions(new String[]{Manifest.permission.CAMERA}, RC_PERMISSION);
		} else {
		  mPermissionGranted = true;
		  mCodeScanner.startPreview();
		}
	  } else {
		mPermissionGranted = true;
		mCodeScanner.startPreview();
	  }
	}
  }

  @Override
  protected void onPause() {
	mCodeScanner.releaseResources();
	super.onPause();
  }

  @Override
  public void onRequestPermissionsResult(
		  int requestCode, @NonNull String[] permissions,
		  @NonNull int[] grantResults) {
	if (requestCode == RC_PERMISSION) {
	  if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
		mPermissionGranted = true;
		mCodeScanner.startPreview();
	  } else {
		mPermissionGranted = false;
		Toast.makeText(CameraScan.this, "You need to allow camera permission",
				Toast.LENGTH_SHORT).show();
		finish();
	  }
	}
  }
}
