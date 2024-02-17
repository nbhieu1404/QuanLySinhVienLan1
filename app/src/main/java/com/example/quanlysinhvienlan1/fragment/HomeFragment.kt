package com.example.quanlysinhvienlan1.fragment

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.quanlysinhvienlan1.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var btnAddClass: Button? = null
    private var prbAddClass: RelativeLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        mapping(view)
        clickEvent()
        return view
    }

    private fun mapping(view: View) {
        btnAddClass = view.findViewById(R.id.btn_AddClass)
        prbAddClass = view.findViewById(R.id.prb_AddClass)
    }


    private fun clickEvent() {
        btnAddClass?.setOnClickListener {
            addClass()
        }
    }


    private fun addClass() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.custom_dialog_add_class)

        val window = dialog.window ?: return
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val edtClassName = dialog.findViewById<EditText>(R.id.edt_ClassName)
        val edtMembersQuantity = dialog.findViewById<EditText>(R.id.edt_MembersQuantity)
        val btnAddClass = dialog.findViewById<Button>(R.id.btn_AddClass)
        val btnCancelAddClass = dialog.findViewById<Button>(R.id.btn_CancelAddClass)

        btnAddClass.setOnClickListener {
            val inputClassName = edtClassName.text.toString()
            val inputMembersQuantity = edtMembersQuantity.text.toString().toIntOrNull()

            when {
                inputClassName.isEmpty() ->
                    edtClassName.error = "Vui lòng nhập tên lớp"

                inputMembersQuantity == null ->
                    edtMembersQuantity.error = "Vui lòng nhập số lượng sinh viên"

                inputMembersQuantity < 1 ->
                    edtMembersQuantity.error = "Số lượng sinh viên phải lớn hơn 0"

                inputMembersQuantity > 100 ->
                    edtMembersQuantity.error = "Số lượng sinh viên tối đa = 100"

                inputClassName.length > 3 && inputMembersQuantity < 100 && inputMembersQuantity > 1 -> {
                    Toast.makeText(requireContext(), "Thành công", Toast.LENGTH_SHORT).show()
                }
            }
        }
        btnCancelAddClass.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun generateRandomClassCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}