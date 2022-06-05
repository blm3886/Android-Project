package com.example.stocks;

import static android.graphics.Color.RED;
import static com.example.stocks.DetailedStockInformation.*;
import static com.example.stocks.R.color.GREEN;
import static com.example.stocks.R.color.Link;
import static com.example.stocks.R.drawable.tradecustombutton;
import static com.google.android.material.color.MaterialColors.getColor;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

public class TradeDialouge extends AppCompatDialogFragment {
    private EditText Val;
    private TextView TradeTitle;
    private TextView calculations;
    private TextView finalCal;
    String tradeTitle;
    float currentWallet;
    private String ticker;
    private int Quantity;
    private Button Buy;
    private Button Sell;
    private int EditTextNumber;
    HashMap<Integer,Integer> numbers = new HashMap<>();
    private float currentPrice;
    private float totalPurchaseAmt;
    ArrayList<Integer> inputNums;
    Context mcontext;
    View mainView;
   int TotQuant;

    public TradeDialouge(String TradeTitle,float currentWallet,String ticker, int Quantity, float currentPrice,Context mcontext,View v){
        this.tradeTitle = "Trade "+TradeTitle+" shares";
        this.currentWallet = currentWallet;
        this.ticker = ticker;
        this.Quantity = Quantity;
        Log.e(null, "TradeDialouge: "+Quantity);
        this.currentPrice = currentPrice;
        numbers.put(8,1);
        numbers.put(9,2);
        numbers.put(10,3);
        numbers.put(11,4);
        numbers.put(12,5);
        numbers.put(13,6);
        numbers.put(14,7);
        numbers.put(15,8);
        numbers.put(16,9);
        numbers.put(7,0);
        this.mcontext = mcontext;
        this.mainView = v;
    }

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View Diagview = inflater.inflate(R.layout.treadedialouge,null);
        Button buyButton = (Button) Diagview.findViewById(R.id.BUY);
        buyButton.setBackgroundResource(tradecustombutton);
        Button sellButton = (Button) Diagview.findViewById(R.id.SELL);
        sellButton.setBackgroundResource(tradecustombutton);

        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.e(null, "onClick: "+"PRESSED"+getEditTextNumber());
                if(getEditTextNumber() <= 0){
                    Toast.makeText(getContext(), "Cannot Buy non-positive shares", Toast.LENGTH_SHORT).show();
                }
                else if(getTotalPurchaseAmt() > currentWallet){
                    Toast.makeText(getContext(), "Not Enough Money to Buy", Toast.LENGTH_SHORT).show();
                }
                else{
                    int val = getEditTextNumber();
                    Log.e(null, "onClick: "+getEditTextNumber());
                    SharedPreferenceManager.getInstance(getContext()).setQuantity(getTicker(),getEditTextNumber());
                    SharedPreferenceManager.getInstance(getContext()).setToPorfolio(getTicker());
                    SharedPreferenceManager.getInstance(getContext()).setTotalCost(getTotalPurchaseAmt(),getTicker());
                    SharedPreferenceManager.getInstance(getContext()).setSharesOwned(getEditTextNumber(),getTicker());
                    float currWallet = SharedPreferenceManager.getInstance(getContext()).getCurrentWallet();
                    float balWallet = currWallet - getTotalPurchaseAmt();
                    SharedPreferenceManager.getInstance(getContext()).setCurrentWallet(balWallet);
                    //SharedPreferenceManager.getInstance(getContext()).setNetWorth();
                    //Log.e(null, "onClick: "+"SHARES"+Val);
                    getDialog().dismiss();
                    showTradeSucessDiaglog(String.valueOf(getEditTextNumber()),getTicker());
                   // DetailedStockInformation dt = new DetailedStockInformation();
                    //dt.DisplayData();
                }
            }
        });

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getEditTextNumber() <=0){
                    Toast.makeText(getContext(), "Cannot Buy non-positive shares", Toast.LENGTH_SHORT).show();
                }
                else if(getEditTextNumber() > Quantity){
                    Toast.makeText(getContext(), "Not Enough Shares to sell", Toast.LENGTH_SHORT).show();
                }
                else{
                    int Quant = SharedPreferenceManager.getInstance(getContext()).getQuantity(getTicker());
                    int finalVal = Quant - Quantity;
                    Log.e(null, "onClick: SELL QUANTITY"+finalVal);
                    SharedPreferenceManager.getInstance(getContext()).setQuantity(getTicker(),finalVal);
                    float curBal = SharedPreferenceManager.getInstance(getContext()).getCurrentWallet();
                    float finalBal = curBal+getTotalPurchaseAmt();
                    SharedPreferenceManager.getInstance(getContext()).setCurrentWallet(finalBal);
                    SharedPreferenceManager.getInstance(getContext()).setTotalCost(getTotalPurchaseAmt(),getTicker());
                    if(finalVal ==0){
                        SharedPreferenceManager.getInstance(getContext()).removeFromPortfolio(getTicker());
                    }
                    getDialog().dismiss();
                    showSellTradeSucessDiaglog(String.valueOf(getEditTextNumber()),getTicker());
                    //UpdateData();

                }
            }
        });

        TradeTitle = Diagview.findViewById(R.id.TRADE_TITLE_D);
        TradeTitle.setText(tradeTitle);
        calculations = Diagview.findViewById(R.id.CALCULATIONS);
        finalCal = Diagview.findViewById(R.id.BALANCE);
        TextView Bal = (TextView) Diagview.findViewById(R.id.BALANCE);
        Bal.setText("$"+String.format("%.02f", currentWallet)+" to buy"+this.ticker);
        Val = Diagview.findViewById(R.id.editTextNumber);
        builder.setView(Diagview);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setLayout(300,400);

        Val.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                //calculations.setText(keyEvent.getKeyCode());
                  if(numbers.containsKey(i)){
                  int val = numbers.get(i);
                  setEditTextNumber(val);
                  //Val = Diagview.findViewById(R.id.editTextNumber);
                  Val.setText(String.valueOf(val));
                  TextView Calc = (TextView) Diagview.findViewById(R.id.CALCULATIONS);
                  float TotVal = val*getCurrentPrice();
                  setTotalPurchaseAmt(TotVal);
                  String print = String.valueOf(val)+".0*$"+String.format("%.02f", getCurrentPrice())+"/share="+String.format("%.02f", TotVal);
                  Calc.setText(print);}
                  else{
                      Toast.makeText(getContext(),"Please enter a valid amount", Toast.LENGTH_LONG).show();
                  }
                //og.e(null, "onKey: "+i+" "+keyEvent.getKeyCode());
                return true;
            }


        });


        return dialog;
    }

    public void showTradeSucessDiaglog(String quantity, String tickerName){
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.tradesuccessdialoug);
        dialog.getWindow().setBackgroundDrawableResource(tradecustombutton);

        TextView text1 = (TextView) dialog.findViewById(R.id.textView8);
        text1.setText("You have successfully bought "+quantity+"\n"+"shares of "+tickerName);



        Button done = (Button) dialog.findViewById(R.id.buttonCONG);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                //UpdateData();
            }
        });

        dialog.show();
    }

    public void showSellTradeSucessDiaglog(String quantity, String tickerName){
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.tradesuccessdialoug);
        dialog.show();

        TextView text1 = (TextView) dialog.findViewById(R.id.textView8);
        text1.setText("You have successfully sold "+quantity+"\n"+"shares of "+tickerName);

        Button done = (Button) dialog.findViewById(R.id.buttonCONG);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.dismiss();
                UpdateData();

            }
        });
    }

    @SuppressLint("ResourceAsColor")
    public void UpdateData(){
        View v = getMainView();
        int shares = SharedPreferenceManager.getInstance(getContext()).getQuantity(getTicker());
        TextView SHARES = v.findViewById(R.id.SHARES);
        SHARES.setText(shares);

        TextView AVGCOST = v.findViewById(R.id.AVGCOST);
        float totalCost = SharedPreferenceManager.getInstance(getContext()).getTotalCost(getTicker());
        int QuantityVal = SharedPreferenceManager.getInstance(getContext()).getQuantity(getTicker());
        Log.e(null, "onClick: CHANGED QUANTITY"+QuantityVal);
        float avgCost = totalCost/QuantityVal;
        AVGCOST.setText("$"+String.format("%.2f",avgCost));
        TextView TOTALCOST = v.findViewById(R.id.TOTCOST);
        TOTALCOST.setText("$"+String.format("%.02f",totalCost));
        TextView CHANGE = v.findViewById(R.id.CHANGE);
        float change = avgCost*this.currentPrice;
        CHANGE.setText("$"+String.format("%.02f",change));
        if(change > 0){
            CHANGE.setTextColor(GREEN);
        }
        else if(change < 0){
            CHANGE.setTextColor(RED);
        }
    }

    public View getMainView() {
        return this.mainView;
    }

    public void setMainView(View mainView) {
        this.mainView = mainView;
    }

    public void changeData(){
        Context contextDeatils = getMcontext();

    }

    public Context getMcontext() {
        return this.mcontext;
    }

    public void setMcontext(Context mcontext) {
        this.mcontext = mcontext;
    }

    public interface TradeDialougeListerner{
        public void applyText(int value);
    }

    public int getEditTextNumber() {
        return this.EditTextNumber;
    }

    public void setEditTextNumber(int editTextNumber) {
        this.EditTextNumber = editTextNumber;
    }

    public float getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(float currentPrice) {
        this.currentPrice = currentPrice;
    }

    public float getTotalPurchaseAmt() {
        return totalPurchaseAmt;
    }

    public void setTotalPurchaseAmt(float totalPurchaseAmt) {
        this.totalPurchaseAmt = totalPurchaseAmt;
    }


    public int getQuantity() {
        return this.Quantity;
    }

    public void setQuantity(int quantity) {
        Quantity = quantity;
    }


    public String getTicker() {
        return this.ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public int getTotQuant() {
        return TotQuant;
    }

    public void setTotQuant(int totQuant) {
        TotQuant = totQuant;
    }


}
