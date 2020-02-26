package in.srijanju.androidapp.view;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import in.srijanju.androidapp.R;
import in.srijanju.androidapp.SrijanActivity;
import in.srijanju.androidapp.model.User;

public class MainPage extends SrijanActivity implements
		NavigationView.OnNavigationItemSelectedListener {
  public Toolbar toolbar;
  public DrawerLayout drawerLayout;
  public NavController navController;
  public NavigationView navigationView;
  private FirebaseUser user;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main_page);

	user = FirebaseAuth.getInstance().getCurrentUser();
	if (user == null) {
	  Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
	  FirebaseAuth.getInstance().signOut();
	  AuthUI.getInstance().signOut(getApplicationContext());
	  Intent intent = new Intent(MainPage.this, MainActivity.class);
	  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
	  startActivity(intent);
	  finish();
	  return;
	}
	setupNavigation();
  }

  private void setupNavigation() {

	toolbar = findViewById(R.id.toolbar);
	setSupportActionBar(toolbar);

	drawerLayout = findViewById(R.id.drawer_layout);
	drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

	navigationView = findViewById(R.id.navigationView);

	navController = Navigation.findNavController(this, R.id.nav_host_fragment);

	NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout);

	NavigationUI.setupWithNavController(navigationView, navController);

	navigationView.setNavigationItemSelectedListener(this);

	final View headerView = navigationView.getHeaderView(0);
	Glide.with(MainPage.this).asDrawable().load("https://firebasestorage.googleapis.com/v0/b/srijanju20.appspot.com/o/app_nav_back.jpg?alt=media&token=6eeeb98c-41a3-4e9e-b407-3f0b037bc265").into(new CustomTarget<Drawable>() {
	  @Override
	  public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
		headerView.setBackground(resource);
	  }

	  @Override
	  public void onLoadCleared(@Nullable Drawable placeholder) {

	  }
	});
	FirebaseDatabase.getInstance().getReference("srijan/profile/" + user.getUid() +
			"/parentprofile").addListenerForSingleValueEvent(new ValueEventListener() {
	  @Override
	  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
		User cUser = null;
		try {
		  cUser = dataSnapshot.getValue(User.class);
		} catch (Exception ignored) {
		}
		String name = "null", email = "null";
		if (cUser != null) {
		  name = cUser.name;
		  email = cUser.email;
		}
		((TextView) headerView.findViewById(R.id.tv_name)).setText(name);
		((TextView) headerView.findViewById(R.id.tv_email)).setText(email);
	  }

	  @Override
	  public void onCancelled(@NonNull DatabaseError databaseError) {

	  }
	});
  }

  @Override
  public boolean onSupportNavigateUp() {
	return NavigationUI.navigateUp(Navigation.findNavController(this, R.id.nav_host_fragment), drawerLayout);
  }

  @Override
  public void onBackPressed() {
	if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
	  drawerLayout.closeDrawer(GravityCompat.START);
	} else {
	  super.onBackPressed();
	}
  }

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

	menuItem.setChecked(true);

	drawerLayout.closeDrawers();

	int id = menuItem.getItemId();

	switch (id) {

	  case R.id.about:
		navController.navigate(R.id.aboutFrag);
		break;

	  case R.id.events:
		navController.navigate(R.id.eventsFrag);
		break;

	  case R.id.ca:
		navController.navigate(R.id.caFrag);
		break;

	  case R.id.gallery:
		navController.navigate(R.id.galleryFrag);
		break;
	  case R.id.sponsors:
		navController.navigate(R.id.sponsorsFrag);
		break;

	  case R.id.merch:
		navController.navigate(R.id.merchFrag);
		break;

	  case R.id.team:
		navController.navigate(R.id.teamFrag);
		break;
	}
	return true;

  }

}
