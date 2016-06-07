package com.xaircraft.xmap.component;

import com.xaircraft.xmap.R;

import android.R.color;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * 自定义ImageButton，模拟ImageButton，并在其下方显示文字 
 * @author Administrator
 *
 */
@SuppressLint("ResourceAsColor") public class MainImageViewControl extends LinearLayout {
	  
	    private ImageView mImgView = null;  
	    private TextView mTextView = null;  
	    private Context mContext;  
	    private int mImgResourceId = 0;
	      
	    public MainImageViewControl(Context context, AttributeSet attrs) {  
	        super(context, attrs);  
	        // TODO Auto-generated constructor stub  
	        LayoutInflater.from(context).inflate(R.layout.contorl_imgbutton, this, true);  
	        mContext = context;  
	        mImgView = (ImageView)findViewById(R.id.img);  
	        mTextView = (TextView)findViewById(R.id.text);
	        mTextView.setGravity(Gravity.CENTER);
	        mTextView.setTextColor(getResources().getColor(color.background_dark));
	        mImgView.setSelected(true);
	        android.view.ViewGroup.LayoutParams para;
	        para = mImgView.getLayoutParams();

	        mImgView.setLayoutParams(para);       
	    }  
	  
	   /*设置图片接口*/  
	    public void setImageResource(int resId){  
	    	mImgResourceId = resId;
	        mImgView.setImageResource(resId);  
	    }  
	      
	    /*设置文字接口*/  
	    public void setText(String str){  
	        mTextView.setText(str);  
	    }  
	    
	    /*设置文字接口*/  
	    public void setTextColor(int color){  
	        mTextView.setTextColor(color); 
	    }  
	    
	    /*设置文字大小*/  
	    public void setTextSize(float size){  
	        mTextView.setTextSize(size);  
	    }  
	    
	    public int getResourseImageId(){
	    	return mImgResourceId;
	    }
	  
//	     /*设置触摸接口*/  
	    public void setOnTouch(OnTouchListener listen){  
	        mImgView.setOnTouchListener(listen);  
	        //mTextView.setOnTouchListener(listen);  
	    }  
	}  