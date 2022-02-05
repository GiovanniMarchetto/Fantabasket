package it.units.fantabasket.ui.access;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import it.units.fantabasket.databinding.FragmentLoginBinding;
import it.units.fantabasket.utils.Utils;
import org.jetbrains.annotations.NotNull;


public class LoginFragment extends Fragment {

    private FragmentLoginBinding loginBinding;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        loginBinding = FragmentLoginBinding.inflate(inflater, container, false);
        return loginBinding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loginBinding.lostPasswordButton.setOnClickListener(viewListener ->
                Utils.sendEmailForResetPassword(view, AccessActivity.email));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        loginBinding = null;
    }

}