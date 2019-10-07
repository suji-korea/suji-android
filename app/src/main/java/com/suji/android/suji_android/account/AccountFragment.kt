package com.suji.android.suji_android.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.suji.android.suji_android.R
import com.suji.android.suji_android.adapter.SoldListAdapter
import com.suji.android.suji_android.database.model.Sale
import com.suji.android.suji_android.helper.Constant
import com.suji.android.suji_android.helper.Utils
import com.suji.android.suji_android.listener.ItemClickListener
import kotlinx.android.synthetic.main.account_fragment.*
import kotlinx.android.synthetic.main.account_fragment.view.*
import java.text.DecimalFormat

class AccountFragment : Fragment() {
    private lateinit var adapter: SoldListAdapter
    private lateinit var items: List<Sale>
    private val viewModel: AccountViewModel by lazy {
        ViewModelProviders.of(this).get(AccountViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initViewModel()

        val view = inflater.inflate(R.layout.account_fragment, container, false)

        adapter = SoldListAdapter()
        view.sold_fragment_items.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        view.sold_fragment_items.adapter = adapter

//        binding.day = findDay
//        binding.week = findWeek
//        binding.month = findMonth
//        binding.all = findAll

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun computePrice(items: List<Sale>) {
        var sumPrice = 0
        var cardMoney = 0
        var cashMoney = 0

        for (item in items) {
            sumPrice += item.price

            if (item.pay == Constant.PayType.CARD) {
                cardMoney += item.price
            } else {
                cashMoney += item.price
            }
        }

        food_sold_total_price.text = DecimalFormat.getCurrencyInstance().format(sumPrice).toString()
        food_sold_card_price.text = DecimalFormat.getCurrencyInstance().format(cardMoney).toString()
        food_sold_cash_price.text = DecimalFormat.getCurrencyInstance().format(cashMoney).toString()
    }

    private fun initViewModel() {
        viewModel.deleteSoldDate(
            Utils.getStartDate(1),
            Utils.getEndDate(1)
        )
        viewModel.getAllSold().observe(this, object : Observer<List<Sale>> {
            override fun onChanged(t: List<Sale>?) {
                t?.let {
                    adapter.setItems(t)

                    computePrice(t)
                }
            }
        })
    }

    private val findDay: ItemClickListener = object : ItemClickListener {
        override fun onClick(item: Any?) {
            items = viewModel.findSaleOfDate(
                Utils.getStartTime(),
                Utils.getEndTime()
            )
            adapter.setItems(items)

            computePrice(items)
        }
    }

    private val findWeek: ItemClickListener = object : ItemClickListener {
        override fun onClick(item: Any?) {
            items = viewModel.findSaleOfDate(
                Utils.getStartWeek(),
                Utils.getEndWeek()
            )
            adapter.setItems(items)

            computePrice(items)
        }
    }

    private val findMonth: ItemClickListener = object : ItemClickListener {
        override fun onClick(item: Any?) {
            items = viewModel.findSaleOfDate(
                Utils.getStartDate(0),
                Utils.getEndDate(0)
            )
            adapter.setItems(items)

            computePrice(items)
        }
    }

    private val findAll: ItemClickListener = object : ItemClickListener {
        override fun onClick(item: Any?) {
            viewModel.getAllSold().observe(this@AccountFragment, object : Observer<List<Sale>> {
                override fun onChanged(t: List<Sale>?) {
                    t?.let {
                        adapter.setItems(t)

                        computePrice(t)
                    }
                }
            })
        }
    }
}