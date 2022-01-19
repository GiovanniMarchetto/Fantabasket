package it.units.fantabasket.utils;

import android.text.Editable;
import android.text.TextWatcher;

public interface TextWatcherAfterChange extends TextWatcher {
    @Override
    default void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    default void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    void afterTextChanged(Editable var1);
}
