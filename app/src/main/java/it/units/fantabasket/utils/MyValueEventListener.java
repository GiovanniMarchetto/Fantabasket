package it.units.fantabasket.utils;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import org.jetbrains.annotations.NotNull;

public interface MyValueEventListener extends ValueEventListener {
    String TAG_EVENT_LISTENER = "MIO-ERRORE";

    void onDataChange(@NonNull @NotNull DataSnapshot snapshot);

    @Override
    default void onCancelled(@NonNull @NotNull DatabaseError error) {
        Log.w(TAG_EVENT_LISTENER, error.getMessage() + System.lineSeparator() + error.getDetails());
    }
}
