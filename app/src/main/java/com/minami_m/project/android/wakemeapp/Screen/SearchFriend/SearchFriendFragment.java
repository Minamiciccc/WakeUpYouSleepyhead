package com.minami_m.project.android.wakemeapp.Screen.SearchFriend;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.minami_m.project.android.wakemeapp.Common.Handler.InputHandler;
import com.minami_m.project.android.wakemeapp.Common.Handler.InputValidationHandler;
import com.minami_m.project.android.wakemeapp.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFriendFragment extends Fragment implements InputValidationHandler {
    private static final String TAG = "SearchFriendFragment";
    EditText editText;


    public SearchFriendFragment() {
        // Required empty public constructor
    }

    public static SearchFriendFragment newInstance() {
        return new SearchFriendFragment();
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        SearchFriendFragmentListener listener = (SearchFriendFragmentListener)getActivity();
//        listener.resetSearchButton();
//        editText.setText("");
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_friend, container, false);
        editText = view.findViewById(R.id.search_by_email_text_field);
        SearchFriendActivity activity = (SearchFriendActivity) getActivity();
        activity.setEditEmail(editText);
        Log.i(TAG, "onCreateView: 12345 view?");
        return view;
    }

    @Override
    public boolean isValidInput() {
        return InputHandler.isValidFormEmail(editText);
    }

}
