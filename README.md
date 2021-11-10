# AndroidStudio_Currency_Calculator
## Description:
This is a simple calculator app with the ability to convert currency.

The calculator can be referred to as a 4-function calculator. 
It’s able to do simple arithmetic, which includes addition, subtraction, multiplication, and division.
Also it can CLEAR the whole input and insert a decimal point.

The user cannot input a negative number, however the result of a problem can be negative with which the user can create another problem.
As long as the value of the base currency is negative or in form of a problem, there won't be a converted currency value.

The max length of the textViews is 15 so the problem must be max 15 long for the calculator to work.
The result of the currency conversion will have max 4 digits beyond the decimal point, so any numbers beyond that will be
rounded up or down depending on their value(1 to 4 rounds down and 5 to 9 rounds up).

## Technologies:
The live API used for the currency conversion is "free currencyapi" https://freecurrencyapi.net/.
In order to request the currency data, Kotlin's coroutines were used, with dependencies:
1) implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8'
2) implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.5'

We also need to connect to the internet to get the data, so we use the permission:
uses-permission android:name="android.permission.INTERNET"

Finally, we use ViewBinding so we can interact easily with the views:
buildFeatures {
    viewBinding true
}


## Pictures:
![image](https://user-images.githubusercontent.com/34765932/141085602-baa77448-eecd-45b6-ae75-55a1e327ef7f.png)

![image](https://user-images.githubusercontent.com/34765932/141085729-a3f63878-4fc4-4971-bf3b-8fd2a485a3ff.png)


## Some more info:
- This project was created in Android Studio/Kotlin.
- There are plenty of comments inside the codes files to help with the understanding of the code.
