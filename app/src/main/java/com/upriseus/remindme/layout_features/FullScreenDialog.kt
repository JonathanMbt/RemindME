package com.upriseus.remindme.layout_features

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.appbar.MaterialToolbar
import com.upriseus.remindme.R

// Base on this project : https://github.com/Schalex1998/Android-FullScreen-Dialog

class FullScreenDialog : DialogFragment() {
    private lateinit var toolbar : MaterialToolbar


    //allow static use
    companion object disp {
        val TAG = "full_screen_dialog"
        fun display(fragmentManager: FragmentManager): FullScreenDialog {
            val fullScreenDialog = FullScreenDialog()
            fullScreenDialog.show(fragmentManager, TAG)
            return fullScreenDialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.RemindME_FullScreenDialog);
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view: View = inflater.inflate(R.layout.full_screen_dialog, container, false)
        toolbar = view.findViewById(R.id.toolbar)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setNavigationOnClickListener { dismiss() }
        toolbar.title = "Some Title"
        toolbar.inflateMenu(R.menu.save_dialog)
        toolbar.setOnMenuItemClickListener {
            dismiss()
            true
        }
    }
}