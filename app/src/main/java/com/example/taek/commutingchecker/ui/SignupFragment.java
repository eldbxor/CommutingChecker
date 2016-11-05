package com.example.taek.commutingchecker.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.taek.commutingchecker.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Awesometic on 2016-07-02.
 */
public class SignupFragment extends Fragment {
    private View rootView;

    private EditText et_employee_number;
    private EditText et_name;
    private EditText et_password;
    private Spinner spn_department;
    private Spinner spn_position;
    private Button btn_send;

    String employee_number;
    String name;
    String password;
    String id_department;
    String id_position;

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
        spn_department = (Spinner) rootView.findViewById(R.id.signup_department);
        spn_position = (Spinner) rootView.findViewById(R.id.signup_position);
        btn_send = (Button) rootView.findViewById(R.id.signup_signupButton);

        et_smartphoneAddr.setText(MainActivity.myMacAddress);
        et_smartphoneAddr.setEnabled(false);

        final HashMap<String, String> departmentItemsHashMap = new HashMap<>();
        final HashMap<String, String> positionItemsHashMap = new HashMap<>();

        try {
            for (int i = 0; i < EntryActivity.departmentListJsonArr.length(); i++) {
                JSONObject row = EntryActivity.departmentListJsonArr.getJSONObject(i);
                departmentItemsHashMap.put(row.getString("id"), row.getString("name"));
            }

            for (int i = 0; i < EntryActivity.positionListJsonArr.length(); i++) {
                JSONObject row = EntryActivity.positionListJsonArr.getJSONObject(i);
                positionItemsHashMap.put(row.getString("id"), row.getString("name"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String[] departmentItems = new String[departmentItemsHashMap.size()];
        String[] positionItems = new String[positionItemsHashMap.size()];

        int departmentItemsIndex = 0;
        for (String key : departmentItemsHashMap.keySet()) {
            departmentItems[departmentItemsIndex] = departmentItemsHashMap.get(key);

            ++departmentItemsIndex;
        }

        int positionItemsIndex = 0;
        for (String key : positionItemsHashMap.keySet()) {
            positionItems[positionItemsIndex] = positionItemsHashMap.get(key);

            ++positionItemsIndex;
        }

        ArrayAdapter<String> departmentSpinnerAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, departmentItems);
        ArrayAdapter<String> positionSpinnerAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, positionItems);

        spn_department.setAdapter(departmentSpinnerAdapter);
        spn_position.setAdapter(positionSpinnerAdapter);

        spn_department.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                for (HashMap.Entry<String, String> entry : departmentItemsHashMap.entrySet()) {
                    if (parent.getSelectedItem().toString().equals(entry.getValue())) {
                        id_department = entry.getKey();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                id_department = "";
            }
        });

        spn_position.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                for (HashMap.Entry<String, String> entry : positionItemsHashMap.entrySet()) {
                    if (parent.getSelectedItem().toString().equals(entry.getValue())) {
                        id_position = entry.getKey();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                id_position = "";
            }
        });

        btn_send.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                employee_number = et_employee_number.getText().toString();
                name = et_name.getText().toString();
                password = et_password.getText().toString();

                if (employee_number.length() == 0 || name.length() == 0 || password.length() == 0
                        || id_department.length() == 0 || id_position.length() == 0) {
                    Toast.makeText(getActivity().getApplicationContext(), "모든 항목을 채워야 합니다", Toast.LENGTH_SHORT).show();

                } else {
                    et_employee_number.setEnabled(false);
                    et_name.setEnabled(false);
                    et_password.setEnabled(false);
                    spn_department.setEnabled(false);
                    spn_position.setEnabled(false);
                    btn_send.setEnabled(false);

                    try {
                        MainActivity.mSocket.signupRequest(MainActivity.myMacAddress, employee_number, name, password, id_department, id_position);

                        int timerCount = 0;
                        while (true) {
                            if (timerCount == 100) {
                                Toast.makeText(getActivity().getApplicationContext(), "서버와 연결할 수 없습니다. 잠시 후 다시 시도하세요.", Toast.LENGTH_LONG).show();

                                MainActivity.mSocket.close();
                                MainActivity.mainActivity.finish();
                            } else if (EntryActivity.isSignupRequestSuccess) {
                                break;
                            } else {
                                Thread.sleep(100);
                                timerCount++;
                            }
                        }

                        MainActivity.mSocket.close();
                        MainActivity.mainActivity.finish();

                        Toast.makeText(getActivity().getApplicationContext(), "가입 신청이 완료되었습니다. 앱을 종료하세요.", Toast.LENGTH_LONG).show();
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
