package com.example.licenta_aplicatiestatiemeteo_moticicavladflorin

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.licenta_aplicatiestatiemeteo_moticicavladflorin.databinding.ActivityHomeBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class RoundedImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {

    private val path = Path()
    private var cornerRadius = 250f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        path.reset()
        path.addRoundRect(
            0f, 0f, w.toFloat(), h.toFloat(),
            floatArrayOf(0f, 0f, 0f, 0f, cornerRadius, cornerRadius, cornerRadius, cornerRadius),
            Path.Direction.CW
        )
        path.close()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.clipPath(path)
        super.onDraw(canvas)
        canvas.restore()
    }
}

class Home : AppCompatActivity()
{
    private lateinit var database: DatabaseReference
    private lateinit var binding: ActivityHomeBinding

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    private lateinit var lineChart: LineChart
    private lateinit var chartContainer: FrameLayout
    private val dataSets = mutableListOf<LineDataSet>()
    private var xAxisValue = 0f
    private val mHandler = Handler(Looper.getMainLooper())
    private var isChartVisible = false
    val dataSet1 = createDataSet("Temp", Color.RED)
    val dataSet2 = createDataSet("Umi", Color.MAGENTA)
    val dataSet3 = createDataSet("Gaz", Color.BLACK)
    val dataSet4 = createDataSet("Par", Color.CYAN)
    val dataSet5 = createDataSet("Vân", Color.GREEN)
    val dataSet6 = createDataSet("Plo", Color.BLUE)
    val dataSet7 = createDataSet("Lum", Color.YELLOW)
    val dataSet8 = createDataSet("mmHG", Color.GRAY)
    val dataSet9 = createDataSet("Pasc", Color.LTGRAY)

    private var temperatureGlobal: Float = 0.0f
    private var humidityGlobal: Int = 0
    private var gasGlobal: Int = 0
    private var particlesGlobal: Double = 0.0
    private var lightGlobal: Int = 0
    private var mmhgGlobal: Int = 0
    private var paGlobal: Int = 0
    private var windGlobal: Double = 0.0
    private var rainGlobal: Int = 0

    @RequiresApi(Build.VERSION_CODES.O)
    val currentTimeGlobal: LocalTime = LocalTime.now()

    @RequiresApi(Build.VERSION_CODES.O)
    private var noapte = LocalTime.of(0,0, 0)
    @RequiresApi(Build.VERSION_CODES.O)
    private var rasaritStart = LocalTime.of(5,30, 0)
    @RequiresApi(Build.VERSION_CODES.O)
    private var rasaritEnd = LocalTime.of(7,0, 0)
    @RequiresApi(Build.VERSION_CODES.O)
    private var apusStart = LocalTime.of(20, 0, 0)
    @RequiresApi(Build.VERSION_CODES.O)
    private var apusEnd = LocalTime.of(22, 0, 0)


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        supportActionBar?.hide()
        binding.buttonRead.setOnClickListener{ readData() }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lineChart = findViewById(R.id.lineChart)
        chartContainer = findViewById(R.id.chartContainer)
        val showGraphButton: Button = findViewById(R.id.buttonHistory)
        showGraphButton.setOnClickListener {
            toggleChartVisibility()
        }

        createNotificationChannel(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkConditionsAndSendNotification(this)
        }

        fun setWhiteOutline()
        {
            binding.textViewTemp.setTextColor(Color.parseColor("#ffffff"))
            binding.textViewHum.setTextColor(Color.parseColor("#ffffff"))
            binding.textViewTemperaturaUmiditate.setTextColor(Color.parseColor("#ffffff"))
            binding.textViewGas.setTextColor(Color.parseColor("#ffffff"))
            binding.textViewParticle.setTextColor(Color.parseColor("#ffffff"))
            binding.textViewCalitateParticule.setTextColor(Color.parseColor("#ffffff"))
            binding.textViewSpeed.setTextColor(Color.parseColor("#ffffff"))
            binding.textViewDirection.setTextColor(Color.parseColor("#ffffff"))
            binding.textViewVitezaDirectie.setTextColor(Color.parseColor("#ffffff"))
            binding.textViewRain.setTextColor(Color.parseColor("#ffffff"))
            binding.textViewPrecipitation.setTextColor(Color.parseColor("#ffffff"))
            binding.textViewPloaiePrecipitatii.setTextColor(Color.parseColor("#ffffff"))
            binding.textViewLight.setTextColor(Color.parseColor("#ffffff"))
            binding.textViewUV.setTextColor(Color.parseColor("#ffffff"))
            binding.textViewLuminaUV.setTextColor(Color.parseColor("#ffffff"))
            binding.textViewMMHG.setTextColor(Color.parseColor("#ffffff"))
            binding.textViewPA.setTextColor(Color.parseColor("#ffffff"))
            binding.textViewPresiuni.setTextColor(Color.parseColor("#ffffff"))

            binding.temperature.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_ATOP)
            binding.humidity.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_ATOP)
            binding.airquality.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_ATOP)
            binding.particle.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_ATOP)
            binding.wind.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_ATOP)
            binding.direction.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_ATOP)
            binding.rain.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_ATOP)
            binding.precipitation.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_ATOP)
            binding.light.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_ATOP)
            binding.uv.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_ATOP)
            binding.pressuremmHG.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_ATOP)
            binding.pressurePa.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_ATOP)
        }

        fun setBlackOutline()
        {
            binding.textViewTemp.setTextColor(Color.parseColor("#000000"))
            binding.textViewHum.setTextColor(Color.parseColor("#000000"))
            binding.textViewTemperaturaUmiditate.setTextColor(Color.parseColor("#000000"))
            binding.textViewGas.setTextColor(Color.parseColor("#000000"))
            binding.textViewParticle.setTextColor(Color.parseColor("#000000"))
            binding.textViewCalitateParticule.setTextColor(Color.parseColor("#000000"))
            binding.textViewSpeed.setTextColor(Color.parseColor("#000000"))
            binding.textViewDirection.setTextColor(Color.parseColor("#000000"))
            binding.textViewVitezaDirectie.setTextColor(Color.parseColor("#000000"))
            binding.textViewRain.setTextColor(Color.parseColor("#000000"))
            binding.textViewPrecipitation.setTextColor(Color.parseColor("#000000"))
            binding.textViewPloaiePrecipitatii.setTextColor(Color.parseColor("#000000"))
            binding.textViewLight.setTextColor(Color.parseColor("#000000"))
            binding.textViewUV.setTextColor(Color.parseColor("#000000"))
            binding.textViewLuminaUV.setTextColor(Color.parseColor("#000000"))
            binding.textViewMMHG.setTextColor(Color.parseColor("#000000"))
            binding.textViewPA.setTextColor(Color.parseColor("#000000"))
            binding.textViewPresiuni.setTextColor(Color.parseColor("#000000"))

            binding.temperature.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_ATOP)
            binding.humidity.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_ATOP)
            binding.airquality.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_ATOP)
            binding.particle.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_ATOP)
            binding.wind.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_ATOP)
            binding.direction.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_ATOP)
            binding.rain.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_ATOP)
            binding.precipitation.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_ATOP)
            binding.light.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_ATOP)
            binding.uv.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_ATOP)
            binding.pressuremmHG.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_ATOP)
            binding.pressurePa.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_ATOP)
        }

        fun setBgColor(color: Int)
        {
            binding.linearLayout1.setBackgroundColor(color)
            binding.linearLayout2.setBackgroundColor(color)
            binding.linearLayout3.setBackgroundColor(color)
            binding.linearLayout4.setBackgroundColor(color)
            binding.linearLayout5.setBackgroundColor(color)
            binding.linearLayout6.setBackgroundColor(color)
        }

        val defaultStartColorDay = Color.parseColor("#0254f5")
        val defaultCenterColorDay = Color.parseColor("#74a0f7")
        val defaultEndColorDay = Color.parseColor("#a8c4e0")
        val defaultGradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(defaultStartColorDay, defaultCenterColorDay, defaultEndColorDay))
        binding.backView.background = defaultGradientDrawable

        val defaultDayHexColor = "#ffffff"
        val defaultDayColor = Color.parseColor(defaultDayHexColor)
        setBgColor(defaultDayColor)
        setBlackOutline()

        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable
        {
            override fun run()
            {
                binding.buttonRead.performClick()

                //BACKGROUND
                if(currentTimeGlobal >= noapte && currentTimeGlobal < rasaritStart)
                {
                    //noapte -> rasarit start
                    val startColorNight = Color.parseColor("#01021c")
                    val endColorNight = Color.parseColor("#16195e")
                    val gradientDrawable = GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(startColorNight, endColorNight))
                    binding.backView.background = gradientDrawable

                    val nightHexColor = "#232791"
                    val nightColor = Color.parseColor(nightHexColor)
                    setBgColor(nightColor)
                    setWhiteOutline()
                }
                else if(currentTimeGlobal >= rasaritStart && currentTimeGlobal < rasaritEnd)
                {
                    //rasarit start -> rasarit end
                    val startColorDawn = Color.parseColor("#0b166e")
                    val centerColorDawn = Color.parseColor("#6b81f2")
                    val endColorDawn = Color.parseColor("#f7b145")
                    val gradientDrawable = GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(startColorDawn, centerColorDawn, endColorDawn))
                    binding.backView.background = gradientDrawable

                    val dawnHexColor = "#ffffff"
                    val dawnColor = Color.parseColor(dawnHexColor)
                    setBgColor(dawnColor)
                    setBlackOutline()
                }
                else if(currentTimeGlobal >= rasaritEnd && currentTimeGlobal < apusStart)
                {
                    //ziua    AKA    rasarit end -> apus start
                    val startColorDay = Color.parseColor("#0254f5")
                    val centerColorDay = Color.parseColor("#74a0f7")
                    val endColorDay = Color.parseColor("#a8c4e0")
                    val gradientDrawable = GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(startColorDay, centerColorDay, endColorDay))
                    binding.backView.background = gradientDrawable

                    val dayHexColor = "#ffffff"
                    val dayColor = Color.parseColor(dayHexColor)
                    setBgColor(dayColor)
                    setBlackOutline()
                }
                else if(currentTimeGlobal >= apusStart && currentTimeGlobal < apusEnd)
                {
                    //apus start -> apus end
                    val startColorDusk = Color.parseColor("#010112")
                    val centerColorDusk = Color.parseColor("#423eb0")
                    val endColorDusk = Color.parseColor("#e06e51")
                    val gradientDrawable = GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(startColorDusk, centerColorDusk, endColorDusk))
                    binding.backView.background = gradientDrawable

                    val duskHexColor = "#232791"
                    val duskColor = Color.parseColor(duskHexColor)
                    setBgColor(duskColor)
                    setWhiteOutline()
                }
                else
                {
                    val startColorNight = Color.parseColor("#01021c")
                    val endColorNight = Color.parseColor("#16195e")
                    val gradientDrawable = GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(startColorNight, endColorNight))
                    binding.backView.background = gradientDrawable

                    val nightHexColor = "#232791"
                    val nightColor = Color.parseColor(nightHexColor)
                    setBgColor(nightColor)
                    setWhiteOutline()
                }

                handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(runnable, 5000)
        Toast.makeText(this, "Se citesc datele...", Toast.LENGTH_LONG).show()
    }

    companion object {
        const val REQUEST_CODE_NOTIFICATION_PERMISSION = 1
    }

    private fun setupChart() {
        lineChart.apply {
            description = Description().apply {
                text = ""
            }
            setBackgroundColor(Color.WHITE)
            setDrawGridBackground(false)
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)

            dataSets.add(dataSet1)
            dataSets.add(dataSet2)
            dataSets.add(dataSet3)
            dataSets.add(dataSet4)
            dataSets.add(dataSet5)
            dataSets.add(dataSet6)
            dataSets.add(dataSet7)
            dataSets.add(dataSet8)
            dataSets.add(dataSet9)

            val xAxis = xAxis
            xAxis.position = XAxis.XAxisPosition.TOP
            xAxis.granularity = 5f
            xAxis.setLabelCount(10, true)
            xAxis.textColor = Color.BLACK
            xAxis.textSize = 10f

            val yAxis = axisLeft
            yAxis.axisMinimum = 0f
            yAxis.axisMaximum = 100f
            yAxis.granularity = 5f
            yAxis.setLabelCount(10, true)
            yAxis.textColor = Color.BLACK
            yAxis.textSize = 10f

            axisLeft.axisMinimum = -30f
            axisLeft.axisMaximum = 1000f

            data = LineData(dataSets as List<ILineDataSet>?)
        }
    }

    private fun createDataSet(label: String, color: Int): LineDataSet {
        val dataSet = LineDataSet(null, label).apply {
            this.color = color
            setCircleColor(color)
            lineWidth = 5f
            circleRadius = 4f
            setDrawCircleHole(false)
            valueTextSize = 10f
        }
        return dataSet
    }

    private fun startDataSimulation() {
        mHandler.postDelayed(object : Runnable {
            override fun run() {
                addEntriesToCharts()
                mHandler.postDelayed(this, 1000)
            }
        }, 1000)
    }

    private fun addEntriesToCharts() {
        val value1 = temperatureGlobal
        val value2 = humidityGlobal
        val value3 = gasGlobal
        val value4 = particlesGlobal
        val value5 = windGlobal
        val value6 = rainGlobal
        val value7 = lightGlobal
        val value8 = mmhgGlobal
        val value9 = paGlobal

        xAxisValue += 1f

        dataSet1.addEntry(Entry(xAxisValue, value1))
        dataSet2.addEntry(Entry(xAxisValue, value2.toFloat()))
        dataSet3.addEntry(Entry(xAxisValue, value3.toFloat()))
        dataSet4.addEntry(Entry(xAxisValue, value4.toFloat()))
        dataSet5.addEntry(Entry(xAxisValue, value5.toFloat()))
        dataSet6.addEntry(Entry(xAxisValue, value6.toFloat()))
        dataSet7.addEntry(Entry(xAxisValue, value7.toFloat()))
        dataSet8.addEntry(Entry(xAxisValue, value8.toFloat()))
        dataSet9.addEntry(Entry(xAxisValue, value9.toFloat()))

        lineChart.data.notifyDataChanged()
        lineChart.notifyDataSetChanged()

        lineChart.setVisibleXRangeMaximum(10f)
        lineChart.moveViewToX(xAxisValue)
    }

    private fun toggleChartVisibility() {
        if (!isChartVisible) {
            chartContainer.visibility = FrameLayout.VISIBLE
            lineChart.visibility = FrameLayout.VISIBLE
            startDataSimulation()
            isChartVisible = true
        } else {
            chartContainer.visibility = FrameLayout.GONE
            lineChart.visibility = FrameLayout.GONE
            mHandler.removeCallbacksAndMessages(null)
            isChartVisible = false
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notifications"
            val descriptionText = "App Notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun sendNotification(context: Context, title: String, text: String, notificationId: Int) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, "CHANNEL_ID")
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_NOTIFICATION_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                checkConditionsAndSendNotification(this)
            }
        }
    }

    private fun checkConditionsAndSendNotification(context: Context) {
        val condition = false
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun readData()
    {
        //TIME
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yy / HH:mm:ss")
        val formattedDateTime = LocalDateTime.now().format(formatter)
        binding.textViewDateTime.text = formattedDateTime


        //TEMPERATURE
        database = FirebaseDatabase.getInstance().getReference("Sensor")
        database.child("Temperature").get().addOnSuccessListener {
            if(it.exists())
            {
                val temp:Float = it.value.toString().toFloat()
                temperatureGlobal = temp
                val celsius = " ℃"
                val tempset:String = temp.toString() + celsius
                binding.textViewTemp.text = tempset
            }
        }.addOnFailureListener {
            Toast.makeText(this, "FAILED TEMP", Toast.LENGTH_SHORT).show()
        }


        //HUMIDITY
        database = FirebaseDatabase.getInstance().getReference("Sensor")
        database.child("Humidity").get().addOnSuccessListener {
            if(it.exists())
            {
                val hum:Int = it.value.toString().toInt()
                humidityGlobal = hum
                val percent = " %"
                val humset:String = hum.toString() + percent
                binding.textViewHum.text = humset
            }
        }.addOnFailureListener {
            Toast.makeText(this, "FAILED HUM", Toast.LENGTH_SHORT).show()
        }


        //GAS
        database = FirebaseDatabase.getInstance().getReference("Sensor")
        database.child("Gas").get().addOnSuccessListener {
            if(it.exists())
            {
                val gas:Int = it.value.toString().toInt()
                gasGlobal = gas
                if(gasGlobal > 700) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        sendNotification(this, "Alertă Gaz", "Nivel Gaz peste 700 ppm!", 1)
                    }
                    Toast.makeText(this, "Alertă Gaz", Toast.LENGTH_SHORT).show()
                }
                val ppm = " ppm"
                val gasset:String = gas.toString() + ppm
                binding.textViewGas.text = gasset
            }
        }.addOnFailureListener {
            Toast.makeText(this, "FAILED GAS", Toast.LENGTH_SHORT).show()
        }


        //DUST
        database = FirebaseDatabase.getInstance().getReference("Sensor")
        database.child("Dust").get().addOnSuccessListener {
            if(it.exists())
            {
                val dust:Float = it.value.toString().toFloat()
                val truncatedDust = String.format("%.2f", dust).toDouble()
                if(truncatedDust > 2) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        sendNotification(this, "Alertă Praf", "Particule mari de praf în zonă!", 2)
                    }
                    Toast.makeText(this, "Alertă Praf", Toast.LENGTH_SHORT).show()
                }
                particlesGlobal = truncatedDust
                val density = " ppm"
                val dustset:String = truncatedDust.toString() + density
                binding.textViewParticle.text = dustset
            }
        }.addOnFailureListener {
            Toast.makeText(this, "FAILED DUST", Toast.LENGTH_SHORT).show()
        }


        //RAIN
        database = FirebaseDatabase.getInstance().getReference("Sensor")
        database.child("Rain").get().addOnSuccessListener {
            if(it.exists())
            {
                var rain:Int = it.value.toString().toInt()
                if(rain == 1)
                {
                    rain = 0
                    binding.textViewPrecipitation.text = "Uscat"
                    binding.textViewPrecipitation.translationX = 80f
                    binding.textViewPrecipitation.translationY = -182f
                    binding.textViewRain.translationX = 120f
                }
                else
                {
                    //PRECIPITATION
                    if(temperatureGlobal <= 0.0)
                    {
                        binding.textViewPrecipitation.text = "Ninsoare"
                        binding.textViewPrecipitation.translationX = 30f
                        binding.textViewPrecipitation.translationY = -182f
                        binding.textViewRain.translationX = 100f
                    }
                    else if(temperatureGlobal in 0.1..4.0)
                    {
                        binding.textViewPrecipitation.text = "Lapovita/Grindina"
                        binding.textViewPrecipitation.translationX = 30f
                        binding.textViewPrecipitation.translationY = -220f
                        binding.textViewPrecipitation.textSize = 20f
                        binding.textViewRain.translationX = 100f
                    }
                    else if(temperatureGlobal > 4.0)
                    {
                        binding.textViewPrecipitation.text = "Ploaie"
                        binding.textViewPrecipitation.translationX = 80f
                        binding.textViewPrecipitation.translationY = -182f
                        binding.textViewRain.translationX = 100f
                    }
                }
                rainGlobal = rain
                val percent = " %"
                val rainset:String = rain.toString() + percent
                binding.textViewRain.text = rainset
            }
        }.addOnFailureListener {
            Toast.makeText(this, "FAILED RAIN", Toast.LENGTH_SHORT).show()
        }


        //LIGHT
        database = FirebaseDatabase.getInstance().getReference("Sensor")
        database.child("Light").get().addOnSuccessListener {
            if(it.exists())
            {
                val light:Int = it.value.toString().toInt()
                lightGlobal = light
                val lux = " lux"
                val lightset:String = light.toString() + lux
                binding.textViewLight.text = lightset
                if(light > 999)
                    binding.textViewLight.translationX = 6f
                else if(light in 100..999)
                    binding.textViewLight.translationX = 30f
                else
                    binding.textViewLight.translationX = 65f
            }
        }.addOnFailureListener {
            Toast.makeText(this, "FAILED LIGHT", Toast.LENGTH_SHORT).show()
        }


        //UV
        database = FirebaseDatabase.getInstance().getReference("Sensor")
        database.child("UV").get().addOnSuccessListener {
            if(it.exists())
            {
                val uv:Int = it.value.toString().toInt()
                if(uv > 7) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        sendNotification(
                            this,
                            "Indice UV ridicat",
                            "Limitați expunerea îndelungată!",
                            3
                        )
                    }
                    Toast.makeText(this, "Alertă UV", Toast.LENGTH_SHORT).show()
                }
                val index = "Index "
                val uvset:String = index + uv.toString()
                binding.textViewUV.text = uvset
            }
        }.addOnFailureListener {
            Toast.makeText(this, "FAILED UV", Toast.LENGTH_SHORT).show()
        }


        //PRESSURE MMHG
        database = FirebaseDatabase.getInstance().getReference("Sensor")
        database.child("Pressure MMHG").get().addOnSuccessListener {
            if(it.exists())
            {
                val prmmhg:Int = it.value.toString().toInt()
                mmhgGlobal = prmmhg
                val mmhg = " mmHG"
                val prmhgset:String = prmmhg.toString() + mmhg
                binding.textViewMMHG.text = prmhgset
            }
        }.addOnFailureListener {
            Toast.makeText(this, "FAILED MMHG", Toast.LENGTH_SHORT).show()
        }


        //PRESSURE PA
        database = FirebaseDatabase.getInstance().getReference("Sensor")
        database.child("Pressure PA").get().addOnSuccessListener {
            if(it.exists())
            {
                val prpa:Int = it.value.toString().toInt()
                paGlobal = prpa
                val pa = " Pa"
                if(paGlobal < 10000) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        sendNotification(
                            this,
                            "Scădere presiune atmosferică",
                            "Posibilă furtună în curând!",
                            4
                        )
                    }
                    Toast.makeText(this, "Alertă Presiune", Toast.LENGTH_SHORT).show()
                }
                val prpaset:String = prpa.toString() + pa
                binding.textViewPA.text = prpaset
            }
        }.addOnFailureListener {
            Toast.makeText(this, "FAILED PA", Toast.LENGTH_SHORT).show()
        }


        //SPEED
        database = FirebaseDatabase.getInstance().getReference("Sensor")
        database.child("Speed").get().addOnSuccessListener {
            if(it.exists())
            {
                val speed:Float = it.value.toString().toFloat()
                val ms = " m/s"
                val truncatedSpeed = String.format("%.2f", speed).toDouble()
                windGlobal = truncatedSpeed;
                val speedset:String = truncatedSpeed.toString() + ms
                binding.textViewSpeed.text = speedset
            }
        }.addOnFailureListener {
            Toast.makeText(this, "FAILED SPEED", Toast.LENGTH_SHORT).show()
        }


        //DIRECTION
        database = FirebaseDatabase.getInstance().getReference("Sensor")
        database.child("Compass").get().addOnSuccessListener {
            if(it.exists())
            {
                val compass:String = it.value.toString()
                binding.textViewDirection.text = compass
            }
        }.addOnFailureListener {
            Toast.makeText(this, "FAILED COMPASS", Toast.LENGTH_SHORT).show()
        }

        //STATUS SET
        if((currentTimeGlobal >= rasaritStart && currentTimeGlobal < rasaritEnd) || (currentTimeGlobal >= rasaritEnd && currentTimeGlobal < apusStart))
        {
            //ZIUA
            if(rainGlobal == 0)
            {
                val textViewPloaie = findViewById<TextView>(R.id.textViewStatusPloaie)
                textViewPloaie.text = "Fără \nPloaie"

                if (temperatureGlobal > 35 && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.cleandayhot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if (temperatureGlobal > 35 && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.cleandayhot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if (temperatureGlobal > 35 && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.cleandayhot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.cleandaywarm)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.cleandaywarm)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.cleandaywarm)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.cleandaycloudy)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.cleandaycloudy)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.cleandaycloudy)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if (temperatureGlobal < 0 && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.colddayclear)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if (temperatureGlobal < 0 && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.colddayclear)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if (temperatureGlobal < 0 && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.colddayclear)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }
            }
            else if(rainGlobal in 0..30)
            {
                val textViewPloaie = findViewById<TextView>(R.id.textViewStatusPloaie)
                textViewPloaie.text = "Ploaie \nSlabă"

                if (temperatureGlobal > 35 && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.raindaylvl1)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if (temperatureGlobal > 35 && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.raindaylvl1)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if (temperatureGlobal > 35 && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.raindaylvl1)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.raindaylvl1)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.raindaylvl1)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.raindaylvl1)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.raindaylvl1)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.raindaylvl1)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.raindaylvl1)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if (temperatureGlobal < 0 && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.colddayrain)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if (temperatureGlobal < 0 && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.colddayrain)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if (temperatureGlobal < 0 && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.colddayrain)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }
            }
            else if(rainGlobal in 30..70)
            {
                val textViewPloaie = findViewById<TextView>(R.id.textViewStatusPloaie)
                textViewPloaie.text = "Ploaie \nPuternică"

                if (temperatureGlobal > 35 && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.raindaylvl2)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if (temperatureGlobal > 35 && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.raindaylvl2)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if (temperatureGlobal > 35 && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.raindaylvl2)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.raindaylvl2)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.raindaylvl2)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.raindaylvl2)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.raindaylvl2)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.raindaylvl2)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.raindaylvl2)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if (temperatureGlobal < 0 && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.colddayrain)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if (temperatureGlobal < 0 && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.colddayrain)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if (temperatureGlobal < 0 && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.colddayrain)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }
            }
            else if(rainGlobal in 70..100)
            {
                val textViewPloaie = findViewById<TextView>(R.id.textViewStatusPloaie)
                textViewPloaie.text = "Ploaie \nTorențială"

                if (temperatureGlobal > 35 && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.raindaylvl3)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if (temperatureGlobal > 35 && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.raindaylvl3)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if (temperatureGlobal > 35 && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.raindaylvl3)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.raindaylvl3)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.raindaylvl3)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.raindaylvl3)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.raindaylvl3)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.raindaylvl3)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.raindaylvl3)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if (temperatureGlobal < 0 && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.colddayrain)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if (temperatureGlobal < 0 && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.colddayrain)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if (temperatureGlobal < 0 && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.colddayrain)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }
            }
        }
        else if((currentTimeGlobal >= noapte && currentTimeGlobal < rasaritStart) || (currentTimeGlobal >= apusStart && currentTimeGlobal < apusEnd))
        {
            //NOAPTEA
            if(rainGlobal == 0)
            {
                val textViewPloaie = findViewById<TextView>(R.id.textViewStatusPloaie)
                textViewPloaie.text = "Fără \nPloaie"

                if (temperatureGlobal > 35 && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.cleannighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if (temperatureGlobal > 35 && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.cleannighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if (temperatureGlobal > 35 && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.cleannighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.cleannightwarm)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.cleannightwarm)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.cleannightwarm)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.cleannightcloudy)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.cleannightcloudy)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.cleannightcloudy)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if (temperatureGlobal < 0 && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.coldnight)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if (temperatureGlobal < 0 && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.coldnight)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if (temperatureGlobal < 0 && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.coldnight)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }
            }
            else if(rainGlobal in 0..30)
            {
                val textViewPloaie = findViewById<TextView>(R.id.textViewStatusPloaie)
                textViewPloaie.text = "Ploaie \nSlabă"

                if (temperatureGlobal > 35 && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if (temperatureGlobal > 35 && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if (temperatureGlobal > 35 && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnightcloudy)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnightcloudy)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnightcloudy)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if (temperatureGlobal < 0 && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.coldnightrain)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if (temperatureGlobal < 0 && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.coldnightrain)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if (temperatureGlobal < 0 && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.coldnightrain)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }
            }
            else if(rainGlobal in 30..70)
            {
                val textViewPloaie = findViewById<TextView>(R.id.textViewStatusPloaie)
                textViewPloaie.text = "Ploaie \nPuternică"

                if (temperatureGlobal > 35 && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if (temperatureGlobal > 35 && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if (temperatureGlobal > 35 && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnightcloudy)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnightcloudy)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnightcloudy)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if (temperatureGlobal < 0 && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.coldnightrain)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if (temperatureGlobal < 0 && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.coldnightrain)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if (temperatureGlobal < 0 && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.coldnightrain)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }
            }
            else if(rainGlobal in 70..100)
            {
                val textViewPloaie = findViewById<TextView>(R.id.textViewStatusPloaie)
                textViewPloaie.text = "Ploaie \nTorențială"

                if (temperatureGlobal > 35 && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if (temperatureGlobal > 35 && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if (temperatureGlobal > 35 && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnightcloudy)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnightcloudy)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnightcloudy)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if (temperatureGlobal < 0 && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.coldnightrain)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if (temperatureGlobal < 0 && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.coldnightrain)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if (temperatureGlobal < 0 && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.coldnightrain)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }
            }
        }
        else
        {
            //NOAPTEA
            if(rainGlobal == 0)
            {
                val textViewPloaie = findViewById<TextView>(R.id.textViewStatusPloaie)
                textViewPloaie.text = "Fără \nPloaie"

                if (temperatureGlobal > 35 && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.cleannighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if (temperatureGlobal > 35 && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.cleannighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if (temperatureGlobal > 35 && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.cleannighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.cleannightwarm)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.cleannightwarm)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.cleannightwarm)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.cleannightcloudy)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.cleannightcloudy)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.cleannightcloudy)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if (temperatureGlobal < 0 && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.coldnight)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if (temperatureGlobal < 0 && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.coldnight)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if (temperatureGlobal < 0 && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.coldnight)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }
            }
            else if(rainGlobal in 0..30)
            {
                val textViewPloaie = findViewById<TextView>(R.id.textViewStatusPloaie)
                textViewPloaie.text = "Ploaie \nSlabă"

                if (temperatureGlobal > 35 && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if (temperatureGlobal > 35 && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if (temperatureGlobal > 35 && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnightcloudy)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnightcloudy)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnightcloudy)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if (temperatureGlobal < 0 && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.coldnightrain)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if (temperatureGlobal < 0 && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.coldnightrain)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if (temperatureGlobal < 0 && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.coldnightrain)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }
            }
            else if(rainGlobal in 30..70)
            {
                val textViewPloaie = findViewById<TextView>(R.id.textViewStatusPloaie)
                textViewPloaie.text = "Ploaie \nPuternică"

                if (temperatureGlobal > 35 && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if (temperatureGlobal > 35 && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if (temperatureGlobal > 35 && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnightcloudy)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnightcloudy)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnightcloudy)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if (temperatureGlobal < 0 && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.coldnightrain)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if (temperatureGlobal < 0 && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.coldnightrain)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if (temperatureGlobal < 0 && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.coldnightrain)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }
            }
            else if(rainGlobal in 70..100)
            {
                val textViewPloaie = findViewById<TextView>(R.id.textViewStatusPloaie)
                textViewPloaie.text = "Ploaie \nTorențială"

                if (temperatureGlobal > 35 && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if (temperatureGlobal > 35 && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if (temperatureGlobal > 35 && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if ((temperatureGlobal > 20 && temperatureGlobal <= 35) && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnighthot)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnightcloudy)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnightcloudy)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if ((temperatureGlobal > 0 && temperatureGlobal <= 20) && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.rainnightcloudy)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }

                else if (temperatureGlobal < 0 && windGlobal == 0.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.coldnightrain)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Fără \nVânt"
                }
                else if (temperatureGlobal < 0 && (windGlobal > 0.0 && windGlobal <= 2.0))
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.coldnightrain)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nSlab"
                }
                else if (temperatureGlobal < 0 && windGlobal > 2.0)
                {
                    val roundedImageView = findViewById<RoundedImageView>(R.id.top_view)
                    roundedImageView.setImageResource(R.drawable.coldnightrain)
                    val textViewVant = findViewById<TextView>(R.id.textViewStatusVant)
                    textViewVant.text = "Vânt \nPuternic"
                }
            }
        }

        setupChart()
    }
}