package com.example.taek.commutingchecker;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Awesometic on 2016-07-02.
 */
public class SignupFragment extends Fragment {
    private View rootView;

    private EditText et_employee_number;
    private EditText et_name;
    private EditText et_password;
    private EditText et_department;
    private EditText et_position;
    private Button btn_send;

    public static SignupFragment newInstance() {
        SignupFragment fragment = new SignupFragment();
        return fragment;
    }

    public SignupFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_signup, container, false);

        EditText et_smartphoneAddr = (EditText) rootView.findViewById(R.id.signup_smartphoneAddr);
        et_employee_number = (EditText) rootView.findViewById(R.id.signup_employee_number);
        et_name = (EditText) rootView.findViewById(R.id.signup_name);
        et_password = (EditText) rootView.findViewById(R.id.signup_password);
        et_department = (EditText) rootView.findViewById(R.id.signup_department);
        et_position = (EditText) rootView.findViewById(R.id.signup_position);
        btn_send = (Button) rootView.findViewById(R.id.signup_signupButton);

        et_smartphoneAddr.setText(EntryActivity.smartphoneAddr);
        et_smartphoneAddr.setEnabled(false);

        btn_send.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                String employee_number = et_employee_number.getText().toString();
                String name = et_name.getText().toString();
                String password = et_password.getText().toString();
                String department = et_department.getText().toString();
                String position = et_position.getText().toString();

                if (employee_number.length() == 0 || name.length() == 0 || password.length() == 0
                        || department.length() == 0 || position.length() == 0) {
                    Toast.makeText(getActivity().getApplicationContext(), "모든 항목을 채워야 합니다", Toast.LENGTH_SHORT).show();

                } else {
                    et_employee_number.setEnabled(false);
                    et_name.setEnabled(false);
                    et_password.setEnabled(false);
                    et_department.setEnabled(false);
                    et_position.setEnabled(false);
                    btn_send.setEnabled(false);

                    try {
                        EntryActivity.mSocket.signupRequest(EntryActivity.smartphoneAddr, employee_number, name, password, department, position);
                        Thread.sleep(500);

                        Toast.makeText(getActivity().getApplicationContext(), "가입 신청이 완료되었습니다. 앱을 종료하세요", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
