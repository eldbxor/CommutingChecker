package com.example.taek.commutingchecker;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

/**
 * Created by Awesometic on 2016-06-09.
 */
public class ChartFragment extends Fragment {

    public static ChartFragment newInstance() {
        ChartFragment fragment = new ChartFragment();
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
            case "부서별 인원 수":
                return populationOfEachDepartment(inflater, container, savedInstanceState);

            default:
                return inflater.inflate(R.layout.fragment_chart, container, false);
        }
    }

    private View populationOfEachDepartment(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chart_population_of_each_department, container, false);

        PieChart pieChart = (PieChart) rootView.findViewById(R.id.chart_population_of_each_department);

        // creating data values
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(4f, 0));
        entries.add(new Entry(8f, 1));
        entries.add(new Entry(6f, 2));
        entries.add(new Entry(12f, 3));
        entries.add(new Entry(18f, 4));
        entries.add(new Entry(9f, 5));

        PieDataSet dataset = new PieDataSet(entries, "# of Calls");
        dataset.setColors(ColorTemplate.COLORFUL_COLORS); // set the color
        pieChart.animateY(2000);

        // creating labels
        ArrayList<String> labels = new ArrayList<>();
        labels.add("January");
        labels.add("February");
        labels.add("March");
        labels.add("April");
        labels.add("May");
        labels.add("June");

        PieData data = new PieData(labels, dataset); // initialize Piedata
        pieChart.setData(data); // set data into chart

        pieChart.setDescription("Description");  // set the description

        return rootView;
    }
}