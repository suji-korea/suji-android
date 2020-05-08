package com.suji.android.suji_android.food

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.suji.android.suji_android.R
import com.suji.android.suji_android.adapter.FoodListAdapter
import com.suji.android.suji_android.database.model.Food
import com.suji.android.suji_android.helper.Utils
import com.suji.android.suji_android.listener.ItemClickListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.food_create_dialog.view.*
import kotlinx.android.synthetic.main.food_fragment.view.*
import kotlinx.android.synthetic.main.submenu_layout.view.*

class FoodFragment : Fragment() {
    private lateinit var adapter: FoodListAdapter
    private val foodViewModel: FoodViewModel by lazy {
        ViewModelProvider(this).get(FoodViewModel::class.java)
    }
    private val dialogView: View by lazy {
        LayoutInflater.from(context).inflate(R.layout.food_create_dialog, null, false)
    }
    private val disposeBag = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        initView()
        val view = inflater.inflate(R.layout.food_fragment, container, false)

        adapter = FoodListAdapter(listener)
        view.main_food_list.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        view.main_food_list.adapter = adapter
        view.create_food.setOnClickListener(createFood)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposeBag.dispose()
    }

    private fun initView() {
        foodViewModel.getAllFood()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { adapter.setItems(it) },
                { e -> e.printStackTrace() }
            ).addTo(disposeBag)
    }

    private val createFood: View.OnClickListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            when (v!!.id) {
                R.id.create_food -> {
                    AlertDialog.Builder(activity, R.style.AppTheme_AppCompat_CustomDialog)
                        .setPositiveButton("만들기", object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                val subMenuList: ArrayList<Food> = ArrayList()
                                val foodName: String = dialogView.create_menu_edit_name.text.toString()
                                val foodPrice: String = dialogView.create_menu_edit_price.text.toString()

                                dialogView.create_menu_edit_name.text.clear()
                                dialogView.create_menu_edit_price.text.clear()

                                if (Utils.blankString(foodName) || Utils.blankString(foodPrice)) {
                                    Toast.makeText(context, "이름과 가격을 정확하게 입력해주세요!", Toast.LENGTH_SHORT).show()
                                    return
                                }

                                for (i in 0 until dialogView.create_sub_menu.childCount) {
                                    val subMenuView = dialogView.create_sub_menu.getChildAt(i)
                                    val subMenuName = subMenuView.create_submenu_name_edit_text.text.toString()
                                    val subMenuPrice = subMenuView.create_submenu_price_edit_text.text.toString()

                                    subMenuView.create_submenu_name_edit_text.text.clear()
                                    subMenuView.create_submenu_price_edit_text.text.clear()

                                    subMenuList.add(Food(subMenuName, subMenuPrice.toInt()))
                                }

                                if (subMenuList.size == 0) {
                                    foodViewModel.insert(Food(foodName, foodPrice.toInt()))
                                } else {
                                    foodViewModel.insert(Food(foodName, foodPrice.toInt(), subMenuList))
                                }

                                (dialogView.parent as ViewGroup).removeAllViews()
                                dialog!!.dismiss()
                            }
                        })
                        .setNegativeButton("취소", object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                (dialogView.parent as ViewGroup).removeAllViews()
                                (dialogView.create_sub_menu as ViewGroup).removeAllViews()
                                dialog!!.dismiss()
                            }
                        })
                        .setNeutralButton("부가 메뉴", null)
                        .setView(dialogView)
                        .show()
                        .let {
                            it.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(object : View.OnClickListener {
                                override fun onClick(v: View?) {
                                    val subMenuLayout = layoutInflater.inflate(R.layout.submenu_layout, dialogView.create_sub_menu, false)
                                    subMenuLayout.submenu_delete.setOnClickListener {
                                        dialogView.create_sub_menu.removeView(subMenuLayout)
                                    }
                                    dialogView.create_sub_menu.addView(subMenuLayout)
                                }
                            })

                            Utils.dialogReSizing(it)
                        }

                }
            }
        }
    }

    private val listener = object : ItemClickListener {
        override fun onItemClick(view: View, item: Any?) {
            when (view.id) {
                R.id.food_modify -> {
                    if (item is Food) {
                        dialogView.create_menu_edit_name.setText(item.name)
                        dialogView.create_menu_edit_price.setText(item.price.toString())

                        for (subItem in item.sub) {
                            val subMenuLayout = layoutInflater.inflate(R.layout.submenu_layout, dialogView.create_sub_menu, false)
                            subMenuLayout.submenu_delete.setOnClickListener {
                                dialogView.create_sub_menu.removeView(subMenuLayout)
                            }
                            subMenuLayout.create_submenu_name_edit_text.setText(subItem.name)
                            subMenuLayout.create_submenu_price_edit_text.setText(subItem.price.toString())
                            dialogView.create_sub_menu.addView(subMenuLayout)
                        }

                        AlertDialog.Builder(activity, R.style.AppTheme_AppCompat_CustomDialog)
                            .setPositiveButton("수정", object : DialogInterface.OnClickListener {
                                override fun onClick(dialog: DialogInterface?, which: Int) {
                                    val subMenuList: ArrayList<Food> = ArrayList()
                                    val foodName: String = dialogView.create_menu_edit_name.text.toString()
                                    val foodPrice: String = dialogView.create_menu_edit_price.text.toString()

                                    if (Utils.blankString(foodName) || Utils.blankString(foodPrice)) {
                                        Toast.makeText(context, "이름과 가격을 정확하게 입력해주세요!", Toast.LENGTH_SHORT).show()
                                        return
                                    }

                                    for (i in 0 until dialogView.create_sub_menu.childCount) {
                                        val subMenuView = dialogView.create_sub_menu.getChildAt(i)
                                        val subMenuName = subMenuView.create_submenu_name_edit_text.text.toString()
                                        val subMenuPrice = subMenuView.create_submenu_price_edit_text.text.toString()
                                        Log.i("subItem", subMenuName)

                                        subMenuView.create_submenu_name_edit_text.text.clear()
                                        subMenuView.create_submenu_price_edit_text.text.clear()

                                        subMenuList.add(Food(subMenuName, subMenuPrice.toInt()))
                                    }

                                    item.name = foodName
                                    item.price = foodPrice.toInt()

                                    if (item.sub.size != 0) {
                                        item.sub = subMenuList
                                    }

                                    foodViewModel.update(item)

                                    (dialogView.parent as ViewGroup).removeAllViews()
                                    (dialogView.create_sub_menu as ViewGroup).removeAllViews()
                                    dialog!!.dismiss()
                                }
                            })
                            .setNegativeButton("취소", object : DialogInterface.OnClickListener {
                                override fun onClick(dialog: DialogInterface?, which: Int) {
                                    (dialogView.parent as ViewGroup).removeAllViews()
                                    (dialogView.create_sub_menu as ViewGroup).removeAllViews()
                                    dialog!!.dismiss()
                                }
                            })
                            .setNeutralButton("부가 메뉴", null)
                            .setView(dialogView)
                            .show()
                            .let {
                                it.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(object : View.OnClickListener {
                                    override fun onClick(v: View?) {
                                        val subMenuLayout = layoutInflater.inflate(R.layout.submenu_layout, dialogView.create_sub_menu, false)
                                        subMenuLayout.submenu_delete.setOnClickListener {
                                            dialogView.create_sub_menu.removeView(subMenuLayout)
                                        }
                                        dialogView.create_sub_menu.addView(subMenuLayout)
                                    }
                                })

                                Utils.dialogReSizing(it)
                            }
                    }
                }
                R.id.food_delete -> {
                    (item as Food).let {
                        foodViewModel.delete(item)
                    }
                }
            }
        }
    }
}