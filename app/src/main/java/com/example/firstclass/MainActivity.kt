package com.example.firstclass

import androidx.appcompat.app.AppCompatActivity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainActivityViewModel
    private lateinit var beginDateLabel: TextView
    private lateinit var endDateLabel: TextView

    private lateinit var beginDateCal: ImageView
    private lateinit var endDateCal: ImageView

    private lateinit var flyType : Switch

    private lateinit var search : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        beginDateLabel = findViewById<TextView>(R.id.beginDate)
        endDateLabel = findViewById<TextView>(R.id.endDate)

        beginDateCal = findViewById(R.id.beginDateCalendar);
        endDateCal = findViewById(R.id.endDateCalendar);

        flyType = findViewById(R.id.switchBtn)

        search = findViewById(R.id.search)

        beginDateCal.setOnClickListener { showDatePickerDialog(MainActivityViewModel.DateType.BEGIN) }
        endDateCal.setOnClickListener { showDatePickerDialog(MainActivityViewModel.DateType.END) }

        flyType.setOnClickListener {
            // do whatever you need to do when the switch is toggled here
            viewModel.changeFlyType()
        }

        search.setOnClickListener{
            //https://opensky-network.org/api/flights/arrival?airport=LFPO&begin=1572172110&end=1572258510
            var url ="https://opensky-network.org/api/flights/";
            if(viewModel.getFlyType().value == true){ url += "arrival?"; }
            else{ url += "departure?" }

            val beginDateTimesTamp = SimpleDateFormat("dd/MM/yy").parse(beginDateLabel.text.toString()).time
            val endDateTimesTamp = SimpleDateFormat("dd/MM/yy").parse(endDateLabel.text.toString()).time
            val spinner = findViewById<Spinner>(R.id.airportList);
            url+= "airport="+spinner.selectedItem.toString().split(" ")[0] +"&begin="+beginDateTimesTamp.toString().substring(0,11).toBigInteger() +"&end="+endDateTimesTamp.toString().substring(0,11).toBigInteger()
            val res = mapOf<String, String>()
            GlobalScope.launch(Dispatchers.IO) {

                    //url="https://opensky-network.org/api/flights/arrival?airport=LFPO&begin=1572172110&end=1572258510"
                     RequestManager.getSuspended(url,res)
                println(res)

            }
            //Log.d("monUrl",url)


        }

        viewModel.getAirportNamesListLiveData().observe(this) {
            val adapter = ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item, it
            )

            val airportSpinner = findViewById<Spinner>(R.id.airportList)
            airportSpinner.adapter = adapter
        }


        viewModel.getBeginDateLiveData().observe(this) {
            beginDateLabel.text = Utils.dateToString(it.time)
        }

        viewModel.getEndDateLiveData().observe(this) {
            endDateLabel.text = Utils.dateToString(it.time)
        }


    }
    // open date picker dialog
    private fun showDatePickerDialog(dateType: MainActivityViewModel.DateType) {
        // Date Select Listener.
        val dateSetListener =
            OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                viewModel.updateCalendarLiveData(dateType, calendar)
            }

        val currentCalendar = if (dateType == MainActivityViewModel.DateType.BEGIN) viewModel.getBeginDateLiveData().value else viewModel.getEndDateLiveData().value

        val datePickerDialog = DatePickerDialog(
            this,
            dateSetListener,
            currentCalendar!!.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }


}