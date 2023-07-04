package com.example.myapplication


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import kotlin.math.pow


class MainActivity : AppCompatActivity() {
    private var modorder: Int = 0
    private var scalingfact: Double = 0.0
    private var numerology: Int = 0
    private var overhead: Array<Double> = arrayOf(0.0, 0.0)
    private val modOptions = arrayOf(1, 2, 3, 4, 5, 6, 7, 8)
    private val scfOptions = arrayOf(1.0, 0.8, 0.75, 0.4)
    private val numOptions = arrayOf(0, 1, 2, 3)
    private val ohdOptions = arrayOf("[0.14, 0.08]",
        "[0.18, 0.10]"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val numCarr = findViewById<EditText>(R.id.num_carriers)
        val mimo = findViewById<EditText>(R.id.MIMO_NUM)
        val nPrb=findViewById<EditText>(R.id.prbCount)
        val cr=findViewById<EditText>(R.id.crate)
        val modSpinner = findViewById<Spinner>(R.id.mod_ord)
        val scfSpinner = findViewById<Spinner>(R.id.sf)
        val numSpinner = findViewById<Spinner>(R.id.numer)
        val ohdSpinner = findViewById<Spinner>(R.id.overhead)
        val calcButton = findViewById<Button>(R.id.CALC)
        val tputText = findViewById<TextView>(R.id.TPUT)

        val modAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, modOptions)
        modSpinner.adapter = modAdapter

        val scfAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, scfOptions)
        scfSpinner.adapter = scfAdapter

        val numAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, numOptions)
        numSpinner.adapter = numAdapter

        val ohdAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ohdOptions)
        ohdSpinner.adapter = ohdAdapter

        calcButton.setOnClickListener {
            val carrierNum = numCarr.text.toString().toIntOrNull() ?: 0
            val mimoNum = mimo.text.toString().toIntOrNull() ?: 0
            val prbNum=nPrb.text.toString().toIntOrNull() ?: 0
            val cRate=cr.text.toString().toFloatOrNull()?: 0

            val (dlThroughput, ulThroughput) = calculateThroughput(
                carrierNum,
                modorder,
                mimoNum,
                scalingfact,
                numerology,
                prbNum,
                cRate as Float,
                overhead
            )

            val dlTh =String.format("%.3f", dlThroughput)
            val ulTh = String.format("%.3f", ulThroughput)
            tputText.text = "Down Link Datarate = $dlTh Gbps \n Uplink Datarate = $ulTh Gbps"
        }

        modSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                modorder = modOptions[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                modorder = 0
            }
        }

        scfSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                scalingfact = scfOptions[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                scalingfact = 0.0
            }
        }

        numSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                numerology = numOptions[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                numerology = -1
            }
        }
        ohdSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                overhead = parseArrayStringToDoubleArray(ohdOptions[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                overhead = arrayOf(0.0, 0.0)
            }
        }



    }


    }

    private fun calculateThroughput(
        carrierNum: Int,
        mod_order: Int,
        mimoNum: Int,
        scaling_fact: Double,
        numerology: Int,
        prb_number: Int,
        cdRate: Float,
        elements: Array<Double>
    ): Pair<Double, Double> {
        val nrNumerology = numerology
        val numbers = elements
        val dlOh = numbers[0]
        val ulOh = numbers[1]
        val rMax = cdRate
        val ts = 0.001 / (14 * (2.0.pow(nrNumerology.toDouble())))
        val nPrb =prb_number
        var dlThroughput = 0.0
        var ulThroughput = 0.0
        if (scaling_fact != 0.0) {
            for (j in 1..carrierNum) {
                dlThroughput += ((mod_order) * mimoNum * scaling_fact * rMax * ((nPrb * 12) / ts) * (1 - dlOh))
                ulThroughput += ((mod_order) * mimoNum * scaling_fact * rMax * ((nPrb * 12) / ts) * (1 - ulOh))
            }
        }
        dlThroughput *= 10.0.pow(-9) // Throughput in Gbps
        ulThroughput *= 10.0.pow(-9) // Throughput in Gbps

        return Pair(dlThroughput, ulThroughput)
    }
    private fun parseArrayStringToDoubleArray(arrayString: String): Array<Double> {
        val trimmedString = arrayString.trim('[', ']') // Remove the square brackets at the beginning and end

        val elements = trimmedString.split(", ") // Split the string by commas and spaces
        val doubleArray = Array(elements.size) { index ->
            elements[index].toDouble() // Convert each element to a Double
        }

        return doubleArray
    }

