package com.upriseus.remindme.layout_features

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TimePicker
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.upriseus.remindme.R
import com.upriseus.remindme.features.reminders.Reminders
import java.util.*


// INFO : Based on this project : https://github.com/Schalex1998/Android-FullScreen-Dialog

class FullScreenDialog(private val updated_reminder: Reminders? = null) : DialogFragment(), OnMapReadyCallback {
    private lateinit var toolbar : MaterialToolbar
    private lateinit var dateContent : TextInputEditText
    private lateinit var listener : DialogListener
    private lateinit var timePicker  : TimePicker
    private lateinit var message  : TextInputEditText
    private lateinit var recurringReminder : SwitchMaterial
    private lateinit var weeklyReminder : LinearLayout
    private lateinit var date : TextInputLayout
    private lateinit var notifReminder : SwitchMaterial
    private lateinit var timebasedReminder : SwitchMaterial
    private lateinit var mapView : MapView
    private lateinit var map : GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var location : Marker? = null
    private var circle : Circle? = null
    private var recurring = false
    private var selectedDay = -1
    private var selectedMonth = -1
    private var selectedYear = -1
    private var isUpdated = false
    private val c = Calendar.getInstance()
    private var isMapOpen = false
    private var defaultLayoutParams : ConstraintLayout.LayoutParams? = null

    //allow static use
    companion object disp {
        val TAG = "full_screen_dialog"
        fun display(fragmentManager: FragmentManager, listener: DialogListener? = null): FullScreenDialog {
            val fullScreenDialog = FullScreenDialog()
            if(listener != null){
                fullScreenDialog.listener = listener
            }
            fullScreenDialog.show(fragmentManager, TAG)
            return fullScreenDialog
        }

        fun displayUpdate(
            fragmentManager: FragmentManager,
            listener: DialogListener? = null,
            updated_reminder: Reminders
        ) : FullScreenDialog{
            val fullScreenDialog = FullScreenDialog(updated_reminder)
            fullScreenDialog.isUpdated = true
            if(listener != null){
                fullScreenDialog.listener = listener
            }
            fullScreenDialog.show(fragmentManager, TAG)
            return fullScreenDialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.RemindME_FullScreenDialog)
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view: View = inflater.inflate(R.layout.full_screen_dialog, container, false)
        toolbar = view.findViewById(R.id.toolbar)

        // INFO : challenge
        mapView = view.findViewById<MapView>(R.id.map)
        MapsInitializer.initialize(activity)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)
        defaultLayoutParams = mapView.layoutParams as ConstraintLayout.LayoutParams?


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        timePicker = view.findViewById(R.id.time_picker)
        timePicker.setIs24HourView(true)

        date = view.findViewById<TextInputLayout>(R.id.date)
        dateContent = view.findViewById(R.id.date_content)
        date.setEndIconOnClickListener {
            selectDate()
        }

        recurringReminder = view.findViewById(R.id.recurring_reminders)
        weeklyReminder = view.findViewById(R.id.weekly_reminders)

        timebasedReminder = view.findViewById(R.id.time_based)

        notifReminder = view.findViewById(R.id.notif_reminder)

        message = view.findViewById(R.id.message_text)
        if(updated_reminder?.reminderTime != null){
            c.timeInMillis = updated_reminder.reminderTime
            selectedYear = c.get(Calendar.YEAR)
            selectedMonth = c.get(Calendar.MONTH)
            selectedDay = c.get(Calendar.DAY_OF_MONTH)
            message.setText(updated_reminder.message)
            timePicker.hour = c.get(Calendar.HOUR_OF_DAY)
            timePicker.minute = c.get(Calendar.MINUTE)
        }else{
            selectedYear = c.get(Calendar.YEAR)
            selectedMonth = c.get(Calendar.MONTH)
            selectedDay = c.get(Calendar.DAY_OF_MONTH)
        }
        dateContent.setText(DateFormat.format("dd/MM/yyyy", c).toString())
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setNavigationOnClickListener { dismiss() }
        if(updated_reminder != null){
            toolbar.title = "Update Reminder"
        }else{
            toolbar.title = "New Reminder"
        }
        toolbar.inflateMenu(R.menu.save_dialog)

        recurringReminder.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                timePicker.visibility = View.GONE
                date.visibility = View.GONE
                mapView.visibility = View.GONE
                weeklyReminder.visibility = View.VISIBLE
                recurring = true
            }else{
                timePicker.visibility = View.VISIBLE
                date.visibility = View.VISIBLE
                mapView.visibility = View.VISIBLE
                weeklyReminder.visibility = View.GONE
                recurring = false
            }
        }

        timebasedReminder.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                timePicker.visibility = View.VISIBLE
                date.visibility = View.VISIBLE
            }else{
                timePicker.visibility = View.GONE
                date.visibility = View.GONE
            }
        }


        toolbar.setOnMenuItemClickListener {
            when(it.itemId){
                // TODO : Verification des champs vides et message d'erreur
                R.id.action_save -> {
                    val reminderData = mutableMapOf<String, String>()
                    reminderData["hours"] = timePicker.hour.toString()
                    reminderData["minutes"] = timePicker.minute.toString()
                    reminderData["message"] = message.text.toString()
                    reminderData["day"] = selectedDay.toString()
                    reminderData["month"] = selectedMonth.toString()
                    reminderData["year"] = selectedYear.toString()
                    reminderData["recurring"] = recurring.toString()
                    reminderData["notif"] = notifReminder.isChecked.toString()
                    reminderData["locationx"] = location?.position?.longitude.toString()
                    reminderData["locationy"] = location?.position?.latitude.toString()
                    reminderData["timeBased"] = timebasedReminder.isChecked.toString()
                    if (updated_reminder != null) {
                        listener.userUpdatedAValue(reminderData, updated_reminder)
                    } else {
                        listener.userSelectedAValue(reminderData)
                    }
                }
                else -> dismiss()
            }
            dismiss()
            true
        }
    }

    private fun selectDate(){
        val listener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            selectedYear = year
            selectedMonth = month
            selectedDay = dayOfMonth
            c.set(selectedYear, selectedMonth, selectedDay)
            dateContent.setText(DateFormat.format("dd/MM/yyyy", c).toString())
        }
        val datePickerDialog = DatePickerDialog(
            requireActivity(),
            listener,
            selectedYear,
            selectedMonth,
            selectedDay
        )
        datePickerDialog.show()
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap?) {
        if (p0 != null) {
            map = p0
            enableMyLocation()
            // INFO : https://stackoverflow.com/questions/36785542/how-to-change-the-position-of-my-location-button-in-google-maps-using-android-st
            if (mapView.findViewById<View?>("1".toInt()) != null) {
                val locationButton = (mapView.findViewById<View>("1".toInt()).parent as View).findViewById<View>("2".toInt())
                val layoutParams = locationButton.layoutParams as RelativeLayout.LayoutParams
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                layoutParams.setMargins(0, 0, 30, 30)
                locationButton.setOnClickListener {
                    location?.remove()
                }
            }

            fusedLocationClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                    with(map) {
                        val latLng = LatLng(it.latitude, it.longitude)
                        moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))
                    }
                }
            }

            // INFO : challenge
            map.setOnMapClickListener {
                if(!isMapOpen){
                    val params = ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.MATCH_PARENT
                    )
                    mapView.layoutParams = params
                    isMapOpen = true
                }else{
                    mapView.layoutParams = defaultLayoutParams
                    isMapOpen = false
                }
            }

            map.setOnMapLongClickListener {
                location?.remove()
                circle?.remove()
                location = map.addMarker(
                    MarkerOptions().position(it)
                        .title("Reminder location")
                )
                location?.showInfoWindow()
                circle = map.addCircle(
                    CircleOptions()
                        .center(it)
                        .strokeColor(Color.argb(50, 70, 70, 70))
                        .fillColor(Color.argb(70, 150, 150, 150))
                        .radius(100.toDouble())
                )
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 13f))
            }
        }
    }


    private fun isPermissionGranted(): Boolean {

        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == 1) {
            if(grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }
}