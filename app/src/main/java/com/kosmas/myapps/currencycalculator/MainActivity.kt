package com.kosmas.myapps.currencycalculator

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.kosmas.myapps.currencycalculator.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL


class MainActivity : AppCompatActivity(), View.OnClickListener{

    private lateinit var binding: ActivityMainBinding

    private lateinit var myTastyToast : Toast //To show hints

    var baseCurrency = "EUR"
    var secondaryCurrency = "EUR"
    private var conversionRate = 0.0

    private var lastNumeric = false //Track if last digit is numeric so we control operators and decimal points
    private var thereIsDecimalPoint = false //Calculator problems can have max 2 decimal points. 1 in the left problem partition and 1 in the right. example: 2.31 + 33.01

    @SuppressLint("ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        myTastyToast = Toast.makeText(this, "message", Toast.LENGTH_SHORT)

        spinnerSetup() //Prepare our spinners based on the api data
        textChangedStuff() //Update currency when currency value is changed (we use only afterTextChanged)

        //Subscribe all calculator buttons to onClickListener
        binding.button7.setOnClickListener(this)
        binding.button8.setOnClickListener(this)
        binding.button9.setOnClickListener(this)
        binding.buttonDivide.setOnClickListener(this)
        binding.button4.setOnClickListener(this)
        binding.button5.setOnClickListener(this)
        binding.button6.setOnClickListener(this)
        binding.buttonMultiply.setOnClickListener(this)
        binding.button1.setOnClickListener(this)
        binding.button2.setOnClickListener(this)
        binding.button3.setOnClickListener(this)
        binding.buttonMinus.setOnClickListener(this)
        binding.buttonDecimal.setOnClickListener(this)
        binding.button0.setOnClickListener(this)
        binding.buttonClear.setOnClickListener(this)
        binding.buttonPlus.setOnClickListener(this)
        binding.buttonEqual.setOnClickListener(this)
    }

    //Depending on button clicked, call different method
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button7 -> onDigit(v)
            R.id.button8 -> onDigit(v)
            R.id.button9 -> onDigit(v)
            R.id.buttonDivide -> onOperator(v)
            R.id.button4 -> onDigit(v)
            R.id.button5 -> onDigit(v)
            R.id.button6 -> onDigit(v)
            R.id.buttonMultiply -> onOperator(v)
            R.id.button1 -> onDigit(v)
            R.id.button2 -> onDigit(v)
            R.id.button3 -> onDigit(v)
            R.id.buttonMinus -> onOperator(v)
            R.id.buttonDecimal -> onDecimal()
            R.id.button0 -> onDigit(v)
            R.id.buttonClear -> onClear()
            R.id.buttonPlus -> onOperator(v)
            R.id.buttonEqual -> onEqual()
        }
    }

    // Runs with each digit button click
    private fun onDigit(view: View?){
        //Get text of button pressed
        binding.tvFirstConversion.append((view as? Button)?.text)
        lastNumeric = true
    }

    //Add operator pressed unless there is already one or there is no numeric digit on the left
    private fun onOperator(view: View?){
        //If last digit is numeric (not operator) and there no other operator
        if(lastNumeric && !isOperatorAdded(binding.tvFirstConversion.text.toString()) ){

            //Add operator to our textView/calculator screen
            binding.tvFirstConversion.append((view as Button).text)
            lastNumeric = false
            thereIsDecimalPoint = false
        }
        else {
            //Show the new toast the right time. Also toasts won't get queued up.
            myTastyToast.setText("Cannot insert operator there.")
            myTastyToast.show()
        }
    }

    private fun isOperatorAdded(value: String) : Boolean{
        //If "-" is the first digit then it means that the number is negative and it is not considered as an operator
        return if(value.startsWith("-")) {
            false
        }
        else //Return true if we find an operator
        {
            value.contains("/") || value.contains("*") || value.contains("+") || value.contains("-")
        }
    }

    //CLR button clears the textViews/screen of calculator
    private fun onClear(){

        binding.tvFirstConversion.text = ""
        binding.tvSecondConversion.text = ""
        lastNumeric = false
        thereIsDecimalPoint = false
    }

    //Can only put "." only if before it was a numeric digit and there is no other "."
    private fun onDecimal(){
        if(lastNumeric && !thereIsDecimalPoint){
            binding.tvFirstConversion.append(".")
            lastNumeric = false
            thereIsDecimalPoint = true
        }
        else {
            myTastyToast.setText("Cannot insert decimal point there.")
            myTastyToast.show()
        }
    }

    //Result of calculations
    private fun onEqual(){
        //Last digit must be numeric in order to calculate
        if(lastNumeric){
            var calcValue = binding.tvFirstConversion.text.toString()
            var prefix = ""

            try {
                //If our problem starts with "-" we need to ignore it before splitting so no errors would appear
                if(calcValue.startsWith("-")){
                    prefix = "-"
                    calcValue = calcValue.substring(1)
                }

                //Find the operator in our problem
                when {
                    //If operator is "-"
                    calcValue.contains("-") -> {
                        //Split the problem in 2 parts/numbers
                        val splitValue = calcValue.split("-")

                        var leftNumber = splitValue[0]
                        val rightNumber = splitValue[1]

                        if(prefix.isNotEmpty()){
                            leftNumber = prefix + leftNumber //Add minus in front
                        }

                        removeZeros((leftNumber.toDouble() - rightNumber.toDouble()).toString())

                    }
                    //If operator is "+"
                    calcValue.contains("+") -> {

                        val splitValue = calcValue.split("+")

                        var leftNumber = splitValue[0]
                        val rightNumber = splitValue[1]

                        if(prefix.isNotEmpty()){
                            leftNumber = prefix + leftNumber
                        }

                        removeZeros((leftNumber.toDouble() + rightNumber.toDouble()).toString())

                    }
                    //If operator is "/"
                    calcValue.contains("/") -> {

                        val splitValue = calcValue.split("/")

                        var leftNumber = splitValue[0]
                        val rightNumber = splitValue[1]

                        if(prefix.isNotEmpty()){
                            leftNumber = prefix + leftNumber
                        }

                        val divisionCheck = (leftNumber.toDouble() / rightNumber.toDouble()).toString()

                        //When dividing by zero the output is "Infinity" or "-Infinity" (when we have a negative number)
                        //So we need to prevent that.
                        if(divisionCheck == "Infinity" || divisionCheck == "-Infinity")
                        {
                            myTastyToast.setText("Cannot divide by zero.")
                            myTastyToast.show()
                        }
                        else if(divisionCheck == "NaN" || divisionCheck == "-NaN") //zero / zero = not a number
                        {
                            myTastyToast.setText("Cannot divide zero by zero.")
                            myTastyToast.show()
                        }
                        else //Result is acceptable
                            removeZeros(divisionCheck)
                    }
                    //If operator is "*"
                    calcValue.contains("*") -> {

                        val splitValue = calcValue.split("*")

                        var leftNumber = splitValue[0]
                        val rightNumber = splitValue[1]

                        if(prefix.isNotEmpty()){
                            leftNumber = prefix + leftNumber
                        }

                        removeZeros((leftNumber.toDouble() * rightNumber.toDouble()).toString())
                    }
                    else -> {
                        myTastyToast.setText("Please create a valid problem.")
                        myTastyToast.show()
                    }
                }
            }catch (e: ArithmeticException){
                Log.e("Main", "$e")
            }
        }else {
            myTastyToast.setText("Please create a valid problem.")
            myTastyToast.show()
        }
    }

    private fun removeZeros(result: String){

        //Get certain amount of decimal digits to combat mathematical errors
        //This way we also lose some precision (as the result is rounded 1-4 = down / 5-9 = up)
        //But this calculator works with max 4 decimal digits
        var resultValue = String.format("%.4f", result.toDouble())

        //Remove zeros from the right side of the result calculation and also the decimal "." operator if it has nothing on it's right
        while (true) {
            //If last digit is zero and it is the only one -> result is 0 and we leave as is
            //If last digit is non-zero and is not "." -> result is a numeric and we leave as is
            if (resultValue[resultValue.length - 1] == '0' && resultValue.length == 1
                || resultValue[resultValue.length - 1] != '0' && resultValue[resultValue.length - 1] != '.'
            ) {
                break
            }
            //Else remove last character (last character is "." or "0" with more digit(s) on the left)
            //However when we remove the decimal point we end the procedure as there is no need to continue.
            //Even if the next digit is 0, it's still an integer and we leave as is.
            else if (resultValue[resultValue.length - 1] == '0') {
                resultValue = resultValue.substring(0, resultValue.length - 1)
            } else if (resultValue[resultValue.length - 1] == '.') {
                resultValue = resultValue.substring(0, resultValue.length - 1)
                break
            }
        }

        binding.tvFirstConversion.text = resultValue

        lastNumeric = true //As in any case, the right most digit of the result will be numeric -> can put any digit next to it on the right

        thereIsDecimalPoint = binding.tvFirstConversion.text.contains(".")
    }


    private fun textChangedStuff() {
        binding.tvFirstConversion.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                try {
                    getApiResult() //Update currency values

                } catch (e: Exception) {
                    Log.e("Main", "$e")
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d("Main", "Before Text Changed")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("Main", "OnTextChanged")
            }
        })
    }

    private fun getApiResult() {
        if (binding.tvFirstConversion.text != null
                && binding.tvFirstConversion.text.isNotBlank()
                && binding.tvFirstConversion.text.isNotEmpty()) {

            //API query that returns the conversion rate depending on our base currency and the currency we want the base to be converted into
            val aPI = "https://freecurrencyapi.net/api/v2/latest?apikey=baeb9110-4085-11ec-b4ea-251138d18eb5&base_currency=$baseCurrency"

            //If base currency has an operator or is negative value, do not convert
            if(binding.tvFirstConversion.text.contains("/") || binding.tvFirstConversion.text.contains("*")
                    || binding.tvFirstConversion.text.contains("+") || binding.tvFirstConversion.text.contains("-")) {

                binding.tvSecondConversion.text = ""

            }//If we have the same currencies, their values of the conversion are the same
            else if(baseCurrency == secondaryCurrency) {

                    binding.tvSecondConversion.text = binding.tvFirstConversion.text
            }
            else {
                //We use IO as we deal with network api requests / data
                GlobalScope.launch(Dispatchers.IO) {

                    try {
                        val apiResult = URL(aPI).readText()
                        val jsonObject = JSONObject(apiResult) //Create JSON object based on api query

                        //Get the conversion rate in the data json object
                        conversionRate = jsonObject.getJSONObject("data")
                                        .getString(secondaryCurrency)
                                        .toDouble()

                        Log.d("Main", "$conversionRate")
                        Log.d("Main", apiResult)

                        //We change thread to Main as we want to change the UI
                        withContext(Dispatchers.Main) {
                            //Get the text (String) of the first currency edit text and convert it to numeric Double
                            //Multiply it with the conversion rate.
                            //We use double as our types so we have the option of acquiring a more detailed value
                            //We also precision format the double value as some conversions can be too big for us to see
                            //Lastly show the converted result in the second textView
                            val conversion = binding.tvFirstConversion.text.toString().toDouble() * conversionRate

                            val precisionConversion = String.format("%.4f", conversion) //x digits beyond the decimal point

                            binding.tvSecondConversion.text = precisionConversion
                        }
                    } catch (e: Exception) {
                        Log.e("Main", "$e")
                    }
                }
            }
        }
    }

    private fun spinnerSetup() {
        val spinner1: Spinner = binding.spFirstConversion
        val spinner2: Spinner = binding.spSecondConversion

        //Create default currencies in case api request fails
        val currenciesArrayList : MutableList<String> = ArrayList()
        currenciesArrayList.add("EUR")
        currenciesArrayList.add("USD")

        val aPI = "https://freecurrencyapi.net/api/v2/latest?apikey=baeb9110-4085-11ec-b4ea-251138d18eb5"

        GlobalScope.launch(Dispatchers.IO) {

            val currenciesData: Iterator<String>

            try {
                val apiResult = URL(aPI).readText()
                val jsonObject = JSONObject(apiResult)

                currenciesData =
                        jsonObject.getJSONObject("data").keys() //Get all currency symbols

                //We change thread to Main for non-network related procedures
                withContext(Dispatchers.Main) {

                    //Save all currencies one by one into our list
                    currenciesArrayList.clear()

                    while (currenciesData.hasNext()){
                        currenciesArrayList.add(currenciesData.next())
                    }
                    currenciesArrayList.add("USD") //As USD currency is the base originally, it is not included in the "data" json object, so we need to insert it manually
                }

                //Log.d("Main", symbolData.next())
                Log.d("Main", apiResult)

            } catch (e: Exception) {
                Log.e("Main", "$e")
            }
        }

        //Use ArrayAdapter to populate our spinner with each currency
        ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                currenciesArrayList)
                .also{
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            //Set the created adapter for each spinner
            spinner1.adapter = it
            spinner2.adapter = it
        }

        spinner1.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
            {
                //Get the value/currency of the item/row selected based on it's position from the parent AdapterView
                baseCurrency = parent?.getItemAtPosition(position).toString()
                getApiResult() //Update currency values
            }
            override fun onNothingSelected(parent: AdapterView<*>?)
            {
                Log.d("Main", "onNothingSelectedSpinner1")
            }
        })

        spinner2.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
            {
                secondaryCurrency = parent?.getItemAtPosition(position).toString()
                getApiResult()
            }
            override fun onNothingSelected(parent: AdapterView<*>?)
            {
                Log.d("Main", "onNothingSelectedSpinner2")
            }
        })
    }

}