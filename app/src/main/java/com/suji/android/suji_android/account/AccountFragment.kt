package com.suji.android.suji_android.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.suji.android.suji_android.R
import com.suji.android.suji_android.adapter.ProductListAdapter
import com.suji.android.suji_android.basic.BasicApp
import com.suji.android.suji_android.database.model.Sale
import com.suji.android.suji_android.databinding.AccountFragmentBinding
import com.suji.android.suji_android.helper.Constant
import java.text.DecimalFormat

class AccountFragment : Fragment() {
    private lateinit var binding: AccountFragmentBinding
    private lateinit var adapter: ProductListAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var soldItems: List<Sale>
    private var viewModel: AccountViewModel = AccountViewModel(BasicApp.app)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        initViewModel()
        binding = DataBindingUtil.inflate<AccountFragmentBinding>(inflater,
            R.layout.account_fragment,
            container,
            false)
            .apply {
                adapter = ProductListAdapter(Constant.ViewType.SOLD_VIEW)
                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                soldFragmentItems.layoutManager = layoutManager
                soldFragmentItems.adapter = adapter
            }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(AccountViewModel::class.java)
        viewModel.getAllSold().observe(this, object : Observer<List<Sale>> {
            override fun onChanged(t: List<Sale>?) {
                t?.let {
                    adapter.setItems(t)
                    soldItems = t

                    var sumPrice = 0
                    var cardMoney = 0
                    var cashMoney = 0
                    for (item in soldItems) {
                        sumPrice += item.price

                        if (item.pay == Constant.PayType.CARD) {
                            cardMoney += item.price
                        } else {
                            cashMoney += item.price
                        }
                    }
                    binding.foodSoldTotalPrice.text = DecimalFormat.getCurrencyInstance().format(sumPrice).toString()
                    binding.foodSoldCardPrice.text = DecimalFormat.getCurrencyInstance().format(cardMoney).toString()
                    binding.foodSoldCashPrice.text = DecimalFormat.getCurrencyInstance().format(cashMoney).toString()
                }

                executePendingBindings()
            }
        })
    }

    private fun executePendingBindings() {
        binding.executePendingBindings()
    }
}