package com.minami_m.project.android.wakemeapp.SearchFriend;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.transition.Fade;
import android.support.transition.TransitionSet;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.minami_m.project.android.wakemeapp.ActivityChangeListener;
import com.minami_m.project.android.wakemeapp.FirebaseRealtimeDatabaseHelper;
import com.minami_m.project.android.wakemeapp.FragmentChangeListener;
import com.minami_m.project.android.wakemeapp.InputHandler;
import com.minami_m.project.android.wakemeapp.MainActivity;
import com.minami_m.project.android.wakemeapp.R;
import com.minami_m.project.android.wakemeapp.Model.User;
import com.minami_m.project.android.wakemeapp.RealtimeDatabaseCallback;
import com.minami_m.project.android.wakemeapp.SignIn.SignInActivity;
import com.minami_m.project.android.wakemeapp.inputValidationHandler;
import com.squareup.picasso.Picasso;

public class SearchFriendActivity extends AppCompatActivity
        implements FragmentChangeListener, inputValidationHandler,
        SearchFriendFragmentListener, ActivityChangeListener {
    private Button search_btn;
    private EditText editEmail;
    private static final String TAG = "SearchFriendActivity";
    private FirebaseUser currentUser;
    private String friendId;
    private User friend;
    private User mUser;
    private static final String DIALOG_TAG = "dialog";

    public void setEditEmail(EditText editEmail) {
        this.editEmail = editEmail;
    }

    // TODO: Implement BACK or CANCEL to input search field again.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_search_friend);
        search_btn = findViewById(R.id.search_button);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseRealtimeDatabaseHelper.readUserData(currentUser.getUid(), new RealtimeDatabaseCallback() {
                @Override
                public User getUser(User user) {
                    mUser = user;
                    return user;
                }
            });
        } else {
            launchActivity(SignInActivity.class);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (search_btn.getText().equals(getResources().getString(R.string.search_email))) {
                    Log.i(TAG, "onClick: 12345 no email field?");
                    if (editEmail != null) {
                        Log.i(TAG, "onClick: 12345 success!");
                        if (isValidInput()) {
                            Log.i(TAG, "onClick: " + editEmail.getText().toString());
                            searchFriendByEmail(editEmail.getText().toString());
                        }
                    }
                } else if (search_btn.getText()
                        .equals(getResources().getString(R.string.add_as_friend))) {
                    followNewFriend(mUser, friend);
                } else {
                    // TODO: fix
                    Log.i(TAG, "onClick: 123456 Something wrong...");
                }

            }
        });
    }

    public void searchFriendByEmail(final String email) {
        if (email.equals(currentUser.getEmail())) {
            Toast.makeText(this, "Search friend's ID", Toast.LENGTH_SHORT).show();
            return;
        }
        ValueEventListener searchListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    if (userSnapshot.child("email").getValue().equals(email)) {
                        Log.i(TAG, "onDataChange: " + userSnapshot.getKey());
                        friendId = userSnapshot.getKey();
                        friend = userSnapshot.getValue(User.class);
                        search_btn.setText(R.string.add_as_friend);
                        replaceFragment(SearchFriendResultFragment.newInstance());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i(TAG, "onCancelled: " + databaseError.getMessage());
            }
        };
        FirebaseRealtimeDatabaseHelper.USERS_REF.addListenerForSingleValueEvent(searchListener);
        if (friend == null) {
            Log.i(TAG, "onDataChange: 123456789 no User?");
            Toast.makeText(this, R.string.email_not_match, Toast.LENGTH_SHORT).show();
        }
    }

    // TODO
    public void followNewFriend(final User mUser, final User friend) {
        FirebaseRealtimeDatabaseHelper.FRIEND_ID_LIST_REF
                .child(mUser.getId())
                .child(friend.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Toast.makeText(getApplicationContext(),
                                    "Already following the currentUser",
                                    Toast.LENGTH_SHORT).show();
                            replaceFragment(SearchFriendFragment.newInstance());
                            search_btn.setText(R.string.search_email);

                        } else {
                            FirebaseRealtimeDatabaseHelper.followFriend(mUser.getId(), friendId);
                            FirebaseRealtimeDatabaseHelper.createChatRoom(mUser, friend);
                            SuccessfullyAddedDialog.newInstance()
                                    .show(getSupportFragmentManager(), DIALOG_TAG);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    @Override
    public void replaceFragment(Fragment newFragment) {
        TransitionSet set = new TransitionSet();
        set.addTransition(new Fade());
//        Slide slide = new Slide();
//        slide.setSlideEdge(Gravity.BOTTOM);
//        set.addTransition(slide);
        newFragment.setEnterTransition(set);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.search_friend_container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    @Override
    public boolean isValidInput() {
        return InputHandler.isValidFormEmail(editEmail);
    }


    @Override
    public void showFriend(ImageView iconHolder, TextView nameHolder) {
        if (friend == null) return;
        if (friend.getIcon() != null) {
            Log.i(TAG, "showFriend: 123456" + friend.getIcon());
            Picasso.get().load(friend.getIcon()).into(iconHolder);
            nameHolder.setText(friend.getName());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        search_btn.setText(R.string.search_email);
        friend = null;
    }


    @Override
    public void launchActivity(Class nextActivity) {
        Intent intent = new Intent(this, nextActivity);
        startActivity(intent);
    }
}
