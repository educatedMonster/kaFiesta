package com.example.kafiesta.screens.test.test_order

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.kafiesta.R
import com.example.kafiesta.constants.DialogTag
import com.example.kafiesta.constants.IntentConst
import com.example.kafiesta.constants.OrderConst
import com.example.kafiesta.constants.OrderConst.ORDER_COMPLETED
import com.example.kafiesta.constants.OrderConst.ORDER_DELIVERY
import com.example.kafiesta.constants.OrderConst.ORDER_PENDING
import com.example.kafiesta.constants.OrderConst.ORDER_PREPARING
import com.example.kafiesta.constants.UserConst
import com.example.kafiesta.databinding.TestOrderFragmentBinding
import com.example.kafiesta.domain.OrderBaseDomain
import com.example.kafiesta.screens.image_viewer.ImageViewerActivity
import com.example.kafiesta.screens.image_viewer.dialog.RejectOrderDialog
import com.example.kafiesta.screens.main.fragment.order.OrderStatusEnum
import com.example.kafiesta.screens.main.fragment.order.OrderViewModel
import com.example.kafiesta.screens.main.fragment.order.others.dialogs.DialogOrderDetails
import com.example.kafiesta.screens.test.interfaces_test_order.Page
import com.example.kafiesta.screens.test.interfaces_test_order.RefreshOrderListener
import com.example.kafiesta.screens.test.interfaces_test_order.TestOrderAdapter
import com.example.kafiesta.screens.test.interfaces_test_order.ViewPagerRecyclerAdapter
import com.example.kafiesta.screens.test.pages.OrderCompletedPage
import com.example.kafiesta.screens.test.pages.OrderDeliveryPage
import com.example.kafiesta.screens.test.pages.OrderPendingPage
import com.example.kafiesta.screens.test.pages.OrderPreparingPage
import com.example.kafiesta.utilities.extensions.showToast
import com.example.kafiesta.utilities.getDateNow
import com.example.kafiesta.utilities.getDialog
import com.example.kafiesta.utilities.helpers.OrderRecyclerClick
import com.example.kafiesta.utilities.helpers.RecyclerClick
import com.example.kafiesta.utilities.helpers.SharedPrefs
import com.example.kafiesta.utilities.helpers.getSecurePrefs
import com.example.kafiesta.utilities.tab.CustomTabLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.trackerteer.taskmanagement.utilities.extensions.gone
import com.trackerteer.taskmanagement.utilities.extensions.visible

open class TestOrderFragment : Fragment(), RefreshOrderListener {
    private val orderViewModel: OrderViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
            .create(OrderViewModel::class.java)
    }

    private var userId = 0L
    private lateinit var mSharedPrefs: SharedPrefs
    private lateinit var binding: TestOrderFragmentBinding
    private var mAdapter: TestOrderAdapter? = null
    private lateinit var viewPagerAdapter: ViewPagerRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return initDataBinding(inflater, container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mSharedPrefs = SharedPrefs(getSecurePrefs(requireActivity().application))
        userId = mSharedPrefs.getString(UserConst.USER_ID)!!.toLong()
        initConfig()
    }

    override fun onResume() {
        super.onResume()
        initRequest()
    }

    override fun onSwipeRefreshOrder(orderPosition: Int, orderTitle: String) {
        initRefreshRequest(orderPosition, orderTitle)
    }

    private fun initDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.test_order_fragment, container,
            false
        )
        binding.lifecycleOwner = this
        binding.orderViewModel = orderViewModel
        return binding.root
    }

    fun initConfig() {
        initAdapter()
        initViewPager()
        initTabLayout()
        initRequest()
        initLiveData()
        initRequest()
    }

    private fun initViewPager() {
        val pageList = ArrayList<Page>()
        pageList.add(OrderPendingPage())
        pageList.add(OrderPreparingPage())
        pageList.add(OrderDeliveryPage())
        pageList.add(OrderCompletedPage())

        viewPagerAdapter = ViewPagerRecyclerAdapter(
            requireActivity(),
            pageList,
            mAdapter!!.onClickCallBack
        )

        viewPagerAdapter.setRefreshOrderListener(this)

        binding.viewPager.apply {
            offscreenPageLimit = 4
            adapter = viewPagerAdapter
        }
    }

    private fun initTabLayout() {
        val titleList = listOf(
            R.string.tab_text_1,
            R.string.tab_text_2,
            R.string.tab_text_3,
            R.string.tab_text_4)
//        val iconList = listOf(
//        R.drawable.ic_order_pending,
//            R.drawable.ic_order_prepare,
//            R.drawable.ic_order_delivery,
//            R.drawable.ic_order_completed)
        binding.tabLayout.setupWithViewPager(binding.viewPager,
            titleList,
            null,
            CustomTabLayout.TYPE_YELLOW_BG)
    }

    private fun initLiveData() {
        orderViewModel.apply {
            orderList.observe(viewLifecycleOwner) {
                // for some reason there is a chance that the viewPagerAdapter
                // is not yet fully created
                if (viewPagerAdapter.getAdaptersSize() == 0) {
                    return@observe
                }

                val list = it as ArrayList<OrderBaseDomain>
                val pendingList = arrayListOf<OrderBaseDomain>()
                val preparingList = arrayListOf<OrderBaseDomain>()
                val deliveryList = arrayListOf<OrderBaseDomain>()
                val completedList = arrayListOf<OrderBaseDomain>()

                list.forEach { orders ->
                    val tempList = when (orders.order.status) {
                        ORDER_PENDING -> pendingList
                        ORDER_PREPARING -> preparingList
                        ORDER_DELIVERY -> deliveryList
                        ORDER_COMPLETED -> completedList
                        else -> pendingList
                    }
                    tempList.add(orders)
                }

                viewPagerAdapter.resetList(
                    0,
                    pendingList,
                    ORDER_PENDING
                )

                viewPagerAdapter.resetList(
                    1,
                    preparingList,
                    ORDER_PREPARING
                )

                viewPagerAdapter.resetList(
                    2,
                    deliveryList,
                    ORDER_DELIVERY
                )

                viewPagerAdapter.resetList(
                    3,
                    completedList,
                    ORDER_COMPLETED
                )
            }

            orderStatus.observe(viewLifecycleOwner) {
                (getDialog(requireActivity(),
                    DialogTag.DIALOG_ORDER_DETAILS) as DialogOrderDetails?)?.dismiss()
                (getDialog(requireActivity(),
                    DialogTag.DIALOG_REJECT_REMARK) as RejectOrderDialog?)?.dismiss()
                initRequest()
            }

            certainOrderFormState.observe(viewLifecycleOwner) {
                binding.viewPager.findViewWithTag<SwipeRefreshLayout>(it.certainOrderStatus + "swipe")
                    .isRefreshing = it.isLoading

                val shimmer =
                    binding.viewPager.findViewWithTag<ShimmerFrameLayout>(it.certainOrderStatus + "shimmer")
                val recycler =
                    binding.viewPager.findViewWithTag<RecyclerView>(it.certainOrderStatus + "recycler")
                val empty =
                    binding.viewPager.findViewWithTag<View>(it.certainOrderStatus + "empty")

                if (it.isLoading) {
                    shimmer.visible()
                    shimmer.startShimmer()
                    recycler.gone()
                    empty.gone()
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        shimmer.gone()
                        shimmer.stopShimmer()
                        recycler.visible()
                        if (viewPagerAdapter.getAdapterItemList(it.orderPosition) != 0) {
                            viewPagerAdapter.resetList(
                                it.orderPosition,
                                it.list as ArrayList<OrderBaseDomain>,
                                it.orderTitle
                            )
                            empty.gone()
                        } else {
                            empty.visible()
                        }
                    }, 500)
                }
            }
        }
    }

    private fun initRefreshRequest(orderPosition: Int, orderTitle: String) {
        orderViewModel.fetchCertainOrderStatus(
            orderPosition = orderPosition,
            orderTitle = orderTitle,
            orderStatusEnum = getPageEnum(orderTitle)!!,
            search = "",
            merchant_user_id = 5,
            date_from = getDateNow(),
            date_to = getDateNow())
    }

    fun initRequest() {
        orderViewModel.getAllOrderList(
            orderStatusEnum = OrderStatusEnum.ALL,
            search = "",
            merchant_user_id = userId,
            date_from = getDateNow(),
            date_to = getDateNow())
    }

    private fun getPageEnum(certainTask: String): OrderStatusEnum? {
        return when (certainTask) {
            ORDER_PENDING -> OrderStatusEnum.PENDING
            ORDER_PREPARING -> OrderStatusEnum.PREPARING
            ORDER_DELIVERY -> OrderStatusEnum.DELIVERY
            ORDER_COMPLETED -> OrderStatusEnum.COMPLETED
            else -> null
        }
    }

    private fun initAdapter() {
        mAdapter = TestOrderAdapter(
            context = requireContext(),
            onClickCallBack = RecyclerClick(
                click = {
                    val model = it as OrderBaseDomain
                    val dialog = DialogOrderDetails(
                        status = model.order.status,
                        model = model,
                        onClickCallBack = OrderRecyclerClick(
                            accept = {
                                orderViewModel.orderMoveStatus(
                                    model.order.id,
                                    ORDER_PREPARING,
                                    ""
                                )
                                showToast(getString(R.string.dialog_message_order_preparing,
                                    model.order.orderId))
                            },
                            move_delivery = {
                                orderViewModel.orderMoveStatus(
                                    model.order.id,
                                    ORDER_DELIVERY,
                                    ""
                                )
                                showToast(getString(R.string.dialog_message_order_preparing,
                                    model.order.orderId))
                            },
                            move_completed = {
                                orderViewModel.orderMoveStatus(
                                    model.order.id,
                                    ORDER_COMPLETED,
                                    ""
                                )
                                showToast(getString(R.string.dialog_message_order_preparing,
                                    model.order.orderId))
                            },
                            reject = {
                                showWarningRejectDialog(model.order.id)
                            },
                            proofURL = {
                                val intent =
                                    Intent(requireContext(), ImageViewerActivity::class.java)
                                intent.putExtra(IntentConst.ORDER_ID, model.order.id)
                                startActivity(intent)
                                requireActivity().overridePendingTransition(R.anim.enter_from_bottom,
                                    R.anim.stay)

                                (getDialog(requireActivity(),
                                    DialogTag.DIALOG_ORDER_DETAILS) as DialogOrderDetails?)?.dismiss()
                            }
                        ),
                        activity = requireActivity()
                    )
                    dialog.show(requireActivity().supportFragmentManager,
                        DialogTag.DIALOG_ORDER_DETAILS)
                }
            ))
    }

    private fun showWarningRejectDialog(orderId: Long) {
        RejectOrderDialog(
            listener = object : RejectOrderDialog.Listener {
                override fun onRejectOrder(remark: String) {
                    orderViewModel.orderMoveStatus(
                        orderId,
                        OrderConst.ORDER_REJECTED,
                        remark
                    )
                }
            }
        ).show(requireActivity().supportFragmentManager, DialogTag.DIALOG_REJECT_REMARK)
    }
}