package com.example.vaccinateasap

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Request.Method.GET
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var searchButton : Button
    lateinit var pinCodeEdt : EditText
    lateinit var centerRV : RecyclerView
    lateinit var loadingPB : ProgressBar
    lateinit var centerList: List<CenterRVModal>
    lateinit var centerRVAdapter: CenterRVAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        searchButton=findViewById(R.id.idBtnSearch)
        pinCodeEdt=findViewById(R.id.idEdtPinCode)
        centerRV=findViewById(R.id.idRVcenters)
        loadingPB=findViewById(R.id.idPBLoading)
        centerList=ArrayList<CenterRVModal>()
        searchButton.setOnClickListener{
            val pinCode=pinCodeEdt.text.toString()
            if(pinCode.length!=6){
                Toast.makeText(this,"please enter a valid pin code",Toast.LENGTH_SHORT).show()
            }else{
                (centerList as ArrayList<CenterRVModal>).clear()
                val c= Calendar.getInstance()
                val year= c.get(Calendar.YEAR)
                val month= c.get(Calendar.MONTH)
                val day=c.get(Calendar.DAY_OF_MONTH)

                val dpd=DatePickerDialog(this,
                    DatePickerDialog.OnDateSetListener{view,year,month,dayOfMonth ->
                        loadingPB.setVisibility(View.VISIBLE)
                        val dateStr: String="""$dayOfMonth-${month+1}-$year"""
                        getAppointmentDetails(pinCode,dateStr)
                    },
                    year,
                    month,
                    day
                )
                dpd.show()


            }
        }

    }

    private fun getAppointmentDetails(pinCode: String, date:String){
        val url= "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByPin?pincode="+pinCode+"&date="+date
        val queue = Volley.newRequestQueue(this)
        val request=JsonObjectRequest(Request.Method.GET,url,null, {
                response ->
             loadingPB.setVisibility(View.GONE)
                try {
                val centerArray=response.getJSONArray("centers")
                if(centerArray.length().equals(0)){
                    Toast.makeText(this,"no vaccination centers available",Toast.LENGTH_SHORT).show()

                }
                for(i in 0 until centerArray.length()){
                    val centerObj= centerArray.getJSONObject(i)
                    val centerName:String = centerObj.getString("name")
                    val centerAddress:String=centerObj.getString("address")
                    val centerFromTime:String=centerObj.getString("from")
                    val centerToTime:String=centerObj.getString("to")
                    val fee_type:String=centerObj.getString("fee_type")
                    val sessionObj= centerObj.getJSONArray("sessions").getJSONObject(0)
                    val availableCapacity: Int =sessionObj.getInt("available_capacity")
                    val ageLimit : Int= sessionObj.getInt("min_age_limit")
                    val vaccineName: String=sessionObj.getString("vaccine")

                    val center=CenterRVModal(
                        centerName,
                        centerAddress,centerFromTime,centerToTime,fee_type,ageLimit,vaccineName,availableCapacity
                    )
                        centerList=centerList+center
                }
                centerRVAdapter = CenterRVAdapter(centerList)
                centerRV.layoutManager=LinearLayoutManager(this)
                centerRV.adapter=centerRVAdapter
            }
            catch (e: JSONException){
                loadingPB.setVisibility(View.GONE)
                e.printStackTrace()
            }
        },
            {error->
                Toast.makeText(this,"failed to get data", Toast.LENGTH_SHORT).show()
        })

        queue.add(request)

    }
}