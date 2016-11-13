package com.example.taek.commutingchecker.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.taek.commutingchecker.R;
import com.example.taek.commutingchecker.utils.SocketIO;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by Awesometic on 2016-06-09.
 */
public class ChartFragment extends Fragment {

    public static JSONArray chartData = new JSONArray();
    public static boolean chartDataReceived;

    public static ChartFragment newInstance() {
        ChartFragment fragment = new ChartFragment();
        chartDataReceived = false;

        return fragment;
    }

    public ChartFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        switch (getActivity().getTitle().toString()) {
            //nav_movement

            //nav_work
            case "부서별 인원 수":
                SocketIO.ChartSignal signal = SocketIO.ChartSignal.POPULATION;

                return populationOfEachDepartment(inflater, container, savedInstanceState, signal);

            case "개인 출석":
                SocketIO.ChartSignal signal1= SocketIO.ChartSignal.POPULATION;

                return attendenceEachPeople(inflater, container, savedInstanceState, signal1);

            //default
            default:
                return inflater.inflate(R.layout.fragment_chart, container, false);
        }
    }

    private View populationOfEachDepartment(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState, SocketIO.ChartSignal signal) {
        View rootView = inflater.inflate(R.layout.fragment_chart_population_of_each_department, container, false);

        PieChart pieChart = (PieChart) rootView.findViewById(R.id.chart_population_of_each_department);

        try {
            MainActivity.mSocket.getServersRsaPublicKey();
            do {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (MainActivity.mSocket.isServersPublicKeyInitialized() == false);

            MainActivity.mSocket.requestChartData(signal);
            do {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (chartDataReceived == false);
            chartDataReceived = false;

            if (chartData != null) {
                ArrayList<Entry> entries = new ArrayList<>();
                ArrayList<String> labels = new ArrayList<>();

                for (int i = 0; i < chartData.length(); i++) {
                    entries.add(new Entry(chartData.getJSONObject(i).getInt("count"), i));
                    labels.add(chartData.getJSONObject(i).getString("department"));
                }

                PieDataSet dataset = new PieDataSet(entries, "# of people");
                dataset.setColors(ColorTemplate.COLORFUL_COLORS);
                pieChart.animateY(1000);

                PieData data = new PieData(labels, dataset);
                pieChart.setData(data);

                pieChart.setDescription("부서별 인원 수를 보여줍니다. 클릭하면 해당 부서의 상세 정보를 볼 수 있습니다.");

            } else {

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return rootView;
    }

    private View attendenceEachPeople(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState, SocketIO.ChartSignal signal) {
        View rootView = inflater.inflate(R.layout.fragment_chart_attendence_each_people, container, false);

        LineChart lineChart = (LineChart) rootView.findViewById(R.id.chart_attendence_each_people);


        try {
            MainActivity.mSocket.getServersRsaPublicKey();
            do {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (MainActivity.mSocket.isServersPublicKeyInitialized() == false);

            MainActivity.mSocket.requestChartData(signal);
            do {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (chartDataReceived == false);
            chartDataReceived = false;

            if (chartData != null) {
                ArrayList<Entry> entries = new ArrayList<>();
                ArrayList<String> labels = new ArrayList<>();

                for (int i = 0; i < chartData.length(); i++) {
                    entries.add(new Entry(chartData.getJSONObject(i).getInt("count"), i));
                    labels.add(chartData.getJSONObject(i).getString("department"));
                }

                LineDataSet dataset1 = new LineDataSet(entries, "# of people");
                dataset1.setColors(ColorTemplate.VORDIPLOM_COLORS);
                dataset1.setDrawFilled(true);
                dataset1.setDrawCubic(true);
                dataset1.setDrawValues(false);

                LineDataSet dataset2 = new LineDataSet(entries, "# of people2");
                dataset2.setColors(ColorTemplate.MATERIAL_COLORS);
                dataset2.setDrawCubic(false);
                dataset2.setDrawValues(false);

                LineData data1 = new LineData(labels, dataset1);
                LineData data2 = new LineData(labels, dataset2);
                lineChart.setData(data1);
                lineChart.setData(data2);

                lineChart.setDescription("부서별 인원 수를 보여줍니다. 클릭하면 해당 부서의 상세 정보를 볼 수 있습니다.");

            } else {

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return rootView;
    }
}