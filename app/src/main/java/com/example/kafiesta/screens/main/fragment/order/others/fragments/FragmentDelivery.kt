package com.example.kafiesta.screens.main.fragment.order.others.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kafiesta.R
import com.example.kafiesta.constants.DialogTag
import com.example.kafiesta.constants.OrderConst
import com.example.kafiesta.constants.UserConst
import com.example.kafiesta.databinding.FragmentDeliveryBinding
import com.example.kafiesta.domain.OrderBaseDomain
import com.example.kafiesta.screens.main.fragment.order.OrderStatusEnum
import com.example.kafiesta.screens.main.fragment.order.others.adapter.OrderAdapter
import com.example.kafiesta.screens.main.fragment.order.OrderViewModel
import com.example.kafiesta.screens.main.fragment.order.others.dialogs.DialogOrderDetails
import com.example.kafiesta.utilities.decorator.DividerItemDecoration
import com.example.kafiesta.utilities.getDialog
import com.example.kafiesta.utilities.helpers.OrderRecyclerClick
import com.example.kafiesta.utilities.helpers.RecyclerClick
import com.example.kafiesta.utilities.helpers.SharedPrefs
import com.example.kafiesta.utilities.helpers.getSecurePrefs
import com.example.kafiesta.utilities.extensions.showToast
import com.trackerteer.taskmanagement.utilities.extensions.gone
import com.trackerteer.taskmanagement.utilities.extensions.visible
import kotlinx.android.synthetic.main.fragment_pending.view.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * A placeholder fragment containing a simple view.
 */
class FragmentDelivery : Fragment() {

    private lateinit var binding: FragmentDeliveryBinding
    private val orderViewModel: OrderViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
            .create(OrderViewModel::class.java)
    }

    private lateinit var mAdapter: OrderAdapter
    private var userId = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return initBinding(inflater, container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        userId = SharedPrefs(getSecurePrefs(requireContext())).getString(UserConst.USER_ID)!!.toLong()
        initConfig()
    }

    override fun onResume() {
        super.onResume()
        initRequest()
    }

    private fun initBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_delivery,
            container,
            false
        )
        binding.lifecycleOwner = this
        binding.orderViewModel = orderViewModel
        return binding.root
    }

    private fun initConfig() {
        initAdapter()
        initViews()
        initLiveData()
    }

    private fun initAdapter() {
        mAdapter = OrderAdapter(
            context = requireContext(),
            onClickCallBack = RecyclerClick(
            click = {
                val a = it as OrderBaseDomain
                val dialog = DialogOrderDetails(
                    status = a.order.status,
                    model = a,
                    onClickCallBack = OrderRecyclerClick(
                        accept = {},
                        move_delivery = {},
                        move_completed = { model ->
                            val order = model as OrderBaseDomain
                            orderViewModel.orderMoveStatus(
                                order.order.id,
                                OrderConst.ORDER_COMPLETED,
                                ""
                            )
                            showToast(getString(R.string.dialog_message_order_completed, order.order.orderId))
                        },
                        reject = {},
                        proofURL = {}
                    ),
                    activity = requireActivity()
                )
                dialog.show(requireActivity().supportFragmentManager,
                    DialogTag.DIALOG_ORDER_DETAILS)
            }
        ))
    }

    private fun initViews() {
        binding.root.recycler_view.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
            addItemDecoration(
                DividerItemDecoration(
                    context,
                    R.drawable.list_divider_decoration
                )
            )
        }.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1)) {

                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })

        binding.swipeRefreshLayout.setOnRefreshListener {
            initRequest()
        }
    }

    private fun initLiveData() {
        orderViewModel.apply {
            orderDeliveryList.observe(viewLifecycleOwner) {
                when {
                    it.isNotEmpty() -> {
                        binding.layoutEmptyTask.root.gone()
                        it.forEach { model ->
                            mAdapter.addData(model)
                        }
                    }
                    mAdapter.itemCount == 0 -> {
                        binding.layoutEmptyTask.root.visible()
                    }
                    else -> {
                        binding.layoutEmptyTask.root.gone()
                    }
                }
            }

            isLoading.observe(viewLifecycleOwner) {
                binding.swipeRefreshLayout.isRefreshing = it

            }

            orderStatus.observe(viewLifecycleOwner) {
                (getDialog(requireActivity(), DialogTag.DIALOG_ORDER_DETAILS) as DialogOrderDetails?)?.dismiss()
                showToast(it)
                initRequest()
            }
        }
    }

    fun initRequest() {
        mAdapter.clearAdapter()
        orderViewModel.getAllOrderList(
            orderStatusEnum = OrderStatusEnum.DELIVERY,
//            length = 10,
//            start = 0,
            search = "",
            merchant_user_id = 5,
            date_from = getDateNow(),
            date_to = getDateNow())
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding
    }

    private fun getDateNow() :  String{
        val current = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime.now()
        } else {
            TODO("VERSION.SDK_INT < O")
        }

        val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
        } else {
            TODO("VERSION.SDK_INT < O")
        }

        val formatted = current.format(formatter)
        println("Current Date and Time is: $formatted")
        return formatted
    }


}
