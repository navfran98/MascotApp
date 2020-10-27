package com.barkindustries.mascotapp.ui.chatWithVets;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ChatWithVetsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public LiveData<String> getText() {
        return mText;
    }

    // used to "set" the "parameter"
    public void select(String item) {
        mText.setValue(item);
    }
}