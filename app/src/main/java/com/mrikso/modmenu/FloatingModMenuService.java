package com.mrikso.modmenu;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class FloatingModMenuService extends Service {
    Drawable icon;
    private WindowManager mWindowManager;
    private View mFloatingView;
    private RelativeLayout mCollapsed;
    private LinearLayout patches, mExpandet, settings, mButtonPanel, mTitlle, settingsTitle;
    private ImageView startimage, closeimage, closeimage_title, closeimage_settings, mSettinsMenu, openSite;
    private SeekBar seek_icon, seek_alpha, seek_red, seek_green, seek_blue;
    private WindowManager.LayoutParams params;
    private float curent_icon;
    private SharedPreferences.Editor editor;
    private int mProgress_icon, background_color, alpha, red, green, blue;
    private TextView alpha_text, red_text, green_text, blue_text, current_opacity_icon;
    private Spinner spinner;

    //инициализируем методы из нативной библиотеки
   // private native String toastFromJNI();

    private native void godmode_on();

    private native void godmode_off();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        loadPrefs();
        initFloating();
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            public void run() {
                FloatingModMenuService.this.Thread();
                handler.postDelayed(this, 1000);
            }
        });
    }

    private int dp2px(int dp){
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /* access modifiers changed from: private */
    public void Thread() {
        if (this.mFloatingView == null) {
            return;
        }
        if (Util.isAppBackground()) {
            this.mFloatingView.setVisibility(View.INVISIBLE);
        } else {
            this.mFloatingView.setVisibility(View.VISIBLE);
        }
    }

    //@SuppressLint("ClickableViewAccessibility")
    //инициаизируем разметку нашего мод-меню
    private void initFloating() {

        AssetManager assetManager = getAssets();
        //инициализируем элементы
        FrameLayout rootFrame = new FrameLayout(getBaseContext()); //глобальная разметка
        RelativeLayout mRootContainer = new RelativeLayout(getBaseContext());//разметка на которую будут помещены две разметки иконки и самого меню
        mCollapsed = new RelativeLayout(getBaseContext());//разметка иконки(когда меню свернуто)
        mExpandet = new LinearLayout(getBaseContext());//разметка меню(когда меню развернуто)
        patches = new LinearLayout(getBaseContext());//разметка самих опций(когда меню развернуто)
        mButtonPanel = new LinearLayout(getBaseContext());//разметка кнопок опций(когда меню развернуто)
        mTitlle = new LinearLayout(getBaseContext());//разметка заголовка меню(когда меню развернуто)
        settingsTitle = new LinearLayout(getBaseContext());//разметка заголовка настроек(когда меню развернуто)
        //mCloseMenu = new Button(getBaseContext());//кнопка закрыть меню(когда меню развернуто)
        mSettinsMenu = new ImageView(getBaseContext());//кнопка настроить меню(когда меню развернуто)
        //прописываем разметку
        FrameLayout.LayoutParams flayoutParams = new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        rootFrame.setLayoutParams(flayoutParams);
        mRootContainer.setLayoutParams(new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        //корневая разметка плавающей иконки
        mCollapsed.setLayoutParams(new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        mCollapsed.setVisibility(View.VISIBLE);//mExpandet видимый
        try {
            startimage = new ImageView(getBaseContext());
            startimage.setLayoutParams(new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
            //задаём размер иконки
            int dimension = 60;//60dp
            int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dimension, getResources().getDisplayMetrics());
            startimage.getLayoutParams().height = dimensionInDp;
            startimage.getLayoutParams().width = dimensionInDp;
            startimage.requestLayout();
            startimage.setScaleType(ImageView.ScaleType.FIT_XY);
            InputStream inputStream_hack = assetManager.open("ic_hack_floating.png");
            icon = Drawable.createFromStream(inputStream_hack, null);
            startimage.setImageDrawable(icon);
            startimage.setAlpha(curent_icon);
            //
            closeimage = new ImageView(getBaseContext());
            closeimage.setLayoutParams(new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
            ((ViewGroup.MarginLayoutParams) startimage.getLayoutParams()).topMargin = dp2px(10);
            //задаём размер иконки закрыть
            int dimensionClose = 20;//20dp
            int dimensionInDpClose = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dimensionClose, getResources().getDisplayMetrics());
            closeimage.getLayoutParams().height = dimensionInDpClose;
            closeimage.getLayoutParams().width = dimensionInDpClose;
            closeimage.requestLayout();
            InputStream inputStream_close = assetManager.open("ic_close.png");
            Drawable ic_close = Drawable.createFromStream(inputStream_close, null);
            closeimage.setImageDrawable(ic_close);
            closeimage.setAlpha(curent_icon);
            ((ViewGroup.MarginLayoutParams) closeimage.getLayoutParams()).leftMargin = dp2px(35);
            //
            //иконка закрыть для развернутого меню
            closeimage_title = new ImageView(getBaseContext());
            LinearLayout.LayoutParams pp1 = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            pp1.gravity = Gravity.LEFT;
            pp1.height = dimensionInDpClose;
            pp1.width = dimensionInDpClose;
            closeimage_title.setLayoutParams(pp1);
            closeimage_title.requestLayout();
            closeimage_title.setImageDrawable(ic_close);
            //
            closeimage_settings = new ImageView(getBaseContext());
            LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            pp.gravity = Gravity.LEFT;
            pp.height = dimensionInDpClose;
            pp.width = dimensionInDpClose;
            closeimage_settings.setLayoutParams(pp);
            closeimage_settings.requestLayout();
            closeimage_settings.setImageDrawable(ic_close);
            ((ViewGroup.MarginLayoutParams) closeimage_settings.getLayoutParams()).leftMargin = dp2px(95);
            //
            LinearLayout.LayoutParams set = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            set.gravity = Gravity.RIGHT;
            set.height = dimensionInDpClose;
            set.width = dimensionInDpClose;
            mSettinsMenu.setLayoutParams(set);
            mSettinsMenu.requestLayout();
            InputStream inputStream_settings = assetManager.open("ic_settings.png");
            Drawable ic_settings = Drawable.createFromStream(inputStream_settings, null);
            mSettinsMenu.setImageDrawable(ic_settings);
            //
            LinearLayout.LayoutParams pp3 = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            openSite = new ImageView(getBaseContext());
            pp3.gravity = Gravity.LEFT;
            pp3.height = dimensionInDpClose;
            pp3.width = dimensionInDpClose;
            openSite.setLayoutParams(pp3);
            openSite.requestLayout();
            InputStream inputStream_openSite = assetManager.open("ic_open.png");
            Drawable ic_openSite = Drawable.createFromStream(inputStream_openSite, null);
            openSite.setImageDrawable(ic_openSite);
            ((ViewGroup.MarginLayoutParams) openSite.getLayoutParams()).leftMargin = dp2px(95);
        } catch (IOException ex) {
            Toast.makeText(getBaseContext(), ex.toString(), Toast.LENGTH_LONG).show();
        }
        //прописываем разметку когда меню развернуто
        mExpandet.setVisibility(View.GONE);//mExpandet скрытый
        mExpandet.setBackgroundColor(background_color);
        //mExpandet.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params_mExpandet = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        mExpandet.setLayoutParams(params_mExpandet);
        ScrollView scrollView = new ScrollView(getBaseContext());
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        patches.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, WRAP_CONTENT));
        patches.setOrientation(LinearLayout.VERTICAL);
        mButtonPanel.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        mTitlle.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        TextView title_text = new TextView(getBaseContext());
        title_text.setText("Mod Menu v1.0.1");
        title_text.setTextColor(Color.RED);
        title_text.setTypeface(Typeface.DEFAULT_BOLD);
        title_text.setTextSize(16);
        title_text.setPadding(10, 10, 10, 10);
        LinearLayout.LayoutParams title_Layout = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        title_Layout.gravity = Gravity.CENTER;
        title_text.setLayoutParams(title_Layout);
        mTitlle.addView(title_text);
        mTitlle.addView(closeimage_title);
        settings = new LinearLayout(getBaseContext());
        settings.setVisibility(View.GONE);//mExpandet скрытый
        settings.setBackgroundColor(background_color);
        settings.setOrientation(LinearLayout.VERTICAL);
        settings.setLayoutParams(new LinearLayout.LayoutParams(dp2px(175), WRAP_CONTENT));
        TextView settings_title_text = new TextView(getBaseContext());
        settings_title_text.setText("Settings");
        settings_title_text.setTextColor(Color.RED);
        settings_title_text.setTypeface(Typeface.DEFAULT_BOLD);
        settings_title_text.setTextSize(16);
        //settings_title_text.setPadding(10, 10, 10, 10);
        settingsTitle.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        LinearLayout.LayoutParams settingstitle_Layout = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        settingstitle_Layout.gravity = Gravity.CENTER;
        settings_title_text.setLayoutParams(settingstitle_Layout);
        ScrollView settings_scroll = new ScrollView(getBaseContext());
        settings_scroll.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, dp2px(160)));
        LinearLayout settings_scroll_linear = new LinearLayout(getBaseContext());
        settings_scroll_linear.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        settings_scroll_linear.setOrientation(LinearLayout.VERTICAL);
        TextView opacity_icon = new TextView(getBaseContext());
        opacity_icon.setText("Change opacity icon");
        opacity_icon.setTypeface(Typeface.DEFAULT_BOLD);
        opacity_icon.setTextColor(Color.WHITE);
        opacity_icon.setPadding(10, 10, 10, 10);
        current_opacity_icon = new TextView(getBaseContext());
        current_opacity_icon.setText("Opacity:" + curent_icon);
        current_opacity_icon.setTextColor(Color.WHITE);
        current_opacity_icon.setPadding(10, 10, 10, 10);
        TextView background_menu = new TextView(getBaseContext());
        background_menu.setText("Change color background");
        background_menu.setTypeface(Typeface.DEFAULT_BOLD);
        background_menu.setTextColor(Color.WHITE);
        background_menu.setPadding(10, 10, 10, 10);
        alpha_text = new TextView(getBaseContext());
        alpha_text.setText("Alpha:" + alpha);
        alpha_text.setTextColor(Color.WHITE);
        alpha_text.setPadding(10, 10, 10, 10);
        red_text = new TextView(getBaseContext());
        red_text.setText("Red:" + red);
        red_text.setTextColor(Color.WHITE);
        red_text.setPadding(10, 10, 10, 10);
        green_text = new TextView(getBaseContext());
        green_text.setText("Green:" + green);
        green_text.setTextColor(Color.WHITE);
        green_text.setPadding(10, 10, 10, 10);
        blue_text = new TextView(getBaseContext());
        blue_text.setText("Blue:" + blue);
        blue_text.setTextColor(Color.WHITE);
        blue_text.setPadding(10, 10, 10, 10);
        seek_icon = new SeekBar(getBaseContext());
        seek_icon.setMax(10);
        seek_icon.setProgress(mProgress_icon);
        seek_alpha = new SeekBar(getBaseContext());
        seek_alpha.setMax(255);
        seek_alpha.setProgress(alpha);
        seek_red = new SeekBar(getBaseContext());
        seek_red.setProgress(red);
        seek_red.setMax(255);
        seek_green = new SeekBar(getBaseContext());
        seek_green.setMax(255);
        seek_green.setProgress(green);
        seek_blue = new SeekBar(getBaseContext());
        seek_blue.setProgress(blue);
        seek_blue.setMax(255);
        /*Спиннер на всякий случай*/
/*        String[] items = {"Item 1", "Item 2", "Item 3"};
        spinner = new Spinner(getBaseContext());
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        spinner.setAdapter(arrayAdapter);
        spinner.setBackgroundColor(background_color);*/
        settingsTitle.addView(settings_title_text);
        settingsTitle.addView(closeimage_settings);
        settings.addView(settingsTitle);
        settings.addView(settings_scroll);
        settings_scroll.addView(settings_scroll_linear);
        settings_scroll_linear.addView(opacity_icon);
        settings_scroll_linear.addView(current_opacity_icon);
        settings_scroll_linear.addView(seek_icon);
        settings_scroll_linear.addView(background_menu);
        settings_scroll_linear.addView(alpha_text);
        settings_scroll_linear.addView(seek_alpha);
        settings_scroll_linear.addView(red_text);
        settings_scroll_linear.addView(seek_red);
        settings_scroll_linear.addView(green_text);
        settings_scroll_linear.addView(seek_green);
        settings_scroll_linear.addView(blue_text);
        settings_scroll_linear.addView(seek_blue);
        //settings_scroll_linear.addView(spinner);

        //добавляем элементы в корневую разметку
        rootFrame.addView(mRootContainer);
        //добавляем элементы в mRootContainer
        mRootContainer.addView(mCollapsed);//меню закрыто
        mRootContainer.addView(mExpandet);//меню открыто
        mRootContainer.addView(settings);
        //добавляем элементы в mCollapsed
        mCollapsed.addView(startimage);
        mCollapsed.addView(closeimage);
        //добавляем элементы в mExpandet
        //mExpandet.addView(scrollView);
        //scrollView.addView(patches);
        mExpandet.addView(patches);
        patches.addView(mTitlle);
        //добавляем элементы в mButtonPanel
        mButtonPanel.addView(mSettinsMenu);
        mButtonPanel.addView(openSite);
        mFloatingView = rootFrame;//rootFrame(mFloatingView) корневая разметка всего окна

        //Add the view to the window.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            params = new WindowManager.LayoutParams(
                    WRAP_CONTENT,
                    WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            params = new WindowManager.LayoutParams(
                    WRAP_CONTENT,
                    WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }

        //Specify the view position
        params.gravity = Gravity.TOP | Gravity.START;        //Initially view will be added to top-left corner
        params.x = 0;
        params.y = 100;

        //Add the view to the window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingView, params);

        //The root element of the collapsed view layout
        final View collapsedView = mCollapsed;
        //The root element of the expanded view layout
        final View expandedView = mExpandet;

        mFloatingView.setOnTouchListener(onTouchListener());
        startimage.setOnTouchListener(onTouchListener());
        initMenuButton(collapsedView, expandedView);
        modMenu();
    }

    //обработчик события, когда перетаскивают объект касанием
    private View.OnTouchListener onTouchListener() {
        return new View.OnTouchListener() {
            final View collapsedView = mCollapsed;
            //The root element of the expanded view layout
            final View expandedView = mExpandet;
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        //remember the initial position.
                        initialX = params.x;
                        initialY = params.y;

                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        int Xdiff = (int) (event.getRawX() - initialTouchX);
                        int Ydiff = (int) (event.getRawY() - initialTouchY);

                        //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                        //So that is click event.
                        if (Xdiff < 10 && Ydiff < 10) {
                            if (isViewCollapsed()) {
                                //When user clicks on the image view of the collapsed layout,
                                //visibility of the collapsed layout will be changed to "View.GONE"
                                //and expanded view will become visible.
                                collapsedView.setVisibility(View.GONE);
                                expandedView.setVisibility(View.VISIBLE);
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);

                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(mFloatingView, params);
                        return true;
                }
                return false;
            }
        };
    }

    //иницализируем обработчики событий для кнопок итд..
    private void initMenuButton(final View collapsedView, final View expandedView) {
        startimage.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View view) {
                collapsedView.setVisibility(View.GONE);
                expandedView.setVisibility(View.VISIBLE);
            }
        });
        closeimage.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSelf();
               // Toast.makeText(getBaseContext(), "Close", Toast.LENGTH_LONG).show();
            }
        });
        closeimage_title.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View view) {
                collapsedView.setVisibility(View.VISIBLE);
                expandedView.setVisibility(View.GONE);
                Toast.makeText(getBaseContext(), "Menu Hided", Toast.LENGTH_SHORT).show();
            }
        });
        mSettinsMenu.setOnClickListener(new ImageView.OnClickListener() {
            final View settingsView = settings;

            @Override
            public void onClick(View view) {
                settingsView.setVisibility(View.VISIBLE);
                collapsedView.setVisibility(View.GONE);
                expandedView.setVisibility(View.GONE);
            }
        });
        closeimage_settings.setOnClickListener(new ImageView.OnClickListener() {
            final View settingsView = settings;

            @Override
            public void onClick(View view) {
                settingsView.setVisibility(View.GONE);
                expandedView.setVisibility(View.VISIBLE);
            }
        });
        seek_icon.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //float value = (float)(progress/1000);
                float value = Util.getConvertetValue(progress);
                curent_icon = value;
                startimage.setAlpha(value);
                closeimage.setAlpha(value);
                current_opacity_icon.setText("Opacity:" + curent_icon);
                editor.putFloat("current_background", curent_icon).apply();
                editor.putInt("current_progress_icon", progress).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seek_alpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                changeColor();
                editor.putInt("current_progress_alpha", progress).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seek_red.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                changeColor();
                editor.putInt("current_progress_red", progress).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seek_green.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                changeColor();
                editor.putInt("current_progress_green", progress).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seek_blue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                changeColor();
                editor.putInt("current_progress_blue", progress).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //Open the wesite on this button click
        openSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collapsedView.setVisibility(View.VISIBLE);
                expandedView.setVisibility(View.GONE);
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));//your link to profile, site etc.
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);

                //close the service and remove view from the view hierarchy
                //     stopSelf();
            }
        });
        /*Спиннер на всякий случай*/
       /*spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) view).setTextColor(Color.WHITE);
                switch (position) {
                    case 0:
                        Toast.makeText(getBaseContext(), "Test 1", Toast.LENGTH_LONG).show();
                        break;
                    case 1:
                        Toast.makeText(getBaseContext(), "Test 2", Toast.LENGTH_LONG).show();
                        break;
                    case 2:
                        Toast.makeText(getBaseContext(), "Test 3", Toast.LENGTH_LONG).show();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/

    }

    private void changeColor() {

        alpha = seek_alpha.getProgress();
        red = seek_red.getProgress();
        green = seek_green.getProgress();
        blue = seek_blue.getProgress();
        background_color = Color.argb(alpha, red, green, blue);
        //background_color = ("#"+Integer.toHexString(alpha)+Integer.toHexString(red)+Integer.toHexString(green)+Integer.toHexString(blue));
        alpha_text.setText("Alpha:" + alpha);
        red_text.setText("Red:" + red);
        green_text.setText("Green:" + green);
        blue_text.setText("Blue:" + blue);
        settings.setBackgroundColor(background_color);
        mExpandet.setBackgroundColor(background_color);
        editor.putInt("background_color", background_color).apply();
    }

    //ru: основное меню патчей
    //en: current patch menu
    private void modMenu() {
        addSwitch("God Mode", new SW() {
            public void OnWrite(boolean isChecked) {
                if (isChecked) {
                    godmode_on();
                    //Toast.makeText(getBaseContext(), toastFromJNI(), Toast.LENGTH_LONG).show();
                    Toast.makeText(getBaseContext(), "God Mode is activated", Toast.LENGTH_SHORT).show();
                } else {
                    godmode_off();

                    Toast.makeText(getBaseContext(), "God Mode is disabled", Toast.LENGTH_SHORT).show();
                }
            }
        });
        addSwitch("Unlimited Health", new SW() {
            public void OnWrite(boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(getBaseContext(), "Unlimited Health is activated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getBaseContext(), "Unlimited Health is disabled", Toast.LENGTH_SHORT).show();
                }
            }
        });
        /*patches.addView(spinner);*/
        patches.addView(mButtonPanel);
    }

    //метод для быстрого добавления свичей в меню патчей
    private void addSwitch(String name, final SW listner) {
        Switch sw = new Switch(this);
        sw.setText(name);
        sw.setTextColor(Color.WHITE);
        //sw.setTextSize(dipToPixels());
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                listner.OnWrite(isChecked);
            }
        });
        patches.addView(sw);
    }

    //загрузка и установка настроек по-умолчанию
    private void loadPrefs() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("mod_menu", 0);
        editor = preferences.edit();
        background_color = preferences.getInt("background_color", -14606047);
        curent_icon = preferences.getFloat("current_background", 1);
        alpha = preferences.getInt("current_progress_alpha", 255);
        red = preferences.getInt("current_progress_red", 33);
        green = preferences.getInt("current_progress_green", 33);
        blue = preferences.getInt("current_progress_blue", 33);
        mProgress_icon = preferences.getInt("current_progress_icon", 10);
    }

    private boolean isViewCollapsed() {
        return mFloatingView == null || mCollapsed.getVisibility() == View.VISIBLE;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingView != null){
            mWindowManager.removeView(mFloatingView);
            Toast.makeText(getBaseContext(), "Mod Menu Service is stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private interface SW {
        void OnWrite(boolean z);
    }

}